package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin

/**
 * A special kind of [VersionedSnapshot] that stores information about joined entities.
 *
 * @param [AggregateT] The Aggregate this join represents
 * @param [JoinIdT] The type for the join's [.getId] field
 * @param [JoinedAggregateIdT] The type for the other aggregate that [AggregateT] joins to
 * @param [EventIdT] The type for the [EventT]'s id field
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface VersionedJoin<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        JoinIdT,
        JoinedAggregateIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateIdT, AggregateT, EventIdT, in EventT>> :
        VersionedSnapshot<AggregateIdT, AggregateT, JoinIdT, EventIdT, EventT>,
        BaseJoin<AggregateIdT, AggregateT, JoinIdT, JoinedAggregateIdT, EventIdT, EventT>
