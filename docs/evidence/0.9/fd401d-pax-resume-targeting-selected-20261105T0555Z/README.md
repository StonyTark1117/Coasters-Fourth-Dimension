# FD401D selected-JAR PAX pre-resume telemetry and targeting attempt

This bounded two-client run was made on 2026-11-05 from the selected source
baseline `ec2fcda3082f5d9ce729641a6394fdc5c5d67a36`. The selected
`0.9.0-dev.27` JAR is included as `qualification.jar`; SHA-256 is
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`. The
run log says Fourth Dimension loaded from the matching generated JAR path, and
the generated JAR and retained copy have identical hashes.

## Runtime inputs

The profile was combined: Track Styles 1.2.0 (`coasterssimulatedextratypes`)
and Engineered 1.1.1 (`coastersengineered`). The Observer log lists Minecraft
1.21.1, NeoForge 21.1.248, Create 6.0.10 (`create`), Coasters Simulated 0.1.5
(`simulatedcoasters`), Flywheel 1.0.6 (`flywheel`), Sable 2.0.3 (`sable`),
Sable Companion 1.6.0 (`sablecompanion`), Veil 4.1.4 (`veil`), and Ponder
1.0.82 (`ponder`). The Create commit reported by the log is
`ac0c444d9828da3453ae8cc65338e8de063286fb`. `launch/` preserves the generated
launch scripts and client/server classpath, VM and program-argument files.
`launch/classpath-jars.sha256` records the hashes of the JAR files in those
three generated runtime classpaths; the class/resource directories that also
precede the JAR entries in the development classpaths are not archived as
runtime inputs. The packaged selected JAR is confirmed by hash and load log,
but this dev-classpath run does not establish that every loaded class came
exclusively from that JAR.

The launch scripts specify Java 21 at `/usr/lib/jvm/java-21-openjdk/bin/java`
and `-Xmx1536M` for each client and the server. The retained run record has no
scope/cgroup transcript, so this README makes no process memory-cap claim.
The copied input world is the saved four-car PAX fault world from the earlier
selected-JAR power-fault run; its SHA-256 is recorded in `world.sha256`, which
points to that committed archive. RCON queried the powered Create motor at
`(-856,143,83)` and recorded `id: "create:motor"`, `Speed: 16.0f`, and
`ScrollValue: 16` (`rcon-setup.log`).

## Bounded observation

Observer joined the copied world and received one verified
`FD_CLIENT_TELEMETRY` snapshot at `(-856,144,83)` (`telemetry-extract.txt`,
`observer-console.log`): PAX, four cars, `FAULT`, one occupied rider,
restraints closed, and the fault `Winch power lost: retain the train; inspect
and resume or recover`. This is a single pre-resume snapshot. It proves that
the occupied fault state reached this client snapshot; it does not prove that
the goggles, a sign, or another client rendered matching output.

The resume action did not occur. `pre-resume-fault.png` shows the attempted
operator view aimed at grass at `(-856,86,83)`, while the winch is at
y=144. `rcon-player-check.txt` independently records Observer at
`[-856.5,86.0,81.5]`, looking east at pitch 60, with no root vehicle; the
queried block at `(-856,85,83)` was not a block entity. The retained targeting
screenshots show the attempts but do not establish a winch hit. The bounded
failure is reacquiring the winch target from this view; no successful use or
state transition is recorded.

## Limits

This run does **not** prove resume, recovered motion, station capture,
restraint release, or rider unload. The FAULT snapshot is occupied and has
restraints closed, but no post-resume telemetry exists. It does not close the
FD401D fault-recovery row. The copied world and prior fault setup are described
in [the selected-JAR power-fault evidence](../fd401-pax-live-rider-power-fault-656725bc-20261105/README.md).

`SHA256SUMS` covers every file in this evidence directory except itself.
