package com.github.rahulsom.grooves.api.snapshots.internal

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import org.reactivestreams.Publisher

/**
 * Marks a class as a snapshot. This makes no assumption about the type of snapshot.
 *
 * @param [AggregateIdT] The type of [AggregateType.id]
 * @param [AggregateT] The Aggregate this snapshot works over
 * @param [SnapshotIdT] The type for [id]
 * @param [EventIdT] The type for [BaseEvent.id]
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

    /**
     * A unique identifier for the Snapshot.
     * This typically maps to the primary key in a persistence mechanism.
     */
    var id: SnapshotIdT?

    /**
     * Returns the aggregate as an Observable.
     * This can be used in a reactive way to obtain the aggregate.
     * Grooves will use this method when it needs the aggregate.
     */
    fun getAggregateObservable(): Publisher<AggregateT>

    /**
     * Accepts an aggregate and sets that as the aggregate that this snapshot represents the state of.
     *
     * This can be used for querying to speed up retrieval of snapshots when a request for one is made.
     */
    fun setAggregate(aggregate: AggregateT)

    /**
     * An Observable of the aggregate that deprecated this snapshot's aggregate.
     * Grooves will use this to perform a redirect if asked to.
     */
    fun getDeprecatedByObservable(): Publisher<AggregateT>

    /**
     * Accepts an aggregate and sets that as the aggregate that this snapshot's aggregate has been deprecated by.
     */
    fun setDeprecatedBy(deprecatingAggregate: AggregateT)

    /**
     * An Observable of aggregates that were deprecated by the aggregate of this snapshot.
     * Grooves will use this to find events that are on the other aggregate and make them available for computation of this aggregate's snapshot.
     */
    fun getDeprecatesObservable(): Publisher<AggregateT>

}
