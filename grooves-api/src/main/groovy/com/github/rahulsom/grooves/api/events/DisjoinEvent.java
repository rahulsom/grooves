package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

/**
 * Breaks a join from {@link Aggregate} to {@link DisjoinedAggregate} that had existed earlier
 *
 * @param <Aggregate>          The Aggregate that had its link severed
 * @param <EventIdType>        The Type for Event's {@link #getId} field
 * @param <EventType>          The parent event type
 * @param <DisjoinedAggregate> The target aggregate
 * @author Rahul Somasunderam
 */
public interface DisjoinEvent<
        Aggregate extends AggregateType,
        EventIdType,
        EventType,
        DisjoinedAggregate extends AggregateType>
        extends BaseEvent<Aggregate, EventIdType, EventType> {

    DisjoinedAggregate getJoinAggregate();

    void setJoinAggregate(DisjoinedAggregate rollupAggregate);
}
