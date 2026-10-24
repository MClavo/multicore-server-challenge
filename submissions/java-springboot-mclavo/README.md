# Conduit — Spring Boot

Front: nginx

Java 25 (installer: Eclipse Temurin 25.0.5+8), Spring Boot 4.1.2, Gradle 9.8.2,
Spring MVC with embedded Tomcat, Spring Security, Bean Validation, Spring Data JPA,
Hibernate, HikariCP and the PostgreSQL JDBC driver. Spring Boot's dependency management
supplies library versions. Requires the challenge's existing PostgreSQL 18 schema and seed.

## Running

`install.sh` installs the pinned JDK on Ubuntu 24.04 when run as root. An existing Java 25
installation also works; the Gradle wrapper supplies Gradle.

```sh
bash build.sh
DATABASE_URL=postgres://conduit:conduit@127.0.0.1:5432/conduit_dev \
JWT_SECRET=multicore-server-challenge HOST=127.0.0.1 PORT=3000 bash start.sh
```

The build produces `build/libs/conduit.jar` without running tests. The start script runs
the server in the foreground. `config/start.sh` delegates to the root start script for
compatibility with the original scaffold.

| Variable | Default |
| --- | --- |
| `DATABASE_URL` | `postgres://conduit:conduit@127.0.0.1:5432/conduit_dev` |
| `HOST` | `127.0.0.1` |
| `PORT` | `3000` |
| `JWT_SECRET` | `multicore-server-challenge` |

`start.sh` converts `DATABASE_URL` into Spring Boot's standard `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` environment variables.
Database URIs accept both `postgres://` and `postgresql://`, percent-encoded credentials,
IPv6 hosts and JDBC-compatible query parameters. Explicitly supplied Spring datasource
environment variables take precedence. When the URI omits credentials, or `DATABASE_URL`
is unset, the corresponding defaults come from `application.yml`. Launching the JAR
directly uses standard Spring datasource properties rather than converting `DATABASE_URL`.

## Implementation

For opt-in local Prometheus/Grafana diagnostics, see
[observability/README.md](observability/README.md). It documents the `observe` profile,
loopback-only management listener on port 3111, monitoring startup/shutdown commands,
and the provisioned **Spring Boot Performance** dashboard at <http://localhost:3001>.
Leave the profile unset and stop the monitoring stack for normal benchmarks.

Features live under `dev.mclavo.conduit`: users, profiles, articles, comments and tags each
have controllers, transactional services, Spring Data repositories and explicit DTOs.
Security, configuration and shared error/validation helpers have separate packages.
All persistence uses JPA mappings, repository methods and JPQL; there is no custom SQL.

The API implements registration, login, user updates, profiles, follows, article CRUD,
global and filtered lists, personal feeds, favorites, comments and sorted tags. It uses
the benchmark's filter precedence, count cap of 1,000, newest-first ordering, arbitrary
offset pagination, newest 100 comments, exact response fields and UTC millisecond timestamps.
Request validation rejects type coercion and enforces bcrypt's 72-byte password limit.
Jackson's ordinary deserialization settings live in `application.yml`; the sole mapper
customizer rejects number/boolean conversions to strings, including tag-list elements.
The shared `@RestControllerAdvice` maps malformed JSON, invalid types and Bean Validation
failures to HTTP 422 with the benchmark's `errors.body` response shape.
Passwords use bcrypt cost 10. The stateless security chain uses `Authorization: Token <jwt>`;
HS256 signatures and expiration are checked on every authenticated request. Public signup,
login and tags ignore authentication headers. A small JCA-backed Spring Security JWT decoder
supports the contract's literal secret, including secrets shorter than Nimbus's minimum size.

Article creation, tags and tag associations commit atomically. An identity-generated article
receives its final title-and-id slug in that transaction, then its response is read after commit.
A tag uniqueness race retries the complete transaction, up to five attempts. Follows and
favorites serialize each viewer's relationship writes using a database row lock for idempotency.
Article and comment mutations enforce ownership; existing database foreign keys handle deletes.

Hibernate validates the existing schema and never creates or changes it. SQL initialization
is disabled, `spring.jpa.open-in-view=false`, and Hibernate result/second-level caches are
disabled. Spring Boot autoconfigures the datasource and HikariCP with its default
10-connection pool, below the challenge limit;
Tomcat keeps idle connections open for 70 seconds for Nginx. No performance-specific caching,
native SQL, reactive stack or custom connection/thread tuning is used.

## Validation status

The observability changes passed Java compilation (`./gradlew --offline --no-daemon compileJava`),
Docker Compose configuration validation, YAML/JSON syntax checks, and `git diff --check`.
No tests or benchmarks were created or run, and no application or monitoring services were
started. The pre-existing scaffold test was left untouched. Live scraping, dashboard rendering,
conformance and measurements remain unverified.
