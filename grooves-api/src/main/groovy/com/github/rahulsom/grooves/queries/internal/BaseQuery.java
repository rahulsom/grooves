package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import rx.Observable;

import java.util.Date;
import java.util.List;

/**
 * Aggregate trait that simplifies computing snapshots from events
 *
 * @param <Aggregate>      The Aggregate type
 * @param <EventIdType>    The Event's id's type
 * @param <EventType>      The Event type
 * @param <SnapshotIdType> The snapshot's id's type
 * @param <SnapshotType>   The snapshot type
 * @author Rahul Somasunderam
 */
public interface BaseQuery<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        SnapshotIdType,
        SnapshotType extends BaseSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType>
        > {
    /**
     * When no snapshot is found in the database, the query has to create the zero of the snapshot.
     * This is the implementation of such a snapshot
     *
     * @return The empty snapshot
     */
    SnapshotType createEmptySnapshot();

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity
     *
     * @param maxPosition The position before which a snapshot is required
     * @param aggregate   The aggregate for which a snapshot is required
     * @return An observable that returns at most one SnapshotType
     */
    Observable<SnapshotType> getSnapshot(long maxPosition, Aggregate aggregate);

    /**
     * Gets the last snapshot before given timestamp. Is responsible for discarding attached entity
     *
     * @param maxTimestamp The maximum timestamp of the snapshot
     * @param aggregate    The aggregate for which a snapshot is required
     * @return An observable that returns at most one SnapshotType
     */
    Observable<SnapshotType> getSnapshot(Date maxTimestamp, Aggregate aggregate);

    /**
     * Detaches a snapshot from any state information from a persistence mechanism
     *
     * @param snapshot The snapshot to be detached
     */
    void detachSnapshot(SnapshotType snapshot);

    /**
     * Decides whether applying more events is permitted on a snapshot
     *
     * @param snapshot The snapshot
     * @return whether more events can be applied
     */
    boolean shouldEventsBeApplied(SnapshotType snapshot);

    /**
     * Finds all events for a given list of aggregates
     *
     * @param aggregates The list of aggregates
     * @return The list of events
     */
    Observable<EventType> findEventsForAggregates(List<Aggregate> aggregates);

    /**
     * Adds an aggregate to the list of aggregates that are deprecated by the aggregate of a snapshot
     *
     * @param snapshot            The snapshot that points to the winning aggregate
     * @param deprecatedAggregate The aggregate that is deprecated
     */
    void addToDeprecates(SnapshotType snapshot, Aggregate deprecatedAggregate);

    /**
     * If an event is a proxied type, unwrap it and return an event type
     *
     * @param event The event
     * @return An unproxied event
     */
    EventType unwrapIfProxy(EventType event);

    /**
     * Exception handler when applying events
     *
     * @param e        The exception
     * @param snapshot The snapshot
     * @param event    The event that resulted in an exception
     * @return The outcome of handling the exception
     */
    EventApplyOutcome onException(Exception e, SnapshotType snapshot, EventType event);

    /**
     * @return An executor that applies events
     */
    Executor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor();
}
