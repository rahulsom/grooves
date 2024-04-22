package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot

/**
 * Marks a class as a snapshot. This supports both temporal and versioned access.
 *
 * @param [AggregateT] The Aggregate this snapshot works over
 * @param [SnapshotIdT] The type for [BaseSnapshot.id]
 * @param [EventIdT] The type for [BaseEvent.id]
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface Snapshot<
    AggregateT,
    SnapshotIdT,
    EventIdT,
    in EventT : BaseEvent<AggregateT, EventIdT, in EventT>,
    > :
    VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
    TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>