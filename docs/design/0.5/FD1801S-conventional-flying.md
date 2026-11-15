# FD1801S — Conventional Flying specification

**Specification complete; implementation remains FD1801A–C.** Version 1,
2026-11-14 America/Phoenix. Normative [shared conventions](specification-conventions.md).

## Reference and chosen posture mechanism

[B&M's primary Flying page](https://www.bolliger-mabillard.com/flying-coaster)
supports a below-track face-down flying posture. This package selects an original
station-changeover implementation: passengers board seated, close body/leg
restraints, rotate to prone at the stopped dock, ride with that joint locked and
return seated before unloading. The precise hinge, seat count, rate and lock
logic below are game choices; the source is not an engineering drawing.

| Item | Game specification |
| --- | --- |
| Family / variant | `flying / station_changeover_v1`; dedicated overhead N contacts, posture sweep and station changeover capability |
| Seats | Four abreast at carrier coordinates X=`-1.5,-.5,.5,1.5`; hip pivot row Y=Z=0 |
| Hinge | Chassis `(0,-2.50,0)`, local X; pitch `phi=0` seated, `phi=+π/2` prone |
| Transform | `world = B * T(0,-2.50,0) * Rx(phi) * seatLocal`; B is the conventions' graph-origin design transform, converted once from native axes/bearing origin |
| Closed occupied carrier box | X=[-2,2], Y=[-1.25,1.50], Z=[-.60,.70] relative to hip pivot; includes body/leg restraints and foot support |
| Sweep reservation | half-width2.25, aboveO1.10, belowO4.50, half-length2.0; hanger forks at Z=±2.25 outside moving row sweep; whole frame half-length2.50 |
| Transition limits | 0≤phi≤π/2, |phiDot|≤.25 rad/s, |phiDDot|≤.35 rad/s²; mechanical endpoint locks; no free-motion mode |
| Consist / route | One to four identical cars at distance8; radius≥16, full native bank/inversions, roll-rate geometry≤5°/block, speed≤16; level dock≥36 |

The occupied YZ corner radius is at most `sqrt(1.5²+.7²)=1.6553`, so bottom
sweep stays above -4.1553. During the positive 90° transition, highest point is
`-2.5+sqrt(1.5²+.6²)=-.88445`, leaving only .18445 below the -.70 native underbrace.
**Therefore the station/Flying bogie must relocate its lower brace to Y=-.55 or
above**, leaving .33445, and verify the original brace is absent. This is a
required original geometry change, not a waiver of the .25 clearance. N wheel
contacts stay fixed. The new frame's reserved half-length is2.50, including forks.

## Restraints, locks and interrupted changeover

`SEATED_OPEN → SEATED_SECURED → TO_PRONE → PRONE_LOCKED → RUNNING → DOCK_PRONE
→ TO_SEATED → SEATED_LOCKED → OPEN`.

Both torso and lower-leg retention close before TO_PRONE. The entire consist is
held at the level dock; opening platforms clear the continuous transition sweep.
Each endpoint lock requires error≤2°, |phiDot|≤.05 and five valid ticks. Dispatch
requires every car's PRONE_LOCKED state and clear route; normal travel cannot
change posture. Return stops every car before unlocking the prone joint for the
bounded seated transition. Only SEATED_LOCKED at the dock permits restraint open
and Shift unload. A fixed track inversion does not alter phi or release the lock.

Power loss during a transition holds travel and physically brakes the joint at
its current phi, retaining restraints. Keep `INTERRUPTED_TRANSITION` plus actual
pose/rate, not a false seated/prone endpoint. Recovery revalidates dock, bodies,
clearance and authority and offers explicit Resume-to-target or Return-to-seated;
either moves through the same bounded path. A jam/time-out exposes a fault and
retains current pose. No occupied teleport or automatic harness release is allowed.
A power failure during PRONE_LOCKED travel keeps its latch and invokes travel
braking. Native Chain supplies lift/drive; posture motors cannot provide travel.

## Loading, camera and saved state

Native Sable body/joint owns posture; equal .8-unit rider masses follow actual
seat-relative torso points through the physical body. Do not keep the old seated
`seat.above()` point in the chassis frame. The same posture transform moves seat,
body model, restraint and eye origin. The original prone rider pose has its body
long axis head-first along +Z and a declared neutral head pose looking forward;
head-rest pitch relative to the tilted torso is `-phi`. This is a model/eye-frame
choice, not a second carrier rotation. Independent player look composes after
that neutral head frame; comfort changes presentation only. Verify the physical
body still becomes prone even though its neutral gaze can remain forward.

Common schema adds phi/phiDot, endpoint lock, transition target/direction/progress,
interruption reason and separate body/leg restraint bits. Snapshot at the same
physical instant. Restart restores that state before joint drive; late join receives
the hinge hierarchy before rider mounting. Removal/death/dismount restores normal
camera and clears assignment, including during an interrupted transition.

## Required interfaces and finite handoff

Reuse Fixed Inverted's overhead contracts and the integrated seat/restraint/camera
interfaces. [Native API inspection](../../evidence/0.5/specifications/native-interfaces.md)
provides an X rotary joint, bounded motor/limits and saved-body primitives. New
work is the authored posture/eye/body/torso transform, two endpoint locks, brace
clearance and dock-only changeover; this does not require moving track.

Finite C1–C6 plan: ordinary seated boarding, both restraints, complete station
changeover, occupied prone bank/inversion, return seated/open/unload; native swept
brace/fork/rider clearance and four-car spacing; interruption mid-changeover with
explicit bounded recovery; restore one mid-transition save and final camera state.
Use the early occupied mechanics handoff for later player acceptance. Reject
missing posture capability, wrong track or an attempted in-route changeover.
New-generation route changeover/yaw is outside this variant, not removed from 1.0.

Content: original overhead track and clear brace/forks, four-seat posture carrier,
body/leg restraints and endpoint locks, seated/prone/transitional rider presentation,
car/track/control items and recipes, readable posture/fault controls, station
changeover guide and one player-buildable flying example.
