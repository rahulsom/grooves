package com.github.rahulsom.grooves.java;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Event {
    /**
     * Aggregates to which this event can be applied
     *
     * @return The type of the Aggregate
     */
    Class<?> value();
}
