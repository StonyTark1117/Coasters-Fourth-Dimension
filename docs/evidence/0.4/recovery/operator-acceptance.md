# FD402 guarded recovery operator checkpoint

The 0.9.0-dev.15 checkpoint implements the production recovery workflow in
[RECOVERY.md](../../../RECOVERY.md). FD402 remains in progress toward the 0.4 gate;
this evidence does not close the broader 0.9 candidate gates.

## Production behavior

An operator selects a certified bare ordinary Coasters bearing, captures/retains
the complete matching 4D consist and closes restraints. Guarded editor actions
attach the native recovery coupler, start slow towing, brake and disconnect it.
The original car/track family is preserved. The ordinary bearing receives no 4D
vehicle record or seat-rotation mechanism, and while attached also requires the
consist's functional track family. Native spacing is 6/12/14 blocks by family.

The operator cannot dispatch a ride, open restraints or release retention in
recovery mode. Incomplete bodies, altered gear, a missing coupling, wrong track,
assigned winch ownership, stale requests and excessive speed reject or stop
operation. Towing is limited to 0.75 blocks/s; it never automatically resumes.
A lost ordinary bearing retains identity. Native UNLOADED holding permits later
restoration; actual REMOVED destruction records a durable tombstone. Unavailable
bearings must restore before disconnect, preventing dormant native links from
returning after their operator record has been cleared.

A stopped, retained PAX consist can explicitly release its loaded physical
winch/catch/carriage for recovery. This preserves the original cars, carriers and
four-car native links. A moving shuttle or missing assigned carriage rejects
handover.

Save schema and network protocol are 6. Schemas 1–5 load forward; back up worlds
and upgrade clients/servers together. Recovery-mode saves cannot be downgraded to
a build that does not read schema 6.

## Native validation

These tests use real Sable bodies, Coasters running-rail support, native couplers,
native passenger seat entities, Coasters chains and Create creative motors.
Only the ordinary bearing receives chain propulsion. They add no artificial
world actuator, commanded vehicle velocity or motion teleport during towing.
The tools/editor exercise the actual server handlers with a permitted FakePlayer;
this does not establish rendered GUI or network-client operation.

- `guardedRecoveryUsesNativeChainDriveAcrossFiveFamilies` builds all five actual
  recovery examples. All 32 native passengers remain mounted during fully loaded
  retained towing. Each car travels over five blocks, with intact couplers and
  captured carrier alignment. It exercises normal tool sneak-use selection,
  unretained/stale/moving-disconnect rejects, braking, serialization, schema-5
  migration and forty actual wrong-family initial-snap rejections on both 4D and
  ordinary bodies. Matching rail support survives a broken recovery link;
  permanent ordinary-bearing destruction is recorded and allows later stationary
  disconnect/unloading without replacement allocation.
- The same test moves each actual recovery group into Sable holding. Every
  original bearing, chassis and carrier is held under its original ID. Missing
  resume and dormant disconnect reject. All five groups, native couplings and 32
  passengers restore; they remain retained/braked and resume only through the
  explicit editor operation.
- `paxRecoveryHandsOverTheActualStationaryCarriage` starts a native powered
  four-car shuttle, rejects active handover, captures/retains it, and uses the
  explicit production handover. Its actual carriage and catch ownership are
  removed; its four original cars/eight carriers and original couplings remain.
  No replacement carriage is allocated. This separate handover case is empty.
- `occupiedOriginalTrainsPreserveLinksThroughRecovery` tows an actual two-car
  Arrow train with eight passengers and a four-car PAX train with sixteen.
  Both travel over three blocks on native chains, then brake and disconnect the
  bearing. All original train links and maintenance retention remain. Restraints
  open normally and all 24 passengers unload. This PAX towing fixture is an
  unassigned flat consist, separate from the handover fixture.

The first broken-link diagnostic exposed a production support bug: requiring
recovery partner availability in structural eligibility made a disabled car lose
its own valid rails after unlink. Structural eligibility now preserves matching
4D rail support; the recovery runtime separately faults/brakes missing equipment.
The final tests cover this corrected behavior rather than counting the diagnostic
failure as a physical coupling limitation.

Exact final profile results and condensed native output are stored alongside this
file as `operator-standalone-results.json`, `operator-combined-results.json`, and
the corresponding `operator-*-native.txt` files. Both profiles pass 33 required
native tests and 25 unit tests, with the same source manifest and reproducible JAR
digest. The combined profile uses the pinned Track Styles 1.2.0 and Engineered
1.1.1 releases; optional dependencies are not bundled. Historical eight-job CI
success for preceding commit 70f604a is recorded separately in
`previous-checkpoint-ci.json` and does not validate the new production code.

Reproduce with Java 21:

```sh
./gradlew --no-daemon build runGameTestServer
./gradlew --no-daemon build runGameTestServer -PfdIntegrations=styles,engineered
```

GameTests recreate their disposable world. Do not use `clean` where important
packaged GUI worlds remain inside the build directory.

## Remaining acceptance

Rendered ordinary player selection/editor/propulsion/boarding, command invocation,
complete curved/banked offsets and clearance, nonzero Arrow rack/profile scenes,
broader coupler strength and propulsion/braking scenes, Engineered recovery/FE
operation, and fresh-process recovery-mode lifecycle evidence remain open. These
checks do not certify added ordinary-cart assemblies or a survival locomotive.
The occupied PAX handover-to-towing sequence still needs end-to-end acceptance;
separate passing handover and flat towing cases do not prove that sequence.
