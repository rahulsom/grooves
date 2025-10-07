package com.github.rahulsom.grooves.test;

import java.util.Map;

/**
 * A request to the REST API.
 */
public record RestRequest(String path, Map<String, Object> query) {
    /**
     * Creates a new RestRequest.
     *
     * @param path The path to the resource
     */
    public RestRequest(String path) {
        this(path, Map.of());
    }

}
