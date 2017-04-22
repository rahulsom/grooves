package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;

/**
 * @param <Aggregate>             The Aggregate this join represents
 * @param <JoinIdType>            The type for the join's {@link #getId()} field
 * @param <JoinedAggregateIdType> The type for the other aggregate that {@link Aggregate} joins to
 * @param <EventIdType>           The type for the {@link EventType}'s id field
 * @param <EventType>             The base type for events that apply to {@link Aggregate}
 * @author Rahul Somasunderam
 */
public interface Join<
        Aggregate extends AggregateType, JoinIdType, JoinedAggregateIdType, EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>>
        extends
        TemporalJoin<Aggregate, JoinIdType, JoinedAggregateIdType, EventIdType, EventType>,
        VersionedJoin<Aggregate, JoinIdType, JoinedAggregateIdType, EventIdType, EventType>,
        Snapshot<Aggregate, JoinIdType, EventIdType, EventType> {

    @Override
    default void setLastEvent(EventType aidBaseEvent) {
        this.setLastEventTimestamp(aidBaseEvent.getTimestamp());
        this.setLastEventPosition(aidBaseEvent.getPosition());
    }
}
