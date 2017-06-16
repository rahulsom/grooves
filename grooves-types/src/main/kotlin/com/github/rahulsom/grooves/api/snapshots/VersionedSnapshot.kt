package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot

/**
 * Marks a class as a versioned snapshot.
 *
 * @param [AggregateT] The Aggregate this snapshot works over
 * @param [SnapshotIdT] The type for the snapshot's [.getId] field
 * @param [EventIdT] The type for the [EventT]'s id field
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface VersionedSnapshot<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        SnapshotIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateIdT, AggregateT, EventIdT, in EventT>> :
        BaseSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT> {

    var lastEventPosition: Long?

    override fun setLastEvent(event: EventT) {
        this.lastEventPosition = event.position
    }
}
