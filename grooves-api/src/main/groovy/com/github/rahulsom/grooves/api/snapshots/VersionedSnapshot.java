package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;

/**
 * Marks a class as a versioned snapshot.
 *
 * @param <AggregateT>  The Aggregate this snapshot works over
 * @param <SnapshotIdT> The type for the snapshot's {@link #getId()} field
 * @param <EventIdT>    The type for the {@link EventT}'s id field
 * @param <EventT>      The base type for events that apply to {@link AggregateT}
 *
 * @author Rahul Somasunderam
 */
public interface VersionedSnapshot<
        AggregateT extends AggregateType,
        SnapshotIdT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>>
        extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT> {

    Long getLastEventPosition();

    void setLastEventPosition(Long id);

    @Override
    default void setLastEvent(EventT aidBaseEvent) {
        this.setLastEventPosition(aidBaseEvent.getPosition());
    }
}
