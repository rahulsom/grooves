package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.logging.Trace;

/**
 * Sets the version of the snapshot to the given version.
 */
@FunctionalInterface
public interface SnapshotVersioner<SnapshotT, VersionOrTimestampT> {
    /**
     * Updates the snapshot with the specified version or timestamp.
     * This is typically used to mark the snapshot as being at a particular point in time.
     *
     * @param snapshot the snapshot to update with version information
     * @param versionOrTimestamp the version or timestamp to set on the snapshot
     */
    @Trace
    void invoke(SnapshotT snapshot, VersionOrTimestampT versionOrTimestamp);
}
