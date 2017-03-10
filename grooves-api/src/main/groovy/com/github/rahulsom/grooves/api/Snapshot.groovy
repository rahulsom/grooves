package com.github.rahulsom.grooves.api

/**
 * Marks a class as a snapshot
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
interface Snapshot<A extends AggregateType, ID> {
    ID getId()
    void setId(ID id)

    A getAggregate()
    void setAggregate(A aggregate)

    A getDeprecatedBy()
    void setDeprecatedBy(A aggregate)

    Long getLastEvent()
    void setLastEvent(Long id)

    Date getLastEventTimestamp()
    void setLastEventTimestamp(Date timestamp)

    Set<A> getDeprecates()
}
