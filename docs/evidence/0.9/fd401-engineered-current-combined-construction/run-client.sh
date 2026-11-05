#!/usr/bin/env bash
set -euo pipefail
cd /tmp/fd401-c-combined-construction/game
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx1024M \
  @/tmp/fd401-c-combined-current/build/moddev/clientRunClasspath.txt \
  @/tmp/fd401-c-combined-current/build/moddev/clientRunVmArgs.txt \
  -Dfml.modFolders= net.neoforged.devlaunch.Main \
  @/tmp/fd401-c-combined-current/build/moddev/clientRunProgramArgs.txt
