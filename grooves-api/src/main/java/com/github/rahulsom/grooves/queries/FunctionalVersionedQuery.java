package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import com.github.rahulsom.grooves.queries.internal.SimpleExecutor;
import com.github.rahulsom.grooves.queries.internal.SimpleQuery;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Util class to build Versioned Queries in a Functional Style.
 *
 * @param <AggregateT>   The aggregate over which the query executes
 * @param <EventIdT>     The type of the EventT's id field
 * @param <EventT>       The type of the Event
 * @param <SnapshotIdT>  The type of the SnapshotT's id field
 * @param <SnapshotT>    The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder", setterPrefix = "with")
public class FunctionalVersionedQuery<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        > implements
        VersionedQuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        SimpleQuery<AggregateT, EventIdT, EventT, EventT, SnapshotIdT, SnapshotT> {

    private BiFunction<Long, AggregateT, Publisher<SnapshotT>> snapshot;
    private Supplier<SnapshotT> emptySnapshot;
    private TriFunction<AggregateT, SnapshotT, Long, Publisher<EventT>> events;
    private Predicate<SnapshotT> applyEvents;
    private BiConsumer<SnapshotT, AggregateT> deprecator;
    private TriFunction<Exception, SnapshotT, EventT,
            Publisher<EventApplyOutcome>> exceptionHandler;
    private BiFunction<EventT, SnapshotT, Publisher<EventApplyOutcome>> eventHandler;

    @NotNull
    @Override
    public SimpleExecutor<AggregateT, EventIdT, EventT, ?, SnapshotIdT, SnapshotT,
            ?> getExecutor() {
        return new SimpleExecutor<>();
    }

    @NotNull
    @Override
    public Publisher<SnapshotT> getSnapshot(long version, @NotNull AggregateT aggregate) {
        return snapshot.apply(version, aggregate);
    }

    @NotNull
    @Override
    public SnapshotT createEmptySnapshot() {
        return emptySnapshot.get();
    }

    @NotNull
    @Override
    public Publisher<EventT> getUncomputedEvents(
            @NotNull AggregateT aggregate, SnapshotT lastSnapshot, long version) {
        return events.apply(aggregate, lastSnapshot, version);
    }

    @Override
    public boolean shouldEventsBeApplied(@NotNull SnapshotT snapshot) {
        return applyEvents.test(snapshot);
    }

    @Override
    public void addToDeprecates(
            @NotNull SnapshotT snapshot, @NotNull AggregateT deprecatedAggregate) {
        deprecator.accept(snapshot, deprecatedAggregate);
    }

    @NotNull
    @Override
    public Publisher<EventApplyOutcome> onException(
            @NotNull Exception e, @NotNull SnapshotT snapshot, @NotNull EventT event) {
        return exceptionHandler.apply(e, snapshot, event);
    }

    @NotNull
    @Override
    public Publisher<EventApplyOutcome> applyEvent(
            @NotNull EventT event, @NotNull SnapshotT snapshot) {
        return eventHandler.apply(event, snapshot);
    }

    /**
     * Builder for FunctionalVersionedQuery.
     *
     * @param <AggregateT>   The aggregate over which the query executes
     * @param <EventIdT>     The type of the EventT's id field
     * @param <EventT>       The type of the Event
     * @param <SnapshotIdT>  The type of the SnapshotT's id field
     * @param <SnapshotT>    The type of the Snapshot
     */
    public static class Builder<
            AggregateT,
            EventIdT,
            EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
            > {}

}
