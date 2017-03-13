package com.github.rahulsom.grooves.api

import groovy.transform.PackageScope

/**
 * Marks a class as a snapshot. This makes no assumption about the type of snapshot.
 *
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
@PackageScope interface BaseSnapshot<A extends AggregateType, ID> {
    ID getId()
    void setId(ID id)

    A getAggregate()
    void setAggregate(A aggregate)

    A getDeprecatedBy()
    void setDeprecatedBy(A aggregate)

    Set<A> getDeprecates()

    void setLastEvent(BaseEvent<A, ID> event)
}

/**
 * Marks a class as a versioned snapshot
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
trait VersionedSnapshot<A extends AggregateType, ID> implements BaseSnapshot<A, ID> {
    abstract Long getLastEventPosition()
    abstract void setLastEventPosition(Long id)

    @Override
    void setLastEvent(BaseEvent<A, ID> event) {
        this.lastEventPosition = event.position
    }
}

/**
 * Marks a class as a temporal snapshot
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
trait TemporalSnapshot<A extends AggregateType, ID> implements BaseSnapshot<A, ID> {
    abstract Date getLastEventTimestamp()
    abstract void setLastEventTimestamp(Date timestamp)

    @Override
    void setLastEvent(BaseEvent<A, ID> event) {
        this.lastEventTimestamp = event.timestamp
    }
}

/**
 * Marks a class as a snapshot. This supports both temporal and versioned access.
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
trait Snapshot<A extends AggregateType, ID> implements VersionedSnapshot<A, ID>, TemporalSnapshot<A, ID> {

    @Override
    void setLastEvent(BaseEvent<A, ID> event) {
        this.lastEventTimestamp = event.timestamp
        this.lastEventPosition = event.position
    }
}
