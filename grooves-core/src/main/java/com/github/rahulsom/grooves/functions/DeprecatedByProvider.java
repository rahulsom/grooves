package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.DeprecatedByResult;
import com.github.rahulsom.grooves.logging.Trace;

/**
 * Identifies the Aggregate that is deprecated by the given event, and the event id that's the
 * converse of the given event.
 */
@FunctionalInterface
public interface DeprecatedByProvider<EventT, AggregateT, EventIdT> {
    /**
     * Determines which aggregate is deprecated by the given event and provides the event ID.
     *
     * @param event the event that potentially deprecates another aggregate
     * @return the result containing the deprecated aggregate and the converse event ID
     */
    @Trace
    DeprecatedByResult<AggregateT, EventIdT> invoke(EventT event);
}
