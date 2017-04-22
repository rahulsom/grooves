package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.*;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin;
import rx.Observable;

import java.util.List;

/**
 * Executes a query as a Join.
 *
 * @param <AggregateT>         The Aggregate this join represents
 * @param <EventIdT>           The type for the {@link EventT}'s id field
 * @param <EventT>             The base type for events that apply to {@link AggregateT}
 * @param <SnapshotIdT>        The type for the join's id field
 * @param <JoinedAggregateIdT> The type for the id of the other aggregate that {@link AggregateT}
 *                             joins to
 * @param <JoinedAggregateT>   The type for the other aggregate that {@link AggregateT} joins to
 * @param <SnapshotT>          The type of Snapshot that is computed
 * @param <JoinEventT>         The type of the Join Event
 * @param <DisjoinEventT>      The type of the disjoin event
 * @author Rahul Somasunderam
 */
public class JoinExecutor<
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        JoinedAggregateIdT,
        JoinedAggregateT extends AggregateType<JoinedAggregateIdT>,
        SnapshotIdT,
        SnapshotT extends BaseJoin<AggregateT, SnapshotIdT, JoinedAggregateIdT,
                EventIdT, EventT>,
        JoinEventT extends JoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateT, EventIdT, EventT, JoinedAggregateT>>
        extends
        QueryExecutor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    private final Class<JoinEventT> classJoinE;
    private final Class<DisjoinEventT> classDisjoinE;

    public JoinExecutor(Class<JoinEventT> classJoinE, Class<DisjoinEventT> classDisjoinE) {
        this.classJoinE = classJoinE;
        this.classDisjoinE = classDisjoinE;
    }

    @Override
    public Observable<SnapshotT> applyEvents(
            BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> query,
            SnapshotT initialSnapshot,
            Observable<EventT> events,
            List<Deprecates<AggregateT, EventIdT, EventT>> deprecatesList,
            List<AggregateT> aggregates) {


        return events.reduce(initialSnapshot, (snapshot, event) -> {
            if (!query.shouldEventsBeApplied(snapshot)) {
                return snapshot;
            } else {
                log.debug("     -> Event: $event");

                if (event instanceof Deprecates) {
                    return applyDeprecates((Deprecates<AggregateT, EventIdT, EventT>) event,
                            query, aggregates, deprecatesList);
                } else if (event instanceof DeprecatedBy) {
                    return applyDeprecatedBy(
                            (DeprecatedBy<AggregateT, EventIdT, EventT>) event,
                            initialSnapshot);
                } else if (classJoinE.isAssignableFrom(event.getClass())) {
                    initialSnapshot.getJoinedIds().add(
                            ((JoinEventT) event).getJoinAggregate().getId());
                    return initialSnapshot;
                } else if (classDisjoinE.isAssignableFrom(event.getClass())) {
                    initialSnapshot.getJoinedIds().remove(
                            ((DisjoinEventT) event).getJoinAggregate().getId());
                    return initialSnapshot;
                } else {
                    return initialSnapshot;
                }
            }
        });


    }
}
