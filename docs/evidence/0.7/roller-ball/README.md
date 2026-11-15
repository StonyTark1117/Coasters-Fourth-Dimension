# Roller Ball — completed FD1502A–C

**Current status: complete, 2026-11-15.** [Mechanics and occupied handoff](A-handoff.md)
and [content/player acceptance](BC-acceptance.md) reconcile all required cases,
including the actual summit restart and targeted shared look-restoration fix.
All owned client/server/Xvfb units are inactive; saved worlds and traces remain
on persistent storage. The following chronology preserves the original failures
and bounded observations; historical next actions are not current assignments.

## Historical implementation checkpoints

Owner `01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary `main`, 2026-11-15.
FD1502A is in progress. Axis A–C is integrated at0a49c089; its shared native
carrier/COM conventions are consumed without replaying Axis.

The first four-seat transverse body, retained guide/phase state, matching track,
lift section tool, original functional assets and fixed vertical route are authored.
Coherent compile03 passes on Java21. These are implementation inputs, not acceptance.

Persistent run root `/mnt/NVME4/fourthdimension-runs/fd1502-roller-ball-20261115`.
The build's `runs/gametest` is a symlink to the selected native world outside build.
No graphical client/display has been started for Roller Ball.

Native01 (namespace fourthdimension_fd1502 only) found two concrete failures:

- Four-seat native hinge and .8/3.2 declared mass checks passed, with empty mass11,
  actual inertia12.34917 and hinge gaps .000128–.000172. The state roundtrip then
  rejected seat(-4,0,-1), whose squared offset17 exceeds the old generic16 bound.
  Permit17 only for the Roller Ball family; exact family geometry validation remains.
- Route edge20's nominal radius8 rounded corner fell below the required native
  radius. Enlarged the final two bends to radius12 by changing their endpoints;
  no minimum-radius criterion was relaxed.

Both failures are preserved in native01.log and native01/world. Server saved and
exited. Next native02 checks these corrected conditions; no closed family reopens.

Native02 retained the same passing physical hinge/load measurements, but the save
read also needed the declared child-pivot offset1 (the older generic bound was0).
The whole guard was inspected before retry. The bend change also moved the
failure to the preceding radius12 corner. After two failures, inspected saved NBT
and pinned Create bytecode: vertical projected tangent intersection is null, so
Bezier handles use chord/3. [Calculation](native-handle-diagnosis.json) gives
minimum endpoint radius7.567 for nominal12, versus10.090 for nominal16. Both
final corners are now16 and bottom return moved down8 blocks. Native03 rechecks
these named changes, retaining the required radius8 and exact Roller geometry.

Native03 passed both cases: all22 route edges satisfy fixed vertical-plane/radius
eligibility; one native carrier has four seats, mass11→11.8→14.2, hinge gap
.000128–.000172 and exact serialized body/angle/lift phase. [Raw pass](native/geometry-load-pass.log).
Server saved all dimensions and exited. This closes allocation/load/geometry,
not moving lift/zigzag/recovery or occupied client acceptance. Next is native
whole-route travel with the distinct lift interruption and return capture.

Route01 stopped just after dispatch because the platform began exactly at the
conservative lateral sweep boundary; subpixel native position drift turned that
contact into overlap. Moved both side platforms one block outwards (nearest
platform blocks±6 relative anchor). Required sweep remains unchanged; reachable
normal seats will be checked in the occupied run. Saved route01 remains retained.

Route02 cleared the platform but stopped before the first curve: saved rails show
curved sections invalidated by fingerprint mismatch. Unlike the accepted Axis
example, Roller recorded fingerprints while still constructing adjoining edges.
Moved binding after the entire native loop construction and added a delayed
stored-graph validity/fingerprint assertion before dispatch. This specifically
reopens persistent route geometry; the native four-seat hinge/load pass stands.
Route02 saved all dimensions and exited; its world and log are retained.

Route03 confirms all22 stored native fingerprints after the two-pass correction,
but timed out: ±2π Generic X limits held the carrier nearzero despite guidecommands.
Route04's ±π limits freed the axis; dock alignment then exposed the motor tracking error. Reused the accepted Flying higher-gain motor convention (4000/400, gravity divided by actual I). Both saved/exited.
Also corrected a geometry review finding: seat hips and torso COM were half a block
forward of the specified ±.5 longitudinal pair. Mesh, selection, rider and native
mass proxy now all shift Z+.5; axle/struts connect visibly. The changed COM requires
the affected stationary load/hinge check again, without reopening other families.

Route05 reclosed the changed load/hinge case: mass11→14.2, inertia11.34917→12.98317,
gaps .000124–.000165. Guide follows q=-pitch and the vertical latch engages.
Power cut correctly retained latch/restraints but the shared 6-block/s² brake
left downhill creep on the vertical rail. Added an actual native world constraint
at the measured chassis pose during stopped retention; explicit Resume removes
it. An interrupted nonlatched guide now brakes its bounded reference to a hold.
Departure DOCK stays guided until the free boundary instead of tumbling early.

Route06 passed the power stop/resume and summit guide, then exceeded descent8.
Applying the parent velocity correction equally to both masses (route07/08)
exposed a control error: the native Chain temporarily accelerates the light bogie
before hinge solving, so its individual pre-solve velocity over-braked the whole
assembly and stalled lift entry. After those two attempts, inspected the pipeline
velocity API and changed the measured quantity to combined mass-weighted momentum
along the track. The correction remains a passive counter-impulse to both bodies.
Route07's fixed-tick angle assertion also assumed an arrival time; removed that
test-only assumption while preserving the finite whole-route timeout.
All runs saved/exited. Native load and stored route binding evidence remain valid.

Route09/10 reached native free tumble; overspeed damping needed more authority
above2rad/s and bend transients exceeded8. Updated damping20 above2 and passive
translation target6.5 (required ceiling8 unchanged). The ±π limit also stopped
full turns. Route11 tried axis-lock toggling; source diagnosis confirms
[Rapier lock_axes is additive](https://docs.rs/rapier3d/latest/rapier3d/dynamics/struct.GenericJoint.html#method.lock_axes),
so omitting X cannot unlock it. Replace only the joint on latch transitions,
with the same anchors and measured latch frame, preserving body poses/velocities.
The released joint now has no X limit. Route11 saved/exited; no client launched.

Route12 passed full native travel, power-loss hold/explicit Resume, all zigzags,
full unwrapped motion and return dock capture: speed peak7.65833, relative angle
−1.56057..6.28318, same native rider/body throughout8738ticks. Required full-turn
release now works without a residual axis limit. Actual guide rate .29365 passes,
but sampled acceleration2.65869 exceeds.5, so only that guide acceleration case
remains open; the whole-route evidence is retained. A separate short guide
namespace measures the offending interval instead of repeating the free route.
Server saved/exited; raw [route pass](native/route-pass.log).

Guide01 isolated periodic latch chatter: native snapped chassis angular velocity
can be zero while its route frame turns. Geometry now selects only straight
vertical lift or level departure for a latch, and dispatch preserves the dock
latch. Guide02 removed chatter but retained a curve-entry transient1.3736rad/s²;
Guide03 releases the latch one block before curvature, reducing it to.8645.
Finite-difference relative angle still accelerated1.8508rad/s² at that exact
entry: tangent continuity does not imply curvature continuity, and the native
frame pitch rate steps at a straight/curve boundary. The next targeted correction
slows guided travel around anchors rather than changing the acceleration criterion
or repeatedly tuning motor gains. All three guide worlds saved/exited.

Guide04's slower entry isolated the remaining transient on completely level
track at latch release, before curve pitch changed: first acceleration.657rad/s²
followed by motor catch-up. That matches the asymmetric rider's gravity load
acting while a newly created hinge motor takes over. Instead of recreating the
motor hinge on release, keep its gravity compensation active throughout and use
a separate native angular-X latch. Removing only that latch preserves the live
motor, free-turn hinge and both body states. Guide05 targets this corrected
handoff; no free-descent replay is needed for this guide-only change.

Guide05 disproved motor recreation as the remaining release cause: retaining
the live hinge did not remove the same .66 initial gravity acceleration. Inspected
[pinned Sable2.0.3 JNI source](https://github.com/ryanhcode/sable/blob/17d29f5b99d50e3cc13b536b23f9181ade99f14f/sable_rapier/src/main/rust/rapier/src/lib.rs):
its impulse API accepts body-local torque impulses. The motor boolean is a
maximum-force enable, not a force/acceleration model selector (correcting the
earlier hypothesis). Guide06 replaces the delayed position-offset compensation
with measured gravity torque ×dt on the carrier and opposite chassis reaction,
only while guided/capturing. The native motor still follows the bounded reference;
free tumble receives no gravity compensation or artificial spin impulse.

Guide06 only reduced the transient slightly; Guide07 increased native spring and
damping together10×, preserving their tracking time constant while rejecting
external load disturbances. The bounded reference is unchanged. Both sampled
angular acceleration .17355 and finite-angle acceleration .25765 now pass, with
rates .03802/.10694. However, trace review found that low-speed exit braking
stalled before the vertical straight. The pre-solve momentum still includes
Chain's gravity-support pull; it is not actual completed travel. Guide08 measures
chassis displacement across native substeps for its passive brake and explicitly
checks progress beyond the entry bend. This reopens only travel-limit feedback,
not native loading or the underlying free hinge/capture evidence.

Guide08 passes entry progress (center Y135.7 from origin80 at tick2300), sampled
rate .03595/acceleration .17889 and finite-angle rate .10277/acceleration .26408.
The server saved and exited. [Raw guide pass](native/guide-pass.log). Route13
checks only the affected travel-brake feedback across descent/return using the
existing native route fixture; unchanged loading, geometry and guide cases carry
forward. Occupied motion and an actual summit restart remain open.

Route13 closes affected travel feedback: full circuit at9927ticks; speed peak
7.23750blocks/s; relative angle −1.57082..6.28319; guide rate .04930 and
acceleration .37371; lift hold/resume, native passenger retention, free descent,
return and capture pass. [Raw feedback pass](native/travel-feedback-pass.log).
Server saved/exited. Frozen launch-01 is prepared for one private occupied cycle,
actual summit restart, controls/content and finite visual review.

Initial private launch-01 established normal camera turn and reachable vanilla
placement. Two seat aims missed; live native transform targeting then identified
the actual seat block. Geometry inspection found a real X offset: the child pivot
is at block-center X=.5, so earlier hipX .75/.25 gave offsets−3.75/+2.75.
Corrected hip/model offsets to1.25/.75 (±3.25 relative pivot) and centered the
axle's mesh/shape/native COM consistently. This changes transverse mass symmetry,
not local-X inertia or guide reference. Recheck native hinge/load and the actual
occupied/visual claims; preserve unrelated guide/route results. No rider boarded
the earlier geometry. Client stopped, server saved/exited before replacement.

Native04 passes the corrected exact±3.25 hip offsets, empty transverse COM
symmetry, native hinge and four riders on one body. Empty/loaded mass11/14.2;
local-X inertia remains11.349166669/12.983166671 as before; max hinge gap.000146.
The guide and route dynamics evidence therefore carries forward.

Recovery01 used an invalid fixture variant and failed before its guard; replaced
with the valid Arrow variant. Recovery02 retained/repaired/re-aligned the occupied
Roller correctly, but Resume found one adjacent curve still invalid after native
graph propagation during removal. Saved native and original adjacent curves are
identical; the validity latch persists after the transient. The explicit repair
now checks the entire connected closed Roller route against every original hash
before marking those exact legs valid, like the existing bounded PAX route repair.
Changed/missing geometry remains rejected; this is not an automatic rebind.
Recovery03 verifies that affected retention/repair/Resume path.

Recovery03 passes ordinary-N/wrong-family/second-car rejection, occupied missing
support retention, invalid replacement/hash rejection, exact route repair, native
realignment and explicit Resume with the same chassis/child/rider. Server saved
and exited. [Raw recovery pass](native/recovery-pass.log). The occupied launch02
continues its unchanged ride code; repair is independently covered by this
qualified source checkpoint, with no broader profile replay.

Occupied launch02 completed the same car's native lift, guided summit, free
descent, damped return and stopped upright capture. Dev normally boarded the
verified seat, closed restraints and Dispatched via server-acknowledged editor
input. Normal Open and Shift unloaded at(-5.5,81,4.5) on the side platform;
passenger=false and nativeSeatOrientation=null. [Return](occupied/return-ready.json),
[normal unload](occupied/normal-unload.json). No rider duplication or support fault.
The release-boundary observation was recorded, but its actual process restart
was not performed before the car progressed; only that restart claim remains
open, to be collected with an observed-boundary owned-server stop.
