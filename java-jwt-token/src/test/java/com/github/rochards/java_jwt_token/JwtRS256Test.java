package com.github.rochards.java_jwt_token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Test examples using asymmetric cryptographic signing algorithms.
 * For these examples I'm using the RS256 signature, that stands for RSASSA-PKCS1-v1_5 with SHA-256.
 * */
class JwtRS256Test {

    private static final String ISSUER = "java-jwt-token";

    @Test
    void shouldGenerateAndValidateRs256Token() throws NoSuchAlgorithmException {
        var keyPair = generateRsaKeyPair();
        var algorithm = Algorithm.RSA256(
                (RSAPublicKey) keyPair.getPublic(),
                (RSAPrivateKey) keyPair.getPrivate()
        );
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

        // The sender signs with the private key, and the receiver verifies with the public key.
        // OAuth servers usually publish these keys at an endpoint such as /.well-known/jwks.json
        var verifier = JWT.require(Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), null))
                .withIssuer(ISSUER)
                .build();

        var decodedJwt = verifier.verify(token);

        System.out.printf("Header: %s\n", new String(Base64.getUrlDecoder().decode(decodedJwt.getHeader())));
        System.out.printf("Payload: %s\n", new String(Base64.getUrlDecoder().decode(decodedJwt.getPayload())));

        assertEquals("peter", decodedJwt.getSubject());
        assertEquals("spiderman", decodedJwt.getClaim("role").asString());
    }

    private KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }
}
