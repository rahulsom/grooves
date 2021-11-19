package com.github.rahulsom.grooves.logging

/**
 * Marks a method as being used in tracing.
 * These methods calls are logged at trace level with information on nesting.
 *
 * @see [IndentedLogging]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Trace(
    val twoStep: Boolean
)