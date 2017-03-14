package com.github.rahulsom.grooves.queries

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.internal.BaseEvent
import com.github.rahulsom.grooves.api.BaseSnapshot
import com.github.rahulsom.grooves.queries.internal.BaseQuery
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A trait that simplifies computing snapshots from events
 *
 * @param <A> The Aggregate type
 * @param <E> The Event type
 * @param <S> The snapshot type
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
trait BaseQueryUtil<A extends AggregateType, E extends BaseEvent<A, E>, S extends BaseSnapshot<A, ?>>
        implements BaseQuery<A, E, S> {
    private static Logger log = LoggerFactory.getLogger(getClass())

}
