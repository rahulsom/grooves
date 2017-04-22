package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;

import java.util.Date;

/**
 * Marks a class as a temporal snapshot.
 *
 * @param <AggregateT>  The Aggregate this snapshot works over
 * @param <SnapshotIdT> The type for the snapshot's {@link #getId()} field
 * @param <EventIdT>    The type for the {@link EventT}'s id field
 * @param <EventT>      The base type for events that apply to {@link AggregateT}
 *
 * @author Rahul Somasunderam
 */
public interface TemporalSnapshot<
        AggregateT extends AggregateType,
        SnapshotIdT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>>
        extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT> {

    Date getLastEventTimestamp();

    void setLastEventTimestamp(Date timestamp);

    @Override
    default void setLastEvent(EventT event) {
        this.setLastEventTimestamp(event.getTimestamp());
    }
}
