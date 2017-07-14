package com.github.rahulsom.grooves.api

/**
 * Marks a class as an aggregate.
 *
 * @param [AggregateIdT] The type of [AggregateType.id]
 *
 * @author Rahul Somasunderam
 */
interface AggregateType<out AggregateIdT> {
    /**
     * A unique identifier for the Aggregate.
     * This typically maps to the primary key in a persistence mechanism.
     */
    val id: AggregateIdT?
}
