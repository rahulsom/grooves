package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.Pair;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;
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
 *
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
     *
     * @return An Observable that returns at most one snapshot
     */
    default Observable<SnapshotT> getLastUsableSnapshot(
            final AggregateT aggregate, long maxPosition) {
        return getSnapshot(maxPosition, aggregate)
                .defaultIfEmpty(createEmptySnapshot())
                .doOnNext(it -> {
                    final String snapshotAsString =
                            it.getLastEventPosition() == null ? "<none>" :
                                    it.getLastEventPosition() == 0 ? "<none>" :
                                            it.toString();
                    getLog().debug("  -> Last Usable Snapshot: " + snapshotAsString);
                    detachSnapshot(it);

                    it.setAggregate(aggregate);
                });
    }

    /**
     * Given a last event, finds the latest snapshot older than that event, and events between the
     * snapshot and the desired version.
     *
     * @param aggregate The aggregate for which such data is desired
     * @param version   The version of the snapshot that is desired
     *
     * @return A Tuple containing the snapshot and the events
     */
    default Observable<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            AggregateT aggregate, long version) {
        return getSnapshotAndEventsSince(aggregate, version, true);
    }

    /**
     * Given a last event, finds the latest snapshot older than that event, and events between the
     * snapshot and the desired version.
     *
     * @param aggregate            The aggregate for which such data is desired
     * @param version              The version of the snapshot that is desired
     * @param reuseEarlierSnapshot Whether earlier snapshots can be reused for this computation. It
     *                             is generally a good idea to set this to true unless there are
     *                             known reverts that demand this be set to false.
     *
     * @return A Tuple containing the snapshot and the events
     */
    default Observable<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            AggregateT aggregate, long version, boolean reuseEarlierSnapshot) {
        if (reuseEarlierSnapshot) {
            return getLastUsableSnapshot(aggregate, version).flatMap(lastSnapshot -> {
                final Observable<EventT> uncomputedEvents =
                        getUncomputedEvents(aggregate, lastSnapshot, version);

                final Observable<EventT> uncomputedReverts =
                        uncomputedEvents
                                .filter(it -> it instanceof RevertEvent);

                return uncomputedReverts.isEmpty().flatMap(eventsAreForwardOnly -> {
                    if (eventsAreForwardOnly) {
                        return uncomputedEvents
                                .toList()
                                .doOnNext(ue -> getLog().debug("     Events in pair: " + ue.stream()
                                        .map(it -> it.getId().toString())
                                        .collect(Collectors.joining(", "))))
                                .map(ue -> new Pair<>(lastSnapshot, ue));
                    } else {
                        return uncomputedReverts
                                .toList()
                                .doOnNext(eventList ->
                                    getLog().info("     Uncomputed reverts exist: "
                                            + eventList.stream()
                                            .map(EventT::toString)
                                            .collect(Collectors.joining(
                                                    ",\n    ", "[\n    ", "\n]"))
                                    )
                                )
                                .flatMap(ue ->
                                        getSnapshotAndEventsSince(aggregate, version, false));
                    }
                });
            });

        } else {
            SnapshotT lastSnapshot = createEmptySnapshot();

            final Observable<List<EventT>> uncomputedEvents =
                    getUncomputedEvents(aggregate, lastSnapshot, version)
                            .toList();

            return uncomputedEvents
                    .doOnNext(ue -> getLog().debug("     Events in pair: " + ue.stream()
                            .map(it -> it.getId().toString())
                            .collect(Collectors.joining(", "))))
                    .map(ue -> new Pair<>(lastSnapshot, ue));
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
     *
     * @return An Observable that returns at most one Snapshot
     */
    default Observable<SnapshotT> computeSnapshot(
            AggregateT aggregate, long version, boolean redirect) {

        getLog().info("Computing snapshot for {} version {}",
                aggregate, version == Long.MAX_VALUE ? "<LATEST>" : version);

        return getSnapshotAndEventsSince(aggregate, version).flatMap(seTuple2 -> {
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
                            .map(Observable::just)
                            .onErrorReturn(throwable -> getExecutor()
                                    .applyReverts(
                                            getSnapshotAndEventsSince(aggregate, version, false)
                                                    .flatMap(it -> Observable.from(it.getSecond()))
                                    )
                                    .toList()
                            )
                            .flatMap(it -> it)
                            .flatMap(Observable::from);

            final Observable<SnapshotT> snapshotObservable =
                    getExecutor().applyEvents(this, lastUsableSnapshot, forwardOnlyEvents,
                            new ArrayList<>(), Collections.singletonList(aggregate));
            return snapshotObservable
                    .doOnNext(snapshot -> {
                        if (!events.isEmpty()) {
                            snapshot.setLastEvent(events.get(events.size() - 1));
                        }

                        getLog().info("  --> Computed: " + snapshot);
                    })
                    .flatMap(it -> {
                        EventT lastEvent = events.isEmpty() ? null : events.get(events.size() - 1);
                        return it.getDeprecatedBy() != null
                                && lastEvent != null
                                && lastEvent instanceof DeprecatedBy
                                && redirect ?
                                computeSnapshot(it.getDeprecatedBy(), version) :
                                Observable.just(it);
                    });
        });

    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     *
     * @return An Observable that returns at most one Snapshot
     */
    default Observable<SnapshotT> computeSnapshot(AggregateT aggregate, long version) {
        return computeSnapshot(aggregate, version, true);
    }

}
