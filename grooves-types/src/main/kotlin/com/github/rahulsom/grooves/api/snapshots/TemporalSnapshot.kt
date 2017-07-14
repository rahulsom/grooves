package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot
import java.util.Date

/**
 * Marks a class as a temporal snapshot.
 *
 * @param [AggregateIdT] The type of [AggregateT.id]
 * @param [AggregateT]   The Aggregate this snapshot works over
 * @param [SnapshotIdT]  The type for [BaseSnapshot.id]
 * @param [EventIdT]     The type for [EventT.id]
 * @param [EventT]       The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface TemporalSnapshot<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        SnapshotIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateIdT, AggregateT, EventIdT, in EventT>> :
        BaseSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT> {

    /**
     * The timestamp of the last event that this snapshot represents.
     *
     * This is useful in finding a matching or suitable snapshot based on the timestamp of a snapshot.
     */
    var lastEventTimestamp: Date?

    override fun setLastEvent(event: EventT) {
        this.lastEventTimestamp = event.timestamp
    }
}
