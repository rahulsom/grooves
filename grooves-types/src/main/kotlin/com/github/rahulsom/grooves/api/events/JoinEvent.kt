package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType
import rx.Observable

/**
 * Creates a join from [AggregateT] to [JoinedAggregateT] that had not existed earlier.
 *
 * @param [AggregateT] The Aggregate that has been linked to the [JoinedAggregateT]
 * @param [EventIdT] The Type for Event's [.getId] field
 * @param [EventT] The parent event type
 * @param [JoinedAggregateT] The target aggregate
 *
 * @author Rahul Somasunderam
 */
interface JoinEvent<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        EventIdT,
        EventT,
        JoinedAggregateIdT,
        JoinedAggregateT : AggregateType<JoinedAggregateIdT>> :
        BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> {

    val joinAggregateObservable: Observable<JoinedAggregateT>

}
