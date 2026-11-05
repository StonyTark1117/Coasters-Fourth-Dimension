# FD401 refreshed-JAR combined-profile client reopen

On 2026-11-05, a Java 21 client launched the selected clean `ec2fcda` artifact
with Track Styles 1.2.0 and Engineered 1.1.1. The JAR SHA-256 was
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7` before
launch and after exit. The [classpath](clientRunClasspath.txt), [program
arguments](clientRunProgramArgs.txt), [VM arguments](clientRunVmArgs.txt),
[launch script](run-client.sh), and [dependency hashes](dependencies.sha256)
identify the runtime inputs. The client used `-Xmx1536M`, private Xvfb `:244`,
and `systemd-run --user --scope -p MemoryMax=3G`; the scope completed with
`Result=success`. No build ran during the client session.

The copied saved world was the combined-profile telemetry fixture from
[`fd401-create-telemetry-combined`](../fd401-create-telemetry-combined/README.md).
Its generated Quick Play identifier (`FD401FamilyBare`) did not match the
copied save directory (`world`), so the initial Quick Play attempt failed; the
failure is retained in [quickplay-miss.png](quickplay-miss.png). Selecting the
existing `world` entry from the World List loaded the fixture. The source
fixture archive SHA-256 is recorded in [world.sha256](world.sha256):
`8b65a4ac4ffeaa9c87138f3142228b4be01f43e07f87a29301249e53e6d386a2`. The
packaged mod log identifies Fourth Dimension as loaded from the selected JAR
path and records the integrated server restoring Dev's saved rider assignment:

```
FD_BOARDING_RESTORE queued player=Dev assignments=1
FD_BOARDING_RESTORE mounted player=Dev
```

The [loaded-world screenshot](loaded-world.png) and [F3
view](track-f3.png) are retained with the complete compressed
[client log](client.log.gz). The world was saved and the client exited normally;
the log ends with `Stopping!` after the integrated server saved all dimensions.

This adds one exact-artifact combined-profile client launch and occupied-world
reopen observation to FD401A's runtime identity map. The session did **not**
reach a Display Link source picker and did not capture live goggles, signs, or
Create output changes. It therefore closes no FD401D telemetry acceptance
scenario, and it does not complete FD401A's base/Styles/Engineered profile
matrix. The fixture world is a prior server save, not a fresh player-built
route.
