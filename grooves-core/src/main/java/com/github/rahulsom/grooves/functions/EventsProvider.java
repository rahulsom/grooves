package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.logging.Trace;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides events that are later than the last known snapshot up until the version specified.
 * If the version specified is null, then all events are returned.
 */
@FunctionalInterface
public interface EventsProvider<AggregateT, VersionOrTimestampT, SnapshotT, EventT> {
    /**
     * Provides a stream of events for the aggregates within the version/timestamp range.
     *
     * @param aggregates the list of aggregates to get events for
     * @param versionOrTimestamp the maximum version or timestamp to include, or null for all events
     * @param lastSnapshot the last known snapshot to determine the starting point for events
     * @return a stream of events that occurred after the last snapshot up to the specified version
     */
    @Trace
    Stream<EventT> invoke(List<AggregateT> aggregates, VersionOrTimestampT versionOrTimestamp, SnapshotT lastSnapshot);
}
