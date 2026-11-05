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
pass it; the [current artifact](CURRENT-QUALIFICATION.md) still needs applicable
render qualification. The gate does not require decorations or a shader. Every 100 client ticks
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

**Status: planned; no whole-frame/tick or memory baseline is claimed.** This
protocol extends the existing subsystem checks. The 120,000 ns/invocation and
6,000 ns/carrier limits above remain unchanged in all four native profiles.

### Fixed workload and hardware

Prepare and hash one reusable park world within the supported ±1024 x/z region:
an original two-car Arrow circuit, one Legacy, one Free Spin with inducer and
damper spans, one Revised, and a four-car PAX shuttle. This is nine chassis;
record actual carrier, rail, span, rider and loaded-chunk counts in the manifest.
Keep the layouts separate, load their required chunks, and repeat normal dispatch,
travel and capture. Occupy the native seats with declared fixture riders, replacing
one with the connected player. Include both a repeatable park camera route and
an occupied ride view for every family; pin their order and duration. Fixtures
establish a benchmark workload, not FD403 survival construction acceptance.

Run **base and combined** profiles on the same world, using Original appearance
for base and a saved single/double Arrow style plan plus declared Engineered
controls for combined. Keep the world/profile variants separately hashed.
Styles-only and Engineered-only retain native target checks and their rendering
acceptance; extend this park matrix if they show a regression absent in combined.

The intended hardware tier is the recorded AMD Ryzen 9 7900X3D / Radeon RX 7900 XTX
development host. Record actual CPU, RAM, GPU/OpenGL renderer, driver, kernel,
Java, display resolution and dependency hashes at run time; old host observations
are not a substitute. Start at 1280×720, fixed FANCY mode, 12-chunk render and
8-chunk simulation distance, VSync off, unlimited frame rate, and no shader.
Use the same settings for baseline and candidate. Record the selected Flywheel
backend. Supported shader runs remain separately labeled FD904A measurements.

Follow [AGENTS.md](../AGENTS.md): project on the development host, Java 21,
task-owned private Xvfb and explicit `DISPLAY`, 1536M client heap, an enforced
3 GiB total client-launch cap, and memory/process preflight. Run one graphical
client at a time for this benchmark; give the dedicated server its own world,
port and recorded heap/process budget. Never move the test onto the user's desktop.
If that private display exposes llvmpipe, record a separate software-renderer
baseline; it cannot close the native-GPU target. An unavailable GPU context is a
specific open FD904B item, not permission to use another agent's display.

### Measurements and frozen budgets

1. Add or select verified collectors for whole-client frame intervals and whole
   server tick execution time, plus once-per-second heap, process RSS, cgroup
   usage/peak and GC pause data. Preserve raw timestamps and identify whether a
   collector measures CPU frame submission or actual presentation; record both
   when available. Existing `FD_CLIENT_RENDER` stage timings cannot substitute.
2. Select the baseline JAR before testing the candidate. Warm the fully loaded
   world for five minutes, then take three independent five-minute samples of
   the fixed view/ride sequence for each profile. Publish median, p95, p99,
   maximum, and sample count for frame and tick times; report frames over
   50/100 ms and ticks over 50 ms separately. Preserve all samples, including
   stalls, without deleting outliers. Isolate startup/loading in its own record.
3. Freeze `park-baseline.json` with artifact/world/settings/hardware hashes and
   measured budgets before the candidate comparison. For each profile, the
   proposed regression ceilings are **110% of the median across the three
   baseline runs' p95 and p99 values**, calculated separately for frame and tick
   times. Freeze absolute numbers in milliseconds. These relative ceilings do
   not make an already unplayable baseline acceptable: baseline review also
   requires sustained 20-TPS operation (p95 tick work <=50 ms), and records the
   observed frame-rate tier without promising a universal FPS target.
4. Repeat the three samples on the candidate under identical conditions, then
   run the normal mixed-family dispatch/capture workload continuously for
   **30 minutes per profile** after warmup. Require all three comparison samples
   and the sustained window to meet the frozen p95/p99 ceilings and tick budget.
   Preserve the per-family view segments so a fast empty view cannot hide a bad
   ride. A noisy or failed run remains evidence; diagnose it before a retry.
5. For memory, freeze a plateau budget from the baseline's last two minutes of
   each sample: candidate median post-GC live heap and RSS must be <=110% of
   their baseline medians, remain inside declared process/cgroup caps, and show
   no continuing increase across the sustained run's final three five-minute
   windows. Record those window values and GC p95/p99/max. If no natural GC
   provides comparable live-heap samples, use a declared identical endpoint
   measurement protocol for both artifacts, outside timing windows; do not
   infer a live-heap value from allocated heap or force GC only on one build.
6. End with capture, unload, normal save/shutdown and a cold reopen. Retain the
   same identities and one successful normal dispatch/capture after reopening.
   No OOM, lost rider, save failure, accumulating orphan bodies or sustained
   tick backlog is acceptable. Verify the launch scope stayed active for the run.

The first full baseline establishes numbers; it does not itself close a regression
comparison. Repeat the identical baseline artifact independently if it is also
the first candidate, then compare later builds against the frozen record.
Budget changes require a documented workload/hardware reason and a new baseline;
never raise a threshold solely to turn a failing candidate green.

### Evidence and completion

Retain `park-baseline.json`, candidate results, raw frame/tick/memory/GC samples,
collector versions/source hashes, world/profile manifests, launch commands,
screenshots identifying each view, exit codes and a machine-checkable budget
comparison. Until those artifacts exist, FD904B stays planned/in progress.
This nine-chassis benchmark does not certify maximum-capacity parks: 64 vehicles
and other admission limits are separate correctness boundaries, tested by FD901.
