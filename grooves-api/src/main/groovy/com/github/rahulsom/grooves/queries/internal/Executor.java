package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import rx.Observable;

import java.util.List;

/**
 * Executes a query by controlling how events are applied.
 *
 * @param <AggregateT>  The type of Aggregate
 * @param <EventIdT>    The type of Event Id
 * @param <EventT>      The type of Event
 * @param <SnapshotIdT> The type of Snapshot Id
 * @param <SnapshotT>   The type of Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface Executor<
        AggregateT extends AggregateType,
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
    Observable<EventT> applyReverts(Observable<EventT> events);

    /**
     * Applies forward events on a snapshot
     *
     * @param query           The query that demands the events to be applied.
     * @param initialSnapshot The snapshot to be mutated
     * @param events          The list of forward events
     * @param deprecatesList  The list of Deprecate events
     * @param aggregates      The list of deprecated aggregates
     * @param aggregate       The aggregate on which we are currently working
     *
     * @return The Snapshot that has been mutated
     */
    Observable<SnapshotT> applyEvents(
            BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> query,
            SnapshotT initialSnapshot,
            Observable<EventT> events,
            List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesList,
            List<AggregateT> aggregates,
            AggregateT aggregate);
}
