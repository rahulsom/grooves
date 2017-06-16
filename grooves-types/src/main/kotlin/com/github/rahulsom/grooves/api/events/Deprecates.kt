package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType
import rx.Observable

/**
 * Aggregate deprecation event.
 *
 * The aggregate on which this event is applied is considered the winner of the merge.
 * The converse is [DeprecatedBy] which needs to be applied to the other aggregate which
 * loses the merge.
 *
 * @param [AggregateT] Aggregate this event applies to
 * @param [EventIdT] The Type for Event's [.getId] field
 * @param [EventT] The parent event type
 *
 * @author Rahul Somasunderam
 */
interface Deprecates<AggregateIdT, AggregateT : AggregateType<AggregateIdT>, EventIdT, EventT> :
        BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> {
    fun getConverseObservable(): Observable<out DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT>>

    fun getDeprecatedObservable(): Observable<AggregateT>
}
