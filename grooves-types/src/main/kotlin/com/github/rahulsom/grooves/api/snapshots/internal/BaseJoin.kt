package com.github.rahulsom.grooves.api.snapshots.internal

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent

/**
 * A special kind of [BaseSnapshot] that stores information about joined entities.
 *
 * @param [AggregateIdT]       The type of [AggregateT.id]
 * @param [AggregateT]         The Aggregate this snapshot works over
 * @param [JoinIdT]            The type for [BaseJoin.id]
 * @param [JoinedAggregateIdT] The type for the id of the aggregate that [AggregateT] to
 * @param [EventIdT]           The type for [EventT.id]
 * @param [EventT]             The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface BaseJoin<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        JoinIdT,
        JoinedAggregateIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateIdT, AggregateT, EventIdT, in EventT>> :
        BaseSnapshot<AggregateIdT, AggregateT, JoinIdT, EventIdT, EventT> {

    /**
     * The list of [JoinedAggregateIdT] that are joined to [BaseSnapshot.aggregate].
     *
     * TODO Turn to SortedSet
     */
    var joinedIds: List<JoinedAggregateIdT>

}
