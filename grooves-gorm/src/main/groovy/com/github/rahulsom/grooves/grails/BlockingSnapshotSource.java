package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.grails.datastore.gorm.GormEntity;
import rx.Observable;

import java.util.Date;
import java.util.List;

import static com.github.rahulsom.grooves.grails.QueryUtil.LATEST_BY_POSITION;
import static com.github.rahulsom.grooves.grails.QueryUtil.LATEST_BY_TIMESTAMP;
import static com.github.rahulsom.grooves.grails.QueryUtil.SNAPSHOTS_BY_AGGREGATE;
import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod;
import static rx.Observable.defer;
import static rx.Observable.empty;
import static rx.Observable.just;

/**
 * Supplies Snapshots from a Blocking Gorm Source.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface BlockingSnapshotSource<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT> &
                GormEntity<SnapshotT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                SnapshotT, QueryT>
        > extends QuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
        QueryT> {

    SnapshotT detachSnapshot(SnapshotT snapshot);

    @Override
    default Observable<SnapshotT> getSnapshot(long maxPosition, AggregateT aggregate) {
        return defer(() -> {
            //noinspection unchecked
            List<SnapshotT> snapshots = (List<SnapshotT>) (maxPosition == Long.MAX_VALUE ?
                    invokeStaticMethod(
                            getSnapshotClass(),
                            SNAPSHOTS_BY_AGGREGATE,
                            new Object[]{aggregate.getId(), LATEST_BY_POSITION}) :
                    invokeStaticMethod(
                            getSnapshotClass(),
                            QueryUtil.SNAPSHOTS_BY_POSITION,
                            new Object[]{aggregate.getId(), maxPosition, LATEST_BY_POSITION}));

            return DefaultGroovyMethods.asBoolean(snapshots) ?
                    just(detachSnapshot(snapshots.get(0))) :
                    empty();
        });
    }

    @Override
    default Observable<SnapshotT> getSnapshot(Date maxTimestamp, AggregateT aggregate) {
        return defer(() -> {
            //noinspection unchecked
            List<SnapshotT> snapshots = (List<SnapshotT>) (maxTimestamp == null ?
                    invokeStaticMethod(
                            getSnapshotClass(),
                            SNAPSHOTS_BY_AGGREGATE,
                            new Object[]{aggregate.getId(), LATEST_BY_TIMESTAMP}) :
                    invokeStaticMethod(
                            getSnapshotClass(),
                            QueryUtil.SNAPSHOTS_BY_TIMETTAMP,
                            new Object[]{aggregate.getId(), maxTimestamp, LATEST_BY_TIMESTAMP}));
            return DefaultGroovyMethods.asBoolean(snapshots) ?
                    just(detachSnapshot(snapshots.get(0))) :
                    empty();
        });
    }

    Class<SnapshotT> getSnapshotClass();

}
