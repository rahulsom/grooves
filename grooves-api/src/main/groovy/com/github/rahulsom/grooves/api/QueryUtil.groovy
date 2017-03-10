package com.github.rahulsom.grooves.api

import groovy.transform.CompileStatic
import groovy.transform.TailRecursive
import groovy.transform.TypeCheckingMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
trait QueryUtil<A extends AggregateType, E extends BaseEvent<A, E>, S extends Snapshot<A, ?>> {
    private Logger log = LoggerFactory.getLogger(getClass())

    abstract S createEmptySnapshot()

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity
     *
     * @param startWithEvent
     * @param aggregate
     * @return
     */
    abstract Optional<S> getSnapshot(long startWithEvent, A aggregate)

    /**
     * Gets the last snapshot before given timestamp. Is responsible for discarding attached entity
     *
     * @param timestamp
     * @param aggregate
     * @return
     */
    abstract Optional<S> getSnapshot(Date timestamp, A aggregate)

    abstract void detachSnapshot(S retval)

    /**
     *
     * @param aggregate
     * @param startWithEvent
     * @return
     */
    private S getLatestSnapshot(A aggregate, long startWithEvent) {
        S lastSnapshot = getSnapshot(startWithEvent, aggregate).orElse(createEmptySnapshot()) as S

        log.info "    --> Last Snapshot: ${lastSnapshot.lastEvent ? lastSnapshot : '<none>'}"
        detachSnapshot(lastSnapshot)

        lastSnapshot.aggregate = aggregate
        lastSnapshot
    }

    /**
     *
     * @param aggregate
     * @param startAtTime
     * @return
     */
    private S getLatestSnapshot(A aggregate, Date startAtTime) {
        S lastSnapshot = getSnapshot(startAtTime, aggregate).orElse(createEmptySnapshot()) as S

        log.info "    --> Last Snapshot: ${lastSnapshot.lastEvent ? lastSnapshot : '<none>'}"
        detachSnapshot(lastSnapshot)

        lastSnapshot.aggregate = aggregate
        lastSnapshot
    }

    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events list of events
     * @param accumulator accumulator of events.
     * @return
     */
    @TailRecursive
    private List<E> applyReverts(List<E> events, List<E> accumulator) {
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
            return applyReverts(tail.findAll { it.id != revert.revertedEventId }, accumulator)
        } else {
            return applyReverts(tail, accumulator + head)
        }
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

