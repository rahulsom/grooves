package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.EventApplyOutcome;
import com.github.rahulsom.grooves.logging.Trace;

/**
 * A function that handles exceptions thrown when applying an event to a snapshot.
 */
@FunctionalInterface
public interface ExceptionHandler<SnapshotT, EventT> {
    /**
     * Handles exceptions that occur when applying an event to a snapshot.
     *
     * @param exception the exception that was thrown
     * @param snapshot the snapshot that was being modified when the exception occurred
     * @param event the event that caused the exception
     * @return the outcome indicating how to proceed after handling the exception
     */
    @Trace
    EventApplyOutcome invoke(Exception exception, SnapshotT snapshot, EventT event);
}
