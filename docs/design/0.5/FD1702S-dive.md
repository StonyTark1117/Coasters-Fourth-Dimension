# FD1702S — Dive specification

**Specification complete; implementation remains FD1702A–C.** Version 1,
2026-11-14 America/Phoenix. Normative [shared conventions](specification-conventions.md).

## Reference, vehicle and track

[B&M's primary Dive page](https://www.bolliger-mabillard.com/dive-coaster) describes
a pause before a vertical/beyond-vertical plunge and 6/7/8/10-wide floorless rows.
Choose one original **six-abreast** fixed row, one to three linked cars. Geometry,
limits and controller timing below are game design, not manufacturer measurements.

| Item | Specification |
| --- | --- |
| Family / variant | `dive / six_abreast_v1`, dedicated N-contact track with wide fixed-seat clearance and hold-capture section |
| Seats | X=`-2.5,-1.5,-.5,.5,1.5,2.5`, Y=1.50, Z=0, face+Z; individual harnesses/foot support |
| Closed occupied row | X=[-3,3], Y=[1.10,3.40], Z=[-1,1]; fixed to chassis, no rotary seat |
| Envelope | half-width3.25, aboveO3.65, belowO.95, half-length1.75 |
| Consist / route | One to three identical cars, center distance8; radius≥16, native full bank/inversions, roll-rate geometry≤5°/block; travel≤16 |
| Hold section | Authored fixed crest/drop approach; lead-car hold pose aimed 60° downward; complete-consist envelope clear; actual rail engagement maintained |
| Approach / hold | Approach≤2 blocks/s; hold acquired only |v|≤.05 for five ticks with braking authority; dwell default60 ticks, editable20–200 ticks while empty/stopped |
| Dock | Level, minimum28 blocks for three cars; opening platforms clear before motion |

Ordinary N contact equality does not prove this wide row or hold interface.
The full static hold pose includes rear cars on their own graph frames; never
rotate the whole consist around the front bearing as a visual effect. A route
with too little stopping distance or unavailable exit is ineligible for release.

## Hold/release authority and interruption

`DOCK_OPEN → SECURED → RUNNING → APPROACH → HOLD_ACQUIRING → HELD → RELEASE_ARMED
→ DROP → BRAKING → DOCK_CLOSED → OPEN`.

FD1701A is the single operations authority. The native Chain lift supplies uphill
travel, passive braking acquires hold, and gravity starts the drop after release.
A released hold does not itself add launch velocity. Enter APPROACH only for the
bound section with all cars supported/restraints closed. HELD starts the dwell
clock only after the whole-consist speed and physical hold readback satisfy guards.
The server uses elapsed simulation ticks; a pause cannot expire the dwell off-line.

On expiry, release requires valid authority epoch, route/exit binding, complete
train, closed restraints and no emergency/provider fault. A request is idempotent;
replayed button/redstone packets do not release twice. An occupied edit cannot
change dwell or bypass the hold. Clear exit means this ride's declared route and
local hold predicate, not the later FD1701B/C park-wide multi-train manager.

Loss of power/controller during acquisition retains braking and closes the release
gate. Loss during HELD freezes dwell/release progress and retains the physical
hold. After reload or restored power, require explicit Resume and revalidation;
never release because wall-clock time passed. If the car has already physically
left the hold, persist `DROP` and brake safely rather than teleporting it back.
Use a server commit of phase/epoch with the release action so stale commands cannot
re-enact the transition. Missing bodies, support or a link stops the surviving
consist and retains restraints. Only a verified level dock permits open/unload.

## Seat, camera, data and implementation handoff

Fixed-seat ownership comes from FD1101A; no posture actuator or rotor state is
needed. Riders follow native chassis pitch through the edge/drop once, preserving
independent look and comfort. Common schema adds hold section/fingerprint, approach
status, held/released phase, remaining dwell, validated exit predicate and command
epoch. Late join observes held versus released consistently; restore physical
pose before consuming transition state.

[Native review](../../evidence/0.5/specifications/native-interfaces.md) confirms fixed
banked curve and native propulsion/link primitives. The missing behavior is the
specified FD1701A hold/release authority and this vehicle's geometry, not an
optional plugin. Physical hold must actually constrain/brake the complete train;
a GUI timer alone fails the mechanism criterion.

Finite C1–C6 handoff: normal six-seat construction/boarding/dispatch, controlled
approach, measured held zero-motion interval, validated timed release and occupied
drop, return/unload; three-car wide-row/crest sweep; reject invalid exit/support or
open restraint; one interruption before release and one restore after the release
commit. Reuse fixed-seat/net/camera and generic lifecycle proofs; add only these
authority/timing distinctions. No broader multi-train dependency or new full
profile sweep is introduced.

Original content: wide fixed chassis and six-seat row, harnesses/foot support,
N-contact wide-clearance track and crest hold presentation, hold/release controls
and readable countdown/fault, car/track/control items and recipes, operator guide
and one buildable lift/hold/drop/return example.
