package com.github.rahulsom.grooves.api

/**
 * Base class for Events
 *
 * @param <A> Aggregate this event applies to
 *
 * @author Rahul Somasunderam
 */
interface BaseEvent<A extends AggregateType> {
    A getAggregate()

    abstract String getAudit()

    Date getDate()

    String getCreatedBy()

    BaseEvent<A> getRevertedBy()

    void setRevertedBy(BaseEvent<A> revertEvent)

    public <T extends Object> T getId()

    Long getPosition()
}



