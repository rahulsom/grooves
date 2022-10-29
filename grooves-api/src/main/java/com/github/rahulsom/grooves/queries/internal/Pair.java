package com.github.rahulsom.grooves.queries.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable class that can store two objects.
 *
 * @param <FirstT>  Type of first object
 * @param <SecondT> Type of second object
 *
 * @author Rahul Somasunderam
 */
@RequiredArgsConstructor
@Getter
public class Pair<FirstT, SecondT> {
    @NotNull private final FirstT first;
    @NotNull private final SecondT second;
}
