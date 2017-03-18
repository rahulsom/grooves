package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

public interface JoinEvent<
        Aggregate extends AggregateType, EventIdType, EventType, JoinedAggregate extends AggregateType>
        extends BaseEvent<Aggregate, EventIdType, EventType> {

    JoinedAggregate getJoinAggregate();
    void setJoinAggregate(JoinedAggregate rollupAggregate);
}
