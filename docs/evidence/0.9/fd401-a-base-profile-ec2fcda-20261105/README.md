# FD401A selected-JAR base client load

On 2026-11-05, the selected clean `ec2fcda` JAR (`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`) loaded in the base profile on Java 21. The client reached the Minecraft title screen and the normal Mods screen listed **Coasters: Fourth Dimension 0.9.0-dev.27**. The startup [log](client.log.gz) identifies the exact JAR path and lists ten loaded mods, with neither Track Styles nor Engineered. The [title screen](menu-ready.png) and [mod entry](mod-fourth-dimension.png) are retained.

The [classpath](clientRunClasspath.txt), [VM arguments](clientRunVmArgs.txt), [program arguments](clientRunProgramArgs.txt), [launch script](run-client.sh), and [dependency hashes](dependencies.sha256) identify the inputs. The isolated game directory was `/tmp/fd401a-base-20261105/game`; private Xvfb `:249` and an active `MemoryMax=3G` scope contained the client. The game JVM used `-Xmx1024M`. The scope exited with `Result=success`, the client log ended `Stopping!`, and the task-owned display was stopped. The selected JAR hash matched before and after the run.

This proves a bounded **base-profile client startup and mod load** on the selected artifact. It does not prove an in-world base operator or telemetry scenario. Styles-only and Engineered-only selected-JAR profile launches are separate FD401A rows.
