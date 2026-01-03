package com.github.rahulsom.grooves;

/**
 * Core interface for querying snapshots from event streams using the Grooves framework.
 * Provides methods to compute snapshots at specific points in time with support for redirects.
 *
 * @param <AggregateT> the type of the aggregate being queried
 * @param <VersionOrTimestampT> the type used for versioning (timestamp or version number)
 * @param <SnapshotT> the type of the computed snapshot
 */
public interface GroovesQuery<AggregateT, VersionOrTimestampT, SnapshotT> {

    /**
     * Computes a snapshot for the given aggregate at the specified point in time.
     *
     * @param aggregate the aggregate to compute the snapshot for
     * @param at the point in time (version or timestamp) to compute the snapshot at
     * @param redirect whether to handle redirects during computation
     * @return the result containing either the computed snapshot or redirect information
     */
    GroovesResult<SnapshotT, AggregateT, VersionOrTimestampT> computeSnapshot(
            AggregateT aggregate, VersionOrTimestampT at, boolean redirect);

    /**
     * Convenience method to compute a snapshot with redirects enabled.
     * Returns only the snapshot portion of the result, assuming success.
     *
     * @param aggregate the aggregate to compute the snapshot for
     * @param at the point in time (version or timestamp) to compute the snapshot at
     * @return the computed snapshot
     */
    default SnapshotT computeSnapshot(AggregateT aggregate, VersionOrTimestampT at) {
        return ((GroovesResult.Success<SnapshotT, AggregateT, VersionOrTimestampT>)
                        computeSnapshot(aggregate, at, true))
                .snapshot();
    }
}
