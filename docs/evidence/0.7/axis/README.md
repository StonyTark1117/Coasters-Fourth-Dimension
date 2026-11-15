# Axis — FD1302A–C

Owner: 0.7 lead, chat `01a1dd1d-1468-7f81-8f31-5532ad7a178d`.
Primary checkout `/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`,
starting HEAD `b8db0f06b566ed5b465c758c005db11d5c7b9837`.

Status: implementation and occupied acceptance in progress. Native roll/load,
power/reversal/capture and physical hinge checks pass; no Axis card is closed.
Closed 0.6 cases remain accepted.

Current graphical runtime:
`/mnt/NVME4/fourthdimension-runs/fd1302-axis-20261115`, private display `:316`,
game/RCON ports `25650/25651`, units `fd1302-axis-20261115-{server,client,xvfb}.service`.
The first client connected, then was stopped to correct a versionless options
file that Minecraft had interpreted as legacy numeric bindings. Retained settings
are version 3955, GUI scale 1, render distance 6, simulation distance 4,
raw mouse true and default left/right bindings. No input acceptance preceded this
setup correction. The server/world were then saved cleanly for the COM fix below.

## Current validation

Java 21 compilation passed after extending exhaustive family fixture switches.
[Axis contract checks](native/axis-contract.xml) passed 2/2, and the existing
[signed-axis checks](native/signed-axis-regression.xml) passed 5/5 with the new
negative longitudinal axis. These establish coordinate/capability contracts,
not occupied native mechanics.

The first native probe (`native-01.log` under the persistent runtime root) failed
its setup at tick 10: immediate `rebindDock` ran before the newly allocated
chassis had a settled world pose. The corrected probe defers binding to tick 50,
matching the established native allocation sequence. No acceptance was claimed.
Its original process is finishing a clean save; synchronous chunk writes caused
slow RegionFile IO, corroborated by `native-thread-01.txt`. The second isolated
`build-native2` starts with `sync-chunk-writes=false` and retains the first world.
This was before the graphical runtime started.

The second probe reached the settled native body and rejected the station block
at `(-600,101,-588)` inside the fork reservation. This is a concrete fixture
obstruction, not an input or mouse problem. The corrected station is three blocks
on the clear side, `(-603,101,-588)`. Native pose is unchanged by this correction.

Future build/run root is `/mnt/NVME4/fourthdimension-runs/fd1302-axis-20261115`
on a separate persistent XFS SSD (57 GiB free at selection). Root-disk IO measured
100% utilization, ~439 ms write and ~517 ms flush awaits; old worlds continue
clean saves, not killed or moved live. Third native probe uses the corrected
station and pre-launch `sync-chunk-writes=false`. Display/ports/unit names stay
reserved as above. Source now also persists winding offset/active route phase,
smooths mode damping, and bounds endpoint/reversal command deceleration. These
changes require their targeted checks; no prior mechanics pass is being reset.

## Native mechanics checkpoint — 2026-11-15 08:30 UTC

[The corrected native probe passed](native/native-roll-load-power-recovery.log).
One physical pod weighs 4.0 native units including its fixed keel; one asymmetric
rider adds exactly .8. Measured longitudinal inertia rises from 7.99472656 to
8.41872656. The native carrier reaches unwrapped 7.17459 rad (more than one turn)
at a maximum observed .63719 rad/s. Loss of Chain power stops travel, retains
restraints and damps without capture; mode/angle/omega survive serialization.
Explicit recovery reverses through the same profile, then returns to dock
capture and permits restraint opening. Final reverse angle is −.0118133 rad.

[Bounded command endpoint/reversal tests](native/axis-command.xml) pass 2/2.
The command math was separated from NBT persistence to run on the pure JVM test
classpath. The initial test-classpath failure did not launch a native server.

Pinned Sable/Rapier source inspection (shared Flying investigation) establishes
that motor gains are acceleration-based and the boolean enables the force cap.
Axis uses mass-independent gains, measured gravity moment compensation in that
same actuator, and an explicit force cap. Passive damping is .4 s⁻¹. Capture's
angular-entry threshold applies once per alignment, preventing actuator chatter;
dock, fault, restraint and travel guards remain active throughout.

The accepted native world is preserved outside build outputs at
`/mnt/NVME4/fourthdimension-runs/fd1302-axis-20261115/native-05-world`.
Its process saved all dimensions and exited cleanly at 08:30:06 UTC. The earlier
root-device native processes remain in progressing clean saves; they are not
accepted live runtimes or a handoff. They still require verified exit.

