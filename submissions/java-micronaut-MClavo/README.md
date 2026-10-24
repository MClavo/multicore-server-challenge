# Java Micronaut Conduit

Initial implementation for the Multicore Server Challenge.

- Language: Java 25
- Framework: Micronaut 5.2.3
- HTTP server: Netty
- Database: PostgreSQL 18
- Driver: PostgreSQL JDBC
- Connection pool: HikariCP
- Persistence: Manual SQL with JDBC
- Runtime: HotSpot JVM

Front: nginx

## Architecture

Initial implementation using blocking JDBC operations
executed outside Netty's event loops.

## Build

bash build.sh

## Start

DATABASE_URL=postgres://conduit:conduit@127.0.0.1:5432/conduit_dev \
JWT_SECRET=multicore-server-challenge \
HOST=127.0.0.1 \
PORT=3000 \
bash start.sh

## Optimizations

None yet. Establish correctness before performance tuning.