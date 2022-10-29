package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import com.github.rahulsom.grooves.queries.internal.SimpleExecutor;
import com.github.rahulsom.grooves.queries.internal.SimpleQuery;
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
public class FunctionalVersionedQuery<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT,
                EventT>
        > implements
        VersionedQuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT
                >,
        SimpleQuery<AggregateT, EventIdT, EventT, EventT, SnapshotIdT, SnapshotT
                > {

    private BiFunction<Long, AggregateT, Publisher<SnapshotT>> snapshot;
    private Supplier<SnapshotT> emptySnapshot;
    private TriFunction<AggregateT, SnapshotT, Long, Publisher<EventT>> events;
    private Predicate<SnapshotT> applyEvents;
    private BiConsumer<SnapshotT, AggregateT> deprecator;
    private TriFunction<Exception, SnapshotT, EventT,
            Publisher<EventApplyOutcome>> exceptionHandler;
    private BiFunction<EventT, SnapshotT, Publisher<EventApplyOutcome>> eventHandler;

    private FunctionalVersionedQuery() {
    }

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

    public static <
            AggregateT,
            EventIdT,
            EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
            QueryT extends FunctionalVersionedQuery<AggregateT, EventIdT, EventT,
                SnapshotIdT, SnapshotT>
            > Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
            QueryT> newBuilder() {
        return new Builder<>();
    }

    /**
     * Builder for FunctionalVersionedQuery.
     *
     * @param <AggregateT>   The aggregate over which the query executes
     * @param <EventIdT>     The type of the EventT's id field
     * @param <EventT>       The type of the Event
     * @param <SnapshotIdT>  The type of the SnapshotT's id field
     * @param <SnapshotT>    The type of the Snapshot
     * @param <QueryT>       A reference to the query type. Typically a self reference.
     */
    public static final class Builder<
            AggregateT,
            EventIdT,
            EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
            QueryT extends FunctionalVersionedQuery<AggregateT, EventIdT, EventT,
                    SnapshotIdT, SnapshotT>
            > {
        private BiFunction<Long, AggregateT, Publisher<SnapshotT>> snapshot;
        private Supplier<SnapshotT> emptySnapshot;
        private TriFunction<AggregateT, SnapshotT, Long, Publisher<EventT>> events;
        private Predicate<SnapshotT> applyEvents = snapshotT -> true;
        private BiConsumer<SnapshotT, AggregateT> deprecator;
        private TriFunction<Exception, SnapshotT, EventT,
                Publisher<EventApplyOutcome>> exceptionHandler;
        private BiFunction<EventT, SnapshotT, Publisher<EventApplyOutcome>> eventHandler;

        private Builder() {
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT>
                withSnapshot(
                BiFunction<Long, AggregateT, @NotNull Publisher<@NotNull SnapshotT>> snapshot) {
            this.snapshot = snapshot;
            return this;
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withEmptySnapshot(@NotNull Supplier<@NotNull SnapshotT> emptySnapshot) {
            this.emptySnapshot = emptySnapshot;
            return this;
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withEvents(TriFunction<AggregateT, SnapshotT, Long,
                @NotNull Publisher<@NotNull EventT>> events) {
            this.events = events;
            return this;
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withApplyEvents(Predicate<SnapshotT> applyEvents) {
            this.applyEvents = applyEvents;
            return this;
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withDeprecator(BiConsumer<SnapshotT, AggregateT> deprecator) {
            this.deprecator = deprecator;
            return this;
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withExceptionHandler(
                TriFunction<Exception, SnapshotT, EventT,
                        @NotNull Publisher<@NotNull EventApplyOutcome>> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withEventHandler(
                BiFunction<EventT, SnapshotT,
                        @NotNull Publisher<@NotNull EventApplyOutcome>> eventHandler) {
            this.eventHandler = eventHandler;
            return this;
        }

        /**
         * Builds the Functional Versioned Query.
         *
         * @return A Versioned Query
         */
        public VersionedQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> build() {
            FunctionalVersionedQuery<AggregateT, EventIdT, EventT, SnapshotIdT,
                    SnapshotT> functionalVersionedQuery = new FunctionalVersionedQuery<>();
            functionalVersionedQuery.deprecator = this.deprecator;
            functionalVersionedQuery.events = this.events;
            functionalVersionedQuery.exceptionHandler = this.exceptionHandler;
            functionalVersionedQuery.applyEvents = this.applyEvents;
            functionalVersionedQuery.snapshot = this.snapshot;
            functionalVersionedQuery.emptySnapshot = this.emptySnapshot;
            functionalVersionedQuery.eventHandler = this.eventHandler;
            return functionalVersionedQuery;
        }
    }
}
