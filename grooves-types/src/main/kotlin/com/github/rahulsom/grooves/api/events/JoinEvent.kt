package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType
import rx.Observable

/**
 * Creates a join from [AggregateT] to [JoinedAggregateT] that had not existed earlier.
 *
 * @param [AggregateIdT]       The type for [AggregateT.id]
 * @param [AggregateT]         Aggregate this event applies to
 * @param [EventIdT]           The type for [EventT.id]
 * @param [EventT]             The base class for all events in the hierarchy for [AggregateT].
 * @param [JoinedAggregateIdT] The type of [JoinedAggregateT.id]
 * @param [JoinedAggregateT]   The target aggregate
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

    /**
     * An observable that points to the aggregate to which a join is being performed.
     *
     * This join is unidirectional, but a good system will always create a pair of events to make all joins bidirectional.
     * That makes it possible for a system at a later date to change it's problem space and answer questions that unidirectional joins can't.
     * Your directionality should match your plan in [DisjoinEvent]
     */
    val joinAggregateObservable: Observable<JoinedAggregateT>

}
