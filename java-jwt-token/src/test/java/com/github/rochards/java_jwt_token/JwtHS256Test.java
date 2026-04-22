package com.github.rochards.java_jwt_token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
* Test examples using symmetric cryptographic signing algorithms.
* For these examples I'm using the HS256 signature, that stands for HMAC SHA-256.
* */
class JwtHS256Test {

    private static final String SECRET = "my-super-secret-key";
    private static final String ISSUER = "java-jwt-token";

    @Test
    void shouldGenerateAndValidateHs256Token() {
        var algorithm = Algorithm.HMAC256(SECRET);
        var now = Instant.now();

        var token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("peter")
                .withAudience("my-api")
                .withClaim("role", "spiderman")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(3600)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(algorithm);

        System.out.println("Generated token: " + token);


        // That's how a receiver would verify the received token.
        // A challenge of this kind of cryptography is that the sender must find a way to share the used secret with the receiver
        var verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();

        var decodedJwt = verifier.verify(token);

        System.out.printf("Header: %s\n", new String(Base64.getUrlDecoder().decode(decodedJwt.getHeader())));
        System.out.printf("Payload: %s\n", new String(Base64.getUrlDecoder().decode(decodedJwt.getPayload())));


        assertEquals("peter", decodedJwt.getSubject());
        assertEquals("spiderman", decodedJwt.getClaim("role").asString());
    }

    @Test
    void shouldRejectATamperedToken() {
        // Simulating an attempt to tamper with the token's role by adding 'admin'.

        var algorithm = Algorithm.HMAC256(SECRET);
        var issuedAt = Instant.now();
        var expiresIn = issuedAt.plusSeconds(3600);

        var token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("peter")
                .withClaim("role", "spiderman")
                .withIssuedAt(Date.from(issuedAt))
                .withExpiresAt(Date.from(expiresIn))
                .sign(algorithm);
        var tamperedToken = getTamperedToken(token);

        System.out.println("Generated token: " + token);
        System.out.println("Tampered token: " + tamperedToken);


        // Verifying the received token
        var verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();

        var message = assertThrows(SignatureVerificationException.class, () -> verifier.verify(tamperedToken));
        System.out.println("Error message: " + message.getMessage());
    }

    private String getTamperedToken(String token) {
        var decoded = JWT.decode(token);
        var issuedAt = decoded.getIssuedAt().toInstant().getEpochSecond();
        var expiresIn = decoded.getExpiresAt().toInstant().getEpochSecond();

        // Simulating an attempt to tamper with the token's role by adding 'admin'.
        var tamperedPayload = String.format(
                "{\"iss\":\"java-jwt-token\",\"sub\":\"peter\",\"role\":\"spiderman, admin\",\"iat\":%d,\"exp\":%d}",
                issuedAt,
                expiresIn
        );
        var tamperedEncodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tamperedPayload.getBytes(StandardCharsets.UTF_8));

        var parts = token.split("\\.");
        return String.format("%s.%s.%s", parts[0], tamperedEncodedPayload, parts[2]);
    }
}
