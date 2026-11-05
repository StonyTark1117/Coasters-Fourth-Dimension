#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin"
cd /tmp/fd401c-clean-legacy-continuous-20261105/game
exec "$JAVA_HOME/bin/java" -Xmx1024M \
  @/tmp/fd401c-clean-legacy-continuous-build/moddev/clientRunClasspath.txt \
  @/tmp/fd401c-clean-legacy-continuous-build/moddev/clientRunVmArgs.txt \
  -Dfml.modFolders= net.neoforged.devlaunch.Main \
  @/tmp/fd401c-clean-legacy-continuous-build/moddev/clientRunProgramArgs.txt
