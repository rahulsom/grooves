package com.github.rahulsom.grooves.api.snapshots.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;

import java.util.List;

/**
 * Created by rahul on 3/17/17.
 */
public interface BaseJoin<Aggregate extends AggregateType, JoinIdType, JoinedAggregateIdType, EventIdType, EventType extends BaseEvent<Aggregate, EventIdType, EventType>>
        extends BaseSnapshot<Aggregate, JoinIdType, EventIdType, EventType> {

    List<JoinedAggregateIdType> getJoinedIds();
    void setJoinedIds(List<JoinedAggregateIdType> ids);

}
