package com.github.rahulsom.grooves.api.events

import org.reactivestreams.Publisher

/**
 * Aggregate deprecation event.
 *
 * The aggregate on which this event is applied is considered the loser of the merge.
 * The converse is [Deprecates] which needs to be applied to the other aggregate which wins
 * the merge.
 *
 * @param [AggregateT] Aggregate this event applies to
 * @param [EventIdT] The type for [BaseEvent.id]
 * @param [EventT] The base class for all events in the hierarchy for [AggregateT].
 *
 * @author Rahul Somasunderam
 */
interface DeprecatedBy<AggregateT, EventIdT, EventT> :
    BaseEvent<AggregateT, EventIdT, EventT> {

    /**
     * An Observable of the converse of this event.
     * The converse of a [DeprecatedBy] is a [Deprecates] event that does the exact opposite of this event.
     * It tells you that the aggregate on which you apply the [Deprecates] is going to deprecate this aggregate.
     *
     * That gives Grooves an opportunity to look at this aggregate's events and make them available during the computation of the snapshot.
     */
    fun getConverseObservable(): Publisher<out Deprecates<AggregateT, EventIdT, EventT>>

    /**
     * An Observable of the aggregate that deprecates this event's aggregate.
     * If Grooves is asked to redirect to that aggregate, it will use this observable to do so.
     */
    fun getDeprecatorObservable(): Publisher<AggregateT>
}
