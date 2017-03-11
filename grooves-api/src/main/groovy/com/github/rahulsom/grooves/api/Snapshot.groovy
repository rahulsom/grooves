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
}

/**
 * Marks a class as a versioned snapshot
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
interface VersionedSnapshot<A extends AggregateType, ID> extends BaseSnapshot<A, ID> {
    Long getLastEvent()
    void setLastEvent(Long id)
}

/**
 * Marks a class as a temporal snapshot
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
interface TemporalSnapshot<A extends AggregateType, ID> extends BaseSnapshot<A, ID> {
    Date getLastEventTimestamp()
    void setLastEventTimestamp(Date timestamp)
}

/**
 * Marks a class as a snapshot. This supports both temporal and versioned access.
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
interface Snapshot<A extends AggregateType, ID> extends VersionedSnapshot<A, ID>, TemporalSnapshot<A, ID> {}