Remaining Axis A/C evidence: normal player board/operate/capture/unload, banked
one/two-car occupied passage, eight-block native Chain interaction, and new
longitudinal rider/camera composition. B needs affected visible asset/control
review and example usability. No Axis card is closed yet.

## Physical hinge correction — 2026-11-15 08:42 UTC

Flying's investigation identified the native pose contract: body position and
rotation point describe mass COM, while the constraint owns its local hinge.
Axis creation now retains COM and computes its world position relative to the
intended hinge instead of overwriting rotationPoint with the hinge. This changes
only new Axis allocation; no per-tick pose override was introduced.
[The targeted native check](native/native-hinge.log) passed with empty joint
separation .00241081 blocks and asymmetric occupied separation .00241093 blocks.
The process saved all dimensions and exited; its world is preserved outside
build output at `native-hinge-world` beneath the NVME4 run root.
The previous native roll/load/power/capture result is carried forward: this fix
corrects initial world geometry, not the rotary drive or interruption logic.
An opt-in read-only Axis trace records actual joint, seat, rider, roll, banking,
fault and capture state for the pending connected-client check.

## Normal-input checkpoint and example correction — 08:52 UTC

On private :316, normal raw-input turning changed yaw/pitch and normal right-click
placed a vanilla stone (08:44:15 screenshot). The same fixed settings placed two
Axis cars at bearing centers `(0.5,83.625,50.5)` and `(0.5,83.625,58.5)`.
Empty-hand station binds reached Ready; the second temporary station is disclosed
setup. Chain first selected chassis `d8ee30e3-b6e0-44da-93d2-e4b6b36871d1`,
then the other bearing at exactly eight blocks. Its selection data cleared and
the rendered native link plus client train IDs confirmed the two-car consist.
Normal Shift-use rebound both to the declared first dock.

At 08:47:28, one normal seat use boarded Dev in
`c1993c52-1171-4fe4-bf0e-e5490b9f263d`; native mass became 4.8 from 4.0.
Normal held Rotation Tool opened the seated editor. Close restraints (action11)
and Dispatch (action13) succeeded and applied to both linked cars. The train
traversed the banked curve with asymmetric load, independent pod roll and retained
rider. The received client snapshot shows both native train members loaded,
eligible native support, closed restraints and the assigned rider. Camera/native
seat quaternion agree at the retained banked state (see `before-drive-repair.json`
in the run evidence). No helper mounted the player or dispatched the train.

The leading bogie stalled near the powered 30-degree anchor at `(24,83,124)`;
its adjacent Creative Motor at `(24,83,125)` enters the banked running-gear
approach. Both native rail support and Chain power remained valid. Normal Capture
stopped the consist with restraints retained and Ready false off-dock.
The example now spans edges1–3 continuously from the upright approach and omits
motors at the two banked anchors, following the accepted Fixed Inverted layout
repair. Equivalent disclosed world NBT/setup commands are retained in
`evidence/drive-repair-commands.txt`. Server and client saved/exited before the
same occupied world reload, which refreshes the native Chain cache. Remaining:
confirm passage through the former stall point, programmed turn, return and unload.
Normal placement/coupling/boarding and native mechanics are carried forward.

The original root-device native01 process829672 finished all-dimension saving at
08:51:41 UTC and exited. Native02 process838261 still advances its clean save;
its world remains untouched. No forced kill was used.

## Return/lift-entry recovery correction — 08:57 UTC

After the continuous-span repair and normal Resume, both cars passed the former
stall point. The occupied pod completed DRIVE's unwrapped turn and transitioned
to DAMP/FREE without faults or restraint release. The client received the same
unwrapped angle and kept the native seat attachment; `occupied-drive.json` records
angle5.04287 during DRIVE. The return stopped safely on the declared dock edge
because the asymmetrically loaded pod was not yet settled upright for Lift Lock.

That revealed an actual recovery deadlock: `dock()` required the two-block unload
inset even to run the bounded alignment servo, while the lift entry guard stopped
the car before that inset. Axis now permits **retained alignment only** at the
same declared level dock edge; matching rail/fingerprint, physical level pose,
station ownership/range, full-sweep clearance, stopped state and fault acknowledgement
remain required. Ready/open still require the two-block inset for both cars.
This is the only reopened behavior. The occupied world was saved cleanly before
its targeted replay; previous placement/coupling/drive/reload evidence remains valid.

## Final B/C closure

[Construction, content review and cleanup](BC-acceptance.md) close FD1302B/C.
A remains accepted; the explicitly continued runtime is now saved and stopped.
