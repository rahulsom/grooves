package com.github.rahulsom.grooves.queries.internal;

import static io.reactivex.Flowable.fromPublisher;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

/**
 * An executor that applies a function.
 *
 * @param <AggregateT>       The type of Aggregate.
 * @param <EventIdT>         The type of EventT's id.
 * @param <EventT>           The type of Event.
 * @param <ApplicableEventT> The type of EventT that can be applied.
 * @param <SnapshotIdT>      The type of SnapshotT's id.
 * @param <SnapshotT>        The type of Snapshot.
 * @param <QueryT>           The type of Query.
 * @author Rahul Somasunderam
 */
public class SimpleExecutor<
                AggregateT,
                EventIdT,
                EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
                ApplicableEventT extends EventT,
                SnapshotIdT,
                SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
                QueryT extends SimpleQuery<AggregateT, EventIdT, EventT, ApplicableEventT, SnapshotIdT, SnapshotT>>
        extends QueryExecutor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT> {

    @NotNull
    @Override
    protected Flowable<EventApplyOutcome> callMethod(
            @NotNull QueryT query, @NotNull String methodName, @NotNull SnapshotT snapshot, @NotNull EventT event) {
        return fromPublisher(query.applyEvent((ApplicableEventT) event, snapshot));
    }
}
