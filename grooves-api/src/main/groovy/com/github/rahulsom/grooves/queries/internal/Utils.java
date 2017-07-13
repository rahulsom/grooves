package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import rx.Observable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static rx.Observable.just;

/**
 * Utility objects and methods to help with Queries.
 *
 * @author Rahul Somasunderam
 */
public class Utils {

    private static final Collector<CharSequence, ?, String> JOIN_EVENT_IDS =
            Collectors.joining(", ");

    private static final Collector<CharSequence, ?, String> JOIN_EVENTS =
            Collectors.joining(",\n    ", "[\n    ", "\n]");

    private Utils() {
    }

    /**
     * Returns a snapshot or redirects to its deprecator.
     *
     * @param redirect           Whether a redirect is desirable
     * @param events             The sequence of events
     * @param it                 The snapshot
     * @param redirectedSnapshot A computation for the redirected snapshot
     * @param <SnapshotT>        The type of the snapshot
     * @param <EventT>           The type of the event
     *
     * @return An observable of a snapshot.
     */
    public static <
            SnapshotT extends BaseSnapshot,
            EventT extends BaseEvent
            > Observable<SnapshotT> returnOrRedirect(
            boolean redirect, List<EventT> events, SnapshotT it,
            Supplier<Observable<SnapshotT>> redirectedSnapshot) {
        final EventT lastEvent =
                events.isEmpty() ? null : events.get(events.size() - 1);

        final boolean redirectToDeprecator =
                lastEvent != null
                        && lastEvent instanceof DeprecatedBy
                        && redirect;

        return it.getDeprecatedByObservable()
                .flatMap(deprecatedBy -> redirectToDeprecator ? redirectedSnapshot.get() : just(it))
                .defaultIfEmpty(it);

    }

    /**
     * Computes forward only events. This could mean cancelling out events with their reverts
     * within a list, or sometimes, invoking the supplier of fallback events to get events.
     *
     * @param events                    The sequence of events
     * @param executor                  The executor to use for processing events
     * @param fallbackSnapshotAndEvents The fallback supplier
     * @param <AggregateIdT>            The type of {@link AggregateT}'s id
     * @param <AggregateT>              The type of Aggregate
     * @param <EventIdT>                The type of {@link EventT}'s id
     * @param <EventT>                  The type of Event
     * @param <SnapshotIdT>             The type of {@link SnapshotT}'s id
     * @param <SnapshotT>               The type of Snapshot
     * @param <QueryT>                  The type of Query
     *
     * @return an observable of forward only events
     */
    public static <
            AggregateIdT,
            AggregateT extends AggregateType<AggregateIdT>,
            EventIdT,
            EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends BaseSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>,
            QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
                    SnapshotT, QueryT>
            > Observable<EventT> getForwardOnlyEvents(
            List<EventT> events,
            Executor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                    QueryT> executor,
            Supplier<Observable<Pair<SnapshotT, List<EventT>>>> fallbackSnapshotAndEvents) {
        return executor.applyReverts(Observable.from(events))
                .toList()
                .map(Observable::just)
                .onErrorReturn(throwable -> executor
                        .applyReverts(
                                fallbackSnapshotAndEvents.get()
                                        .flatMap(it -> Observable.from(it.getSecond()))
                        )
                        .toList()
                )
                .flatMap(it -> it)
                .flatMap(Observable::from);
    }

    /**
     * Turns a list of events into a readable log style string.
     *
     * @param events   The list of events
     * @param <EventT> The type of events
     *
     * @return A String representation of events
     */
    public static <EventT extends BaseEvent> String stringifyEvents(List<EventT> events) {
        return events.stream()
                .map(EventT::toString)
                .collect(Utils.JOIN_EVENTS);
    }

    /**
     * Turns a list of events into a readable list of ids.
     *
     * @param events   The list of events
     * @param <EventT> The type of events
     *
     * @return A String representation of events
     */
    static <EventT extends BaseEvent> String stringifyEventIds(List<EventT> events) {
        return events.stream()
                .map(i -> i.getId().toString())
                .collect(Utils.JOIN_EVENT_IDS);
    }

}
