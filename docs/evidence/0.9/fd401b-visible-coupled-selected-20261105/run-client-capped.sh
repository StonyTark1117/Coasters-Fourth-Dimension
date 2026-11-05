#!/usr/bin/env bash
set -euo pipefail
(cd '/tmp/fd401b-visual-selected-20261105/game'; exec '/usr/lib/jvm/java-21-openjdk/bin/java' '-Xmx1536M' '@/tmp/fd401b-visual-selected-20261105/build/moddev/clientRunClasspath.txt' '@/tmp/fd401b-visual-selected-20261105/build/moddev/clientRunVmArgs.txt' '-Dfml.modFolders=' 'net.neoforged.devlaunch.Main' '@/tmp/fd401b-visual-selected-20261105/build/moddev/clientRunProgramArgs.txt')
