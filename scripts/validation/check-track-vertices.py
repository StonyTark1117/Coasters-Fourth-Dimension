#!/usr/bin/env python3
"""Compare track vertex attributes against the pinned native Minecraft writer.

Usage: check-track-vertices.py /absolute/packaged-runtime /absolute/output
Run outside park timing windows. No game, display, or Gradle process is started.
"""
import argparse
import os
from pathlib import Path
import shlex
import subprocess

p = argparse.ArgumentParser(description=__doc__)
p.add_argument("runtime", type=Path)
p.add_argument("output", type=Path)
a = p.parse_args()
assert a.runtime.is_absolute() and a.output.is_absolute()
repo = Path(__file__).resolve().parents[2]
java = Path("/usr/lib/jvm/java-21-openjdk")
env = dict(os.environ, JAVA_HOME=str(java), PATH=str(java / "bin") + ":" + os.environ["PATH"])
args = shlex.split((a.runtime / "moddev/clientRunClasspath.txt").read_text())
classpath = args[args.index("-classpath") + 1]
classes = a.output / "classes"
classes.mkdir(parents=True, exist_ok=True)
with (a.output / "result.log").open("w") as log:
    def run(command):
        result = subprocess.run(command, cwd=a.output, env=env, stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT, text=True)
        print(result.stdout, end="", flush=True)
        log.write(result.stdout)
        log.flush()
        result.check_returncode()

    run([str(java / "bin/java"), "-version"])
    run([str(java / "bin/javac"), "-J-Xmx256M", "--release", "21", "-proc:none",
         "-classpath", classpath, "-d", str(classes),
         str(repo / "src/main/java/dev/fourthdimension/client/TrackVertexWriter.java"),
         str(repo / "scripts/validation/TrackVertexWriterCheck.java")])
    run([str(java / "bin/java"), "-Xmx256M", "-classpath",
         str(classes) + ":" + classpath, "TrackVertexWriterCheck"])
