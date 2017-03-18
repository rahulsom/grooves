package com.github.rahulsom.grooves.grails

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import com.github.rahulsom.grooves.queries.QuerySupport
import org.grails.datastore.gorm.GormEntity

import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod

/**
 * Gorm Support for Query Util. <br/>
 * This is the preferred way of writing Grooves applications with Grails.
 *
 * @param <Aggregate> The Aggregate type
 * @param <EventType> The Event type
 * @param <SnapshotType> The snapshot type
 *
 * @author Rahul Somasunderam
 */
abstract class GormQuerySupport<
        Aggregate extends AggregateType & GormEntity<Aggregate>,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType> & GormEntity<EventType>,
        SnapshotIdType,
        SnapshotType extends Snapshot<Aggregate, SnapshotIdType, EventIdType, EventType> & GormEntity<SnapshotType>>
        implements
                QuerySupport<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {

    private final Class<EventType>    eventClass
    private final Class<SnapshotType> snapshotClass

    GormQuerySupport(Class<EventType> eventClass, Class<SnapshotType> snapshotClass) {
        this.eventClass = eventClass
        this.snapshotClass = snapshotClass
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

}
