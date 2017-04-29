package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.GroovesException;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.rahulsom.grooves.queries.internal.Utils.stringifyEventIds;
import static com.github.rahulsom.grooves.queries.internal.Utils.stringifyEvents;
import static rx.Observable.just;

/**
 * Executes a query. This makes a query more flexible by allowing the use of different query
 * executors.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public class QueryExecutor<
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        >
        implements Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Applies all revert events from a list and returns the list with only valid forward events.
     *
     * @param events The list of events
     *
     * @return An Observable of forward only events
     */
    @Override
    public Observable<EventT> applyReverts(Observable<EventT> events) {

        return events.toList().flatMap(eventList -> {
            log.debug(String.format("     EventList: %s", stringifyEventIds(eventList)));
            List<EventT> forwardEvents = new ArrayList<>();
            while (!eventList.isEmpty()) {
                EventT head = eventList.remove(eventList.size() - 1);
                if (head instanceof RevertEvent) {
                    final EventIdT revertedEventId =
                            (EventIdT) ((RevertEvent) head).getRevertedEventId();
                    final Optional<EventT> revertedEvent = eventList.stream()
                            .filter(it -> it.getId().equals(revertedEventId))
                            .findFirst();
                    if (revertedEvent.isPresent()) {
                        eventList.remove(revertedEvent.get());
                    } else {
                        throw new GroovesException(String.format(
                                "Cannot revert event that does not exist in unapplied list - %s",
                                String.valueOf(revertedEventId)));
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
    public Observable<SnapshotT> applyEvents(
            final BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> query,
            SnapshotT initialSnapshot,
            Observable<EventT> events,
            final List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesList,
            final List<AggregateT> aggregates) {

        final AtomicBoolean stopApplyingEvents = new AtomicBoolean(false);

        // s -> snapshotObservable
        return events.reduce(just(initialSnapshot), (s, event) -> s.flatMap(snapshot -> {
            if (!query.shouldEventsBeApplied(snapshot) || stopApplyingEvents.get()) {
                return just(snapshot);
            } else {
                log.debug("     -> Event: {}", event);

                if (event instanceof Deprecates) {
                    return applyDeprecates(
                            (Deprecates<AggregateT, EventIdT, EventT>) event,
                            query, aggregates, deprecatesList);
                } else if (event instanceof DeprecatedBy) {
                    return just(applyDeprecatedBy(
                            (DeprecatedBy<AggregateT, EventIdT, EventT>) event, snapshot));
                } else {
                    String methodName = "apply" + event.getClass().getSimpleName();
                    return callMethod(query, methodName, snapshot, event)
                            .flatMap(retval -> handleMethodResponse(
                                    stopApplyingEvents, snapshot, methodName, retval));
                }
            }
        })).flatMap(it -> it);

    }

    private Observable<? extends SnapshotT> handleMethodResponse(
            AtomicBoolean stopApplyingEvents, SnapshotT snapshot, String methodName,
            EventApplyOutcome retval) {
        if (retval.equals(EventApplyOutcome.CONTINUE)) {
            return just(snapshot);
        } else if (retval.equals(EventApplyOutcome.RETURN)) {
            stopApplyingEvents.set(true);
            return just(snapshot);
        } else {
            throw new GroovesException(
                    "Unexpected value from calling '" + methodName + "'");
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    SnapshotT applyDeprecatedBy(
            final DeprecatedBy<AggregateT, EventIdT, EventT> event, SnapshotT snapshot) {
        AggregateT newAggregate = event.getDeprecator();
        snapshot.setDeprecatedBy(newAggregate);
        return snapshot;
    }

    Observable<SnapshotT> applyDeprecates(
            final Deprecates<AggregateT, EventIdT, EventT> event,
            final BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> util,
            final List<AggregateT> aggregates,
            final List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesList) {
        final SnapshotT newSnapshot = util.createEmptySnapshot();
        newSnapshot.setAggregate(event.getAggregate());

        AggregateT otherAggregate = event.getDeprecated();
        util.addToDeprecates(newSnapshot, otherAggregate);

        return util.findEventsForAggregates(
                DefaultGroovyMethods.plus(aggregates, event.getDeprecated()))
                .filter(it -> !it.getId().equals(event.getId())
                        && !it.getId().equals(event.getConverse().getId()))
                .toSortedList((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .flatMap(sortedEvents -> {
                    log.debug("Sorted Events: " + stringifyEvents(sortedEvents));
                    Observable<EventT> forwardEventsSortedBackwards =
                            applyReverts(Observable.from(sortedEvents));
                    return applyEvents(util, newSnapshot, forwardEventsSortedBackwards,
                            DefaultGroovyMethods.plus(deprecatesList, event), aggregates);
                });
    }

    private Observable<EventApplyOutcome> callMethod(
            BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> util,
            String methodName,
            final SnapshotT snapshot,
            final EventT event) {
        try {
            return (Observable<EventApplyOutcome>) InvokerHelper.invokeMethod(
                    util, methodName, new Object[]{util.unwrapIfProxy(event), snapshot});
        } catch (Exception e1) {
            try {
                return util.onException(e1, snapshot, event);
            } catch (Exception e2) {
                String description = String.format(
                        "{Snapshot: %s; Event: %s; method: %s; originalException: %s}",
                        String.valueOf(snapshot), String.valueOf(event), methodName,
                        String.valueOf(e1));
                log.error(String.format("Exception thrown while calling exception handler. %s",
                        description), e2);
                return just(EventApplyOutcome.RETURN);
            }

        }

    }
}
