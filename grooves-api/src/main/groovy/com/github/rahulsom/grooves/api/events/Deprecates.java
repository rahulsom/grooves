package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

/**
 * Aggregate deprecation event.
 * <br/>
 * The aggregate on which this event is applied is considered the winner of the merge.
 * The converse is {@link DeprecatedBy} which needs to be applied to the other aggregate which loses the merge.
 *
 * @param <Aggregate> Aggregate this event applies to
 * @param <EventIdType> The Type for Event's {@link #getId} field
 * @param <EventType> The parent event type
 *
 * @author Rahul Somasunderam
 */
public interface Deprecates<Aggregate extends AggregateType, EventIdType, EventType>
        extends BaseEvent<Aggregate, EventIdType, EventType> {
    DeprecatedBy<Aggregate, EventIdType, EventType> getConverse();
    Aggregate getDeprecated();
}
