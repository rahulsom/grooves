package com.github.rahulsom.grooves.queries.internal;

import static io.reactivex.Flowable.*;

import com.github.rahulsom.grooves.api.GroovesException;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import io.reactivex.Flowable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility objects and methods to help with Queries.
 *
 * @author Rahul Somasunderam
 */
public class Utils {

    private static final Collector<CharSequence, ?, String> JOIN_EVENT_IDS = Collectors.joining(", ");

    private static final Collector<CharSequence, ?, String> JOIN_EVENTS =
            Collectors.joining(",\n    ", "[\n    ", "\n]");

    private Utils() {}

    /**
     * Returns a snapshot or redirects to its deprecator.
     *
     * @param redirect           Whether a redirect is desirable
     * @param events             The sequence of events
     * @param it                 The snapshot
     * @param redirectedSnapshot A computation for the redirected snapshot
     * @param <AggregateT>       The type of Aggregate
     * @param <EventIdT>         The type of EventT's id
     * @param <EventT>           The type of the event
     * @param <SnapshotIdT>      The type of SnapshotT's id
     * @param <SnapshotT>        The type of the snapshot
     *
     * @return An observable of a snapshot.
     */
    @NotNull
    public static <
                    AggregateT,
                    EventIdT,
                    EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                    SnapshotIdT,
                    SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>>
            Flowable<SnapshotT> returnOrRedirect(
                    boolean redirect,
                    @NotNull List<EventT> events,
                    @NotNull SnapshotT it,
                    @NotNull Supplier<Flowable<SnapshotT>> redirectedSnapshot) {
        final EventT lastEvent = events.isEmpty() ? null : events.get(events.size() - 1);

        final boolean redirectToDeprecator = lastEvent instanceof DeprecatedBy && redirect;

        return fromPublisher(it.getDeprecatedByObservable())
                .flatMap(deprecatedBy -> redirectToDeprecator ? redirectedSnapshot.get() : just(it))
                .defaultIfEmpty(it);
    }

    /**
     * Computes forward only events. This could mean cancelling out events with their reverts
     * within a list, or sometimes, invoking the supplier of fallback events to get events.
     *
     * @param events                    The sequence of events
     * @param executor                  The executor to use for processing events
     * @param fallbackSnapshotAndEvents The fallback supplier
     * @param <AggregateT>              The type of Aggregate
     * @param <EventIdT>                The type of EventT's id
     * @param <EventT>                  The type of Event
     * @param <SnapshotIdT>             The type of SnapshotT's id
     * @param <SnapshotT>               The type of Snapshot
     * @param <QueryT>                  The type of Query
     * @param <ExecutorT>               The type of the query executor
     *
     * @return an observable of forward only events
     */
    @NotNull
    public static <
                    AggregateT,
                    EventIdT,
                    EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                    SnapshotIdT,
                    SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
                    QueryT extends BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
                    ExecutorT extends Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>>
            Flowable<EventT> getForwardOnlyEvents(
                    @NotNull List<EventT> events,
                    @NotNull ExecutorT executor,
                    @NotNull Supplier<Flowable<Pair<SnapshotT, List<EventT>>>> fallbackSnapshotAndEvents) {
        return executor.applyReverts(fromIterable(events))
                .toList()
                .map(Flowable::just)
                .onErrorReturn(throwable -> executor.applyReverts(
                                fallbackSnapshotAndEvents.get().flatMap(it -> fromIterable(it.second())))
                        .toList()
                        .toFlowable())
                .toFlowable()
                .flatMap(it -> it)
                .flatMap(Flowable::fromIterable);
    }

    /**
     * Turns a list of events into a readable log style string.
     *
     * @param events   The list of events
     * @param <EventT> The type of events
     *
     * @return A String representation of events
     */
    @NotNull
    public static <EventT extends BaseEvent> String stringify(@NotNull List<EventT> events) {
        return events.stream().map(EventT::toString).collect(JOIN_EVENTS);
    }

    /**
     * Turns a list of events into a readable list of ids.
     *
     * @param events   The list of events
     * @param <EventT> The type of events
     *
     * @return A String representation of events
     */
    @NotNull
    public static <EventT extends BaseEvent> String ids(@NotNull List<EventT> events) {
        return events.stream().map(i -> String.valueOf(i.getId())).collect(JOIN_EVENT_IDS);
    }

