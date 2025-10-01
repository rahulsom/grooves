package com.github.rahulsom.grooves.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;

import java.io.IOException;
import java.util.Optional;

/**
 * Decorates a Response from OkHttp with some useful methods.
 */
public record HttpResponseDecorator<T>(Response response) {

    /**
     * Returns the response body as a Map.
     *
     * @return a map of the parsed json
     */
    public T getData() {
        return (T) Optional.ofNullable(response.body())
                .map(it -> {
                    try {
                        return it.string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(it -> {
                    try {
                        return new ObjectMapper().readValue(it, Object.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);
    }

    /**
     * Returns the status code of the response.
     *
     * @return the status code
     */
    public int getStatus() {
        return response.code();
    }

}
