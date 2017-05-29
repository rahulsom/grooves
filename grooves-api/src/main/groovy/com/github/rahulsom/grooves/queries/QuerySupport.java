package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;

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
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>
        >
        extends
        TemporalQuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        VersionedQuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    default Executor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
            SnapshotT> getExecutor() {
        return new QueryExecutor<>();
    }

}
