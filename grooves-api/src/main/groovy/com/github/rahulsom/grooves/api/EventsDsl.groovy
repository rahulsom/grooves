package com.github.rahulsom.grooves.api

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * DSL to simplify writing code with Events
 */
class EventsDsl {
    static AtomicLong defaultPositionSupplier = new AtomicLong()
    static class OnSpec<A extends AggregateType, E extends BaseEvent<A, E>> {
        A aggregate
        Consumer<E> eventConsumer

        Supplier<Date> dateSupplier
        Supplier<String> userSupplier
        Supplier<Long> positionSupplier

        void apply(E event) {
            event.aggregate = aggregate

            if (!event.createdBy)
                event.createdBy = userSupplier.get()
            if (!event.position)
                event.position = positionSupplier.get()
            if (!event.date)
                event.date = dateSupplier.get()

            eventConsumer.accept(event)
        }
    }

    static <A extends AggregateType, E extends BaseEvent<A, E>> void on(
            A aggregate,
            Consumer<E> eventConsumer,
            Supplier<Long> positionSupplier = { defaultPositionSupplier.incrementAndGet() },
            Supplier<String> userSupplier = { 'anonymous' },
            Supplier<Date> dateSupplier = { new Date() },
            @DelegatesTo(OnSpec) Closure closure) {

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = new OnSpec(
                aggregate: aggregate,
                eventConsumer: eventConsumer,
                userSupplier: userSupplier,
                dateSupplier: dateSupplier,
                positionSupplier: positionSupplier
        )
        closure.call()
    }
}
