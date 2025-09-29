package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.logging.Trace;

/**
 * Finds a snapshot for a given aggregate that is at most as recent as the given version.
 */
@FunctionalInterface
public interface SnapshotProvider<AggregateT, VersionOrTimestampT, SnapshotT> {
    /**
     * Finds a snapshot for the aggregate that is at most as recent as the version.
     * If no snapshot exists at or before the version, this may return null or empty.
     *
     * @param aggregate the aggregate to find a snapshot for
     * @param versionOrTimestamp the maximum version or timestamp to search up to
     * @return the most recent snapshot at or before the specified version, or null if none exists
     */
    @Trace
    SnapshotT invoke(
            AggregateT aggregate,
            VersionOrTimestampT versionOrTimestamp
    );
}