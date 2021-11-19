package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.DeprecatedByResult
import com.github.rahulsom.grooves.logging.Trace

/**
 * Identifies the Aggregate that is deprecated by the given event, and the event id that's the converse of the given event.
 */
interface DeprecatedByProvider<Event, Aggregate, EventId> {
    @Trace(false)
    fun invoke(event: Event): DeprecatedByResult<Aggregate, EventId>
}