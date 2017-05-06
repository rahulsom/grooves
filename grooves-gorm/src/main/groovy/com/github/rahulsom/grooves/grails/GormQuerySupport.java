package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import org.grails.datastore.gorm.GormEntity;
import rx.Observable;

import java.util.Date;
import java.util.List;

/**
 * Gorm Support for Query Util.
 *
 * <p>This is the preferred way of writing Grooves applications with Grails.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 * @deprecated Use {@link BlockingEventSource} and {@link BlockingSnapshotSource} instead
 */
@Deprecated
public interface GormQuerySupport<
        AggregateT extends AggregateType & GormEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & GormEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT> &
                GormEntity<SnapshotT>
        > extends QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        BlockingEventSource<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        BlockingSnapshotSource<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @Override
    default Observable<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate) {
        return BlockingSnapshotSource.super.getSnapshot(maxPosition, aggregate);
    }

    @Override
    default Observable<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate) {
        return BlockingSnapshotSource.super.getSnapshot(maxTimestamp, aggregate);
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return BlockingEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        return BlockingEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

    @Override
    default Observable<EventT> findEventsForAggregates(List<AggregateT> aggregates) {
        return BlockingEventSource.super.findEventsForAggregates(aggregates);
    }

    Class<EventT> getEventClass();

    Class<SnapshotT> getSnapshotClass();

}
