#!/usr/bin/env bash
set -euo pipefail
cd /tmp/fd401c-clean-combined-free-spin/game
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx1024M \
  @/tmp/fd401-a-ec2fcda-build/moddev/clientRunClasspath.txt \
  @/tmp/fd401-a-ec2fcda-build/moddev/clientRunVmArgs.txt \
  -Dfml.modFolders= net.neoforged.devlaunch.Main \
  @/tmp/fd401-a-ec2fcda-build/moddev/clientRunProgramArgs.txt
