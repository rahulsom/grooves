package com.github.rahulsom.grooves.api

import com.github.rahulsom.grooves.api.internal.BaseEvent

/**
 * Marks a class as a snapshot. This makes no assumption about the type of snapshot.
 *
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
interface BaseSnapshot<A extends AggregateType, ID> {
    ID getId()

    void setId(ID id)

    A getAggregate()

    void setAggregate(A aggregate)

    A getDeprecatedBy()

    void setDeprecatedBy(A aggregate)

    Set<A> getDeprecates()

    void setLastEvent(BaseEvent<A, ID> event)
}
