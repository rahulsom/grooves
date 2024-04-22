package com.github.rahulsom.grooves

interface GroovesQuery<Aggregate, VersionOrTimestamp, Snapshot, Event, EventId> {
    fun computeSnapshot(
        aggregate: Aggregate,
        at: VersionOrTimestamp?,
        redirect: Boolean,
    ): GroovesResult<Snapshot, Aggregate, VersionOrTimestamp>

    fun computeSnapshot(
        aggregate: Aggregate,
        at: VersionOrTimestamp?,
    ) = (
        computeSnapshot(aggregate, at, true)
            as GroovesResult.Success<Snapshot, Aggregate, VersionOrTimestamp>
    ).snapshot
}