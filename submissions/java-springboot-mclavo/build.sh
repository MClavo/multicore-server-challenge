#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ -x /opt/conduit-jdk-25/bin/java ]]; then
    export JAVA_HOME=/opt/conduit-jdk-25
    export PATH="$JAVA_HOME/bin:$PATH"
fi

./gradlew --no-daemon bootJar -x test
