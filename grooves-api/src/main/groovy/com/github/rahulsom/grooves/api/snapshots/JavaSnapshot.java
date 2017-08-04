package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for snapshots in Java.
 *
 * @author rahul somasunderam
 */
public interface JavaSnapshot<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        SnapshotIdT,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>
        >
        extends Snapshot<AggregateIdT, AggregateT, SnapshotIdT, EventIdT, EventT> {

    @Override
    default void setLastEvent(@NotNull EventT event) {
        this.setLastEventTimestamp(event.getTimestamp());
        this.setLastEventPosition(event.getPosition());
    }

}
