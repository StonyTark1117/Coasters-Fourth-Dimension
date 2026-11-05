#!/usr/bin/env bash
set -euo pipefail
cd '/tmp/fd401b-fabulous-repeat2-20261105/game'
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx1536M \
  @/tmp/fd401b-ec2fcda-build/moddev/clientRunClasspath.txt \
  @/tmp/fd401b-ec2fcda-build/moddev/clientRunVmArgs.txt \
  -Dfourthdimension.validation=true \
  -Dfml.modFolders= net.neoforged.devlaunch.Main \
  @/tmp/fd401b-ec2fcda-build/moddev/clientRunProgramArgs.txt
