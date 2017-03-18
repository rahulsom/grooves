package com.github.rahulsom.grooves.queries.internal

import com.github.rahulsom.grooves.api.*
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot
import groovy.transform.CompileStatic
import groovy.transform.TailRecursive
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.InvokerHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class QueryExecutor<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        SnapshotIdType,
        SnapshotType extends BaseSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType>>
        implements
                Executor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {

    protected Logger log = LoggerFactory.getLogger(getClass())

    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events list of events
     * @param accumulator accumulator of events.
     * @return
     */
    List<EventType> applyReverts(
            BaseQuery<Aggregate, EventIdType, EventType,SnapshotIdType, SnapshotType> util,
            List<EventType> events,
            List<EventType> accumulator) {
        if (!events) {
            return accumulator
        }
        def head = events.head()
        def tail = events.tail()

        if (head instanceof RevertEvent<Aggregate, EventIdType, EventType>) {
            def revert = head as RevertEvent<Aggregate, EventIdType, EventType>
            if (!(tail*.id).contains(revert.revertedEventId)) {
                throw new GroovesException("Cannot revert event that does not exist in unapplied list - ${revert.revertedEventId}")
            }
            log.debug "    --> Revert: $revert"
            events.find { it.id == revert.revertedEventId }.revertedBy = revert
            return applyReverts(util, tail.findAll { it.id != revert.revertedEventId }, accumulator)
        } else {
            return applyReverts(util, tail, accumulator + head)
        }
    }

    SnapshotType applyEvents(
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> util,
            SnapshotType snapshot,
            List<EventType> events,
            List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList,
            List<Aggregate> aggregates) {
        if (events.empty || !util.shouldEventsBeApplied(snapshot)) {
            return snapshot
        }
        def event = events.head()
        def remainingEvents = events.tail()

        log.debug "    --> Event: $event"

        if (event instanceof Deprecates<Aggregate, EventIdType, EventType>) {
            applyDeprecates(event, util, aggregates, deprecatesList)
        } else if (event instanceof DeprecatedBy<Aggregate, EventIdType, EventType>) {
            applyDeprecatedBy(event, snapshot)
        } else {
            def methodName = "apply${event.class.simpleName}".toString()
            def retval = callMethod(util, methodName, snapshot, event)
            if (retval == EventApplyOutcome.CONTINUE) {
                applyEvents(util, snapshot as SnapshotType, remainingEvents, deprecatesList, aggregates as List<Aggregate>)
            } else if (retval == EventApplyOutcome.RETURN) {
                snapshot
            } else {
                throw new GroovesException("Unexpected value from calling '$methodName'")
            }
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    protected SnapshotType applyDeprecatedBy(EventType event, SnapshotType snapshot) {
        def deprecatedByEvent = event as DeprecatedBy<Aggregate, EventIdType, EventType>
        def newAggregate = deprecatedByEvent.deprecator
        snapshot.deprecatedBy = newAggregate
        snapshot
    }

    protected SnapshotType applyDeprecates(EventType event, BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> util, List<Aggregate> aggregates, List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList) {
        def deprecatesEvent = event as Deprecates<Aggregate, EventIdType, EventType>
        def newSnapshot = util.createEmptySnapshot()
        newSnapshot.aggregate = deprecatesEvent.aggregate

        def otherAggregate = deprecatesEvent.deprecated
        util.addToDeprecates(newSnapshot, otherAggregate)

        def allEvents = util.findEventsForAggregates(aggregates + deprecatesEvent.deprecated)

        def sortedEvents = allEvents.
                findAll { it.id != deprecatesEvent.id && it.id != deprecatesEvent.converse.id }.
                toSorted { it.timestamp.time }

        log.info "Sorted Events: [\n    ${sortedEvents.join(',\n    ')}\n]"

        def forwardEventsSortedBackwards = applyReverts(util, sortedEvents.reverse(), [] as List<EventType>)
        applyEvents(util, newSnapshot, forwardEventsSortedBackwards.reverse(), deprecatesList + deprecatesEvent, aggregates)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @CompileStatic(TypeCheckingMode.SKIP) protected
    EventApplyOutcome callMethod(
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> util,
            String methodName,
            SnapshotType snapshot,
            EventType event) {
        try {
            InvokerHelper.invokeMethod(util, methodName, [util.unwrapIfProxy(event), snapshot].toArray()) as EventApplyOutcome
        } catch (Exception e1) {
            try {
                util.onException(e1, snapshot, event)
            } catch (Exception e2) {
                def description = "{SnapshotType: ${snapshot}; Event: ${event}; method: $methodName; originalException: $e1}"
                log.error "Exception thrown while calling exception handler. $description", e2
                EventApplyOutcome.RETURN
            }
        }
    }

}
