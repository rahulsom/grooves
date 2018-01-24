package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

public interface SimpleQuery<
        AggregateT,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        ApplicableEventT extends EventT,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>
        > extends
        BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    @NotNull Publisher<EventApplyOutcome> applyEvent(
            @NotNull ApplicableEventT event, @NotNull SnapshotT snapshot);

    @NotNull
    @Override
    default SimpleExecutor<AggregateT, EventIdT, EventT, ?, SnapshotIdT, SnapshotT,
            ?> getExecutor() {
        return new SimpleExecutor<>();
    }
}
