# FD1401 — seated Pipeline specification

**Specification complete; implementation remains FD1401A–C.** Version 1,
2026-11-14 America/Phoenix. Normative [shared conventions](specification-conventions.md).

## Chosen interpretation and evidence

The user's [Pipeline reference](https://coasterpedia.net/wiki/Pipeline_roller_coaster)
identifies cars between rails. Select a seated between-side-rails original design,
inspired by TOGO Ultra Twister geometry. The [primary MLIT investigation summary,
pp.1–2](https://www.mlit.go.jp/common/001281894.pdf) confirms a seated six-person
TOGO vehicle with harnesses, side rails and forward/reverse traversal through
spiral sections. This supports the layout interpretation, not our dimensions or
compliance model. We choose four seats, fixed posture and fixed-track roll sections;
we do not copy the manufacturer's vehicle, route or transfer equipment.
The B&M attraction named Pipeline is not the chosen interpretation. No standing
spring/compliant seats are included. Moving-track tables/tilts stay post-1.0.

## Dimensioned vehicle and contact contract

| Item | Chosen game value |
| --- | --- |
| Family / variant | `pipeline / seated_side_rail_v1`; complete original functional track |
| Rail centers | `P=(±1.50,.325)` in XY; same .13125 × .075 rail half-section as N |
| Wheel contacts | Run above each rail at Y=.600, upstop at Y=.050, radius .20; .55 vertical wheel-row spacing. Side rollers radius .125 touch outer rail faces at X=±1.75625, Y=.325. Wheel rotation axis X for running/upstop, Y for side rollers. |
| Native guide | Bearing at O+.3125Y; original visual/contact assembly replaces native N wheel slice for this family only; native centerline guide unchanged |
| Chassis/cabin | Half-length 2.5; fixed floor Y=-2.25; two rows at Z=±1.25; seat centers X=±.60, Y=-1.70 |
| Seat/rider bound | Each seat's occupied/restraint bound half-width .40, Y=[-2.15,.10], Z=row±.70; four original harness seats; no seat compliance |
| Envelope | Half-width 2.05, aboveO 1.0, belowO 2.50, half-length 2.75; supports stay outside it except declared running contacts |
| Coupling | One or two identical cars, center distance 8; no mixed train. Forward/reverse on the same fixed route; no end teleport. |
| Qualified design route | Curvature radius≥16; roll change≤5°/block; speed≤12; flat dock length≥20 for two cars; level loading opening alongside each row |

The rider bound ends at |X|=1.0. Inner rail face is 1.36875: lateral gap .36875
before support geometry, exceeding .25. Body and harness remain below the rail
centers; the side rails do not cross the specified head/eye region. The central
native bearing is between rows, not inside a seat. Underfloor tie members avoid
the same bound. Open harnesses may exceed the travel envelope only in the loaded
station dock with travel held; dispatch requires closed harness geometry.

Pipeline deliberately cannot use N ordinary track: gauge 3.0 differs from 1.544.
A cosmetic style cannot grant P eligibility. A P-compatible provider must expose
all six retention contact directions, the original wheel ownership, envelope,
lift/brake capability and station alignment. Fixed seat pitch/roll comes from
chassis track frame; there is no rotating carrier or separate seat capture lock.

## Operating states and authority

`DOCKED_OPEN → CLOSED_READY → TRAVEL → BRAKING → DOCKED_CLOSED → OPEN`.
Dispatch requires all four/eight seat restraints, a validated connected route,
complete consist and cleared station opening. One native Create-powered Chain
provider owns travel; gravity handles coast and the shared brake handles stopping.
A track roll changes the whole vehicle, not a passenger joint. Reverse is permitted
only after stop and validated direction authorization; use the same rail identity.
A dedicated fixed return curve replaces any real-world moving transfer device.

Power/controller loss removes drive and brakes; loss of a coupler or route/body
faults the whole train and retains restraints. Save/restart restores the actual
chassis orientation, all seat assignments and stopped state. Explicit revalidation
and resume return it to the dock; only docked, stopped, complete alignment opens
harnesses. Native mount owns rider pose, with shared fixed-seat camera behavior.
Propulsion, restraint and rendering ownership follow the shared conventions.

## Native feasibility and required interfaces

[Inspected native inputs](../../evidence/0.5/specifications/native-interfaces.md)
show a centerline guide independent of visible wheel gauge and fixed Bezier/roll
frames, so the selected fixed-track Pipeline is representable. Required additions
are the P contact descriptor, original wheel rendering/ownership, fixed seated
mount offset, original family eligibility and 8-block consist rule. These belong
to FD1401A; adding only a texture or leaving ordinary wheels visible is insufficient.
FD1101A fixed-seat/renderer contracts are reused. No Axis mechanism is required.
Persist shared fixed-seat schema plus P contact version, coupling distance and
dock/route binding; there is no meaningless carrier spin angle.

## Finite prototype/acceptance handoff

FD1401A starts with original functional geometry, a four-seat car and one two-car
example. On a fixed straight → bank/roll → fixed return → dock route, measure
native engagement, side/run/upstop visual contact, default rider/head clearance
and link retention at the declared spacing. Include forward/reverse and one
interrupted stop/restart. Pass if contacts stay registered, occupied geometry stays
within reserved envelope, identities survive and dock-only unload holds; preserve
failed condition before any repeated launch. This is the required occupied
clearance/station prototype plan, not a claim it already ran.

C1–C6 apply with these distinct claims: P versus N rejection; rail/rider occlusion;
fixed whole-body roll; two-car spacing; safe forward/reverse dock return. Content:
original side-rail section/supports, running/upstop/side wheel set, low chassis,
four seats/harnesses, restrained/open variants, car/track items and recipes,
station guide and a player-buildable fixed-track Pipeline example. No new
compliance actuator, transfer-table asset or stand-up variant is hidden in scope.
