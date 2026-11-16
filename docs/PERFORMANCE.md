# Runtime performance sampling

`/fd performance` reports cumulative server-side work in Fourth Dimension's
carrier runtime since the last reset. It records whole runtime-invocation count,
total nanoseconds, and average nanoseconds per invocation; it also records the
number of live carrier updates, their total and average nanoseconds, and the
number of rebuilt carrier constraints. Invocation time includes this addon's
shared carrier, shuttle, recovery, magnetic and boarding work. Carrier time
covers the individual live-carrier body after its parent and child have passed
validation. It deliberately does not report whole-server tick time, Sable
physics, rendering, shaders, networking, or client FPS.

For a reproducible FD904 baseline, use a clean server with the exact dependency
profile and scene documented beside the result:

1. Start the server and let the selected scene settle.
2. Run `/fd performance reset`.
3. Run the scene for a recorded number of physics ticks with no unrelated build
   or editor work.
4. Run `/fd performance` and record its output, vehicle/carrier counts, Java
   version, hardware, optional-addon profile and scene duration.

On the target host recorded in the baseline, the declared server-side limits are
at most **120,000 ns per runtime invocation** and **6,000 ns per live carrier
update**. They are deliberately host- and workload-scoped; they do not promise
whole-server tick time or FPS. `-PfdPerformanceTargets` enables these two
limits in the three GameTest scenes below. The automated controlled-family
GameTest resets its isolated occupied Arrow/legacy circuit window and requires
at least 10,000 carrier updates. The Free Spin GameTest does the same for an
occupied eight-rider magnetic circuit over at least 8,000 carrier updates. The
PAX GameTest samples an occupied 16-rider lift, gravity return and
station-capture cycle, requiring at least 1,600 carrier updates. All three
write their diagnostic snapshot through the game logger. This command and
harness are host-scoped instrumentation, not a universal performance guarantee.

The current integrated GameTest result is recorded in the
[FD904 baseline evidence](evidence/0.9/runtime-baseline.md).

## Client track-render sampling

For a real-client render sample, launch an existing functional-track world with
`-PfdValidation -PfdRenderValidation`. Add `-PfdRenderValidationExit` to make the
client stop normally after the configured `-PfdRenderValidationStages=N` original-track
`geometryStages` (default 500). This opt-in completion gate requires at least one mesh
build and at least N `geometryStages`, each with both an in-range original rail
and a procedural quad. Empty loading or transition stages do not count toward
N. Early packaged render records predate the `geometryStages` counter and
do not establish this stronger condition. Later exact `2c5d9a7`
[FAST/FABULOUS records](evidence/0.9/fd401-render-modes-exact-2c5d9a7/README.md)
pass it. Carry those results forward when the relevant rendering inputs are
unchanged; [current qualification](CURRENT-QUALIFICATION.md) records later Styles
acceptance. A different artifact hash alone does not require another 500 stages. The gate does not require decorations or a shader. Every 100 client ticks
the log emits
`FD_CLIENT_RENDER`, which includes cumulative render-stage, `geometryStages`
and mesh-build counts, average nanoseconds for each, and cumulative in-range original-rail,
procedural-quad and optional-decoration-piece counts within the renderer's
160-block distance test. The counters reset when
the client level changes. They time this addon's `AFTER_BLOCK_ENTITIES` track
stage only; they do not measure Minecraft's whole frame, GPU work, Flywheel,
Veil or shader time, and cannot establish an FPS target.

Shader-mode sampling additionally records the shader loader, exact shader pack,
GPU, and Flywheel backend chosen at startup. A loaded pipeline and nonzero
`FD_CLIENT_RENDER` samples prove that this addon's stage ran while the shader
was active; they do not prove image quality, whole-frame performance, or every
Flywheel backend. Record a backend fallback as a limitation of that run rather
than treating it as full rendering acceptance.

With the pinned Flywheel 1.0.6, both built-in accelerated backends require no
shader pack to be active. Iris/Solas therefore causes Flywheel to select `off`;
that expected upstream behavior does not stop this addon's direct render stage,
but it prevents an accelerated-Flywheel claim for shader-mode sampling.

