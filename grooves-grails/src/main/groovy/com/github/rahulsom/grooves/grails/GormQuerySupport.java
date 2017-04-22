package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.grails.datastore.gorm.GormEntity;
import rx.Observable;

import java.util.Date;
import java.util.List;

import static com.github.rahulsom.grooves.grails.QueryUtil.*;
import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod;

/**
 * Gorm Support for Query Util.
 *
 * <p>This is the preferred way of writing Grooves applications with Grails.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 * @author Rahul Somasunderam
 */
public interface GormQuerySupport<
        AggregateT extends AggregateType & GormEntity<AggregateT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT> & GormEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT> &
                GormEntity<SnapshotT>
        > extends QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @Override
    default Observable<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate) {
        return Observable.defer(() -> {
            //noinspection unchecked
            List<SnapshotT> snapshots = (List<SnapshotT>) (maxPosition == Long.MAX_VALUE ?
                    invokeStaticMethod(
                            getSnapshotClass(),
                            "findAllByAggregateId",
                            new Object[]{aggregate.getId(), LATEST_BY_POSITION}) :
                    invokeStaticMethod(
                            getSnapshotClass(),
                            "findAllByAggregateIdAndLastEventPositionLessThan",
                            new Object[]{aggregate.getId(), maxPosition, LATEST_BY_POSITION}));
            return DefaultGroovyMethods.asBoolean(snapshots) ?
                    Observable.just(snapshots.get(0)) :
                    Observable.empty();
        });
    }

    @Override
    default Observable<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate) {
        return Observable.defer(() -> {
            //noinspection unchecked
            List<SnapshotT> snapshots = (List<SnapshotT>) (maxTimestamp == null ?
                    invokeStaticMethod(
                            getSnapshotClass(),
                            "findAllByAggregateId",
                            new Object[]{aggregate.getId(), LATEST_BY_TIMESTAMP}) :
                    invokeStaticMethod(
                            getSnapshotClass(),
                            "findAllByAggregateIdAndLastEventTimestampLessThan",
                            new Object[]{aggregate.getId(), maxTimestamp, LATEST_BY_TIMESTAMP}));
            return DefaultGroovyMethods.asBoolean(snapshots) ?
                    Observable.just(snapshots.get(0)) :
                    Observable.empty();
        });
    }

    @Override
    default void detachSnapshot(SnapshotT snapshot) {
        if (snapshot.isAttached()) {
            snapshot.discard();
            snapshot.setId(null);
        }

    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return Observable.defer(() -> {
            final long position = lastSnapshot == null ? 0 :
                    lastSnapshot.getLastEventPosition() == null ? 0 :
                            lastSnapshot.getLastEventPosition();
            //noinspection unchecked
            return Observable.from((List<EventT>) invokeStaticMethod(
                    getEventClass(),
                    "findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals",
                    new Object[]{aggregate, position, version, INCREMENTAL_BY_POSITION}
                    )
            );
        });
    }

    @Override
    default Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        return Observable.defer(() -> {
            final Date lastEventTimestamp = lastSnapshot.getLastEventTimestamp();
            //noinspection unchecked
            return Observable.from((List<EventT>) (lastEventTimestamp == null ?
                    invokeStaticMethod(
                            getEventClass(),
                            "findAllByAggregateAndTimestampLessThanEquals",
                            new Object[]{aggregate, snapshotTime, INCREMENTAL_BY_TIMESTAMP}) :
                    invokeStaticMethod(
                            getEventClass(),
                            "findAllByAggregateAndTimestampGreaterThanAndTimestampLessThanEquals",
                            new Object[]{aggregate, lastEventTimestamp,
                                    snapshotTime, INCREMENTAL_BY_TIMESTAMP})));
        });
    }

    @Override
    default Observable<EventT> findEventsForAggregates(List<AggregateT> aggregates) {
        //noinspection unchecked
        return Observable.defer(() -> Observable.from(
                (List<EventT>) invokeStaticMethod(
                        getEventClass(),
                        "findAllByAggregateInList",
                        new Object[]{aggregates, INCREMENTAL_BY_POSITION}
                )
        ));
    }

    Class<EventT> getEventClass();

    Class<SnapshotT> getSnapshotClass();

}
