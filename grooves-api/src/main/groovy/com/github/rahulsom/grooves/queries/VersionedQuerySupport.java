package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.GroovesException;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.Pair;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

import static com.github.rahulsom.grooves.queries.internal.Utils.*;
import static io.reactivex.Flowable.empty;
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
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT>
        >
        extends
        BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT> {

    /**
     * Finds the last usable snapshot. For a given maxPosition, finds a snapshot that's older than
     * that version number so a new one can be incrementally computed if possible.
     *
     * @param aggregate   The aggregate for which a snapshot is to be computed
     * @param maxPosition The maximum allowed version of the snapshot that is deemed usable
     *
     * @return A Publisher that returns at most one snapshot
     */
    default Flowable<SnapshotT> getLastUsableSnapshot(
            final AggregateT aggregate, long maxPosition) {
        return flowable(getSnapshot(maxPosition, aggregate))
                .defaultIfEmpty(createEmptySnapshot())
                .doOnNext(it -> {
                    final String snapshotAsString =
                            it.getLastEventPosition() == null ? "<none>" :
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
    default Single<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
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
    default Single<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            AggregateT aggregate, long version, boolean reuseEarlierSnapshot) {
        if (reuseEarlierSnapshot) {
            return getLastUsableSnapshot(aggregate, version)
                    .flatMap(lastSnapshot ->
                            flowable(getUncomputedEvents(aggregate, lastSnapshot, version))
                                    .toList()
                                    .toFlowable()
                                    .flatMap(events -> {
                                        if (events.stream().anyMatch(
                                                it -> it instanceof RevertEvent)) {
                                            List<EventT> reverts = events.stream()
                                                    .filter(it -> it instanceof RevertEvent)
                                                    .collect(toList());

                                            getLog().info("     Uncomputed reverts exist: {}",
                                                    stringify(reverts));

                                            return getSnapshotAndEventsSince(
                                                    aggregate, version, false)
                                                    .toFlowable();

                                        } else {
                                            getLog().debug("     Events since last snapshot: {}",
                                                    stringify(events));

                                            return Flowable.just(new Pair<>(lastSnapshot, events));
                                        }
                                    }))
                    .firstOrError();

        } else {
            SnapshotT lastSnapshot = createEmptySnapshot();

            final Single<List<EventT>> uncomputedEvents =
                    flowable(getUncomputedEvents(aggregate, lastSnapshot, version))
                            .toList();

            return uncomputedEvents
                    .doOnSuccess(ue ->
                            getLog().debug("     Events since origin: {}", stringify(ue)))
                    .map(ue -> new Pair<>(lastSnapshot, ue));
        }

    }

    default Executor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT
            > getExecutor() {
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
     * @return A Publisher that returns at most one Snapshot
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
     * @return A Publisher that returns at most one Snapshot
     */
    default Publisher<SnapshotT> computeSnapshot(
            AggregateT aggregate, long version, boolean redirect) {

        getLog().info("Computing snapshot for {} version {}",
                aggregate, version == Long.MAX_VALUE ? "<LATEST>" : version);

        return getSnapshotAndEventsSince(aggregate, version).toFlowable().flatMap(seTuple2 -> {
            List<EventT> events = seTuple2.getSecond();
            SnapshotT snapshot = seTuple2.getFirst();

            getLog().info("Events: {}", events);

            if (events.stream().anyMatch(it -> it instanceof RevertEvent)) {
                return flowable(snapshot
                        .getAggregateObservable()).flatMap(aggregate1 ->
                        aggregate1 == null ?
                                computeSnapshotAndEvents(
                                        aggregate, version, redirect, events, snapshot)
                                        .toFlowable() :
                                empty())
                        .map(Flowable::just)
                        .defaultIfEmpty(computeSnapshotAndEvents(
                                aggregate, version, redirect, events, snapshot).toFlowable())
                        .flatMap(it -> it);
            }

            return computeSnapshotAndEvents(aggregate, version, redirect, events, snapshot)
                    .toFlowable();
        });

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
     * @return A Publisher of the snapshot
     */
    default Single<SnapshotT> computeSnapshotAndEvents(
            AggregateT aggregate, long version, boolean redirect, List<EventT> events,
            SnapshotT lastUsableSnapshot) {
        lastUsableSnapshot.setAggregate(aggregate);

        Flowable<EventT> forwardOnlyEvents = getForwardOnlyEvents(events, getExecutor(),
                () -> getSnapshotAndEventsSince(aggregate, version, false)).cache();

        Flowable<EventT> applicableEvents = forwardOnlyEvents
                .filter(e -> e instanceof Deprecates)
                .toList()
                .toFlowable()
                .flatMap(list -> list.isEmpty() ?
                        forwardOnlyEvents :
                        getSnapshotAndEventsSince(aggregate, version, false)
                                .toFlowable()
                                .flatMap(p -> getForwardOnlyEvents(
                                        p.getSecond(), getExecutor(), () ->
                                                Single.error(new GroovesException(
                                                        "Couldn't apply deprecates events")))));

        final Single<SnapshotT> snapshotTypeObservable =
                single(getExecutor().applyEvents((QueryT) this, lastUsableSnapshot,
                        applicableEvents, new ArrayList<>(), aggregate));

        return snapshotTypeObservable
                .doOnSuccess(snapshot -> {
                    if (!events.isEmpty()) {
                        snapshot.setLastEvent(events.get(events.size() - 1));
                    }
                    getLog().info("  --> Computed: {}", snapshot);
                })
                .flatMap(it ->
                        single(
                                returnOrRedirect(redirect, events, it, () ->
                                        single(it.getDeprecatedByObservable())
                                                .flatMap(aggregateT ->
                                                        single(computeSnapshot(aggregateT, version))
                                                )
                                )
                        )
                );
    }

    @Override
    default Publisher<EventT> findEventsBefore(EventT event) {
        return flowable(event.getAggregateObservable())
                .flatMap(it -> flowable(getUncomputedEvents(it, null, event.getPosition())));
    }

}
