package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.GroovesException;
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
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

import static com.github.rahulsom.grooves.queries.internal.Utils.returnOrRedirect;
import static com.github.rahulsom.grooves.queries.internal.Utils.stringify;
import static io.reactivex.Flowable.*;
import static java.util.stream.Collectors.toList;

/**
 * Default interface to help in building versioned snapshots.
 *
 * @param <AggregateIdT> The type of {@link AggregateT}'s id
 * @param <AggregateT>   The aggregate over which the query executes
 * @param <EventIdT>     The type of the {@link EventT}'s id field
 * @param <EventT>       The type of the Event
 * @param <SnapshotIdT>  The type of the {@link SnapshotT}'s id field
 * @param <SnapshotT>    The type of the Snapshot
 * @param <QueryT>       A reference to the query type. Typically a self reference.
 *
 * @author Rahul Somasunderam
 */
public interface VersionedQuerySupport<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends VersionedSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT,
                EventT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>
        >
        extends
        BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        VersionedQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

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
                    getLog().debug("  -> Last Usable Snapshot: {}", snapshotAsString);
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
        if (reuseEarlierSnapshot) {
            return getLastUsableSnapshot(aggregate, version).flatMap(lastSnapshot ->
                    fromPublisher(getUncomputedEvents(aggregate, lastSnapshot, version))
                            .toList()
                            .toFlowable()
                            .flatMap(events -> {
                                if (events.stream().anyMatch(it -> it instanceof RevertEvent)) {
                                    List<EventT> reverts = events.stream()
                                            .filter(it -> it instanceof RevertEvent)
                                            .collect(toList());
                                    getLog().info("     Uncomputed reverts exist: {}",
                                            stringify(reverts));
                                    return getSnapshotAndEventsSince(
                                            aggregate, version, false);
                                } else {
                                    getLog().debug("     Events since last snapshot: {}",
                                            stringify(events));
                                    return just(new Pair<>(lastSnapshot, events));

                                }
                            }));

        } else {
            SnapshotT lastSnapshot = createEmptySnapshot();

            final Flowable<List<EventT>> uncomputedEvents =
                    fromPublisher(getUncomputedEvents(aggregate, lastSnapshot, version))
                            .toList()
                            .toFlowable();

            return uncomputedEvents
                    .doOnNext(ue -> getLog().debug("     Events since origin: {}",
                            stringify(ue)))
                    .map(ue -> new Pair<>(lastSnapshot, ue));
        }

    }

    @NotNull
    default QueryExecutor getExecutor() {
        return new QueryExecutor<>();
    }

    Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, long version);

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param version   The version number, starting at 1
     *
     * @return An Flowable that returns at most one Snapshot
     */
    default Publisher<SnapshotT> computeSnapshot(AggregateT aggregate, long version) {
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
    default Publisher<SnapshotT> computeSnapshot(
            AggregateT aggregate, long version, boolean redirect) {

        getLog().info("Computing snapshot for {} version {}",
                aggregate, version == Long.MAX_VALUE ? "<LATEST>" : version);

        return (getSnapshotAndEventsSince(aggregate, version).flatMap(seTuple2 -> {
            List<EventT> events = seTuple2.getSecond();
            SnapshotT lastUsableSnapshot = seTuple2.getFirst();

            getLog().info("     Events including redirects: {}", Utils.stringify(events));

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

        Flowable<EventT> forwardOnlyEvents = Utils.getForwardOnlyEvents(
                events, getExecutor(), () -> getSnapshotAndEventsSince(aggregate, version, false)
        );

        Flowable<EventT> applicableEvents = forwardOnlyEvents
                .filter(e -> e instanceof Deprecates)
                .toList()
                .toFlowable()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return forwardOnlyEvents;
                    } else {
                        Flowable<Pair<SnapshotT, List<EventT>>> snapshotAndEventsSince =
                                getSnapshotAndEventsSince(aggregate, version, false);
                        return snapshotAndEventsSince.flatMap(p ->
                                Utils.getForwardOnlyEvents(
                                        p.getSecond(),
                                        getExecutor(),
                                        () -> error(new GroovesException(
                                                "Couldn't apply deprecates events"))
                                ));
                    }
                });

        final Flowable<SnapshotT> snapshotObservable =
                getExecutor().applyEvents((QueryT) this, lastUsableSnapshot, applicableEvents,
                        new ArrayList<>(), aggregate);

        EventT lastEvent = events.isEmpty() ? null : events.get(events.size() - 1);

        Function<AggregateT, Publisher<SnapshotT>> deprecatorToSnapshot =
                x -> {
                    DeprecatedBy deprecatedBy = (DeprecatedBy) lastEvent;
                    return fromPublisher(
                            fromPublisher(
                                    (Publisher<Deprecates>) deprecatedBy.getConverseObservable())
                                    .flatMap(deprecates ->
                                            computeSnapshot(x, deprecates.getPosition()))
                    );
                };

        return snapshotObservable
                .doOnNext(snapshot -> {
                    if (!events.isEmpty()) {
                        Utils.setLastEvent(snapshot, lastEvent);
                    }

                    getLog().info("  --> Computed: {}", snapshot);
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
