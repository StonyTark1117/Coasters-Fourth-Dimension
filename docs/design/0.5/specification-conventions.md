# Shared conventions for the ten 0.5 vehicle specifications

**Design version 1, 2026-11-14 America/Phoenix.** These are original game designs,
not physical engineering dimensions, manufacturer replicas or implemented features.
Each linked package is normative together with this file. The finite implementation
claims are plans consumed by its existing A/B/C tasks, not new 0.5 runtime gates.

## Coordinates, fit and dimensions

Use a right-handed **design** frame `O=(graph sample), X=-TrackBasis.right,
Y=TrackBasis.up, Z=canonical TrackBasis.forward`; bank is part of that frame.
The pinned native quaternion has columns `(right, up, -forward)`, so the design
frame is `B = Q_native * Ry(π)`. Convert design-local X/Z signs once when writing
native plot coordinates; do not rotate the rail body to evade its guide. Thus
`+Z` means forward in these specs, while native model north is `-Z`. The chosen
positive X is the opposite transverse side to native +X. This keeps all yaw/pitch
formulae right-handed. Local upright means +Y, not world up.
A reverse-moving car retains this canonical frame. Native bearing center is
`O + .3125Y`; do this conversion once. All lengths below are blocks; angles are
radians in storage, degrees in player-facing descriptions; speeds use blocks/s,
angular speeds rad/s, acceleration rad/s². Every unlabelled new dimension is a
chosen game dimension. Inspected native dimensions are explicitly identified.

`N` running gear preserves measured Coasters centers `(±.772,.325)`, rail
cross-section half-width `.13125`, half-height `.075`, wheel radius `.2`, wheel-row
separation `.55`, and tread span `.625… .9375`. A dedicated overhead frame may
move its spine/support away from occupants but may not silently move these contacts.
Pipeline `P` uses its explicitly dimensioned new side contacts and an original
wheel renderer. No new family fits ordinary track merely because its bogie uses N.
The qualified vehicle envelope, functional capture/propulsion capabilities and
mount type must also match. Reject an unknown, undersized, wrong-family or
missing-mechanism provider before spawn, coupling and boundary traversal.

Current four-scalar `Gear` and `Clearance` records are insufficient to describe
contact orientation and longitudinal extent. Consumers add a versioned contact
layout/mount frame plus `(halfWidth, aboveO, belowO, halfLength)` envelope. Preserve
old serialization/IDs/defaults. Do not reinterpret `sideContact=.9375` as a real
world side-wheel radius. Provider identity and artwork never decide eligibility.

Clearance includes body, restraints, default player model and vanilla equipment
within the authored rider bound. Reserve at least .25 between those swept bounds
and track/support/scenery and .5 between separate vehicles; tighter joint bearing
interfaces are explicit mechanical contacts. The numeric envelope is a conservative
design reservation, not measured rendered clearance. Curved/banked/linked sweeps
use actual native transforms and continuous/substep bounds in later mechanics.
Station platforms occupy only the specified boarding opening and retract/clear
before motion. Unknown oversized cosmetics need their own envelope declaration;
accepted ordinary default-player scope is not an arbitrary-model guarantee.

## Physics and ownership

One Coasters chassis owns longitudinal rail engagement; Sable owns physical body
and joint motion. Each independent carrier is a native body, with riders attached
through one seat owner. Fixed carriers may use the integrated fixed-seat contract.
Do not run the reference pendulum solver in addition to native gravity/inertia.
`NativeLoading`'s equal `.8` mass-unit rider convention applies; pose-aware torso
points move with the same physical carrier. No inferred player weight. Empty mass,
center of mass and inertia come from the authored blocks/mass tracker and must be
logged by each first native prototype; the specified ballast is deliberate game
input, not a claimed measurement of an unbuilt vehicle.

