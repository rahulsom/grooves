package com.github.rahulsom.grooves.api.events;

import com.github.rahulsom.grooves.api.AggregateType;

import java.util.Date;

/**
 * Base class for Events
 *
 * @param <Aggregate> Aggregate this event applies to
 * @param <EventIdType> The Type for Event's {@link #getId} field
 * @param <EventType> Event Type
 *
 * @author Rahul Somasunderam
 */
public interface BaseEvent<Aggregate extends AggregateType, EventIdType, EventType> {
    Aggregate getAggregate();
    void setAggregate(Aggregate aggregate);

    String getAudit();

    Date getTimestamp();
    void setTimestamp(Date timestamp);

    String getCreatedBy();
    void setCreatedBy(String creator);

    RevertEvent<Aggregate, EventIdType, EventType> getRevertedBy();
    void setRevertedBy(RevertEvent<Aggregate, EventIdType, EventType> revertEvent);

    EventIdType getId();

    Long getPosition();
    void setPosition(Long position);
}
