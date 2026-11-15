# FD1601A mechanics and occupied handoff — 2026-11-15

Owner `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`, reviewed HEAD
`bfb676d4063e903e7649203b4a20143d39af9866`. Changes are uncommitted for the lead
to integrate; all Git and central status work remains with the lead.

## Qualified fixed mechanism

The four-abreast row shares its native chassis rigid body. There is no independent
rotor, swing joint or drive; fractional seat mounts and added rider mass use the
same exact centers. Fixed-only native seat entity ownership avoids double-counting
adjacent boundary-centered seats. Fixed saved records retain identical chassis/
seat body, exact dock geometry and remembered train membership without serializing
unused rotary angle/axis fields. Other families retain their existing paths.

`native/accepted-native.log` passes four-car native topology/coupling, exact3.2
added mass for four adjacent riders, dispatch, wrong-dock exclusion, fixed
serialization and frame-loss whole-train hold. `native/unit.log` passes the
Fixed-specific contract/mount/envelope rejection test. The two earlier failures
were fixture binding/cache timing; their raw logs remain preserved.

Matching fixed overhead track requires radius>=16, roll change<=5degrees/block,
1–4 identical cars,8-block spacing and speed<=16blocks/s. Runtime checks actual
current/stopping-distance corridor and the following connected edge; power,
controller, frame, coupler and support failures retain/stop the remembered train.
Dock rebind/readiness require the exact declared level geometry, whole train and
closed recovery state. Resume is explicit; redstone release cannot dispatch.
Shared repaired-support realignment is retained; ordinary-bearing tow is rejected.

## Actual occupied run

Java21 packaged client :314,6GiB client-only scope/1536M heap; dedicated server
ports25646/25647 with2GiB heap/4GiB scope. Default Use/Attack and Raw Input remained
fixed. Same-client device camera turning and reachable vanilla lever use passed.
Disclosed setup supplies route/materials/position; it earns no construction credit.

`occupied/` holds readbacks, captures, exact artifact hash and compressed original
input/console/client/server logs. Normal cushion right-click mounted Dev in first
car; normal Close restraints and Dispatch operated all four linked cars. The
first target found an invisible full-block frame outline; its correction now
uses only visible geometry and passed the same normal seat interaction.

The first quarter-roll exposed the example motor in the required swept corridor.
The guard stopped all cars and retained the rider. Moving the motor to the other
side instead fouled running gear. The corrected example uses a continuous native
128-block Chain span over the four roll edges, driven from the upright approach.
No clearance or physical collision was weakened. The source generator and retained
world now use that arrangement; disclosed repair commands are archived.

The occupied train then traversed the complete inversion and return route without
fault. Native20Hz pose observations show every car's upY below-.99997, fixed seat
spacing error below6e-14, and identical chassis/seat ownership throughout. The
15-axis oriented-box analysis of all four reserved envelopes found at least3.5447
blocks separating gap across4867 complete four-car samples. This is sampled
native evidence, complemented by runtime stopping-distance/world obstruction
checks, not a claim of a mathematical continuous-time collision proof.

Normal Stop held all cars at their current banked poses. Clean save/reopen
preserved all four native poses (coordinate drift under.0001 blocks), retained
restraints, stopped state and Dev's same native seat. Resume required the normal
operator button. At the returning declared dock, normal Stop reached Ready,
Open restraints opened all four, and normal Shift cleared native rider assignment.
`dock-unloaded.json` reports passenger=false, no assigned vehicle, native seat
orientation=null and cameraUpY=.99999994. No helper mount/dispatch/unload was used.

Wing/Suspended/S1 representative remote-rider and shared lifecycle evidence carries
forward: this family uses unchanged native mount/synchronization/camera ownership,
with fixed-only mount centers and entity ownership covered here. No second-client
synchronization implementation or earlier-family case is changed.

## Continuing ownership

This closes A's occupied handoff and supplies later Flying/Powered Inverted
consumers. B/C still own normal construction/crafting and finite content review;
those use this proof rather than repeating the ride. No0.7 work is activated.
The same owner actively continues B/C on exact root
`/home/braydon/.local/state/fourthdimension-runs/fd1601-fixed-inverted-20261115`,
world`server/FixedInvertedWorkshop`, display314, ports25646/25647 and
`fd1601-fixed-inverted-20261115-{client,server,xvfb}.service`. No runtime is handed
to another chat. Final outcome cleanup will save and stop all three units.
