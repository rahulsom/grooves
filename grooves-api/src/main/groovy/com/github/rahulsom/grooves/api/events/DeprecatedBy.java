package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

/**
 * Aggregate deprecation event.
 * <br/>
 * The aggregate on which this event is applied is considered the loser of the merge.
 * The converse is {@link Deprecates} which needs to be applied to the other aggregate which wins the merge.
 *
 * @author Rahul Somasunderam
 */
public interface DeprecatedBy<Aggregate extends AggregateType, EventIdType, EventType>
        extends BaseEvent<Aggregate, EventIdType, EventType> {
    Deprecates<Aggregate, EventIdType, EventType> getConverse();
    Aggregate getDeprecator();
}
