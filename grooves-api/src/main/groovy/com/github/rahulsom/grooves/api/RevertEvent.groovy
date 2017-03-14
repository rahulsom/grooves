package com.github.rahulsom.grooves.api

import com.github.rahulsom.grooves.api.internal.BaseEvent

/**
 * Revert a prior event. A reverted event's effects are not applied.
 *
 * @author Rahul Somasunderam
 */
interface RevertEvent<A extends AggregateType, E> extends BaseEvent<A, E> {
    public <T> T getRevertedEventId()
}
