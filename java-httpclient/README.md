# Java HttpClient Notes

This repository is a learning lab to understand how `java.net.http.HttpClient` works in practice and how to handle common HTTP communication problems.

This project was created with assistance from OpenAI Codex.

## Purpose
The focus is on learning through tests, especially around:
- Successful HTTP calls.
- Non-`200` responses.
- Timeouts.
- Connection failures.
- Connection resets.
- HTTPS/TLS handshake failures.
- Insecure and proper ways of trusting certificates.

## Tech Stack

- Java 21.
- Maven.
- WireMock.
- JUnit Jupiter.
- Jackson Databind.

## Project Structure

Main classes:

- `Meeting.java`.
- `MeetingManagementService.java`: service that builds an `HttpRequest`, sends it with `HttpClient`, and maps the response body to `Meeting`.

Test classes:

- `MeetingManagementServiceTest.java`: HTTP tests over plain WireMock.
- `SSLMeetingManagementServiceTest.java`: HTTPS/TLS tests using WireMock with a local certificate.

### Java Tests

#### `MeetingManagementServiceTest`

- `shouldFetchMeetingByIdWithSuccess`
  - Purpose: validates a normal successful `GET` request returning JSON and mapping it into `Meeting`.

- `shouldHandleHttp404StatusAndReturnEmptyWhenFetchingMeetingById`
  - Purpose: shows how the current service behaves when the server responds with `404`.

- `shouldThrowHttpTimeoutExceptionWhenFetchingMeetingById`
  - Purpose: simulates a slow server response using WireMock fixed delay and validates the request timeout behavior.

- `shouldThrowConnectionExceptionWhenFetchingMeetingById`
  - Purpose: shows what happens when the client tries to connect to an endpoint where no server is listening.

- `shouldThrowIOExceptionWhenConnectionsResetsWhenFetchingMeetingById`
  - Purpose: simulates a connection reset at the TCP level with WireMock fault injection.
  - **Note**: WireMock documents that `CONNECTION_RESET_BY_PEER` works mainly on Unix-like systems and may behave differently on Windows.

#### `SSLMeetingManagementServiceTest`

**Note**: These tests require the local PKCS#12 keystore file described below.

- `shouldThrowSSLHandshakeExceptionWhenFetchingMeetingById`
  - Purpose: shows the default Java HTTPS client rejecting a self-signed certificate that it does not trust.

- `shouldFetchMeetingByIdIgnoringSSLWithSuccess`
  - Purpose: shows the insecure workaround where a custom trust manager accepts the certificate at runtime.
  - **Note**: this is useful for learning only and it's not recommended for real applications.

- `shouldFetchMeetingByIdWithTrustedHttpsCertificate`
  - Purpose: shows the proper HTTPS success path by building an `SSLContext` from a truststore that contains the WireMock certificate.

**Prerequisite for all SSL tests**:

Generate the expected local certificate file first. Command:
```bash
keytool -genkeypair \
  -alias wiremock-local \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650 \
  -storetype PKCS12 \
  -keystore src/test/resources/wiremock-localhost.p12 \
  -storepass password \
  -keypass password \
  -dname "CN=localhost, OU=Dev, O=Local, L=Local, ST=Local, C=BR" \
  -ext "SAN=DNS:localhost,IP:127.0.0.1"
```

If you change the certificate file path, type, or password, update these values in `SSLMeetingManagementServiceTest`:

- `DEFAULT_KEYSTORE_PASSWORD`
- `DEFAULT_KEYSTORE_PATH`
- `DEFAULT_KEYSTORE_TYPE`

## Notes

### `.p12` Files

`.p12` is the PKCS#12 keystore format. In this project, the file is used to store:

- The private key used by WireMock for HTTPS.
- The self-signed certificate presented by WireMock.
- Certificate metadata such as subject and SAN entries.

Because it can contain a private key, it should stay local and should not be committed to a public repository.

### `HttpClient` vs `HttpRequest`

- `HttpRequest` is only a description of the request: URI, method, headers, timeout, and body.
- `HttpClient` performs the real I/O: opening or reusing connections, doing TCP/TLS handshakes, sending bytes, and reading the response.

So creating an `HttpRequest` does not send anything yet. The network communication starts only when code calls `httpClient.send(...)`.

### Usual Request Flow

When `MeetingManagementService.fetchById(...)` runs, the usual flow is:

```text
MeetingManagementService
    -> builds HttpRequest
    -> calls HttpClient.send(...)
    -> DNS lookup if needed
    -> connection reuse or new TCP handshake
    -> TLS handshake if HTTPS
    -> HTTP request is sent
    -> server sends HTTP response
    -> HttpClient returns HttpResponse<String>
    -> Jackson maps the body into Meeting
```

Compact view:

```text
Service -> HttpRequest -> HttpClient -> TCP/TLS -> Server -> HttpResponse -> Meeting
```

### Production-Style Certificate Trust

In real applications, it is common to avoid writing runtime trust-manager code and instead configure trust externally.

The usual production-style approach is:

1. Export or obtain the server certificate or, preferably, the CA certificate.
2. Import it into a truststore.
3. Start the JVM with truststore properties.
4. Use `HttpClient.newHttpClient()` normally.

Example import command:

```bash
keytool -importcert \
  -alias my-server-ca \
  -file ca-cert.pem \
  -keystore truststore.p12 \
  -storetype PKCS12 \
  -storepass changeit \
  -noprompt
```

Example JVM startup:

```bash
java \
  -Djavax.net.ssl.trustStore=/path/to/truststore.p12 \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -Djavax.net.ssl.trustStoreType=PKCS12 \
  -jar app.jar
```

Important point:

- When `javax.net.ssl.trustStore` is set, the JVM uses that truststore instead of the default JDK `cacerts`.
- If you need both public CAs and internal certificates, the truststore must contain both.

In production, trusting the issuing CA is usually better than trusting one leaf certificate directly.
