package com.github.rahulsom.grooves.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as being used in tracing.
 * These methods calls are logged at trace level with information on nesting.
 *
 * @see IndentedLogging
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trace {}
