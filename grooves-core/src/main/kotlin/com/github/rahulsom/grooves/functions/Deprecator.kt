package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.logging.Trace

/**
 * Stores information in a snapshot about an aggregate that is deprecated by it.
 */
interface Deprecator<Snapshot, Aggregate> {
    @Trace
    fun invoke(
        snapshot: Snapshot,
        deprecatingAggregate: Aggregate,
    )
}