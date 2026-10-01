#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JAVA_TOOL_OPTIONS=-Xmx2G
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runServer -PfdPackaged -PfdPackagedJar=/home/braydon/.cache/fourthdimension-48535694/create-guidance-before.jar -PfdIntegrations=engineered,styles,additions,magnetized,immersion -PfdValidation > '/home/braydon/.codex/worktrees/coaster-integrations/Coasters: Fourth Dimension/build/create-guidance/resumed/server.log' 2>&1
result=$?
printf '%s\n' "$result" > '/home/braydon/.codex/worktrees/coaster-integrations/Coasters: Fourth Dimension/build/create-guidance/resumed/server.exit'
exit "$result"
