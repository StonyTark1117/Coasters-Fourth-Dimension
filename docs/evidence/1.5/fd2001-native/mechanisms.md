# Distinct moving mechanism native proof

2026-11-22, primary `main` over `f85f4a27`, uncommitted 1.5 integration.
Java 21.0.13.1; owner `/root/fd13_body_design`. Persistent build, private project
cache, raw logs and retained worlds are under
`/mnt/NVME4/fourthdimension-runs/fd15-moving-native-20261122`.

## Result

Seven distinct production-coordinator paths passed cumulatively. `mechanisms-2.log`
passed five cases and timed out two; `mechanisms-combined-3.log` passed only those
two after the diagnosed static-target wake fix. Accepted translation evidence is
reused from [the adapter/coordinator record](README.md), not replayed.

| Path | Program | Interruption | Max measured speed (blocks/s) | Max angular speed (rad/s) | Final position error (blocks) |
| --- | --- | --- | ---: | ---: | ---: |
| Turntable | 90° yaw | Explicit operator stop | 0.00131 | 0.21298 | <0.000001 |
| Drop | 4 down | Actual Creative Motor power removed | 1.88752 | 0 | 0.000153 |
| Tilt | 30° downward pitch | Explicit operator stop | 0.13786 (COM motion) | 0.14796 | 0.000837 |
| Elevator | 4 up | Actual Creative Motor power removed | 1.30884 | <0.000001 | 0.000290 |
| Teeter | One −15°/+15° cycle, finish level | Explicit operator stop | 0.14975 (COM motion) | 0.16593 | 0.000809 |
| Side-drop | 4 sideways and 4 down | Explicit operator stop | 2.20873 | 0.00445 | 0.001716 |
| Compound | Concurrent yaw to 90° and pitch within 15°, finish level | Explicit operator stop | 0.06840 (COM motion) | 0.21725 | 0.001741 |

Each creates a real moving controller, stationary Creative Motor, physical bed and
native socket approaches; retains a real Wing with original vehicle metadata and
closed normal restraints; captures/moves through `MovingRuntime.operate`; brakes
while disconnected; requires explicit Resume after actual stationary readback;
and reaches a measured stopped, locked, authenticated fixed graph bridge.
Rigid chassis-to-bed pose is checked within 0.05 blocks. Endpoint tolerances remain
0.025 blocks, 0.25°, linear speed 0.025 blocks/s and angular speed 0.1°/s.
The 30° fixture uses fractional socket geometry, not rounded rail endpoints.

These are fake-operator, helper-constructed, empty-seat native checks. They do not
claim normal player construction, occupied rendering/input, cold-process restart,
or every selectable distance/port/cycle. The same finite program/capability code
covers those settings; distinct player and family cases have separate owners.

## Native settling repair and physical measurements

Side-drop stopped within 1.8mm of its interruption target but retained a native
solver correction velocity 0.0412 blocks/s. Compound reached its endpoint within
1.8mm but retained speed 0.0394 and angular rate 0.00283 rad/s. Both were correctly
denied docking by existing stopped guards. Inspection found the adapter reset its
unchanged native motor frame and woke the bed on every physics tick, preventing
normal native sleeping. `commandTarget` now updates/wakes only on an actual target
change. Moving targets and the first emergency hold still wake immediately.
No body is teleported, velocity zeroed, or acceptance tolerance widened. The
changed-condition two-case run passed; its saved states retain zero real native
velocities and `RECONNECTED` locks.

Raw `FD2002_PASS` lines include maximum finite differences of velocity between
server ticks. These include emergency braking, not just the smooth requested
program: linear peaks were 13.10 (drop),27.42 (elevator),27.39 (side-drop) blocks/s²;
angular peaks were 3.316 (turntable),2.555 (tilt),0.306 (teeter),2.679 (compound) rad/s².
They are sampled observations, not exact continuous peak bounds or comfort
claims. Smooth program acceleration/jerk limits are command derivatives; do not
label them measured emergency-brake limits. Native actuators remain finite:
linear stiffness/damping/force caps 4000/160/24 times retained total mass,
angular 40000/1600/240 times that mass. Full stopping excursion and continuous
physical jerk were not recorded by these fixtures.

The final combined run includes the measured support correction: compiled
`MovingPartBlock` and built `moving_bed.json` both use saddle Y 20.25. The copied
model and class/source hashes are retained beside its log. This is physical
clearance evidence for these paths, not a rendered visual acceptance claim.

## Preservation and reproduction

`world-mechanisms-2` and `world-mechanisms-combined-3` preserve cleanly saved worlds.
Their `*-saved-state.json` summaries include original FoundationData SHA256,
physical poses, velocities, phases and faults. `mechanisms-combined-3-source.sha256`
and `mechanisms-combined-3-classes.sha256` identify the targeted fix and fixture.
Both runtime logs end with all dimensions saved and GameTest shutdown; the final
command exited 0. No owned display, service or Minecraft process remains active.
`mechanisms-1.log` is only the corrected wildcard-import compile failure.

The accepted five-case holder now uses `fourthdimension_fd2002_mechanisms`; the
separate affected pair uses `fourthdimension_fd2006_combined`. Both have their own
matching `structure/empty.nbt`. Use Java 21, `--no-daemon`, owner-specific
`--project-cache-dir`, `-PfdBuildDirectory`, the selected
`-PfdGameTestNamespaces`, and `-PfdValidation runGameTestServer` only for a named
relevant future change.
