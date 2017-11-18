package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import org.grails.datastore.gorm.GormEntity;
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
        AggregateT extends AggregateType<AggregateIdT> & GormEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> &
                GormEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT> &
                GormEntity<SnapshotT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT>
        > extends QuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
        SnapshotT, QueryT>, //<1>
        BlockingEventSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT>,
        BlockingSnapshotSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT, QueryT> {

    Class<EventT> getEventClass(); // <2>

    Class<SnapshotT> getSnapshotClass(); // <3>

    @Override
    default Publisher<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate) {
        return BlockingSnapshotSource.super
                .getSnapshot(maxPosition, aggregate); // <4>
    }

    @Override
    default Publisher<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate) {
        return BlockingSnapshotSource.super
                .getSnapshot(maxTimestamp, aggregate);
    }

    @Override
    default Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return BlockingEventSource.super
                .getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @Override
    default Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        return BlockingEventSource.super
                .getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

}
// end::documented[]
