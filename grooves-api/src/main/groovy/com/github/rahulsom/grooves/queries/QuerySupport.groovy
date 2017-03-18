package com.github.rahulsom.grooves.queries

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.CompileStatic

/**
 * Aggregate trait that simplifies computing temporal snapshots from events
 *
 * @param <Aggregate> The Aggregate type
 * @param <EventType> The Event type
 * @param <SnapshotType> The snapshot type
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
trait QuerySupport<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        SnapshotIdType,
        SnapshotType extends Snapshot<Aggregate, SnapshotIdType, EventIdType, EventType>>
        extends VersionedQuerySupport<Aggregate,EventIdType, EventType,SnapshotIdType, SnapshotType>
        implements TemporalQuerySupport<Aggregate,EventIdType, EventType,SnapshotIdType, SnapshotType> {

}
