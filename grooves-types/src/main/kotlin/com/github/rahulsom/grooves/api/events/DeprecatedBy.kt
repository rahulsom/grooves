package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType
import rx.Observable

/**
 * Aggregate deprecation event.
 *
 * The aggregate on which this event is applied is considered the loser of the merge.
 * The converse is [Deprecates] which needs to be applied to the other aggregate which wins
 * the merge.
 *
 * @param <AggregateT> Aggregate this event applies to
 * @param <EventIdT>   The Type for Event's [.getId] field
 * @param <EventT>     The parent event type
 *
 * @author Rahul Somasunderam
 */
interface DeprecatedBy<AggregateIdT, AggregateT : AggregateType<AggregateIdT>, EventIdT, EventT> :
        BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> {
    fun getConverseObservable(): Observable<out Deprecates<AggregateIdT, AggregateT, EventIdT, EventT>>

    fun getDeprecatorObservable(): Observable<AggregateT>
}
