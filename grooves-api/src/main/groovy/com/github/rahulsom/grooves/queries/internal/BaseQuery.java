package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    SnapshotType createEmptySnapshot();

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity
     *
     * @param startWithEvent
     * @param aggregate
     * @return
     */
    Optional<SnapshotType> getSnapshot(long startWithEvent, Aggregate aggregate);

    /**
     * Gets the last snapshot before given timestamp. Is responsible for discarding attached entity
     *
     * @param timestamp
     * @param aggregate
     * @return
     */
    Optional<SnapshotType> getSnapshot(Date timestamp, Aggregate aggregate);

    void detachSnapshot(SnapshotType retval);

    boolean shouldEventsBeApplied(SnapshotType snapshot);

    List<EventType> findEventsForAggregates(List<Aggregate> aggregates);

    void addToDeprecates(SnapshotType snapshot, Aggregate otherAggregate);

    EventType unwrapIfProxy(EventType event);

    EventApplyOutcome onException(Exception e, SnapshotType snapshot, EventType event);

    Executor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor();
}
