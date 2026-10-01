#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JAVA_TOOL_OPTIONS=-Xmx2G
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runServer -PfdPackaged -PfdPackagedJar=/home/braydon/.cache/fourthdimension-48535694/camera-connections-before.jar -PfdValidation -PfdFrozen -PfdIntegrations=immersion > '/home/braydon/.codex/worktrees/coaster-integrations/Coasters: Fourth Dimension/build/camera-connections/before/server.log' 2>&1
code=$?
printf '%s\n' "$code" > '/home/braydon/.codex/worktrees/coaster-integrations/Coasters: Fourth Dimension/build/camera-connections/before/server.exit'
exit "$code"
