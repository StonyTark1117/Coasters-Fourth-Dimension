# FD1302 — Axis specification

**Specification complete; implementation remains FD1302A–C.** Version 1,
2026-11-14 America/Phoenix. Normative [shared conventions](specification-conventions.md).

## Axis decision and reference boundary

The requested [Axis reference](https://coasterpedia.net/wiki/Axis_Coaster_(S%26S_Worldwide_product))
and [S&S's indexed primary product summary](https://www.s-s.com/ride-pages/axis-coaster)
identify the new-axis rotating-carrier concept. The product URL returned 404 on
inspection; [S&S's current primary history](https://www.s-s.com/) still identifies
the AXIS prototype. This establishes inspiration, not dimensions or a manufacturer
actuator specification. The exact mechanism below is an explicit game design:
**rotation about local longitudinal Z**, parallel to canonical travel, with a
single outboard two-seat group. This is roll about a horizontal axis on level
track, not local-Y spinning, transverse 4D pitch, or a yaw-only cabin animation.

## Dimensioned physical contract

| Item | Original game specification |
| --- | --- |
| Variant / track | `axis / longitudinal_roll_v1`; dedicated N contacts, outboard full-roll envelope, capture and damp/drive attachment |
| Pivot / axis | `(4.0,1.5,0)`; unit `(0,0,1)` in chassis frame |
| Seats | Two seats side by side about the pivot at `(3.5,.10,0)` and `(4.5,.10,0)`, both face +Z; harness/foot support |
| Carrier sweep | Radius 2.25 around Z, half-length 1.25; reservation X=[1.50,6.50], Y=[-1.0,4.0], Z=[-1.50,1.50] |
| Whole-vehicle envelope | half-width 6.50, aboveO 4.0, belowO 1.0, half-length 2.0; asymmetric actual occupied volume remains explicit |
| Frame | Side outrigger connects at Z=±1.75, outside rotating pod ends; joint bearings lie on longitudinal axis. No cross-member crosses the passenger disk. |
| Load | Native body inertia plus .8/unit per rider; 1 unit game ballast at carrier-relative `(0,-1,0)`; actual first moment/inertia measured in first prototype |
| Range / modes | Continuous unwrapped turns; FREE, DAMP, bounded DRIVE and CAPTURE; no hard ±45° swing stop |
| Limits | `c/I=.4 s⁻¹`; overspeed damping at 2 rad/s; travel fault at 3; driven request ≤1 rad/s and .75 rad/s² |
| Consist / route | One or two identical cars at distance 8; radius≥16; full native bank/roll, geometry roll-rate≤5°/block; travel≤12 |

The near edge of the conservative carrier reservation is X=1.50; native running
gear outboard bound 1.12 leaves .38. Reserve supports away from the whole rotation
volume and outrigger. The deliberately large asymmetric width is a game clearance
choice; mirrored skins cannot alter mass/pivot or create another configuration.
Native link distance is possible but current family restrictions need extending;
use complete native coupled sweeps rather than bounding straight poses alone.

## Motion, operation and failures

`DOCK_OPEN → SECURED_CAPTURED → DISPATCH → FREE/DAMP/DRIVE → TRAVEL_STOP →
ALIGN → CAPTURED → OPEN`. FREE uses native gravity, inertia and declared loading;
DAMP removes relative angular energy. DRIVE is an explicitly authored game mode
for a finite route section with rate/acceleration bounds, then smoothly hands back
to FREE or DAMP. It is not claimed as the reference product's control algorithm.
Changing mode while occupied is only by an already validated route program; the
player cannot edit a moving occupied car. Driven targets use an unwrapped profile;
reversal retraces canonical distance. This mode has no scenery target binding.

Native Create-powered Chain owns travel. Carrier torque cannot launch or brake
longitudinal motion. Station capture targets upright relative chassis (0 mod 2π)
through the common bounded servo. Restraints open only stopped at the level dock,
with every carrier captured. Lift capture/release is an authored constant-relative
orientation lock, distinct from Suspended's bounded lateral swing mechanism.

Power loss changes DRIVE to DAMP and brakes travel; it does not release restraints
or jump to capture. Rollback retains the same axis and actual angle/omega. A failed
capture remains stopped and latched, exposes the unmet guard and permits explicit
bounded re-alignment after support/authority validation. Restart restores phase,
mode/profile revision, unwrapped angle, relative omega and joint identity before
accepting a new command. Common persistence/sync adds free/damp/drive transition
progress and the outboard mount definition. One seat/body/camera owner composes
physical longitudinal roll once; player look remains independent.

## Required seams and falsifiable prototype plan

[Native interface inspection](../../evidence/0.5/specifications/native-interfaces.md)
finds arbitrary local rotary normals and limits/motors. Axis can use the shared
axis-aware support delivered by S1 and bounded-mode lessons from Suspended, but
not its ±45° dynamics. Required interfaces: Z-axis angle extraction/restore,
outboard native load/inertia, route mode transition, same-family 8-block links,
full-sweep eligibility and capture. No native primitive is inaccessible; off-axis
stability and coupled clearance remain distinct FD1302A prototype claims.

Prototype one then two occupied cars through a banked curve with an asymmetric
rider load, one continuous turn, a bounded driven-to-free transition and stop/
capture. Measure rail engagement, parent/child transforms, native mass/inertia,
relative angle/omega, coupling distance and rider/camera composition. Interrupt
power during DRIVE and reverse through the same profile after explicit recovery.
Fail if the wrong axis rotates, loading is merely cosmetic, a frame/rider intersects,
roll wraps on save, drive bypasses restraint authority or capture snaps a rider.
Reuse the existing client lifecycle proof except new longitudinal roll paths.

C1–C6 plus those claims are the finite acceptance plan. Content: original N bogie,
outboard fork/frame, longitudinal bearing and two-seat carrier/harness, contact/
clearance track, damping/drive controls, car/track/control items and recipes,
visible modes, operating guide and one buildable banked/inverting example.
