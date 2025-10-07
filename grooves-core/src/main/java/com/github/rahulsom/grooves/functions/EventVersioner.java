package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.logging.Trace;

/**
 * Provides the version of the event.
 */
@FunctionalInterface
public interface EventVersioner<EventT, VersionOrTimestampT> {
    /**
     * Extracts the version or timestamp from the given event.
     *
     * @param event the event to get the version/timestamp from
     * @return the version or timestamp of the event
     */
    @Trace
    VersionOrTimestampT invoke(EventT event);
}
