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
import rx.Observable
import rx.Observer

/**
 * Aggregate trait that simplifies computing temporal snapshots from events
 *
 * @param <Aggregate>      The aggregate over which the query executes
 * @param <EventIdType>    The type of the Event's id field
 * @param <EventType>      The type of the Event
 * @param <SnapshotIdType> The type of the Snapshot's id field
 * @param <SnapshotType>   The type of the Snapshot
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
     * @param maxTimestamp
     * @return
     */
    private Observable<SnapshotType> getLatestSnapshot(Aggregate aggregate, Date maxTimestamp) {
        getSnapshot(maxTimestamp, aggregate).
                defaultIfEmpty(createEmptySnapshot()).
                map {
                    log.info "    --> Last SnapshotType: ${it.lastEventTimestamp ? it : '<none>'}"
                    detachSnapshot(it)

                    it.aggregate = aggregate
                    it
                }
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
            def lastSnapshot = getLatestSnapshot(aggregate, maxLastEventTimestamp).toBlocking().first()

            List<EventType> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime).toList().toBlocking().first()
            def uncomputedReverts = uncomputedEvents.findAll {
                it instanceof RevertEvent<Aggregate, EventIdType, EventType>
            } as List<RevertEvent>

            if (uncomputedReverts) {
                log.info "Uncomputed reverts exist: ${uncomputedEvents}"
                getSnapshotAndEventsSince(aggregate, null, snapshotTime)
            } else {
                log.info "Events in pair: ${uncomputedEvents*.position}"
                new Tuple2(lastSnapshot, uncomputedEvents)
            }
        } else {
            def lastSnapshot = createEmptySnapshot()

            List<EventType> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime).toList().toBlocking().first()

            log.info "Events in pair: ${uncomputedEvents*.position}"
            new Tuple2(lastSnapshot, uncomputedEvents)
        }

    }

    /**
     * Computes a snapshot for specified version of an aggregate
     * @param aggregate The aggregate
     * @param moment The moment at which the snapshot is desired
     * @return An Optional SnapshotType. Empty if cannot be computed.
     */
    Observable<SnapshotType> computeSnapshot(Aggregate aggregate, Date moment) {
        Tuple2<SnapshotType, List<EventType>> seTuple2 = getSnapshotAndEventsSince(aggregate, moment)
        def events = seTuple2.second
        def snapshot = seTuple2.first

        if (events.any { it instanceof RevertEvent<Aggregate, EventIdType, EventType> } && snapshot.aggregate) {
            return Observable.empty()
        }
        snapshot.aggregate = aggregate

        Observable<EventType> forwardOnlyEvents = executor.applyReverts(this, Observable.from(events)).
                onErrorReturn {
                    executor.applyReverts(this, Observable.from(getSnapshotAndEventsSince(aggregate, null, moment).second))
                }

        executor.
                applyEvents(this, snapshot, forwardOnlyEvents, [], [aggregate]).
                doOnEach ({ SnapshotType snapshotType ->
                    if (events) {
                        snapshotType.lastEvent = events.last()
                    }
                    log.info "  --> Computed: $snapshotType"
                } as Observer<SnapshotType>)
    }

    QueryExecutor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        new QueryExecutor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType>()
    }

    abstract Observable<EventType> getUncomputedEvents(Aggregate aggregate, SnapshotType lastSnapshot, Date snapshotTime)
}
