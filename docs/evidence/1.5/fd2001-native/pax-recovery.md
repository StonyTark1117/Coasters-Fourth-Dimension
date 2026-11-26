# Original four-car PAX moving-turn recovery

Accepted targeted server result, 2026-11-26, primary `main` at base
`f85f4a277c7c7542a4f07a158f18f580beb41187` plus the preserved working source snapshot.
This closes the actual four-car PAX full-turn and original-carriage recovery case.
It does not add an occupied-client or cold-process claim, nor reopen prior PAX qualification.

## Executed behavior

`MovingPaxRecoveryGameTests.originalWinchRecoversAfterFullShowTurn` builds a 32 m
PAX turntable and an original fixed winch station between two elevated spikes.
Four actual native PAX cars, with their eight carrier bodies, are linked through
normal assembly. The original powered carriage lifts them; gravity and native
rail guides carry the complete train onto the docked bed. Normal emergency
capture stops it there before moving admission inspects every physical body.

The production moving operations capture all four chassis and eight carriers,
run the full 360-degree turn, interrupt it with emergency hold, explicitly resume,
and reconnect with physical socket locks. Normal Release is followed by explicit
Dispatch to the original PAX winch. The test observes its real native catch,
return to the unchanged original station coordinate, and actual native station
holds for all four cars with restraints closed, empty car faults, and no cars
remaining in the moving service ownership set. The carriage UUID, route endpoints
and station coordinate are asserted unchanged throughout recovery.

The final moving assembly is still `RELEASING`: conservative receiving-span claims
are not asserted clear while the train is parked at its original fixed station.
The completed recovery does not silently release those reservations.

## Physical measurements and raw evidence

Persistent run root:
`/mnt/NVME4/fourthdimension-runs/fd15-moving-native-20261122`.

- `pax-recovery-18.log`: `FD2001_PAX_SHOW_STARTED` (12 bodies),
  `FD2001_MEASURE label=pax-show360`, `FD2001_PAX_RELEASED`,
  `FD2001_PAX_RECOVERY_REQUEST`, `FD2001_PAX_RECOVERY_PASS`, all 1 required test
  passed, `BUILD SUCCESSFUL in 39s`, all dimensions saved, process exit 0.
- `pax-show360-run18-physics.csv`: 2,512 native callbacks at 0.025 s, 62.8 s
  observed moving/hold duration. Unwrapped quaternion yaw sums to
  359.999999954 degrees; absolute accumulated yaw is 360.556650720 degrees,
  including small stop/settle corrections.
- Maximum observed ordinary-motion angular speed 0.242001287 rad/s,
  angular acceleration 0.053207749 rad/s², angular jerk 3.154135660 rad/s³.
  Emergency braking angular acceleration/jerk maxima are separately recorded
  as 3.127543616 rad/s² and 125.678458019 rad/s³; they are not relabeled as
  ordinary program motion.
- First stopped measurement after emergency hold: 0.275 s. Maximum angular
  stopping excursion 0.004864992 rad and translation 0.000038947 m.
- Original station coordinate: 62.67991454081201; original carriage:
  `6b05c2cc-559d-4328-bf73-4c57f2abbbf6`.
- `world-pax-recovery-18`: clean final world. `pax-recovery18-sources/` preserves
  selected source inputs and `SHA256SUMS`; `pax-recovery18-classes.sha256`
  identifies affected compiled classes; `pax-recovery18-evidence.sha256`
  identifies accepted raw log and CSV.

## Diagnosed changes

The dock now uses four native locks, one at each side of both end sockets.
Unchanged position, orientation, linear-speed and angular-speed limits still
control bridge publication. Native rail-guide hooks install a free constraint
that disables contacts only between an authenticated rolling chassis and the
specific locked bed on its bridge or immediate connected approach. It adds no
pose drive. The exclusion clears on guide removal, departure, disconnect or
missing bodies. Retained and unrelated collider behavior is preserved.

Raw `pax-contact-diag-10.log` established a separate linked-car failure: the front
PAX car entered the authenticated dynamic bridge, but `ConsistRules` looked for
its support only among fixed saved rails. The other three cars consequently lost
native snap. That lookup now uses the existing authenticated moving route context.

`pax-settling-12.log` exposed repeated gravity impulses in the old soft PAX world
hold. `ShuttleRuntime` now retains each attained pose with an all-axis native lock
using the matching center-of-mass local anchor. It no longer resets velocities
on every callback. Captured carriers first reach their existing angle/rate
readiness, then receive physical posture holds too. Explicit carriage recovery
keeps those holds until its actual catch. No teleport, tolerance widening or
reassignment of station/route/carriage was added.

The routes owner's PAX-specific fixed-rail handoff preserves captured carrier
posture, so the original winch can finish `CAPTURING` into `BOARDING`. The fixture
waits for the actual station holds installed after that transition before
accepting recovery, rather than treating the phase flag alone as evidence.

## Reproduction and cleanup

Use Java 21 (`/usr/lib/jvm/java-21-openjdk`) and the isolated persistent build:

```sh
./gradlew --no-daemon --max-workers=2 \
  --project-cache-dir /mnt/NVME4/fourthdimension-runs/fd15-moving-native-20261122/project-cache \
  -PfdBuildDirectory=/mnt/NVME4/fourthdimension-runs/fd15-moving-native-20261122/build \
  -PfdValidation -PfdGameTestNamespaces=fourthdimension_fd2001_pax_recovery \
  runGameTestServer
```

The run is headless and uses a fake operator for fixture construction and normal
operation calls. It does not claim mouse input or an occupied rendered ride.
Before concurrent launches the host had 46 GiB available, the parent cgroup was
unbounded with approximately 24 GB current use, and persistent NVME storage had
147 GB free. The accepted server and Gradle launcher exited; no process using this
owned run root or Gradle daemon remained at cleanup. No GUI display was started.
Failed diagnostic worlds and logs were preserved independently.
