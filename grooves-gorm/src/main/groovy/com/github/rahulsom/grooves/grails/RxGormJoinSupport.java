package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DisjoinEvent;
import com.github.rahulsom.grooves.api.events.JoinEvent;
import com.github.rahulsom.grooves.api.snapshots.Join;
import com.github.rahulsom.grooves.queries.JoinSupport;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.JoinExecutor;
import grails.gorm.rx.RxEntity;
import rx.Observable;

import java.util.Date;
import java.util.List;

/**
 * RxGorm implementation of Join Support.
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
 * @deprecated Use {@link RxEventSource} and {@link RxSnapshotSource} instead
 */
@Deprecated
public interface RxGormJoinSupport<
        AggregateT extends AggregateType & RxEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & RxEntity<EventT>,
        JoinedAggregateIdT,
        JoinedAggregateT extends AggregateType & RxEntity<JoinedAggregateT>,
        SnapshotIdT,
        SnapshotT extends Join<AggregateT, SnapshotIdT, JoinedAggregateIdT,
                EventIdT, EventT> & RxEntity<SnapshotT>,
        JoinEventT extends JoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>
        > extends
        JoinSupport<AggregateT, EventIdT, EventT, JoinedAggregateIdT, JoinedAggregateT,
                SnapshotIdT, SnapshotT, JoinEventT, DisjoinEventT>,
        RxEventSource<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        RxSnapshotSource<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @Override
    default Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> getExecutor() {
        //noinspection unchecked
        return new JoinExecutor<>(getJoinEventClass(), getDisjoinEventClass());
    }

    @Override
    default Observable<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate) {
        return RxSnapshotSource.super.getSnapshot(maxPosition, aggregate);
    }

    @Override
    default Observable<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate) {
        return RxSnapshotSource.super.getSnapshot(maxTimestamp, aggregate);
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return RxEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        return RxEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

    @Override
    default Observable<EventT> findEventsForAggregates(List<AggregateT> aggregates) {
        return RxEventSource.super.findEventsForAggregates(aggregates);
    }
}
