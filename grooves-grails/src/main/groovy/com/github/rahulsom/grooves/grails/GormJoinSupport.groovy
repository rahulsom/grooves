package com.github.rahulsom.grooves.grails

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.snapshots.Join
import com.github.rahulsom.grooves.queries.JoinSupport
import com.github.rahulsom.grooves.queries.internal.Executor
import com.github.rahulsom.grooves.queries.internal.JoinExecutor
import org.grails.datastore.gorm.GormEntity
import rx.Observable

import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod

/**
 *
 * @param < Aggregate >
 * @param < EventIdType >
 * @param < EventType >
 * @param < JoinedAggregateIdType >
 * @param < JoinedAggregateType >
 * @param < SnapshotIdType >
 * @param < SnapshotType >
 * @param < JoinE >
 * @param < DisjoinE >
 *
 * @author Rahul Somasunderam
 */
abstract class GormJoinSupport<
        Aggregate extends AggregateType & GormEntity<Aggregate>,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType> & GormEntity<EventType>,
        JoinedAggregateIdType,
        JoinedAggregateType extends AggregateType & GormEntity<JoinedAggregateType>,
        SnapshotIdType,
        SnapshotType extends Join<Aggregate, SnapshotIdType, JoinedAggregateIdType, EventIdType, EventType> & GormEntity<SnapshotType>,
        JoinE extends JoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>,
        DisjoinE extends DisjoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>>
        implements
                JoinSupport<Aggregate, EventIdType, EventType, JoinedAggregateIdType, JoinedAggregateType, SnapshotIdType, SnapshotType, JoinE, DisjoinE> {

    private final Class<Aggregate> classAggregate
    private final Class<EventIdType> classEventIdType
    private final Class<EventType> eventClass
    private final Class<JoinedAggregateIdType> classJoinedAggregateIdType
    private final Class<JoinedAggregateType> joinedAggregateClass
    private final Class<SnapshotIdType> snapshotIdClass
    private final Class<SnapshotType> snapshotClass
    final Class<JoinE> joinEventClass
    final Class<DisjoinE> disjoinEventClass

    GormJoinSupport(
            Class<Aggregate> classAggregate,
            Class<EventIdType> classEventIdType, Class<EventType> eventClass,
            Class<JoinedAggregateIdType> classJoinedAggregateIdType, Class<JoinedAggregateType> joinedAggregateClass,
            Class<SnapshotIdType> snapshotIdClass, Class<SnapshotType> snapshotClass,
            Class<JoinE> joinEventClass, Class<DisjoinE> disjoinEventClass) {
        this.classAggregate = classAggregate
        this.classEventIdType = classEventIdType
        this.eventClass = eventClass
        this.classJoinedAggregateIdType = classJoinedAggregateIdType
        this.joinedAggregateClass = joinedAggregateClass
        this.snapshotIdClass = snapshotIdClass
        this.snapshotClass = snapshotClass
        this.joinEventClass = joinEventClass
        this.disjoinEventClass = disjoinEventClass
    }

    public static final Map LATEST = [sort: 'lastEventPosition', order: 'desc', offset: 0, max: 1]
    public static final Map INCREMENTAL = [sort: 'position', order: 'asc']

    @Override
    final Observable<SnapshotType> getSnapshot(long maxPosition, Aggregate aggregate) {
        List<SnapshotType> snapshots = maxPosition == Long.MAX_VALUE ?
                invokeStaticMethod(snapshotClass, 'findAllByAggregateId',
                        [aggregate.id, LATEST].toArray()) as List<SnapshotType> :
                invokeStaticMethod(snapshotClass, 'findAllByAggregateIdAndLastEventPositionLessThan',
                        [aggregate.id, maxPosition, LATEST].toArray()) as List<SnapshotType>
        snapshots ? Observable.just(snapshots[0]) : Observable.empty()
    }

    @Override
    final Observable<SnapshotType> getSnapshot(Date maxTimestamp, Aggregate aggregate) {
        List<SnapshotType> snapshots = maxTimestamp == null ?
                invokeStaticMethod(snapshotClass, 'findAllByAggregateId',
                        [aggregate.id, LATEST].toArray()) as List<SnapshotType> :
                invokeStaticMethod(snapshotClass, 'findAllByAggregateIdAndLastEventTimestampLessThan',
                        [aggregate.id, maxTimestamp, LATEST].toArray()) as List<SnapshotType>
        snapshots ? Observable.just(snapshots[0]) : Observable.empty()
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
        Observable.from(
                invokeStaticMethod(eventClass, 'findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals',
                        [aggregate, lastSnapshot?.lastEventPosition ?: 0L, version, INCREMENTAL].toArray()) as List<EventType>)
    }

    @Override
    final Observable<EventType> getUncomputedEvents(Aggregate aggregate, SnapshotType lastSnapshot, Date snapshotTime) {
        Observable.from(lastSnapshot.lastEventTimestamp ?
                invokeStaticMethod(eventClass, 'findAllByAggregateAndTimestampGreaterThanAndTimestampLessThanEquals',
                        [aggregate, lastSnapshot.lastEventTimestamp, snapshotTime, INCREMENTAL].toArray()) as List<EventType> :
                invokeStaticMethod(eventClass, 'findAllByAggregateAndTimestampLessThanEquals',
                        [aggregate, snapshotTime, INCREMENTAL].toArray()) as List<EventType>)
    }

    @Override
    final Observable<EventType> findEventsForAggregates(List<Aggregate> aggregates) {
        Observable.from(invokeStaticMethod(eventClass, 'findAllByAggregateInList', [aggregates, INCREMENTAL].toArray()) as List<EventType>)
    }

    @Override
    Executor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        new JoinExecutor<Aggregate, EventIdType, EventType, JoinedAggregateIdType, JoinedAggregateType, SnapshotIdType, SnapshotType, JoinE, DisjoinE>(
                joinEventClass, disjoinEventClass)
    }

}
