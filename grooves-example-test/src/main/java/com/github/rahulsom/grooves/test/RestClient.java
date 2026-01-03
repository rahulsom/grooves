package com.github.rahulsom.grooves.test;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * A simple REST client that uses OkHttp.
 */
@RequiredArgsConstructor
public class RestClient {

    /**
     * Performs a GET request.
     *
     * @param restRequest The parameters for the request
     * @param <T> The type of response
     *
     * @return The response
     */
    @SneakyThrows
    public <T> HttpResponseDecorator<T> get(final RestRequest restRequest) {
        final var okHttpClient = new OkHttpClient.Builder().build();

        final var queryMap = Optional.ofNullable(restRequest.query()).orElse(Map.of());

        final String query = queryMap.entrySet().stream()
                .map(it -> it.getKey() + "=" + it.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        final var url = baseUrl + restRequest.path() + "?" + query;
        final var request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build();
        final var response = okHttpClient.newCall(request).execute();

        return new HttpResponseDecorator<>(response);
    }

    private final String baseUrl;
}
