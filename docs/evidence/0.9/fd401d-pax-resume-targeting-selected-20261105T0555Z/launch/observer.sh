#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin"
export ALSOFT_DRIVERS=null
(cd '/tmp/fd401d-pax-resume-selected-20261105T0555Z/operator'; exec '/usr/lib/jvm/java-21-openjdk/bin/java' '-Xmx1536M' '@/tmp/fd401d-pax-resume-selected-20261105T0555Z/build/moddev/observerRunClasspath.txt' '@/tmp/fd401d-pax-resume-selected-20261105T0555Z/build/moddev/observerRunVmArgs.txt' '-Dfml.modFolders=' 'net.neoforged.devlaunch.Main' '@/tmp/fd401d-pax-resume-selected-20261105T0555Z/build/moddev/observerRunProgramArgs.txt')