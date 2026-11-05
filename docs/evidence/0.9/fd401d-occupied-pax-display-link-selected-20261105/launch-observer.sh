#!/bin/bash
set -e
cd '/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/observer/game'
exec '/usr/lib/jvm/java-21-openjdk/bin/java' -Xmx1536M '@/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/evidence/observerRunClasspath.txt' '@/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/evidence/observerRunVmArgs.txt' '-Dfml.modFolders=' 'net.neoforged.devlaunch.Main' '@/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/evidence/observerProgram.txt'
