package com.github.rahulsom.grooves.test;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;
import java.util.Optional;

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
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Map<String, Object> queryMap = Optional.ofNullable(restRequest.getQuery()).orElse(Map.of());

        final String query = queryMap.entrySet().stream()
                .map(it -> it.getKey() + "=" + it.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        String url = baseUrl + restRequest.getPath() + "?" + query;
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build();
        Response response = okHttpClient.newCall(request).execute();

        return new HttpResponseDecorator<>(response);
    }

    private final String baseUrl;

}
