package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

/**
 * Creates a join from {@link Aggregate} to {@link JoinedAggregate} that had not existed earlier
 *
 * @param <Aggregate>       The Aggregate that has been linked to the {@link JoinedAggregate}
 * @param <EventIdType>     The Type for Event's {@link #getId} field
 * @param <EventType>       The parent event type
 * @param <JoinedAggregate> The target aggregate
 *
 * @author Rahul Somasunderam
 */
public interface JoinEvent<
        Aggregate extends AggregateType,
        EventIdType,
        EventType,
        JoinedAggregate extends AggregateType>
        extends BaseEvent<Aggregate, EventIdType, EventType> {

    JoinedAggregate getJoinAggregate();

    void setJoinAggregate(JoinedAggregate rollupAggregate);
}
