package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;

public interface Join<
        Aggregate extends AggregateType, JoinIdType, JoinedAggregateIdType, EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>>
        extends
        TemporalJoin<Aggregate, JoinIdType, JoinedAggregateIdType, EventIdType, EventType>,
        VersionedJoin<Aggregate, JoinIdType, JoinedAggregateIdType, EventIdType, EventType>,
        Snapshot<Aggregate, JoinIdType, EventIdType, EventType> {

    @Override default void setLastEvent(EventType aidBaseEvent) {
        this.setLastEventTimestamp(aidBaseEvent.getTimestamp());
        this.setLastEventPosition(aidBaseEvent.getPosition());
    }
}