    /**
     * Sets the last event of a snapshot. Detects the kind of snapshot and sets required
     * properties appropriately
     *
     * @param snapshot The snapshot
     * @param event    The last event
     */
    public static void setLastEvent(@NotNull BaseSnapshot snapshot, @NotNull BaseEvent event) {
        if (snapshot instanceof VersionedSnapshot) {
            ((VersionedSnapshot) snapshot).setLastEventPosition(event.getPosition());
        }
        if (snapshot instanceof TemporalSnapshot) {
            ((TemporalSnapshot) snapshot).setLastEventTimestamp(event.getTimestamp());
        }
    }

    /**
     * Computes applicable events.
     *
     * @param <AggregateT>           The aggregate over which the query executes
     * @param <EventIdT>             The type of the EventT's id field
     * @param <EventT>               The type of the Event
     * @param <SnapshotIdT>          The type of the SnapshotT's id field
     * @param <SnapshotT>            The type of the Snapshot
     * @param forwardOnlyEvents      Known forward only events
     * @param executor               An instance of Executor
     * @param snapshotAndEventsSince Events to use if forwardOnlyEvents is empty
     *
     * @return events that can be applied.
     */
    public static <
                    AggregateT,
                    EventIdT,
                    EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                    SnapshotIdT,
                    SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>>
            Flowable<EventT> getApplicableEvents(
                    @NotNull Flowable<EventT> forwardOnlyEvents,
                    @NotNull Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> executor,
                    @NotNull Supplier<Flowable<Pair<SnapshotT, List<EventT>>>> snapshotAndEventsSince) {
        return forwardOnlyEvents
                .filter(e -> e instanceof Deprecates)
                .toList()
                .toFlowable()
                .flatMap(list -> list.isEmpty()
                        ? forwardOnlyEvents
                        : snapshotAndEventsSince
                                .get()
                                .flatMap(p -> getForwardOnlyEvents(
                                        p.second(),
                                        executor,
                                        () -> error(new GroovesException("Couldn't apply deprecates events")))));
    }

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Computes a Flowable of a Pair of Snapshot and List of Events.
     *
     * @param reuseEarlierSnapshot         Whether earlier snapshots can be reused for this
     *                                     computation. It is generally a good idea to set this to
     *                                     true unless there are known reverts that demand this be
     *                                     set to false.
     * @param lastUsableSnapshot           Supplies the last usable snapshot
     * @param uncomputedEvents             Gets the list of uncomputed events for a snapshot
     * @param nonReusableSnapshotAndEvents Supplies a snapshot without reuse
     * @param emptySnapshot                Supplies an empty snapshot
     * @param <AggregateT>                 The aggregate over which the query executes
     * @param <EventIdT>                   The type of the EventT's id field
     * @param <EventT>                     The type of the Event
     * @param <SnapshotIdT>                The type of the SnapshotT's id field
     * @param <SnapshotT>                  The type of the Snapshot
     *
     * @return A flowable with one pair of snapshot and list of events.
     */
    @NotNull
    public static <
                    AggregateT,
                    EventIdT,
                    EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                    SnapshotIdT,
                    SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>>
            Flowable<Pair<SnapshotT, List<EventT>>> getSnapshotsWithReuse(
                    boolean reuseEarlierSnapshot,
                    @NotNull Supplier<Flowable<SnapshotT>> lastUsableSnapshot,
                    @NotNull Function<SnapshotT, Publisher<EventT>> uncomputedEvents,
                    @NotNull Supplier<Flowable<Pair<SnapshotT, List<EventT>>>> nonReusableSnapshotAndEvents,
                    @NotNull Supplier<SnapshotT> emptySnapshot) {
        if (reuseEarlierSnapshot) {
            return lastUsableSnapshot.get().flatMap(lastSnapshot -> fromPublisher(uncomputedEvents.apply(lastSnapshot))
                    .toList()
                    .toFlowable()
                    .flatMap(events -> {
                        if (events.stream().anyMatch(it -> it instanceof RevertEvent)) {
                            List<EventT> reverts = events.stream()
                                    .filter(it -> it instanceof RevertEvent)
                                    .toList();
                            logger.info("     Uncomputed reverts exist: {}", stringify(reverts));
                            return nonReusableSnapshotAndEvents.get();
                        } else {
                            logger.debug("     Events since last snapshot: {}", stringify(events));
                            return just(new Pair<>(lastSnapshot, events));
                        }
                    }));
        } else {
            SnapshotT lastSnapshot = emptySnapshot.get();

            final Flowable<List<EventT>> uncomputedEventsF =
                    fromPublisher(uncomputedEvents.apply(lastSnapshot)).toList().toFlowable();

            return uncomputedEventsF
                    .doOnNext(ue -> logger.debug("     Events since origin: {}", stringify(ue)))
                    .map(ue -> new Pair<>(lastSnapshot, ue));
        }
    }
}
