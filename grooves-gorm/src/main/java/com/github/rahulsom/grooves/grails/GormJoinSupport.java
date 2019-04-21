package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DisjoinEvent;
import com.github.rahulsom.grooves.api.events.JoinEvent;
import com.github.rahulsom.grooves.api.snapshots.Join;
import com.github.rahulsom.grooves.queries.JoinSupport;
import com.github.rahulsom.grooves.queries.internal.JoinExecutor;
import org.grails.datastore.gorm.GormEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

import java.util.Date;

/**
 * Gorm implementation of Join Support.
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
 * @deprecated Use {@link BlockingEventSource} and {@link BlockingSnapshotSource} instead
 */
@Deprecated
public interface GormJoinSupport<
        AggregateIdT,
        AggregateT extends GormAggregate<AggregateIdT> & GormEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & GormEntity<EventT>,
        JoinedAggregateIdT,
        JoinedAggregateT extends GormAggregate<JoinedAggregateIdT> & GormEntity<JoinedAggregateT>,
        SnapshotIdT,
        SnapshotT extends Join<AggregateT, SnapshotIdT, JoinedAggregateT, EventIdT, EventT> &
                GormEntity<SnapshotT>,
        JoinEventT extends JoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>
        > extends
        JoinSupport<AggregateT, EventIdT, EventT,
                JoinedAggregateT, SnapshotIdT, SnapshotT, JoinEventT, DisjoinEventT>,
        BlockingEventSource<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        BlockingSnapshotSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @NotNull
    @Override
    default JoinExecutor<AggregateT, EventIdT, EventT, JoinedAggregateT, SnapshotIdT,
            SnapshotT, JoinEventT, DisjoinEventT, ?> getExecutor() {
        //noinspection unchecked
        return new JoinExecutor<>(getJoinEventClass(), getDisjoinEventClass());
    }

    @NotNull
    @Override
    default Publisher<EventT> findEventsBefore(@NotNull EventT event) {
        return JoinSupport.super.findEventsBefore(event);
    }

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(long maxPosition, @NotNull AggregateT aggregate) {
        return BlockingSnapshotSource.super.getSnapshot(maxPosition, aggregate);
    }

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(
            @Nullable Date maxTimestamp, @NotNull AggregateT aggregate) {
        return BlockingSnapshotSource.super.getSnapshot(maxTimestamp, aggregate);
    }

    @NotNull
    @Override
    default Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return BlockingEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @NotNull
    @Override
    default Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, SnapshotT lastSnapshot, @NotNull Date snapshotTime) {
        return BlockingEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

}
