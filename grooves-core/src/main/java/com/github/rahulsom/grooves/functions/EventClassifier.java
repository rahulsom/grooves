package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.EventType;
import com.github.rahulsom.grooves.logging.Trace;

/**
 * Classifies the type of event.
 */
@FunctionalInterface
public interface EventClassifier<EventT> {
    /**
     * Determines the type of the given event.
     *
     * @param event the event to classify
     * @return the type of the event (Normal, Revert, Deprecates, or DeprecatedBy)
     */
    @Trace
    EventType invoke(EventT event);
}