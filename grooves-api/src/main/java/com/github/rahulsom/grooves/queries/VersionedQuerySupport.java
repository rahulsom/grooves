package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import com.github.rahulsom.grooves.queries.internal.Pair;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;
import com.github.rahulsom.grooves.queries.internal.Utils;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.github.rahulsom.grooves.queries.internal.Utils.*;
import static io.reactivex.Flowable.empty;
import static io.reactivex.Flowable.fromPublisher;

/**
 * Default interface to help in building versioned snapshots.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the EventT's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the SnapshotT's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface VersionedQuerySupport<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        >
        extends
        BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        VersionedQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    /**
     * Finds the last usable snapshot. For a given maxPosition, finds a snapshot that's older than
     * that version number so a new one can be incrementally computed if possible.
     *
     * @param aggregate   The aggregate for which a snapshot is to be computed
     * @param maxPosition The maximum allowed version of the snapshot that is deemed usable
     *
     * @return An Flowable that returns at most one snapshot
     */
    default Flowable<SnapshotT> getLastUsableSnapshot(
            final AggregateT aggregate, long maxPosition) {
        return fromPublisher(getSnapshot(maxPosition, aggregate))
                .defaultIfEmpty(createEmptySnapshot())
                .doOnNext(it -> {
                    final String snapshotAsString =
                            it.getLastEventPosition() == 0 ? "<none>" :
                                    it.getLastEventPosition() == 0 ? "<none>" :
                                            it.toString();
                    LoggerFactory.getLogger(getClass())
                        .debug("  -> Last Usable Snapshot: {}", snapshotAsString);
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
    default Flowable<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
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
    default Flowable<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            AggregateT aggregate, long version, boolean reuseEarlierSnapshot) {
        return Utils.getSnapshotsWithReuse(
                reuseEarlierSnapshot,
                () -> getLastUsableSnapshot(aggregate, version),
                lastSnapshot -> getUncomputedEvents(aggregate, lastSnapshot, version),
                () -> getSnapshotAndEventsSince(aggregate, version, false),
                this::createEmptySnapshot
        );

    }

    @NotNull
    default QueryExecutor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, ?> getExecutor() {
        return new QueryExecutor<>();
    }

    @NotNull Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, @Nullable SnapshotT lastSnapshot, long version);

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     *
     * @return An Flowable that returns at most one Snapshot
     */
    @NotNull
    default Publisher<SnapshotT> computeSnapshot(@NotNull AggregateT aggregate, long version) {
        return computeSnapshot(aggregate, version, true);
    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's
     *                  snapshot. Defaults to true.
     *
     * @return An Flowable that returns at most one Snapshot
     */
    @NotNull
    default Publisher<SnapshotT> computeSnapshot(
            @NotNull AggregateT aggregate, long version, boolean redirect) {

        LoggerFactory.getLogger(getClass()).info("Computing snapshot for {} version {}",
                aggregate, version == Long.MAX_VALUE ? "<LATEST>" : version);

        return (getSnapshotAndEventsSince(aggregate, version).flatMap(seTuple2 -> {
            List<EventT> events = seTuple2.getSecond();
            SnapshotT lastUsableSnapshot = seTuple2.getFirst();

            LoggerFactory.getLogger(getClass())
                .info("     Events including redirects: {}", Utils.stringify(events));

            if (events.stream().anyMatch(it -> it instanceof RevertEvent)) {
                return fromPublisher(lastUsableSnapshot.getAggregateObservable())
                        .flatMap(aggregate1 -> aggregate1 == null ?
                                computeSnapshotAndEvents(
                                        aggregate, version, redirect, events, lastUsableSnapshot) :
                                empty())
                        .map(Flowable::just)
                        .defaultIfEmpty(computeSnapshotAndEvents(
                                aggregate, version, redirect, events, lastUsableSnapshot))
                        .flatMap(it -> it);
            }
            return computeSnapshotAndEvents(
                    aggregate, version, redirect, events, lastUsableSnapshot);
        }));

    }

    /**
     * Computes snapshot and events based on the last usable snapshot.
     *
     * @param aggregate          The aggregate on which we are working
     * @param version            The version that we desire
     * @param redirect           Whether a redirect should be performed if the aggregate has been
     *                           deprecated by another aggregate
     * @param events             The list of events
     * @param lastUsableSnapshot The last known usable snapshot
     *
     * @return An observable of the snapshot
     */
    default Flowable<SnapshotT> computeSnapshotAndEvents(
            AggregateT aggregate, long version, boolean redirect, List<EventT> events,
            SnapshotT lastUsableSnapshot) {
        lastUsableSnapshot.setAggregate(aggregate);

        Flowable<EventT> forwardOnlyEvents = getForwardOnlyEvents(
                events, getExecutor(), () -> getSnapshotAndEventsSince(aggregate, version, false)
        );

        Flowable<EventT> applicableEvents =
                getApplicableEvents(forwardOnlyEvents, getExecutor(),
                        () -> getSnapshotAndEventsSince(aggregate, version, false)
                );

        final Flowable<SnapshotT> snapshotObservable =
                getExecutor().applyEvents(this, lastUsableSnapshot, applicableEvents,
                        new ArrayList<>(), aggregate);

        EventT lastEvent = events.isEmpty() ? null : events.get(events.size() - 1);

        Function<AggregateT, Publisher<SnapshotT>> deprecatorToSnapshot =
                x -> {
                    DeprecatedBy deprecatedBy = (DeprecatedBy) lastEvent;
                    Flowable<Deprecates> deprecatesFlowable =
                            deprecatedBy == null ? Flowable.empty() :
                                    fromPublisher((Publisher<Deprecates>) deprecatedBy
                                            .getConverseObservable());
                    return deprecatesFlowable
                            .flatMap(deprecates ->
                                    computeSnapshot(x, deprecates.getPosition()));
                };

        return snapshotObservable
                .doOnNext(snapshot -> {
                    if (!events.isEmpty()) {
                        Utils.setLastEvent(snapshot, lastEvent);
                    }

                    LoggerFactory.getLogger(getClass()).info("  --> Computed: {}", snapshot);
                })
                .flatMap(it -> returnOrRedirect(redirect, events, it,
                        () -> fromPublisher(it.getDeprecatedByObservable())
                                .flatMap(deprecatorToSnapshot)
                ));
    }

    @NotNull
    @Override
    default Publisher<EventT> findEventsBefore(@NotNull EventT event) {
        return fromPublisher(event.getAggregateObservable())
                .flatMap(aggregate ->
                        fromPublisher(getUncomputedEvents(aggregate, null, event.getPosition())));
    }

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity.
     *
     * @param maxPosition The position before which a snapshot is required
     * @param aggregate   The aggregate for which a snapshot is required
     *
     * @return An observable that returns at most one Snapshot
     */
    @NotNull Publisher<SnapshotT> getSnapshot(long maxPosition, @NotNull AggregateT aggregate);

}
