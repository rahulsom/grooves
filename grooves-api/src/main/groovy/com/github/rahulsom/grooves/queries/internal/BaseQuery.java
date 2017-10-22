package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Aggregate trait that simplifies computing snapshots from events.
 *
 * @param <AggregateIdT> The type of {@link AggregateT}'s id
 * @param <AggregateT>   The aggregate over which the query executes
 * @param <EventIdT>     The type of the {@link EventT}'s id field
 * @param <EventT>       The type of the Event
 * @param <SnapshotIdT>  The type of the {@link SnapshotT}'s id field
 * @param <SnapshotT>    The type of the Snapshot
 * @param <QueryT>       A reference to the query type. Typically a self reference.
 *
 * @author Rahul Somasunderam
 */
public interface BaseQuery<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT, QueryT>
        > {

    default Logger getLog() {
        return LoggerFactory.getLogger(getClass());
    }

    /**
     * When no snapshot is found in the database, the query has to create the zero of the snapshot.
     * This is the implementation of such a snapshot.
     *
     * @return The empty snapshot
     */
    SnapshotT createEmptySnapshot();

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity.
     *
     * @param maxPosition The position before which a snapshot is required
     * @param aggregate   The aggregate for which a snapshot is required
     *
     * @return A Publisher that returns at most one Snapshot
     */
    Publisher<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate);

    /**
     * Gets the last snapshot before given timestamp. Is responsible for discarding attached entity.
     *
     * @param maxTimestamp The maximum timestamp of the snapshot
     * @param aggregate    The aggregate for which a snapshot is required
     *
     * @return A Publisher that returns at most one Snapshot
     */
    Publisher<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate);

    /**
     * Decides whether applying more events is permitted on a snapshot.
     *
     * @param snapshot The snapshot
     *
     * @return whether more events can be applied
     */
    boolean shouldEventsBeApplied(SnapshotT snapshot);

    /**
     * Finds all events older than a given event.
     *
     * @param event The event before which events are eligible
     *
     * @return The list of events
     */
    Publisher<EventT> findEventsBefore(EventT event);

    /**
     * Adds an aggregate to the list of aggregates that are deprecated by the aggregate of a
     * snapshot.
     *
     * @param snapshot            The snapshot that points to the winning aggregate
     * @param deprecatedAggregate The aggregate that is deprecated
     */
    void addToDeprecates(SnapshotT snapshot, AggregateT deprecatedAggregate);

    /**
     * Exception handler when applying events.
     *
     * @param e        The exception
     * @param snapshot The snapshot
     * @param event    The event that resulted in an exception
     *
     * @return The outcome of handling the exception
     */
    Publisher<EventApplyOutcome> onException(Exception e, SnapshotT snapshot, EventT event);

    /**
     * @return An executor that applies events.
     */
    Executor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
            QueryT> getExecutor();
}
