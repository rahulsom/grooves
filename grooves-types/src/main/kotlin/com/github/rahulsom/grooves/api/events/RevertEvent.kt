package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType

/**
 * Revert a prior event. Aggregate reverted event's effects are not applied.
 *
 * @param [AggregateT] Aggregate this event applies to
 * @param [EventIdT] The Type for Event's [.getId] field
 * @param [EventT] Event Type that could be reverted
 *
 * @author Rahul Somasunderam
 */
interface RevertEvent<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        EventIdT,
        EventT> :
        BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> {

    val revertedEventId: EventIdT
}
