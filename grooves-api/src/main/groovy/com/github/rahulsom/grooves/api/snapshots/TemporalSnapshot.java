package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;

import java.util.Date;

/**
 * Marks a class as a temporal snapshot
 *
 * @param <Aggregate>      The Aggregate this snapshot works over
 * @param <SnapshotIdType> The type for the snapshot's {@link #getId()} field
 * @param <EventIdType>    The type for the {@link EventType}'s id field
 * @param <EventType>      The base type for events that apply to {@link Aggregate}
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

    @Override
    default void setLastEvent(EventType event) {
        this.setLastEventTimestamp(event.getTimestamp());
    }
}
