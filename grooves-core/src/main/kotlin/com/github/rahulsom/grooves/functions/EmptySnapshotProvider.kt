package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.logging.Trace

/**
 * A snapshot provider that always returns an empty snapshot.
 * This can be used to build a snapshot with additional events.
 */
interface EmptySnapshotProvider<Aggregate, Snapshot> {
    @Trace
    fun invoke(aggregate: Aggregate): Snapshot
}