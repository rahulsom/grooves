package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.logging.Trace;

/**
 * Given a revert event, provides the event that is reverted by it.
 */
@FunctionalInterface
public interface RevertedEventProvider<EventT> {
    /**
     * Given a revert event, returns the original event that is being reverted.
     *
     * @param event the revert event
     * @return the original event that is reverted by the given revert event
     */
    @Trace
    EventT invoke(EventT event);
}