package com.github.rahulsom.grooves.test;

import groovy.json.JsonSlurper;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;

import java.io.IOException;
import java.util.Optional;

/**
 * Decorates a Response from OkHttp with some useful methods.
 */
@RequiredArgsConstructor
public class HttpResponseDecorator {

    /**
     * Returns the response body as a Map.
     * @return a map of the parsed json
     */
    public Object getData() {
        return Optional.ofNullable(response.body())
                .map(it -> {
                    try {
                        return it.string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(it -> new JsonSlurper().parseText(it))
                .orElse(null);
    }

    /**
     * Returns the status code of the response.
     * @return the status code
     */
    public int getStatus() {
        return response.code();
    }

    private final Response response;
}
