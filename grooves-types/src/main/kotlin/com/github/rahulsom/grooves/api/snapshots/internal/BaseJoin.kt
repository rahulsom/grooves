package com.github.rahulsom.grooves.api.snapshots.internal

import com.github.rahulsom.grooves.api.events.BaseEvent

/**
 * A special kind of [BaseSnapshot] that stores information about joined entities.
 *
 * @param [AggregateT] The Aggregate this snapshot works over
 * @param [JoinIdT] The type for [BaseJoin.id]
 * @param [EventIdT] The type for [BaseEvent.id]
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface BaseJoin<
        AggregateT,
        JoinIdT,
        in JoinedAggregateT,
        EventIdT,
        in EventT : BaseEvent<AggregateT, EventIdT, in EventT>> :
    BaseSnapshot<AggregateT, JoinIdT, EventIdT, EventT> {

    fun addJoinedAggregate(joinedAggregateT: JoinedAggregateT)
    fun removeJoinedAggregate(joinedAggregateT: JoinedAggregateT)

}
