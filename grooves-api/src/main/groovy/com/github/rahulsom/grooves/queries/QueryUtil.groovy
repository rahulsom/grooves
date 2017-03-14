package com.github.rahulsom.grooves.queries

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.internal.BaseEvent
import com.github.rahulsom.grooves.api.Snapshot
import groovy.transform.CompileStatic

/**
 * A trait that simplifies computing temporal snapshots from events
 *
 * @param <A> The Aggregate type
 * @param <E> The Event type
 * @param <S> The snapshot type
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
trait QueryUtil<A extends AggregateType, E extends BaseEvent<A, E>, S extends Snapshot<A, ?>>
        extends VersionedQueryUtil<A,E,S> implements TemporalQueryUtil<A,E,S> {

}
