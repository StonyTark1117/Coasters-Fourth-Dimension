# Availability guard: partial native result

Source `3bc57cf391fbea8b4dc1e360e39ef46a9e938a87` contains the narrow physical
availability guard and independent native fixture cases. Its Engineered-only
development run completed 51 tests with one required failure, saved/stopped
normally and returned exit 1. It is **not a complete availability acceptance pass**.

The same five direct calls that failed open on `4c2eb51` now have maximum queued
force/torque delta **0.0**. Ten healthy controls exceed 0.001. Cases 0/1 each
unloaded/restored a carrier with native two-car link continuity. Case 2 blocked
the surviving source with a peer chassis unavailable, then restored that body
identity. The native partial chassis restore produced [1,1] consist sizes; the
explicit fixture relink returned false, ending the test before chassis post-restore
motor controls. No successful `fixtureRelink` marker exists. Cases 3–5 did not run.

Both suites overlapped from the start: the shared-root performance-enabled
Engineered run began around 11:13:37 UTC, and this run around 11:13:47 UTC.
`overlap-observation.json` records process ownership and estimated start times.
Do not describe this run or its partial motor checks as isolated performance evidence.

The source/JAR identity, class parity, exact command and actual exit are retained
with full compressed logs and the matching JAR. `run.py` recorded timestamps and
captured all five native exports after process completion. Their large payloads
remain local under `/tmp/fd-engineered-availability-3bc57cf/native-exports/`;
the capture manifest here is not a substitute for those payloads or new geometry acceptance.

The next agreed fixture removes the chassis relink dependency: each chassis case
requires its one surviving-source safety probe, restored body identity/availability,
and cleanup. Post-restore motor controls remain only for carrier cases with proved
link continuity. Fresh cases and actual counters require six unloads, ten unavailable
calls and twenty positive controls. This does not prove natural chunk cycling,
automatic train-link restoration, occupied operation or the full FD401/release gate.
The production guard stays unchanged for that fixture revision.
