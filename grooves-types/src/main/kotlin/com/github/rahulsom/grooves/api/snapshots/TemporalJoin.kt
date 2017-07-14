package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin

/**
 * A special kind of [TemporalSnapshot] that stores information about joined entities.
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
interface TemporalJoin<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        JoinIdT,
        JoinedAggregateIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateIdT, AggregateT, EventIdT, in EventT>> :
        TemporalSnapshot<AggregateIdT, AggregateT, JoinIdT, EventIdT, EventT>,
        BaseJoin<AggregateIdT, AggregateT, JoinIdT, JoinedAggregateIdT, EventIdT, EventT>
