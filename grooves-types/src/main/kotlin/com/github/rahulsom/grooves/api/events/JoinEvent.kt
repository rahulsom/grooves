package com.github.rahulsom.grooves.api.events

import org.reactivestreams.Publisher

/**
 * Creates a join from [AggregateT] to [JoinedAggregateT] that had not existed earlier.
 *
 * @param [AggregateT] Aggregate this event applies to
 * @param [EventIdT] The type for [BaseEvent.id]
 * @param [EventT] The base class for all events in the hierarchy for [AggregateT].
 * @param [JoinedAggregateT] The target aggregate
 *
 * @author Rahul Somasunderam
 */
interface JoinEvent<
        AggregateT,
        EventIdT,
        EventT,
        JoinedAggregateT> :
    BaseEvent<AggregateT, EventIdT, EventT> {

    /**
     * An observable that points to the aggregate to which a join is being performed.
     *
     * This join is unidirectional, but a good system will always create a pair of events to make all joins bidirectional.
     * That makes it possible for a system at a later date to change it's problem space and answer questions that unidirectional joins can't.
     * Your directionality should match your plan in [DisjoinEvent]
     */
    val joinAggregateObservable: Publisher<JoinedAggregateT>
}