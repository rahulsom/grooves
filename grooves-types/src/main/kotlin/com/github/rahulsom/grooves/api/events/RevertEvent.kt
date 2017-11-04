package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType

/**
 * Revert a prior event. A reverted event's effects are not applied when computing a snapshot.
 *
 * @param [AggregateIdT] The type for [AggregateType.id]
 * @param [AggregateT] Aggregate this event applies to
 * @param [EventIdT] The type for [BaseEvent.id]
 * @param [EventT] The base class for all events in the hierarchy for [AggregateT].
 *
 * @author Rahul Somasunderam
 */
interface RevertEvent<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        EventIdT,
        EventT> :
        BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> {

    /**
     * [BaseEvent.id] from the event that should be reverted.
     */
    val revertedEventId: EventIdT
}
