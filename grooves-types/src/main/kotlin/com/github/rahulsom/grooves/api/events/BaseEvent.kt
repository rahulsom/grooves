package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType
import rx.Observable
import java.util.Date

/**
 * Base class for Events corresponding to [AggregateT].
 *
 * @param [AggregateIdT] The type for [AggregateType.id]
 * @param [AggregateT]   Aggregate this event applies to
 * @param [EventIdT]     The type for [BaseEvent.id]
 * @param [EventT]       The base class for all events in the hierarchy for [AggregateT]. In
 *                       this case, it is a self reference.
 *
 * @author Rahul Somasunderam
 */
interface BaseEvent<AggregateIdT, AggregateT : AggregateType<AggregateIdT>, EventIdT, EventT> {

    /**
     * Returns the aggregate as an Observable.
     * This can be used in a reactive way to obtain the aggregate.
     * Grooves will use this method when it needs the aggregate.
     */
    fun getAggregateObservable(): Observable<AggregateT>

    /**
     * A reference to the aggregate that this event applies to.
     */
    var aggregate: AggregateT?

    /**
     * The timestamp corresponding to the event.
     * This is the time an event was received by the system.
     * It is important that the system store/apply events in order.
     */
    var timestamp: Date?

    /**
     * The event that reverted this event.
     * This should be implemented as a transient in any persistence mechanism.
     * When a [RevertEvent] occurs such that it reverts this event, it will set itself to this field.
     * This is useful when building auditing solutions.
     */
    var revertedBy: RevertEvent<AggregateIdT, AggregateT, EventIdT, EventT>?

    /**
     * A unique identifier for the Event.
     * This typically maps to the primary key in a persistence mechanism.
     */
    val id: EventIdT?

    /**
     * The ordinal position of this Event.
     * Systems can choose between using global positions, i.e. a version number for the whole system, or, an aggregate specific position, i.e. a version number for the aggregate.
     */
    var position: Long?

}
