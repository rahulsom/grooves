package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

/**
 * Revert a prior event. Aggregate reverted event's effects are not applied.
 *
 * @param <AggregateT> Aggregate this event applies to
 * @param <EventIdT>   The Type for Event's {@link #getId} field
 * @param <EventT>     Event Type that could be reverted
 *
 * @author Rahul Somasunderam
 */
public interface RevertEvent<AggregateT extends AggregateType, EventIdT, EventT>
        extends BaseEvent<AggregateT, EventIdT, EventT> {

    EventIdT getRevertedEventId();
}
