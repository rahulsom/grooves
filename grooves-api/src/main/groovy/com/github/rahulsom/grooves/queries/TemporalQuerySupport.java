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
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregate trait that simplifies computing temporal snapshots from events
 *
 * @param <Aggregate>      The aggregate over which the query executes
 * @param <EventIdType>    The type of the Event's id field
 * @param <EventType>      The type of the Event
 * @param <SnapshotIdType> The type of the Snapshot's id field
 * @param <SnapshotType>   The type of the Snapshot
 * @author Rahul Somasunderam
 */
public interface TemporalQuerySupport<
        Aggregate extends AggregateType,
        EventIdType,
        EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
        SnapshotIdType,
        SnapshotType extends TemporalSnapshot<Aggregate, SnapshotIdType, EventIdType, EventType>
        >
        extends
        BaseQuery<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> {

    /**
     * @param aggregate    The aggregate for which the latest snapshot is desired
     * @param maxTimestamp The max last event timestamp allowed for the snapshot
     * @return An Observable that returns at most one snapshot
     */
    default Observable<SnapshotType> getLastUsableSnapshot(final Aggregate aggregate, Date maxTimestamp) {
        return getSnapshot(maxTimestamp, aggregate).
                defaultIfEmpty(createEmptySnapshot()).
                map(it -> {
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
     * Given a last event, finds the latest snapshot older than that event
     *
     * @param aggregate
     * @param moment
     * @return
     */
    default Tuple2<SnapshotType, List<EventType>> getSnapshotAndEventsSince(Aggregate aggregate, Date moment) {
        return getSnapshotAndEventsSince(aggregate, moment, moment);
    }

    default Tuple2<SnapshotType, List<EventType>> getSnapshotAndEventsSince(Aggregate aggregate, Date maxLastEventTimestamp, Date snapshotTime) {
        if (DefaultGroovyMethods.asBoolean(maxLastEventTimestamp)) {
            SnapshotType lastSnapshot = getLastUsableSnapshot(aggregate, maxLastEventTimestamp).toBlocking().first();

            List<EventType> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime).toList().toBlocking().first();
            final List<EventType> uncomputedReverts = uncomputedEvents.stream().filter(it -> it instanceof RevertEvent).collect(Collectors.toList());

            if (DefaultGroovyMethods.asBoolean(uncomputedReverts)) {
                getLog().info("     Uncomputed reverts exist: [\n    " + uncomputedEvents.stream().map(it -> it.getId().toString()).collect(Collectors.joining(", ")) + "\n]");
                return getSnapshotAndEventsSince(aggregate, null, snapshotTime);
            } else {
                getLog().debug("     Events in pair: " + uncomputedEvents.stream().map(it -> it.getId().toString()).collect(Collectors.joining(", ")));
                return new Tuple2<>(lastSnapshot, uncomputedEvents);
            }

        } else {
            SnapshotType lastSnapshot = createEmptySnapshot();

            List<EventType> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, snapshotTime).toList().toBlocking().first();

            getLog().debug("     Events in pair: " + uncomputedEvents.stream().map(it -> it.getId().toString()).collect(Collectors.joining(", ")));
            return new Tuple2<>(lastSnapshot, uncomputedEvents);
        }


    }

    /**
     * Computes a snapshot for specified version of an aggregate
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     * @param redirect  If there has been a deprecation, redirect to the current aggregate's snapshot. Defaults to true.
     * @return An Optional SnapshotType. Empty if cannot be computed.
     */
    default Observable<SnapshotType> computeSnapshot(Aggregate aggregate, Date moment, boolean redirect) {
        getLog().info("Computing snapshot for " + String.valueOf(aggregate) + " at " + String.valueOf(moment));
        Tuple2<SnapshotType, List<EventType>> seTuple2 = getSnapshotAndEventsSince(aggregate, moment);
        List<EventType> events = seTuple2.getSecond();
        SnapshotType snapshot = seTuple2.getFirst();

        if (events.stream().anyMatch(it -> it instanceof RevertEvent) && snapshot.getAggregate() != null) {
            return Observable.empty();
        }

        snapshot.setAggregate(aggregate);

        Observable<EventType> forwardOnlyEvents = getExecutor().applyReverts(Observable.from(events)).
                toList().
                onErrorReturn(throwable -> getExecutor().
                        applyReverts(
                                Observable.
                                        from(getSnapshotAndEventsSince(aggregate, null, moment).
                                                getSecond())
                        ).
                        toList().
                        toBlocking().
                        first()
                ).
                flatMap(Observable::from);

        final Observable<SnapshotType> snapshotTypeObservable = getExecutor().
                applyEvents(this, snapshot, forwardOnlyEvents, new ArrayList<>(), Collections.singletonList(aggregate));
        return snapshotTypeObservable.
                doOnNext(snapshotType -> {
                    if (DefaultGroovyMethods.asBoolean(events)) {
                        snapshotType.setLastEvent(DefaultGroovyMethods.last(events));
                    }

                    getLog().info("  --> Computed: " + String.valueOf(snapshotType));
                }).
                flatMap(it -> {
                    EventType lastEvent = DefaultGroovyMethods.asBoolean(events) ? DefaultGroovyMethods.last(events) : null;
                    return it.getDeprecatedBy() != null && lastEvent != null && lastEvent instanceof DeprecatedBy && redirect ? computeSnapshot(it.getDeprecatedBy(), moment) : Observable.just(it);
                });
    }

    /**
     * Computes a snapshot for specified version of an aggregate
     *
     * @param aggregate The aggregate
     * @param moment    The moment at which the snapshot is desired
     * @return An Optional SnapshotType. Empty if cannot be computed.
     */
    default Observable<SnapshotType> computeSnapshot(Aggregate aggregate, Date moment) {
        return computeSnapshot(aggregate, moment, true);
    }

    default Executor<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> getExecutor() {
        return new QueryExecutor<>();
    }

    Observable<EventType> getUncomputedEvents(Aggregate aggregate, SnapshotType lastSnapshot, Date snapshotTime);
}
