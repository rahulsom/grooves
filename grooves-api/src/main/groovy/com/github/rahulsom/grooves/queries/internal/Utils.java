package com.github.rahulsom.grooves.queries.internal;

import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by rxs6995 on 4/27/17.
 */
public class Utils {
    public static final Collector<CharSequence, ?, String> JOIN_EVENTS =
            Collectors.joining(",\n    ", "[\n    ", "\n]");
    public static final Collector<CharSequence, ?, String> JOIN_EVENT_IDS =
            Collectors.joining(", ");
}
