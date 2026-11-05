#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin"
cd "/tmp/fd401b-styles-absent-occupied-20261105/game"
exec "$JAVA_HOME/bin/java" -Xmx1536M @"/tmp/fd401b-styles-absent-occupied-20261105/classpath.txt" @"/tmp/fd401b-styles-absent-occupied-20261105/vmargs.txt" -Dfml.modFolders= net.neoforged.devlaunch.Main @"/tmp/fd401b-styles-absent-occupied-20261105/program.txt"
