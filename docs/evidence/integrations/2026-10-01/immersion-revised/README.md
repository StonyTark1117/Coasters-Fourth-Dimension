# Revised ZacSpin camera observations

This checkpoint exercises a real player in the native chain-powered Revised
ZacSpin example with Immersion alone, then two real players with all five addons.
It narrows camera and normal-unloading coverage; FD904 and the integration release
matrix remain open. Numerical carrier tracking is separate from visual acceptance.

## Reproduction

Use Java 21, Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Flywheel 1.0.6, Sable 2.0.3, Coasters 0.1.5 and
Fourth Dimension 0.9.0-dev.1. Immersion is exactly 0.2.3 with Aeronautics 1.3.2.
The combined profile additionally installs Engineered 1.1.1, Track Styles 1.2.0,
Additions 0.2.0, Magnetized 1.1.0 and Interiors 0.6.1v3. Full loaded-mod lists,
artifact hash, command lines and source/class hashes are retained.

The packaged dedicated server binds only loopback, in a fresh flat world.
Clients use independent owned Xvfb displays, software OpenGL, an 854×480 game
viewport within 1280×720, GUI scale 2, five-chunk render distance and a 30 FPS cap.
There are no shaders and this is not a performance measurement. Raw mouse input
is disabled in the disposable clients so XTest produces ordinary mouse events.

Build with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build`. Copy the JAR to an
immutable path. Launch `runServer`, `runClient`, and (combined only) `runObserver`
with `-PfdPackaged -PfdPackagedJar=/absolute/copy.jar -PfdValidation` and the
selected `-PfdIntegrations=immersion` or
`-PfdIntegrations=engineered,styles,additions,magnetized,immersion` profile.
Clients connect with `-PfdConnect=127.0.0.1:25585`.

1. In a fresh world, `/fd validation camera_ride create` builds the existing
   Revised example at (450,140,480), and force-loads its circuit. Let it settle.
2. `camera_ride board 0 Dev` uses validated native seat boarding. In combined,
   board Observer in carrier 2. `close` and `dispatch` call normal shared ride
   operations. All four pairs remain independent; the fixture assigns no carrier
   pose or camera transform. The unmounted operator is teleported to the station
   before boarding, with the documented initial mouse heading.
3. After departure, power the real station using a redstone block at
   (444,140,501). It captures the returning car through normal station behavior.
4. Select all five camera types using F5, checking the actual diagnostic name:
   FIRST_PERSON, SUB_LEVEL_VIEW, THIRD_PERSON_BACK, SUB_LEVEL_VIEW_UNLOCKED and
   THIRD_PERSON_FRONT. Sable inserts two additional types; counting vanilla F5
   presses is insufficient. Move the mouse during the run.
5. Wait for all-carrier readiness, open restraints and hold Shift to leave.
   Board Dev in carrier 3 and Observer in carrier 1 for a second full circuit.
   In first person, exercise zero inertia, the master switch disabled, then
   150 ms inertia with roll/pitch stabilization both 100, and restored defaults.
   These are three maximum comfort parameters, not every Immersion setting.
6. Finish with normal station capture, open restraints and Shift unloading.
   Preserve logs, screenshots, the stopped native save and exact settings.

`drivers/accept.py` performs the mode selection by observed name, checks config
reload, and records byte offsets delimiting each phase in the untouched client
logs. The read-only diagnostics compare normalized raw Immersion orientation to
the authoritative carrier render pose. Neither this comparison nor a finite
camera quaternion proves that an external camera view is unobstructed.

## Results and limitations

The three client runs provide **1,435 samples** inside the explicitly delimited
nine-phase intervals: 478 Immersion-only, 477 combined Dev and 480 combined
Observer. Maximum raw-pose error is **4.2147×10⁻⁸ radians**. Matched Foundation
snapshots total 602, 210 and 210 respectively. Each accepted run contains two
normal first-person dismounts, with zero remaining body travel, ordinary pitch/up
alignment, full health, ground contact and no remaining native mount. The combined
riders cover all four independent carriers across their two assignments; this is
one seat per pair, not all eight seat-facing positions.

The retained `verification.json` audits nine measured phases per client, actual
carrier assignment changes, mouse input, native circuit motion, station readiness,
disabled effects, real mounted-to-unmounted transitions, ordinary first-person
camera restoration and matching server/client Foundation snapshots. The stopped
native save has no occupant or seat reservations left. Build validation includes
19 passing unit tests and all 36 required standalone native tests, with seven
explicit absent-addon fixture skips. Those native tests verify startup/regressions;
no headless camera claim is inferred from them.

The initial Immersion-only `ride.py` run is retained as exploratory evidence.
Its phase labels assumed vanilla's three-mode F5 cycle and therefore misname
several views. It is excluded from the nine-phase acceptance intervals. The later
`accept.py` run in the same client selects modes by their real names.

Visual inspection found substantial rider/seat occlusion in the Immersion-only
Sable external views and close-up obstruction in front third person at those mouse
angles. Selected combined-pack views have useful outside views, so the obstruction
is pose/view dependent; this checkpoint does not establish its root cause.
These captures are retained as a rendering limitation, not a five-mode visual
pass. The stationary effects-on/off and native scroll-zoom controls further
investigate this independently of the moving numerical measurements. Both frozen
and normally ticking parked controls are retained, because camera interpolation
while ticks are frozen cannot establish live scroll-zoom behavior. Twenty native
zoom-out wheel steps also leave the normally ticking parked view obstructed with
effects both on and off. The input is recorded; the diagnostic does not measure
Sable's private zoom accumulator.

The first stationary control driver incorrectly expected the diagnostic inertia
setting to become zero when the master switch was off. It actually reports the
configured 40 ms while processing is disabled. That observation timeout and its
original script/events are retained. The corrected driver checks the configured
value separately from the processing flag; no ride state or camera pose was
modified to satisfy it.

The passive Revised example produces real carrier swings, not a commanded full
turn. This checkpoint does not certify Revised full inversion. Dimension changes,
occupied moving restarts, spectator mode, every seat-facing/pose option, shaders,
Flywheel alternatives and client performance targets remain outside its scope.
All-addon loading here does not certify active external motor/equipment operation.
Native dependency resource/optional-shader warnings and the unavailable headless
narrator library remain in the full logs; they are not silently omitted. Both
dedicated profiles retain twelve nonfatal ClientLevel distribution probes, also
present in the preceding combined Additions checkpoint, plus the fresh-flat-world
empty-layer warning. Both servers finish startup and shut down with exit zero;
this is not a warning-free-startup claim.

Run from the repository root:

```sh
python3 scripts/verify-immersion-revised.py docs/evidence/integrations/2026-10-01/immersion-revised
```

## Follow-up

The [camera connection checkpoint](../camera-connections/README.md) identifies
and fixes the missing native orbit-camera connection group. This original evidence
remains the pre-fix observation, including its obstructed captures. Vanilla third-
person collision behavior and the broader release gates are still separate.
