package com.github.rahulsom.grooves.functions;

/**
 * A function that returns an event id for a given event.
 */
@FunctionalInterface
public interface EventIdProvider<EventT, EventIdT> {
    /**
     * Extracts the unique identifier from the given event.
     *
     * @param event the event to get the ID from
     * @return the unique identifier of the event
     */
    EventIdT invoke(EventT event);
}
