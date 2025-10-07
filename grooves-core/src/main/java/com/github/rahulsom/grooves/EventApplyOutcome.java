package com.github.rahulsom.grooves;

/**
 * The outcome of applying an event to a snapshot.
 */
public enum EventApplyOutcome {
    /** Stop processing and return the current snapshot. */
    RETURN,
    /** Continue processing with the next event. */
    CONTINUE
}
