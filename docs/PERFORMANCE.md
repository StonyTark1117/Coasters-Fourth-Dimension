# Runtime performance sampling

`/fd performance` reports the cumulative server-side time spent in Fourth
Dimension's carrier runtime since the last reset: runtime-invocation count,
total nanoseconds, and average nanoseconds per invocation. It measures this addon's
carrier, shuttle, recovery, magnetic and boarding updates only. It deliberately
does not report whole-server tick time, Sable physics, rendering, shaders,
networking, or client FPS.

For a reproducible FD904 baseline, use a clean server with the exact dependency
profile and scene documented beside the result:

1. Start the server and let the selected scene settle.
2. Run `/fd performance reset`.
3. Run the scene for a recorded number of physics ticks with no unrelated build
   or editor work.
4. Run `/fd performance` and record its output, vehicle/carrier counts, Java
   version, hardware, optional-addon profile and scene duration.

The controlled train, free-spin and PAX shuttle scenes still need recorded
host baselines, targets, renderer/shader measurements and acceptance results
before FD904 can close. The automated controlled-family GameTest resets this
sample for its isolated occupied Arrow/legacy circuit window and requires at
least 1,800 runtime invocations. The Free Spin GameTest does the same for an
occupied eight-rider magnetic circuit over at least 2,200 runtime invocations.
The PAX GameTest samples an occupied
16-rider lift, gravity return and station-capture cycle, requiring at least 200
runtime invocations. All three write their diagnostic snapshot through the game
logger but do not turn the current host's timing into a release target. This
command and harness are instrumentation, not a performance guarantee.

The current integrated GameTest result is recorded in the
[FD904 baseline evidence](evidence/0.9/runtime-baseline.md).
