package com.github.rahulsom.grooves.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Response;

/**
 * Decorates a Response from OkHttp with some useful methods.
 */
public record HttpResponseDecorator<T>(Response response, TypeReference<T> typeReference) {

    public HttpResponseDecorator(Response response) {
        this(response, new TypeReference<>() {});
    }

    /**
     * Returns the response body as a Map.
     *
     * @return a map of the parsed json
     */
    public T getData() {
        return Optional.ofNullable(response.body())
                .map(it -> {
                    try {
                        return it.string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(it -> {
                    try {
                        return new ObjectMapper().readValue(it, typeReference);
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
