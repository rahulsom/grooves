package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.logging.Trace

/**
 * Finds a snapshot for a given aggregate that is at most as recent as the given version.
 */
interface SnapshotProvider<Aggregate, VersionOrTimestamp, Snapshot> {
    @Trace(false)
    fun invoke(aggregate: Aggregate, versionOrTimestamp: VersionOrTimestamp?): Snapshot?
}