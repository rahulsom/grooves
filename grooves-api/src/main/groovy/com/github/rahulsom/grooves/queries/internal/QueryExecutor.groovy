package com.github.rahulsom.grooves.queries.internal

import com.github.rahulsom.grooves.api.*
import com.github.rahulsom.grooves.api.internal.BaseEvent
import groovy.transform.CompileStatic
import groovy.transform.TailRecursive
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j

/**
 * Created by rahul on 3/13/17.
 */
@Slf4j
class QueryExecutor<A extends AggregateType, E extends BaseEvent<A, E>, S extends BaseSnapshot<A, ?>> {
    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events list of events
     * @param accumulator accumulator of events.
     * @return
     */
    @TailRecursive static List<E> applyReverts(BaseQuery<A, E, S> util, List<E> events, List<E> accumulator) {
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

    @TailRecursive static S applyEvents(BaseQuery<A, E, S> util, S snapshot, List<E> events, List deprecatesList, List<A> aggregates) {
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
    private static EventApplyOutcome callMethod(BaseQuery<A, E, S> util, String methodName, S snapshot, E event) {
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
