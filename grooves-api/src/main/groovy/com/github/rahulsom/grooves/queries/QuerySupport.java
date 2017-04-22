package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;

/**
 * Aggregate trait that simplifies computing temporal snapshots from events
 *
 * @param <Aggregate>      The aggregate over which the query executes
 * @param <EventIdType>    The type of the Event's id field
 * @param <EventType>      The type of the Event
 * @param <SnapshotIdType> The type of the Snapshot's id field
 * @param <SnapshotType>   The type of the Snapshot
 * @author Rahul Somasunderam
 */
public interface QuerySupport<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        SnapshotIdType,
        SnapshotType extends Snapshot<Aggregate, SnapshotIdType, EventIdType, EventType>
        >
        extends
        TemporalQuerySupport<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType>,
        VersionedQuerySupport<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {

    default Executor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        return new QueryExecutor<>();
    }

}
