package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import java.util.Date;

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
     * @return An Flowable that returns at most one Snapshot
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
     * @return An Optional SnapshotType. Empty if cannot be computed.
     */
    @NotNull Publisher<SnapshotT> computeSnapshot(
            @NotNull AggregateT aggregate, @NotNull Date moment, boolean redirect);

}
