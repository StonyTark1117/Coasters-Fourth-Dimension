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
- `recoveryDemoCommandBuildsTheOperatorFixture` invokes `/fd demo recovery_arrow`
  through the registered server dispatcher with an operator-permitted command
  source. It verifies that the exact command-origin rail and Arrow vehicle are
  created, operator-ready and unlinked, and that the documented native motor,
  clutch and lever start in the unpowered state. This is server command-path
  coverage, not rendered player chat or GUI evidence.

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

## Packaged client checkpoint

The same JAR now has an [empty standalone Arrow client check](gui/arrow-gui-acceptance.md):
normal selection/attachment and motor-value controls, same-ID braked coupling
restoration in a fresh JVM, explicit start, a route-end stop, brake action,
power stop, normal disconnect and restraint opening. Both clients exit normally
with all validation flags false. The explicit brake action follows the automatic
route stop; it is not a new rendered moving-brake measurement. Saved snapshots
preserve identities, bindings, carrier configuration and all rail/profile fields.
All eight exact-head CI jobs on 5130179 also passed all 33 required native tests;
see `operator-checkpoint-ci.json`. All-family rendered acceptance remains open.

The later [occupied Arrow render/motion checkpoint](occupied-render-motion/acceptance.md)
adds a fresh development-client run with a real mounted local rider, a replicated
native ordinary-bearing link and native chain motion. It records exact client
render-path observations for the fourth-dimension bodies and the separate
Coasters ordinary-cart renderer. Its bounded Arrow-only scope does not replace
the remaining rendered operator or lifecycle acceptance below.

The later [occupied banked Arrow render/motion checkpoint](occupied-banked-render-motion/acceptance.md)
adds a fresh client run over the native quarter-bend and sustained 30-degree
bank. It independently measures the moved chassis and ordinary bearing world-up
vectors rather than treating Arrow controlled spin as bank traversal. It remains
one bounded Arrow scene and does not replace the remaining acceptance below.

## Remaining acceptance

Rendered ordinary player selection/editor/propulsion/boarding,
complete curved/banked offsets and clearance, nonzero Arrow rack/profile scenes,
broader coupler strength and propulsion/braking scenes, and fresh-process
recovery-mode lifecycle evidence remain open. These
checks do not certify added ordinary-cart assemblies or a survival locomotive.
The bounded installed-motor brake/start/brake callback check is recorded in the
[Engineered motor-interlock checkpoint](engineered-motor-interlock/acceptance.md);
at that earlier checkpoint it did not close the broader Engineered/FE item.
The later [isolated Engineered recovery/FE checkpoint](engineered-fe/acceptance.md)
closes that bounded item with the provider's native controller and FE capability:
braked/starved holds, explicit occupied towing and consumption, the recovery
speed cap, rebraking without depletion, rider retention and cleanup all pass.
Rendered manual Engineered operation and all-family provider coverage remain
outside that result.
The later [dev.17 occupied handover checkpoint](occupied-handover/acceptance.md)
adds one native end-to-end handover/towing/braking/disconnection/unloading test
with sixteen passengers in their original seats. Rendered occupied handover and
complete curved/banked and lifecycle coverage remain open.

The [fresh-process Arrow recovery checkpoint](fresh-process-recovery/acceptance.md)
now preserves one occupied braked native recovery link across a client/server
restart and completes its normal release in the second process. It is a bounded
one-player Arrow scene; full multiplayer and all-family lifecycle acceptance
remain open.

The [rendered recovery-editor checkpoint](rendered-recovery-editor/acceptance.md)
now opens and renders the actual linked-vehicle editor after production
attachment. It does not replace manual ordinary player selection, button input,
propulsion, or boarding acceptance.
