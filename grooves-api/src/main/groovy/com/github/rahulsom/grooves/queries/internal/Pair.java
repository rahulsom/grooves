package com.github.rahulsom.grooves.queries.internal;

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

    public <F extends FirstT, S extends SecondT> Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public FirstT getFirst() {
        return first;
    }

    public SecondT getSecond() {
        return second;
    }
}
