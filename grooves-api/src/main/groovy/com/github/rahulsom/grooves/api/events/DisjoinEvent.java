package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

public interface DisjoinEvent<
        Aggregate extends AggregateType,
        EventIdType,
        EventType,
        DisjoinedAggregate extends AggregateType>
        extends BaseEvent<Aggregate, EventIdType, EventType> {

    DisjoinedAggregate getJoinAggregate();
    void setJoinAggregate(DisjoinedAggregate rollupAggregate);
}
