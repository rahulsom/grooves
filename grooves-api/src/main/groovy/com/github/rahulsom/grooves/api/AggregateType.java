package com.github.rahulsom.grooves.api;

/**
 * Marks a class as an aggregate.
 * <p>
 * <br/>
 * You should not have to use this class.
 *
 * @param <AggregateIdType>
 *
 * @author Rahul Somasunderam
 */
public interface AggregateType<AggregateIdType> {
    AggregateIdType getId();
    void setId(AggregateIdType id);
}
