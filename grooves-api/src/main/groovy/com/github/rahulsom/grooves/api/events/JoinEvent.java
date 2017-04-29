package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

/**
 * Creates a join from {@link AggregateT} to {@link JoinedAggregateT} that had not existed earlier.
 *
 * @param <AggregateT>       The Aggregate that has been linked to the {@link JoinedAggregateT}
 * @param <EventIdT>         The Type for Event's {@link #getId} field
 * @param <EventT>           The parent event type
 * @param <JoinedAggregateT> The target aggregate
 *
 * @author Rahul Somasunderam
 */
public interface JoinEvent<
        AggregateT extends AggregateType,
        EventIdT,
        EventT,
        JoinedAggregateT extends AggregateType>
        extends BaseEvent<AggregateT, EventIdT, EventT> {

    JoinedAggregateT getJoinAggregate();

    void setJoinAggregate(JoinedAggregateT rollupAggregate);
}