## Representative park acceptance (FD904B)

**Status: complete (2026-11-16 UTC).** [The fixed park evidence](evidence/1.0/lead/park/README.md)
covers initial base/combined acceptance, the30-minute sustained workload/memory
plateau, all17 mounted views and candidate17's affected comparisons at the
previously frozen later-regression limits. All owned park runtimes saved and stopped.
Admission limits remain separate from the measured workload's performance tier. The120,000ns/invocation
and6,000ns/carrier limits above remain unchanged. Reuse accepted native profile
checks unless a performance-relevant change affects them; they are not a
four-profile prerequisite to every park measurement.

FD502 owns early collector readiness and useful development measurements. Final
expanded-park acceptance is scheduled after FD1000; earlier valid subsystem and
unchanged workload results remain reusable. Do not run a full park benchmark at
every family milestone or compare different workloads as though only the JAR changed.

### Fixed workload and hardware

The original core workload is a reusable park within the supported ±1024 x/z region:
an original two-car Arrow circuit, one Legacy, one Free Spin with inducer and
damper spans, one Revised, and a four-car PAX shuttle. This is nine chassis;
record actual carrier, rail, span, rider and loaded-chunk counts in the manifest.
Keep the layouts separate, load their required chunks, and repeat normal dispatch,
travel and capture. Occupy the native seats with declared fixture riders, replacing
one with the connected player. Include both a repeatable park camera route and
an occupied ride view for every family; pin their order and duration. Fixtures
establish a benchmark workload, not FD403 survival construction acceptance.

At FD1000, extend that workload to include each added motion mechanism and the
agreed distinct variants: fixed Wing, Spinning, Suspended, Axis, Pipeline and
Roller Ball. Use their retained example layouts where possible; record actual
chassis/carrier/rider/chunk counts for the chosen designs instead of retaining
“nine chassis” as a claim about the expanded scene. Freeze its route, view sequence
and settings once. A changed workload gets its own baseline; old measurements
remain attached to the scene they measured. No per-family rebuild is required.

Run **base and the frozen combined** profiles on the same world, using Original appearance
for base and a saved single/double Arrow style plan plus declared Engineered
controls for combined. Keep the world/profile variants separately hashed.
Individual optional profiles retain their accepted applicable native/render
checks; add a separate park profile only for a performance-relevant path absent
in the combined scene or an observed regression. Avoid benchmarking every addon
subset or every family/profile combination.

The intended hardware tier is the recorded AMD Ryzen 9 7900X3D / Radeon RX 7900 XTX
development host. Record actual CPU, RAM, GPU/OpenGL renderer, driver, kernel,
Java, display resolution and dependency hashes at run time; old host observations
are not a substitute. Start at 1280×720, fixed FANCY mode, 12-chunk render and
8-chunk simulation distance, VSync off, unlimited frame rate, and no shader.
Use the same settings for baseline and candidate. Record the selected Flywheel
backend. Supported shader runs remain separately labeled FD904A measurements.

Follow [AGENTS.md](../AGENTS.md): project on the development host, Java 21,
task-owned private Xvfb and explicit `DISPLAY`, default 1536M client heap, and
an enforced 6 GiB Minecraft-only scope with memory/process preflight. Prepare the
direct Java 21 launch script outside that scope; Gradle must not share its cap.
Raise heap/scope only as measured need and host headroom require, recording the
choice. Run one graphical client at a time; give the dedicated server its own
world, port and recorded budget. Historical successful runs at other limits
retain their behavior evidence. Compare memory results only under comparable
settings. Never move the test onto the user's desktop.
If that private display exposes llvmpipe, record a separate software-renderer
baseline; it cannot close the native-GPU target. An unavailable GPU context is a
specific open FD904B item, not permission to use another agent's display.

### Measurements and frozen budgets

1. Add or select verified collectors for whole-client frame intervals and whole
   server tick execution time, plus once-per-second heap, process RSS, cgroup
   usage/peak and GC pause data. Preserve raw timestamps and identify whether a
   collector measures CPU frame submission or actual presentation; record both
   when available. Existing `FD_CLIENT_RENDER` stage timings cannot substitute.
