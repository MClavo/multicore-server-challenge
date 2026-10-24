#!/usr/bin/env bash
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
    echo "install.sh must run as root" >&2
    exit 1
fi

apt-get update

apt-get install -y \
    ca-certificates \
    curl \
    gpg

install -d -m 0755 /etc/apt/keyrings

curl -fsSL \
  https://packages.adoptium.net/artifactory/api/gpg/key/public \
  | gpg --batch --yes --dearmor \
      -o /etc/apt/keyrings/adoptium.gpg

. /etc/os-release

echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb ${VERSION_CODENAME} main" \
  > /etc/apt/sources.list.d/adoptium.list

apt-get update
apt-get install -y temurin-25-jdk

java -version