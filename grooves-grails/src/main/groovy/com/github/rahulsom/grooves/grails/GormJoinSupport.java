package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DisjoinEvent;
import com.github.rahulsom.grooves.api.events.JoinEvent;
import com.github.rahulsom.grooves.api.snapshots.Join;
import com.github.rahulsom.grooves.queries.JoinSupport;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.JoinExecutor;
import org.grails.datastore.gorm.GormEntity;
import rx.Observable;

import java.util.Date;
import java.util.List;

/**
 * Gorm implementation of Join Support.
 *
 * @param <AggregateT>         The Aggregate this join represents
 * @param <EventIdT>           The type for the {@link EventT}'s id field
 * @param <EventT>             The base type for events that apply to {@link AggregateT}
 * @param <SnapshotIdT>        The type for the join's id field
 * @param <JoinedAggregateIdT> The type for the other id of aggregate that {@link AggregateT}
 *                             joins to
 * @param <JoinedAggregateT>   The type for the other aggregate that {@link AggregateT} joins to
 * @param <SnapshotT>          The type of Snapshot that is computed
 * @param <JoinEventT>         The type of the Join Event
 * @param <DisjoinEventT>      The type of the disjoin event
 * @author Rahul Somasunderam
 */
public interface GormJoinSupport<
        AggregateT extends AggregateType & GormEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & GormEntity<EventT>,
        JoinedAggregateIdT,
        JoinedAggregateT extends AggregateType & GormEntity<JoinedAggregateT>,
        SnapshotIdT,
        SnapshotT extends Join<AggregateT, SnapshotIdT, JoinedAggregateIdT,
                EventIdT, EventT> & GormEntity<SnapshotT>,
        JoinEventT extends JoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>
        > extends
        JoinSupport<AggregateT, EventIdT, EventT, JoinedAggregateIdT, JoinedAggregateT,
                SnapshotIdT, SnapshotT, JoinEventT, DisjoinEventT>,
        GormQuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @Override
    default Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> getExecutor() {
        //noinspection unchecked
        return new JoinExecutor<>(getJoinEventClass(), getDisjoinEventClass());
    }

    @Override
    default Observable<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate) {
        return GormQuerySupport.super.getSnapshot(maxPosition, aggregate);
    }

    @Override
    default Observable<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate) {
        return GormQuerySupport.super.getSnapshot(maxTimestamp, aggregate);
    }

    @Override
    default void detachSnapshot(SnapshotT snapshot) {

    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return GormQuerySupport.super.getUncomputedEvents(aggregate, lastSnapshot, version);
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        return GormQuerySupport.super.getUncomputedEvents(aggregate, lastSnapshot, snapshotTime);
    }

    @Override
    default Observable<EventT> findEventsForAggregates(List<AggregateT> aggregates) {
        return GormQuerySupport.super.findEventsForAggregates(aggregates);
    }
}
