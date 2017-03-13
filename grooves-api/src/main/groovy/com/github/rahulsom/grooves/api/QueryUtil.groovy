package com.github.rahulsom.grooves.api

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.TailRecursive
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface BaseQuery<A extends AggregateType, E extends BaseEvent<A, E>, S extends BaseSnapshot<A, ?>> {
    S createEmptySnapshot()

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity
     *
     * @param startWithEvent
     * @param aggregate
     * @return
     */
    Optional<S> getSnapshot(long startWithEvent, A aggregate)

    /**
     * Gets the last snapshot before given timestamp. Is responsible for discarding attached entity
     *
     * @param timestamp
     * @param aggregate
     * @return
     */
    Optional<S> getSnapshot(Date timestamp, A aggregate)

    void detachSnapshot(S retval)

    boolean shouldEventsBeApplied(S snapshot)

    List<E> findEventsForAggregates(List<A> aggregates)

    void addToDeprecates(S snapshot, A otherAggregate)

    E unwrapIfProxy(E event)

    EventApplyOutcome onException(Exception e, S snapshot, E event)

}

@Slf4j
class QueryExecutor<A extends AggregateType, E extends BaseEvent<A, E>, S extends BaseSnapshot<A, ?>> {
    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events list of events
     * @param accumulator accumulator of events.
     * @return
     */
    @TailRecursive @PackageScope static List<E> applyReverts(BaseQuery<A,E,S> util, List<E> events, List<E> accumulator) {
        if (!events) {
            return accumulator
        }
        def head = events.head()
        def tail = events.tail()

        if (head instanceof RevertEvent<A, E>) {
            def revert = head as RevertEvent<A, E>
            if (!(tail*.id).contains(revert.revertedEventId)) {
                throw new Exception("Cannot revert event that does not exist in unapplied list - ${revert.revertedEventId}")
            }
            log.debug "    --> Revert: $revert"
            events.find { it.id == revert.revertedEventId }.revertedBy = revert
            return applyReverts(util, tail.findAll { it.id != revert.revertedEventId }, accumulator)
        } else {
            return applyReverts(util, tail, accumulator + head)
        }
    }

