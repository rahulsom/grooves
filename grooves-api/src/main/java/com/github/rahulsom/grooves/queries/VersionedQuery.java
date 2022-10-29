package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

/**
 * A query that produces a {@link VersionedSnapshot}.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the EventT's id field
 * @param <EventT>      The type of the event
 * @param <SnapshotIdT> The type of the SnapshotT's id field
 * @param <SnapshotT>   The type of the snapshot
 * @author Rahul Somasunderam
 */
public interface VersionedQuery<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>> {
    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     * @return A Publisher that returns at most one Snapshot
     */
    @NotNull Publisher<SnapshotT> computeSnapshot(
            @NotNull AggregateT aggregate, long version);

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's
     *                  snapshot. Defaults to true.
     * @return A Publisher that returns at most one Snapshot
     */
    @NotNull Publisher<SnapshotT> computeSnapshot(
            @NotNull AggregateT aggregate, long version, boolean redirect);

}
