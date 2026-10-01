#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JAVA_TOOL_OPTIONS=-Xmx2G
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runServer -PfdPackaged -PfdPackagedJar=/home/braydon/.cache/fourthdimension-48535694/additions-lifecycle.jar -PfdValidation -PfdFrozen -PfdIntegrations=additions > '/home/braydon/.codex/worktrees/coaster-integrations/Coasters: Fourth Dimension/build/additions-lifecycle/seed/server.log' 2>&1
code=$?
printf '%s\n' "$code" > '/home/braydon/.codex/worktrees/coaster-integrations/Coasters: Fourth Dimension/build/additions-lifecycle/seed/server.exit'
exit "$code"
