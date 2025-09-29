package com.github.rahulsom.grooves;

/**
 * Represents the different types of events in the event sourcing system.
 */
public enum EventType {
    /** A standard event that applies changes to the aggregate. */
    Normal,
    /** An event that reverts a previous event. */
    Revert,
    /** An event that marks other events/aggregates as deprecated. */
    Deprecates,
    /** An event that indicates this event is deprecated by another. */
    DeprecatedBy
}