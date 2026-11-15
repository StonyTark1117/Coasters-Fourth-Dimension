# FD1501 — S1/S2 Spinning specification

**Specification complete; implementation remains FD1501A–F.** Version 1,
2026-11-14 America/Phoenix. Normative [shared conventions](specification-conventions.md).

## Adopted variants and sources

[Gerstlauer's primary Spinning page](https://www.gerstlauer-rides.de/en/products/roller-coasters/spinning-coaster)
supports free vertical-axis passenger rotation and station alignment/locking.
[Maurer's primary S-Car brochure](https://media.maurer-rides.de/products/spinning-coaster/documents/SC_Prospect_en.pdf)
describes rider-load and track-dependent response. [MACK Xtreme Spinning](https://www.mack-rides.com/products/xtreme-spinning-coaster)
confirms free full-turn spin, inversions, LSM launch and external magnetic spin
influence. Exact manufacturer masses/pivots were not supplied by these references.
All dimensions, limits and arrangements below are original game choices.

| Contract | S1 conventional, delivered A–C | S2 launched/inverting, delivered D–F |
| --- | --- | --- |
| Variant | `spinning / conventional_v1` | `spinning / launched_inverting_v1` |
| Carrier | One 4-seat body about local **Y** | One 4-seat body about local **Y**, including when Y is inverted in world space |
| Seats | Back-to-back rows: `(±.60,1.50,±.75)`, face outward ±Z | Same declared seat spacing; distinct invertible harness/back/foot support assembly |
| Pivot | `(0,1.75,0)` | `(0,1.75,0)` |
| Rotating rider/body bound | Cylinder radius 2.0 around Y; Y=[1.10,3.40] | Cylinder radius 2.0; Y=[1.10,3.40], all closed restraint parts inside it |
| Reserved envelope | half-width 2.25, aboveO 3.65, belowO .95, half-length 2.25 | Same physical reservation; full bank/inversion sweep required in world space |
| Gear / track | N contacts; dedicated S1 envelope + capture capability | N contacts; dedicated S2 inversion clearance + capture + magnetic attachment + launch capability |
| Consist | One car | One to four identical cars at center distance 8, independent per-car spin |
| Route bound | Radius≥12, bank≤60°, slope≤60°, no inversions, speed≤10 | Radius≥16, full roll and inversions, roll-rate geometry≤5°/block, speed≤16 |
| Free damping | `c/I=.35 s⁻¹` | `c/I=.20 s⁻¹` |
| Spin limit behavior | Add passive damping above 2 rad/s; travel fault above 3 | Add passive damping above 3 rad/s; travel fault above 4 |

Full turn angles are unwrapped without hard angular stops. These speed bounds
are initial game operating limits, not physical manufacturer data; a solver
stability failure must be fixed or explicitly revise the design before acceptance.
There is no authored target-facing/scenery-yaw mode in either variant.

## Loading, capture and longitudinal drive

Use equal .8-unit riders at their actual torso points and native empty body
mass/inertia; no synthetic gravity torque. Side-to-side or front/back occupancy
changes the first moment/inertia and response to chassis acceleration. A balanced
load on a straight level constant-speed route need not spontaneously spin.
Physical dampers apply relative joint torque; do not impose a pseudo-random angle.
Do not derive spin from longitudinal speed alone.

State sequence: `DOCK_OPEN → SECURED_CAPTURED → DISPATCH → FREE → DAMP →
STOPPED_ALIGN → CAPTURED → OPEN`. Stop longitudinal travel independently of angle.
Alignment uses the bounded capture servo and common latch guards; target is the
nearest equivalent outward-facing station orientation, preserving winding. Lift
travel can retain the captured relative angle; release only after the complete car
leaves the designated lift-lock section. On a pitch-changing section this is a
chassis-relative lock, deliberately not a world-upright self-leveling seat.

**Working standalone propulsion:** native Create-powered **Chain** on a validated
lift/drive span, fed by a normal Create kinetic motor/source, through
`CoasterCartChainLiftDrive.applySpeedMatch`. This primitive is present in the pinned
Coasters 0.1.5 input and already used by accepted core routes. S2's standalone
launch is an original kinetic drive span using this native path, with a bounded
speed ramp from 0 to 12 blocks/s at ≤3 blocks/s² over at least 24 blocks. It is
explicitly a game substitute for the reference LSM. The ramp/controller adapter controls the actual Create kinetic source/span speed
through a stress-supplied drive governor; `applySpeedMatch` reads that speed and
its final argument is time-step, not a requested velocity. The governor, ramp and
interlock wiring remain FD1501D work; they are not an unavailable optional mod.
S2 does not wait for FD812. Optional Engineered may supply travel only through
its separately accepted adapter and the same single-authority interlock.

The example needs a ≥24-block clear launch section, a downhill/inverting coast
section and a physical stopping/damping dock. Only one selected drive writes
longitudinal impulse at a time. Chain/provider loss cuts drive and invokes the
shared passive brake; retaining rollback support does not permit an uncontrolled
reverse launch. Explicit stopped direction authorization is required to restart.
Magnetic spin devices have no authority to provide travel speed.

## S2 magnetic influence and interruption

A dedicated S2 attachment exposes local-Y `INDUCE` and `DAMP` spans. Reuse field
strength/distance falloff and rate-limited torque infrastructure where mechanics
match, but project torque onto the declared Y axis. `INDUCE` has a configured
sign and bounded torque such that |alpha|≤1 rad/s²; DAMP is opposing relative
omega and cannot add spin energy. At the guard speed both reduce to damping.
No programmed scenery target or instantaneous angle assignment is included.

Loss of launch power while rotating preserves native engagement and free/damped
carrier motion, brakes travel and retains restraints. Rollback uses the same
canonical carrier axis; it never reverses stored spin angle merely because travel
reverses. Coupler/body/route loss stops the entire consist; a surviving body's pose
is retained. Save/reload restores each carrier angle/turns/omega, independent capture
mode, magnetic span binding, launch-ramp progress and authority epoch. A pending
ramp is not resumed automatically after restart. Late-join rider/camera pose uses
one native body hierarchy and the common sequence rules.

## Interfaces and finite implementation handoff

[Native review](../../evidence/0.5/specifications/native-interfaces.md) confirms a
unit Y rotary normal is accepted, but current `CarrierRuntime` hard-codes X in
measurement and persistence. FD1501A must generalize those exact seams together
with seat-frame/loading, damping and capture. FD1501D adds only S2 magnetic-axis,
inversion/consist and launch differences. Dedicated tracks are chosen because
ordinary N wheel fit alone supplies neither this swept envelope nor capture or
S2 magnetic/launch interfaces. Appearance adapters cannot relax those requirements.

A finite native prototype plan covers: balanced versus asymmetric loading under
one same curve/acceleration input; free motion and damping without double torque;
stop then bounded capture; S2 inversion with retained load, launch + magnetic
influence, four-car independence/clearance, interrupted launch/rollback and restart.
Measure actual native mass/inertia, angle/omega, travel speed, engagement, restraints
and seat/body transforms. Pass means load affects physical response, specified
bounds hold, stopping never forces an occupied snap, and every car captures before
unload. The first changed-axis occupied handoff proves camera/mount ownership;
later S2 evidence reuses that path and adds inversion/magnetic/consist differences.

C1–C6 map to these finite claims; no extra spinning configurations are implied.
Content: two distinct original chassis/carrier/restraint assemblies, spin bearing,
matching functional N-contact track definitions, capture station, S2 inducer/damper
attachments and launch-control presentation, car/track/device items and recipes,
S1 and S2 instructions and two buildable examples. Shared textures do not collapse
these separate physics and acceptance contracts.
