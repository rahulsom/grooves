package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DisjoinEvent;
import com.github.rahulsom.grooves.api.events.JoinEvent;
import com.github.rahulsom.grooves.api.snapshots.Join;
import com.github.rahulsom.grooves.queries.internal.JoinExecutor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

/**
 * Default interface that makes joins easier to write.
 *
 * @param <AggregateT>         The Aggregate this join represents
 * @param <EventIdT>           The type for the EventT's id field
 * @param <EventT>             The base type for events that apply to AggregateT
 * @param <SnapshotIdT>        The type for the join's id field
 * @param <JoinedAggregateT>   The type for the other aggregate that AggregateT joins to
 * @param <SnapshotT>          The type of Snapshot that is computed
 * @param <JoinEventT>         The type of the Join Event
 * @param <DisjoinEventT>      The type of the disjoin event
 *
 * @author Rahul Somasunderam
 */
public interface JoinSupport<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        JoinedAggregateT,
        SnapshotIdT,
        SnapshotT extends Join<AggregateT, SnapshotIdT, JoinedAggregateT, EventIdT, EventT>,
        JoinEventT extends JoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>
        > extends
        VersionedJoinSupport<AggregateT, EventIdT, EventT,
                JoinedAggregateT, SnapshotIdT, SnapshotT, JoinEventT, DisjoinEventT>,
        TemporalJoinSupport<AggregateT, EventIdT, EventT,
                JoinedAggregateT, SnapshotIdT, SnapshotT, JoinEventT, DisjoinEventT> {

    @NotNull
    @Override
    default JoinExecutor<AggregateT, EventIdT, EventT, JoinedAggregateT, SnapshotIdT,
            SnapshotT, JoinEventT, DisjoinEventT, ?> getExecutor() {
        return new JoinExecutor<>(getJoinEventClass(), getDisjoinEventClass());
    }

    @NotNull
    @Override
    default Publisher<EventT> findEventsBefore(@NotNull EventT event) {
        return VersionedJoinSupport.super.findEventsBefore(event);
    }
}
