package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import org.grails.datastore.gorm.GormEntity;
import org.reactivestreams.Publisher;

import java.util.Date;
import java.util.List;

import static com.github.rahulsom.grooves.grails.QueryUtil.*;
import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod;
import static rx.Observable.defer;
import static rx.Observable.from;
import static rx.RxReactiveStreams.toPublisher;

/**
 * Supplies Events from a Blocking Gorm Source.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface BlockingEventSource<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT> & GormEntity<EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT, QueryT>
        > extends QuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
        QueryT> {

    @Override
    default Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        final long position =
                lastSnapshot == null || lastSnapshot.getLastEventPosition() == null ? 0 :
                        lastSnapshot.getLastEventPosition();
        //noinspection unchecked
        return toPublisher(defer(() -> from((List<EventT>) invokeStaticMethod(
                getEventClass(),
                UNCOMPUTED_EVENTS_BY_VERSION,
                new Object[]{aggregate, position, version, INCREMENTAL_BY_POSITION}
        ))));
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
        return toPublisher(defer(() ->
                from((List<EventT>) invokeStaticMethod(getEventClass(), method, params))));
    }

    /**
     * The class of events that this returns.
     *
     * @return The class of events
     */
    Class<EventT> getEventClass();

}
