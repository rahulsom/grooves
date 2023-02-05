package com.github.rahulsom.grooves.test;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * A request to the REST API.
 */
@RequiredArgsConstructor
@Getter
public class RestRequest {
    /**
     * Creates a new RestRequest.
     *
     * @param path The path to the resource
     */
    public RestRequest(String path) {
        this(path, Map.of());
    }

    private final String path;
    private final Map<String, Object> query;
}
