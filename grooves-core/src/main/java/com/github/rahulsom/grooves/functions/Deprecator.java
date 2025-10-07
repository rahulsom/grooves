package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.logging.Trace;

/**
 * Stores information in a snapshot about an aggregate that is deprecated by it.
 */
@FunctionalInterface
public interface Deprecator<SnapshotT, AggregateT> {
    /**
     * Stores deprecation information in the snapshot about an aggregate that is deprecated by it.
     *
     * @param snapshot the snapshot to update with deprecation information
     * @param deprecatingAggregate the aggregate that is causing the deprecation
     */
    @Trace
    void invoke(SnapshotT snapshot, AggregateT deprecatingAggregate);
}
