package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.*;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.reactivex.Flowable.fromPublisher;
import static io.reactivex.Flowable.just;

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
                JoinedAggregateIdT, JoinedAggregateT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>
        >
        extends
        QueryExecutor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT> {

    private final Class<JoinEventT> classJoinE;
    private final Class<DisjoinEventT> classDisjoinE;

    public JoinExecutor(Class<JoinEventT> classJoinE, Class<DisjoinEventT> classDisjoinE) {
        this.classJoinE = classJoinE;
        this.classDisjoinE = classDisjoinE;
    }

    @NotNull
    @Override
    public Flowable<SnapshotT> applyEvents(
            @NotNull BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                    SnapshotT> query,
            @NotNull SnapshotT initialSnapshot,
            @NotNull Flowable<EventT> events,
            @NotNull List<Deprecates<AggregateIdT, AggregateT, EventIdT, EventT>> deprecatesList,
            @NotNull AggregateT aggregate) {

        // s -> snapshotObservable
        return events.reduce(just(initialSnapshot), (s, event) -> s.flatMap(snapshot -> {
            if (!query.shouldEventsBeApplied(snapshot)) {
                return just(snapshot);
            } else {
                log.debug("     -> Applying Event: {}", event);

                if (event instanceof Deprecates) {
                    Deprecates<AggregateIdT, AggregateT, EventIdT, EventT> deprecatesEvent =
                            (Deprecates<AggregateIdT, AggregateT, EventIdT, EventT>) event;
                    return applyDeprecates(
                            deprecatesEvent, query, events, deprecatesList, aggregate);
                } else if (event instanceof DeprecatedBy) {
                    DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT> deprecatedByEvent =
                            (DeprecatedBy<AggregateIdT, AggregateT, EventIdT, EventT>) event;
                    return applyDeprecatedBy(deprecatedByEvent, initialSnapshot);
                } else if (classJoinE.isAssignableFrom(event.getClass())) {
                    JoinEventT joinEvent = (JoinEventT) event;
                    return fromPublisher(joinEvent.getJoinAggregateObservable())
                            .map(joinedAggregate -> {
                                initialSnapshot.getJoinedIds().add(joinedAggregate.getId());
                                return initialSnapshot;
                            });
                } else if (classDisjoinE.isAssignableFrom(event.getClass())) {
                    DisjoinEventT disjoinEvent = (DisjoinEventT) event;
                    return fromPublisher(disjoinEvent.getJoinAggregateObservable())
                            .map(joinedAggregate -> {
                                initialSnapshot.getJoinedIds().remove(joinedAggregate.getId());
                                return initialSnapshot;
                            });
                } else {
                    return just(initialSnapshot);
                }
            }
        })).toFlowable().flatMap(it -> it);

    }
}
