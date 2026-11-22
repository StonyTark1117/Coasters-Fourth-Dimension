# FD2204 Flying Bike evidence — accepted

Owner `/root/fd13_spinning_check`, primary `main`. Persistent run root:
`/mnt/NVME4/fourthdimension-runs/fd14-flying-bike-20261122`.
Only this original two-seat, two-launch native profile is exercised. No other
family acceptance is reopened. The native and occupied acceptance records below are cumulative.

## Native mechanics and diagnosed entry defect

`native-first.log` passed the one targeted native GameTest with a real native
cart, committed Chain spans, real Create shaft transmission and native pig mass.
It demonstrated both speed maxima (8 and 12 blocks/s), power-loss retained hold,
restored-power no-start, explicit resume, serialized interrupted identity and a
complete physical circuit ending at a dock with open restraints. It did not
measure entry acceleration, prove a real player/camera, or perform cold restart.

A per-tick physical-speed assertion exposed a second-launch entry discontinuity.
`native-entry.log` and `native-entry-bounded.log` retain those failures. Pre-spin
matches the empty second span to measured approach speed; the dedicated native
matcher cap limits each physical propulsion callback to 3 blocks/s².

`native-entry-trace.log` and `native-entry-collision-trace.log` are DIAGNOSTIC
runs: their early acceleration assertion was deliberately deferred to preserve
the failed entry trace. Their GameTest PASS lines are not acceleration acceptance
(the printed maxima are 54.21 and 93.48 blocks/s²). At entry the matcher changed
speed from 7.793 to 7.811, then the next callback began at 5.009: the large impulse
occurred in the physical step, not inside the native matcher.

Transformed collision-box readback isolated the full-cube Flying Bike controller
at native local (0,-1,0). Its swept world Y109.124–110.126 intersected the native
anchor's Y110–110.5. The native chassis contact is intentional; this additional
controller overlap was not. The controller collision proxy is now the authored
lower saddle, with the terminal bearing neck treated as the intentional native
running-gear attachment. Upper support crossheads were lowered and retain their
continuous controller-to-cradle load path. The connected export audit covers all
39 fixed elements and all 56 integer-degree restraint positions from0 to55.

The final affected native run below retained the acceleration assertion on EVERY
running tick, including both launch entries; it also denied a missing side exit
platform before restoring that platform. Source-local maximum checks do not
substitute for that physical readback.

## Reproduction and ownership

Use Java21, `--offline --no-daemon --max-workers=2`, namespace
`fourthdimension_fd2204`, own `-PfdBuildDirectory=.../build`, and
`--project-cache-dir .../project-cache`. Separate build directories alone do not
isolate Gradle's execution history; the explicit project cache is required for
parallel owners. Native worlds and logs remain under the persistent run root.

The reserved GUI resource record is `run.json`: private Xvfb353, loopback
ports25773/25774, client/server paths under this root, 1536M client heap and6GiB
Minecraft-only client unit. The owned GUI run is complete and all three units are inactive; see `cleanup.txt`.

## Accepted straight-entry correction

The controller correction alone still failed in `native-entry-final.log` (98.11 blocks/s²). Native `CoasterCartBlock` implements Sable-specific `BlockSubLevelCollisionShape`: its actual upper-half collider is `[0,.5,0]–[1,1,1]`, so the earlier ordinary-block full-cube audit overstated some native contacts. Actual residual curve yaw brought that running gear into the transverse drive shaft at the curve endpoint. Moving each drive start eight blocks down an upright straight resolves the contact. The ten-edge example has two56-block Chains and two8-block unpowered alignment approaches. Route validation enforces that approach for each launch.

`native-straight-entry.log`: **2/2 passed**, Java21, clean shutdown. Actual first speed maximum8.000000004, second12.000000956 blocks/s, maximum per-tick physical acceleration3.000030517 blocks/s². The assertion ran on every running tick including entry (only dispatch initialization/recovery transition excluded). It also retained real native passenger mass, actual power-loss stop, restored-power no-start, explicit powered recovery, full physical circuit and final dock opening. The saved world is `accepted-native-world/`, outside disposable build output.

The second test physically placed the native car on180° track and verified dry world-horizontal bounded exits, five stopped ticks, actual angular speed≤.01rad/s, original dock/phase retention, new boarding and dispatch/resume denial, conservative saved intent, and missing-platform denial. It contains no player unload claim. Fresh unowned specialty authority also rejects Engineered/Magnetized/CTNB/external providers while allowing native Chain. No optional mod runtime is implied.

After this run, original-dock restoration was tightened: evacuation cannot be cleared by relabelling another nearby level rail as the original dock; saved endpoints and geometry must match. This is compiled in the client package and does not change propulsion.

## Model serialization correction

The first shared client exposed vanilla's element bound rejection for Flying Bike seat z33. `generate-flying-bike.py` now exports that model at z−1 and uses NeoForge root `transform.translation=[0,0,.0625]`, `origin=corner`, restoring exact authored geometry. The audit expands the translation, verifies all element coordinates are within[-16,32], and retains the same39-element connected load path, restraint sweep and bounds. The owned client below loaded the corrected model without model-parser errors and verified the occupied pose.


## Occupied construction, circuit and normal unload

The dedicated server and private Xvfb `:353` ran on Java 21, loopback
`25773/25774`, with a 1536M client heap and 6 GiB Minecraft-only client unit.
The client window was 854×480. Default mouse bindings and `rawMouseInput:true`
were verified. Normal mouse turning and reachable crafting-table use both
worked in this same client. `input.jsonl` records input and screenshot paths;
`console.jsonl` explicitly records setup and readback commands.

