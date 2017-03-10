package com.github.rahulsom.grooves.api

/**
 * Base class for Events
 *
 * @param <A> Aggregate this event applies to
 *
 * @author Rahul Somasunderam
 */
interface BaseEvent<A extends AggregateType, E> {
    A getAggregate()
    void setAggregate(A aggregate)

    String getAudit()

    Date getTimestamp()
    void setTimestamp(Date timestamp)

    String getCreatedBy()
    void setCreatedBy(String creator)

    RevertEvent<A, E> getRevertedBy()
    void setRevertedBy(RevertEvent<A, E> revertEvent)

    public <T extends Object> T getId()

    Long getPosition()
    void setPosition(Long position)
}



