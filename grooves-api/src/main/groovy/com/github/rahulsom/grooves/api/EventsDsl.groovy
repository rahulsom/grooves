package com.github.rahulsom.grooves.api

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import com.github.rahulsom.grooves.queries.QuerySupport

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * DSL to simplify writing code with Events
 *
 * @author Rahul Somasunderam
 */
class EventsDsl {
    static AtomicLong defaultPositionSupplier = new AtomicLong()
    private static class OnSpec<
            SnapshotIdType,
            Aggregate extends AggregateType,
            EventIdType,
            EventType extends BaseEvent<Aggregate, EventIdType, EventType>,
            SnapshotType extends Snapshot<Aggregate, SnapshotIdType, EventIdType, EventType>> {
        Aggregate aggregate
        Consumer  entityConsumer

        Supplier<Date> timestampSupplier
        Supplier<String> userSupplier
        Supplier<Long> positionSupplier

        void apply(EventType event) {
            event.aggregate = aggregate

            if (!event.createdBy)
                event.createdBy = userSupplier.get()
            if (!event.position)
                event.position = positionSupplier.get()
            if (!event.timestamp)
                event.timestamp = timestampSupplier.get()

            entityConsumer.accept(event)
        }

        void snapshotWith(
                QuerySupport<Aggregate, EventIdType, EventType, SnapshotIdType, SnapshotType> queryUtil,
                Consumer<SnapshotType> beforePersist = null) {

            queryUtil.computeSnapshot(aggregate, Long.MAX_VALUE).ifPresent {
                beforePersist?.accept(it)
                entityConsumer.accept(it)
            }
        }
    }

    static <Aggregate extends AggregateType,
            EventIdType,
            EventType extends BaseEvent<Aggregate, EventIdType, EventType>> Aggregate on(
            Aggregate aggregate,
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
