#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin"
cd "/tmp/fd401c-pax-recovery-dedicated-20261105/server"
exec "$JAVA_HOME/bin/java" -Xmx1536M @"/tmp/fd401c-pax-recovery-dedicated-20261105/evidence/serverRunClasspath.txt" @"/tmp/fd401c-pax-recovery-dedicated-20261105/evidence/serverRunVmArgs.txt" -Dfml.modFolders= net.neoforged.devlaunch.Main @"/tmp/fd401c-pax-recovery-dedicated-20261105/evidence/serverRunProgramArgs.txt"
