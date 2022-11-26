package com.github.rahulsom.grooves.java;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Query.
 *
 * <p>The query is something that computes a snapshot out of an aggregate and a collection of events
 *
 * @author Rahul Somasunderam
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Query {
    /**
     * The aggregate for which this query needs to be performed.
     *
     * @return The aggregate for which this query needs to be performed
     */
    Class<?> aggregate();

    /**
     * The snapshot that the query will return.
     *
     * @return The snapshot that the query will return
     */
    Class<? extends Snapshot> snapshot();
}
