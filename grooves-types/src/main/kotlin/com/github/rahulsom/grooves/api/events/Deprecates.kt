package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType
import org.reactivestreams.Publisher

/**
 * Aggregate deprecation event.
 *
 * The aggregate on which this event is applied is considered the winner of the merge.
 * The converse is [DeprecatedBy] which needs to be applied to the other aggregate which
 * loses the merge.
 *
 * @param [AggregateIdT] The type for [AggregateT.id]
 * @param [AggregateT]   Aggregate this event applies to
 * @param [EventIdT]     The type for [EventT.id]
 * @param [EventT]       The base class for all events in the hierarchy for [AggregateT].
 *
 * @author Rahul Somasunderam
 */
interface Deprecates<AggregateIdT, AggregateT : AggregateType<AggregateIdT>, EventIdT, EventT> :
        BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> {

    /**
     * A Publisher of the converse of this event.
     * The converse of a [Deprecates] is a [DeprecatedBy] event that does the exact opposite of this event.
     * It tells you that the aggregate on which you apply the [DeprecatedBy] has been deprecated by this event's aggregate.
     * That gives Grooves an opportunity to redirect if asked to this event's aggregate.
     */
    fun getConverseObservable(): Publisher<out DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT>>

    /**
     * A Publisher of the aggregate that was deprecated by this event's aggregate.
     * Grooves will use this to find events that are on the other aggregate and make them available for computation of this aggregate's snapshot.
     */
    fun getDeprecatedObservable(): Publisher<AggregateT>
}
