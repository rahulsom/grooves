package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import grails.gorm.rx.RxEntity;
import rx.Observable;

import java.util.Date;

/**
 * Rx Gorm Support for Query Util.
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
 * @deprecated Use {@link RxEventSource} and {@link RxSnapshotSource} instead
 */
@Deprecated
public interface RxGormQuerySupport<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT> & RxEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>
                & RxEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>
                & RxEntity<SnapshotT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT, QueryT>
        > extends QuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
        SnapshotT, QueryT>,
        RxSnapshotSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT, QueryT>,
        RxEventSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT, QueryT> {

    @Override
    default Observable<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate) {
        return RxSnapshotSource.super.getSnapshot(maxPosition, aggregate);
    }

    @Override
    default Observable<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate) {
        return RxSnapshotSource.super.getSnapshot(maxTimestamp, aggregate);
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return RxEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        return RxEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

}
