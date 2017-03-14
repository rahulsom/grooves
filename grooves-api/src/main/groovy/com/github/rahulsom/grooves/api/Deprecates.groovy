package com.github.rahulsom.grooves.api

import com.github.rahulsom.grooves.api.internal.BaseEvent

/**
 * A deprecation event.
 * <br/>
 * The aggregate on which this event is applied is considered the winner of the merge.
 * The converse is {@link DeprecatedBy} which needs to be applied to the other aggregate which loses the merge.
 *
 * @author Rahul Somasunderam
 */
interface Deprecates<A extends AggregateType, E> extends BaseEvent<A, E> {
    DeprecatedBy<A, E> getConverse()

    A getDeprecated()
}
