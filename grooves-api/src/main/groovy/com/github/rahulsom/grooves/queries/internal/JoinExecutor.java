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
 *
 * @author Rahul Somasunderam
 */
public class JoinExecutor<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        JoinedAggregateIdT,
        JoinedAggregateT extends AggregateType<JoinedAggregateIdT>,
        SnapshotIdT,
        SnapshotT extends BaseJoin<AggregateIdT, AggregateT, SnapshotIdT, JoinedAggregateIdT,
                EventIdT, EventT>,
        JoinEventT extends JoinEvent<AggregateIdT, AggregateT, EventIdT, EventT,
                JoinedAggregateIdT, JoinedAggregateT>,
        DisjoinEventT extends DisjoinEvent<AggregateIdT, AggregateT, EventIdT, EventT,
                JoinedAggregateIdT, JoinedAggregateT>>
        extends
        QueryExecutor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    private final Class<JoinEventT> classJoinE;
    private final Class<DisjoinEventT> classDisjoinE;

    public JoinExecutor(Class<JoinEventT> classJoinE, Class<DisjoinEventT> classDisjoinE) {
        this.classJoinE = classJoinE;
        this.classDisjoinE = classDisjoinE;
    }

    @Override
    public Observable<SnapshotT> applyEvents(
            BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> query,
            SnapshotT initialSnapshot,
            Observable<EventT> events,
            List<Deprecates<AggregateIdT, AggregateT, EventIdT, EventT>> deprecatesList,
            List<AggregateT> aggregates, AggregateT aggregate) {


        // s -> snapshotObservable
        return events.reduce(Observable.just(initialSnapshot), (s, event) -> s.flatMap(snapshot -> {
            if (!query.shouldEventsBeApplied(snapshot)) {
                return Observable.just(snapshot);
            } else {
                log.debug("     -> Applying Event: {}", event);

                if (event instanceof Deprecates) {
                    return applyDeprecates((Deprecates<AggregateIdT, AggregateT, EventIdT, EventT>)
                                    event,
                            query, aggregates, deprecatesList, aggregate);
                } else if (event instanceof DeprecatedBy) {
                    return applyDeprecatedBy(
                            (DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT>) event,
                            initialSnapshot);
                } else if (classJoinE.isAssignableFrom(event.getClass())) {
                    JoinEventT joinEvent = (JoinEventT) event;
                    return joinEvent
                            .getJoinAggregateObservable()
                            .map(joinedAggregate -> {
                                initialSnapshot.getJoinedIds().add(joinedAggregate.getId());
                                return initialSnapshot;
                            });
                } else if (classDisjoinE.isAssignableFrom(event.getClass())) {
                    DisjoinEventT disjoinEvent = (DisjoinEventT) event;
                    return disjoinEvent
                            .getJoinAggregateObservable()
                            .map(joinedAggregate -> {
                                initialSnapshot.getJoinedIds().remove(joinedAggregate.getId());
                                return initialSnapshot;
                            });
                } else {
                    return Observable.just(initialSnapshot);
                }
            }
        })).flatMap(it -> it);

    }
}
