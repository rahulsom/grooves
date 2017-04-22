package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;

/**
 * Marks a class as a snapshot. This supports both temporal and versioned access.
 *
 * @param <AggregateT>  The Aggregate this snapshot works over
 * @param <SnapshotIdT> The type for the snapshot's {@link #getId()} field
 * @param <EventIdT>    The type for the {@link EventT}'s id field
 * @param <EventT>      The base type for events that apply to {@link AggregateT}
 *                
 * @author Rahul Somasunderam
 */
public interface Snapshot<
        AggregateT extends AggregateType, SnapshotIdT, EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>>
        extends
        VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
        TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT> {

    @Override
    default void setLastEvent(EventT aidBaseEvent) {
        this.setLastEventTimestamp(aidBaseEvent.getTimestamp());
        this.setLastEventPosition(aidBaseEvent.getPosition());
    }

}
