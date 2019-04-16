package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

import static io.reactivex.Flowable.fromPublisher;

public class SimpleExecutor<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        ApplicableEventT extends EventT,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
        QueryT extends
                SimpleQuery<AggregateT, EventIdT, EventT, ApplicableEventT, SnapshotIdT, SnapshotT>
        > extends
        QueryExecutor<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT> {

    @NotNull
    @Override
    protected Flowable<EventApplyOutcome> callMethod(
            @NotNull QueryT query,
            @NotNull String methodName,
            @NotNull SnapshotT snapshot,
            @NotNull EventT event) {
        return fromPublisher(query.applyEvent((ApplicableEventT) event, snapshot));
    }
}
