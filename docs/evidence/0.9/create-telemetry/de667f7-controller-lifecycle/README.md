# Integrated candidate: controller binding and tracking re-entry

On 2026-11-02, two connected clients exercised the exact standalone source
`de667f7bd7deef808d6229df9e2d386e87dd598a`, packaged JAR SHA-256
`6231a0241bef082df3f3edff689e74ae2604465aa095c13656136f13e10de3c5`.
This continues the existing dedicated world with validation logging; no rebuild
or runtime code change occurred. [manifest.json](manifest.json) records hashes.

## Native unload and fresh client re-entry

Observer initially received/drew the empty four-car PAX train with closed
restraints and dispatch ready. Both clients then moved to separate remote
platforms near `(900,85,-900)`, outside the ride's tracking range.

The first attempt retained the winch chunk as forced: native availability stayed
4/4 chassis and 8/8 carriers, so that attempt is not an unloading result.
Removing all force-loaded chunks permitted native availability to reach 0/4 and
0/8. Dev returned, normally opened restraints through the Rotation Tool editor
while Observer remained away, and left again. The retained second unload record
confirms 0/4 chassis and 0/8 carriers with no assigned riders.

When Observer returned to the winch, all four original chassis and eight original
carriers became available. Its received snapshot and visible goggles showed open
restraints and dispatch denied, replacing the earlier ready snapshot. The saved
vehicle/carrier identities are unchanged. The winch's normal Display Link binding
and source configuration also survived.

This proves an empty native unload/reload and fresh tracking re-entry state. The
non-ticking controller's sign stayed blank while the vehicles were absent; no
live client or sign display of `Vehicle or carrier unavailable` was captured in
that interval. Unavailable-message presentation is exercised separately below.

## Unbound station presentation and normal binding

A separate straight Arrow section was placed with the normal Controlled 4D Track
item between fixture anchors `(0,83,20)` and `(0,83,56)`. The normal Controlled
4D Car item placed a car at the first anchor. It was retained by the existing
end-of-compatible-track guard; this is not a demonstrated operating circuit.

Observer normally placed a Boarding Station at `(-6,83,24)`. Before binding,
its received snapshot was unavailable and its goggles read **No bound 4D vehicle**.
Observer used the normal Display Link item and configuration GUI to connect that
station to the sign at `(-6,84,30)` with **Ride status**. The sign stored the same
message and visibly showed its width-limited prefix, **No bound 4D**.

A normal empty-hand use bound the nearby Arrow car and opened its editor. After
closing the editor, goggles showed the actual available one-car, four-seat state,
and the same display source replaced the unavailable text with the car's actual
**End of compatible track; capture and inspect the route** warning. The sign's
visible prefix is width-limited. This records the controller's actual response;
it does not claim that this endpoint fixture is a completed ride or fault-free.

## Limits

The PNGs, paired server observations and client log prefixes establish the cases
above. All processes were still active when these prefixes were captured. Anchor,
platform and blank-sign setup, inventory and positioning used commands; track/car/
station placement, display binding/configuration and restraint controls used
normal client interactions. No telemetry packet or displayed text was injected.

This does not certify occupied chunk interruption, every family/profile, or a
visible unavailable state during native unloading. Hidden Xvfb/llvmpipe is
functional GUI evidence, not hardware/FPS or complete rendered-clearance proof.
FD401 and the full 0.9 gate remain open.
