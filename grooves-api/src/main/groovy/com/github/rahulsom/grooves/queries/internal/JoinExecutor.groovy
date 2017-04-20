package com.github.rahulsom.grooves.queries.internal

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.*
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import rx.Observable

/**
 *
 * @param <Aggregate>
 * @param <EventIdType>
 * @param <EventType>
 * @param <JoinedAggregateIdType>
 * @param <JoinedAggregateType>
 * @param <SnapshotIdType>
 * @param <SnapshotType>
 * @param <JoinE>
 * @param <DisjoinE>
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
@TupleConstructor
class JoinExecutor<
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

    Class<JoinE> classJoinE
    Class<DisjoinE> classDisjoinE

    @Override
    Observable<SnapshotType> applyEvents(
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> query,
            SnapshotType initialSnapshot,
            Observable<EventType> events,
            List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList,
            List<Aggregate> aggregates) {


        events.reduce(initialSnapshot) { SnapshotType snapshot, EventType event ->
            if (!query.shouldEventsBeApplied(snapshot)) {
                return snapshot
            } else {
                log.debug "    --> Event: $event"

                if (event instanceof Deprecates<Aggregate, EventIdType, EventType>) {
                    applyDeprecates(event, query, aggregates, deprecatesList)
                } else if (event instanceof DeprecatedBy<Aggregate, EventIdType, EventType>) {
                    applyDeprecatedBy(event, initialSnapshot)
                } else if (classJoinE.isAssignableFrom(event.class)) {
                    initialSnapshot.joinedIds.add((event as JoinE).joinAggregate.id)
                    initialSnapshot
                } else if (classDisjoinE.isAssignableFrom(event.class)) {
                    initialSnapshot.joinedIds.remove((event as DisjoinE).joinAggregate.id)
                    initialSnapshot
                } else {
                    initialSnapshot
                }
            }
        }


    }
}
