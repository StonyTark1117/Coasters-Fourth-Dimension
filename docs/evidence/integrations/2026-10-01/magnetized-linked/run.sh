#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JAVA_TOOL_OPTIONS=-Xmx2G
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer -PfdIntegrations=magnetized > build/magnetized-linked/magnetized.log 2>&1
code=$?
printf '%s\n' "$code" > build/magnetized-linked/magnetized.exit
exit "$code"
