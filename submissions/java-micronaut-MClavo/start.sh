#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

: "${DATABASE_URL:?DATABASE_URL is required}"

if [[ "$DATABASE_URL" =~ ^postgres(ql)?://([^:]+):([^@]+)@([^:/]+):([0-9]+)/([^?]+)$ ]]; then
    export DB_USERNAME="${BASH_REMATCH[2]}"
    export DB_PASSWORD="${BASH_REMATCH[3]}"
    export JDBC_URL="jdbc:postgresql://${BASH_REMATCH[4]}:${BASH_REMATCH[5]}/${BASH_REMATCH[6]}"
else
    echo "Unsupported DATABASE_URL format" >&2
    exit 1
fi

JAR=$(find build/libs -maxdepth 1 -name '*-all.jar' -print -quit)

if [[ -z "$JAR" ]]; then
    echo "Application JAR not found. Run build.sh first." >&2
    exit 1
fi

exec java -jar "$JAR"