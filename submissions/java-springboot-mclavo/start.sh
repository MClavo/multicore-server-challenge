#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# Decode credentials without interpreting literal '+' or backslash characters as escapes.
decode_uri_component() {
    local remaining="$1" decoded="" byte
    while [[ "$remaining" =~ ^([^%]*)%([0-9a-fA-F]{2})(.*)$ ]]; do
        decoded+="${BASH_REMATCH[1]}"
        if [[ "${BASH_REMATCH[2]}" == 00 ]]; then
            echo 'DATABASE_URL credentials cannot contain NUL bytes' >&2
            return 1
        fi
        printf -v byte '%b' "\\x${BASH_REMATCH[2]}"
        decoded+="$byte"
        remaining="${BASH_REMATCH[3]}"
    done
    if [[ "$remaining" == *%* ]]; then
        echo 'Invalid percent-encoding in DATABASE_URL credentials' >&2
        return 1
    fi
    printf -v "$2" '%s' "$decoded$remaining"
}

if [[ -n "${DATABASE_URL:-}" ]]; then
    case "$DATABASE_URL" in
        postgres://*|postgresql://*) database_connection="${DATABASE_URL#*://}" ;;
        *) echo 'DATABASE_URL must be a PostgreSQL connection URI' >&2; exit 1 ;;
    esac

    database_authority="${database_connection%%/*}"
    database_location="${database_connection#*/}"
    database_location="${database_location%%#*}"
    if [[ "$database_connection" == "$database_authority" || -z "$database_location" || "$database_location" == [\?\#]* ]]; then
        echo 'DATABASE_URL must include a database name' >&2
        exit 1
    fi

    if [[ "$database_authority" == *@* ]]; then
        database_credentials="${database_authority%@*}"
        database_authority="${database_authority##*@}"
        decode_uri_component "${database_credentials%%:*}" database_username
        if [[ "$database_credentials" == *:* ]]; then
            decode_uri_component "${database_credentials#*:}" database_password
        else
            database_password=""
        fi
        export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME-$database_username}"
        export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD-$database_password}"
    fi

    if [[ -z "$database_authority" || "$database_authority" == *[\?\#]* ]]; then
        echo 'DATABASE_URL must include a database host' >&2
        exit 1
    fi
    export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL-jdbc:postgresql://$database_authority/$database_location}"
fi

if [[ -x /opt/conduit-jdk-25/bin/java ]]; then
    java_command=/opt/conduit-jdk-25/bin/java
elif [[ -n "${JAVA_HOME:-}" ]]; then
    java_command="$JAVA_HOME/bin/java"
else
    java_command=java
fi

exec "$java_command" -jar build/libs/conduit.jar
