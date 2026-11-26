# Maximum ordinary native workload and complete show turn — 2026-11-26

The focused `MovingMaximumShowGameTests.maximumNativeShowTurn` passed **1/1** on Java 21, with clean server save/shutdown. This closes the physical full-show-turn and measured maximum-admitted-workload observations. It does **not** certify a performance budget, passenger compatibility, or a client experience. The prior four-car admission/fifth-car refusal remains accepted and was not repeated.

## Actual case and assertions

A production `MovingConstruction.build` created a **48-block native_service turntable**, four original minimal native cars were spawned at 9/15/21/27 m and actually coupled at 6 m, and the authenticated production operations captured and moved the complete train. A creative fake operator supplies the build/control requests; this is a headless physical GameTest, not player input evidence. No motion, body transform or rail identity was injected after capture.

The selected program is a full retained **360° show followed by the 90° destination selection**. The test calls production `emergencyHold` at 35% of nominal program time, observes a stopped disconnected `FAULT_HOLD`, then invokes authenticated `RESUME`. At every GameTest tick while program time is positive and before final `RECONNECTED`, it asserts both locks are disengaged and `liveDockEdge` is absent. Thus passing the initial/final ports during the show does not expose an intermediate bridge. The final assertions require an authenticated bridge, measured pose within unchanged adapter tolerances, stopped native bodies, all four original identities and each body's complete retained rigid transform within 0.05 m.

| Observation | Result |
|---|---:|
| Actual accumulated native yaw | 7.853981549680789 rad = 449.999995° |
| Maximum final car COM transform error | 0.0002515934 m |
| Native physics samples | 2,868 at 0.025 s each |
| Stop-to-first-stopped time | 0.75 s |
| Greatest origin displacement during hold | 0.00000724837 m |
| Greatest angular displacement during hold | 0.0236850572 rad = 1.35705° |
| Retained physical load | one bed + four native car bodies |
| Native mass tracker total | 151.0 native mass units |
| Production per-linear-axis force cap | 3,624 native units |
| Production per-angular-axis torque cap | 36,240 native units |

The mass-derived caps are the adapter's actual actuator settings, not measured contact force, torque draw or Create stress consumption.

## Actual derivatives

The collector observes native velocities before each physics step and differences them using the actual 0.025 s callback timestep. It never writes body pose or actuators. Values include numerical/transient responses; no smoothing or tolerance relaxation was applied.

| Maximum | Motion/resumed motion | Emergency hold |
|---|---:|---:|
| Linear speed (m/s) | 0.00111448 | 0.000659309 |
| Angular speed (rad/s) | 0.253919735 | 0.241871552 |
| Linear acceleration (m/s²) | 0.0340351 | 0.0149150 |
| Angular acceleration (rad/s²) | 0.0692049 | 1.2331784 |
| Linear jerk (m/s³) | 2.1686078 | 0.8520985 |
| Angular jerk (rad/s³) | 2.0093964 | 70.0431332 |

These are physical measurements, **not a claim that all actual derivatives meet the analytic program bounds**. In particular, measured motion angular jerk exceeds the commanded 0.261799 rad/s³ limit, and emergency braking has a different transient envelope. `derivative-peaks.json` identifies peak time and phase. The full raw CSV retains every sample for the milestone's physical-limit reconciliation.

## Timing and limits of interpretation

The interval begins immediately before `MOVE` and ends at final authenticated reconnection. Delta `CarrierRuntime.performanceSnapshot()` contains **8,604 global addon callbacks**, **3,339,187,082 ns**, average **388,097 ns/callback**. It covers this addon's callback across the running dimensions, including shared runtime work; it is not isolated turntable cost or Sable physics CPU. No other moving test runs in this server process. Other owners had independent processes on the host.

The unchanged legacy FD904 target is 120,000 ns/callback. **This maximum-load observation exceeds that value**; this record makes no performance-target pass claim and does not reopen unrelated accepted park runs. The integrator must reconcile the declared 1.5 workload against the applicable budget without silently widening it.

The fixture also samples Minecraft's preceding completed server tick and NeoForge dimension tick arrays once per GameTest tick (1,434 observations):

- Server tick mean **7.008967 ms**, maximum **3,124.662182 ms**. The sample interval includes the capture/Move transition tick, which performs synchronous geometry validation. No warmup/outlier was removed.
- Overworld tick mean **3.819629 ms**, maximum **10.538558 ms**. This is the native world-tick timer; it excludes work outside that timer.
- Public server average maximum **35.498322 ms**, final **3.254903 ms**. The log's historical field name `serverSmoothedTickMs*` denotes the public server average accessor, not a percentile.

Server/world timing includes their native tick scopes but does not isolate Sable solver time or renderer time. The GameTest server ran accelerated: simulated 71.7 s of collection took less wall time. This is not a sustained real-time park benchmark or client FPS test.

## Provenance, raw evidence and reproduction

Primary `main` HEAD was `f85f4a277c7c7542a4f07a158f18f580beb41187` plus shared uncommitted 1.5 work. Exact compiled addon classes and resources are preserved in `/mnt/NVME4/fourthdimension-runs/fd15-maxworkload-20261126/tested-addon-classes-resources.zip` (SHA-256 **67a816464fa1b74d49886288022c547039ad71fa9f8d4c7e35506b8bb990bd72**). This is a frozen compiled test payload, not a distributable release JAR. `artifact.json` records hashes for the payload, fixture, collector, template, log and physics CSV.

- [Complete passing log](run.log)
- [Artifact hashes](artifact.json)
- [Derivative peaks](derivative-peaks.json)
- [Cleanup verification](cleanup.json)
- Raw 40 Hz data: `/mnt/NVME4/fourthdimension-runs/fd15-maxworkload-20261126/physics.csv`
- Saved passed world: `/mnt/NVME4/fourthdimension-runs/fd15-maxworkload-20261126/world-passed`

Command, from the primary checkout, with Java 21 first on PATH:

```sh
./gradlew --no-daemon --max-workers=2 \
  --project-cache-dir /mnt/NVME4/fourthdimension-runs/fd15-maxworkload-20261126/cache \
  -PfdBuildDirectory=/mnt/NVME4/fourthdimension-runs/fd15-maxworkload-20261126/build \
  -PfdValidation -PfdGameTestNamespaces=fourthdimension_fd2007_maxshow runGameTestServer
```

Before the run: persistent XFS mount with 147 GiB free, host 46 GiB available after the setup refusal, inherited cgroup `memory.max=max`; no GUI/display was started. An initial compile caught two wrong API names before launch. The first launched fixture correctly refused its controller at 36 m inside the conservative stay/deck swept bounds. After inspecting that exact obstruction, the fixture controller moved to 44 m, inside the production 64 m origin limit; no production geometry or tolerance changed. The refused world and logs remain preserved. The one corrected physical run passed and testing stopped.

The server saved overworld/nether/end and sublevels, Gradle returned 0, and `/proc` inspection found no Java process referencing this run directory. No run units or displays were created. No commits or central queue edits were made by this owner.
