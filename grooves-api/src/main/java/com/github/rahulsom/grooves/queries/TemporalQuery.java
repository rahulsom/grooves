package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import java.util.Date;

/**
 * A query that produces a {@link TemporalSnapshot}.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the EventT's id field
 * @param <EventT>      The type of the event
 * @param <SnapshotIdT> The type of the SnapshotT's id field
 * @param <SnapshotT>   The type of the snapshot
 * @author Rahul Somasunderam
 */
public interface TemporalQuery<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>> {
    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     *
     * @return A Publisher that returns at most one Snapshot
     */
    @NotNull Publisher<SnapshotT> computeSnapshot(
            @NotNull AggregateT aggregate, @NotNull Date moment);

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's
     *                  snapshot. Defaults to true.
     *
     * @return An Optional SnapshotType. Empty if it cannot be computed.
     */
    @NotNull Publisher<SnapshotT> computeSnapshot(
            @NotNull AggregateT aggregate, @NotNull Date moment, boolean redirect);

}
