#!/bin/sh
cd /tmp/fd401d-controller-visuals-selected-20261105/observer/game
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx1536M @/tmp/fd401d-family-selected-20261105/build/moddev/observerRunClasspath.txt @/tmp/fd401d-family-selected-20261105/build/moddev/observerRunVmArgs.txt -Dfml.modFolders= net.neoforged.devlaunch.Main @/tmp/fd401d-family-selected-20261105/build/moddev/observerRunProgramArgs.txt
