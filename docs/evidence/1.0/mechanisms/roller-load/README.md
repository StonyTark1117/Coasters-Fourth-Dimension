# FD904B Roller Ball full-load motion repair

Completed November 15, 2026 (America/Phoenix). Owner: Mechanisms,
`01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`; returned to the lead for integration.
This closes the assigned full-capacity motion defect, not FD904B's remaining
park/performance work. Work stayed on primary `main` at `f5fcd80f`; no Git
mutation, graphical client, or access to the lead's park world occurred.

## Result

An isolated native example at `(-750,110,110)` reproduced the first lift-curve
stall with four **distinct** native riders and carrier mass **14.2**. Removing
three fixture pigs in the same process reduced mass to **11.8** and moved the
same car **2.5573445 blocks upward in 100 ticks**, into the vertical lift. No
pose write, capture/resume, restart, or constraint reconstruction was used.
The first removal probe used normal dismount, which closed restraints correctly
rejected; its unchanged 14.2 mass invalidated that probe. The corrected probe
discarded only the three synthetic fixture entities, keeping restraints closed.

The final repair passed one complete four-rider native cycle at tick **7896**,
including lift, summit release, free descent, return and ready dock capture.
Four unique occupants and mass 14.2 were asserted on every operating tick.
At tick 1503 the fixture removed actual motor power. The stopped, restrained car
drifted only **3.0205e-7 blocks** over the hold; restoring power did not resume
travel. Explicit Resume completed the cycle.

The affected entry rise passed the existing guide bounds at tick 1225:

| Measurement | Peak | Existing bound |
| --- | ---: | ---: |
| Native relative angular rate | 0.0506944 rad/s | 0.3501 |
| Native relative angular acceleration | 0.173802 rad/s² | 0.5001 |
| Pose-derived angular rate | 0.118615 rad/s | 0.3501 |
| Pose-derived angular acceleration | 0.242375 rad/s² | 0.5001 |

See [exact results](results.txt), [verification and artifact hashes](verification.json),
and [final native log](qualify06.log.gz).

## Cause and repair

Pinned Coasters `CoasterCartChainLiftDrive.applySpeedMatch` drives the running-gear
body. Roller Ball has a separate, much heavier carrier joined to that chassis.
At the steep curve end the native Chain velocity correction to the 3-unit chassis
was insufficient to lift the complete 17.2-unit assembly. Native Chain uses the
span chord projected onto the local tangent; its negative signed Chain speed is
deliberately negated for cart travel, so the observed `-2` was not reversed power.

`RollerBallRuntime` now shares the **same native Chain operation** with the loaded
carrier only where powered Chain travel opposes gravity. Level, descending and
unpowered spans receive no added carrier drive. No independent travel target,
gravity cancellation, position correction, or change to guide motor limits was
introduced.

The extra native pull must also respect the existing brake. Native Chain permits
64 blocks/s² acceleration, whereas the guide brake permits 24. The runtime
predicts combined translation from mass-weighted body velocities and gravity,
then trims only added carrier pull that the existing brake cannot absorb.
Completed-pose feedback remains in the limiter. The existing slow-join envelope
also covers departure from a straight segment's starting anchor, while the
running-gear frame finishes the adjoining curve. [Production diff](runtime.diff).

## Evidence scope and diagnosed iterations

The historical [four-seat loading test](../../../0.7/roller-ball/native/load-correction-pass.log)
proves mass, hinge geometry and save fields on a stationary dock. Historical
[guide acceptance](../../../0.7/roller-ball/native/guide-pass.log) and
[route acceptance](../../../0.7/roller-ball/native/route-pass.log) used one rider.
They remain accepted; this case adds the specifically missing full-load motion
claim. The fixture constructs the route and seats animals directly and makes no
new normal-input, real-player, multiplayer, rendering or performance claim.

The entry guide assertion is evaluated after the initial curve and four blocks
of the first vertical rise. This retains the historical guide-only scope using
route progress rather than a fixed tick count on a now faster lift. Later-cycle
peaks remain reported as in the historical route test, not new acceptance gates:
the final entire-cycle native angular acceleration was 0.396510 and pose-derived
acceleration was 0.585008 rad/s². The historical accepted whole-route record itself
reports native angular acceleration 2.65869 without a whole-cycle 0.5 assertion.
The production command-rate/acceleration limits remain unchanged.

All attempts are preserved, including failures; none were silently replaced:

| Record | Finding and next change |
| --- | --- |
| `diagnosis-restraint-guard` | Restraints rejected normal fixture dismount; load stayed 14.2. Corrected removal method. |
| `diagnosis` | Four-rider stall, same-process removal to one rider, 100 ticks of progress. |
| `qualify01` | Full cycle/power hold passed, but fixture incorrectly asserted entry bound over entire cycle. Corrected assertion scope. |
| `qualify02` | Real entry pose-acceleration failure 0.920942. Added gravity-adjusted prediction. |
| `qualify03` | Entry peak reduced to 0.605679, still failed. Extended departure-side join cap. |
| `qualify04` | Unchanged peak; paused motion changes for substep diagnosis. |
| `trace` | Recorded native 64 acceleration against 24 brake; required carrier-pull budget. Temporary production tracing removed. |
| `qualify05` | Entry bounds and power hold passed; descending SUMMIT stalled. Restricted assistance to travel against gravity. |
| `qualify06` | Entry bounds, no-power retention/explicit resume and complete four-rider cycle passed. Stop. |

## Reproduction, artifact and cleanup

Opt-in namespace: `fourthdimension_fd904_roller_load`; sole test
`Fd904RollerLoadGameTests.fullLoadMotion`. `FD904_ROLLER_MODE=diagnose` selects
the baseline stall/removal probe; leave unset for qualification. Run diagnosis
against the pre-repair runtime, not the fixed code which should not stall.
The dedicated empty NBT fixture is under that namespace.

Persistent root:
`/mnt/NVME4/fourthdimension-runs/fd904-roller-load-20261115`.
The Java 21 Gradle preparation generated the direct GameTest launch script;
each run used its own directory/world, a named systemd user service, 3 GiB JVM
heap and 6 GiB service cap. Port 25691 was reserved for these isolated runs.
The final unit used 1.5 GiB peak memory according to systemd. Native tests execute
faster than wall-clock game time; this is not a performance measurement.

Final artifact:
`evidence/repair06-coasters-fourth-dimension-0.9.0-dev.27.jar` under the run root.
SHA-256: `66410d17c78b45c2560ca7314d3165b7948877ff6838d340e5819e382e2443d9`.
The executed classpath's RollerBallRuntime bytes exactly match that JAR's class;
this was a source-classpath native test, not a packaged-install claim. The lead
may integrate the one production class and the opt-in fixture; unrelated profile
work present in the shared checkout remains the lead's responsibility.

Every run saved and exited. All owned units have PID 0; diagnostic failure states
were cleared after recording them. No owned Java process or listening endpoint
remains. No client/display was started. All nine worlds and full logs remain
under the persistent root; [cleanup verification](cleanup.json) lists them.
Only the assigned runtime source, new test, empty NBT and this evidence directory
were edited. Source/evidence are returned uncommitted for lead integration.
