package com.github.rochards.java_httpclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SSLMeetingManagementServiceTest {

    public static final String DEFAULT_KEYSTORE_PASSWORD = "password";
    public static final String DEFAULT_KEYSTORE_PATH = "src/test/resources/wiremock-localhost.p12";
    public static final String DEFAULT_KEYSTORE_TYPE = "PKCS12";

    private WireMockServer wireMockServer;
    private MeetingManagementService meetingManagementService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(
                wireMockConfig()
                        .httpsPort(0)
                        .keystoreType(DEFAULT_KEYSTORE_TYPE)
                        .keystorePath(DEFAULT_KEYSTORE_PATH)
                        .keystorePassword(DEFAULT_KEYSTORE_PASSWORD)
                        .keyManagerPassword(DEFAULT_KEYSTORE_PASSWORD)
        );
        wireMockServer.start();

        meetingManagementService = new MeetingManagementService(
                HttpClient.newHttpClient(),
                URI.create("https://localhost:" + wireMockServer.httpsPort()),
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
    void shouldThrowSSLHandshakeExceptionWhenFetchingMeetingById() {
        /*
         * That's a pretty common error when trying to establish a HTTPs connection when Java does not recognize the
         * presented certificate by the server. I'm presenting a self-signed TLS certificate set in the setUp method,
         * and the default Java client does not trust it.
         *
         * WireMock Https docs: https://wiremock.org/docs/https/
         * */
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

        assertThrows(SSLHandshakeException.class, () -> meetingManagementService.fetchById("meeting-123"));
    }

    @Test
    void shouldFetchMeetingByIdIgnoringSSLWithSuccess() throws NoSuchAlgorithmException, IOException, InterruptedException, KeyManagementException {
        // Developers often do this to avoid SSL handshake errors, but it is not recommended outside local tests.
        var trustManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        var httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();


        meetingManagementService = new MeetingManagementService(
                httpClient,
                URI.create("https://localhost:" + wireMockServer.httpsPort()),
                new ObjectMapper()
        );


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
    }

    @Test
    void shouldFetchMeetingByIdWithTrustedHttpsCertificate() throws Exception {
        // Create a PKCS12 truststore instance for the certificate we want the client to trust.
        var trustStore = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
        try (var input = Files.newInputStream(Paths.get(DEFAULT_KEYSTORE_PATH))) {
            trustStore.load(input, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
        }

        // TrustManagerFactory creates the trust managers that decide which server certificates the client accepts.
        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        /*
        SSLContext holds the TLS configuration the HttpClient will use for secure connections.

        Here it is initialized with the trust managers built from our truststore.
        First null: no custom client key managers are configured.
        Second argument: trust managers that validate the server certificate.
        Final null: use the default source of randomness for TLS operations.
        */
        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // Build an HttpClient that uses the custom SSL configuration.
        var httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();

        // Use the HTTPS WireMock endpoint with the client that now trusts its certificate.
        meetingManagementService = new MeetingManagementService(
                httpClient,
                URI.create("https://localhost:" + wireMockServer.httpsPort()),
                new ObjectMapper()
        );

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
                                      """))
        );

        var optionalMeeting = meetingManagementService.fetchById("meeting-123");

        assertTrue(optionalMeeting.isPresent());
    }
}
