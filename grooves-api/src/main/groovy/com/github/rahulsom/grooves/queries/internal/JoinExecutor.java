package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.*;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin;
import rx.Observable;

import java.util.List;

/**
 * @param <Aggregate>
 * @param <EventIdType>
 * @param <EventType>
 * @param <JoinedAggregateIdType>
 * @param <JoinedAggregateType>
 * @param <SnapshotIdType>
 * @param <SnapshotType>
 * @param <JoinE>
 * @param <DisjoinE>
 * @author Rahul Somasunderam
 */
public class JoinExecutor<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        JoinedAggregateIdType,
        JoinedAggregateType extends AggregateType<JoinedAggregateIdType>,
        SnapshotIdType,
        SnapshotType extends BaseJoin<Aggregate, SnapshotIdType, JoinedAggregateIdType, EventIdType, EventType>,
        JoinE extends JoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>,
        DisjoinE extends DisjoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>>
        extends
        QueryExecutor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {

    private final Class<JoinE> classJoinE;
    private final Class<DisjoinE> classDisjoinE;

    public JoinExecutor(Class<JoinE> classJoinE, Class<DisjoinE> classDisjoinE) {
        this.classJoinE = classJoinE;
        this.classDisjoinE = classDisjoinE;
    }

    @Override
    public Observable<SnapshotType> applyEvents(
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> query,
            SnapshotType initialSnapshot,
            Observable<EventType> events,
            List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList,
            List<Aggregate> aggregates) {


        return events.reduce(initialSnapshot, (snapshot, event) -> {
            if (!query.shouldEventsBeApplied(snapshot)) {
                return snapshot;
            } else {
                log.debug("     -> Event: $event");

                if (event instanceof Deprecates) {
                    return applyDeprecates((Deprecates<Aggregate, EventIdType, EventType>) event,
                            query, aggregates, deprecatesList);
                } else if (event instanceof DeprecatedBy) {
                    return applyDeprecatedBy((DeprecatedBy<Aggregate, EventIdType, EventType>) event, initialSnapshot);
                } else if (classJoinE.isAssignableFrom(event.getClass())) {
                    initialSnapshot.getJoinedIds().add(((JoinE) event).getJoinAggregate().getId());
                    return initialSnapshot;
                } else if (classDisjoinE.isAssignableFrom(event.getClass())) {
                    initialSnapshot.getJoinedIds().remove(((DisjoinE) event).getJoinAggregate().getId());
                    return initialSnapshot;
                } else {
                    return initialSnapshot;
                }
            }
        });


    }
}
