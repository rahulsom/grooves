package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;
import rx.Observable;

/**
 * Aggregate deprecation event.
 *
 * <p>The aggregate on which this event is applied is considered the winner of the merge.
 * The converse is {@link DeprecatedBy} which needs to be applied to the other aggregate which
 * loses the merge.
 *
 * @param <AggregateT> Aggregate this event applies to
 * @param <EventIdT>   The Type for Event's {@link #getId} field
 * @param <EventT>     The parent event type
 *
 * @author Rahul Somasunderam
 */
public interface Deprecates<AggregateT extends AggregateType, EventIdT, EventT>
        extends BaseEvent<AggregateT, EventIdT, EventT> {
    Observable<DeprecatedBy<AggregateT, EventIdT, EventT>> getConverseObservable();

    Observable<AggregateT> getDeprecatedObservable();
}
