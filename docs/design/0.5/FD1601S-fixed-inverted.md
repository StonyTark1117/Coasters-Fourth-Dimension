# FD1601S — Fixed Inverted specification

**Specification complete; implementation remains FD1601A–C.** Version 1,
2026-11-14 America/Phoenix. Normative [shared conventions](specification-conventions.md).

## Reference and configuration

[B&M's primary Inverted description](https://www.bolliger-mabillard.com/inverted-coaster)
supports below-track, feet-free riding through inversions and banked turns.
The selected four-abreast row, dimensions and limits are original game choices.
Reuse Suspended's overhead running-gear interface with **fixed** seat orientation
relative to chassis; there is no swing joint, damper or independent roll.

| Item | Game specification |
| --- | --- |
| Variant / gear | `fixed_inverted / four_abreast_v1`; dedicated overhead N-contact track and fixed-seat envelope |
| Seat locations | X=`-1.50,-.50,.50,1.50`, Y=-3.25, Z=0; all face canonical+Z; four individual harnesses/foot supports |
| Occupied closed bound | X=[-2,2], Y=[-4.25,-1.50], Z=[-1,1]; hanger outside it except seat mount contacts |
| Whole envelope | half-width2.25, aboveO1.10, belowO4.50, half-length1.75 |
| Bogie / hanger | Measured N wheels; overhead spine and two outer hangers connect below running gear to row; no stock under-track decorative spine through rider heads |
| Consist | One to four identical cars, center distance8; no mixed-family linking |
| Route | Radius≥16; complete native bank/inversions; geometric roll rate≤5°/block; speed≤16; level dock≥36 blocks for four cars |
| Loading | Native assembled mass + equal .8/unit per occupant at fixed torso points; no separate rotary inertia/state |

The head/harness bound ends Y=-1.50, at least .80 below the inspected -.70 native
underbrace. The specified row/body stays within its reserved envelope; the later
native sweep must check the actual rail, hanger, banked curves and whole consist.
Contact equality with ordinary rail does not make it overhead-seat compatible.
Functional providers declare overhead mount/envelope/capture, regardless of style.

## Operation and failure contract

`DOCK_OPEN → CLOSED_READY → RUNNING → BRAKING → DOCK_CLOSED → OPEN`.
All occupied seats must be restrained before dispatch; the entire consist must
have matching support, a valid route and a clear dock. A stationary train is not
necessarily at an unload dock. Fixed seats require no alignment motor: dock
capture means stopped chassis at its declared level loading geometry and retained
native support. Native Create Chain supplies lift/drive; gravity supplies coast;
shared braking stops the whole train.

Power/controller loss holds travel and restraints. Coupler/body/support loss faults
the train; do not let front-car status open rear restraints. Interrupted inverse
pose is saved as the chassis physical pose, not reset to upright. Explicit recovery
revalidates support/consist and returns to the dock, with no automatic release or
unsupported native-bearing tow claim. The same native seat owner moves riders
with chassis and the fixed offset once. Relative look/comfort and camera restoration
reuse FD1101A's integrated fixed-seat path.

Persist common fixed-seat identity, offsets, assignments, restraints, dock/support
and travel state. There is no unused rotor angle or fake captured-body UUID.
Late join/reconnect reconstructs the below-track seat assignment under the same
chassis; death/permanent removal cleans mounts exactly once.

## Interfaces, acceptance and content

Use FD1101A's fixed-seat contract and FD1301A's qualified overhead geometry; only
family definitions, seat/hanger arrangement, envelope and consist bounds differ.
[Measured native inputs](../../evidence/0.5/specifications/native-interfaces.md)
supply N contacts, basis and link-distance primitive. The existing add-on's
Arrow/PAX-only consist restriction needs explicit extension for this configuration.
No new free-motion prototype is necessary to specify a fixed mechanism.

Finite C1–C6 handoff: prove the below-track row stays rigid through occupied
bank/inversion; four-car and rail/hanger/rider clearance; correct fixed-seat
capture/unload; one save/reopen in non-upright pose; reject missing overhead sweep
or ordinary/wrong track. Reuse unchanged fixed-seat mount/camera/network proof
from Wing and overhead contact proof from Suspended. Do not rerun their full
matrices merely to add this family label.

Original content: overhead track/support style, N bogie/hangers, four-seat row,
open/closed harnesses and foot supports, car/track items and recipes, below-track
boarding instructions, fixed-seat labels and one buildable lift/inversion example.
This is a complete pre-1.0 vehicle, without inheriting free lateral swing.
