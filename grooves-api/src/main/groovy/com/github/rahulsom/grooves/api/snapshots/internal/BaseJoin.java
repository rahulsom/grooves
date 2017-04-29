package com.github.rahulsom.grooves.api.snapshots.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;

import java.util.List;

/**
 * A special kind of {@link BaseSnapshot} that stores information about joined entities.
 *
 * @param <AggregateT>         The Aggregate this join represents
 * @param <JoinIdT>            The type for the join's {@link #getId()} field
 * @param <JoinedAggregateIdT> The type for the other aggregate that {@link AggregateT} joins to
 * @param <EventIdT>           The type for the {@link EventT}'s id field
 * @param <EventT>             The base type for events that apply to {@link AggregateT}
 *
 * @author Rahul Somasunderam
 */
public interface BaseJoin<
        AggregateT extends AggregateType,
        JoinIdT,
        JoinedAggregateIdT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>
        >
        extends BaseSnapshot<AggregateT, JoinIdT, EventIdT, EventT> {

    List<JoinedAggregateIdT> getJoinedIds();

    void setJoinedIds(List<JoinedAggregateIdT> ids);

}
