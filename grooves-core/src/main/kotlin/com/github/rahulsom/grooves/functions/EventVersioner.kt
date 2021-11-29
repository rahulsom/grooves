package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.logging.Trace

/**
 * Provides the version of the event.
 */
interface EventVersioner<Event, VersionOrTimestamp> {
    @Trace
    fun invoke(event: Event): VersionOrTimestamp
}