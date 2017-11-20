package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import org.reactivestreams.Publisher;

import java.util.Date;

public interface VersionedQuery<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends VersionedSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT,
                        EventT>> {
    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     *
     * @return An Flowable that returns at most one Snapshot
     */
    Publisher<SnapshotT> computeSnapshot(AggregateT aggregate, long version);

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's
     *                  snapshot. Defaults to true.
     *
     * @return An Flowable that returns at most one Snapshot
     */
    Publisher<SnapshotT> computeSnapshot(AggregateT aggregate, long version, boolean redirect);

}
