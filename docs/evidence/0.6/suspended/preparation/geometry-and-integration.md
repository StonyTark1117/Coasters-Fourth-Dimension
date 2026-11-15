# Suspended geometry/content preparation

Input is the accepted FD1301 design at main `d73d43b8`; this document maps it to
reusable assets and upcoming native implementation. It does not revise or re-run
0.5 acceptance. Platform confirmed locally: Minecraft1.21.1, NeoForge21.1.248,
Create6.0.10-280, Coasters0.1.5, Sable2.0.3, Java21.

## Exact carrier/seat mapping prepared for the axis API

Graph frame origin O; native axes X=right,Y=up,Z=backward. The symmetric cabin is
unchanged by choosing its Z-positive axis; its physical ±45° swing is about native
Z. Document the sign once for angle displays. Do not put the child on the old
X-axis lateral-wing path.

A child plot pivot at `(center.x, center.y+.25, center.z+.5)` and native seat
blocks relative to the center block `(-1,-2,0)` and `(0,-2,0)` place their block
centers at graph `(±.5,-3.25,0)` when the parent pivot is graph `(0,-1.5,0)`.
Parent anchor is native bearing center plus `(0,-1.8125,0)`: the inspected bearing
anchor is graph+.3125Y. This avoids integer-block rounding of the specified seat
centers and avoids applying the bearing offset twice. Child physical orientation
and rotation point must be consistent; the guide continues to own the parent.

The cabin resource mounts at child `(0,-3,0)`: its model floor Y8…10 gives graph
Y=-4.25…-4.125. Model X-16…16 gives ±1 block; model Z-12…28 about the pivot Z=.5
gives ±1.25. Side panels leave both seat-door openings at local Z0…16. Two seat
models use original orange cushions, black open/closed restraints and iron edging.
The folded open restraint ends at graphY=-1.875, still below the declared -1.75 top.
No riding-state livery feature is introduced.

## Actual Create seat semantics inspected

Java21 `javap` of the pinned Create slim JAR shows `SeatBlock.sitDown` creates the
seat entity at block `(x+.5,y,z+.5)`, **not** y+.5. `SeatEntity` height is .35;
`positionRider` adds .0625 and subtracts the passenger VEHICLE attachment. The
Minecraft1.21.1 source artifact defines player's VEHICLE attachment at .6 and
standing height1.8. Thus nominal player feet are seat-block Y-.1875 and the
conservative standing-height top is Y+1.6125. With block origin graphY=-3.75,
that top is -2.1375, within the accepted cabin bound. Native rider render and
actual model/armor vertices still need the occupied geometry check; this arithmetic
is a mapping input, not rendered acceptance.

Cushion-only use shape and a larger rendered backrest differ in existing seats.
For new Suspended seats, expose a deliberate cushion/back/arm use outline matching
the authored blocks, while avoiding a solid collision barrier that traps the rider.
Before each native client use, check actual interaction hit and distance; do not
rely on a highlighted backrest or send use after MISS. The two open side doors
provide a reachable cushion ray from an outside dock platform.

## Hanger and track input for Fixed Inverted reuse

Keep N wheels unchanged. New track spine/supports must be **above** the running
gear or outside the cabin swing. In particular, do not retain the old custom
`RUNNING_UNDERBRACE_HEIGHT=-.70` just because its centerline seems clear: its .06
radius also occupies space. The old centerline-to-cabin clearance is not the mesh
clearance. The accepted design already requires original overhead supports.

Proposed original track spine center Y=1.25 with .18 cross-section; transverse
support centerY=1.0 at each declared interval; contact-side stubs outboard of the
measured1.12 wheel envelope. Validate actual native wheel mesh/contact clearance
before accepting it. No below-track decorative spine runs through the hanger.

Hanger fork faces at Z=±1.5 stay outside cabin |Z|≤1.25. Each rod/fork has radius
.0625. Center journal runs along Z at graphY=-1.5; its collision with its bearing
is intentional, not scenery. Static upper fork rods bridge to the bogie without
entering the rotating pod cylinder. Required original pieces can be block-native
segments or a family renderer under existing body ownership; renderer-only cabin
motion is prohibited.

The reusable overhead output is N contact geometry + original overhead support
mesh/ownership + swept-envelope/dock checks. Free swing/limits/lift state belong
only to Suspended; Fixed Inverted consumes the overhead interface with rigid seats.

## Prepared resource inventory

`build-assets.py` generates12 parse-checked resource files /50 cuboid elements:
seat open/closed + eight facing/state variants, two-seat cabin, lock/free indicator,
recognizable overhead-car inventory model, matching overhead-track item, and
track/car recipes. It writes only `preparation/resources/`. `language-input.json`
is a merge input for lead-owned language data, never a replacement shared file.
Vanilla texture references are deliberate material choices, not missing assets.

Registration is deferred to the explicit file handoff. Native lock state must drive
its indicator; the indicator never substitutes for an actual joint lock. These
assets need placed/occupied/inventory views and the finite FD916 review after
integration. A generator pass does not close B.

## Minimal integration requirements sent to lead

- Integrated S1 axis/pivot measurement, native joint, save/restore, loading and
  rider/camera handoff; consumed by commit, not copied from an unfinished checkout.
- Native generic joint with five locked DOFs and Z angular `setLimit(-π/4,+π/4)`;
  soft damping from35°, normalized `.65*I` coefficient and1.5 rad/s guard. No
  artificial second gravity solver. Latch acquisition checks actual angle/rate.
- Independent lift/dock binding and phase: dock-open, locked departure/lift,
  all-car exit release, free swing, damping and stopped align. Pending/latched
  interruption restores exact state with explicit resume.
- Family dispatch/board/consist/track hooks; up-to-four same-family8-block links;
  original overhead support rendering and family-specific native contact rejection.

No production edits or launch before the lead records exact file ownership and
FD1501A's occupied contract is integrated. No blanket test expansion follows this
preparation; native and client checks will address only the stated new claims.