Setup supplied upstream ingredients, recipe unlocks and a crafting table, and
`fd demo flying_bike_track` built the disclosed ten-edge track/Chain example.
This is not a claim of hand-built track or survival power generation. In
Survival, normal recipe-book/crafting-table input produced sixteen Flying Bike
Track tools, one car and two governors, consuming the stated ingredients.
Normal item use placed the car, bound the original station, placed both crafted
governors, bound the first to the car and linked the second. Create's preferred
placement orientation initially faced the second governor away from its Chain;
F3 diagnosed that state, and two normal Wrench rotations set its output east.
The failed link did not claim acceptance. The subsequent reciprocal link passed.

Actual car UUID `3c15fe59-0758-4bdb-abb8-f9b664b9134c`, rider
`380df991-f603-344c-a090-369bad2a924a` (Dev), native seat
`(20481033,125,20481032)` remained consistent. Disclosed source-local positioning
and aim setup put the player within reach of the seat; empty-hand right-click
mounted normally. Normal Rotation Tool Close and Dispatch launched the car.
`occupied-circuit-readback.jsonl` records the same occupied car through launch 1,
coast, launch 2 at 12 blocks/s, return braking and dock readiness. Opening
restraints and normal Shift dismount cleared both RootVehicle and occupants and
placed Dev on the normal dry side platform at `(-2.5,108,39.5)`.

`occupied-prone.png` and `banked-prone-side.png` show the fixed horizontal torso,
forward head/grips, restraint and continuous parent-owned support. Normal mouse
look changed the view while the torso remained in its cradle. The first-person
view remained attached to the banked car (`banked-first-person.png`); native
contraption-camera modes encountered during F5 cycling are not labelled as
first-person acceptance. The client reported no Flying Bike model parse errors.

## Real cold-process restart and zero-output repair

After normal reboarding/Close/Dispatch, the server was stopped while launch 1
was moving at 1.65 blocks/s with the rider restrained. The server saved all
dimensions, and the client quit normally. Both Java units were verified inactive
before fresh processes loaded the same world. The rider automatically restored
to the same native car and seat, restrained and held, with no automatic motion.
`interrupted-occupied-before-restart/` retains the saved interrupted world.

This exposed a concrete kinetic persistence mismatch: the governor correctly
reset its transient output to zero but Create had saved its driven shaft at
2.10 blocks/s. Its unchanged-value short circuit prevented propagating zero.
The narrow fix reconciles actual Chain speed while no fresh lease is permitted,
and allows zero output through that short circuit. Source-owned shaft checks,
nonzero ramp and native acceleration are unchanged. Governor messages also now
say the actual **Stop** button instead of Capture.

`prepare-cold-fix.log` passed compilation/packaging. The fresh patched process
loaded the preserved world with governor output **0** and actual Chain **0**,
same rider/car and retained hold. Normal Stop acknowledged the restored state;
normal Resume issued fresh authority and produced physical motion again
(1.50 blocks/s car, 1.65 blocks/s Chain at first readback). This closes the named
cold-output defect without reopening the accepted full native/circuit cases.
`cold-fix-artifact.sha256` records the actual patched artifact; initial occupied
artifact provenance remains separately in `client-artifact.sha256`.

## Banked evacuation through normal controls

The recovered occupied car reached its banked unpowered coast. Normal Stop
retained it at world center approximately `(78.59,110.625,111.9)` with near-zero
linear velocity. Disclosed transformed diagnostic markers measured prone heads
at `(79.3238,107.6340,111.3304)` and `(78.8745,108.5306,109.6000)`.
Command-built dry stone exits were finite setup: right floor x77–82/y107/z115–116,
left floor x77–82/y108/z105–106. The left platform entered the moving sweep,
which correctly retained a motion fault; evacuation does not authorize motion
through that geometry. It checks stationary bodies and world-horizontal exits.

The occupied player normally placed a second Boarding Station at `(80,107,114)`
on the reachable face of the right platform, then empty-hand used it. It bound
as **evacuation only**, with both exits valid and the original launch dock
preserved. Normal Dispatch was denied without release or movement
(`evacuation-dispatch-denied.png`). Normal Open followed by Shift dismount
placed Dev at `(80.5,108,114.5)` on the sturdy station top adjoining the exit
platform. RootVehicle was absent and the car's occupants list was empty. No
fallback teleport or command dismount was used. The native 180° fixture covers
all-roll exit predicates and boarding/Resume denial; this live banked case adds
actual player placement, binding, controls, rendering and ordinary unload.

`final-foundation.json` is a read-only decode of the cleanly saved final world:
original dock `(-3,108,38)`, evacuation station `(80,107,114)`, native owner held,
empty occupants and `resume_required=true`. The coast remains evacuation-only;
it does not promise self-propelled recovery from an unpowered stop.

## Cleanup and carry-forward

Final server log records all dimensions saved at 12:52:39 local. The client
quit through its menu. Client PID1099020, server PID1098985 and owned Xvfb
were then verified inactive (`cleanup.txt`, all MainPID0). Worlds, failure logs,
initial native evidence and screenshots remain under the persistent run root.
No other task's processes or files were stopped or removed.

`final-owned-source.sha256` records the owned source boundary. The final affected
artifact SHA256 is `1cbcef5f501afee150c32d07e19b0947a80786c4146dabe7fe4727600f8a292d`.
The text-only guide orientation teaching and final evidence additions need no
runtime replay. Primary `main` was `235255cf21845cda0e4921b5d036425c55a9a66b`;
all new FD2204 files remain unstaged for the root's selective 1.4 integration.
Other owners' uncommitted shared and specialty files were preserved.
