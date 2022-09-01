package com.github.rahulsom.grooves.impl

import com.github.rahulsom.grooves.EventApplyOutcome
import com.github.rahulsom.grooves.EventApplyOutcome.RETURN
import com.github.rahulsom.grooves.EventType.DeprecatedBy
import com.github.rahulsom.grooves.EventType.Deprecates
import com.github.rahulsom.grooves.EventType.Normal
import com.github.rahulsom.grooves.EventType.Revert
import com.github.rahulsom.grooves.GroovesQuery
import com.github.rahulsom.grooves.GroovesResult
import com.github.rahulsom.grooves.functions.ApplyMoreEventsPredicate
import com.github.rahulsom.grooves.functions.DeprecatedByProvider
import com.github.rahulsom.grooves.functions.Deprecator
import com.github.rahulsom.grooves.functions.EmptySnapshotProvider
import com.github.rahulsom.grooves.functions.EventClassifier
import com.github.rahulsom.grooves.functions.EventHandler
import com.github.rahulsom.grooves.functions.EventIdProvider
import com.github.rahulsom.grooves.functions.EventVersioner
import com.github.rahulsom.grooves.functions.EventsProvider
import com.github.rahulsom.grooves.functions.ExceptionHandler
import com.github.rahulsom.grooves.functions.RevertedEventProvider
import com.github.rahulsom.grooves.functions.SnapshotProvider
import com.github.rahulsom.grooves.functions.SnapshotVersioner
import com.github.rahulsom.grooves.logging.IndentedLogging
import com.github.rahulsom.grooves.logging.Trace
import org.slf4j.LoggerFactory
import java.util.stream.Collectors

