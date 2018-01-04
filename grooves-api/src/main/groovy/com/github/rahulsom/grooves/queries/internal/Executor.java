package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Executes a query by controlling how events are applied.
 *
 * @param <AggregateT>   The type of Aggregate
 * @param <EventIdT>     The type of {@link EventT}'s id
 * @param <EventT>       The type of Event
 * @param <SnapshotIdT>  The type of {@link SnapshotT}'s id
 * @param <SnapshotT>    The type of Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface Executor<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        > {
    /**
     * Applies reverts to a list of events and then returns forward events.
     *
     * @param events The list of events
     *
     * @return a list of events after the reverts have been applied
     */
    @NotNull Flowable<EventT> applyReverts(@NotNull Flowable<EventT> events);

    /**
     * Applies forward events on a snapshot
     *
     * @param query           The query that demands the events to be applied.
     * @param initialSnapshot The snapshot to be mutated
     * @param events          The list of forward events
     * @param deprecatesList  The list of Deprecate events
     * @param aggregate       The aggregate on which we are currently working
     *
     * @return The Snapshot that has been mutated
     */
    @NotNull Flowable<SnapshotT> applyEvents(
            @NotNull BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>
                    query,
            @NotNull SnapshotT initialSnapshot,
            @NotNull Flowable<EventT> events,
            @NotNull List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesList,
            @NotNull AggregateT aggregate);
}
