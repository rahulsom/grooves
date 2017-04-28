package com.github.rahulsom.grooves.api.snapshots.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import rx.Observable;

import java.util.Set;

/**
 * Marks a class as a snapshot. This makes no assumption about the type of snapshot.
 *
 * @param <AggregateT>  The Aggregate this snapshot works over
 * @param <SnapshotIdT> The type for the snapshot's {@link #getId()} field
 * @param <EventIdT>    The type for the {@link EventT}'s id field
 * @param <EventT>      The base type for events that apply to {@link AggregateT}
 *                
 * @author Rahul Somasunderam
 */
public interface BaseSnapshot<
        AggregateT extends AggregateType,
        SnapshotIdT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>> {
    SnapshotIdT getId();

    void setId(SnapshotIdT id);

    AggregateT getAggregate();

    void setAggregate(AggregateT aggregate);

    Observable<AggregateT> getDeprecatedByObservable();

    void setDeprecatedBy(AggregateT aggregate);

    Set<AggregateT> getDeprecates();

    void setLastEvent(EventT event);
}
