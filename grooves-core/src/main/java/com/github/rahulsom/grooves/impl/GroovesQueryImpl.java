package com.github.rahulsom.grooves.impl;

import com.github.rahulsom.grooves.EventApplyOutcome;
import com.github.rahulsom.grooves.EventType;
import com.github.rahulsom.grooves.GroovesQuery;
import com.github.rahulsom.grooves.GroovesResult;
import com.github.rahulsom.grooves.functions.ApplyMoreEventsPredicate;
import com.github.rahulsom.grooves.functions.DeprecatedByProvider;
import com.github.rahulsom.grooves.functions.Deprecator;
import com.github.rahulsom.grooves.functions.EmptySnapshotProvider;
import com.github.rahulsom.grooves.functions.EventClassifier;
import com.github.rahulsom.grooves.functions.EventHandler;
import com.github.rahulsom.grooves.functions.EventIdProvider;
import com.github.rahulsom.grooves.functions.EventVersioner;
import com.github.rahulsom.grooves.functions.EventsProvider;
import com.github.rahulsom.grooves.functions.ExceptionHandler;
import com.github.rahulsom.grooves.functions.RevertedEventProvider;
import com.github.rahulsom.grooves.functions.SnapshotProvider;
import com.github.rahulsom.grooves.functions.SnapshotVersioner;
import com.github.rahulsom.grooves.logging.IndentedLogging;
import com.github.rahulsom.grooves.logging.Trace;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the GroovesQuery interface that provides snapshot computation.
 * This implementation orchestrates functional components to build snapshots from event streams,
 * handling redirects, deprecations, reverts, and exceptions as needed.
 *
 * @param <VersionOrTimestampT> the type used for versioning (timestamp or version number)
 * @param <SnapshotT> the type of the computed snapshot
 * @param <AggregateT> the type of the aggregate being queried
 * @param <EventT> the type of events in the stream
 * @param <EventIdT> the type of event identifiers
 */
