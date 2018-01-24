package com.github.rahulsom.grooves.queries;

@SuppressWarnings("WeakerAccess")
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}
