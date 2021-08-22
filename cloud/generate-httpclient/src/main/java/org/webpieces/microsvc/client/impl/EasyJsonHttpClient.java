package org.webpieces.microsvc.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.orderlyhealth.api.util.HttpMethod;
import com.orderlyhealth.api.util.SneakyThrow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

@Singleton
public class EasyJsonHttpClient {

    private final ObjectMapper mapper;
    private final HttpClient client;

    @Inject
    public EasyJsonHttpClient(ObjectMapper mapper) {
        this.mapper = mapper;
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Easily send an HTTPS request and receive a String response
     *
     * @param url        The endpoint to hit. Ex: https://api.pagerduty.com/incidents
     * @param headers    A String -> String map of headers. May be null or empty
     * @param parameters A String -> String map of query parameters. May be null or empty
     * @param body       The request body. May be null
     */
    public CompletableFuture<String> sendRequest(HttpMethod method, String url, Map<String, String> headers,
                                                 Map<String, String> parameters, Object body) {

        String jsonBody = null;
        if (body != null) {
            try {
                jsonBody = mapper.writeValueAsString(body);
            } catch (JsonProcessingException e) {
                throw SneakyThrow.sneak(e);
            }
        }

        HttpRequest.BodyPublisher bodyPublisher;
        if (jsonBody != null) {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonBody);
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .method(method.getMethod(), bodyPublisher);

        if (parameters != null && !parameters.isEmpty()) {
            url += convertParams(parameters);
        }
        builder.uri(URI.create(url));

        if (headers != null && !headers.isEmpty()) {
            builder.headers(convertHeaders(headers));
        }

        HttpRequest request = builder.build();
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return future.thenApply(HttpResponse::body); // Return just the body of the response

    }

    /**
     * Easily send an HTTPS request and receive a Jackson JsonNode response. You do the deserialization yourself.
     * This is useful if you only need a few fields of the JSON response, so you don't have to make a whole POJO just to
     * use {@code ObjectMapper::readValue}
     *
     * @param url        The endpoint to hit. Ex: https://api.pagerduty.com/incidents
     * @param headers    A String -> String map of headers. May be null or empty
     * @param parameters A String -> String map of query parameters. May be null or empty
     * @param body       The request body. May be null
     */
    public CompletableFuture<JsonNode> sendJsonRequest(HttpMethod method, String url, Map<String, String> headers,
                                                       Map<String, String> parameters, Object body) {

        CompletableFuture<String> future = sendRequest(method, url, headers, parameters, body);

        return future.thenApply(response -> {
            try {
                return mapper.readTree(response);
            } catch (IOException e) {
                throw SneakyThrow.sneak(e);
            }
        });

    }

    private String convertParams(Map<String, String> map) {
        StringJoiner builder = new StringJoiner("&", "?", "");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = URLEncoder.encode(entry.getKey(), Charsets.UTF_8);
            String value = URLEncoder.encode(entry.getValue(), Charsets.UTF_8);
            builder.add(key + "=" + value);
        }
        return builder.toString();
    }

    private String[] convertHeaders(Map<String, String> map) {
        List<String> headers = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            headers.add(entry.getKey());
            headers.add(entry.getValue());
        }
        return headers.toArray(new String[0]);
    }

}
