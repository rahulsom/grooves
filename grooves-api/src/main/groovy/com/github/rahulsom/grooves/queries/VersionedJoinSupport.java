package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DisjoinEvent;
import com.github.rahulsom.grooves.api.events.JoinEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedJoin;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.JoinExecutor;

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
public interface VersionedJoinSupport<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        JoinedAggregateIdType,
        JoinedAggregateType extends AggregateType,
        SnapshotIdType,
        SnapshotType extends VersionedJoin<Aggregate, SnapshotIdType, JoinedAggregateIdType, EventIdType, EventType>,
        JoinE extends JoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>,
        DisjoinE extends DisjoinEvent<Aggregate, EventIdType, EventType, JoinedAggregateType>
        > extends
        VersionedQuerySupport<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {
    Class<JoinE> getJoinEventClass();

    Class<DisjoinE> getDisjoinEventClass();

    @Override
    default Executor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        return new JoinExecutor<>(getJoinEventClass(), getDisjoinEventClass());
    }

}
