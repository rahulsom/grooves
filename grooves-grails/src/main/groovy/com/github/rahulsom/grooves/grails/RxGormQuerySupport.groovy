package com.github.rahulsom.grooves.grails

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import com.github.rahulsom.grooves.queries.QuerySupport
import grails.gorm.rx.RxEntity
import rx.Observable

import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod

/**
 * Gorm Support for Query Util. <br/>
 * This is the preferred way of writing Grooves applications with Grails.
 *
 * @param <Aggregate> The Aggregate type
 * @param <EventIdType>
 * @param <EventType> The Event type
 * @param <SnapshotIdType>
 * @param <SnapshotType> The snapshot type
 *
 * @author Rahul Somasunderam
 */
abstract class RxGormQuerySupport <
        Aggregate extends AggregateType & RxEntity<Aggregate>,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType> & RxEntity<EventType>,
        SnapshotIdType,
        SnapshotType extends Snapshot<Aggregate, SnapshotIdType, EventIdType, EventType> & RxEntity<SnapshotType>>
        implements
                QuerySupport<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {

    private final Class<EventType>    eventClass
    private final Class<SnapshotType> snapshotClass

    RxGormQuerySupport(Class<EventType> eventClass, Class<SnapshotType> snapshotClass) {
        this.eventClass = eventClass
        this.snapshotClass = snapshotClass
    }

    public static final Map LATEST = [sort: 'lastEventPosition', order: 'desc', offset: 0, max: 1]
    public static final Map INCREMENTAL = [sort: 'position', order: 'asc']

    @Override
    final Observable<SnapshotType> getSnapshot(long maxPosition, Aggregate aggregate) {
        maxPosition == Long.MAX_VALUE ?
                invokeStaticMethod(snapshotClass, 'findAllByAggregateId',
                        [aggregate.id, LATEST].toArray()) as Observable<SnapshotType>:
                invokeStaticMethod(snapshotClass, 'findAllByAggregateIdAndLastEventPositionLessThan',
                        [aggregate.id, maxPosition, LATEST].toArray()) as Observable<SnapshotType>
    }

    @Override
    final Observable<SnapshotType> getSnapshot(Date maxTimestamp, Aggregate aggregate) {
        maxTimestamp == null ?
                invokeStaticMethod(snapshotClass, 'findAllByAggregateId',
                        [aggregate.id, LATEST].toArray()) as Observable<SnapshotType> :
                invokeStaticMethod(snapshotClass, 'findAllByAggregateIdAndLastEventTimestampLessThan',
                        [aggregate.id, maxTimestamp, LATEST].toArray()) as Observable<SnapshotType>
    }

    @Override
    final void detachSnapshot(SnapshotType snapshot) {
        if (snapshot.isAttached()) {
            snapshot.discard()
            snapshot.id = null
        }
    }

    @Override
    final Observable<EventType> getUncomputedEvents(Aggregate aggregate, SnapshotType lastSnapshot, long version) {
        invokeStaticMethod(eventClass, 'findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals',
                [aggregate, lastSnapshot?.lastEventPosition ?: 0L, version, INCREMENTAL].toArray()) as Observable<EventType>
    }

    @Override
    final Observable<EventType> getUncomputedEvents(Aggregate aggregate, SnapshotType lastSnapshot, Date snapshotTime) {
        lastSnapshot.lastEventTimestamp ?
                invokeStaticMethod(eventClass, 'findAllByAggregateAndTimestampGreaterThanAndTimestampLessThanEquals',
                        [aggregate, lastSnapshot.lastEventTimestamp, snapshotTime, INCREMENTAL].toArray()) as Observable<EventType> :
                invokeStaticMethod(eventClass, 'findAllByAggregateAndTimestampLessThanEquals',
                        [aggregate, snapshotTime, INCREMENTAL].toArray()) as Observable<EventType>
    }

    @Override
    final Observable<EventType> findEventsForAggregates(List<Aggregate> aggregates) {
        invokeStaticMethod(eventClass, 'findAllByAggregateInList', [aggregates, INCREMENTAL].toArray()) as Observable<EventType>
    }

}
