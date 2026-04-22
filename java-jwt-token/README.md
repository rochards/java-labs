# Java JWT Token

This project was created with assistance from OpenAI Codex.

Information about JWT tokens can be found at:
- [Introduction to JSON Web Tokens](https://www.jwt.io/introduction#what-is-json-web-token).
- [RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519).

## Purpose

This project is a small Java lab for learning how JSON Web Tokens (JWT) work in practice.
It focuses on generating, validating, and testing signed tokens using both symmetric and asymmetric signing algorithms.

## Tech Stack

- Java 21.
- Maven.
- JUnit 6.
- [`java-jwt`](https://github.com/auth0/java-jwt) library from Auth0 for creating and verifying JWTs.

## Project Structure

- `src/test/java/com/github/rochards/java_jwt_token/JwtHS256Test.java`: examples and tests for JWT generation, validation, and tampering with the `HS256` algorithm.
- `src/test/java/com/github/rochards/java_jwt_token/JwtRS256Test.java`: examples and tests for JWT generation and validation with the `RS256` algorithm.

## Requirements

To run this project, you need:

- JDK 21 installed.
- Maven installed.

To run the tests:

```bash
mvn test
```
