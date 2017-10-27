package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import org.reactivestreams.Publisher;

public interface SimpleQuery<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
        ApplicableEventT extends EventT,
        SnapshotIdT,
        SnapshotT extends BaseSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT>,
        QueryT extends SimpleQuery<AggregateIdT, AggregateT, EventIdT, EventT, ApplicableEventT,
                SnapshotIdT, SnapshotT, QueryT>
        > extends
        BaseQuery<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT, QueryT> {

    Publisher<EventApplyOutcome> applyEvent(ApplicableEventT event, SnapshotT snapshot);

    @Override
    default Executor<AggregateIdT, AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT,
            QueryT> getExecutor() {
        return new SimpleExecutor<>();
    }
}
