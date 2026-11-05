#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin"
cd /tmp/fd401b-geometry-selected-20261105/game
exec "$JAVA_HOME/bin/java" -Xmx768M \
  @/tmp/fd401b-geometry-selected-20261105/evidence/clientRunClasspath.txt \
  @/tmp/fd401b-geometry-selected-20261105/evidence/clientRunVmArgs.txt \
  -Dfml.modFolders= \
  -Dfourthdimension.renderValidation=true \
  -Dfourthdimension.renderValidationGraphicsMode=FANCY \
  -Dfourthdimension.renderValidationStages=500 \
  -Dfourthdimension.renderValidationExit=false \
  net.neoforged.devlaunch.Main \
  @/tmp/fd401b-geometry-selected-20261105/evidence/clientRunProgramArgs.txt \
  --quickPlaySingleplayer FD401StopCycle
