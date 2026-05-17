package com.github.rochards.java_httpclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class MeetingManagementServiceTest {

    private WireMockServer wireMockServer;
    private MeetingManagementService meetingManagementService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();

        meetingManagementService = new MeetingManagementService(
                HttpClient.newHttpClient(),
                URI.create(wireMockServer.baseUrl()),
                new ObjectMapper()
        );
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldFetchMeetingByIdWithSuccess() throws IOException, InterruptedException {
        wireMockServer.stubFor(
                get(urlEqualTo("/v1/meetings/meeting-123"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                          "title": "Platform sync",
                                          "description": "Discuss service updates"
                                        }
                                        """)
                        )
        );

        var optionalMeeting = meetingManagementService.fetchById("meeting-123");

        assertTrue(optionalMeeting.isPresent());
        var meeting = optionalMeeting.get();
        assertEquals("Platform sync", meeting.title());
        assertEquals("Discuss service updates", meeting.description());

        wireMockServer.verify(getRequestedFor(urlEqualTo("/v1/meetings/meeting-123")));
    }

    @Test
    void shouldHandleHttp404StatusAndReturnEmptyWhenFetchingMeetingById() throws IOException, InterruptedException {
        wireMockServer.stubFor(
                get(urlEqualTo("/v1/meetings/meeting-123"))
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                        )
        );

        var optionalMeeting = meetingManagementService.fetchById("meeting-123");

        assertTrue(optionalMeeting.isEmpty());
    }

    @Test
    void shouldThrowHttpTimeoutExceptionWhenFetchingMeetingById() {
        wireMockServer.stubFor(
                get(urlEqualTo("/v1/meetings/meeting-123"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withFixedDelay(3500)
                                .withBody("""
                                        {
                                          "title": "Platform sync",
                                          "description": "Discuss service updates"
                                        }
                                        """)
                        )
        );

        assertThrows(HttpTimeoutException.class, () -> meetingManagementService.fetchById("meeting-123"));
    }

    @Test
    void shouldThrowConnectionExceptionWhenFetchingMeetingById() {
        meetingManagementService = new MeetingManagementService(
                HttpClient.newHttpClient(),
                URI.create("https://localhost"), // not mocked, so host does not respond
                new ObjectMapper()
        );

        assertThrows(ConnectException.class, () -> meetingManagementService.fetchById("meeting-123"));
    }

    @Test
    void shouldThrowIOExceptionWhenConnectionsResetsWhenFetchingMeetingById() {
        /*
         * WireMock fault simulation docs: https://wiremock.org/docs/simulating-faults/
         * Important caveat from the docs:
         *   - this works properly mainly on Unix-like systems
         *   - on Windows it may hang instead of producing a reset
         * */
        wireMockServer.stubFor(
                get(urlEqualTo("/v1/meetings/meeting-123"))
                        .willReturn(aResponse()
                                .withFault(Fault.CONNECTION_RESET_BY_PEER)
                        )
        );

        assertThrows(IOException.class, () -> meetingManagementService.fetchById("meeting-123"));
    }
}
