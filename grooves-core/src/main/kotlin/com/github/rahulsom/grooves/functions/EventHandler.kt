package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.EventApplyOutcome
import com.github.rahulsom.grooves.logging.Trace

/**
 * Applies the event to a snapshot.
 */
interface EventHandler<Event, Snapshot> {
    @Trace
    fun invoke(event: Event, snapshot: Snapshot): EventApplyOutcome
}