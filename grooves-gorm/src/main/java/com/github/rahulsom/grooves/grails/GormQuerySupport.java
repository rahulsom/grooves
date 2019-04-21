package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import org.grails.datastore.gorm.GormEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

import java.util.Date;

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
// tag::documented[]
public interface GormQuerySupport<
        AggregateIdT,
        AggregateT extends GormAggregate<AggregateIdT> & GormEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & GormEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT> &
                GormEntity<SnapshotT>
        > extends QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>, //<1>
        BlockingEventSource<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        BlockingSnapshotSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    Class<EventT> getEventClass(); // <2>

    Class<SnapshotT> getSnapshotClass(); // <3>

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(long maxPosition, @NotNull AggregateT aggregate) {
        return BlockingSnapshotSource.super
                .getSnapshot(maxPosition, aggregate); // <4>
    }

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(Date maxTimestamp, @NotNull AggregateT aggregate) {
        return BlockingSnapshotSource.super
                .getSnapshot(maxTimestamp, aggregate);
    }

    @NotNull
    @Override
    default Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, @Nullable SnapshotT lastSnapshot, long version) {
        return BlockingEventSource.super
                .getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @NotNull
    @Override
    default Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, @Nullable SnapshotT lastSnapshot,
            @NotNull Date snapshotTime) {
        return BlockingEventSource.super
                .getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

}
// end::documented[]
