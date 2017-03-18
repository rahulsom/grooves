package com.github.rahulsom.grooves.queries.internal

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.*
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin
import groovy.transform.CompileStatic

@CompileStatic
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
    @Override
    SnapshotType applyEvents(
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> util,
            SnapshotType snapshot,
            List<EventType> events,
            List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList,
            List<Aggregate> aggregates) {

        if (events.empty || !util.shouldEventsBeApplied(snapshot)) {
            return snapshot
        }
        def event = events.head()
        def remainingEvents = events.tail()

        log.debug "    --> Event: $event"

        if (event instanceof Deprecates<Aggregate, EventIdType, EventType>) {
            applyDeprecates(event, util, aggregates, deprecatesList)
        } else if (event instanceof DeprecatedBy<Aggregate, EventIdType, EventType>) {
            applyDeprecatedBy(event, snapshot)
        } else if (event instanceof JoinE) {
            snapshot.joinedIds.add((event as JoinE).joinAggregate.id)
            applyEvents(util, snapshot as SnapshotType, remainingEvents, deprecatesList, aggregates)
        } else if (event instanceof DisjoinE) {
            snapshot.joinedIds.remove((event as DisjoinE).joinAggregate.id)
            applyEvents(util, snapshot as SnapshotType, remainingEvents, deprecatesList, aggregates)
        } else {
            applyEvents(util, snapshot as SnapshotType, remainingEvents, deprecatesList, aggregates)
        }
    }
}
