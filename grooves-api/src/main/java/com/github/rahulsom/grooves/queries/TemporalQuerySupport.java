package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import com.github.rahulsom.grooves.queries.internal.Pair;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;
import com.github.rahulsom.grooves.queries.internal.Utils;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.github.rahulsom.grooves.queries.internal.Utils.*;
import static io.reactivex.Flowable.empty;
import static io.reactivex.Flowable.fromPublisher;

/**
 * Default interface to help in building temporal snapshots.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the EventT's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the SnapshotT's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface TemporalQuerySupport<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        >
        extends
        BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        TemporalQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    /**
     * Finds the last usable snapshot. For a given maxTimestamp, finds a snapshot whose last event
     * is older than timestamp so a new one can be incrementally computed if possible.
     *
     * @param aggregate    The aggregate for which the latest snapshot is desired
     * @param maxTimestamp The max last event timestamp allowed for the snapshot
     *
     * @return A Publisher that returns at most one snapshot
     */
    @NotNull default Publisher<SnapshotT> getLastUsableSnapshot(
            @NotNull final AggregateT aggregate, @NotNull Date maxTimestamp) {
        return fromPublisher(getSnapshot(maxTimestamp, aggregate))
                .defaultIfEmpty(createEmptySnapshot())
                .doOnNext(it -> {
                    LoggerFactory.getLogger(getClass()).debug("  -> Last Usable Snapshot: {}",
                            it.getLastEventTimestamp() == null ? "<none>" : it.toString());
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
    @NotNull default Publisher<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            @NotNull AggregateT aggregate, @NotNull Date moment) {
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
    @NotNull default Publisher<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            @NotNull AggregateT aggregate, @NotNull Date moment, boolean reuseEarlierSnapshot) {
        return Utils.getSnapshotsWithReuse(
                reuseEarlierSnapshot,
                () -> fromPublisher(getLastUsableSnapshot(aggregate, moment)),
                lastSnapshot -> getUncomputedEvents(aggregate, lastSnapshot, moment),
                () -> fromPublisher(getSnapshotAndEventsSince(aggregate, moment, false)),
                this::createEmptySnapshot
        );
    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     *
     * @return A Publisher that returns at most one Snapshot
     */
    @NotNull default Publisher<SnapshotT> computeSnapshot(
            @NotNull AggregateT aggregate, @NotNull Date moment) {
        return computeSnapshot(aggregate, moment, true);
    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's
     *                  snapshot. Defaults to true.
     *
     * @return An Optional SnapshotType. Empty if it cannot be computed.
     */
    @NotNull default Publisher<SnapshotT> computeSnapshot(
            @NotNull AggregateT aggregate, @NotNull Date moment, boolean redirect) {
        if (LoggerFactory.getLogger(getClass()).isInfoEnabled()) {
            LoggerFactory.getLogger(getClass()).info("Computing snapshot for {} at {}", aggregate,
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(moment));
        }

        return fromPublisher(getSnapshotAndEventsSince(aggregate, moment)).flatMap(seTuple2 -> {
            List<EventT> events = seTuple2.second();
            SnapshotT snapshot = seTuple2.first();

            LoggerFactory.getLogger(getClass())
                    .info("     Events including redirects: {}", Utils.stringify(events));

            if (events.stream().anyMatch(it -> it instanceof RevertEvent)) {
                return fromPublisher(snapshot.getAggregateObservable())
                        .flatMap(aggregate1 ->
                                aggregate1 == null ?
                                        computeSnapshotAndEvents(
                                                aggregate, moment, redirect, events, snapshot) :
                                        empty())
                        .map(Flowable::just)
                        .defaultIfEmpty(fromPublisher(computeSnapshotAndEvents(
                                aggregate, moment, redirect, events, snapshot)))
                        .flatMap(it -> it);
            } else {
                return computeSnapshotAndEvents(aggregate, moment, redirect, events, snapshot);
            }

        });

    }

    /**
     * Computes snapshot and events based on the last usable snapshot.
     *
     * @param aggregate          The aggregate on which we are working
     * @param moment             The moment for which we desire a snapshot
     * @param redirect           Whether a redirect should be performed if the aggregate has been
     *                           deprecated by another aggregate
     * @param events             The list of events
     * @param lastUsableSnapshot The last known usable snapshot
     *
     * @return An observable of the snapshot
     */
    @NotNull default Publisher<SnapshotT> computeSnapshotAndEvents(
            @NotNull AggregateT aggregate,
            @NotNull Date moment,
            boolean redirect,
            @NotNull List<EventT> events,
            @NotNull SnapshotT lastUsableSnapshot) {
        lastUsableSnapshot.setAggregate(aggregate);

        Flowable<EventT> forwardOnlyEvents = getForwardOnlyEvents(events, getExecutor(),
                () -> fromPublisher(getSnapshotAndEventsSince(aggregate, moment, false)));

        Flowable<EventT> applicableEvents =
                getApplicableEvents(forwardOnlyEvents, getExecutor(), () ->
                        fromPublisher(getSnapshotAndEventsSince(aggregate, moment, false))
                );

        final Flowable<SnapshotT> snapshotTypeObservable =
                getExecutor().applyEvents(this, lastUsableSnapshot, applicableEvents,
                        new ArrayList<>(), aggregate);
        return snapshotTypeObservable
                .doOnNext(snapshot -> {
                    if (!events.isEmpty()) {
                        Utils.setLastEvent(snapshot, events.get(events.size() - 1));
                    }
                    LoggerFactory.getLogger(getClass()).info("  --> Computed: {}", snapshot);
                })
                .flatMap(it -> returnOrRedirect(redirect, events, it,
                        () -> fromPublisher(it.getDeprecatedByObservable())
                                .flatMap(x -> fromPublisher(computeSnapshot(x, moment)))
                ));
    }

    @NotNull
    @Override
    default QueryExecutor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, ?> getExecutor() {
        return new QueryExecutor<>();
    }

    @NotNull
    @Override
    default Publisher<EventT> findEventsBefore(@NotNull EventT event) {
        return fromPublisher(event.getAggregateObservable())
                .flatMap(aggregate ->
                        fromPublisher(getUncomputedEvents(aggregate, null, event.getTimestamp())));
    }

    /**
     * Returns events that are not part of lastSnapshot, but are needed for computing the snapshot
     * until snapshotTime.
     *
     * @param aggregate    The aggregate
     * @param lastSnapshot The last available snapshot
     * @param snapshotTime The time for which we need a snapshot
     * @return A list of events
     */
    @NotNull Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, @Nullable SnapshotT lastSnapshot,
            @NotNull Date snapshotTime);

    /**
     * Gets the last snapshot before given timestamp. Is responsible for discarding attached entity.
     *
     * @param maxTimestamp The maximum timestamp of the snapshot
     * @param aggregate    The aggregate for which a snapshot is required
     *
     * @return An observable that returns at most one Snapshot
     */
    @NotNull Publisher<SnapshotT> getSnapshot(
            @Nullable Date maxTimestamp, @NotNull AggregateT aggregate);

}
