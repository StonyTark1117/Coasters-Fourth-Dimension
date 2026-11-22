# Axis/Suspended fork-attached brake — P3 affected views

Passed 2026-11-22. Source/runtime owner `/root/fd13_structure_reconcile`,
primary `main` at `b5e78f31` plus the frozen FD2104 working changes. This is
the finite changed-geometry check for P3. It combines the assembled-source
[reconciliation](../base-structure-reconciliation.md), the focused geometric
[audit output](geometry.txt), and the client observations below. It does not
claim another physical ride, occupied route qualification, or all-family review.

`VehicleMechanismRenderer.java` SHA256:
`bc47271faefa6376e184c76d55f7e657086fe176c5f959ab96a2228cfd85b49a`.
[Inputs](inputs.sha256) pin the renderer, audit, packaged JAR and launch args.
The packaged JAR was on the direct launch classpath; this run did not use a
second mod copy in `client/mods`. [Build](build.log) succeeded under Java
21.0.13.1 / Gradle 9.2.1, offline, no daemon reuse. Six existing deprecation
warnings were reported; no build error.

## Owned run and method

Parent explicitly handed off its stopped, saved runtime:
`/mnt/NVME4/fourthdimension-runs/fd13-base-20261122`, run record owner
`fd13-coordinator`, Xvfb `:341`, ports `25713/25714`, world `SkinWorkshop`.
All three `fd13-base-20261122-{client,server,xvfb}.service` units were inactive
before reuse. Only these units/display received input. Current connection
readback showed Dev online; window `2097160` was 854×480, and default mouse
bindings with `rawMouseInput:true` were retained. The existing run's accepted
normal input setup was reused; only F1 was pressed for these observations.
Camera positions, restraint setup and poses were explicit console fixtures.

Client used `JAVA_TOOL_OPTIONS=-Xmx1536M` in its separate 6GiB unit; server
used 2048M in a separate 4GiB unit. Gradle stayed outside both. Initial host
headroom was 39GiB; persistent NVMe XFS had 163GiB free. Observed unit peaks:
client 2,776,150,016 bytes, server 1,719,230,464 bytes.

The observer was put in spectator mode to prevent gravity moving the camera.
Sable `out_sub_level` transformed exact native plot coordinates into the
world, with `anchored eyes` for aiming. For the two rotated stills, Sable
physics was paused, the child orientation changed, and its translation
adjusted around the existing pivot. Temporary marker positions measured
parent/child pivot coincidence. This is a static pose fixture, not motion
acceptance. All commands and readbacks are in [console.jsonl](console.jsonl).

An initial Suspended pose measurement incorrectly used an integer X coordinate,
which Minecraft centered by +.5. It was diagnosed from the coordinate command
semantics and corrected to explicit `.0`; intermediate 17:37 screenshots in
the persistent raw run are not accepted evidence. The accepted 17:38:42 image
below uses the corrected, measured pose.

## Observations

Suspended chassis `b09ec612-3b1b-4bee-a1c4-737b74014e50`, child
`8c3e0cbe-3955-4061-83c6-54f4731d4777`, reused the original saved vehicle.

- [Neutral, open](suspended-neutral-open.png) and [neutral, closed](suspended-neutral-closed.png): the stationary race is against the existing front fork; its receiver/journal remains centered. The disc/caliper package is above the cabin and clear of the shown seat/restraint geometry.
- [35° swing](suspended-swing35.png): the cabin and journal rotate together while the fixed race/arms remain on the fork. No visible opening at that attachment or new seat/restraint intrusion in this view. Parent pivot was `(200.5,78.8124923706,230.5)`; corrected child pivot `(200.4999711181,78.8124511230,230.5)`, separation below .000051 blocks. This is within the existing ±45° swing envelope.

No Axis vehicle existed in this saved world (`inspect axis` required exactly
one). A disclosed `fd demo axis` at command source `(-300,140,-500)` created
chassis `1991ad02-6f03-4f19-8065-b2b355d728ab`, child
`fda9bc96-61b4-4e4c-81a2-87ef02858562`. The example route was not dispatched.

- [Neutral, open](axis-neutral-open.png) and [neutral, closed](axis-neutral-closed.png): the fixed ring front face meets the existing fork clamp; the child shaft, collar and seat structure are visible through/behind the mount.
- [90° roll](axis-roll90.png): the rectangular journal and seats turn through the widened race, while the fixed ring stays at the same fork endpoint. The observed collar remains coaxial with the shaft. Parent pivot `(-295.4999872657,144.8124259055,-451.4999999983)` and child `(-295.4999872657,144.8124259055,-451.5)` agree within .000001 blocks.

The analytic audit covers the rectangular shaft's worst corner radius, the
32-sided fixed bore, endpoint planes, receiver grip and caliper bridge outside
the rotating disc. The two visual angles complement that calculation; they
do not independently sample every intermediate angle. Existing occupied
mechanics/seat/camera/restraint evidence is retained: P3 changes only the
external brake placement and clearances, without changing those interfaces.
These screenshots are empty-car observations, and restraint changes used the
disclosed fixture, not normal player interaction.

## Restore and cleanup

Both children were restored to neutral, physics unpaused, restraints opened,
and the final live state reported stopped/latched with no fault. Temporary
markers were removed; Dev was returned to Creative mode at the Axis station.
`save-all flush` reported saved, then `stop` saved all dimensions and exited.
Client and Xvfb were stopped afterward. [Cleanup](cleanup.txt) verifies every
owned unit inactive, MainPID=0, Result=success. [Server log](server.log) retains
the final saves; [client log](client.log) retains the loaded mod and run state.
The saved world, original evidence and all intermediate observations remain
under the persistent run root. No production source changed during this run.
