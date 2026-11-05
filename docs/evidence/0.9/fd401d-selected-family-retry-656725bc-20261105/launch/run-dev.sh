#!/bin/sh
cd /tmp/fd401d-family-followup-20261105/dev/game
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx1536M @/tmp/fd401d-family-followup-20261105/build/moddev/clientRunClasspath.txt @/tmp/fd401d-family-followup-20261105/build/moddev/clientRunVmArgs.txt -Dfml.modFolders= net.neoforged.devlaunch.Main @/tmp/fd401d-family-followup-20261105/build/moddev/clientRunProgramArgs.txt --username Dev
