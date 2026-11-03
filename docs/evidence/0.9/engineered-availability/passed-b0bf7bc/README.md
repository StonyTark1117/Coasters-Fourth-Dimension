# Engineered physical-availability guard: native pass

Source `b0bf7bc07103bb6893a2cc636d9e3c0fe16e40ad` completed all **51 required
GameTests**, with normal server save/shutdown, Gradle exit **0**, and post-exit
native export capture exit **0**. The run began at 2026-11-03 11:33:09 UTC and ended
at 11:37:50 UTC. This is Engineered-only development-runtime evidence using loose
compiled classes; it is not a packaged-client or full FD401/0.9 certification.

## Observed safety result

Six fresh two-car Arrow fixtures exercised each chassis and all four carriers.
Every case used actual `SubLevelRemovalReason.UNLOADED`, retaining logical
vehicle identity and healthy interlock flags. Before each unavailable-body
callback, the test required two-car logical membership and unavailable telemetry.
The direct installed Engineered `MotorDrive.apply` call was measured across all
surviving chassis/carriers, with queued force and torque restored after every probe.

- Six distinct native bodies were unloaded and restored with the same UUID.
- All ten required surviving-source motor calls added exactly **0.0** force/torque.
- All twenty positive controls exceeded 0.001: twelve before unload and eight
  after carrier restoration.
- Carrier restores preserved native consist sizes `[2, 2]` and healthy propulsion.
- Both partial chassis restores produced native consist sizes `[1, 1]`. Their
  assertions establish same-ID physical restoration and available telemetry only.
  They do **not** establish train-link recovery or post-restore chassis propulsion.
  Each case cleaned up completely before a fresh independent fixture began.

The actual-count completion marker is:

```text
FD_ENGINEERED_AVAILABILITY complete nativeUnloads=6 unavailableCalls=10 positiveCalls=20 failures=[]
```

`review.json` records each measured call and checks exact per-case source coverage,
not merely the total or completion marker. Reproduce the offline extraction with
`python3 review-run.py` in this directory. Diagnostics are in full Gradle stdout;
Minecraft's separate server log does not include those `System.out` markers.

## Source, artifact and runtime identity

The matching addon JAR SHA-256 is
`ee07e410bf9c5179bf8b4d12cc51d72a16d6e4585ecab82ee5449e3d9a82a2cf`.
All 224 packaged classes match the compiled runtime classes. The production
`EngineeredInterlock.class` is byte-identical to the partial `3bc57cf` run;
this revision changes fixture scope, not the guard. Git was clean at preflight
and after process exit. See the preflight, runner metadata and class-parity report.

The command was run through `flock -n /tmp/fd-gametest-runtime.lock` after the
previous Arrow wrapper exited. The runner also refused preexisting game JVMs
under `/build/runs/`; that extra path check alone is not a universal runtime detector.
The shared lock was held for the run and post-exit capture. The retained process
observations show this run's wrapper/game and no competing game at observation
time. They are snapshots, not a continuous process trace or performance benchmark.
The earlier baseline and partial runs retain their separately disclosed overlaps.

The actual command was `./gradlew runGameTestServer --offline --no-daemon
-PfdIntegrations=engineered`. Full logs identify Engineered 1.1.1, Coasters 0.1.5,
Create 6.0.10 and Sable 2.0.3. `runtime-open-jars.json` supplements that identity
with hashes of 108 JARs open by the running game; it is not an exhaustive
resolution report. No optional addon expansion was activated.

## Evidence and limits

Both complete logs, matching addon JAR, exact runner, metadata, measured calls,
process observations and hash index are retained here. All five generated native
exports were successfully captured after exit; their large payloads remain local
under `/tmp/fd-engineered-availability-b0bf7bc/native-exports/`. The retained capture
manifest does not substitute for those payloads or establish new geometry acceptance.

The regression proves callback safety for motors whose source is a **4D chassis**.
It does not test a motor on an ordinary recovery bearing coupled to a 4D car; that
adjacent missing-component callback path remains unverified. It also does not
prove natural chunk cycling, automatic chassis relinking, occupied restoration,
GUI/FE workflows, rendering, all optional profiles or the full release gates.
The prior failing baseline and incomplete fixture runs remain retained unchanged.
