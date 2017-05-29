package com.github.rahulsom.grooves.api.snapshots;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.api.events.BaseEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by rxs6995 on 5/24/17.
 */
public interface JavaJoin<
        AggregateIdT,
        AggregateT extends AggregateType<AggregateIdT>,
        JoinIdT,
        JoinedAggregateIdT,
        EventIdT,
        EventT extends BaseEvent<AggregateIdT, AggregateT, EventIdT, EventT>> extends
        JavaSnapshot<AggregateIdT, AggregateT, JoinIdT, EventIdT, EventT>,
        Join<AggregateIdT, AggregateT, JoinIdT, JoinedAggregateIdT, EventIdT, EventT> {

    @Override
    default void setLastEvent(@NotNull EventT event) {
        this.setLastEventTimestamp(event.getTimestamp());
        this.setLastEventPosition(event.getPosition());
    }

}
