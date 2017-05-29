package com.github.rahulsom.grooves.api.events

import com.github.rahulsom.grooves.api.AggregateType
import rx.Observable
import java.util.Date

/**
 * Base class for Events.
 *
 * @param [AggregateT] Aggregate this event applies to
 * @param [EventIdT] The Type for Event's [.getId] field
 * @param [EventT] Event Type
 *
 * @author Rahul Somasunderam
 */
interface BaseEvent<AggregateIdT, AggregateT : AggregateType<AggregateIdT>, EventIdT, EventT> {

    fun getAggregateObservable(): Observable<AggregateT>
    var aggregate: AggregateT?

    var timestamp: Date?

    var createdBy: String?

    var revertedBy: RevertEvent<AggregateIdT, AggregateT, EventIdT, EventT>?

    val id: EventIdT?

    var position: Long?

    fun getAudit(): String
}
