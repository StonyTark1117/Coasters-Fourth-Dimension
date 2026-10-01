# PAX phase-specific native chunk recovery

Extending the occupied lift test to outbound travel, returning and capture exposed
five runtime defects:

1. An interrupted capture rebuilt its catch before the empty carriage had reached
   the train. The first regression measured a 3.31-block lead-car jump between ticks.
2. The winch checked its block state before checking physics availability. During
   unloading, that lookup could renew a FULL chunk ticket around the old winch.
   Native bodies unloaded, but world block entities remained retained. Diagnostics
   show the winch chunk at ticket level 33 with adjacent chunks inaccessible.
3. A gravity-phase reload did not rebuild the empty carriage's world guide. Its
   body fell after reload; the new position check caught movement beyond 0.3 blocks.
4. A carriage could return before its winch chunk became available. Waiting for the
   winch before recreating the guide left a load-order gap; a repeated base run
   measured 1.607 blocks of carriage travel outside the settling envelope.
5. The loading dependency hook expanded carriers and linked chassis in separate
   passes. Starting from some carriers returned only three of twelve train bodies.
   That permits a coupled train's carriers to unload in separate groups. A native
   dependency assertion reproduced this in both outbound and lifting fixtures.

## Runtime corrections

`ShuttleRuntime` now checks native physics availability before reading the winch's
block state or block entity. Loaded removal still faults the train and performs the
existing cleanup; an unavailable chunk does not get loaded by that check.

The catch now verifies that its two actual attachment points are within 0.25 blocks
before creating a new constraint. Existing valid joints remain intact. Lifting
refuses an unaligned catch; capture keeps approaching before engaging. This also
prevents a faulted capture from snapping a separated train to its carriage on reload.

Every faulted phase rebuilds the carriage's world guide when its native body returns,
including while the winch chunk remains unavailable. The retained winch metadata
supplies the carriage identity without a world-block lookup.
Only the original caught phases attempt to restore the train catch, subject to the
same proximity check. An interrupted gravity run retains an uncaught train and an
anchored empty carriage. No train teleport, launch impulse, persistence schema
change or external PAX propulsion was added.

The loading dependency hook now expands native linked chassis and all their rotary
carriers until the group stops growing. It explicitly calls Coasters' link expansion,
so a chassis introduced by the carrier hook receives its linked partners regardless
of RETURN-injection order. It does not recursively call Sable's dependency helper.

## Test contract

`ShuttleChunkGameTests` contains four independent occupied cases, one for each moving
phase. Before eviction, every one of the twelve train bodies must resolve to a
native loading group containing all twelve. During reload, the train must expose
either all twelve bodies or none, including in the partial-load interval. Each compact
three-section route carries sixteen native pigs on four coupled cars with eight carrier bodies and one physical carriage. Each case releases forced
chunk tickets during its target phase, requires actual removal of every original
body and block entity, then restores fresh instances with the original UUIDs.
Before restoring the full route, each case forces only the carriage chunk and
observes 100 ticks with the winch explicitly outside ticking range. The carriage
must remain within 1.5 blocks of its first returned position throughout this gap.
The lift interruption occurs farther up the spike so its carriage chunk is outside
the winch chunk's forced-ticket neighborhood. World probes use non-loading lookups.
Accelerated GameTests explicitly drain native save/unload work while waiting; this
is not natural eviction latency evidence.

The tests preserve the original rider-to-carrier assignments, inspect actual living
mounts, deny restraint opening in the interrupted state and require correct shared
telemetry: four cars, sixteen occupied seats, closed restraints, the current winch
phase and no dispatch permission while active or faulted. They watch initial reload
motion for jumps over 1.5 blocks per tick. The carriage must stay within a 1.5-block
initial settling envelope, then within 0.3 blocks of its position after 100 ticks
until an explicit recovery command. Initial travel, per-tick movement and settled
drift are recorded separately.

Lifting and capture require a 60-tick stable hold, with less than 0.3 blocks of
three-dimensional lead-car drift and an unchanged persisted carriage coordinate,
before explicit `resume()`. Outbound and returning have already released their
catch: `resume()` must reject with the manual-recovery instruction. Those tests wait
for the restrained train to brake and every carrier to become boarding-ready, then
call `recover()`. They do not reinterpret this uncaught state as an instantaneous
mechanical hold. Final capture must return all four cars to readiness, preserve
coupling and all riders, and permit normal unloading with every pig alive.

With Engineered 1.1.1, each case also unloads and reloads the real controller and
observer. Native commands compete with the winch in every applicable phase, using
the default `requireEnergy=false` configuration. Presence and observer checks must
pass. Outbound manual recovery deliberately skips the normal RETURNING phase;
the other cases require presence in all six phases. Other installed addons are
present in the combined profile; their active equipment is not added to these routes.

## Failed investigations retained

