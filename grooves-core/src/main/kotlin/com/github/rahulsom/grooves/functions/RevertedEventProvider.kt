package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.logging.Trace

/**
 * Given a revert event, provides the event that is reverted by it.
 */
interface RevertedEventProvider<Event> {
    @Trace
    fun invoke(event: Event): Event
}