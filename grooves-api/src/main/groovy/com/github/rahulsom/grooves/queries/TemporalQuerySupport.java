package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.queries.internal.*;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.rahulsom.grooves.queries.internal.Utils.stringifyEvents;

/**
 * Default interface to help in building temporal snapshots.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface TemporalQuerySupport<
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        >
        extends
        BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    /**
     * Finds the last usable snapshot. For a given maxTimestamp, finds a snapshot whose last event
     * is older than timestamp so a new one can be incrementally computed if possible.
     *
     * @param aggregate    The aggregate for which the latest snapshot is desired
     * @param maxTimestamp The max last event timestamp allowed for the snapshot
     *
     * @return An Observable that returns at most one snapshot
     */
    default Observable<SnapshotT> getLastUsableSnapshot(
            final AggregateT aggregate, Date maxTimestamp) {
        return getSnapshot(maxTimestamp, aggregate)
                .defaultIfEmpty(createEmptySnapshot())
                .doOnNext(it -> {
                    final String snapshotAsString =
                            it.getLastEventTimestamp() == null ? "<none>" :
                                    it.toString();
                    getLog().debug("  -> Last Usable Snapshot: " + snapshotAsString);
                    detachSnapshot(it);

                    it.setAggregate(aggregate);
                });
    }

    /**
     * Given a timestamp, finds the latest snapshot older than that timestamp, and events between
     * the snapshot and the desired timestamp.
     *
     * @param aggregate The aggregate for which such data is desired
     * @param moment    The maximum timestamp of the last event
     *
     * @return A Tuple containing the snapshot and the events
     */
    default Observable<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            AggregateT aggregate, Date moment) {
        return getSnapshotAndEventsSince(aggregate, moment, true);
    }

    /**
     * Given a timestamp, finds the latest snapshot older than that timestamp, and events between
     * the snapshot and the desired timestamp.
     *
     * @param aggregate            The aggregate for which such data is desired
     * @param moment               The moment for the desired snapshot
     * @param reuseEarlierSnapshot Whether earlier snapshots can be reused for this computation. It
     *                             is generally a good idea to set this to true unless there are
     *                             known reverts that demand this be set to false.
     *
     * @return A Tuple containing the snapshot and the events
     */
    default Observable<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            AggregateT aggregate, Date moment, boolean reuseEarlierSnapshot) {
        if (reuseEarlierSnapshot) {
            return getLastUsableSnapshot(aggregate, moment).flatMap(lastSnapshot -> {
                final Observable<EventT> uncomputedEvents =
                        getUncomputedEvents(aggregate, lastSnapshot, moment);

                return uncomputedEvents.toList()
                        .flatMap(events -> {
                            if (events.stream().anyMatch(it -> it instanceof RevertEvent)) {
                                List<EventT> reverts = events.stream()
                                        .filter(it -> it instanceof RevertEvent).collect(
                                                Collectors.toList());
                                getLog().info("     Uncomputed reverts exist: "
                                        + stringifyEvents(reverts));
                                return getSnapshotAndEventsSince(aggregate, moment, false);
                            } else {
                                getLog().debug("     Events since last snapshot: " + stringifyEvents(events));
                                return Observable.just(new Pair<>(lastSnapshot, events));

                            }
                        });

            });


        } else {
            SnapshotT lastSnapshot = createEmptySnapshot();

            final Observable<List<EventT>> uncomputedEvents =
                    getUncomputedEvents(aggregate, lastSnapshot, moment)
                            .toList();

            return uncomputedEvents
                    .doOnNext(ue ->
                            getLog().debug("     Events since origin: " + stringifyEvents(ue)))
                    .map(ue -> new Pair<>(lastSnapshot, ue));
        }


    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's
     *                  snapshot. Defaults to true.
     *
     * @return An Optional SnapshotType. Empty if cannot be computed.
     */
    default Observable<SnapshotT> computeSnapshot(
            AggregateT aggregate, Date moment, boolean redirect) {
        getLog().info("Computing snapshot for {} at {}", aggregate, moment);

        return getSnapshotAndEventsSince(aggregate, moment).flatMap(seTuple2 -> {
            List<EventT> events = seTuple2.getSecond();
            SnapshotT snapshot = seTuple2.getFirst();

            getLog().info("Events: " + events);

            if (events.stream().anyMatch(it -> it instanceof RevertEvent)) {
                return snapshot
                        .getAggregateObservable().flatMap(aggregate1 -> {
                            getLog().info("Aggregate1: " + aggregate1);
                            if (aggregate1 == null) {
                                return computeSnapshotAndEvents(
                                        aggregate, moment, redirect, events, snapshot);
                            } else {
                                return Observable.empty();
                            }
                        })
                        .map(Observable::just)
                        .defaultIfEmpty(computeSnapshotAndEvents(
                                aggregate, moment, redirect, events, snapshot))
                        .flatMap(it -> it);
            }

            return computeSnapshotAndEvents(aggregate, moment, redirect, events, snapshot);
        });

    }

    default Observable<SnapshotT> computeSnapshotAndEvents(
            AggregateT aggregate,
            Date moment,
            boolean redirect,
            List<EventT> events,
            SnapshotT snapshot) {
        snapshot.setAggregate(aggregate);

        Observable<EventT> forwardOnlyEvents = Utils.getForwardOnlyEvents(events, getExecutor(),
                () -> getSnapshotAndEventsSince(aggregate, moment, false));

        final Observable<SnapshotT> snapshotTypeObservable =
                getExecutor().applyEvents(this, snapshot, forwardOnlyEvents, new ArrayList<>(),
                        Collections.singletonList(aggregate), aggregate);
        return snapshotTypeObservable
                .doOnNext(snapshotType -> {
                    if (!events.isEmpty()) {
                        snapshotType.setLastEvent(events.get(events.size() - 1));
                    }
                    getLog().info("  --> Computed: " + snapshotType);
                })
                .flatMap(it -> Utils.returnOrRedirect(redirect, events, it,
                        () -> it.getDeprecatedByObservable()
                                .flatMap(x -> computeSnapshot(x, moment))
                ));
    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     *
     * @return An Observable that returns at most one Snapshot
     */
    default Observable<SnapshotT> computeSnapshot(AggregateT aggregate, Date moment) {
        return computeSnapshot(aggregate, moment, true);
    }

    default Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT
            > getExecutor() {
        return new QueryExecutor<>();
    }

    Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime);
}
