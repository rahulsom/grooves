package com.github.rahulsom.grooves.queries;

/**
 * A function that takes three arguments and produces a result.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <R> the type of the result
 * @author Rahul Somasunderam
 */
@SuppressWarnings("WeakerAccess")
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}
