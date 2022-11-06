package com.github.rahulsom.grooves.test;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * DSL to simplify writing code with Events.
 *
 * @param <AggregateT>   The Aggregate on which this operates
 * @param <EventIdT>     The type of EventT's id
 * @param <EventT>       The type of event on which this operates
 *
 * @author Rahul Somasunderam
 */
public class EventsDsl<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>> {

    private static final AtomicLong defaultPositionSupplier = new AtomicLong();

    protected static AtomicLong getDefaultPositionSupplier() {
        return defaultPositionSupplier;
    }

    /**
     * Allows executing a consumer with some context to set up events.
     *
     * @param <SnapshotIdT>     The type of SnapshotT's id
     * @param <SnapshotT>       The type of Snapshot Generated
     * @param aggregate         The aggregate on which the consumer must operate
     * @param entityConsumer    A Consumer that decides what happens when apply is called on an
     *                          entity
     * @param positionSupplier  A supplier which offers the default position number for an event
     *                          when it is not provided
     * @param timestampSupplier A supplier that provides the date for an event if it isn't set
     * @param closure           The block of code to execute with the aggregate
     *
     * @return The aggregate after all the code has been executed
     */
    @NotNull
    public <SnapshotIdT,
            SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
            > AggregateT on(
            @NotNull AggregateT aggregate,
            @NotNull Consumer entityConsumer,
            @NotNull Supplier<Long> positionSupplier,
            @NotNull Supplier<Date> timestampSupplier,
            @NotNull Consumer<OnSpec<AggregateT, EventIdT, EventT, SnapshotIdT,
                                SnapshotT>> closure) {

        OnSpec<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> spec =
                new OnSpec<>();
        spec.setAggregate(aggregate);
        spec.setEntityConsumer(entityConsumer);
        spec.setTimestampSupplier(timestampSupplier);
        spec.setPositionSupplier(positionSupplier);
        closure.accept(spec);
        return aggregate;
    }

    @NotNull
    public <SnapshotIdT,
            SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT,
                    EventT>> AggregateT on(
            @NotNull AggregateT aggregate,
            @NotNull Consumer entityConsumer,
            @NotNull Supplier<Long> positionSupplier,
            @NotNull Consumer<OnSpec<AggregateT, EventIdT, EventT, SnapshotIdT,
                    SnapshotT>> closure) {
        return on(aggregate, entityConsumer, positionSupplier, Date::new,
                closure);
    }

    @NotNull
    public <SnapshotIdT,
            SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT,
                    EventT>> AggregateT on(
            @NotNull AggregateT aggregate,
            @NotNull Consumer entityConsumer,
            @NotNull Consumer<OnSpec<AggregateT, EventIdT, EventT, SnapshotIdT,
                    SnapshotT>> closure) {
        return on(aggregate, entityConsumer, defaultPositionSupplier::incrementAndGet,
                Date::new, closure);
    }

}