@RequiredArgsConstructor
public class GroovesQueryImpl<VersionOrTimestampT, SnapshotT, AggregateT, EventT, EventIdT>
        implements GroovesQuery<AggregateT, VersionOrTimestampT, SnapshotT, EventT, EventIdT> {

    private final SnapshotProvider<AggregateT, VersionOrTimestampT, SnapshotT> snapshotProvider;
    private final EmptySnapshotProvider<AggregateT, SnapshotT> emptySnapshotProvider;
    private final EventsProvider<AggregateT, VersionOrTimestampT, SnapshotT, EventT> eventsProvider;
    private final ApplyMoreEventsPredicate<SnapshotT> applyMoreEventsPredicate;
    private final EventClassifier<EventT> eventClassifier;
    private final Deprecator<SnapshotT, EventT> deprecator;
    private final ExceptionHandler<SnapshotT, EventT> exceptionHandler;
    private final EventHandler<EventT, SnapshotT> eventHandler;
    private final EventVersioner<EventT, VersionOrTimestampT> eventVersioner;
    private final SnapshotVersioner<SnapshotT, VersionOrTimestampT> snapshotVersioner;
    private final DeprecatedByProvider<EventT, AggregateT, EventIdT> deprecatedByProvider;
    private final RevertedEventProvider<EventT> revertedEventProvider;
    private final EventIdProvider<EventT, EventIdT> eventIdProvider;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @NotNull
    @Trace
    @Override
    public GroovesResult<SnapshotT, AggregateT, VersionOrTimestampT> computeSnapshot(
            AggregateT aggregate, VersionOrTimestampT at, boolean redirect) {
        var providedSnapshot = snapshotProvider.invoke(aggregate, at);
        var snapshot = providedSnapshot != null ? providedSnapshot : emptySnapshotProvider.invoke(aggregate);
        var events = eventsProvider.invoke(List.of(aggregate), at, snapshot).toList();

        return computeSnapshotImpl(events, snapshot, List.of(aggregate), at, redirect, (c, s) -> {
            if (s != null) {
                log.trace("{} -> {}", c.data, s);
            }
            IndentedLogging.stepOut();
        });
    }

    private record CallIdentifier(String data) {}

    private GroovesResult<SnapshotT, AggregateT, VersionOrTimestampT> computeSnapshotImpl(
            List<EventT> events,
            SnapshotT snapshot,
            List<AggregateT> aggregates,
            VersionOrTimestampT at,
            boolean redirect,
            BiConsumer<CallIdentifier, SnapshotT> beforeReturn) {
        var indent = IndentedLogging.indent();

        var callIdentifier = new CallIdentifier(indent + "computeSnapshotImpl(<... " + events.size() + " items>, "
                + snapshot + ", " + aggregates + ", " + at + ")");
        log.trace(callIdentifier.data);
        IndentedLogging.stepIn();

        var revertEvents = new ArrayList<EventT>();
        var forwardEvents = new ArrayList<EventT>();
        for (EventT event : events) {
            if (eventClassifier.invoke(event) == EventType.Revert) {
                revertEvents.add(event);
            } else {
                forwardEvents.add(event);
            }
        }

        if (revertsExistOutsideEvents(revertEvents, indent, forwardEvents)) {
            var snapshot1 = emptySnapshotProvider.invoke(aggregates.get(0));
            var events1 = eventsProvider.invoke(aggregates, at, snapshot1).toList();
            return computeSnapshotImpl(events1, snapshot1, aggregates, at, redirect, (c, s) -> {
                beforeReturn.accept(c, s);
                IndentedLogging.stepOut();
            });
        }

        var deprecatesEvents = forwardEvents.stream()
                .filter(event -> eventClassifier.invoke(event) == EventType.Deprecates)
                .collect(Collectors.toCollection(ArrayList::new));
        while (!deprecatesEvents.isEmpty()) {
            var event = deprecatesEvents.remove(0);
            var converseId = deprecatedByProvider.invoke(event).eventId();
            deprecator.invoke(snapshot, event);
            forwardEvents.remove(event);
            forwardEvents.removeIf(e -> Objects.equals(eventIdProvider.invoke(e), converseId));
        }

        for (EventT event : forwardEvents) {
            if (applyMoreEventsPredicate.invoke(snapshot)) {
                EventApplyOutcome outcome;
                switch (eventClassifier.invoke(event)) {
                    case Normal -> outcome = tryRunning(snapshot, event, () -> eventHandler.invoke(event, snapshot));
                    case Deprecates -> {
                        beforeReturn.accept(callIdentifier, null);
                        throw new IllegalStateException("Shouldn't have found Deprecates event here - " + event);
                    }
                    case DeprecatedBy -> {
                        var ret = deprecatedByProvider.invoke(event);
                        log.debug(
                                "{}  ...The aggregate was deprecated by {}. "
                                        + "Recursing to compute snapshot for it...",
                                indent,
                                ret.aggregate());
                        var refEvent = eventsProvider
                                .invoke(List.of(ret.aggregate()), null, emptySnapshotProvider.invoke(ret.aggregate()))
                                .toList()
                                .stream()
                                .filter(e -> Objects.equals(eventIdProvider.invoke(e), ret.eventId()))
                                .findFirst()
                                .orElseThrow();

                        var redirectVersion = eventVersioner.invoke(refEvent);
                        var otherSnapshot = snapshotProvider.invoke(ret.aggregate(), redirectVersion);
                        if (otherSnapshot == null) {
                            otherSnapshot = emptySnapshotProvider.invoke(ret.aggregate());
                        }
                        var newAggregates = new ArrayList<>(List.of(ret.aggregate()));
                        newAggregates.addAll(aggregates);
                        var newEvents = eventsProvider
                                .invoke(newAggregates, redirectVersion, otherSnapshot)
                                .toList();
                        if (redirect) {
                            var finalAggregates = new ArrayList<>(aggregates);
                            finalAggregates.add(ret.aggregate());
                            return computeSnapshotImpl(newEvents, otherSnapshot, finalAggregates, at, true, (c, s) -> {
                                beforeReturn.accept(c, s);
                                IndentedLogging.stepOut();
                            });
                        } else {
                            return new GroovesResult.Redirect<>(ret.aggregate(), redirectVersion);
                        }
                    }
                    case Revert -> {
                        beforeReturn.accept(callIdentifier, null);
                        throw new IllegalStateException("Shouldn't have found Revert event here - " + event);
                    }
                    default -> throw new IllegalStateException("Unknown event type: " + eventClassifier.invoke(event));
                }

                var versionOrTimestamp = eventVersioner.invoke(event);
                snapshotVersioner.invoke(snapshot, versionOrTimestamp);

                if (outcome == EventApplyOutcome.RETURN) {
                    log.debug("{}  ...Event apply outcome was RETURN. " + "Returning snapshot...", indent);
                    beforeReturn.accept(callIdentifier, snapshot);
                    return new GroovesResult.Success<>(snapshot);
                }
            }
        }

        var versionOrTimestamp = eventVersioner.invoke(events.get(events.size() - 1));
        snapshotVersioner.invoke(snapshot, versionOrTimestamp);

        beforeReturn.accept(callIdentifier, snapshot);
        return new GroovesResult.Success<>(snapshot);
    }

    private boolean revertsExistOutsideEvents(
            ArrayList<EventT> revertEvents, String indent, ArrayList<EventT> forwardEvents) {
        while (!revertEvents.isEmpty()) {
            var mostRecentRevert = revertEvents.remove(revertEvents.size() - 1);
            var revertedEvent = revertedEventProvider.invoke(mostRecentRevert);

            if (revertEvents.remove(revertedEvent)) {
                log.debug("{}  ...Reverting revertEvent {} based on {}", indent, revertedEvent, mostRecentRevert);
            } else {
                if (forwardEvents.remove(revertedEvent)) {
                    log.debug("{}  ...Reverting forwardEvent {} based on {}", indent, revertedEvent, mostRecentRevert);
                } else {
                    var problem = "There is an event that needs to be reverted but part of " + "the last snapshot - "
                            + revertedEvent;
                    log.debug("{}  ...{}. Recursing with older snapshot...", indent, problem);
                    return true;
                }
            }
        }
        return false;
    }

    private EventApplyOutcome tryRunning(SnapshotT snapshot, EventT event, Supplier<EventApplyOutcome> code) {
        try {
            return code.get();
        } catch (Exception e) {
            return exceptionHandler.invoke(e, snapshot, event);
        }
    }
}
