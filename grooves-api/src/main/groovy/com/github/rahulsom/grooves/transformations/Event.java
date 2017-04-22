package com.github.rahulsom.grooves.transformations;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.transformations.internal.EventASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an Event
 *
 * @author Rahul Somasunderam
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass(classes = EventASTTransformation.class)
public @interface Event {
    /**
     * Aggregates to which this event can be applied
     *
     * @return The type of the Aggregate
     */
    Class<AggregateType> value();
}
