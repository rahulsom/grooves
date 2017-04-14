package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;

/**
 * Marks a class as a snapshot. This supports both temporal and versioned access.
 *
 * @param <Aggregate> The Aggregate this snapshot works over
 * @param <SnapshotIdType>
 * @param <EventIdType>
 * @param <EventType>
 *
 * @author Rahul Somasunderam
 */
public interface Snapshot<
        Aggregate extends AggregateType, SnapshotIdType, EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>>
        extends
        VersionedSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType>,
        TemporalSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType> {

    @Override default void setLastEvent(EventType aidBaseEvent) {
        this.setLastEventTimestamp(aidBaseEvent.getTimestamp());
        this.setLastEventPosition(aidBaseEvent.getPosition());
    }

}
