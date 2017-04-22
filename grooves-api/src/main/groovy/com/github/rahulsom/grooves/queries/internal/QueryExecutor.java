package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.GroovesException;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import groovy.transform.CompileStatic;
import groovy.transform.TypeCheckingMode;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @param <Aggregate>      The aggregate over which the query executes
 * @param <EventIdType>    The type of the Event's id field
 * @param <EventType>      The type of the Event
 * @param <SnapshotIdType> The type of the Snapshot's id field
 * @param <SnapshotType>   The type of the Snapshot
 * @author Rahul Somasunderam
 */
public class QueryExecutor<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        SnapshotIdType,
        SnapshotType extends BaseSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType>
        >
        implements Executor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {

    final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events The list of events
     * @return An Observable of forward only events
     */
    @Override
    public Observable<EventType> applyReverts(
            Observable<EventType> events) {

        return events.toList().flatMap(eventList -> {
            log.debug(
                    String.format("     EventList: %s",
                            eventList.stream().
                                    map(i -> i.getId().toString()).
                                    collect(Collectors.joining(", ")))
            );
            List<EventType> forwardEvents = new ArrayList<>();
            while (!eventList.isEmpty()) {
                EventType head = DefaultGroovyMethods.pop(eventList);
                if (head instanceof RevertEvent) {
                    final EventIdType revertedEventId = (EventIdType) ((RevertEvent) head).getRevertedEventId();
                    final Optional<EventType> revertedEvent = eventList.stream().filter(it -> it.getId().equals(revertedEventId)).findFirst();
                    if (revertedEvent.isPresent()) {
                        eventList.remove(revertedEvent.get());
                    } else {
                        throw new GroovesException("Cannot revert event that does not exist in unapplied list - " + String.valueOf(revertedEventId));
                    }

                } else {
                    forwardEvents.add(0, head);
                }

            }

            assert forwardEvents.stream().noneMatch(it -> it instanceof RevertEvent);

            return Observable.from(forwardEvents);
        });
    }

    @Override
    public Observable<SnapshotType> applyEvents(
            final BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> query,
            SnapshotType initialSnapshot, Observable<EventType> events,
            final List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList,
            final List<Aggregate> aggregates) {

        final AtomicBoolean stopApplyingEvents = new AtomicBoolean(false);

        return events.reduce(initialSnapshot, (snapshot, event) -> {
            if (!query.shouldEventsBeApplied(snapshot) || stopApplyingEvents.get()) {
                return snapshot;
            } else {
                log.debug("     -> Event: " + String.valueOf(event));

                if (event instanceof Deprecates) {
                    return applyDeprecates((Deprecates<Aggregate, EventIdType, EventType>) event, query, aggregates, deprecatesList);
                } else if (event instanceof DeprecatedBy) {
                    return applyDeprecatedBy((DeprecatedBy<Aggregate, EventIdType, EventType>) event, snapshot);
                } else {
                    String methodName = "apply" + event.getClass().getSimpleName();
                    EventApplyOutcome retval = callMethod(query, methodName, snapshot, event);
                    if (retval.equals(EventApplyOutcome.CONTINUE)) {
                        return snapshot;
                    } else if (retval.equals(EventApplyOutcome.RETURN)) {
                        stopApplyingEvents.set(true);
                        return snapshot;
                    } else {
                        throw new GroovesException("Unexpected value from calling \'" + methodName + "\'");
                    }

                }

            }

        });

    }

    @SuppressWarnings("GrMethodMayBeStatic")
    SnapshotType applyDeprecatedBy(
            final DeprecatedBy<Aggregate, EventIdType, EventType> event, SnapshotType snapshot) {
        Aggregate newAggregate = event.getDeprecator();
        snapshot.setDeprecatedBy(newAggregate);
        return snapshot;
    }

    SnapshotType applyDeprecates(
            final Deprecates<Aggregate, EventIdType, EventType> event,
            final BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> util,
            final List<Aggregate> aggregates,
            final List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList) {
        final SnapshotType newSnapshot = util.createEmptySnapshot();
        newSnapshot.setAggregate(event.getAggregate());

        Aggregate otherAggregate = event.getDeprecated();
        util.addToDeprecates(newSnapshot, otherAggregate);

        return util.findEventsForAggregates(DefaultGroovyMethods.plus(aggregates, event.getDeprecated())).
                filter(it -> !it.getId().equals(event.getId()) && !it.getId().equals(event.getConverse().getId())).
                toSortedList((a, b) -> a.getTimestamp().compareTo(b.getTimestamp())).
                flatMap(sortedEvents -> {
                    log.debug("     Sorted Events: [\n    " + DefaultGroovyMethods.join((Iterable) sortedEvents, ",\n    ") + "\n]");
                    Observable<EventType> forwardEventsSortedBackwards = applyReverts(Observable.from(sortedEvents));
                    return applyEvents(util, newSnapshot, forwardEventsSortedBackwards, DefaultGroovyMethods.plus(deprecatesList, event), aggregates);
                }).toBlocking().first();
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    private EventApplyOutcome callMethod(
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> util,
            String methodName,
            final SnapshotType snapshot,
            final EventType event) {
        try {
            return DefaultGroovyMethods.asType(
                    InvokerHelper.invokeMethod(
                            util,
                            methodName,
                            new Object[]{util.unwrapIfProxy(event), snapshot}),
                    EventApplyOutcome.class);
        } catch (Exception e1) {
            try {
                return util.onException(e1, snapshot, event);
            } catch (Exception e2) {
                String description = "{SnapshotType: " + String.valueOf(snapshot) + "; Event: " + String.valueOf(event) + "; method: " + methodName + "; originalException: " + String.valueOf(e1) + "}";
                log.error("Exception thrown while calling exception handler. " + String.valueOf(description), e2);
                return EventApplyOutcome.RETURN;
            }

        }

    }
}
