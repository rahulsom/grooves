package com.github.rahulsom.grooves;

/**
 * Represents the result of a deprecation operation with the deprecated aggregate and event ID.
 *
 * @param <AggregateT> the type of the aggregate being deprecated
 * @param <EventIdT> the type of the event identifier
 * @param aggregate the aggregate that is being deprecated
 * @param eventId the ID of the event that serves as the converse of the deprecation
 */
public record DeprecatedByResult<AggregateT, EventIdT>(AggregateT aggregate, EventIdT eventId) {}
