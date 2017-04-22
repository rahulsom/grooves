package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;
import groovy.lang.Tuple2;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default interface to help in building temporal snapshots.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
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
     * @return An Observable that returns at most one snapshot
     */
    default Observable<SnapshotT> getLastUsableSnapshot(
            final AggregateT aggregate, Date maxTimestamp) {
        return getSnapshot(maxTimestamp, aggregate)
                .defaultIfEmpty(createEmptySnapshot())
                .map(it -> {
                    final String snapshotAsString = it == null ? "<none>" :
                            it.getLastEventTimestamp() == null ? "<none>" :
                                    it.toString();
                    getLog().debug("  -> Last Usable Snapshot: " + snapshotAsString);
                    detachSnapshot(it);

                    it.setAggregate(aggregate);
                    return it;
                });
    }

    /**
     * Given a timestamp, finds the latest snapshot older than that timestamp, and events between
     * the snapshot and the desired timestamp.
     *
     * @param aggregate The aggregate for which such data is desired
     * @param moment    The maximum timestamp of the last event
     * @return A Tuple containing the snapshot and the events
     */
    default Tuple2<SnapshotT, List<EventT>> getSnapshotAndEventsSince(
            AggregateT aggregate, Date moment) {
        return getSnapshotAndEventsSince(aggregate, moment, moment);
    }

    /**
     * Given a timestamp, finds the latest snapshot older than that timestamp, and events between
     * the snapshot and the desired timestamp.
     *
     * @param aggregate             The aggregate for which such data is desired
     * @param maxLastEventTimestamp The maximum allowed timestamp allowed in the snapshot
     * @param moment                The moment for the desired snapshot
     * @return A Tuple containing the snapshot and the events
     */
    default Tuple2<SnapshotT, List<EventT>> getSnapshotAndEventsSince(
            AggregateT aggregate, Date maxLastEventTimestamp, Date moment) {
        if (maxLastEventTimestamp != null) {
            SnapshotT lastSnapshot = getLastUsableSnapshot(aggregate, maxLastEventTimestamp)
                    .toBlocking()
                    .first();

            List<EventT> uncomputedEvents =
                    getUncomputedEvents(aggregate, lastSnapshot, moment)
                            .toList()
                            .toBlocking()
                            .first();

            final List<EventT> uncomputedReverts =
                    uncomputedEvents
                            .stream()
                            .filter(it -> it instanceof RevertEvent)
                            .collect(Collectors.toList());

            if (!uncomputedReverts.isEmpty()) {
                getLog().info("     Uncomputed reverts exist: [\n    "
                        + uncomputedEvents.stream()
                        .map(it -> it.getId().toString())
                        .collect(Collectors.joining(", "))
                        + "\n]");
                return getSnapshotAndEventsSince(aggregate, null, moment);
            } else {
                getLog().debug("     Events in pair: "
                        + uncomputedEvents.stream()
                        .map(it -> it.getId().toString())
                        .collect(Collectors.joining(", ")));
                return new Tuple2<>(lastSnapshot, uncomputedEvents);
            }

        } else {
            SnapshotT lastSnapshot = createEmptySnapshot();

            List<EventT> uncomputedEvents =
                    getUncomputedEvents(aggregate, lastSnapshot, moment)
                            .toList()
                            .toBlocking()
                            .first();

            getLog().debug("     Events in pair: "
                    + uncomputedEvents.stream()
                    .map(it -> it.getId().toString())
                    .collect(Collectors.joining(", ")));
            return new Tuple2<>(lastSnapshot, uncomputedEvents);
        }


    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's
     *                  snapshot. Defaults to true.
     * @return An Optional SnapshotType. Empty if cannot be computed.
     */
    default Observable<SnapshotT> computeSnapshot(
            AggregateT aggregate, Date moment, boolean redirect) {
        getLog().info(String.format("Computing snapshot for %s at %s", String.valueOf(aggregate),
                String.valueOf(moment)));
        Tuple2<SnapshotT, List<EventT>> seTuple2 =
                getSnapshotAndEventsSince(aggregate, moment);
        List<EventT> events = seTuple2.getSecond();
        SnapshotT snapshot = seTuple2.getFirst();

        if (events.stream().anyMatch(it -> it instanceof RevertEvent)
                && snapshot.getAggregate() != null) {
            return Observable.empty();
        }

        snapshot.setAggregate(aggregate);

        Observable<EventT> forwardOnlyEvents =
                getExecutor().applyReverts(Observable.from(events))
                        .toList()
                        .onErrorReturn(throwable -> getExecutor()
                                .applyReverts(
                                        Observable.from(
                                                getSnapshotAndEventsSince(aggregate, null, moment)
                                                        .getSecond())
                                )
                                .toList()
                                .toBlocking()
                                .first())
                        .flatMap(Observable::from);

        final Observable<SnapshotT> snapshotTypeObservable =
                getExecutor().applyEvents(this, snapshot, forwardOnlyEvents, new ArrayList<>(),
                        Collections.singletonList(aggregate));
        return snapshotTypeObservable
                .doOnNext(snapshotType -> {
                    if (!events.isEmpty()) {
                        snapshotType.setLastEvent(events.get(events.size() - 1));
                    }
                    getLog().info("  --> Computed: " + String.valueOf(snapshotType));
                })
                .flatMap(it -> {
                    EventT lastEvent = events.isEmpty() ? null : events.get(events.size() - 1);
                    return it.getDeprecatedBy() != null && lastEvent != null
                            && lastEvent instanceof DeprecatedBy && redirect
                            ? computeSnapshot(it.getDeprecatedBy(), moment) :
                            Observable.just(it);
                });
    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     * @return An Optional SnapshotType. Empty if cannot be computed.
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
