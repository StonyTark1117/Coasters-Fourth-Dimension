# Confirmed native-chain availability gaps

Source `0fc8a76ba2b2d0226027f3e88bdf3b4a481bc94b` completed **54 GameTests with
three required failures**. The server saved and stopped normally. Gradle exited
**1** after 4m 46s; all five post-exit native exports were captured successfully
(capture exit **0**). This is a failing baseline, not safety acceptance.

## Measurements

| Source and condition | Actual calls | Maximum linear velocity change | Maximum angular velocity change |
| --- | --- | --- | --- |
| Ordinary recovery bearing, explicitly braked | 3 | 0.0 | 0.0 |
| Ordinary recovery bearing, healthy active tow | 3 | 0.5 | 0.0 |
| Ordinary recovery bearing, unloaded component before recovery tick | 3 | **0.5** | 0.0 |
| Ordinary recovery bearing, after recovery tick faults/stops group | 3 | 0.0 | 0.0 |
| Two-car 4D consist, held before unload | 12 | 0.0 | 0.0 |
| Two-car 4D consist, healthy before unload | 12 | 0.5 | 0.0 |
| Surviving chassis source after a consist component unloads | 10 | **0.5** | 0.0 |
| Carrier restored, healthy chassis source | 8 | 0.5 | 0.0 |

Each recovery fixture unloads carrier 0, carrier 1 or chassis through actual native
`UNLOADED` removal while retaining logical recovery identity and healthy flags.
Each two-car fixture unloads one of those components on either car. The source
remains live and contained; all other expected bodies remain present. Telemetry
reports unavailability before the direct callback. All nine removed bodies restore
with their original IDs. Carrier restoration retains two native links on each car;
chassis restoration proves identity/availability only, not automatic relinking.

Every chain callback uses a real powered production-example chain span and an
actual finite target speed of -0.5. The probe records each surviving body's exact
original linear and angular velocity, temporarily establishes a verified zero
velocity baseline, calls `CoasterCartChainLiftDrive.applySpeedMatch`, reads native
pipeline velocity, then restores and verifies the original velocities in `finally`.
Power, membership and logical interlocks are unchanged by this normalization.
The 23 healthy/restored positive controls establish that the callback was effective;
the 15 held/braked controls establish that the existing holds were effective.

The existing Engineered ordinary-bearing test is the third failure. It again
records **0.08305553527832525** queued force/torque delta in all three before-tick
unload cases, zero while braked and after recovery tick, and the same nonzero
healthy controls. These force/torque measurements are distinct from chain velocity.
The prior Engineered 4D-source test again completes six unloads, ten zero blocked
calls and twenty positive controls without failures.

## Provenance and reproduction

All twelve evidence-completeness checks in `review.json` pass. Exactly two chain
safety checks fail: recovery before-tick zero and chassis unavailable zero.
Run `python3 review-run.py` to reproduce extraction from the compressed full log.
The new tests each register one `onEachTick` callback and schedule subsequent
stages through a private ordered queue, avoiding nested GameTest action-map edits.

The matching addon JAR SHA-256 is
`f9c302f8816c2d63bfb4026b61109c61f7d3d0dda977d98d97e858a33018bcfd`.
All **233** packaged classes match the compiled runtime classes. Both production
interlock classes are unchanged from `270697b`. Source was clean before and after
the run. Execution used the Engineered-only development loose-class profile, not
a packaged-JAR release validation. Commands, timestamps, preflight, compile log,
runner, full Gradle/server logs, matching JAR and 108 live open-JAR hashes are retained.

The runner acquired `/tmp/fd-gametest-runtime.lock` with nonblocking `flock -n` and
held it through export capture. Prelaunch found no non-daemon Java process. Start
observation records only this game runtime; end observation, after lock release,
includes the next owner's shared-root GameTest runtime. These are bounded process
observations, not continuous tracing or a performance result. The earlier failed
lock acquisition did not execute this runner or launch a game.

Large native exports remain local at
`/tmp/fd-native-chain-availability-0fc8a76/native-exports/`; the retained manifest
records capture integrity but does not substitute for the payloads. No geometry
acceptance is inferred from those exports.

## Consequence and limits

Native chain drive's shared `PropulsionInterlock` permits both ordinary recovery
bearing and 4D chassis callbacks during the physical-unavailability window. The
Engineered-specific 4D guard does not protect this chain path or ordinary bearing
sources. These measurements justify reviewing a shared availability predicate
while preserving healthy maintenance-retained towing and unrelated ordinary carts.
No production policy changed in this baseline.

Direct callback evidence does not prove natural scheduling always reaches the
window. This run does not establish natural chunk lifecycle, occupied recovery,
automatic relinking, all-family integration acceptance or completion of 0.9.
