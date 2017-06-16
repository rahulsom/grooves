package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType
import rx.Observable

/**
 * Breaks a join from [AggregateT] to [DisjoinedAggregateT] that had existed earlier.
 *
 * @param [AggregateT] The Aggregate that had its link severed
 * @param [EventIdT] The Type for Event's [.getId] field
 * @param [EventT] The parent event type
 * @param [DisjoinedAggregateT] The target aggregate
 *
 * @author Rahul Somasunderam
 */
interface DisjoinEvent<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        EventIdT,
        EventT,
        JoinedAggregateIdT,
        JoinedAggregateT : AggregateType<JoinedAggregateIdT>> :
        BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> {

    val joinAggregateObservable: Observable<JoinedAggregateT>

}
