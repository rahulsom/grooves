package com.github.rahulsom.grooves.queries.internal;

import org.jetbrains.annotations.NotNull;

/**
 * Immutable class that can store two objects.
 *
 * @param <FirstT>  Type of first object
 * @param <SecondT> Type of second object
 * @author Rahul Somasunderam
 */
public record Pair<FirstT, SecondT>(@NotNull FirstT first, @NotNull SecondT second) {
}
