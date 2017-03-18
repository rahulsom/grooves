package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;

/**
 * Marks a class as a versioned snapshot
 *
 * @param <Aggregate> The Aggregate this snapshot works over
 * @author Rahul Somasunderam
 */
public interface VersionedSnapshot<Aggregate extends AggregateType, SnapshotIdType, EventIdType, EventType extends BaseEvent<Aggregate, EventIdType, EventType>>
        extends BaseSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType> {

    Long getLastEventPosition();
    void setLastEventPosition(Long id);

    @Override
    default void setLastEvent(EventType aidBaseEvent) {
        this.setLastEventPosition(aidBaseEvent.getPosition());
    }
}
