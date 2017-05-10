package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;
import rx.Observable;

import java.util.Date;

/**
 * Base class for Events.
 *
 * @param <AggregateT> Aggregate this event applies to
 * @param <EventIdT>   The Type for Event's {@link #getId} field
 * @param <EventT>     Event Type
 *
 * @author Rahul Somasunderam
 */
public interface BaseEvent<AggregateT extends AggregateType, EventIdT, EventT> {
    Observable<AggregateT> getAggregateObservable();

    void setAggregate(AggregateT aggregate);

    String getAudit();

    Date getTimestamp();

    void setTimestamp(Date timestamp);

    String getCreatedBy();

    void setCreatedBy(String creator);

    RevertEvent<AggregateT, EventIdT, EventT> getRevertedBy();

    void setRevertedBy(RevertEvent<AggregateT, EventIdT, EventT> revertEvent);

    EventIdT getId();

    Long getPosition();

    void setPosition(Long position);
}
