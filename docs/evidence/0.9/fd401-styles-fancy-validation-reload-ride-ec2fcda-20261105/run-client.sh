#!/usr/bin/env bash
set -euo pipefail
cd /tmp/fd401b-fancy-repeat-20261105/game
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx768M \
  @/tmp/fd401b-ec2fcda-build/moddev/clientRunClasspath.txt \
  @/tmp/fd401b-ec2fcda-build/moddev/clientRunVmArgs.txt \
  -Dfml.modFolders= net.neoforged.devlaunch.Main \
  @/tmp/fd401b-ec2fcda-build/moddev/clientRunProgramArgs.txt
