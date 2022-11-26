package com.github.rahulsom.grooves.test;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.reactivex.Flowable.fromPublisher;

/**
 * Helps configure the {@code on} method.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
@Setter
public class OnSpec<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT>> {
    @Getter
    private AggregateT aggregate;
    private Consumer entityConsumer;
    private Supplier<Date> timestampSupplier;
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
    @NotNull public <T extends EventT> T apply(@NotNull T event) {
        event.setAggregate(aggregate);

        if (event.getPosition() == 0) {
            event.setPosition(positionSupplier.get());
        }
        //if (event.getTimestamp() == null) {
        event.setTimestamp(timestampSupplier.get());
        //}

        entityConsumer.accept(event);

        return event;
    }

    /**
     * Computes and persists a snapshot based on a QueryUtil on the aggregate that this
     * OnSpec applies on.
     *
     * @param <QueryT>      The type of Query to be executed
     * @param query         The Query Util to compute the snapshot
     * @param beforePersist Code to execute before persisting the snapshot.
     *
     * @return The snapshot after persisting
     */
    @NotNull
    public <QueryT extends QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT,
            SnapshotT>> SnapshotT snapshotWith(
                    @NotNull QueryT query,
                    @NotNull Consumer<SnapshotT> beforePersist) {

        SnapshotT snapshotT = fromPublisher(query.computeSnapshot(aggregate, Long.MAX_VALUE))
                .blockingFirst();

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
    @NotNull
    public <QueryT extends QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT,
            SnapshotT>> SnapshotT snapshotWith(@NotNull QueryT query) {
        return snapshotWith(query, snapshotT -> {
        });
    }

}