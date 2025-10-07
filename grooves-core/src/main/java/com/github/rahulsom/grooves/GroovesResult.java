package com.github.rahulsom.grooves;

/**
 * Represents the result of a snapshot computation operation.
 * Can either be a successful computation or a redirect to another aggregate/version.
 *
 * @param <SnapshotT> the type of the snapshot
 * @param <AggregateT> the type of the aggregate
 * @param <VersionOrTimestampT> the type used for versioning
 */
public sealed interface GroovesResult<SnapshotT, AggregateT, VersionOrTimestampT> {

    /**
     * Represents a successful snapshot computation.
     *
     * @param <SnapshotT> the type of the snapshot
     * @param <AggregateT> the type of the aggregate
     * @param <VersionOrTimestampT> the type used for versioning
     * @param snapshot the computed snapshot
     */
    record Success<SnapshotT, AggregateT, VersionOrTimestampT>(SnapshotT snapshot)
            implements GroovesResult<SnapshotT, AggregateT, VersionOrTimestampT> {}

    /**
     * Represents a redirect to another aggregate/version during snapshot computation.
     *
     * @param <SnapshotT> the type of the snapshot
     * @param <AggregateT> the type of the aggregate
     * @param <VersionOrTimestampT> the type used for versioning
     * @param aggregate the aggregate to redirect to
     * @param at the version/timestamp to redirect to
     */
    record Redirect<SnapshotT, AggregateT, VersionOrTimestampT>(AggregateT aggregate, VersionOrTimestampT at)
            implements GroovesResult<SnapshotT, AggregateT, VersionOrTimestampT> {}
}
