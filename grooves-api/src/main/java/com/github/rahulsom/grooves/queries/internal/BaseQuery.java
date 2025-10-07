package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

/**
 * Aggregate trait that simplifies computing snapshots from events.
 *
 * @param <AggregateT>   The aggregate over which the query executes
 * @param <EventIdT>     The type of the EventT's id field
 * @param <EventT>       The type of the Event
 * @param <SnapshotIdT>  The type of the SnapshotT's id field
 * @param <SnapshotT>    The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface BaseQuery<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>> {

    /**
     * When no snapshot is found in the database, the query has to create the zero of the snapshot.
     * This is the implementation of such a snapshot.
     *
     * @return The empty snapshot
     */
    @NotNull
    SnapshotT createEmptySnapshot();

    /**
     * Decides whether applying more events is permitted on a snapshot.
     *
     * @param snapshot The snapshot
     *
     * @return whether more events can be applied
     */
    boolean shouldEventsBeApplied(@NotNull SnapshotT snapshot);

    /**
     * Finds all events older than a given event.
     *
     * @param event The event before which events are eligible
     *
     * @return The list of events
     */
    @NotNull
    Publisher<EventT> findEventsBefore(@NotNull EventT event);

    /**
     * Adds an aggregate to the list of aggregates that are deprecated by the aggregate of a
     * snapshot.
     *
     * @param snapshot            The snapshot that points to the winning aggregate
     * @param deprecatedAggregate The aggregate that is deprecated
     */
    void addToDeprecates(@NotNull SnapshotT snapshot, @NotNull AggregateT deprecatedAggregate);

    /**
     * Exception handler when applying events.
     *
     * @param e        The exception
     * @param snapshot The snapshot
     * @param event    The event that resulted in an exception
     *
     * @return The outcome of handling the exception
     */
    @NotNull
    Publisher<EventApplyOutcome> onException(@NotNull Exception e, @NotNull SnapshotT snapshot, @NotNull EventT event);

    /**
     * Provide an executor to execute the query.
     *
     * @return An executor that applies events.
     */
    @NotNull
    Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> getExecutor();
}
