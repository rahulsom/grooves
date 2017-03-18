package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;

import java.util.Date;

/**
 * Marks a class as a temporal snapshot
 *
 * @param <Aggregate>      The Aggregate this snapshot works over
 * @param <SnapshotIdType> Type of Snapshot's `id` field
 * @author Rahul Somasunderam
 */
public interface TemporalSnapshot<
        Aggregate extends AggregateType,
        SnapshotIdType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>>
        extends BaseSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType> {

    Date getLastEventTimestamp();
    void setLastEventTimestamp(Date timestamp);

    @Override default void setLastEvent(EventType event) {
        this.setLastEventTimestamp(event.getTimestamp());
    }
}
