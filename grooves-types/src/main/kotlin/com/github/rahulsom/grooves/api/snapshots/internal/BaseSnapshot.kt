package com.github.rahulsom.grooves.api.snapshots.internal

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import rx.Observable

/**
 * Marks a class as a snapshot. This makes no assumption about the type of snapshot.
 *
 * @param [AggregateT] The Aggregate this snapshot works over
 * @param [SnapshotIdT] The type for the snapshot's [.getId] field
 * @param [EventIdT] The type for the [EventT]'s id field
 * @param [EventT] The base type for events that apply to [AggregateT]
 *
 * @author Rahul Somasunderam
 */
interface BaseSnapshot<
        AggregateIdT,
        AggregateT : AggregateType<AggregateIdT>,
        SnapshotIdT,
        EventIdT,
        in EventT : BaseEvent<AggregateIdT, AggregateT, EventIdT, in EventT>> {

    var id: SnapshotIdT?

    fun getAggregateObservable(): Observable<AggregateT>

    fun setAggregate(aggregate: AggregateT): Unit

    fun getDeprecatedByObservable(): Observable<AggregateT>

    fun setDeprecatedBy(deprecatingAggregate: AggregateT): Unit

    fun getDeprecatesObservable(): Observable<AggregateT>

    fun setLastEvent(event: EventT): Unit
}
