package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot
import java.util.Date

/**
 * Marks a class as a temporal snapshot.
 *
 * @param [AggregateT] The Aggregate this snapshot works over
 * @param [SnapshotIdT] The type for [BaseSnapshot.id]
 * @param [EventIdT] The type for [BaseEvent.id]
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface TemporalSnapshot<
        AggregateT,
        SnapshotIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateT, EventIdT, in EventT>> :
    BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT> {

    /**
     * The timestamp of the last event that this snapshot represents.
     *
     * This is useful in finding a matching or suitable snapshot based on the timestamp of a snapshot.
     */
    var lastEventTimestamp: Date?
}