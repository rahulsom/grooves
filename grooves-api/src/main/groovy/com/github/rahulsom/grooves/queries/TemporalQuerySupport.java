package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.GroovesException;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.Deprecates;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.queries.internal.BaseQuery;
import com.github.rahulsom.grooves.queries.internal.Executor;
import com.github.rahulsom.grooves.queries.internal.Pair;
import com.github.rahulsom.grooves.queries.internal.QueryExecutor;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.reactivestreams.Publisher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.github.rahulsom.grooves.queries.internal.Utils.*;
import static io.reactivex.Flowable.empty;
import static java.util.stream.Collectors.toList;

/**
 * Default interface to help in building temporal snapshots.
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
public interface TemporalQuerySupport<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends TemporalSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT,
                EventT>,
        QueryT extends BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT>>
        extends
        BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT> {

    /**
     * Finds the last usable snapshot. For a given maxTimestamp, finds a snapshot whose last event
     * is older than timestamp so a new one can be incrementally computed if possible.
     *
     * @param aggregate    The aggregate for which the latest snapshot is desired
     * @param maxTimestamp The max last event timestamp allowed for the snapshot
     *
     * @return A Publisher that returns at most one snapshot
     */
    default Flowable<SnapshotT> getLastUsableSnapshot(
            final AggregateT aggregate, Date maxTimestamp) {
        return flowable(getSnapshot(maxTimestamp, aggregate))
                .defaultIfEmpty(createEmptySnapshot())
                .doOnNext(it -> getLog().debug("  -> Last Usable Snapshot: {}",
                        it.getLastEventTimestamp() == null ? "<none>" : it.toString()))
                .doOnNext(it -> it.setAggregate(aggregate));
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
    default Single<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
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
    default Single<Pair<SnapshotT, List<EventT>>> getSnapshotAndEventsSince(
            AggregateT aggregate, Date moment, boolean reuseEarlierSnapshot) {
        if (reuseEarlierSnapshot) {
            return getLastUsableSnapshot(aggregate, moment)
                    .flatMap(lastSnapshot ->
                            flowable(getUncomputedEvents(aggregate, lastSnapshot, moment))
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
                                                    aggregate, moment, false)
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
                    flowable(getUncomputedEvents(aggregate, lastSnapshot, moment))
                            .toList();

            return uncomputedEvents
                    .doOnSuccess(ue ->
                            getLog().debug("     Events since origin: {}", stringify(ue)))
                    .map(ue -> new Pair<>(lastSnapshot, ue));
        }


    }

    /**
     * Computes a snapshot for specified version of an aggregate.
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     *
     * @return A Publisher that returns at most one Snapshot
     */
    default Publisher<SnapshotT> computeSnapshot(AggregateT aggregate, Date moment) {
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
     * @return An Optional SnapshotType. Empty if cannot be computed.
     */
    default Publisher<SnapshotT> computeSnapshot(
            AggregateT aggregate, Date moment, boolean redirect) {
        if (getLog().isInfoEnabled()) {
            getLog().info("Computing snapshot for {} at {}", aggregate,
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(moment));
        }

        return getSnapshotAndEventsSince(aggregate, moment).toFlowable().flatMap(seTuple2 -> {
            List<EventT> events = seTuple2.getSecond();
            SnapshotT snapshot = seTuple2.getFirst();

            getLog().info("Events: {}", events);

            if (events.stream().anyMatch(it -> it instanceof RevertEvent)) {
                return flowable(snapshot
                        .getAggregateObservable()).flatMap(aggregate1 ->
                        aggregate1 == null ?
                                computeSnapshotAndEvents(
                                        aggregate, moment, redirect, events, snapshot)
                                        .toFlowable() :
                                empty())
                        .map(Flowable::just)
                        .defaultIfEmpty(computeSnapshotAndEvents(
                                aggregate, moment, redirect, events, snapshot).toFlowable())
                        .flatMap(it -> it);
            }

            return computeSnapshotAndEvents(aggregate, moment, redirect, events, snapshot)
                    .toFlowable();
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
     * @return A Publisher of the snapshot
     */
    default Single<SnapshotT> computeSnapshotAndEvents(
            AggregateT aggregate,
            Date moment,
            boolean redirect,
            List<EventT> events,
            SnapshotT lastUsableSnapshot) {
        lastUsableSnapshot.setAggregate(aggregate);

        Flowable<EventT> forwardOnlyEvents = getForwardOnlyEvents(events, getExecutor(),
                () -> getSnapshotAndEventsSince(aggregate, moment, false)).cache();

        Flowable<EventT> applicableEvents = forwardOnlyEvents
                .filter(e -> e instanceof Deprecates)
                .toList()
                .toFlowable()
                .flatMap(list -> list.isEmpty() ?
                        forwardOnlyEvents :
                        getSnapshotAndEventsSince(aggregate, moment, false)
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
                                                        single(computeSnapshot(aggregateT, moment))
                                                )
                                )
                        )
                );
    }

    default Executor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT
            > getExecutor() {
        return new QueryExecutor<>();
    }

    @Override
    default Publisher<EventT> findEventsBefore(EventT event) {
        return flowable(event.getAggregateObservable())
                .flatMap(it -> flowable(getUncomputedEvents(it, null, event.getTimestamp())));
    }

    Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime);
}
