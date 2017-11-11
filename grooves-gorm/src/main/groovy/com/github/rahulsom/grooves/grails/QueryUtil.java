package com.github.rahulsom.grooves.grails;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

class QueryUtil {
    protected static final String SNAPSHOTS_BY_POSITION =
            "findAllByAggregateIdAndLastEventPositionLessThan";
    protected static final String SNAPSHOTS_BY_TIMETTAMP =
            "findAllByAggregateIdAndLastEventTimestampLessThan";
    protected static final String SNAPSHOTS_BY_AGGREGATE =
            "findAllByAggregateId";
    protected static final String UNCOMPUTED_EVENTS_BY_VERSION =
            "findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals";
    protected static final String UNCOMPUTED_EVENTS_BEFORE_DATE =
            "findAllByAggregateAndTimestampLessThanEquals";
    protected static final String UNCOMPUTED_EVENTS_BY_DATE_RANGE =
            "findAllByAggregateAndTimestampGreaterThanAndTimestampLessThanEquals";

    protected static final Map<String, Object> LATEST_BY_POSITION = createMap(new Object[][]{
            {"sort", "lastEventPosition"},
            {"order", "desc"},
            {"offset", 0},
            {"max", 1}
    });
    protected static final Map<String, Object> INCREMENTAL_BY_POSITION = createMap(new Object[][]{
            {"sort", "position"},
            {"order", "asc"}
    });

    protected static final Map<String, Object> LATEST_BY_TIMESTAMP = createMap(new Object[][]{
            {"sort", "lastEventTimestamp"},
            {"order", "desc"},
            {"offset", 0},
            {"max", 1}
    });
    protected static final Map<String, Object> INCREMENTAL_BY_TIMESTAMP = createMap(new Object[][]{
            {"sort", "timestamp"},
            {"order", "asc"}
    });

    private QueryUtil() {
    }

    private static Map<String, Object> createMap(Object[][] entries) {
        final Map<String, Object> retval = new LinkedHashMap<>();
        for (Object[] row : entries) {
            retval.put((String) row[0], row[1]);
        }
        return unmodifiableMap(retval);
    }
}
