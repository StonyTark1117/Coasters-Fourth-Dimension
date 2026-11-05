#!/bin/bash
set -e
cd '/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/server/game'
exec '/usr/lib/jvm/java-21-openjdk/bin/java' -Xmx1536M '@/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/evidence/serverRunClasspath.txt' '@/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/evidence/serverRunVmArgs.txt' '-Dfml.modFolders=' 'net.neoforged.devlaunch.Main' '@/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/evidence/serverRunProgramArgs.txt'
