package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DisjoinEvent;
import com.github.rahulsom.grooves.api.events.JoinEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalJoin;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import com.github.rahulsom.grooves.queries.internal.JoinExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Default interface that makes temporal joins easier to write.
 *
 * @param <AggregateT>         The Aggregate this join represents
 * @param <EventIdT>           The type for the {@link EventT}'s id field
 * @param <EventT>             The base type for events that apply to {@link AggregateT}
 * @param <JoinedAggregateT>   The type for the other aggregate that {@link AggregateT} joins to
 * @param <SnapshotIdT>        The type for the join's id field
 * @param <SnapshotT>          The type of Snapshot that is computed
 * @param <JoinEventT>         The type of the Join Event
 * @param <DisjoinEventT>      The type of the disjoin event
 * @param <QueryT>             A reference to the query type. Typically a self reference.
 *
 * @author Rahul Somasunderam
 */
public interface TemporalJoinSupport<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        JoinedAggregateT,
        SnapshotIdT,
        SnapshotT extends TemporalJoin<AggregateT, SnapshotIdT, JoinedAggregateT, EventIdT, EventT>,
        JoinEventT extends JoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        QueryT extends BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>
        > extends
        TemporalQuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT> {

    Class<JoinEventT> getJoinEventClass();

    Class<DisjoinEventT> getDisjoinEventClass();

    @NotNull
    @Override
    default JoinExecutor getExecutor() {
        return new JoinExecutor(getJoinEventClass(), getDisjoinEventClass());
    }
}
