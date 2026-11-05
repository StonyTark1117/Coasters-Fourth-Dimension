#!/usr/bin/env bash
set -euo pipefail
cd "/tmp/fd401b-fast-resume-20261105/game"
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx768M @"/tmp/fd401b-fast-resume-20261105/args/clientRunClasspath.txt" @"/tmp/fd401b-fast-resume-20261105/args/clientRunVmArgs.txt" -Dfml.modFolders= net.neoforged.devlaunch.Main @"/tmp/fd401b-fast-resume-20261105/args/clientRunProgramArgs.txt"
