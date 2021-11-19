package com.github.rahulsom.grooves.functions

/**
 * A function that returns an event id for a given event.
 */
interface EventIdProvider<Event, EventId> {
    fun invoke(event: Event): EventId
}