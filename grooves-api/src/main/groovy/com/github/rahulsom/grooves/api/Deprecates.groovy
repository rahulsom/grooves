package com.github.rahulsom.grooves.api

/**
 * A deprecation event.
 * <br/>
 * The aggregate on which this event is applied is considered the winner of the merge.
 * The converse is {@link DeprecatedBy} which needs to be applied to the other aggregate which loses the merge.
 *
 * @author Rahul Somasunderam
 */
interface Deprecates<A extends AggregateType> extends BaseEvent<A> {
    DeprecatedBy<A> getConverse()

    A getDeprecated()
}
