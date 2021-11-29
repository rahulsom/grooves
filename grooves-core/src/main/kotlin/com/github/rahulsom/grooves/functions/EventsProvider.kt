package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.logging.Trace
import java.util.stream.Stream

/**
 * Provides events that are later than the last known snapshot up until the version specified.
 * If the version specified is null, then all events are returned.
 */
interface EventsProvider<Aggregate, VersionOrTimestamp, Snapshot, Event> {
    @Trace
    fun invoke(
        aggregates: List<Aggregate>,
        versionOrTimestamp: VersionOrTimestamp?,
        lastSnapshot: Snapshot
    ): Stream<Event>
}