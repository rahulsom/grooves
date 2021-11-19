package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.EventApplyOutcome
import com.github.rahulsom.grooves.logging.Trace

/**
 * A function that handles exceptions thrown when applying an event to a snapshot.
 */
interface ExceptionHandler<Snapshot, Event> {
    @Trace(false)
    fun invoke(exception: Exception, snapshot: Snapshot, event: Event): EventApplyOutcome
}