The initial run records the capture jump and incomplete block-entity eviction.
The capture-only correction passed that case but did not resolve the remaining
unload failures. Repeated save draining and cleanup of completed fixture tickets
were useful harness corrections, but did not fix the runtime's retained winch chunk;
the ticket-level diagnostics isolated that separate lookup defect.

The first run after the availability fix achieved physical eviction in every phase.
Its gravity tests still imposed the lift's stationary-hold assumption and rejected
continued braking travel. Those assertions were corrected to the documented manual
recovery contract. A separate empty-carriage position check then reproduced the
missing guide before the final runtime correction. No failed run is compatibility
evidence; full local log hashes and retained excerpts identify them separately.

An Engineered candidate applied the stationary carriage's 0.3-block limit from its
first loaded frame and rejected 0.325 blocks of initial lift settling. The final
probe distinguishes bounded initial settling from the subsequent fault hold, while
retaining independent displacement and step limits. This is a measurement correction;
the physical catch and guide behavior are not weakened to accommodate it.

A subsequent base repeat exposed the load-order gap despite passing the earlier
ordinary reload tests. After correcting it, a new staggered-load test passed capture,
returning and outbound but rejected the lift fixture: its initial carriage position
was too close to the winch, so one forced chunk made both tick. Moving the lift
interruption farther up the spike preserves the assertion and tests the intended
independent-load condition. Both failures are retained separately.

## Coupled-body reload investigation

The subsequent staggered matrix passed base, Engineered, Track Styles and Additions,
but stopped in Magnetized: outbound reload produced a 1.510-block lead step and two
native couplers snapped under tension. The other three interruption phases passed.
A diagnostic repeat passed all 32 registered tests, while showing the twelve train
bodies loading during the carriage-only interval, unloading again, and returning
on full restoration. That repeat did not establish a passing final matrix. The subsequent dependency
assertion failed in outbound and lifting: some seeds returned three bodies instead
of twelve. This directly establishes the loading-group defect. The dependency fix
then passed the direct diagnostic repeat and a fresh full matrix, including the
new assertion that all twelve train bodies load together. Immersion and combined
did not run in the stopped candidate matrix; both ran in the final matrix.

## Final verification

The final source passes all 32 registered native tests in each profile, with explicit
absent-addon skips distinguished below. All four PAX interruption cases execute in
every profile. The build and all 19 JUnit tests pass. `verification.json` records the
asserted per-phase measurements and Engineered controller sightings.

| Profile | Registered tests | Absent-addon skips | PAX phases completed |
| --- | ---: | ---: | ---: |
| Base | 32 | 5 | 4 |
| Engineered 1.1.1 | 32 | 3 | 4 |
| Track Styles 1.2.0 | 32 | 5 | 4 |
| Additions 0.2.0 | 32 | 3 | 4 |
| Magnetized 1.1.0 | 32 | 4 | 4 |
| Immersion 0.2.3 | 32 | 5 | 4 |
| Combined | 32 | 0 | 4 |

The largest carriage travel during the 100-tick unavailable-winch interval was
0.0756 blocks. Settled carriage drift measured zero in all 28 cases. The largest
observed initial lead-car step was 0.5486 blocks. Every case retained all sixteen
original rider assignments and completed normal unloading with living passengers.
The matrix verifier also checks exact core/addon versions and the Aeronautics and
Interiors dependencies; the complete mod lists are retained per profile.

## Reproduction

Use Java 21. Run native profiles sequentially because they share the GameTest world:

```sh
set -e
./gradlew build
./gradlew runGameTestServer
for profile in engineered styles additions magnetized immersion engineered,styles,additions,magnetized,immersion; do
  ./gradlew runGameTestServer -PfdIntegrations="$profile" || exit
done
python3 scripts/verify-pax-phase-recovery.py docs/evidence/integrations/2026-10-01/pax-phase-recovery
```

`runs.json` identifies full local logs, SHA-256 hashes and process exit statuses.
Committed excerpts include native checks, mod versions and warning/error lines.
`artifacts.json` identifies the original failing JAR, final built JAR, final runtime
and fixture sources, and evidence verifier. Native Gradle tests execute compiled
classes; the JAR is the accompanying artifact. `classes.json` records byte-identical
compiled/JAR hashes for selected changed runtime and fixture classes. No packaged-server settings or user
worlds are edited by this workflow.

## Scope

The compact gravity cases require minutes of simulation before readiness. In the
final matrix, outbound recovery waited 4,866–4,933 ticks after the initial 100-tick
full-load interval, with 15.53–15.68 blocks of additional lead-car travel. Returning
waited 2,531–2,572 ticks with 7.84–8.31 blocks of travel. These are measured fixture
results, not recovery-time guarantees. The tests retain the normal brake and
readiness thresholds and do not bypass them to complete the cycle.

This is native-passenger server evidence on the compact PAX example. It does not
establish cold restarts in every phase, player clients, camera or transport recovery,
tall/alternative routes, FE-on PAX equipment, active addon removal, damaged storage
or performance. The
broader FD305/FD901–FD904 acceptance matrix remains open.
