package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseJoin;

import java.util.List;

public interface VersionedJoin<
        Aggregate extends AggregateType,
        JoinIdType,
        JoinedAggregateIdType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>>
        extends
        VersionedSnapshot<Aggregate, JoinIdType, EventIdType, EventType>,
        BaseJoin<Aggregate, JoinIdType, JoinedAggregateIdType, EventIdType, EventType> {

}
