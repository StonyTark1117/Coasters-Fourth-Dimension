#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JAVA_TOOL_OPTIONS=-Xmx3G
export DISPLAY=:0
export LIBGL_ALWAYS_SOFTWARE=1
export ALSOFT_DRIVERS=null
export XDG_SESSION_TYPE=x11
unset WAYLAND_DISPLAY
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runClient -PfdPackaged -PfdPackagedJar=/home/braydon/.cache/fourthdimension-48535694/additions-lifecycle-v3.jar -PfdValidation -PfdConnect=127.0.0.1:25585 -PfdIntegrations=additions > '/home/braydon/.codex/worktrees/coaster-integrations/Coasters: Fourth Dimension/build/additions-lifecycle/restored/dev.log' 2>&1
result=$?
printf '%s\n' "$result" > '/home/braydon/.codex/worktrees/coaster-integrations/Coasters: Fourth Dimension/build/additions-lifecycle/restored/dev.exit'
exit "$result"
