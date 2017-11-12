package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import grails.gorm.rx.RxEntity;
import org.reactivestreams.Publisher;
import rx.Observable;

import java.util.Date;

import static com.github.rahulsom.grooves.grails.QueryUtil.*;
import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod;
import static rx.RxReactiveStreams.toPublisher;

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
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> &
                RxEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT, QueryT>
        > extends QuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
        SnapshotT, QueryT> {

    Class<EventT> getEventClass();

    @Override
    default Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        boolean missingOrEmptySnapshot =
                lastSnapshot == null || lastSnapshot.getLastEventPosition() == 0;

        final long position = missingOrEmptySnapshot ? 0 : lastSnapshot.getLastEventPosition();

        //noinspection unchecked
        return toPublisher((Observable<EventT>) invokeStaticMethod(
                getEventClass(),
                UNCOMPUTED_EVENTS_BY_VERSION,
                new Object[]{aggregate, position, version, INCREMENTAL_BY_POSITION}));
    }

    @Override
    default Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        final Date lastEventTimestamp = lastSnapshot.getLastEventTimestamp();
        final String method = lastEventTimestamp == null ?
                UNCOMPUTED_EVENTS_BEFORE_DATE :
                UNCOMPUTED_EVENTS_BY_DATE_RANGE;
        final Object[] params = lastEventTimestamp == null ?
                new Object[]{aggregate, snapshotTime, INCREMENTAL_BY_TIMESTAMP} :
                new Object[]{aggregate, lastEventTimestamp, snapshotTime, INCREMENTAL_BY_TIMESTAMP};

        //noinspection unchecked
        return toPublisher(
                (Observable<EventT>) invokeStaticMethod(getEventClass(), method, params));
    }

    /**
     * Hook to turn a proxy for an aggregate into an aggregate.
     *
     * @param aggregate The aggregate that may or may not be a proxy
     *
     * @return An observable of the aggregate
     */
    Observable<AggregateT> reattachAggregate(AggregateT aggregate);

}
