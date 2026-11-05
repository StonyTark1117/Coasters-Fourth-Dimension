#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin"
export DISPLAY=:343
export ALSOFT_DRIVERS=null
cd /tmp/fd401c-full-circuit-run3-20261105/game
exec "$JAVA_HOME/bin/java" -Xmx1536M @/tmp/fd401c-full-circuit-run3-20261105/clientRunClasspath.txt @/tmp/fd401c-full-circuit-run3-20261105/clientRunVmArgs.txt -Dfml.modFolders= net.neoforged.devlaunch.Main @/tmp/fd401c-full-circuit-run3-20261105/clientRunProgramArgs.txt
