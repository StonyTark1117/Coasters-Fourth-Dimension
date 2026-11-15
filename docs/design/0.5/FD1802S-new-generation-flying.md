# FD1802S — New-generation Flying specification

**Specification complete; implementation remains FD1802A–C.** Version 1,
2026-11-14 America/Phoenix. Normative [conventions](specification-conventions.md)
and [shared physical yaw contract](shared-yaw-contract.md).

## Reference versus required game capability

[Vekoma's primary Flying page](https://www.vekoma.com/flying-coasters) describes
seated/sideways presentation, change to prone before launch, and flying inversions.
This supplies inspiration for route-based vehicle-joint changeover on **fixed**
track. The adopted independent scenery-facing yaw in both seated and prone states
is our explicit game capability, not an asserted copy of Vekoma actuator design.
No moving-track tilt, transfer or full three-dimensional aim system is required.

## Geometry, axes and presentation

| Item | Game specification |
| --- | --- |
| Family / variant | `flying / route_changeover_yaw_v1`; dedicated overhead N track, compound sweep, posture-transition bindings and yaw program capability |
| Seats | Two abreast at posture-body local `(±.50,0,0)`; separate body/leg restraints; hip-centered pose |
| Pivots | Coincident ideal yaw and posture pivots at chassis `(0,-3.0,0)`; native intermediate yaw deck plus inner X posture body. Original bearings may be offset only if both transforms and sweep are updated explicitly. |
| Composition | `B*T(P)*Ry(psi)*Rx(phi)*seatLocal`; outer yaw axis chassis Y, inner posture axis yaw-deck X; phi0 seated, phiπ/2 prone |
| Closed rider/restraint bound | Local X=[-1,1], Y=[-1.25,1.50], Z=[-.60,.70] about hip row |
| Combined envelope | Carrier enclosed in sphere radius `sqrt(1²+1.5²+.7²)=1.93391`; reservation half-width2.75, aboveO1.10 (includes bogie), belowO5.25, half-length2.75 (includes forks) |
| Frame | Bogie at measured N contacts, narrow yaw spindle above pivot and fore/aft posture forks beyond Z=±2.25; no brace through the sphere; original overhead supports outside reservation |
| Motion limits | phi0…π/2, |phiDot|≤.25 rad/s, |phiDDot|≤.35; yaw [-8π,+8π], |psiDot|≤.6, |psiDDot|≤.8 as shared contract |
| Consist / route | One to four identical cars, distance8; radius≥16, full bank/inversion, roll geometry≤5°/block; travel≤16, during posture transitions≤2 |
| Dock / transition section | Level dock≥36; authored posture transition length≥20 blocks at speed≤2; downstream guard holds travel if pose/lock has not completed |

The sphere includes simultaneous yaw and posture, not merely their endpoint
boxes. Its top is -1.06609, .36609 below native -.70 underbrace; its bottom is
-4.93391. The rotating yaw-deck forks need the larger 2.75 lateral reservation as well as
longitudinal clearance. Actual frame clearance must additionally test spindle/forks and native
wheel meshes. N contact equality alone is insufficient for this below-track,
full-turn compound mechanism. Reject ordinary/undersized/wrong-capability track.

A seated dark-ride section can use psi=±π/2 for sideways presentation. A prone
section can continue yaw/target tracking. **Prone heading is head-first**, while
seated heading is front-facing; the shared contract defines a continuous heading
through changeover, preserves independent look, and avoids interpreting prone yaw
as roll around the rider's longitudinal axis. Both physical body and camera paths
must reflect this declared pose convention.

## Route operation, locks and authority

`DOCK_SEATED_OPEN → SECURED → SEATED_ROUTE → TO_PRONE → PRONE_ROUTE → TO_SEATED
→ DOCK_CAPTURE → OPEN`, with concurrent shared yaw mode and a fault/hold overlay.
Unlike Conventional Flying, a previously validated route program may change
posture while travelling at≤2. The section transition moves real joints; track
stays fixed. Entry requires closed torso/leg restraints, valid route fingerprint,
clear compound sweep, authoritative yaw mode and supported speed. At the section
exit, phi error≤2° and |phiDot|≤.05 for five ticks acquires the endpoint lock;
travel cannot leave a required posture section until its lock readback is true.

Release a posture lock only for an authorized route transition or safe dock
recovery. A target-facing yaw section may overlap a posture transition; both obey
the combined sweep and separate rate limits. Distant/degenerate/unloaded scenery
uses the shared deterministic fallback, not uncontrolled pitch. A show stop may
yaw while its acknowledged travel hold remains active. It cannot advance route
posture distance or release restraints.

Native Create Chain supplies the standalone lift/drive/launch path and gravity
coast. Same bounded kinetic launch concept as S2 can be used where needed; an
optional provider is never required merely for New-generation posture/yaw.
On drive/authority/body/marker-dependent failure, brake travel and hold the actual
joint poses with bounded damping; do not jump to a route key or seated state.
Explicit recovery revalidates route, bodies, swept clearance, target and locks,
then resumes the current transition or returns through a designated safe section.
Only stopped, seated, yaw-zero/winding-unwound and captured at dock may open.

## Ownership, persistence and implementation seams

Yaw deck and posture carrier are native bodies; seats/riders mount only to the
inner carrier, with no duplicate yaw applied by renderer or camera. The native
mass tracker includes rider load once on that body, so its inertia and parent
reaction change during posture/yaw. Neutral prone head articulation and independent
look use the shared yaw pose convention; no camera-only target lock is accepted.

Persist the shared yaw schema and Conventional Flying's body/leg restraint,
phi/rate, lock and transition state, plus route posture sections, target endpoint,
entry distance/direction and required exit lock. Restart is held until native
hierarchy and route/marker validity are restored; occupied stale program edits
cannot alter the resumed target. Late-join snapshots define the entire hierarchy
before rider transforms. Reversal retraces authored posture/profile distance;
if a reverse boundary requires seated posture, travel is held until its guard passes.

[Native inspection](../../evidence/0.5/specifications/native-interfaces.md) confirms
arbitrary-axis joints and native bodies, but does not claim this hierarchy already
exists. FD1802A extends integrated posture, marker binding and pose contracts only
where this real vehicle needs them, then hands the shared yaw implementation to
FD1901A. First measure solver stability of the two-body joint chain, exact torso
load transform and combined contact clearance; retain the prototype for content.

## Finite acceptance and content

C1–C6 include seated/sideways travel, route-based seated→prone→seated transitions,
occupied target-facing in **both** postures, combined nonzero-yaw changeover,
banked/inverted targeting, station unwind/capture/unload and the shared yaw finite
failure/reversal/show-stop cases. An occupied native prototype measures actual
heading/target error, phi/psi/rates, engagement and seat/body/view transforms;
second-client late join and one mid-transition restart cover the new sync path.
Reuse Conventional Flying's unchanged restraint and lifecycle paths. A working
seated-only target mode or camera effect does not complete the package.

Original content: N overhead track/supports, bogie/yaw deck/posture fork/carrier,
two seats and body/leg restraints, endpoint locks and restrained pose models,
car/track/transition/marker controls and recipes, finite profile/marker editor
labels and projection preview, player guide and a buildable seated dark section →
route changeover → prone target section → seated dock example. There is no general
scripting editor and no requirement for post-1.0 moving-track mechanisms.
