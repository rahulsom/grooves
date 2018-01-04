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
        SnapshotT extends BaseSnapshot<AggregateT, SnapshotIdT, EventIdT, EventT>,
        QueryT extends SimpleQuery<AggregateT, EventIdT, EventT, ApplicableEventT,
                SnapshotIdT, SnapshotT, QueryT>
        > extends
        BaseQuery<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    Publisher<EventApplyOutcome> applyEvent(ApplicableEventT event, SnapshotT snapshot);

    @NotNull
    @Override
    default SimpleExecutor getExecutor() {
        return new SimpleExecutor<>();
    }
}