    @TailRecursive @PackageScope static S applyEvents(BaseQuery<A,E,S> util, S snapshot, List<E> events, List deprecatesList, List<A> aggregates) {
        if (events.empty || !util.shouldEventsBeApplied(snapshot)) {
            return snapshot
        }
        def event = events.head()
        def remainingEvents = events.tail()

        log.debug "    --> Event: $event"

        if (event instanceof Deprecates<A, E>) {
            def deprecatesEvent = event as Deprecates<A, E>
            def newSnapshot = util.createEmptySnapshot()
            newSnapshot.aggregate = deprecatesEvent.aggregate

            def otherAggregate = deprecatesEvent.deprecated
            util.addToDeprecates(newSnapshot, otherAggregate)

            def allEvents = util.findEventsForAggregates(aggregates + deprecatesEvent.deprecated)

            def sortedEvents = allEvents.
                    findAll { it.id != deprecatesEvent.id && it.id != deprecatesEvent.converse.id }.
                    toSorted { it.timestamp.time }

            log.info "Sorted Events: [\n    ${sortedEvents.join(',\n    ')}\n]"

            def forwardEventsSortedBackwards = applyReverts(util, sortedEvents.reverse(), [] as List<E>)
            applyEvents(util, newSnapshot, forwardEventsSortedBackwards.reverse(), deprecatesList + deprecatesEvent, aggregates)
        } else if (event instanceof DeprecatedBy<A, E>) {
            def deprecatedByEvent = event as DeprecatedBy<A, E>
            def newAggregate = deprecatedByEvent.deprecator
            snapshot.deprecatedBy = newAggregate
            snapshot
        } else {
            def methodName = "apply${event.class.simpleName}".toString()
            def retval = callMethod(util, methodName, snapshot, event)
            if (retval == EventApplyOutcome.CONTINUE) {
                applyEvents(util, snapshot as S, remainingEvents as List<E>, deprecatesList, aggregates as List<A>)
            } else if (retval == EventApplyOutcome.RETURN) {
                snapshot
            } else {
                throw new Exception("Unexpected value from calling '$methodName'")
            }
        }
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    private static EventApplyOutcome callMethod(BaseQuery<A,E,S> util, String methodName, S snapshot, E event) {
        try {
            util."${methodName}"(util.unwrapIfProxy(event), snapshot) as EventApplyOutcome
        } catch (Exception e1) {
            try {
                util.onException(e1, snapshot, event)
            } catch (Exception e2) {
                def description = "{Snapshot: ${snapshot}; Event: ${event}; method: $methodName; originalException: $e1}"
                log.error "Exception thrown while calling exception handler. $description", e2
                EventApplyOutcome.RETURN
            }
        }
    }

}

/**
 * A trait that simplifies computing snapshots from events
 *
 * @param <A> The Aggregate type
 * @param <E> The Event type
 * @param <S> The snapshot type
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
trait BaseQueryUtil<A extends AggregateType, E extends BaseEvent<A, E>, S extends BaseSnapshot<A, ?>>
        implements BaseQuery<A,E,S>{
    private static Logger log = LoggerFactory.getLogger(getClass())

}

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
        implements BaseQueryUtil<A,E,S> {
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
trait VersionedQueryUtil<A extends AggregateType, E extends BaseEvent<A, E>, S extends VersionedSnapshot<A, ?>>
        implements BaseQueryUtil<A,E,S> {
    private Logger log = LoggerFactory.getLogger(getClass())

    /**
     *
     * @param aggregate
     * @param startWithEvent
     * @return
     */
    private S getLatestSnapshot(A aggregate, long startWithEvent) {
        S lastSnapshot = getSnapshot(startWithEvent, aggregate).orElse(createEmptySnapshot()) as S

        log.info "    --> Last Snapshot: ${lastSnapshot.lastEventPosition ? lastSnapshot : '<none>'}"
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
    private Tuple2<S, List<E>> getSnapshotAndEventsSince(A aggregate, long version) {
        getSnapshotAndEventsSince(aggregate, version, version)
    }

    private Tuple2<S, List<E>> getSnapshotAndEventsSince(A aggregate, long maxLastEventInSnapshot, long version) {
        if (maxLastEventInSnapshot) {
            def lastSnapshot = getLatestSnapshot(aggregate, maxLastEventInSnapshot)

            List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, version)
            def uncomputedReverts = uncomputedEvents.findAll { it instanceof RevertEvent<A, E> } as List<RevertEvent>

            if (uncomputedReverts) {
                log.info "Uncomputed reverts exist: ${uncomputedEvents}"
                getSnapshotAndEventsSince(aggregate, 0, version)
            } else {
                log.info "Events in pair: ${uncomputedEvents*.position}"
                if (uncomputedEvents) {
                    lastSnapshot.lastEvent = uncomputedEvents.last()
                }
                new Tuple2(lastSnapshot, uncomputedEvents)
            }
        } else {
            def lastSnapshot = createEmptySnapshot()

            List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, version)

            log.info "Events in pair: ${uncomputedEvents*.position}"
            if (uncomputedEvents) {
                lastSnapshot.lastEvent = uncomputedEvents.last()
            }
            new Tuple2(lastSnapshot, uncomputedEvents)
        }

    }

    abstract List<E> getUncomputedEvents(A aggregate, S lastSnapshot, long version)

    /**
     * Computes a snapshot for specified version of an aggregate
     * @param aggregate The aggregate
     * @param version The version number, starting at 1
     * @return An Optional Snapshot. Empty if cannot be computed.
     */
    Optional<S> computeSnapshot(A aggregate, long version) {

        Tuple2<S, List<E>> seTuple2 = getSnapshotAndEventsSince(aggregate, version)
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

}

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
trait QueryUtil<A extends AggregateType, E extends BaseEvent<A, E>, S extends Snapshot<A, ?>>
        extends VersionedQueryUtil<A,E,S> implements TemporalQueryUtil<A,E,S> {

}
