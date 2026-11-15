# FD813 combined operations profile — qualified

Qualified 2026-11-15 with Java 21 on the unchanged core. The named development
profile is `styles,engineered,additions,ctnb,magnetized`: Track Styles 1.2.0,
Engineered 1.1.1, CTNB 1.2, Magnetized 1.1.0 and Additions' actual coasterfins0.2.2,
plus its selected Aeronautics/Interiors closure. Exact artifact decisions and
hashes are in [dependencies](../dependencies/README.md); the log records loaded
identities. No selected addon is bundled in the Fourth Dimension JAR.

[Combined03](combined-03.log), **1/1 passed**, runs a native Wing body on the
selected CTNB Boost material with a real kinetic Chain source, powered Magnetized
anchor, configured Engineered LAUNCH controller and actual Additions Station on
the same span. It checks behavior, not startup alone:

- CTNB ownership receives exactly one bounded `.4 blocks/s` callback impulse,
  despite the other installed propulsion callbacks.
- The configured Engineered callback queues positive propulsion when selected as
  a test positive control; it queues zero extra force under CTNB or Magnetized
  ownership. The collector restores queued force/torque after each probe.
- A held transfer from CTNB to Magnetized invalidates the old CTNB token. The
  receiving release applies exactly one impulse from the real Magnetized API;
  CTNB material does not add a second drive.
- Actual Additions automatic-dispatch requests cannot steal either foreign owner,
  while its read-only whole-car telemetry remains available.
- Held actual Chain/optional/Engineered callbacks produce no propulsion.

The positive-control provider field and velocity resets are disclosed fixture
inputs for force invariants, not normal player transfer evidence. Normal occupied
controls are established separately by [authority](../authority/README.md),
[CTNB](../ctnb/README.md), [Magnetized](../magnetized/README.md) and
[Additions](../additions/README.md). Their finite family and capability boundaries
remain in effect. This is the uncovered installed conflict case, not a new full
family/profile sweep or 0.9 presentation/seat certification.

Combined01 failed before executing the case because its empty structure resource
was missing. The resource was added. Combined02 reached the positive control but
incorrectly measured immediate velocity for Engineered's queued-force API.
Inspection of the installed API and the existing accepted interlock collector
identified the mismatch. Combined03 measures queued propulsion and passes both
positive and denied callbacks. No production fix or expanded acceptance was needed.
Both raw failed logs remain [01](combined-01.log)/[02](combined-02.log).

Owner `01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary `main`, persistent root
`/mnt/NVME4/fourthdimension-runs/fd08-operations-20261115`. The headless test uses
`combined-build/runs/gametest`; saved02/03 worlds are preserved outside build.
All test Java processes exited;03 saved every dimension and Gradle exited0.
No display/client was launched for this case. The three connected adapter runs
and both family owners have separate verified cleanup records.
