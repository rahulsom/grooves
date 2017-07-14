package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot

/**
 * Marks a class as a snapshot. This supports both temporal and versioned access.
 *
 * @param [AggregateIdT] The type of [AggregateT.id]
 * @param [AggregateT]   The Aggregate this snapshot works over
 * @param [SnapshotIdT]  The type for [BaseSnapshot.id]
 * @param [EventIdT]     The type for [EventT.id]
 * @param [EventT]       The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface Snapshot<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        SnapshotIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateIdT, AggregateT, EventIdT, in EventT>> :
        VersionedSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>,
        TemporalSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT> {

    override fun setLastEvent(event: EventT) {
        this.lastEventTimestamp = event.timestamp
        this.lastEventPosition = event.position
    }

}
