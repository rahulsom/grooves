package com.github.rahulsom.grooves.queries

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.internal.BaseEvent
import com.github.rahulsom.grooves.api.RevertEvent
import com.github.rahulsom.grooves.api.TemporalSnapshot
import com.github.rahulsom.grooves.queries.internal.QueryExecutor
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A trait that simplifies computing temporal snapshots from events
 *
 * @param <A> The Aggregate type
 * @param <E> The Event type
 * @param <S> The snapshot type
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
trait TemporalQueryUtil<A extends AggregateType, E extends BaseEvent<A, E>, S extends TemporalSnapshot<A, ?>>
        implements BaseQueryUtil<A, E, S> {
    private Logger log = LoggerFactory.getLogger(getClass())

    /**
     *
     * @param aggregate
     * @param startAtTime
     * @return
     */
    private S getLatestSnapshot(A aggregate, Date startAtTime) {
        S lastSnapshot = getSnapshot(startAtTime, aggregate).orElse(createEmptySnapshot()) as S

        log.info "    --> Last Snapshot: ${lastSnapshot.lastEventTimestamp ? lastSnapshot : '<none>'}"
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
    private Tuple2<S, List<E>> getSnapshotAndEventsSince(A aggregate, Date moment) {
        getSnapshotAndEventsSince(aggregate, moment, moment)
    }

    private Tuple2<S, List<E>> getSnapshotAndEventsSince(A aggregate, Date maxLastEventTimestamp, Date snapshotTime) {
        if (maxLastEventTimestamp) {
            def lastSnapshot = getLatestSnapshot(aggregate, maxLastEventTimestamp)

            List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime)
            def uncomputedReverts = uncomputedEvents.findAll { it instanceof RevertEvent<A, E> } as List<RevertEvent>

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

            List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime)

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
     * @return An Optional Snapshot. Empty if cannot be computed.
     */
    Optional<S> computeSnapshot(A aggregate, Date moment) {
        Tuple2<S, List<E>> seTuple2 = getSnapshotAndEventsSince(aggregate, moment)
        def events = seTuple2.second as List<E>
        def snapshot = seTuple2.first as S

        if (events.any { it instanceof RevertEvent<A, E> } && snapshot.aggregate) {
            return Optional.empty()
        }
        snapshot.aggregate = aggregate

        List<E> forwardEventsSortedBackwards = QueryExecutor.applyReverts(this, events.reverse(false), [] as List<E>)
        assert !forwardEventsSortedBackwards.find { it instanceof RevertEvent<A, E> }

        def retval = QueryExecutor.applyEvents(this, snapshot, forwardEventsSortedBackwards.reverse(false), [], [aggregate])
        log.info "  --> Computed: $retval"
        Optional.of(retval) as Optional<S>
    }

    abstract List<E> getUncomputedEvents(A aggregate, S lastSnapshot, Date snapshotTime)
}
