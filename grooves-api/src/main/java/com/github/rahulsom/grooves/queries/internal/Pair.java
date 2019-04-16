package com.github.rahulsom.grooves.queries.internal;

import org.jetbrains.annotations.NotNull;

/**
 * Immutable class that can store two objects.
 *
 * @param <FirstT>  Type of first object
 * @param <SecondT> Type of second object
 *
 * @author Rahul Somasunderam
 */
public class Pair<FirstT, SecondT> {
    private final FirstT first;
    private final SecondT second;

    public Pair(@NotNull FirstT first, @NotNull SecondT second) {
        this.first = first;
        this.second = second;
    }

    @NotNull public FirstT getFirst() {
        return first;
    }

    @NotNull public SecondT getSecond() {
        return second;
    }
}
