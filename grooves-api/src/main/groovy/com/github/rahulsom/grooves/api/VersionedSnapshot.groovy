package com.github.rahulsom.grooves.api

import com.github.rahulsom.grooves.api.internal.BaseEvent

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