2. Select one baseline artifact and reuse the fixed world/settings. Warm the
   fully loaded world for five minutes, then take **one five-minute sample per
   base/combined profile** of the view/ride sequence. Publish median, p95, p99,
   maximum and sample count for frame and tick times; report frames over
   50/100 ms and ticks over 50 ms separately. Preserve stalls and outliers;
   isolate startup/loading in its own record. Repetitions are for a diagnosed
   measurement issue, not a mandatory three-run ritual.
3. Record `park-baseline.json` with identities, settings and hardware. Initial
   acceptance requires sustained 20-TPS operation (p95 tick work <=50 ms), the
   sustained/memory checks below, and publication of the observed frame-rate
   tier and its limits. It does not claim a universal FPS target. Freeze later
   regression ceilings at **110% of the baseline p95 and p99** for frame/tick
   times, expressed in milliseconds. Do not raise a threshold to pass a regression.
4. Run the mixed-family dispatch/capture workload for **one 30-minute combined
   profile window** after warmup. Preserve per-family view segments so an empty
   view cannot hide a bad ride. Base gets its five-minute sample; add a base
   sustained window only if its code path differs in a performance-relevant way
   or a measured problem requires it. Reuse an accepted sustained window on
   later builds without relevant runtime/allocation/render/dependency changes.
5. Record post-GC live heap, RSS, cgroup usage/peak and GC p95/p99/max. Stay
   inside the declared budgets and show no continuing memory increase across
   the sustained window's final three five-minute periods. Freeze the baseline
   plateau values. For a later changed artifact, median post-GC heap and RSS
   must be <=110% of comparable baseline medians. If natural GC is insufficient,
   use the same declared endpoint measurement protocol on both artifacts,
   outside timing windows; do not confuse allocated heap with live heap.
6. End with capture, unload and normal save/shutdown. Reuse applicable cold-reopen
   acceptance from FD902/FD903; add a reopen only for an uncovered or changed
   persistence path. No OOM, lost rider, save failure, accumulating orphan bodies
   or sustained tick backlog is acceptable. Verify the launch scope stayed active.

**The initial baseline can also be the first accepted candidate.** Its one set
of measurements closes initial measured acceptance when the criteria above pass;
do not rerun the identical artifact to manufacture a baseline/candidate pair.
A comparative improvement/regression claim requires an actual comparable second
measurement, but that claim is not an extra prerequisite for initial acceptance.

For later performance-relevant changes, take one comparable five-minute sample
in each affected profile and compare to the frozen ceilings. Repeat the sustained
window only for an affected sustained-load or memory path. A noisy or failed result
remains evidence: diagnose before retrying, choose a bounded trial count, and
report unresolved variance rather than launching indefinitely. Workload, collector,
or hardware changes may need a new baseline; document the reason and keep the
old one. Unrelated fixes and documentation/test-only commits do not restart this
protocol. Existing subsystem checks remain separate from whole-frame measurements.

### Evidence and completion

Retain `park-baseline.json`, raw frame/tick/memory/GC samples, collector versions,
world/profile manifests, launch commands, view references and exit codes.
For a later distinct candidate comparison, also retain its results and a
machine-checkable budget comparison. Initial acceptance needs the baseline and
sustained results only; a duplicate same-artifact comparison is not required.
Until the applicable measurements exist, FD904B stays planned/in progress.
Neither the original nine-chassis scene nor the expanded frozen workload certifies
maximum-capacity parks: 64 vehicles and other admission limits remain separate
correctness boundaries covered by FD901.

The 1.0 [park acceptance record](evidence/1.0/lead/park/README.md) is complete
(2026-11-16 UTC): initial base/combined measurements, all17 mounted styles,
combined30-minute workload and memory plateau, and renderer17's affected later
comparisons are accepted. The [classifier correction](evidence/1.0/lead/park/renderer-repair.md#combined-comparison-and-evaluator-correction)
retains the original failed initial-limit check and all samples; the already-frozen
later limits are unchanged. All owned park runtimes saved and stopped.
