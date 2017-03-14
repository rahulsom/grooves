package com.github.rahulsom.grooves.api

import com.github.rahulsom.grooves.api.internal.BaseEvent
import com.github.rahulsom.grooves.queries.QueryUtil

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * DSL to simplify writing code with Events
 */
class EventsDsl {
    static AtomicLong defaultPositionSupplier = new AtomicLong()
    static class OnSpec<SI, A extends AggregateType, E extends BaseEvent<A, E>, S extends Snapshot<A, ?>> {
        A aggregate
        Consumer entityConsumer

        Supplier<Date> timestampSupplier
        Supplier<String> userSupplier
        Supplier<Long> positionSupplier

        void apply(E event) {
            event.aggregate = aggregate

            if (!event.createdBy)
                event.createdBy = userSupplier.get()
            if (!event.position)
                event.position = positionSupplier.get()
            if (!event.timestamp)
                event.timestamp = timestampSupplier.get()

            entityConsumer.accept(event)
        }

        void snapshotWith(QueryUtil<A, E, S> queryUtil, Consumer<S> beforePersist = null) {

            queryUtil.computeSnapshot(aggregate, Long.MAX_VALUE).ifPresent {
                beforePersist?.accept(it)
                entityConsumer.accept(it)
            }
        }
    }

    static <A extends AggregateType, E extends BaseEvent<A, E>> A on(
            A aggregate,
            Consumer entityConsumer,
            Supplier<Long> positionSupplier = { defaultPositionSupplier.incrementAndGet() },
            Supplier<String> userSupplier = { 'anonymous' },
            Supplier<Date> timestampSupplier = { new Date() },
            @DelegatesTo(OnSpec) Closure closure) {

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = new OnSpec(
                aggregate: aggregate,
                entityConsumer: entityConsumer,
                userSupplier: userSupplier,
                timestampSupplier: timestampSupplier,
                positionSupplier: positionSupplier
        )
        closure.call()
        return aggregate
    }
}
