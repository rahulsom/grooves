package com.github.rahulsom.grooves.api

/**
 * Revert a prior event. A reverted event's effects are not applied.
 *
 * @author Rahul Somasunderam
 */
interface RevertEvent<A extends AggregateType, E> extends BaseEvent<A, E> {
    E getRevertedEvent()
}
