# Confirmed ordinary recovery-bearing availability gap

Source `270697b431d3b234bf083a5877574a1cb78d75ff` completed **52 GameTests with one
required failure**: `recoveryBearingRejectsUnavailableTowComponents`. The server
saved and stopped normally; Gradle exited **1**, and post-exit native export
capture exited **0**. This is a failing baseline, not recovery safety acceptance.
The production Engineered guard is unchanged from the passing `b0bf7bc` run.

## Observed behavior

Each of three fresh fixtures uses the production Arrow recovery example, a bare
ordinary bearing, normal select/attach, closed restraints and maintenance retention,
and explicit `Start tow`. The controller stays in COAST while the native link
settles. The test probes both carriers and the chassis in separate fixtures.
It serializes one native body, removes it through `UNLOADED`, and proves that the
logical recovery identity, retention, healthy flags and remaining physical bodies
survive. Telemetry reports the unavailable car. No tick occurs before the next
direct installed `MotorDrive.apply` callback on the ordinary bearing.

| State | Carrier 0 case | Carrier 1 case | Chassis case |
| --- | --- | --- | --- |
| Explicit brake | 0.0 | 0.0 | 0.0 |
| Healthy after Start tow | 0.08305553527832525 | 0.08305553527832525 | 0.08305553527832525 |
| Unloaded, before recovery tick | **0.08305553527832525** | **0.08305553527832525** | **0.08305553527832525** |
| Unloaded, after recovery tick | 0.0 | 0.0 | 0.0 |

Values are the maximum queued local force/torque delta across the surviving group
bodies, not speed or distance. Every probe restores its queued accumulators.
`RecoveryOperations.tick(level, .05)` then sets the expected stopping/fault state,
which blocks the next callback. All three missing bodies restore with the same UUID;
cleanup precedes each fresh case. No restored link continuity is claimed.

The actual completion counters are three native unloads and three calls in each
of the four states above. The final marker retains all three before-tick failures.
`review.json` distinguishes complete evidence from failed safety behavior;
`python3 review-run.py` reproduces extraction from the full compressed Gradle log.
The existing 4D-chassis availability test again completed six unloads, ten blocked
calls and twenty positive controls without failures. That passing test does not
cover an ordinary motor source.

## Provenance

The matching addon JAR SHA-256 is
`18dcc13f1c7ff6bb6085b7e29f3a1baf3ca504fd34887a6297ac24272d0d8877`.
All 226 packaged classes match the compiled runtime classes. Git was clean before
and after the run. This is Engineered-only **development loose-class** execution,
not a packaged release validation. The exact command, timestamps, source/JAR hash,
52-test expectation and runner are retained. The initial compile caught a missing
track-frame import; its log is retained alongside the successful import-only retry.

Execution used the shared `/tmp/fd-gametest-runtime.lock` for the whole run and
export capture. The prelaunch check rejected any existing non-daemon Java process,
including game directories outside `build/runs`. Startup observation shows our game
and a second waiting flock, not a second game. End observations were taken after
our terminal exit and lock release; they may include the next owner's runtime.
These snapshots do not constitute continuous process tracing or a performance result.
Supplementary identity includes hashes of 108 JARs open by our game JVM.

All five generated native exports were captured successfully after exit. Large
payloads remain local at `/tmp/fd-engineered-recovery-availability-270697b/native-exports/`;
the retained capture manifest is not a substitute for those payloads or new geometry
acceptance. Full Gradle/server logs, matching addon JAR, metadata and hash index are
retained here.

## Next investigation and limits

This proves an Engineered callback can add force during the native-unload window
before the recovery tick faults a logically active group. It does not establish
that natural game scheduling necessarily reaches that window. Native chain drive
also uses the shared propulsion guard, but its callback behavior has **not** been
measured here. Probe both an ordinary recovery source and a 4D chassis source before
choosing shared versus Engineered-specific guard placement. No production policy
change was made in this test revision.

No natural chunk lifecycle, occupied recovery, automatic relinking, GUI/FE-network,
all-family or full release acceptance is claimed. The failure keeps the affected
recovery safety work open.