    /**
     * Given a last event, finds the latest snapshot older than that event
     * @param aggregate
     * @param version
     * @return
     */
    private Tuple2<S, List<E>> getSnapshotAndEventsSince(A aggregate, Date moment) {
        getSnapshotAndEventsSince(aggregate, moment, moment)
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
                    lastSnapshot.lastEvent = uncomputedEvents*.position.max()
                    lastSnapshot.lastEventTimestamp = uncomputedEvents*.timestamp.max()
                }
                new Tuple2(lastSnapshot, uncomputedEvents)
            }
        } else {
            def lastSnapshot = createEmptySnapshot()

            List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, version)

            log.info "Events in pair: ${uncomputedEvents*.position}"
            if (uncomputedEvents) {
                lastSnapshot.lastEvent = uncomputedEvents*.position.max()
                lastSnapshot.lastEventTimestamp = uncomputedEvents*.timestamp.max()
            }
            new Tuple2(lastSnapshot, uncomputedEvents)
        }

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
                    lastSnapshot.lastEvent = uncomputedEvents*.position.max()
                    lastSnapshot.lastEventTimestamp = uncomputedEvents*.timestamp.max()
                }
                new Tuple2(lastSnapshot, uncomputedEvents)
            }
        } else {
            def lastSnapshot = createEmptySnapshot()

            List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime)

            log.info "Events in pair: ${uncomputedEvents*.position}"
            if (uncomputedEvents) {
                lastSnapshot.lastEvent = uncomputedEvents*.position.max()
                lastSnapshot.lastEventTimestamp = uncomputedEvents*.timestamp.max()
            }
            new Tuple2(lastSnapshot, uncomputedEvents)
        }

    }

    abstract List<E> getUncomputedEvents(A aggregate, S lastSnapshot, long version)

    abstract List<E> getUncomputedEvents(A aggregate, S lastSnapshot, Date snapshotTime)

    abstract boolean shouldEventsBeApplied(S snapshot)

    abstract List<E> findEventsForAggregates(List<A> aggregates)

    @TailRecursive
    private S applyEvents(S snapshot, List<E> events, List deprecatesList, List<A> aggregates) {
        if (events.empty || !shouldEventsBeApplied(snapshot)) {
            return snapshot
        }
        def event = events.head()
        def remainingEvents = events.tail()

        log.debug "    --> Event: $event"

        if (event instanceof Deprecates<A, E>) {
            def deprecatesEvent = event as Deprecates<A, E>
            def newSnapshot = createEmptySnapshot()
            newSnapshot.aggregate = deprecatesEvent.aggregate

            def otherAggregate = deprecatesEvent.deprecated
            addToDeprecates(newSnapshot, otherAggregate)

            def allEvents = findEventsForAggregates(aggregates + deprecatesEvent.deprecated)

            def sortedEvents = allEvents.
                    findAll { it.id != deprecatesEvent.id && it.id != deprecatesEvent.converse.id }.
                    toSorted { it.timestamp.time }

            log.info "Sorted Events: [\n    ${sortedEvents.join(',\n    ')}\n]"

            def forwardEventsSortedBackwards = applyReverts(sortedEvents.reverse(), [] as List<E>)
            applyEvents(newSnapshot, forwardEventsSortedBackwards.reverse(), deprecatesList + deprecatesEvent, aggregates)
        } else if (event instanceof DeprecatedBy<A, E>) {
            def deprecatedByEvent = event as DeprecatedBy<A, E>
            def newAggregate = deprecatedByEvent.deprecator
            snapshot.deprecatedBy = newAggregate
            snapshot
        } else {
            def methodName = "apply${event.class.simpleName}".toString()
            def retval = callMethod(methodName, snapshot, event)
            if (retval == EventApplyOutcome.CONTINUE) {
                applyEvents(snapshot as S, remainingEvents as List<E>, deprecatesList, aggregates as List<A>)
            } else if (retval == EventApplyOutcome.RETURN) {
                snapshot
            } else {
                throw new Exception("Unexpected value from calling '$methodName'")
            }
        }
    }

    abstract void addToDeprecates(S snapshot, A otherAggregate)

    @CompileStatic(TypeCheckingMode.SKIP)
    private EventApplyOutcome callMethod(String methodName, S snapshot, E event) {
        try {
            this."${methodName}"(unwrapIfProxy(event), snapshot) as EventApplyOutcome
        } catch (Exception e1) {
            try {
                onException(e1, snapshot, event)
            } catch (Exception e2) {
                def description = "{Snapshot: ${snapshot}; Event: ${event}; method: $methodName; originalException: $e1}"
                log.error "Exception thrown while calling exception handler. $description", e2
                EventApplyOutcome.RETURN
            }
        }
    }

    abstract E unwrapIfProxy(E event)

    abstract EventApplyOutcome onException(Exception e, S snapshot, E event)

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

        List<E> forwardEventsSortedBackwards = applyReverts(events.reverse(), [] as List<E>)
        assert !forwardEventsSortedBackwards.find { it instanceof RevertEvent<A, E> }

        def retval = applyEvents(snapshot, forwardEventsSortedBackwards.reverse(), [], [aggregate])
        log.info "  --> Computed: $retval"
        Optional.of(retval)
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

        List<E> forwardEventsSortedBackwards = applyReverts(events.reverse(), [] as List<E>)
        assert !forwardEventsSortedBackwards.find { it instanceof RevertEvent<A, E> }

        def retval = applyEvents(snapshot, forwardEventsSortedBackwards.reverse(), [], [aggregate])
        log.info "  --> Computed: $retval"
        Optional.of(retval)
    }

}
