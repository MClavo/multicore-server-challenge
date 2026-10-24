# Local Spring Boot observability

Opt-in diagnostics for the native Java 25 / Spring Boot application and native PostgreSQL.
Requires Docker Engine with Compose on Fedora Linux, Java 25, and the existing database.
Prometheus `v3.13.4` and Grafana OSS `13.2.2` are pinned official images.

## Start Spring Boot

From the repository root, build without running tests, then start in the foreground:

```sh
cd submissions/java-springboot-mclavo
bash build.sh
SPRING_PROFILES_ACTIVE=observe HOST=127.0.0.1 PORT=3110 \
  DATABASE_URL=postgres://conduit:conduit@127.0.0.1:5432/conduit_dev \
  JWT_SECRET=multicore-server-challenge bash start.sh
```

Use your existing database credentials and JWT secret if they differ. The application API
listens on `127.0.0.1:3110`; only health and Prometheus endpoints are exposed on the separate
`127.0.0.1:3111` management listener. A profile-specific security chain allows those
endpoints without credentials; existing API Token authentication stays in its original chain.

## Start and stop the monitoring stack

In another terminal, from the repository root:

```sh
cd submissions/java-springboot-mclavo/observability
docker compose up -d
```

- Grafana: <http://localhost:3001> — initial login **admin / admin**; set a new password
  when prompted. The password is stored in the Grafana volume.
- Provisioned dashboard: <http://localhost:3001/d/spring-boot-performance>
  (**Spring Boot Performance**). Prometheus is already the default datasource.
- Prometheus: <http://localhost:9090>.
- Scrape target status: <http://localhost:9090/targets> (job `spring-boot`).
- Management health: <http://127.0.0.1:3111/actuator/health>.
- Raw metrics: <http://127.0.0.1:3111/actuator/prometheus>.

Stop and remove the containers, keeping both data volumes:

```sh
docker compose down
```

Stop Spring Boot with Ctrl+C in its terminal. Prometheus retains seven days of samples.
Named volumes preserve metrics and Grafana state across Compose restarts. Run
`docker compose down -v` only if you intend to delete both volumes, including the Grafana
password and metrics history. To inspect startup problems, use `docker compose logs`.

## Networking and Fedora SELinux

Both containers use Linux host networking. Prometheus can therefore scrape the native
application at `127.0.0.1:3111`, and Grafana connects to Prometheus at `127.0.0.1:9090`.
The application binding needs no change. Prometheus explicitly binds `127.0.0.1:9090`
and Grafana explicitly binds `127.0.0.1:3001`; there are no Compose published ports.
Ports 3001, 3111, and 9090 must be free. This configuration targets local Docker Engine
on Linux rather than an isolated rootless network or a remote Docker daemon.

Configuration/dashboard bind mounts use `:ro,Z`: read-only with a private SELinux relabel
for their container. Only the dedicated configuration paths are relabeled. Docker manages
the writable named volumes. Keep SELinux enforcing; no privileged containers, host-wide
relabeling, firewall changes, or additional services are needed. Host networking lets these
containers reach other host-local services, so keep this stack strictly for local use.

## Metrics and interpretation

Prometheus scrapes every five seconds with a four-second timeout. Grafana refreshes every
five seconds and uses `$__rate_interval` with a five-second datasource interval; allow
several scrapes before interpreting rates. The HTTP panels exclude `/actuator` traffic.
HTTP p50/p95/p99 and Hikari acquisition p95 use classic histogram buckets enabled only
in `observe`. Histogram quantiles are estimates; no completed events produces undefined
quantiles, not a zero-latency observation.

Tomcat uses its built-in MBean registry; connector series stay separate so management
threads/connections are not combined with API connectors. Open connections include idle
keep-alive sockets. HikariCP panels use `hikaricp_connections_*` and its acquisition timer;
heap, CPU, GC and thread panels use standard JVM/process meters. GC pause events and time
describe pauses, not all concurrent collector activity. Live threads counts platform threads.
No custom meters or Hibernate statistics are enabled.

Missing/unsupported meters remain **No data** or gaps. HTTP and Hikari timers require
requests/acquisitions; GC pause series may appear only after GC. HTTP error percentages
also show no series until that status class has occurred, and are undefined during idle
windows. No error series alone does not prove zero errors: check scrape availability and
request traffic. Queries do not substitute zeros for missing series or divide-by-zero results.

## Normal benchmarks

Leave `SPRING_PROFILES_ACTIVE` unset (and stop Compose) for normal benchmark execution.
The base configuration disables management HTTP, endpoint access, metric exports, meters,
and observations. Tomcat's MBean registry and histograms are enabled only in `observe`.
The Actuator runtime starter is included because the existing Actuator test starter alone
does not provide production instrumentation. Versions come from Spring Boot dependency
management. The benchmark build/start/install scripts never launch monitoring services.
The observe profile has measurement overhead and should not be used for official results.

References: [Spring Boot metrics](https://docs.spring.io/spring-boot/reference/actuator/metrics.html),
[Actuator endpoint security](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html),
[Docker host networking](https://docs.docker.com/engine/network/drivers/host/),
[SELinux bind mounts](https://docs.docker.com/engine/storage/bind-mounts/#configure-the-selinux-label),
and [Grafana provisioning](https://grafana.com/docs/grafana/latest/administration/provisioning/).
