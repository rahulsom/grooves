package com.github.rahulsom.grooves.grails

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.BaseEvent
import com.github.rahulsom.grooves.api.QueryUtil
import com.github.rahulsom.grooves.api.Snapshot
import org.grails.datastore.gorm.GormEntity

import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod

/**
 * Gorm Support for Query Util. <br/>
 * This is the preferred way of writing Grooves applications with Grails.
 *
 * @param <A> The Aggregate type
 * @param <E> The Event type
 * @param <S> The snapshot type
 *
 * @author Rahul Somasunderam
 */
abstract class GormQueryUtil<
        A extends AggregateType & GormEntity<A>,
        E extends BaseEvent<A, E> & GormEntity<E>,
        S extends Snapshot<A, ?> & GormEntity<S>> implements QueryUtil<A, E, S> {

    final Class<A> aggregateClass
    final Class<E> eventClass
    final Class<S> snapshotClass

    GormQueryUtil(Class<A> aggregateClass, Class<E> eventClass, Class<S> snapshotClass) {
        this.aggregateClass = aggregateClass
        this.eventClass = eventClass
        this.snapshotClass = snapshotClass
    }

    public static final Map LATEST = [sort: 'lastEvent', order: 'desc', offset: 0, max: 1]
    public static final Map INCREMENTAL = [sort: 'position', order: 'asc']

    @Override
    final Optional<S> getSnapshot(long startWithEvent, A aggregate) {
        def snapshots = startWithEvent == Long.MAX_VALUE ?
                invokeStaticMethod(snapshotClass, 'findAllByAggregateId',
                        [aggregate.id, LATEST].toArray()) :
                invokeStaticMethod(snapshotClass, 'findAllByAggregateIdAndLastEventLessThan',
                        [aggregate.id, startWithEvent, LATEST].toArray())
        (snapshots ? Optional.of(snapshots[0]) : Optional.empty()) as Optional<S>
    }

    @Override
    final void detachSnapshot(S retval) {
        if (retval.isAttached()) {
            retval.discard()
            retval.id = null
        }
    }

    @Override
    final List<E> getUncomputedEvents(A aggregate, S lastSnapshot, long lastEvent) {
        invokeStaticMethod(eventClass, 'findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals',
                [aggregate, lastSnapshot?.lastEvent ?: 0L, lastEvent, INCREMENTAL].toArray()) as List<E>
    }

    @Override
    final List<E> findEventsForAggregates(List<A> aggregates) {
        invokeStaticMethod(eventClass, 'findAllByAggregateInList', [aggregates, INCREMENTAL].toArray()) as List<? extends E>
    }

}
