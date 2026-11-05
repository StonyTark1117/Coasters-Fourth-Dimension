#!/usr/bin/env bash
set -euo pipefail
cd /tmp/fd401d-clean-telemetry-20261105/game
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx1536M \
  @/tmp/fd401-a-ec2fcda-build/moddev/clientRunClasspath.txt \
  @/tmp/fd401-a-ec2fcda-build/moddev/clientRunVmArgs.txt \
  -Dfml.modFolders= net.neoforged.devlaunch.Main \
  @/tmp/fd401d-clean-telemetry-20261105/clientRunProgramArgs.txt
