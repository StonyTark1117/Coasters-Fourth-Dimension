#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin"
cd /tmp/fd401b-solas-selected-ec2fcda/game
exec "$JAVA_HOME/bin/java" -Xmx1536M \
  @/tmp/fd401b-solas-selected-ec2fcda/evidence/clientRunClasspath.txt \
  @/tmp/fd401b-solas-selected-ec2fcda/evidence/clientRunVmArgs.txt \
  -Dfml.modFolders=/tmp/fd401b-solas-selected-ec2fcda/game/mods \
  net.neoforged.devlaunch.Main \
  @/tmp/fd401b-solas-selected-ec2fcda/evidence/clientRunProgramArgs.txt
