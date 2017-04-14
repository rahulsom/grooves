package com.github.rahulsom.grooves.queries

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.snapshots.TemporalJoin
import com.github.rahulsom.grooves.queries.internal.JoinExecutor
import com.github.rahulsom.grooves.queries.internal.QueryExecutor

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
trait TemporalJoinSupport<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        JoinedAggregateIdType,
        JoinedAggregateType extends AggregateType,
        SnapshotIdType,
        SnapshotType extends TemporalJoin<Aggregate, SnapshotIdType, JoinedAggregateIdType, EventIdType, EventType>,
        JoinE extends JoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>,
        DisjoinE extends DisjoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>>
        extends
                TemporalQuerySupport<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {

    @Override
    QueryExecutor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        new JoinExecutor<Aggregate,
                EventIdType,
                EventType,
                JoinedAggregateIdType,
                JoinedAggregateType,
                SnapshotIdType,
                SnapshotType,
                JoinE,
                DisjoinE>()
    }
}