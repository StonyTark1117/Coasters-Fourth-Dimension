#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin"
cd /tmp/fd401b-provider-lifecycle-20261105/base/game
exec "$JAVA_HOME/bin/java" -Xmx1024M @/tmp/fd401b-provider-lifecycle-20261105/evidence/base-classpath.txt @/tmp/fd401b-provider-lifecycle-20261105/evidence/base-vmargs.txt -Dfml.modFolders= net.neoforged.devlaunch.Main @/tmp/fd401b-provider-lifecycle-20261105/evidence/base-program.txt
