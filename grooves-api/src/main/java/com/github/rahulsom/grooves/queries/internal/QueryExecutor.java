package com.github.rahulsom.grooves.queries.internal;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.RETURN;
import static com.github.rahulsom.grooves.queries.internal.Utils.ids;
import static com.github.rahulsom.grooves.queries.internal.Utils.stringify;
import static io.reactivex.Flowable.*;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.GroovesException;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import io.reactivex.Flowable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a query. This makes a query more flexible by allowing the use of different query
 * executors.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the EventT's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the SnapshotT's id field
 * @param <SnapshotT>   The type of the Snapshot
 * @param <QueryT>      A reference to the query type.
 * @author Rahul Somasunderam
 */
public class QueryExecutor<
                AggregateT,
                EventIdT,
                EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                SnapshotIdT,
                SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
                QueryT extends BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>>
        implements Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    /**
     * The logger for this class.
     */
    @SuppressWarnings("WeakerAccess")
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Produces a list of elements from a given list appending a new element to it.
     *
     * @param list    the list to append to
     * @param element the new element to append
     * @param <T>     the type of elements in the list
     * @return the new list
     */
    private static <T> List<T> plus(List<T> list, T element) {
        List<T> retval = new ArrayList<>(list);
        retval.add(element);
        return retval;
    }

    /**
     * Applies all revert events from a list and returns the list with only valid forward events.
     *
     * @param events The list of events
     * @return A Flowable of forward only events
     */
    @NotNull
    @Override
    public Flowable<EventT> applyReverts(@NotNull Flowable<EventT> events) {

        return events.toList().toFlowable().flatMap(eventList -> {
            log.debug("     Event Ids (includes reverts that won't be applied): {}", ids(eventList));
            List<EventT> forwardEvents = new ArrayList<>();
            while (!eventList.isEmpty()) {
                EventT head = eventList.remove(eventList.size() - 1);
                if (head instanceof RevertEvent) {
                    final EventIdT revertedEventId = (EventIdT) ((RevertEvent) head).getRevertedEventId();
                    final Optional<EventT> revertedEvent = eventList.stream()
                            .filter(it -> Objects.equals(it.getId(), revertedEventId))
                            .findFirst();

                    if (revertedEvent.isPresent()) {
                        eventList.remove(revertedEvent.get());
                    } else {
                        throw new GroovesException(String.format(
                                "Cannot revert event that does not exist in unapplied list - %s", revertedEventId));
                    }

                } else {
                    forwardEvents.add(0, head);
                }
            }

            assert forwardEvents.stream().noneMatch(RevertEvent.class::isInstance);

            return fromIterable(forwardEvents);
        });
    }

    @NotNull
    @Override
    public Flowable<SnapshotT> applyEvents(
            @NotNull BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> query,
            @NotNull SnapshotT initialSnapshot,
            @NotNull Flowable<EventT> events,
            @NotNull List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesList,
            @NotNull AggregateT aggregate) {

        final AtomicBoolean stopApplyingEvents = new AtomicBoolean(false);

        // s -> snapshotObservable
        return events.reduce(
                        just(initialSnapshot),
                        (s, event) -> s.flatMap(snapshot -> {
                            if (!query.shouldEventsBeApplied(snapshot) || stopApplyingEvents.get()) {
                                return just(snapshot);
                            } else {
                                log.debug("     -> Applying Event: {}", event);

                                if (event instanceof Deprecates) {
                                    Deprecates<AggregateT, EventIdT, EventT> deprecatesEvent =
                                            (Deprecates<AggregateT, EventIdT, EventT>) event;
                                    return applyDeprecates(deprecatesEvent, query, events, deprecatesList, aggregate);
                                } else if (event instanceof DeprecatedBy) {
                                    DeprecatedBy<AggregateT, EventIdT, EventT> deprecatedByEvent =
                                            (DeprecatedBy<AggregateT, EventIdT, EventT>) event;
                                    return applyDeprecatedBy(deprecatedByEvent, snapshot);
                                } else {
                                    String methodName =
                                            "apply" + event.getClass().getSimpleName();
                                    return callMethod((QueryT) query, methodName, snapshot, event)
                                            .flatMap(retval ->
                                                    handleMethodResponse(stopApplyingEvents, snapshot, retval));
                                }
                            }
                        }))
                .toFlowable()
                .flatMap(it -> it);
    }

    /**
     * Decides how to proceed after inspecting the response of a method that returns an
     * EventApplyOutcome.
     *
     * @param stopApplyingEvents Whether a previous decision has been made to stop applying new
     *                           events
     * @param snapshot           The snapshot on which events are being added
     * @param retval             The outcome of calling the method
     * @return The snapshot after deciding what to do with the EventApplyOutcome
     */
    private Flowable<? extends SnapshotT> handleMethodResponse(
            AtomicBoolean stopApplyingEvents, SnapshotT snapshot, EventApplyOutcome retval) {
        return switch (retval) {
            case RETURN -> {
                stopApplyingEvents.set(true);
                yield just(snapshot);
            }
            case CONTINUE -> just(snapshot);
        };
    }

    /**
     * Applies a DeprecatedBy event to a snapshot.
     *
     * @param event    The DeprecatedBy event
     * @param snapshot The snapshot computed until before this event
     * @return The snapshot after applying the DeprecatedBy event
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    Flowable<SnapshotT> applyDeprecatedBy(final DeprecatedBy<AggregateT, EventIdT, EventT> event, SnapshotT snapshot) {
        return fromPublisher(event.getDeprecatorObservable())
                .reduce(snapshot, (snapshotT, aggregate) -> {
                    log.info("        -> {} will cause redirect to {}", event, aggregate);
                    snapshotT.setDeprecatedBy(aggregate);
                    return snapshotT;
                })
                .toFlowable();
    }

    /**
     * Applies a Deprecates event to a snapshot.
     *
     * @param event            The Deprecates event
     * @param query            The Query Util instance
     * @param events           All EventTs that have been gathered so far
     * @param deprecatesEvents The list of Deprecates events that have been collected so
     *                         far
     * @param aggregate        The current aggregate
     * @return The snapshot after applying the Deprecates event
     */
    Flowable<SnapshotT> applyDeprecates(
            final Deprecates<AggregateT, EventIdT, EventT> event,
            final BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> query,
            final Flowable<EventT> events,
            final List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesEvents,
            AggregateT aggregate) {

        log.info("        -> {} will cause recomputation", event);
        final SnapshotT newSnapshot = query.createEmptySnapshot();
        newSnapshot.setAggregate(aggregate);

        return fromPublisher(event.getConverseObservable()).flatMap(converse -> fromPublisher(
                        event.getDeprecatedObservable())
                .flatMap(deprecatedAggregate -> {
                    log.debug("        -> Deprecated Aggregate is: {}. Converse is: {}", deprecatedAggregate, converse);
                    query.addToDeprecates(newSnapshot, deprecatedAggregate);

                    Flowable<EventT> concatenatedEvents = events.concatWith(
                                    fromPublisher(query.findEventsBefore((EventT) converse)))
                            .cache();
                    return concatenatedEvents
                            .filter(it -> !isDeprecatesOrConverse(event, converse, it))
                            .toSortedList(Comparator.comparing(EventT::getTimestamp))
                            .toFlowable()
                            .doOnNext(it -> log.debug("     Reassembled Events: {}", stringify(it)))
                            .flatMap(sortedEvents -> applyEvents(
                                    query,
                                    newSnapshot,
                                    applyReverts(fromIterable(sortedEvents)),
                                    plus(deprecatesEvents, event),
                                    aggregate));
                }));
    }

    private boolean isDeprecatesOrConverse(
            Deprecates<AggregateT, EventIdT, EventT> event,
            DeprecatedBy<AggregateT, EventIdT, EventT> converse,
            EventT it) {
        EventIdT id = it.getId();
        return Objects.equals(id, event.getId()) || Objects.equals(id, converse.getId());
    }

    /**
     * Calls a method on a Query Util instance.
     *
     * @param query      The Query Util instance
     * @param methodName The method to be called
     * @param snapshot   The snapshot to be passed to the method
     * @param event      The event to be passed to the method
     * @return An observable returned by the method, or the result of calling onException on the
     *         Util instance, or an Observable that asks to RETURN if that fails.
     */
    @SuppressWarnings("WeakerAccess")
    protected Flowable<EventApplyOutcome> callMethod(
            QueryT query, String methodName, final SnapshotT snapshot, final EventT event) {
        try {
            final var method = query.getClass().getMethod(methodName, event.getClass(), snapshot.getClass());
            return fromPublisher((Publisher<EventApplyOutcome>) method.invoke(query, event, snapshot));
        } catch (Exception e1) {
            return handleException(query, methodName, snapshot, event, e1);
        }
    }

    private Flowable<EventApplyOutcome> handleException(
            QueryT query, String methodName, SnapshotT snapshot, EventT event, Exception e1) {
        try {
            return fromPublisher(query.onException(e1, snapshot, event));
        } catch (Exception e2) {
            final var description = String.format(
                    "{Snapshot: %s; Event: %s; method: %s; originalException: %s}", snapshot, event, methodName, e1);
            log.error("Exception thrown while calling exception handler. {}", description, e2);
            return just(RETURN);
        }
    }
}
