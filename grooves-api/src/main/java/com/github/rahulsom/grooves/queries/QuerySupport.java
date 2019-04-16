package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

/**
 * Aggregate trait that simplifies computing temporal snapshots from events.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface QuerySupport<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        >
        extends
        TemporalQuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        VersionedQuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @NotNull
    @Override
    default QueryExecutor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, ?> getExecutor() {
        return new QueryExecutor<>();
    }

    @NotNull
    @Override
    default Publisher<EventT> findEventsBefore(@NotNull EventT event) {
        return VersionedQuerySupport.super.findEventsBefore(event);
    }
}
