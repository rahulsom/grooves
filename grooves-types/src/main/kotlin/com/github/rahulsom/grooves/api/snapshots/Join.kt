package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin

/**
 * A special kind of [Snapshot] that stores information about joined entities.
 * This is temporal as well as versioned.
 *
 * @param [AggregateT] The Aggregate this snapshot works over
 * @param [JoinIdT] The type for [BaseJoin.id]
 * @param [EventIdT] The type for [BaseEvent.id]
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface Join<
        AggregateT,
        JoinIdT,
        in JoinedAggregateT,
        EventIdT,
        in EventT : BaseEvent<AggregateT, EventIdT, in EventT>> :
    TemporalJoin<AggregateT, JoinIdT, JoinedAggregateT, EventIdT, EventT>,
    VersionedJoin<AggregateT, JoinIdT, JoinedAggregateT, EventIdT, EventT>,
    Snapshot<AggregateT, JoinIdT, EventIdT, EventT>