class GroovesQueryImpl<VersionOrTimestamp, Snapshot, Aggregate, Event, EventId>(
    private val snapshotProvider: SnapshotProvider<Aggregate, VersionOrTimestamp, Snapshot>,
    private val emptySnapshotProvider: EmptySnapshotProvider<Aggregate, Snapshot>,
    private val eventsProvider: EventsProvider<Aggregate, VersionOrTimestamp, Snapshot, Event>,
    private val applyMoreEventsPredicate: ApplyMoreEventsPredicate<Snapshot>,
    private val eventClassifier: EventClassifier<Event>,
    private val deprecator: Deprecator<Snapshot, Event>,
    private val exceptionHandler: ExceptionHandler<Snapshot, Event>,
    private val eventHandler: EventHandler<Event, Snapshot>,
    private val eventVersioner: EventVersioner<Event, VersionOrTimestamp>,
    private val snapshotVersioner: SnapshotVersioner<Snapshot, VersionOrTimestamp>,
    private val deprecatedByProvider: DeprecatedByProvider<Event, Aggregate, EventId>,
    private val revertedEventProvider: RevertedEventProvider<Event>,
    private val eventIdProvider: EventIdProvider<Event, EventId>
) : GroovesQuery<Aggregate, VersionOrTimestamp, Snapshot, Event, EventId> {

    private val log = LoggerFactory.getLogger(javaClass)

    @Trace
    override fun computeSnapshot(aggregate: Aggregate, at: VersionOrTimestamp?, redirect: Boolean): GroovesResult<Snapshot, Aggregate, VersionOrTimestamp> {
        val providedSnapshot = snapshotProvider.invoke(aggregate, at)
        val snapshot = providedSnapshot ?: emptySnapshotProvider.invoke(aggregate)
        val events = eventsProvider.invoke(listOf(aggregate), at, snapshot).collect(Collectors.toList())

        return computeSnapshotImpl(events, snapshot, listOf(aggregate), at, redirect) { c, s ->
            if (s != null) {
                log.trace("${c.data} -> $s")
            }
            IndentedLogging.stepOut()
        }
    }

    // @JvmInline
    private inline class CallIdentifier(val data: String)

    private tailrec fun computeSnapshotImpl(
        events: List<Event>,
        snapshot: Snapshot,
        aggregates: List<Aggregate>,
        at: VersionOrTimestamp?,
        redirect: Boolean,
        beforeReturn: (CallIdentifier, Snapshot?) -> Unit
    ): GroovesResult<Snapshot, Aggregate, VersionOrTimestamp> {
        val indent = IndentedLogging.indent()

        val callIdentifier = CallIdentifier(
            "${indent}computeSnapshotImpl(<... ${events.size} items>, $snapshot, $aggregates, $at)"
        )
        log.trace(callIdentifier.data)
        IndentedLogging.stepIn()
        val (revertEvents, forwardEvents) = events
            .partition { eventClassifier.invoke(it) == Revert }
            .let { it.first.toMutableList() to it.second.toMutableList() }

        if (revertsExistOutsideEvents(revertEvents, indent, forwardEvents)) {
            val snapshot1 = emptySnapshotProvider.invoke(aggregates[0])
            val events1 = eventsProvider.invoke(aggregates, at, snapshot1).collect(Collectors.toList())
            return computeSnapshotImpl(events1, snapshot1, aggregates, at, redirect) { c, s ->
                beforeReturn(c, s)
                IndentedLogging.stepOut()
            }
        }

        val deprecatesEvents = forwardEvents.filter { eventClassifier.invoke(it) == Deprecates }.toMutableList()
        while (deprecatesEvents.isNotEmpty()) {
            val event = deprecatesEvents.removeAt(0)
            val converseId = deprecatedByProvider.invoke(event).eventId
            deprecator.invoke(snapshot, event)
            forwardEvents.remove(event)
            forwardEvents.removeIf { eventIdProvider.invoke(it) == converseId }
        }

        for (event in forwardEvents) {
            if (applyMoreEventsPredicate.invoke(snapshot)) {
                val outcome: EventApplyOutcome =
                    when (eventClassifier.invoke(event)) {
                        Normal -> tryRunning(snapshot, event) { eventHandler.invoke(event, snapshot) }
                        Deprecates -> {
                            beforeReturn(callIdentifier, null)
                            throw IllegalStateException("Shouldn't have found Deprecates event here - $event")
                        }
                        DeprecatedBy -> {
                            val ret = deprecatedByProvider.invoke(event)
                            log.debug("$indent  ...The aggregate was deprecated by ${ret.aggregate}. Recursing to compute snapshot for it...")
                            val refEvent = eventsProvider.invoke(listOf(ret.aggregate), null, emptySnapshotProvider.invoke(ret.aggregate))
                                .collect(Collectors.toList())
                                .find { eventIdProvider.invoke(it) == ret.eventId }

                            val redirectVersion = eventVersioner.invoke(refEvent!!)
                            val otherSnapshot = snapshotProvider.invoke(ret.aggregate, redirectVersion) ?: emptySnapshotProvider.invoke(ret.aggregate)
                            val newEvents = eventsProvider.invoke(listOf(ret.aggregate) + aggregates, redirectVersion, otherSnapshot)
                                .collect(Collectors.toList())
                            @Suppress("LiftReturnOrAssignment")
                            if (redirect) {
                                return computeSnapshotImpl(newEvents, otherSnapshot, aggregates + listOf(ret.aggregate), at, redirect) { c, s ->
                                    beforeReturn(c, s)
                                    IndentedLogging.stepOut()
                                }
                            } else {
                                return GroovesResult.Redirect(ret.aggregate, redirectVersion)
                            }
                        }
                        Revert -> {
                            beforeReturn(callIdentifier, null)
                            throw IllegalStateException("Shouldn't have found Revert event here - $event")
                        }
                    }

                val versionOrTimestamp = eventVersioner.invoke(event)
                snapshotVersioner.invoke(snapshot, versionOrTimestamp)

                if (outcome == RETURN) {
                    log.debug("$indent  ...Event apply outcome was RETURN. Returning snapshot...")
                    beforeReturn(callIdentifier, snapshot)
                    return GroovesResult.Success(snapshot)
                }
            }
        }

        val versionOrTimestamp = eventVersioner.invoke(events.last())
        snapshotVersioner.invoke(snapshot, versionOrTimestamp)

        beforeReturn(callIdentifier, snapshot)
        return GroovesResult.Success(snapshot)
    }

    private fun revertsExistOutsideEvents(revertEvents: MutableList<Event>, indent: String, forwardEvents: MutableList<Event>): Boolean {
        while (revertEvents.isNotEmpty()) {
            val mostRecentRevert = revertEvents.removeLast()
            val revertedEvent = revertedEventProvider.invoke(mostRecentRevert)

            if (revertEvents.remove(revertedEvent)) {
                log.debug("$indent  ...Reverting $revertedEvent based on $mostRecentRevert")
            } else {
                if (forwardEvents.remove(revertedEvent)) {
                    log.debug("$indent  ...Reverting $revertedEvent based on $mostRecentRevert")
                } else {
                    val problem = "There is an event that needs to be reverted but part of the last snapshot - $revertedEvent"
                    log.debug("$indent  ...$problem. Recursing with older snapshot...")
                    return true
                }
            }
        }
        return false
    }

    private inline fun tryRunning(snapshot: Snapshot, it: Event, code: () -> EventApplyOutcome) =
        try {
            code()
        } catch (e: Exception) {
            exceptionHandler.invoke(e, snapshot, it)
        }
}