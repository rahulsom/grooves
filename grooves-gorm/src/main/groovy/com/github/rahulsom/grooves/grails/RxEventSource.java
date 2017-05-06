package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import grails.gorm.rx.RxEntity;
import rx.Observable;

import java.util.Date;
import java.util.List;

import static com.github.rahulsom.grooves.grails.QueryUtil.INCREMENTAL_BY_POSITION;
import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod;
import static rx.Observable.from;

/**
 * Supplies Events from an Rx Gorm Source.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface RxEventSource<
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & RxEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        > extends QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    Class<EventT> getEventClass();

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        final long position = lastSnapshot == null ? 0 :
                lastSnapshot.getLastEventPosition() == null ? 0 :
                        lastSnapshot.getLastEventPosition();
        //noinspection unchecked
        return (Observable<EventT>) invokeStaticMethod(
                getEventClass(),
                "findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals",
                new Object[]{aggregate, position, version, INCREMENTAL_BY_POSITION});
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        //noinspection unchecked
        return (Observable<EventT>) (lastSnapshot.getLastEventTimestamp() != null ?
                invokeStaticMethod(
                        getEventClass(),
                        "findAllByAggregateAndTimestampGreaterThanAndTimestampLessThanEquals",
                        new Object[]{aggregate, lastSnapshot.getLastEventTimestamp(),
                                snapshotTime, INCREMENTAL_BY_POSITION}) :
                invokeStaticMethod(
                        getEventClass(),
                        "findAllByAggregateAndTimestampLessThanEquals",
                        new Object[]{aggregate, snapshotTime, INCREMENTAL_BY_POSITION}));
    }

    Observable<AggregateT> reattachAggregate(AggregateT aggregate);

    @Override
    default Observable<EventT> findEventsForAggregates(List<AggregateT> aggregates) {
        return from(aggregates)
                .flatMap(this::reattachAggregate)
                .toList()
                .flatMap(reattachedAggregates -> {
                    getLog().info("Finding events for aggregates: {}",
                            reattachedAggregates);
                    //noinspection unchecked
                    return (Observable<EventT>) invokeStaticMethod(
                            getEventClass(),
                            "findAllByAggregateInList",
                            new Object[]{reattachedAggregates, INCREMENTAL_BY_POSITION});
                });
    }

}
