package com.github.rahulsom.grooves.api;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OnSpec<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>> {
    private AggregateT aggregate;
    private Consumer entityConsumer;
    private Supplier<Date> timestampSupplier;
    private Supplier<String> userSupplier;
    private Supplier<Long> positionSupplier;

    private static final Logger log = LoggerFactory.getLogger(OnSpec.class);

    /**
     * Applies an event to an aggregate. This involves checking if any important fields are
     * missing and populating them based on the suppliers.
     *
     * @param <T> type of event
     * @param event The event to be applied
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
     * @param <QueryT>      The type of Query to be executed
     * @param query         The Query Util to compute the snapshot
     * @param beforePersist Code to execute before persisting the snapshot.
     *
     * @return The snapshot after persisting
     */
    public <QueryT extends QuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
            SnapshotT, QueryT>> SnapshotT snapshotWith(
                    QueryT query, Consumer<SnapshotT> beforePersist) {

        SnapshotT snapshotT = query
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
     * @param <QueryT> The type of Query to be executed
     * @param query    The Query Util to compute the snapshot
     *
     * @return The snapshot after persisting
     */
    public <QueryT extends QuerySupport<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT,
            SnapshotT, QueryT>> SnapshotT snapshotWith(QueryT query) {
        return snapshotWith(query, snapshotT -> {
        });
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