package com.github.rahulsom.grooves.grails;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class QueryUtil {
    static final Map<String, Object> LATEST_BY_POSITION = createMap(new Object[][]{
            {"sort", "lastEventPosition"},
            {"order", "desc"},
            {"offset", 0},
            {"max", 1}
    });
    static final Map<String, Object> INCREMENTAL_BY_POSITION = createMap(new Object[][]{
            {"sort", "position"},
            {"order", "asc"}
    });

    static final Map<String, Object> LATEST_BY_TIMESTAMP = createMap(new Object[][]{
            {"sort", "lastEventTimestamp"},
            {"order", "desc"},
            {"offset", 0},
            {"max", 1}
    });
    static final Map<String, Object> INCREMENTAL_BY_TIMESTAMP = createMap(new Object[][]{
            {"sort", "timestamp"},
            {"order", "asc"}
    });

    private static Map<String, Object> createMap(Object[][] entries) {
        final LinkedHashMap<String, Object> retval = new LinkedHashMap<>();
        for (Object[] row : entries) {
            retval.put((String) row[0], row[1]);
        }
        return Collections.unmodifiableMap(retval);
    }

    private QueryUtil() {
    }
}
