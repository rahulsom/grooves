package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import grails.gorm.rx.RxEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

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
        AggregateT extends GormAggregate<AggregateIdT> & RxEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & RxEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT> & RxEntity<SnapshotT>
        > extends QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        RxSnapshotSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        RxEventSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(long maxPosition, @NotNull AggregateT aggregate) {
        return RxSnapshotSource.super.getSnapshot(maxPosition, aggregate);
    }

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(
            @Nullable Date maxTimestamp, @NotNull AggregateT aggregate) {
        return RxSnapshotSource.super.getSnapshot(maxTimestamp, aggregate);
    }

    @NotNull
    @Override
    default Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, @Nullable SnapshotT lastSnapshot, long version) {
        return RxEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @NotNull
    @Override
    default Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, @Nullable SnapshotT lastSnapshot,
            @NotNull Date snapshotTime) {
        return RxEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

}
