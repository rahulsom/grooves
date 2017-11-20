package com.github.rahulsom.grooves.queries;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.TemporalSnapshot;
import com.github.rahulsom.grooves.api.snapshots.VersionedSnapshot;

public class Grooves {
    public static <
            AggregateIdT,
            AggregateT extends AggregateType<AggregateIdT>,
            EventIdT,
            EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends VersionedSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT,
                    EventT>
            > FunctionalVersionedQuery.Builder<AggregateIdT, AggregateT, EventIdT, EventT,
            SnapshotIdT, SnapshotT, ?> versioned() {
        return FunctionalVersionedQuery.Builder.newBuilder();
    }

    public static <
            AggregateIdT,
            AggregateT extends AggregateType<AggregateIdT>,
            EventIdT,
            EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>,
            SnapshotIdT,
            SnapshotT extends TemporalSnapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT,
                    EventT>
            > FunctionalTemporalQuery.Builder<AggregateIdT, AggregateT, EventIdT, EventT,
            SnapshotIdT, SnapshotT, ?> temporal() {
        return FunctionalTemporalQuery.Builder.newBuilder();
    }
}
