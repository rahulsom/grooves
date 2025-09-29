package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.logging.Trace;

/**
 * A snapshot provider that always returns an empty snapshot.
 * This can be used to build a snapshot with additional events.
 */
@FunctionalInterface
public interface EmptySnapshotProvider<AggregateT, SnapshotT> {
    /**
     * Creates an empty snapshot for the given aggregate.
     *
     * @param aggregate the aggregate to create an empty snapshot for
     * @return a new empty snapshot instance
     */
    @Trace
    SnapshotT invoke(AggregateT aggregate);
}