package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static io.reactivex.Flowable.*;

/**
 * Utility objects and methods to help with Queries.
 *
 * @author Rahul Somasunderam
 */
public class Utils {

    private static final Collector<CharSequence, ?, String> JOIN_EVENT_IDS =
            Collectors.joining(", ");

    private static final Collector<CharSequence, ?, String> JOIN_EVENTS =
            Collectors.joining(",\n    ", "[\n    ", "\n]");

    private Utils() {
    }

    /**
     * Returns a snapshot or redirects to its deprecator.
     *
     * @param redirect           Whether a redirect is desirable
     * @param events             The sequence of events
     * @param it                 The snapshot
     * @param redirectedSnapshot A computation for the redirected snapshot
     * @param <AggregateIdT>     The type of {@link AggregateT}'s id
     * @param <AggregateT>       The type of Aggregate
     * @param <EventIdT>         The type of {@link EventT}'s id
     * @param <EventT>           The type of the event
     * @param <SnapshotIdT>      The type of {@link SnapshotT}'s id
     * @param <SnapshotT>        The type of the snapshot
     *
     * @return An observable of a snapshot.
     */
    @NotNull public static <
            AggregateIdT,
            AggregateT extends AggregateType<AggregateIdT>,
            EventIdT,
            EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends BaseSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>
            > Flowable<SnapshotT> returnOrRedirect(
            boolean redirect,
            @NotNull List<EventT> events,
            @NotNull SnapshotT it,
            @NotNull Supplier<Flowable<SnapshotT>> redirectedSnapshot) {
        final EventT lastEvent =
                events.isEmpty() ? null : events.get(events.size() - 1);

        final boolean redirectToDeprecator =
                lastEvent != null
                        && lastEvent instanceof DeprecatedBy
                        && redirect;

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
     * @param <AggregateIdT>            The type of {@link AggregateT}'s id
     * @param <AggregateT>              The type of Aggregate
     * @param <EventIdT>                The type of {@link EventT}'s id
     * @param <EventT>                  The type of Event
     * @param <SnapshotIdT>             The type of {@link SnapshotT}'s id
     * @param <SnapshotT>               The type of Snapshot
     * @param <QueryT>                  The type of Query
     * @param <ExecutorT>               The type of the query executor
     *
     * @return an observable of forward only events
     */
    @NotNull public static <
            AggregateIdT,
            AggregateT extends AggregateType<AggregateIdT>,
            EventIdT,
            EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends BaseSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>,
            QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                    SnapshotT>,
            ExecutorT extends Executor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                    SnapshotT>
            > Flowable<EventT> getForwardOnlyEvents(
            @NotNull List<EventT> events,
            @NotNull ExecutorT executor,
            @NotNull Supplier<Flowable<Pair<SnapshotT, List<EventT>>>>
                    fallbackSnapshotAndEvents) {
        return executor.applyReverts(fromIterable(events))
                .toList()
                .map(Flowable::just)
                .onErrorReturn(throwable -> executor
                        .applyReverts(
                                fallbackSnapshotAndEvents.get()
                                        .flatMap(it -> fromIterable(it.getSecond()))
                        )
                        .toList().toFlowable()
                )
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
    @NotNull public static <EventT extends BaseEvent> String stringify(
            @NotNull List<EventT> events) {
        return events.stream()
                .map(EventT::toString)
                .collect(JOIN_EVENTS);
    }

    /**
     * Turns a list of events into a readable list of ids.
     *
     * @param events   The list of events
     * @param <EventT> The type of events
     *
     * @return A String representation of events
     */
    @NotNull public static <EventT extends BaseEvent> String ids(
            @NotNull List<EventT> events) {
        return events.stream()
                .map(i -> String.valueOf(i.getId()))
                .collect(JOIN_EVENT_IDS);
    }

    /**
     * Sets the last event of a snapshot. Detects the kind of snapshot and sets required
     * properties appropriately
     *
     * @param snapshot The snapshot
     * @param event The last event
     */
    public static void setLastEvent(@NotNull BaseSnapshot snapshot, @NotNull BaseEvent event) {
        if (snapshot instanceof VersionedSnapshot) {
            ((VersionedSnapshot) snapshot).setLastEventPosition(event.getPosition());
        }
        if (snapshot instanceof TemporalSnapshot) {
            ((TemporalSnapshot) snapshot).setLastEventTimestamp(event.getTimestamp());
        }
    }
}
