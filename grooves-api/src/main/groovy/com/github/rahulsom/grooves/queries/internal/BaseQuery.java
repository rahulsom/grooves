package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Date;
import java.util.List;

/**
 * Aggregate trait that simplifies computing snapshots from events.
 *
 * @param <AggregateT>  The Aggregate type
 * @param <EventIdT>    The Event's id's type
 * @param <EventT>      The Event type
 * @param <SnapshotIdT> The snapshot's id's type
 * @param <SnapshotT>   The snapshot type
 * @author Rahul Somasunderam
 */
public interface BaseQuery<
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
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
     * @return An observable that returns at most one SnapshotType
     */
    Observable<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate);

    /**
     * Gets the last snapshot before given timestamp. Is responsible for discarding attached entity.
     *
     * @param maxTimestamp The maximum timestamp of the snapshot
     * @param aggregate    The aggregate for which a snapshot is required
     * @return An observable that returns at most one SnapshotType
     */
    Observable<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate);

    /**
     * Detaches a snapshot from any state information from a persistence mechanism.
     *
     * @param snapshot The snapshot to be detached
     */
    void detachSnapshot(SnapshotT snapshot);

    /**
     * Decides whether applying more events is permitted on a snapshot.
     *
     * @param snapshot The snapshot
     * @return whether more events can be applied
     */
    boolean shouldEventsBeApplied(SnapshotT snapshot);

    /**
     * Finds all events for a given list of aggregates.
     *
     * @param aggregates The list of aggregates
     * @return The list of events
     */
    Observable<EventT> findEventsForAggregates(List<AggregateT> aggregates);

    /**
     * Adds an aggregate to the list of aggregates that are deprecated by the aggregate of a
     * snapshot.
     *
     * @param snapshot            The snapshot that points to the winning aggregate
     * @param deprecatedAggregate The aggregate that is deprecated
     */
    void addToDeprecates(SnapshotT snapshot, AggregateT deprecatedAggregate);

    /**
     * If an event is a proxied type, unwrap it and return an event type.
     *
     * @param event The event
     * @return An unproxied event
     */
    EventT unwrapIfProxy(EventT event);

    /**
     * Exception handler when applying events.
     *
     * @param e        The exception
     * @param snapshot The snapshot
     * @param event    The event that resulted in an exception
     * @return The outcome of handling the exception
     */
    EventApplyOutcome onException(Exception e, SnapshotT snapshot, EventT event);

    /**
     * @return An executor that applies events.
     */
    Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> getExecutor();
}
