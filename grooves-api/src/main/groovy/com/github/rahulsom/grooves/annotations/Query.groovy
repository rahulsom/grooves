package com.github.rahulsom.grooves.annotations

import com.github.rahulsom.grooves.internal.QueryASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a class as a Query
 * <br/>
 * The query is something that computes a snapshot out of an aggregate and a collection of events
 *
 * @author Rahul Somasunderam
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass(classes = QueryASTTransformation)
@interface Query {
    /**
     * The aggregate for which this query needs to be performed
     * @return
     */
    Class aggregate()

    /**
     * The snapshot that the query will return
     * @return
     */
    Class snapshot()
}

