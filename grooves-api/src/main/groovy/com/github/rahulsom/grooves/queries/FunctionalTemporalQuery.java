package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.queries.internal.SimpleExecutor;
import com.github.rahulsom.grooves.queries.internal.SimpleQuery;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Util class to build Temporal Queries in a Functional Style.
 *
 * @param <AggregateT>   The aggregate over which the query executes
 * @param <EventIdT>     The type of the {@link EventT}'s id field
 * @param <EventT>       The type of the Event
 * @param <SnapshotIdT>  The type of the {@link SnapshotT}'s id field
 * @param <SnapshotT>    The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public class FunctionalTemporalQuery<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        > implements
        TemporalQuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>,
        SimpleQuery<AggregateT, EventIdT, EventT, EventT, SnapshotIdT, SnapshotT> {

    private BiFunction<Date, AggregateT, Publisher<SnapshotT>> snapshot;
    private Supplier<SnapshotT> emptySnapshot;
    private TriFunction<AggregateT, SnapshotT, Date, Publisher<EventT>> events;
    private Predicate<SnapshotT> applyEvents;
    private BiConsumer<SnapshotT, AggregateT> deprecator;
    private TriFunction<Exception, SnapshotT, EventT,
            Publisher<EventApplyOutcome>> exceptionHandler;
    private BiFunction<EventT, SnapshotT, Publisher<EventApplyOutcome>> eventHandler;

    FunctionalTemporalQuery() {
    }

    @NotNull
    @Override
    public SimpleExecutor getExecutor() {
        return new SimpleExecutor();
    }

    @NotNull
    @Override
    public Publisher<SnapshotT> getSnapshot(Date maxTimestamp, @NotNull AggregateT aggregate) {
        return snapshot.apply(maxTimestamp, aggregate);
    }

    @NotNull
    @Override
    public SnapshotT createEmptySnapshot() {
        return emptySnapshot.get();
    }

    @Override
    public Publisher<EventT> getUncomputedEvents(
            AggregateT aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        return events.apply(aggregate, lastSnapshot, snapshotTime);
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

    @Override
    public Publisher<EventApplyOutcome> applyEvent(EventT event, SnapshotT snapshot) {
        return eventHandler.apply(event, snapshot);
    }

    /**
     * Builder for {@link FunctionalTemporalQuery}.
     *
     * @param <AggregateT>   The aggregate over which the query executes
     * @param <EventIdT>     The type of the {@link EventT}'s id field
     * @param <EventT>       The type of the Event
     * @param <SnapshotIdT>  The type of the {@link SnapshotT}'s id field
     * @param <SnapshotT>    The type of the Snapshot
     * @param <QueryT>       A reference to the query type. Typically a self reference.
     */
    public static final class Builder<
            AggregateT,
            EventIdT,
            EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
            QueryT extends FunctionalTemporalQuery<AggregateT, EventIdT, EventT,
                    SnapshotIdT, SnapshotT>
            > {
        private BiFunction<Date, AggregateT, Publisher<SnapshotT>> snapshot;
        private Supplier<SnapshotT> emptySnapshot;
        private TriFunction<AggregateT, SnapshotT, Date, Publisher<EventT>> events;
        private Predicate<SnapshotT> applyEvents = snapshotT -> true;
        private BiConsumer<SnapshotT, AggregateT> deprecator;
        private TriFunction<Exception, SnapshotT, EventT,
                Publisher<EventApplyOutcome>> exceptionHandler;
        private BiFunction<EventT, SnapshotT, Publisher<EventApplyOutcome>> eventHandler;

        private Builder() {
        }

        public static <
                AggregateT,
                EventIdT,
                EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                SnapshotIdT,
                SnapshotT extends TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT,
                        EventT>,
                QueryT extends FunctionalTemporalQuery<AggregateT, EventIdT, EventT,
                        SnapshotIdT, SnapshotT>
                > Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> newBuilder() {
            return new Builder<>();
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withSnapshot(BiFunction<Date, AggregateT,
                @NotNull Publisher<@NotNull SnapshotT>> snapshot) {
            this.snapshot = snapshot;
            return this;
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withEmptySnapshot(@NotNull Supplier<@NotNull SnapshotT> emptySnapshot) {
            this.emptySnapshot = emptySnapshot;
            return this;
        }

        public Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
                QueryT> withEvents(TriFunction<AggregateT, SnapshotT, Date,
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
         * Builds the Functional Temporal Query.
         *
         * @return A Temporal Query
         */
        public TemporalQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> build() {
            FunctionalTemporalQuery<AggregateT, EventIdT, EventT, SnapshotIdT,
                    SnapshotT> functionalTemporalQuery = new FunctionalTemporalQuery<>();
            functionalTemporalQuery.deprecator = this.deprecator;
            functionalTemporalQuery.events = this.events;
            functionalTemporalQuery.exceptionHandler = this.exceptionHandler;
            functionalTemporalQuery.applyEvents = this.applyEvents;
            functionalTemporalQuery.snapshot = this.snapshot;
            functionalTemporalQuery.emptySnapshot = this.emptySnapshot;
            functionalTemporalQuery.eventHandler = this.eventHandler;
            return functionalTemporalQuery;
        }
    }
}
