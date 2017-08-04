package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.GroovesException;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.RETURN;
import static com.github.rahulsom.grooves.queries.internal.Utils.ids;
import static com.github.rahulsom.grooves.queries.internal.Utils.stringify;
import static rx.Observable.from;
import static rx.Observable.just;

/**
 * Executes a query. This makes a query more flexible by allowing the use of different query
 * executors.
 *
 * @param <AggregateIdT> The type of {@link AggregateT}'s id
 * @param <AggregateT>   The aggregate over which the query executes
 * @param <EventIdT>     The type of the {@link EventT}'s id field
 * @param <EventT>       The type of the Event
 * @param <SnapshotIdT>  The type of the {@link SnapshotT}'s id field
 * @param <SnapshotT>    The type of the Snapshot
 * @param <QueryT>       A reference to the query type.
 *
 * @author Rahul Somasunderam
 */
public class QueryExecutor<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT>
        > implements Executor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
        QueryT> {

    final Logger log = LoggerFactory.getLogger(getClass());

    private static <T> List<T> plus(List<T> list, T element) {
        List<T> retval = new ArrayList<>();
        retval.addAll(list);
        retval.add(element);
        return retval;
    }

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
            log.debug("     Event Ids (includes reverts that won't be applied): {}",
                    ids(eventList));
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

            return from(forwardEvents);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<SnapshotT> applyEvents(
            QueryT query,
            SnapshotT initialSnapshot,
            Observable<EventT> events,
            List<Deprecates<AggregateIdT, AggregateT, EventIdT, EventT>> deprecatesList,
            AggregateT aggregate) {

        final AtomicBoolean stopApplyingEvents = new AtomicBoolean(false);

        // s -> snapshotObservable
        return events.reduce(just(initialSnapshot), (s, event) -> s.flatMap(snapshot -> {
            if (!query.shouldEventsBeApplied(snapshot) || stopApplyingEvents.get()) {
                return just(snapshot);
            } else {
                log.debug("     -> Applying Event: {}", event);

                if (event instanceof Deprecates) {
                    Deprecates<AggregateIdT, AggregateT, EventIdT, EventT> deprecatesEvent =
                            (Deprecates<AggregateIdT, AggregateT, EventIdT, EventT>) event;
                    return applyDeprecates(
                            deprecatesEvent, query, events, deprecatesList, aggregate);
                } else if (event instanceof DeprecatedBy) {
                    DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT> deprecatedByEvent =
                            (DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT>) event;
                    return applyDeprecatedBy(deprecatedByEvent, snapshot);
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
        switch (retval) {
            case RETURN:
                stopApplyingEvents.set(true);
                return just(snapshot);
            case CONTINUE:
                return just(snapshot);
            default:
                throw new GroovesException(
                        String.format("Unexpected value from calling '%s'", methodName));
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
            final DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT> event,
            SnapshotT snapshot) {
        return event.getDeprecatorObservable().reduce(snapshot, (snapshotT, aggregate) -> {
            log.info("        -> {} will cause redirect to {}", event, aggregate);
            snapshotT.setDeprecatedBy(aggregate);
            return snapshotT;
        });
    }

    /**
     * Applies a {@link Deprecates} event to a snapshot.
     *
     * @param event            The {@link Deprecates} event
     * @param util             The Query Util instance
     * @param events           All {@link EventT}s that have been gathered so far
     * @param deprecatesEvents The list of {@link Deprecates} events that have been collected so
     *                         far
     * @param aggregate        The current aggregate
     *
     * @return The snapshot after applying the {@link Deprecates} event
     */
    Observable<SnapshotT> applyDeprecates(
            final Deprecates<AggregateIdT, AggregateT, EventIdT, EventT> event,
            final QueryT util,
            final Observable<EventT> events,
            final List<Deprecates<AggregateIdT, AggregateT, EventIdT, EventT>> deprecatesEvents,
            AggregateT aggregate) {

        log.info("        -> {} will cause recomputation", event);
        final SnapshotT newSnapshot = util.createEmptySnapshot();
        newSnapshot.setAggregate(aggregate);

        return event.getConverseObservable().flatMap(converse -> event.getDeprecatedObservable()
                .flatMap(deprecatedAggregate -> {
                    log.debug("        -> Deprecated Aggregate is: {}. Converse is: {}",
                            deprecatedAggregate, converse);
                    util.addToDeprecates(newSnapshot, deprecatedAggregate);

                    Observable<EventT> concatenatedEvents =
                            events.concatWith(util.findEventsBefore((EventT) converse))
                                    .cache();
                    return concatenatedEvents
                            .filter(it -> !isDeprecatesOrConverse(event, converse, it))
                            .toSortedList((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                            .doOnNext(it -> log.debug("Reassembled Events: {}", stringify(it)))
                            .flatMap(sortedEvents -> applyEvents(
                                    util, newSnapshot, applyReverts(from(sortedEvents)),
                                    plus(deprecatesEvents, event), aggregate));
                }));

    }

    private boolean isDeprecatesOrConverse(
            Deprecates<AggregateIdT, AggregateT, EventIdT, EventT> event,
            DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT> converse,
            EventT it) {
        EventIdT id = it.getId();
        return Objects.equals(id, event.getId()) || Objects.equals(id, converse.getId());
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
     *         Util instance, or an Observable that asks to RETURN if that fails.
     */
    protected Observable<EventApplyOutcome> callMethod(
            QueryT util, String methodName, final SnapshotT snapshot, final EventT event) {
        try {
            final Method method =
                    util.getClass().getMethod(methodName, event.getClass(), snapshot.getClass());
            return (Observable<EventApplyOutcome>) method.invoke(util, event, snapshot);
        } catch (Exception e1) {
            return handleException(util, methodName, snapshot, event, e1);
        }
    }

    private Observable<EventApplyOutcome> handleException(
            QueryT util, String methodName, SnapshotT snapshot, EventT event, Exception e1) {
        try {
            return util.onException(e1, snapshot, event);
        } catch (Exception e2) {
            String description = String.format(
                    "{Snapshot: %s; Event: %s; method: %s; originalException: %s}",
                    String.valueOf(snapshot), String.valueOf(event), methodName,
                    String.valueOf(e1));
            log.error(String.format("Exception thrown while calling exception handler. %s",
                    description), e2);
            return just(RETURN);
        }
    }
}
