#!/bin/sh
cd /tmp/fd401d-family-followup-20261105/server/game
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx1536M @/tmp/fd401d-family-followup-20261105/build/moddev/serverRunClasspath.txt @/tmp/fd401d-family-followup-20261105/build/moddev/serverRunVmArgs.txt -Dfml.modFolders= net.neoforged.devlaunch.Main @/tmp/fd401d-family-followup-20261105/build/moddev/serverRunProgramArgs.txt
