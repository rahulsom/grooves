package com.github.rahulsom.grooves.queries

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.snapshots.Join
import com.github.rahulsom.grooves.queries.internal.JoinExecutor
import com.github.rahulsom.grooves.queries.internal.QueryExecutor

trait JoinSupport<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        JoinedAggregateIdType,
        JoinedAggregateType extends AggregateType,
        SnapshotIdType,
        SnapshotType extends Join<Aggregate, SnapshotIdType, JoinedAggregateIdType, EventIdType, EventType>,
        JoinE extends JoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>,
        DisjoinE extends DisjoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>>
        implements
                VersionedJoinSupport<Aggregate, EventIdType, EventType, JoinedAggregateIdType, JoinedAggregateType, SnapshotIdType, SnapshotType, JoinE, DisjoinE>,
                TemporalJoinSupport<Aggregate, EventIdType, EventType, JoinedAggregateIdType, JoinedAggregateType, SnapshotIdType, SnapshotType, JoinE, DisjoinE> {

    @Override
    QueryExecutor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        new JoinExecutor<Aggregate, EventIdType, EventType, JoinedAggregateIdType, JoinedAggregateType, SnapshotIdType, SnapshotType, JoinE, DisjoinE>(
                Aggregate, EventIdType, EventType, JoinedAggregateIdType, JoinedAggregateType, SnapshotIdType, SnapshotType, JoinE, DisjoinE)
    }
}
