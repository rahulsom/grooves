package com.github.rahulsom.grooves.api.snapshots.internal

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent

/**
 * A special kind of [BaseSnapshot] that stores information about joined entities.
 *
 * @param <AggregateT>         The Aggregate this join represents
 * @param <JoinIdT>            The type for the join's [.getId] field
 * @param <JoinedAggregateIdT> The type for the other aggregate that [AggregateT] joins to
 * @param <EventIdT>           The type for the [EventT]'s id field
 * @param <EventT>             The base type for events that apply to [AggregateT]
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

    var joinedIds: List<JoinedAggregateIdT>

}
