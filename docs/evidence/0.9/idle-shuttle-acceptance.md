# FD902: idle shuttle native availability

This is a bounded **0.9.0-dev.13** lifecycle checkpoint. The current release stage
remains completion of 0.4. It closes the reproduced idle-winch failure path, not
FD902 or the complete 0.9 candidate gate. Save schema and network protocol remain 5.

## Failure and scope

Dev.12's cold client saved four PAX cars with `Shuttle lead car unavailable` and
closed restraints. Its winch had never lifted: BOARDING, zero cycles and no
carriage. A source-supported restoration-gap hypothesis needed direct evidence.

An instrumented control retains the original controller behavior and adds only
opt-in availability logging. It is separately named `0.9.0-dev.13-control`, not a
release artifact. A normally assembled four-car shuttle in a disposable dedicated
world reopens with all cars already available when the entire route is force-loaded.
That run does **not** reproduce the defect.

A closed dev.12 client world is copied into an isolated game directory. Normal
teleport and `/fd demo pax` commands create a second shuttle at (350,85,-35), with
its winch at **(344,85,-27)**. All four new cars are initially captured, unrestrained
and fault-free. The four older PAX faults are retained in the copied world; no
addon NBT is changed. A normal save/exit preserves this pristine comparison world.
The control's ordinary close-range cold reopen is also fault-free for the new train.

Moving the player to **(128.5,90,-26.5)** on a normal stone viewing platform reproduces
the exact fault. At game tick **41359**, the controller first observes BOARDING,
an available winch, no native lead and zero loaded cars; in the same tick it enters
FAULT with `Shuttle lead car unavailable`. The saved control has that fault and
closed restraints on all four formerly clean cars. All 46 rail records remain
unchanged. This proves the failure path at a loading boundary; it does not recover
the original dev.12 restart's unrecorded per-tick timing.

## Change

An idle winch with no carriage has no native constraint to advance. After checking
for actual block removal, it now leaves BOARDING state alone while Sable holds or
restores its train. This is a state-based rule, with no timeout, forced chunk load,
body allocation, fault reset or readiness override.

`start()` still validates all native chassis/carriers, the actual linked consist,
matching functional tracks, restraints, current readiness, complete route and
power before creating the lift. Active phases, a boarding winch with an existing
carriage, and explicit recovery retain their existing behavior. Previously saved
faults are not automatically cleared.

`-PfdShuttleValidation` logs changes in availability and phase. It does not load
bodies or alter operating decisions, and defaults to false in every run profile.

## Native regression

The new GameTest puts the actual native lead and its dependency chain into Sable's
holding system. It verifies the lead is retained there rather than deleted, then
observes the same availability gap through the production shuttle tick.

Against the original controller it fails with FAULT, a held/unloaded lead and four
closed restraints. The control completes 29 tests with exactly this one required
failure. With the fix, the test preserves phase, faults, restraints and revisions;
an operator start while the lead is unavailable rejects without allocating a
carriage or advancing phase. Normal Sable restoration returns four cars, eight
carriers and the four-car native consist. Deleting an actual route anchor still
rejects dispatch. Standalone and combined Track Styles 1.2.0/Engineered 1.1.1 runs
each pass all **29 required tests** and exit zero. The Java 21 build also passes
all **25 unit tests** with no failures, errors or skips.

## Packaged comparison

The fixed client starts from a second copy of the same pristine comparison world.
Repeating the same viewing-platform/teleport sequence reaches an available winch
with no loaded lead or cars at tick **40531**, while preserving BOARDING and an
empty fault. A vanilla block query confirms zero cycles and no carriage. Normal
save/quit and game exit finish with exit zero. The saved rails and all chassis IDs
match the pristine data; the four tested cars retain their non-angular state,
restraint values and revisions.

A fresh packaged client then reopens from that boundary with all four validation
flags false. A vanilla block query still reports BOARDING and an empty fault.
Returning normally to (343.5,85,-26.5) restores the visible train. The final saved
comparison and normal shutdown are recorded alongside this document.

## Limits and next lifecycle work

This fixture tests an empty, bound shuttle before its first lift. It does not
establish an occupied or caught-carriage cold-reopen gate, and does not exercise
the normal operator recovery of previously saved faults. The inherited four PAX
faults remain explicit in the comparison data; this is not a fault-free five-family
world acceptance. Broader lifecycle, migration, rendering/performance and complete
0.4 acceptance remain open. The original dev.8 bitmap-glyph crash is still unresolved;
the dev.12 scheduler fix and this shuttle change do not claim a font fix.

Run `python3 scripts/verify-idle-shuttle-checkpoint.py` to check these recorded
observations. It reads the evidence and does not replay clients or close any open gate.

The fixed packaged JAR SHA-256 is `68046fab96ebca81ce1e639337ea8fd189b52ef37ad413d881849f56175280ac`, unchanged across the standalone and combined builds. The diagnostic control JAR SHA-256 is `da0ed73980f50bdee686b6470586cfa2023a7add70c3caa31a9878fe4181489e`. The control native test uses the unchanged controller with the new regression through the development source set. Neither artifact bundles optional addon JARs or copied upstream implementation.

## Subsequent lifecycle checkpoint

The [dev.14 caught shuttle checkpoint](caught-shuttle-acceptance.md) records normal recovery of the inherited PAX faults and a separate caught-boarding restoration fix. The original observations above remain historical evidence; the full FD902 gate remains open.
