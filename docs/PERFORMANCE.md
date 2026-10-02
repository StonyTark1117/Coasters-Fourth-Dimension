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

The controlled train, free-spin and PAX shuttle scenes still need recorded
host baselines, targets, renderer/shader measurements and acceptance results
before FD904 can close. The automated controlled-family GameTest resets this
sample for its isolated occupied Arrow/legacy circuit window and requires at
least 10,000 carrier updates. The Free Spin GameTest does the same for an
occupied eight-rider magnetic circuit over at least 8,000 carrier updates. The
PAX GameTest samples an occupied 16-rider lift, gravity return and
station-capture cycle, requiring at least 1,600 carrier updates. All three
write their diagnostic snapshot through the game logger but do not turn the
current host's timing into a release target. This command and harness are
instrumentation, not a performance guarantee.

The current integrated GameTest result is recorded in the
[FD904 baseline evidence](evidence/0.9/runtime-baseline.md).
