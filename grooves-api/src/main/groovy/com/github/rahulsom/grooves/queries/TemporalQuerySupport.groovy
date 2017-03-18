package com.github.rahulsom.grooves.queries

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot
import com.github.rahulsom.grooves.queries.internal.BaseQuery
import com.github.rahulsom.grooves.queries.internal.QueryExecutor
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
trait TemporalQuerySupport<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        SnapshotIdType,
        SnapshotType extends TemporalSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType>>
        implements BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {
    private Logger log = LoggerFactory.getLogger(getClass())

    /**
     *
     * @param aggregate
     * @param startAtTime
     * @return
     */
    private SnapshotType getLatestSnapshot(Aggregate aggregate, Date startAtTime) {
        SnapshotType lastSnapshot = getSnapshot(startAtTime, aggregate).orElse(createEmptySnapshot())

        log.info "    --> Last SnapshotType: ${lastSnapshot.lastEventTimestamp ? lastSnapshot : '<none>'}"
        detachSnapshot(lastSnapshot)

        lastSnapshot.aggregate = aggregate
        lastSnapshot
    }

    /**
     * Given a last event, finds the latest snapshot older than that event
     * @param aggregate
     * @param version
     * @return
     */
    private Tuple2<SnapshotType, List<EventType>> getSnapshotAndEventsSince(Aggregate aggregate, Date moment) {
        getSnapshotAndEventsSince(aggregate, moment, moment)
    }

    private Tuple2<SnapshotType, List<EventType>> getSnapshotAndEventsSince(Aggregate aggregate, Date maxLastEventTimestamp, Date snapshotTime) {
        if (maxLastEventTimestamp) {
            def lastSnapshot = getLatestSnapshot(aggregate, maxLastEventTimestamp)

            List<EventType> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime)
            def uncomputedReverts = uncomputedEvents.findAll {
                it instanceof RevertEvent<Aggregate, EventIdType, EventType>
            } as List<RevertEvent>

            if (uncomputedReverts) {
                log.info "Uncomputed reverts exist: ${uncomputedEvents}"
                getSnapshotAndEventsSince(aggregate, null, snapshotTime)
            } else {
                log.info "Events in pair: ${uncomputedEvents*.position}"
                if (uncomputedEvents) {
                    lastSnapshot.lastEvent = uncomputedEvents.last()
                }
                new Tuple2(lastSnapshot, uncomputedEvents)
            }
        } else {
            def lastSnapshot = createEmptySnapshot()

            List<EventType> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime)

            log.info "Events in pair: ${uncomputedEvents*.position}"
            if (uncomputedEvents) {
                lastSnapshot.lastEvent = uncomputedEvents.last()
            }
            new Tuple2(lastSnapshot, uncomputedEvents)
        }

    }

    /**
     * Computes a snapshot for specified version of an aggregate
     * @param aggregate The aggregate
     * @param moment The moment at which the snapshot is desired
     * @return An Optional SnapshotType. Empty if cannot be computed.
     */
    Optional<SnapshotType> computeSnapshot(Aggregate aggregate, Date moment) {
        Tuple2<SnapshotType, List<EventType>> seTuple2 = getSnapshotAndEventsSince(aggregate, moment)
        def events = seTuple2.second
        def snapshot = seTuple2.first

        if (events.any { it instanceof RevertEvent<Aggregate, EventIdType, EventType> } && snapshot.aggregate) {
            return Optional.empty()
        }
        snapshot.aggregate = aggregate

        List<EventType> forwardEventsSortedBackwards =
                executor.applyReverts(this, events.reverse(false), [])
        assert !forwardEventsSortedBackwards.find { it instanceof RevertEvent<Aggregate, EventIdType, EventType> }

        def retval = executor.applyEvents(this, snapshot, forwardEventsSortedBackwards.reverse(false), [], [aggregate])
        log.info "  --> Computed: $retval"
        Optional.of(retval)
    }

    QueryExecutor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        new QueryExecutor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType>()
    }

    abstract List<EventType> getUncomputedEvents(Aggregate aggregate, SnapshotType lastSnapshot, Date snapshotTime)
}
