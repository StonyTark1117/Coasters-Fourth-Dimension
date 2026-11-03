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
stages (default 500). This opt-in completion gate requires at least one mesh
build and at least N `geometryStages`, each with both an in-range original rail
and a procedural quad. The cumulative rail and quad totals must also each reach
N. Historical packaged render records predate the `geometryStages` counter and
do not establish this stronger condition; it requires a fresh packaged-client
run. The gate does not require decorations or a shader. Every 100 client ticks
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
