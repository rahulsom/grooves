package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.EventApplyOutcome;
import com.github.rahulsom.grooves.logging.Trace;

/**
 * Applies the event to a snapshot.
 */
public interface EventHandler<EventT, SnapshotT> {
    /**
     * Applies the given event to the provided snapshot.
     *
     * @param event the event to apply to the snapshot
     * @param snapshot the snapshot to modify with the event
     * @return the outcome indicating whether to continue processing or return the snapshot
     */
    @Trace
    EventApplyOutcome invoke(
            EventT event,
            SnapshotT snapshot
    );
}