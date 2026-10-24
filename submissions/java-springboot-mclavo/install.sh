#!/usr/bin/env bash
# Run as root on Ubuntu 24.04. Installs the pinned Java 25 toolchain used by build.sh/start.sh.
set -euo pipefail

apt-get update -q
apt-get install -y -q curl ca-certificates tar

work_directory="$(mktemp -d)"
trap 'rm -rf "$work_directory"' EXIT

release_path='jdk-25.0.5%2B8/linux/x64/jdk/hotspot/normal/eclipse'
curl --fail --silent --show-error --location \
    "https://api.adoptium.net/v3/binary/version/$release_path" --output "$work_directory/jdk.tar.gz"
curl --fail --silent --show-error --location \
    "https://api.adoptium.net/v3/checksum/version/$release_path" --output "$work_directory/checksum.txt"
read -r checksum _ < "$work_directory/checksum.txt"
printf '%s  %s\n' "$checksum" "$work_directory/jdk.tar.gz" | sha256sum --check --status
mkdir -p /opt/conduit-jdk-25
tar --extract --gzip --file "$work_directory/jdk.tar.gz" --directory /opt/conduit-jdk-25 --strip-components=1
chmod -R a+rX /opt/conduit-jdk-25
