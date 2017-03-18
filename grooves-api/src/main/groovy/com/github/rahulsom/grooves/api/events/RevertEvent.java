package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

/**
 * Revert a prior event. Aggregate reverted event's effects are not applied.
 *
 * @author Rahul Somasunderam
 */
public interface RevertEvent<Aggregate extends AggregateType, EventIdType, EventType>
        extends BaseEvent<Aggregate, EventIdType, EventType> {

    EventIdType getRevertedEventId();
}
