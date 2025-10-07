package com.github.rahulsom.grooves.groovy.transformations;

import com.github.rahulsom.grooves.groovy.transformations.internal.AggregateASTTransformation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * Marks a class as an Aggregate.
 *
 * @author Rahul Somasunderam
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass(classes = AggregateASTTransformation.class)
public @interface Aggregate {}
