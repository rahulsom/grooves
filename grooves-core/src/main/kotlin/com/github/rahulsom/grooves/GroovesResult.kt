package com.github.rahulsom.grooves

sealed class GroovesResult<Snapshot, Aggregate, VersionOrTimestamp> {
    data class Success<Snapshot, Aggregate, VersionOrTimestamp>(val snapshot: Snapshot) :
        GroovesResult<Snapshot, Aggregate, VersionOrTimestamp>()

    data class Redirect<Snapshot, Aggregate, VersionOrTimestamp>(val aggregate: Aggregate, val at: VersionOrTimestamp) :
        GroovesResult<Snapshot, Aggregate, VersionOrTimestamp>()
}