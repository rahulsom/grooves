package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DisjoinEvent;
import com.github.rahulsom.grooves.api.events.JoinEvent;
import com.github.rahulsom.grooves.api.snapshots.Join;
import com.github.rahulsom.grooves.queries.JoinSupport;
import com.github.rahulsom.grooves.queries.internal.JoinExecutor;
import grails.gorm.rx.RxEntity;
import org.reactivestreams.Publisher;

import java.util.Date;

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
        AggregateIdT,
        AggregateT extends GormAggregate<AggregateIdT> & RxEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & RxEntity<EventT>,
        JoinedAggregateIdT,
        JoinedAggregateT extends GormAggregate<JoinedAggregateIdT> & RxEntity<JoinedAggregateT>,
        SnapshotIdT,
        SnapshotT extends Join<AggregateT, SnapshotIdT, JoinedAggregateT, EventIdT, EventT> &
                RxEntity<SnapshotT>,
        JoinEventT extends JoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>
        > extends
        JoinSupport<AggregateT, EventIdT, EventT,
                JoinedAggregateT, SnapshotIdT, SnapshotT, JoinEventT, DisjoinEventT>,
        RxEventSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        RxSnapshotSource<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @Override
    default JoinExecutor getExecutor() {
        //noinspection unchecked
        return new JoinExecutor<>(getJoinEventClass(), getDisjoinEventClass());
    }

    @Override
    default Publisher<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate) {
        return RxSnapshotSource.super.getSnapshot(maxPosition, aggregate);
    }

    @Override
    default Publisher<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate) {
        return RxSnapshotSource.super.getSnapshot(maxTimestamp, aggregate);
    }

    @Override
    default Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return RxEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @Override
    default Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        return RxEventSource.super.getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

    @Override
    default Publisher<EventT> findEventsBefore(EventT event) {
        return JoinSupport.super.findEventsBefore(event);
    }

}