Damping is `tau=-c*omega` on relative joint velocity. Values called `c/I` are decay
rates in s⁻¹, converted using measured native joint inertia I. Do not apply a raw
coefficient to differently sized carriers. For a hard angular range, use a native generic joint with the other five DOFs
locked and `GenericConstraintHandle.setLimit`; the rotary handle only supplies
a motor. Validate finite unit axes in add-on code; native validation is not enough.
Capture uses a bounded servo (default
max 0.35 rad/s, 0.5 rad/s²) then latches only at error ≤2°, |omega|≤.05 rad/s,
travel speed≤.05 blocks/s, and a valid designated dock for five consecutive server
ticks. Moving lift locks have their separate explicit guard. Fault stopping uses
physical braking/damping; never teleport an occupied carrier to a capture angle.

Safety precedence: lost support/invalid consist → emergency travel stop; missing
body, authority or interlock → deny new drive and keep restraints latched; valid
station capture → unload permitted; otherwise ordinary ride behavior. Holding
travel is independent of whether a carrier is aligned. A failed alignment keeps
the car stopped and restrained with a visible reason. Loss of power never releases
a latch. Restoring power never auto-dispatches. Recovery retains the measured pose,
revalidates support, then explicitly resumes bounded movement to a safe dock.
Native brake operation under missing propulsion power is a deliberate passive
fail-safe game rule. Existing ordinary-bearing recovery compatibility is not
silently extended to new contacts or unknown secured envelopes.

One server operations owner validates all dispatch, restraint, capture, direction,
posture and yaw requests. Normal player controls are car placement on a matching
track, seat use to board, editor/controller requests, and Shift unload only when
permitted. No helper boarding or forced passenger movement earns player evidence.
FD1701A provides shared hold/release authority for Dive and Powered Inverted; it is
not the later multi-train manager. Other vehicles consume existing validated
operations, adding only the needed mechanism state.

Rendering consumes physical interpolated body poses once. Seats/riders and view
use the same composed pose. Player look remains independent; comfort changes only
presentation, never physics. Restore normal camera on dismount, death, disconnect,
dimension change and unavailable body. A second real client is needed only for a
new synchronization path; reuse the lead's accepted shared handoff otherwise.

## Persistence and synchronization schema inputs

Every package stores versioned family/variant and contact-layout IDs; chassis and
carrier UUIDs and hierarchy; route endpoint IDs/fingerprint and canonical distance;
seat IDs and rider UUID assignments; restraint, stop/fault and capture states;
provider/controller binding and authority epoch; joint pivots/axes, angle plus
unwrapped turns, omega and limit/motor mode; ballast and validated parameters.
Snapshot a single post-physics instant. Keep the actual physical pose; rebuilding
joints restores relative velocity once. A fixed carrier has no synthetic rotor state.

Synchronize definitions once per revision and server-authoritative runtime state
with tick/sequence/epoch. Late join receives a complete hierarchy before riders;
stale packets cannot reset newer pose or release state. A missing body or route
holds operation, preserves identity and permits explicit recovery; never create a
second rider assignment or silently substitute the nearest edge. Schema migrations
preserve the original five families. A permanent removal clears owned bodies,
mounts, bindings and constraints exactly once.

## Common finite later acceptance (C1–C6)

These are a compact mapping of existing mechanics/content/player criteria, not
additional milestones. Each package adds only its distinct claims.

1. **C1 Fit:** normal placement on its declared track succeeds; wrong contacts,
   family, missing capability and occupied/obstructed sweep reject with a reason.
2. **C2 Mechanism:** selected dimensions, physical mechanism, declared empty/full/
   asymmetric load and rate/limit guards pass native checks; original-family paths
   remain accepted unless a named shared change affects them.
3. **C3 Occupied workflow:** normal construction, boarding, restraint/dispatch,
   representative travel, stop/capture/open/unload and camera restoration; early
   mechanics occupied handoff is reused for later acceptance.
4. **C4 Lifecycle:** required distinct interruption, save/restart, reconnect/late
   join and permanent removal retain correct identities, pose and authority.
   Reuse common unchanged lifecycle evidence rather than a family × event matrix.
5. **C5 Geometry:** vehicle/track/default-rider restraint sweeps and declared
   consists are clear on the playable example, including new combined motion.
6. **C6 Content:** original functional assets, item/recipe, visible state/control
   labels, tooltip/guide and buildable example accurately teach the implemented
   finite configuration; feed accepted views to FD916.

Optional provider absence always retains the standalone example. No future family
is declared implemented by these specs; no release publication is authorized.
