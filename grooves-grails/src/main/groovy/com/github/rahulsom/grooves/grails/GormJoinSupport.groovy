package com.github.rahulsom.grooves.grails

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.snapshots.Join
import com.github.rahulsom.grooves.queries.JoinSupport
import com.github.rahulsom.grooves.queries.internal.JoinExecutor
import com.github.rahulsom.grooves.queries.internal.QueryExecutor
import org.grails.datastore.gorm.GormEntity

import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod

/**
 * Created by rahul on 3/18/17.
 */
abstract class GormJoinSupport<
        Aggregate extends AggregateType & GormEntity<Aggregate>,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType> & GormEntity<EventType>,
        JoinedAggregateIdType,
        JoinedAggregateType extends AggregateType  & GormEntity<JoinedAggregateType>,
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
    private final Class<JoinE> joinEventClass
    private final Class<DisjoinE> disjoinEventClass

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
    final Optional<SnapshotType> getSnapshot(long startWithEvent, Aggregate aggregate) {
        def snapshots = startWithEvent == Long.MAX_VALUE ?
                invokeStaticMethod(snapshotClass, 'findAllByAggregateId',
                        [aggregate.id, LATEST].toArray()) :
                invokeStaticMethod(snapshotClass, 'findAllByAggregateIdAndLastEventPositionLessThan',
                        [aggregate.id, startWithEvent, LATEST].toArray())
        (snapshots ? Optional.of(snapshots[0]) : Optional.empty()) as Optional<SnapshotType>
    }

    @Override
    final Optional<SnapshotType> getSnapshot(Date startAtTime, Aggregate aggregate) {
        def snapshots = startAtTime == null ?
                invokeStaticMethod(snapshotClass, 'findAllByAggregateId',
                        [aggregate.id, LATEST].toArray()) :
                invokeStaticMethod(snapshotClass, 'findAllByAggregateIdAndLastEventTimestampLessThan',
                        [aggregate.id, startAtTime, LATEST].toArray())
        (snapshots ? Optional.of(snapshots[0]) : Optional.empty()) as Optional<SnapshotType>
    }

    @Override
    final void detachSnapshot(SnapshotType retval) {
        if (retval.isAttached()) {
            retval.discard()
            retval.id = null
        }
    }

    @Override
    final List<EventType> getUncomputedEvents(Aggregate aggregate, SnapshotType lastSnapshot, long version) {
        invokeStaticMethod(eventClass, 'findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals',
                [aggregate, lastSnapshot?.lastEventPosition ?: 0L, version, INCREMENTAL].toArray()) as List<EventType>
    }

    @Override
    final List<EventType> getUncomputedEvents(Aggregate aggregate, SnapshotType lastSnapshot, Date snapshotTime) {
        lastSnapshot.lastEventTimestamp ?
                invokeStaticMethod(eventClass, 'findAllByAggregateAndTimestampGreaterThanAndTimestampLessThanEquals',
                        [aggregate, lastSnapshot.lastEventTimestamp, snapshotTime, INCREMENTAL].toArray()) as List<EventType> :
                invokeStaticMethod(eventClass, 'findAllByAggregateAndTimestampLessThanEquals',
                        [aggregate, snapshotTime, INCREMENTAL].toArray()) as List<EventType>
    }

    @Override
    final List<EventType> findEventsForAggregates(List<Aggregate> aggregates) {
        invokeStaticMethod(eventClass, 'findAllByAggregateInList', [aggregates, INCREMENTAL].toArray()) as List<EventType>
    }

    @Override
    QueryExecutor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        new JoinExecutor<Aggregate, EventIdType, EventType, JoinedAggregateIdType, JoinedAggregateType, SnapshotIdType, SnapshotType, JoinE, DisjoinE>(
                classAggregate, classEventIdType, eventClass, classJoinedAggregateIdType, joinedAggregateClass, snapshotIdClass, snapshotClass, joinEventClass, disjoinEventClass)
    }

}
