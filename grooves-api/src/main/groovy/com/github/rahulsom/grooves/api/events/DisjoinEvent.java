package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;
import rx.Observable;

/**
 * Breaks a join from {@link AggregateT} to {@link DisjoinedAggregateT} that had existed earlier.
 *
 * @param <AggregateT>          The Aggregate that had its link severed
 * @param <EventIdT>            The Type for Event's {@link #getId} field
 * @param <EventT>              The parent event type
 * @param <DisjoinedAggregateT> The target aggregate
 *
 * @author Rahul Somasunderam
 */
public interface DisjoinEvent<
        AggregateT extends AggregateType,
        EventIdT,
        EventT,
        DisjoinedAggregateT extends AggregateType>
        extends BaseEvent<AggregateT, EventIdT, EventT> {

    Observable<DisjoinedAggregateT> getJoinAggregateObservable();

}
