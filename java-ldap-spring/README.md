# Java LDAP Spring Lab with OpenLDAP and Docker Compose

This repository is a learning lab that combines a local OpenLDAP server with a Java Maven application that uses Spring LDAP to interact with that directory.

This project was created with assistance from OpenAI Codex.

## Purpose

The goal of this lab is to help understand, in a practical way:

- How an LDAP directory is structured.
- How to bootstrap LDAP data with LDIF files.
- How a Java application connects to LDAP.
- How Spring LDAP can be used to perform common directory operations.
- How read and write LDAP access can be split across different bind users.

This is intentionally a small lab project, not a production-ready application, that's why one can see passwords exposed in some files.

## Tech stack

- Java 21.
- Maven.
- Docker Compose.
- OpenLDAP via the `bitnamilegacy/openldap` image.
- Spring LDAP via the `spring-ldap-core` dependency.

## Project structure

Important files and folders:

- `src/main/java`: Java source code for the LDAP examples.
- `docker-compose/compose.yaml`: Docker Compose definition for the local OpenLDAP service.
- `docker-compose/ldifs/01-bootstrap.ldif`: Bootstrap LDAP data imported on first startup.
- `docs/understanding-the-ldif.md`: Detailed notes about the bootstrap LDIF

## Prerequisites

To run this lab locally, you should have:

- Java 21 installed.
- Maven installed.
- Docker installed.
- Docker Compose available through `docker compose`

## Run sequence

The recommended sequence is:

1. Start the LDAP service

```bash
docker compose -f docker-compose/compose.yaml up -d
```

2. Confirm the container is up

```bash
docker compose -f docker-compose/compose.yaml logs -f
```

3. Run the Java application

```bash
mvn compile exec:java -Dexec.mainClass="com.github.rochards.java_ldap_spring.JavaLdapSpringApplication"
```

If your Maven setup does not include the `exec` plugin, you can also compile and run the application in whatever way you prefer from your IDE.

## Stop the lab

Stop and remove the container, but keep the named volume with LDAP data:

```bash
docker compose -f docker-compose/compose.yaml down
```

## Reset the lab

Stop the container, remove it, and also delete the named volume used to persist LDAP data:

```bash
docker compose -f docker-compose/compose.yaml down -v
```

Use this when you want a clean restart and need the bootstrap LDIF files to be imported again.

## What you can see in this lab

Without going into implementation details, this project demonstrates LDAP operations such as:

- reading all people entries
- reading all group entries
- finding a person by `uid`
- finding a group by `cn`
- finding groups for a specific person
- creating a new LDAP person entry
- deleting a person entry

It also demonstrates the idea of using:

- a read-only LDAP template bound as a service account
- a write LDAP template bound as an admin or writer account

## Spring usage

The Java side of this project uses Spring LDAP through the `spring-ldap-core` dependency declared in `pom.xml`.

This gives the project access to classes such as:

- `LdapTemplate`
- `LdapContextSource`
- LDAP query builders and mappers used by the repositories

## Notes

- LDAP listens on `ldap://localhost:1389`
- Admin bind DN (Distinguished Name): `cn=admin,dc=example,dc=org`
- Admin password: `adminpassword`
- Service account DN: `uid=app-reader,ou=system,dc=example,dc=org`
- Service account password: `app-reader-password`
- Sample user DN: `uid=alice,ou=people,dc=example,dc=org`
- Sample user password: `alice123`
- User search example: `ldapsearch -x -H ldap://localhost:1389 -D "uid=app-reader,ou=system,dc=example,dc=org" -w app-reader-password -b "ou=people,dc=example,dc=org" "(uid=alice)"`
- Group search example: `ldapsearch -x -H ldap://localhost:1389 -D "uid=app-reader,ou=system,dc=example,dc=org" -w app-reader-password -b "ou=groups,dc=example,dc=org" "(member=uid=alice,ou=people,dc=example,dc=org)"`

### Warning

This learning setup does not define production-grade LDAP access controls. Depending on the server defaults, a regular authenticated user may be able to read much more of the directory than would be acceptable in a real application, potentially including broad access to people and group information.

That is acceptable for this repository because it is only a learning project focused on understanding LDAP structure, authentication flow, and Spring LDAP integration.

For a production-ready application, you should define appropriate LDAP ACLs so users and service accounts have only the minimum access they need.

## Understanding the LDIF file

Detailed notes about the bootstrap LDIF, the sample directory tree, and the schema choices are available in [docs/understanding-the-ldif.md](docs/understanding-the-ldif.md).
