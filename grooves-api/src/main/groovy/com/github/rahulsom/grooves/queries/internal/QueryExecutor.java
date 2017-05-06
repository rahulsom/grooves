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
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.rahulsom.grooves.queries.internal.Utils.stringifyEventIds;
import static com.github.rahulsom.grooves.queries.internal.Utils.stringifyEvents;
import static org.codehaus.groovy.runtime.InvokerHelper.invokeMethod;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<SnapshotT> applyEvents(
            final BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> query,
            SnapshotT initialSnapshot,
            Observable<EventT> events,
            final List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesList,
            final List<AggregateT> aggregates, AggregateT aggregate) {

        final AtomicBoolean stopApplyingEvents = new AtomicBoolean(false);

        // s -> snapshotObservable
        return events.reduce(just(initialSnapshot), (s, event) -> s.flatMap(snapshot -> {
            if (!query.shouldEventsBeApplied(snapshot) || stopApplyingEvents.get()) {
                return just(snapshot);
            } else {
                log.debug("     -> Applying Event: {}", event);

                if (event instanceof Deprecates) {
                    return applyDeprecates(
                            (Deprecates<AggregateT, EventIdT, EventT>) event,
                            query, aggregates, deprecatesList, aggregate);
                } else if (event instanceof DeprecatedBy) {
                    return applyDeprecatedBy(
                            (DeprecatedBy<AggregateT, EventIdT, EventT>) event, snapshot);
                } else {
                    String methodName = "apply" + event.getClass().getSimpleName();
                    return callMethod(query, methodName, snapshot, event)
                            .flatMap(retval -> handleMethodResponse(
                                    stopApplyingEvents, snapshot, methodName, retval));
                }
            }
        })).flatMap(it -> it);

    }

    /**
     * Decides how to proceed after inspecting the response of a method that returns an
     * {@link EventApplyOutcome}.
     *
     * @param stopApplyingEvents Whether a previous decision has been made to stop applying new
     *                           events
     * @param snapshot           The snapshot on which events are being added
     * @param methodName         The name of the method that was called
     * @param retval             The outcome of calling the method
     *
     * @return The snapshot after deciding what to do with the {@link EventApplyOutcome}
     */
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

    /**
     * Applies a {@link DeprecatedBy} event to a snapshot.
     *
     * @param event    The {@link DeprecatedBy} event
     * @param snapshot The snapshot computed until before this event
     *
     * @return The snapshot after applying the {@link DeprecatedBy} event
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    Observable<SnapshotT> applyDeprecatedBy(
            final DeprecatedBy<AggregateT, EventIdT, EventT> event, SnapshotT snapshot) {
        return event.getDeprecatorObservable().reduce(snapshot, (snapshotT, aggregateT) -> {
            log.info("        -> {} will cause redirect to {}", event, aggregateT);
            snapshotT.setDeprecatedBy(aggregateT);
            return snapshotT;
        });
    }

    /**
     * Applies a {@link Deprecates} event to a snapshot.
     *
     * @param event            The {@link Deprecates} event
     * @param util             The Query Util instance
     * @param allAggregates    All {@link AggregateType}s that have been deprecated by current
     *                         aggregate
     * @param deprecatesEvents The list of {@link Deprecates} events that have been collected so far
     * @param aggregate        The current aggregate
     *
     * @return The snapshot after applying the {@link Deprecates} event
     */
    Observable<SnapshotT> applyDeprecates(
            final Deprecates<AggregateT, EventIdT, EventT> event,
            final BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> util,
            final List<AggregateT> allAggregates,
            final List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesEvents,
            AggregateT aggregate) {

        log.info("        -> {} will cause recomputation", event);
        final SnapshotT newSnapshot = util.createEmptySnapshot();
        newSnapshot.setAggregate(aggregate);

        return event.getConverseObservable().flatMap(converse -> event.getDeprecatedObservable()
                .flatMap(deprecatedAggregate -> {
                    log.debug("        -> Deprecated Aggregate is: {}. Converse is: {}",
                            deprecatedAggregate, converse);
                    util.addToDeprecates(newSnapshot, deprecatedAggregate);

                    return util.findEventsForAggregates(
                            DefaultGroovyMethods.plus(allAggregates, deprecatedAggregate))
                            .filter(it -> !it.getId().equals(event.getId())
                                    && !it.getId().equals(converse.getId()))
                            .toSortedList((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                            .flatMap(sortedEvents -> {
                                log.debug("Reassembled Events: {}", stringifyEvents(sortedEvents));
                                Observable<EventT> forwardEventsSortedBackwards =
                                        applyReverts(Observable.from(sortedEvents));
                                return applyEvents(
                                        util,
                                        newSnapshot,
                                        forwardEventsSortedBackwards,
                                        DefaultGroovyMethods.plus(deprecatesEvents, event),
                                        allAggregates,
                                        aggregate);
                            });
                }));

    }

    /**
     * Calls a method on a Query Util instance.
     *
     * @param util       The Query Util instance
     * @param methodName The method to be called
     * @param snapshot   The snapshot to be passed to the method
     * @param event      The event to be passed to the method
     *
     * @return An observable returned by the method, or the result of calling onException on the
     * Util instance, or an Observable that asks to RETURN if that fails.
     */
    Observable<EventApplyOutcome> callMethod(
            BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> util,
            String methodName,
            final SnapshotT snapshot,
            final EventT event) {
        try {
            return (Observable<EventApplyOutcome>) invokeMethod(
                    util, methodName, new Object[]{event, snapshot});
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
