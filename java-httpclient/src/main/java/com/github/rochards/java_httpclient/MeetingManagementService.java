package com.github.rochards.java_httpclient;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeetingManagementService {

    private static final String API_PATH = "/v1/meetings/";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    private static final Logger LOGGER = Logger.getLogger(MeetingManagementService.class.getName());

    private final HttpClient httpClient;
    private final URI baseUri;
    private final ObjectMapper objectMapper;

    public MeetingManagementService(HttpClient httpClient, URI baseUri, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.baseUri = Objects.requireNonNull(baseUri, "baseUri must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public Optional<Meeting> fetchById(String id) throws InterruptedException, IOException {
        Objects.requireNonNull(id, "id must not be null");

        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUri + API_PATH + id))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.log(Level.WARNING, String.valueOf(response.statusCode()));

                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(response.body(), Meeting.class));
        } catch (InterruptedException ex) {
            // Restore the interrupt flag so higher-level code can still detect the cancellation signal.
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Request was interrupted for " + request.uri(), ex);
            throw ex;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "I/O error while calling " + request.uri(), ex);
            throw ex;
        }
    }
}
