package com.github.rahulsom.grooves.api;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * DSL to simplify writing code with Events.
 *
 * @param <AggregateT> The Aggregate on which this operates
 * @param <EventIdT>   The Type of Event's id field
 * @param <EventT>     The type of event on which this operates
 *
 * @author Rahul Somasunderam
 */
public class EventsDsl<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>> {

    private static AtomicLong defaultPositionSupplier = new AtomicLong();

    protected static AtomicLong getDefaultPositionSupplier() {
        return defaultPositionSupplier;
    }

    /**
     * Allows executing a consumer with some context to setup events.
     *
     * @param aggregate         The aggregate on which the consumer must operate
     * @param entityConsumer    A Consumer that decides what happens when apply is called on an
     *                          entity
     * @param positionSupplier  A supplier which offers the default position number for an event
     *                          when it is not provided
     * @param userSupplier      A supplier that provides the User who created the event if it isn't
     *                          set
     * @param timestampSupplier A supplier that provides the date for an event if it isn't set
     * @param closure           The block of code to execute with the aggregate
     *
     * @return The aggregate after all the code has been executed
     */
    public AggregateT on(
            AggregateT aggregate, Consumer entityConsumer, Supplier<Long> positionSupplier,
            Supplier<String> userSupplier, Supplier<Date> timestampSupplier,
            Consumer<OnSpec> closure) {

        OnSpec spec = new OnSpec();
        spec.setAggregate(aggregate);
        spec.setEntityConsumer(entityConsumer);
        spec.setUserSupplier(userSupplier);
        spec.setTimestampSupplier(timestampSupplier);
        spec.setPositionSupplier(positionSupplier);
        closure.accept(spec);
        return aggregate;
    }

    public AggregateT on(
            AggregateT aggregate, Consumer entityConsumer, Supplier<Long> positionSupplier,
            Supplier<String> userSupplier, Consumer<OnSpec> closure) {
        return on(aggregate, entityConsumer, positionSupplier, userSupplier, Date::new, closure);
    }

    public AggregateT on(
            AggregateT aggregate, Consumer entityConsumer, Supplier<Long> positionSupplier,
            Consumer<OnSpec> closure) {
        return on(aggregate, entityConsumer, positionSupplier, () -> "anonymous", Date::new,
                closure);
    }

    public AggregateT on(AggregateT aggregate, Consumer entityConsumer, Consumer<OnSpec> closure) {
        return on(aggregate, entityConsumer, () -> defaultPositionSupplier.incrementAndGet(),
                () -> "anonymous", Date::new, closure);
    }

}
