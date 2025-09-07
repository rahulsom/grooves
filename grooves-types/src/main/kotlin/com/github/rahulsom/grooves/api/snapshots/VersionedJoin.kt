package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin

/**
 * A special kind of [VersionedSnapshot] that stores information about joined entities.
 *
 * @param [AggregateT] The Aggregate this snapshot works over
 * @param [JoinIdT] The type for [BaseJoin.id]
 * @param [EventIdT] The type for [BaseEvent.id]
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface VersionedJoin<
    AggregateT,
    JoinIdT,
    in JoinedAggregateT,
    EventIdT,
    in EventT : BaseEvent<AggregateT, EventIdT, in EventT>,
> :
    VersionedSnapshot<AggregateT, JoinIdT, EventIdT, EventT>,
    BaseJoin<AggregateT, JoinIdT, JoinedAggregateT, EventIdT, EventT>