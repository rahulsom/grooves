package com.github.rahulsom.grooves.api.snapshots

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent

/**
 * A special kind of [Snapshot] that stores information about joined entities.
 *
 * @param [AggregateT] The Aggregate this join represents
 * @param [JoinIdT] The type for the join's [.getId] field
 * @param [JoinedAggregateIdT] The type for the other aggregate that [AggregateT] joins to
 * @param [EventIdT] The type for the [EventT]'s id field
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface Join<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        JoinIdT,
        JoinedAggregateIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateIdT, AggregateT, EventIdT, in EventT>> :
        TemporalJoin<AggregateIdT, AggregateT, JoinIdT, JoinedAggregateIdT, EventIdT, EventT>,
        VersionedJoin<AggregateIdT, AggregateT, JoinIdT, JoinedAggregateIdT, EventIdT, EventT>,
        Snapshot<AggregateIdT, AggregateT, JoinIdT, EventIdT, EventT> {

    override fun setLastEvent(event: EventT) {
        this.lastEventTimestamp = event.timestamp
        this.lastEventPosition = event.position
    }

}
