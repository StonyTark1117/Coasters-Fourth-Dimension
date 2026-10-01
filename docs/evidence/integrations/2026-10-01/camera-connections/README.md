# Native orbit-camera carrier connections

This fixes the Sable orbit-camera obstruction exposed by the preceding
[Revised camera checkpoint](../immersion-revised/README.md). The first-person
Immersion adapter and Sable's camera transforms remain unchanged. This is bounded
Revised evidence, not completion of FD904 or the full integration matrix.

## Cause and change

Sable 2.0.3's `SubLevelHelper.getConnectedChain` walks block-entity actor connection
dependencies. Fourth Dimension creates its rotary constraints directly through
Sable's physics API. Those joints were absent from that traversal. Sable's two
native orbit modes exclude the returned group from collision rays, so the camera
could hit its own chassis or a sibling carrier. The pre-fix client reports only
one member, despite the real car having a chassis and four carriers.

The client-only `CarrierConnectionMixin` augments the native result for a registered
original 4D body. It retains native members, adds the loaded chassis and carriers,
and follows Coasters' native train-link IDs before adding registered peer carriers.
Removed/unavailable bodies are omitted; absent snapshots, prototype vehicles and
unregistered starting bodies retain native behavior. No world blocks, unrelated
bodies, physics constraints, camera positions, rotations or collision rays are
rewritten. Sable still chooses and clamps the camera distance.

The extension applies without Immersion. The dedicated server does not load this
client mixin. The pinned source and bytecode were inspected before implementation;
`getConnectedChain` in Sable 2.0.3 is used by its orbit-camera clamp. This change
does not alter Sable's separate loading-dependency traversal.

## Exact environment and reproduction

Minecraft 1.21.1, Java 21, NeoForge 21.1.248, Create 6.0.10-280, Ponder
1.0.82+mc1.21.1, Flywheel 1.0.6, Sable 2.0.3, Coasters 0.1.5 and Fourth Dimension
0.9.0-dev.1. Immersion is 0.2.3 with Aeronautics bundled 1.3.2. Combined also uses
Engineered 1.1.1, Track Styles 1.2.0, Additions 0.2.0, Magnetized 1.1.0 and
Interiors 0.6.1v3. Logs retain complete loaded-mod lists and `artifacts.json`
identifies the two immutable packaged JARs and their class hashes.

All cases independently copy the same stopped, unoccupied Revised world from the
preceding checkpoint. Its one car, four carriers, eight rails and profiles remain
stable. This is an unoccupied world reuse, not occupied addon-removal certification.
The dedicated server is loopback only. Clients use owned Xvfb displays, software
OpenGL, an 854×480 viewport in 1280×720, GUI scale 2, a 30 FPS cap and five-chunk
render distance. No shaders or performance claims.

Build with Java 21 using `./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2
build`. Use immutable JAR copies with `runServer`/`runClient` (and `runObserver`
for the combined moving case), `-PfdPackaged -PfdPackagedJar=/absolute/copy.jar
-PfdValidation`, and the selected `-PfdIntegrations` profile. The client connects
with `-PfdConnect=127.0.0.1:25585`.

`drivers/observe.py` boards Dev through the existing validated native fixture,
closes restraints, checks each actual F5 camera type, and uses twenty native
scroll-down steps in SUB_LEVEL_VIEW. `FD_CAMERA_GROUP` records native group IDs,
the registered vehicle bodies, public zoom accumulator and actual camera/carrier
world positions. The diagnostic is read-only and opt-in.

For each fixed case, the driver creates an unrelated 9×9 stone wall approximately
12 blocks along the zoomed camera ray, observes the clamp, removes the wall, and
observes restored distance. It finishes by opening restraints and holding Shift
for a normal exit. Original screenshots and byte-offset phase boundaries are
retained. The camera is never assigned a test pose. The first wall/removal frame
can precede the client block update: the raw intervals retain that old distance.
The verifier checks all samples for finite values and group identity, and evaluates
wall distance after four 250 ms diagnostic intervals, requiring at least twelve
settled samples. The initial zero-delay distance assertion failed on that one
pre-update frame; no runtime change or discarded log was used to resolve it.
This is an observation window, not a general networking-latency guarantee.

The combined case additionally runs `drivers/accept.py`: two real players complete
two native chain-powered circuits, use all five camera modes, change from carriers
0/2 to 3/1, exercise first-person settings, return through the real boarding
station, and unload normally. These four assignments cover one seat in each pair;
they do not cover every seat-facing position.

## Retained setup failure

The first combined moving attempt did not have two valid riders: Observer was
killed by a slime at the flat-world spawn while waiting through Dev's stationary
checks. The fixture briefly reported a boarding action on that dead player, but
native occupancy and client diagnostics correctly showed Observer absent. The
mode-selection observation stopped; this attempt is not a two-rider pass.
`combined/spawn-slime-attempt` retains the original driver, events, reports and
captures, and the uninterrupted full server/client logs contain the death.

Dev's occupied car completed its normal station return. The operator set the QA
world to peaceful, respawned Observer with the normal UI, opened restraints and
unloaded Dev. The retry did not restart the server or alter ride/camera state.
The driver now requires full health and confirmed server/client occupancy for
both players before closing restraints or dispatching. Its reports use unique
`accepted-*` labels; the verifier selects those labels and the retry's byte-offset
intervals, so the failed attempt cannot satisfy moving acceptance.

## Interpretation and remaining scope

The audit covers **686 parked camera samples** across the four cases and **969
moving samples** in the successful combined retry (484 Dev, 485 Observer). Both
riders retain the complete five-body group during motion, switch to their second
carrier, and make two normal exits with ordinary first-person pitch/up alignment,
zero body travel, full health and ground contact. Their logs match 493 and 372
server Foundation snapshots. All four saved rail lists remain exactly equal.

The two Java 21 builds pass all 19 unit tests. Packaged dedicated startup and real
client checks run in all three fixed profiles; no headless test is presented as
camera proof. Every common Fourth Dimension class is byte-identical between the
before and fixed JARs; the new connection mixin and its client registration are the
isolated behavioral change. All fourteen recorded server/client/display handles
are gone, both loopback ports are closed and five saved settings files are restored.

The expected pre-fix failure is retained: both orbit modes and maximum scroll zoom
remain near 0.92 blocks with only the ridden body in the group. With the candidate,
both orbit modes reach the native 7-block default, scroll zoom reaches 23 blocks,
the wall shortens that distance to about 11 blocks, and removal restores it. The
fixed cases are Immersion-only, standalone, and combined.

Vanilla THIRD_PERSON_BACK and THIRD_PERSON_FRONT use their ordinary collision
policy and may still be obstructed by the seat/car at some angles. This patch
repairs Sable's connected-body orbit modes; it does not disable ordinary camera
collision or promise an unobstructed view at every angle/default distance. The
native orbit zoom is available for a wider view of the vehicle.

Native train-link traversal is implemented, but linked-train client acceptance,
other-family orbit matrices, unrelated moving-body collision, full inversion,
dimension changes, shader/Flywheel alternatives and performance targets remain
separate. Existing optional-distribution/resource warnings and unavailable narrator
library messages remain in full logs. All cases must exit normally and settings
must be restored before the audit passes.

```sh
python3 scripts/verify-camera-connections.py docs/evidence/integrations/2026-10-01/camera-connections
```
