package com.github.rahulsom.grooves.api

import com.github.rahulsom.grooves.api.internal.BaseEvent

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
