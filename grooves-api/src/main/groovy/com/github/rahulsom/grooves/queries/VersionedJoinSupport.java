package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DisjoinEvent;
import com.github.rahulsom.grooves.api.events.JoinEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedJoin;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.JoinExecutor;

/**
 * Default interface that makes versioned joins easier to write.
 *
 * @param <AggregateT>         The Aggregate this join represents
 * @param <EventIdT>           The type for the {@link EventT}'s id field
 * @param <EventT>             The base type for events that apply to {@link AggregateT}
 * @param <SnapshotIdT>        The type for the join's id field
 * @param <JoinedAggregateIdT> The type for the other id of aggregate that {@link AggregateT} joins
 *                             to
 * @param <JoinedAggregateT>   The type for the other aggregate that {@link AggregateT} joins to
 * @param <SnapshotT>          The type of Snapshot that is computed
 * @param <JoinEventT>         The type of the Join Event
 * @param <DisjoinEventT>      The type of the disjoin event
 *
 * @author Rahul Somasunderam
 */
public interface VersionedJoinSupport<
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        JoinedAggregateIdT,
        JoinedAggregateT extends AggregateType,
        SnapshotIdT,
        SnapshotT extends VersionedJoin<AggregateT, SnapshotIdT, JoinedAggregateIdT,
                EventIdT, EventT>,
        JoinEventT extends JoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>
        > extends
        VersionedQuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {
    Class<JoinEventT> getJoinEventClass();

    Class<DisjoinEventT> getDisjoinEventClass();

    @Override
    default Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT
            > getExecutor() {
        return new JoinExecutor<>(getJoinEventClass(), getDisjoinEventClass());
    }

}
