package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

/**
 * A query that produces a {@link BaseSnapshot}.
 *
 * @param <AggregateT>       The aggregate over which the query executes
 * @param <EventIdT>         The type of the EventT's id field
 * @param <EventT>           The type of the event
 * @param <ApplicableEventT> The type of the event that can be applied to the aggregate
 * @param <SnapshotIdT>      The type of the SnapshotT's id field
 * @param <SnapshotT>        The type of the snapshot
 * @author Rahul Somasunderam
 */
public interface SimpleQuery<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        ApplicableEventT extends EventT,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        > extends
        BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    /**
     * Applies an event to a snapshot.
     *
     * @param event    The event to apply
     * @param snapshot The snapshot to apply the event to
     * @return A Publisher that returns the outcome of applying the event
     */
    @NotNull Publisher<EventApplyOutcome> applyEvent(
            @NotNull ApplicableEventT event, @NotNull SnapshotT snapshot);

    /**
     * Returns the executor to use for applying events.
     *
     * @return The executor to use for applying events
     */
    @NotNull
    @Override
    default SimpleExecutor<AggregateT, EventIdT, EventT, ?, SnapshotIdT, SnapshotT,
            ?> getExecutor() {
        return new SimpleExecutor<>();
    }
}
