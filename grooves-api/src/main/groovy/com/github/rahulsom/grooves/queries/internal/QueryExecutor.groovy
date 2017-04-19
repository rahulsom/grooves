package com.github.rahulsom.grooves.queries.internal

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.api.GroovesException
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.codehaus.groovy.runtime.InvokerHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rx.Observable

/**
 *
 * @param <Aggregate>      The aggregate over which the query executes
 * @param <EventIdType>    The type of the Event's id field
 * @param <EventType>      The type of the Event
 * @param <SnapshotIdType> The type of the Snapshot's id field
 * @param <SnapshotType>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
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
    @Override
    Observable<EventType> applyReverts(
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> query,
            Observable<EventType> events) {

        events.toList().flatMap { eventList ->
            log.debug "EventList: ${eventList*.id}"
            def forwardEvents = []
            while (eventList) {
                def head = eventList.pop()
                if (head instanceof RevertEvent<Aggregate, EventIdType, EventType>) {
                    def revertedEventId = (head as RevertEvent).revertedEventId
                    def revertedEvent = eventList.find {
                        it.id == revertedEventId
                    }
                    if (revertedEvent) {
                        eventList.remove(revertedEvent)
                    } else {
                        throw new GroovesException("Cannot revert event that does not exist in unapplied list - ${revertedEventId}")
                    }
                } else {
                    forwardEvents.add(0, head)
                }
            }
            assert !forwardEvents.find { it instanceof RevertEvent<Aggregate, EventIdType, EventType> }
            Observable.from(forwardEvents)
        }
    }

    @Override
    Observable<SnapshotType> applyEvents(
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> query,
            SnapshotType initialSnapshot,
            Observable<EventType> events,
            List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList,
            List<Aggregate> aggregates) {

        boolean stopApplyingEvents = false
        events.reduce(initialSnapshot) { SnapshotType snapshot, EventType event ->
            if (!query.shouldEventsBeApplied(snapshot) || stopApplyingEvents) {
                return snapshot
            } else {
                log.debug "    --> Event: $event"

                if (event instanceof Deprecates<Aggregate, EventIdType, EventType>) {
                    applyDeprecates(event, query, aggregates, deprecatesList)
                } else if (event instanceof DeprecatedBy<Aggregate, EventIdType, EventType>) {
                    applyDeprecatedBy(event, snapshot)
                } else {
                    def methodName = "apply${event.class.simpleName}".toString()
                    def retval = callMethod(query, methodName, snapshot, event)
                    if (retval == EventApplyOutcome.CONTINUE) {
                        // applyEvents(query, snapshot as SnapshotType, remainingEvents, deprecatesList, aggregates as List<Aggregate>)
                        snapshot
                    } else if (retval == EventApplyOutcome.RETURN) {
                        stopApplyingEvents = true
                        snapshot
                    } else {
                        throw new GroovesException("Unexpected value from calling '$methodName'")
                    }
                }
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

    protected SnapshotType applyDeprecates(
            EventType event,
            BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> util,
            List<Aggregate> aggregates,
            List<Deprecates<Aggregate, EventIdType, EventType>> deprecatesList) {
        def deprecatesEvent = event as Deprecates<Aggregate, EventIdType, EventType>
        def newSnapshot = util.createEmptySnapshot()
        newSnapshot.aggregate = deprecatesEvent.aggregate

        def otherAggregate = deprecatesEvent.deprecated
        util.addToDeprecates(newSnapshot, otherAggregate)

        util.
                findEventsForAggregates(aggregates + deprecatesEvent.deprecated).
                filter { it.id != deprecatesEvent.id && it.id != deprecatesEvent.converse.id }.
                toSortedList { a, b -> a.timestamp.time <=> b.timestamp.time }.
                flatMap { sortedEvents ->
                    log.info "Sorted Events: [\n    ${sortedEvents.join(',\n    ')}\n]"
                    def forwardEventsSortedBackwards = applyReverts(util, Observable.from(sortedEvents))
                    applyEvents(util, newSnapshot, forwardEventsSortedBackwards, deprecatesList + deprecatesEvent, aggregates)
                }.
                toBlocking().
                first()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @CompileStatic(TypeCheckingMode.SKIP)
    protected EventApplyOutcome callMethod(
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
