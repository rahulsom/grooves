package com.github.rahulsom.grooves.api;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
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
        AggregateT extends AggregateType,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>> {

    private static AtomicLong defaultPositionSupplier = new AtomicLong();

    private static final Logger log = LoggerFactory.getLogger(EventsDsl.class);

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

    public class OnSpec<
            SnapshotIdT,
            SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT>> {
        private AggregateT aggregate;
        private Consumer entityConsumer;
        private Supplier<Date> timestampSupplier;
        private Supplier<String> userSupplier;
        private Supplier<Long> positionSupplier;

        /**
         * Applies an event to an aggregate. This involves checking if any important fields are
         * missing and populating them based on the suppliers.
         *
         * @param event The event to be applied
         *
         * @return The event after persisting
         */
        public EventT apply(EventT event) {
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
        public SnapshotT snapshotWith(
                QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> queryUtil,
                Consumer<SnapshotT> beforePersist) {


            SnapshotT snapshotT = queryUtil
                    .computeSnapshot(aggregate, Long.MAX_VALUE)
                    .toBlocking()
                    .single();

            beforePersist.accept(snapshotT);
            entityConsumer.accept(snapshotT);

            log.info("Persisted {}", snapshotT);

            return snapshotT;
        }

        /**
         * Computes and persists a snapshot based on a QueryUtil on the aggregate that this
         * OnSpec applies on.
         *
         * @param queryUtil The Query Util to compute the snapshot
         *
         * @return The snapshot after persisting
         */
        public SnapshotT snapshotWith(
                QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> queryUtil) {
            return snapshotWith(queryUtil, snapshotT -> { });
        }

        public AggregateT getAggregate() {
            return aggregate;
        }

        public void setAggregate(AggregateT aggregate) {
            this.aggregate = aggregate;
        }

        public void setEntityConsumer(Consumer entityConsumer) {
            this.entityConsumer = entityConsumer;
        }

        public void setTimestampSupplier(Supplier<Date> timestampSupplier) {
            this.timestampSupplier = timestampSupplier;
        }

        public void setUserSupplier(Supplier<String> userSupplier) {
            this.userSupplier = userSupplier;
        }

        public void setPositionSupplier(Supplier<Long> positionSupplier) {
            this.positionSupplier = positionSupplier;
        }
    }
}
