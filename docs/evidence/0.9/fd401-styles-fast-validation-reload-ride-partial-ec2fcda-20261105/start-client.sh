#!/usr/bin/env bash
set -euo pipefail
WORK=/tmp/fd401b-fast-20261105
systemd-run --user --scope --collect -p MemoryMax=3G -- \
  env DISPLAY=:252 ALSOFT_DRIVERS=null JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  PATH=/usr/lib/jvm/java-21-openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
  bash -c 'exec /tmp/fd401b-fast-20261105/run-client.sh > /tmp/fd401b-fast-20261105/evidence/client-console.log 2>&1' \
  > "$WORK/evidence/scope-launch.log" 2>&1
