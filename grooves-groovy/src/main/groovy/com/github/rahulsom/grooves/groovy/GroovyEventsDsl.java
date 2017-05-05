package com.github.rahulsom.grooves.groovy;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventsDsl;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Events DSL with great support for Groovy Closures.
 *
 * @param <AggregateT> The Aggregate on which this operates
 * @param <EventIdT>   The Type of Event's id field
 * @param <EventT>     The type of event on which this operates
 *
 * @author Rahul Somasunderam
 */
public class GroovyEventsDsl<
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>> extends
        EventsDsl<AggregateT, EventIdT, EventT> {

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
            @DelegatesTo(OnSpec.class) Closure closure) {

        OnSpec spec = new OnSpec();
        spec.setAggregate(aggregate);
        spec.setEntityConsumer(entityConsumer);
        spec.setUserSupplier(userSupplier);
        spec.setTimestampSupplier(timestampSupplier);
        spec.setPositionSupplier(positionSupplier);
        closure.setDelegate(spec);
        closure.call(spec);
        return aggregate;
    }

    /**
     * Allows executing a consumer with some context to setup events.
     * Defaults the timestamp to current time.
     *
     * @param aggregate        The aggregate on which the consumer must operate
     * @param entityConsumer   A Consumer that decides what happens when apply is called on an
     *                         entity
     * @param positionSupplier A supplier which offers the default position number for an event
     *                         when it is not provided
     * @param userSupplier     A supplier that provides the User who created the event if it isn't
     *                         set
     * @param closure          The block of code to execute with the aggregate
     *
     * @return The aggregate after all the code has been executed
     */
    public AggregateT on(
            AggregateT aggregate, Consumer entityConsumer, Supplier<Long> positionSupplier,
            Supplier<String> userSupplier, @DelegatesTo(OnSpec.class) Closure closure) {
        return on(aggregate, entityConsumer, positionSupplier, userSupplier, Date::new, closure);
    }

    /**
     * Allows executing a consumer with some context to setup events.
     * Defaults the timestamp to current time.
     * Defaults the user to "anonymous".
     *
     * @param aggregate        The aggregate on which the consumer must operate
     * @param entityConsumer   A Consumer that decides what happens when apply is called on an
     *                         entity
     * @param positionSupplier A supplier which offers the default position number for an event
     *                         when it is not provided
     * @param closure          The block of code to execute with the aggregate
     *
     * @return The aggregate after all the code has been executed
     */
    public AggregateT on(
            AggregateT aggregate, Consumer entityConsumer, Supplier<Long> positionSupplier,
            @DelegatesTo(OnSpec.class) Closure closure) {
        return on(aggregate, entityConsumer, positionSupplier, () -> "anonymous", Date::new,
                closure);
    }

    /**
     * Allows executing a consumer with some context to setup events.
     * Defaults the timestamp to current time.
     * Defaults the user to "anonymous".
     * Defaults the position to a monotonically increasing number.
     *
     * @param aggregate      The aggregate on which the consumer must operate
     * @param entityConsumer A Consumer that decides what happens when apply is called on an
     *                       entity
     * @param closure        The block of code to execute with the aggregate
     *
     * @return The aggregate after all the code has been executed
     */
    public AggregateT on(AggregateT aggregate, Consumer entityConsumer,
                         @DelegatesTo(OnSpec.class) Closure closure) {
        return on(aggregate, entityConsumer, () -> getDefaultPositionSupplier().incrementAndGet(),
                () -> "anonymous", Date::new, closure);
    }


}
