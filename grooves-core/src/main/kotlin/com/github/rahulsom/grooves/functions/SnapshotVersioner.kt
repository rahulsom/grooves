package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.logging.Trace

/**
 * Sets the version of the snapshot to the given version.
 */
interface SnapshotVersioner<Snapshot, VersionOrTimestamp> {
    @Trace
    fun invoke(snapshot: Snapshot, versionOrTimestamp: VersionOrTimestamp)
}