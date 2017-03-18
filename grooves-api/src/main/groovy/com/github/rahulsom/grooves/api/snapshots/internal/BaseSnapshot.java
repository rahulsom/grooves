package com.github.rahulsom.grooves.api.snapshots.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;

import java.util.Set;

/**
 * Marks a class as a snapshot. This makes no assumption about the type of snapshot.
 *
 * @param <Aggregate> The Aggregate this snapshot works over
 * @author Rahul Somasunderam
 */
public interface BaseSnapshot<
        Aggregate extends AggregateType,
        SnapshotIdType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>> {
    SnapshotIdType getId();
    void setId(SnapshotIdType id);

    Aggregate getAggregate();
    void setAggregate(Aggregate aggregate);

    Aggregate getDeprecatedBy();
    void setDeprecatedBy(Aggregate aggregate);

    Set<Aggregate> getDeprecates();

    void setLastEvent(EventType event);
}
