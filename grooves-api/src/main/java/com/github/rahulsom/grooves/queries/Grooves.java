package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Supports Functional Reactive Programming style querying.
 *
 * @author Rahul Somasunderam
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Grooves {

    /**
     * Creates a builder for a versioned query.
     * <pre>{@code
     *     Grooves.versioned()
     *          .withSnapshot(version, snapshot -> ?)
     *          .withEmptySnapshot(-> ?)
     *          .withEvents(aggregate, snapshot, version -> ?)
     *          .withApplyEvents(snapshot -> true)
     *          .withDeprecator(snapshot, deprecatingAggregate -> ?)
     *          .withExceptionHandler(exception, snapshot, event -> ?)
     *          .withEventHandler(event, balance -> ?)
     *          .build();
     * }</pre>
     *
     * @param <AggregateT>   The aggregate over which the query executes
     * @param <EventIdT>     The type of the EventT's id field
     * @param <EventT>       The type of the Event
     * @param <SnapshotIdT>  The type of the SnapshotT's id field
     * @param <SnapshotT>    The type of the Snapshot
     * @return a query builder
     */
    @NotNull
    public static <
                    AggregateT,
                    EventIdT,
                    EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                    SnapshotIdT,
                    SnapshotT extends VersionedSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>>
            FunctionalVersionedQuery.Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> versioned() {
        return FunctionalVersionedQuery.<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>newBuilder();
    }

    /**
     * Creates a builder for a temporal query.
     * <pre>{@code
     *     Grooves.temporal()
     *          .withSnapshot(date, snapshot -> ?)
     *          .withEmptySnapshot(-> ?)
     *          .withEvents(aggregate, snapshot, date -> ?)
     *          .withApplyEvents(snapshot -> true)
     *          .withDeprecator(snapshot, deprecatingAggregate -> ?)
     *          .withExceptionHandler(exception, snapshot, event -> ?)
     *          .withEventHandler(event, balance -> ?)
     *          .build();
     * }</pre>
     *
     * @param <AggregateT>   The aggregate over which the query executes
     * @param <EventIdT>     The type of the EventT's id field
     * @param <EventT>       The type of the Event
     * @param <SnapshotIdT>  The type of the SnapshotT's id field
     * @param <SnapshotT>    The type of the Snapshot
     * @return a query builder
     */
    @NotNull
    public static <
                    AggregateT,
                    EventIdT,
                    EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                    SnapshotIdT,
                    SnapshotT extends TemporalSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>>
            FunctionalTemporalQuery.Builder<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> temporal() {
        return FunctionalTemporalQuery.<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT>newBuilder();
    }
}
