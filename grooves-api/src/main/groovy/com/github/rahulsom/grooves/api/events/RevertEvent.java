package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

/**
 * Revert a prior event. Aggregate reverted event's effects are not applied.
 *
 * @param <Aggregate>   Aggregate this event applies to
 * @param <EventIdType> The Type for Event's {@link #getId} field
 * @param <EventType>   Event Type that could be reverted
 *
 * @author Rahul Somasunderam
 */
public interface RevertEvent<Aggregate extends AggregateType, EventIdType, EventType>
        extends BaseEvent<Aggregate, EventIdType, EventType> {

    EventIdType getRevertedEventId();
}
