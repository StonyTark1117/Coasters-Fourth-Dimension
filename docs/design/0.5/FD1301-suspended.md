# FD1301 — Suspended specification

**Specification complete; implementation remains FD1301A–C.** Version 1,
2026-11-14 America/Phoenix. Normative [shared conventions](specification-conventions.md).

## Reference, seating and overhead fit

[Kings Island's primary description of The Bat](https://www.sixflags.com/kingsisland/attractions/the-bat)
confirms Arrow's below-track cars swing laterally in bends. The requested
[discovery reference](https://coasterpedia.net/wiki/Suspended_Coaster_(Arrow_Dynamics_product))
is retained for identity. The chosen two-seat cabin, ±45° limits and lift locks
are game specifications; no unverified manufacturer pivot/damper measurement is
asserted. A new track supports the existing measured N contacts while moving
structural spine/supports above the rail or outside the cabin sweep.

| Item | Original game specification |
| --- | --- |
| Variant | `suspended / lateral_swing_v1`; overhead N contacts plus swing-clearance, lift-lock and dock-capture capabilities |
| Pivot / axis | `(0,-1.50,0)`, local longitudinal Z; cabin hangs below pivot |
| Seats | Two forward-facing seats at `(±.50,-3.25,0)` with harness, floor and footwell |
| Closed cabin/rider box | X=[-1,1], Y=[-4.25,-1.75], Z=[-1.25,1.25]; all rider/harness surfaces in this box |
| Sweep | Joint ±45°; conservative whole envelope half-width 3.0, aboveO 1.10, belowO 4.70, half-length 1.75 |
| Hanger | Fore/aft forks outside pod at Z=±1.50; connect to central bogie without crossing swept cabin; joint interfaces are intentional contacts |
| Dynamics | FREE gravity/inertia; `c/I=.65 s⁻¹`, soft damping begins at |angle|=35°, hard physical joint limit45°; restitution zero; relative speed guard1.5 rad/s |
| Load | Native empty body + .8/unit per rider; 1-unit centered keel ballast at carrier-relative `(0,-2,0)`; no second pendulum solver |
| Consist / route | One to four identical cars, distance8; radius≥16, bank≤30°, slope≤60°, speed≤12; no inversions |
| Dock | Level track, lateral platforms at floor height; minimum length36 for four cars; complete cleared envelope before drive |

At maximum swing the highest cabin corner is
`-1.5 + (1-.25)/sqrt(2) = -.96967`; the inspected native underbrace is at -.70,
leaving .26967 before added decorative geometry. The lowest radial bound is
`-1.5-sqrt(1²+2.75²)=-4.4262`, within the -4.70 reservation. The lateral bound at
45° is 2.6517, within3.0. These are analytic design bounds, not rendered measurement.
Native contact clearance still must include the actual fork and wheel mesh.
Ordinary N rail fit does not provide overhead sweep or lift locks; reject it.

## Lift-lock and station state machine

`DOCK_OPEN → RESTRAINED_LOCKED → LIFT_LOCKED → RELEASE_ARMED → FREE_SWING →
DAMP_APPROACH → STOPPED_ALIGN → DOCK_LOCKED → OPEN`.

Lift entry first reduces swing; engage the physical relative-angle lock only
when |angle|≤2°, |omega|≤.05 and travel≤.5, inside the validated lock-entry section,
for five ticks. Otherwise stop before entering the lift. Once latched at zero,
constant-relative cabin orientation follows the pitched chassis; the cabin is not
magically world-level. The lock remains engaged on the lift at speed≤2. Release
only when all bogies in the consist have cleared the designated lift-exit boundary,
with current route fingerprint, closed restraints and no competing capture request.
A moving exit release does not require the stationary station latch guard.

Free swing uses native loading/acceleration. The soft stop dissipates energy and
hard limit prevents motion beyond45°, including asymmetric loading. Approach
DAMP cannot open restraints. Stop travel, align with the bounded servo, latch and
then open only at the level dock. A failed alignment retains restraints and stop
and reports angle/speed, rather than forcing a teleport or letting riders unload.

Power loss while lift-locked retains the latch and applies travel stop. An interrupted
lock-entry request that has not satisfied guards remains unlocked/damped and stopped.
On restart, a saved latch is retained only after native joint/support identity is
restored; a pending latch is revalidated. Unexpected rollback before lift release
stays locked; rollback in FREE stays bounded/damped until safely stopped. Missing
body/coupler/route faults the entire train. Explicit recovery returns to a matching
dock; ordinary towing is permitted only after a separately qualified secured
adapter proves the overhead envelope, never from N contact equality alone.

## Ownership, persistence, interfaces and finite handoff

Native Create-powered Chain owns lift/travel, shared passive braking owns stopping,
and the rotary joint owns lateral motion. Server transitions own the lock and
restraints; one native seat/body hierarchy owns rider/camera pose. Common schema
adds lock phase/reason, lift-entry/exit bindings, hard/soft limit definition and
pending-release epoch. Reload and late join keep exact angle/omega and original
seat assignments; no default upright reset.

[Native review](../../evidence/0.5/specifications/native-interfaces.md) establishes
Z-axis joint/limit/motor and explicit link-distance capability. Use FD1501A's
integrated axis measurement/save support; add bounded swing/lock semantics here.
Required original track changes move support structure outside the under-track
sweep while retaining N wheel contacts. This overhead interface feeds Fixed Inverted;
it must not export free swing as an unavoidable seating behavior.

Finite native/occupied prototype: one and four-car train on a lift plus banked
curve, empty/balanced/asymmetric declared loads, then damp/stop/capture. Measure
actual mass/inertia, joint extrema, lock guards, frame/pod/rider and adjacent-car
sweeps. Interrupt once during lock acquisition and once while latched; demonstrate
denied unsafe latch, retained lock on power loss, correct explicit release and
normal dock unload. Reuse common save/reconnect paths; add saved pending-latch and
limit-state restoration. C1–C6 plus these limit/failure claims are the complete
acceptance plan, not a request to execute FD1301A in 0.5.

Content: original overhead track/spine/supports, bogie, fork/hanger/cabin, two
harness seats, visible lift/dock latch, damp indicator, original car/track/latch
items and recipes, clear lift-entry/exit instructions and one buildable curved
lift example. No full-turn roll or world-leveling animation substitutes for swing.
