package com.github.rahulsom.grooves.queries.internal

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.internal.BaseEvent
import com.github.rahulsom.grooves.api.BaseSnapshot
import com.github.rahulsom.grooves.api.EventApplyOutcome

/**
 * Created by rahul on 3/13/17.
 */
interface BaseQuery<A extends AggregateType, E extends BaseEvent<A, E>, S extends BaseSnapshot<A, ?>> {
    S createEmptySnapshot()

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity
     *
     * @param startWithEvent
     * @param aggregate
     * @return
     */
    Optional<S> getSnapshot(long startWithEvent, A aggregate)

    /**
     * Gets the last snapshot before given timestamp. Is responsible for discarding attached entity
     *
     * @param timestamp
     * @param aggregate
     * @return
     */
    Optional<S> getSnapshot(Date timestamp, A aggregate)

    void detachSnapshot(S retval)

    boolean shouldEventsBeApplied(S snapshot)

    List<E> findEventsForAggregates(List<A> aggregates)

    void addToDeprecates(S snapshot, A otherAggregate)

    E unwrapIfProxy(E event)

    EventApplyOutcome onException(Exception e, S snapshot, E event)

}
