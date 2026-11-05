#!/usr/bin/env bash
set -euo pipefail
cd /tmp/fd401c-seat-diagnostic/game
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx1024M \
  @/tmp/fd401c-seat-diagnostic-build/moddev/clientRunClasspath.txt \
  @/tmp/fd401c-seat-diagnostic-build/moddev/clientRunVmArgs.txt \
  -Dfml.modFolders= net.neoforged.devlaunch.Main \
  @/tmp/fd401c-seat-diagnostic-build/moddev/clientRunProgramArgs.txt
