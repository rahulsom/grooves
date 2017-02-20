package com.github.rahulsom.grooves.api

/**
 * A deprecation event.
 * <br/>
 * The aggregate on which this event is applied is considered the loser of the merge.
 * The converse is {@link Deprecates} which needs to be applied to the other aggregate which wins the merge.
 *
 * @author Rahul Somasunderam
 */
interface DeprecatedBy<A extends AggregateType> extends BaseEvent<A> {
    Deprecates<A> getConverse()

    A getDeprecator()
}
