package com.github.rahulsom.grooves.api;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import rx.Observable;

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
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>> {

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

    public static AtomicLong getDefaultPositionSupplier() {
        return defaultPositionSupplier;
    }

    public static void setDefaultPositionSupplier(AtomicLong defaultPositionSupplier) {
        EventsDsl.defaultPositionSupplier = defaultPositionSupplier;
    }

    private static AtomicLong defaultPositionSupplier = new AtomicLong();

    public class OnSpec<
            SnapshotIdT,
            SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT>> {
        /**
         * Applies an event to an aggregate. This involves checking if any important fields are
         * missing and populating them based on the suppliers.
         *
         * @param event The event to be applied
         * @param <T>   The Type of event
         *
         * @return The event after persisting
         */
        public <T extends EventT> T apply(T event) {
            event.setAggregate(aggregate);

            if (event.getCreatedBy() == null) {
                event.setCreatedBy(userSupplier.get());
            }
            if (event.getPosition() == null) {
                event.setPosition(positionSupplier.get());
            }
            if (event.getTimestamp() == null) {
                event.setTimestamp(timestampSupplier.get());
            }

            entityConsumer.accept(event);

            return event;
        }

        /**
         * Computes and persists a snapshot based on a QueryUtil on the aggregate that this
         * OnSpec applies on
         *
         * @param queryUtil     The Query Util to compute the snapshot
         * @param beforePersist Code to execute before persisting the snapshot.
         *
         * @return The snapshot after persisting
         */
        public Observable<SnapshotT> snapshotWith(
                QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> queryUtil,
                Consumer<SnapshotT> beforePersist) {

            return queryUtil.computeSnapshot(aggregate, Long.MAX_VALUE).doOnNext(it -> {
                beforePersist.accept(it);
                entityConsumer.accept(it);
            });
        }

        public Observable snapshotWith(
                QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> queryUtil) {
            return snapshotWith(queryUtil, null);
        }

        public AggregateT getAggregate() {
            return aggregate;
        }

        public void setAggregate(AggregateT aggregate) {
            this.aggregate = aggregate;
        }

        public Consumer getEntityConsumer() {
            return entityConsumer;
        }

        public void setEntityConsumer(Consumer entityConsumer) {
            this.entityConsumer = entityConsumer;
        }

        public Supplier<Date> getTimestampSupplier() {
            return timestampSupplier;
        }

        public void setTimestampSupplier(Supplier<Date> timestampSupplier) {
            this.timestampSupplier = timestampSupplier;
        }

        public Supplier<String> getUserSupplier() {
            return userSupplier;
        }

        public void setUserSupplier(Supplier<String> userSupplier) {
            this.userSupplier = userSupplier;
        }

        public Supplier<Long> getPositionSupplier() {
            return positionSupplier;
        }

        public void setPositionSupplier(Supplier<Long> positionSupplier) {
            this.positionSupplier = positionSupplier;
        }

        private AggregateT aggregate;
        private Consumer entityConsumer;
        private Supplier<Date> timestampSupplier;
        private Supplier<String> userSupplier;
        private Supplier<Long> positionSupplier;
    }
}
