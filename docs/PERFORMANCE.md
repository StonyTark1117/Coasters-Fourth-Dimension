# Runtime performance sampling

`/fd performance` reports the cumulative server-side time spent in Fourth
Dimension's carrier runtime since the last reset: physics-tick count, total
nanoseconds, and average nanoseconds per runtime tick. It measures this addon's
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

The controlled train, free-spin and PAX shuttle scenes still need baseline
runs, targets, renderer/shader measurements and acceptance results before FD904
can close. The automated controlled-family GameTest also resets this sample for
its isolated 1,800-tick occupied Arrow/legacy circuit window and requires that
window to be observed. It emits a diagnostic snapshot but does not turn the
current host's timing into a release target. This command and harness are
instrumentation, not a performance guarantee.
