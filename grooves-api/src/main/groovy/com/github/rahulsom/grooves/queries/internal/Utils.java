package com.github.rahulsom.grooves.queries.internal;

import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utility objects and methods to help with Queries.
 *
 * @author Rahul Somasunderam
 */
public class Utils {
    private Utils() {
    }

    public static final Collector<CharSequence, ?, String> JOIN_EVENTS =
            Collectors.joining(",\n    ", "[\n    ", "\n]");
    public static final Collector<CharSequence, ?, String> JOIN_EVENT_IDS =
            Collectors.joining(", ");
}
