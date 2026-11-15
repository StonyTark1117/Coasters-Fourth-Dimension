# S2 content and cumulative acceptance — FD1501E/F

Qualified 2026-11-15. This closes the remaining S2 content/player claims together
with the accepted [D native and occupied evidence](../README.md). No S1, earlier
family, profile sweep or closed D case was rerun.

## Runtime and scope

Artifact 02 SHA-256 `be57c7f7954319908520d7099444b9e9aab9fe30bbf07df5bee98a247d07836d`.
Java 21; client 1536 MiB heap in its own 6 GiB service, server 2048 MiB/4 GiB;
private Xvfb :318, endpoint 127.0.0.1:25654. Owner chat
`01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`. Persistent root:
`/mnt/NVME4/fourthdimension-runs/fd1501d-s2-20261115`.

Normal inputs are recorded in [input.jsonl](input.jsonl). All setup commands are
disclosed in [console.jsonl](console.jsonl) and [initial setup](content-setup.jsonl).
Commands supplied upstream recipe ingredients, recipe unlocks, Rotation Tool,
one existing shared Damper item, two blank native anchors, utility platforms,
viewpoints and the supplied track-only example. They did not craft S2 outputs,
place the car/governor, bind equipment, edit controls, board, dispatch or unload.
An off-platform setup teleport caused a fall/death; the three dropped inventory
stacks were recovered by teleporting those existing item entities to Dev. No S2
item was replaced or duplicated. Later safe viewpoints used verified platforms.

## Normal construction and controls

| Claim | Result / evidence |
| --- | --- |
| Three S2 recipes | Survival recipe book crafted sixteen tracks, one four-seat car and one governor. [Track](track-recipe.png), [car](car-recipe.png), [governor](governor-recipe.png), [inventory](crafted-inventory.png). Six iron/two redstone/red dye paid for track; car consumed four Red Seats, Mechanical Bearing, native Coaster Cart, two iron and one crafted track; governor consumed four iron, two redstone, two shafts and Clutch. |
| Paid track construction | Two normal endpoint clicks connected supplied blank anchors eight blocks apart; inventory decreased from 15 to 13 S2 tracks. [Connection](paid-track-connection.png); final saved foundation has the exact S2 variant. |
| Car and station | Normal car right-click consumed the crafted car; normal station use bound it. [Placement](normal-car-placement.png), console block/inventory readback. |
| Governor | Normal placement consumed the crafted governor in the shaft gap, facing WEST. Empty-hand use bound the stopped empty car and native Chain anchor. Real supplied input was -192 RPM, output 0 while held; status and NBT agree. [Placement](normal-governor-placement.png), [binding](normal-governor-binding.png). |
| Lift lock | Occupied approach correctly rejected the edit. On a clear remote Chain section, normal GUI clicks enabled then disabled the lock. [Enabled](lift-on.png). The distinct controls do not overlap. |
| Magnetic equipment | Normal Apply magnets installed `2:20:damp:1:6:0.1:1`, advanced the rail revision and consumed exactly one supplied shared Damper item. [Payment](magnetic-payment.png), inventory readback. |

The Rotation Tool needs an actual block hit: the side of the native anchor
worked. Earlier clicks along its curved-track surface produced no handler event;
those are not accepted interactions. The first seat cushion hit was beyond
normal Survival reach. Moving to a closer platform gave the same seat hit within
reach and a normal successful mount. Mouse settings were never changed.

## Operating and lifecycle evidence

The crafted car was normally boarded, restrained and dispatched through the
crafted governor. Dispatch closed the editor. Clear third-person captures show
launch, physical free spin and the full track roll with the actual occupied
carrier: [launch](operating-00.png), [roll](operating-07.png),
[inverted horizon](operating-09.png), [inversion](operating-11.png),
[return](operating-15.png). These supply the previously missing unblurred
operating asset views; native full-roll dynamics remain covered by D.

A single normal GUI Capture click was conditioned on read-only player position
at the level return dock, as disclosed in [click log](content-return-click.log).
The train stopped, aligned and became Ready. Normal Open restraints and Shift
unloaded Dev: [open](open-restraints.png), [unload](normal-unload.png).
[Final state](content-unloaded.json) records both cars empty and captured; the
crafted car has no fault, owner held, open restraints, phase DOCK_OPEN, latched
carrier and negligible angle/omega. No command operated ride state.

The actual process restart before content work preserved the original D car's
-4π winding and empty captured pose. [Restart readback](restarted-empty-held.json)
records authority epoch 2, held and suspended, explicit-Resume reason; the saved
pending ramp did not produce output or auto-launch. D already supplies actual
power loss/restoration, explicit restart/revalidation behavior, four-car native
inversion/reverse and body/coupler fault retention. This cumulative evidence
covers S2's distinct lifecycle cases without repeating accepted sibling behavior.

## Finite asset review and handoff

Original generated resources cover the dedicated track/car/governor inventory
icons, four red seats, open/closed black restraints, turntable, bearing and amber
output port. Inventory, placed-world and occupied launch/inversion/return views
above show legible pixel textures, aligned riders/restraints, stable body geometry
and no missing-texture surfaces. Native dimensions and clearance are unchanged.
The [player guide](../../../../SPINNING-S2.md) teaches recipes, native anchors,
launch supply, magnetic target mask, lift lock, capture/unload and retained
recovery. `/fd demo s2_spinning_track` adds a no-car construction example alongside
the one/four-car examples. Lead supplied the S2-only magnetic-tooltip wording fix;
this text-only change carries forward the demonstrated control behavior.

FD1501E and FD1501F are qualified for lead integration and central closure. Their
finite views feed FD916's later reconciliation; they do not open 0.9 work.

At 13:05 UTC the client exited, the server saved all dimensions and exited,
then Xvfb stopped. [Cleanup](cleanup.txt) confirms all three owned units inactive
with MainPID 0. The final world is preserved as `content-unloaded-02-world` outside
the build tree, alongside the live-directory saved world, full logs and raw views.
No runtime remains assigned to this completed S2 check. Powered Inverted Yaw is
the next assigned package and will use a separately prepared runtime.
