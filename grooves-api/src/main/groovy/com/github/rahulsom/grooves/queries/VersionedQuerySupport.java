package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;
import groovy.lang.Tuple2;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default interface to help in building versioned snapshots.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 * @author Rahul Somasunderam
 */
public interface VersionedQuerySupport<
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        >
        extends
        BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    /**
     * Finds the last usable snapshot. For a given maxPosition, finds a snapshot that's older than
     * that version number so a new one can be incrementally computed if possible.
     *
     * @param aggregate   The aggregate for which a snapshot is to be computed
     * @param maxPosition The maximum allowed version of the snapshot that is deemed usable
     * @return An Observable that returns at most one snapshot
     */
    default Observable<SnapshotT> getLastUsableSnapshot(
            final AggregateT aggregate, long maxPosition) {
        return getSnapshot(maxPosition, aggregate)
                .defaultIfEmpty(createEmptySnapshot())
                .map(it -> {
                    final String snapshotAsString = it == null ? "<none>" :
                            it.getLastEventPosition() == null ? "<none>" :
                                    it.getLastEventPosition() == 0 ? "<none>" :
                                            it.toString();
                    getLog().debug("  -> Last Usable Snapshot: " + snapshotAsString);
                    detachSnapshot(it);

                    it.setAggregate(aggregate);
                    return it;
                });
    }

    /**
     * Given a last event, finds the latest snapshot older than that event, and events between the
     * snapshot and the desired version.
     *
     * @param aggregate The aggregate for which such data is desired
     * @param version   The version of the snapshot that is desired
     * @return A Tuple containing the snapshot and the events
     */
    default Tuple2<SnapshotT, List<EventT>> getSnapshotAndEventsSince(
            AggregateT aggregate, long version) {
        return getSnapshotAndEventsSince(aggregate, version, version);
    }

    /**
     * Given a last event, finds the latest snapshot older than that event, and events between the
     * snapshot and the desired version.
     *
     * @param aggregate           The aggregate for which such data is desired
     * @param maxSnapshotPosition The max allowed position of the last event of the snapshot
     * @param version             The version of the snapshot that is desired
     * @return A Tuple containing the snapshot and the events
     */
    default Tuple2<SnapshotT, List<EventT>> getSnapshotAndEventsSince(
            AggregateT aggregate, long maxSnapshotPosition, long version) {
        if (maxSnapshotPosition > 0) {
            SnapshotT lastSnapshot =
                    getLastUsableSnapshot(aggregate, maxSnapshotPosition).toBlocking().first();

            final List<EventT> uncomputedEvents =
                    getUncomputedEvents(aggregate, lastSnapshot, version)
                            .toList()
                            .toBlocking()
                            .first();
            final List<EventT> uncomputedReverts =
                    uncomputedEvents.stream()
                            .filter(it -> it instanceof RevertEvent)
                            .collect(Collectors.toList());

            if (uncomputedReverts.isEmpty()) {
                getLog().debug("     Events in pair: "
                        + uncomputedEvents.stream()
                        .map(it -> it.getId().toString())
                        .collect(Collectors.joining(", ")));
                return new Tuple2<>(lastSnapshot, uncomputedEvents);
            } else {
                getLog().info("     Uncomputed reverts exist: "
                        + uncomputedEvents.stream()
                        .map(it -> it.getId().toString())
                        .collect(Collectors.joining(", ", "[\n    ", "\n]"))
                );
                return getSnapshotAndEventsSince(aggregate, 0, version);
            }

        } else {
            SnapshotT lastSnapshot = createEmptySnapshot();

            final List<EventT> uncomputedEvents =
                    getUncomputedEvents(aggregate, lastSnapshot, version)
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

    default Executor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT
            > getExecutor() {
        return new QueryExecutor<>();
    }

    Observable<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version);

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's
     *                  snapshot. Defaults to true.
     * @return An Observable that returns at most one Snapshot
     */
    default Observable<SnapshotT> computeSnapshot(
            AggregateT aggregate, long version, boolean redirect) {

        getLog().info(String.format("Computing snapshot for %s version %s",
                String.valueOf(aggregate),
                version == Long.MAX_VALUE ? "<LATEST>" : String.valueOf(version)));
        Tuple2<SnapshotT, List<EventT>> seTuple2 =
                getSnapshotAndEventsSince(aggregate, version);
        List<EventT> events = seTuple2.getSecond();
        SnapshotT lastUsableSnapshot = seTuple2.getFirst();

        if (events.stream().anyMatch(it -> it instanceof RevertEvent)
                && lastUsableSnapshot.getAggregate() != null) {
            return Observable.empty();
        }
        lastUsableSnapshot.setAggregate(aggregate);

        Observable<EventT> forwardOnlyEvents =
                getExecutor().applyReverts(Observable.from(events))
                        .toList()
                        .onErrorReturn(throwable -> getExecutor()
                                .applyReverts(
                                        Observable.from(
                                                getSnapshotAndEventsSince(aggregate, 0, version)
                                                        .getSecond())
                                )
                                .toList()
                                .toBlocking()
                                .first()
                        )
                        .flatMap(Observable::from);

        final Observable<SnapshotT> snapshotObservable =
                getExecutor().applyEvents(this, lastUsableSnapshot, forwardOnlyEvents,
                        new ArrayList<>(), Collections.singletonList(aggregate));
        return snapshotObservable
                .doOnNext(snapshot -> {
                    if (!events.isEmpty()) {
                        snapshot.setLastEvent(events.get(events.size() - 1));
                    }

                    getLog().info("  --> Computed: " + String.valueOf(snapshot));
                })
                .flatMap(it -> {
                    EventT lastEvent = events.isEmpty() ? null : events.get(events.size() - 1);
                    return it.getDeprecatedBy() != null && lastEvent != null
                            && lastEvent instanceof DeprecatedBy
                            && redirect ? computeSnapshot(it.getDeprecatedBy(), version) :
                            Observable.just(it);
                });

    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     * @return An Observable that returns at most one Snapshot
     */
    default Observable<SnapshotT> computeSnapshot(AggregateT aggregate, long version) {
        return computeSnapshot(aggregate, version, true);
    }

}
