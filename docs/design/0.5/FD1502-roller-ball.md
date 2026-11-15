# FD1502 — Roller Ball specification

**Specification complete; implementation remains FD1502A–C.** Version 1,
2026-11-14 America/Phoenix. Normative [shared conventions](specification-conventions.md).

## Reference and chosen motion

[RES's primary Roller Ball page](https://resrides.com/project/roller-ball/) describes
a compact vertical ride with swinging, zigzagging and falling motion. Its published
capacity/footprint are product data, not dimensions copied into this game. Its
[vehicle photograph](https://resrides.com/wp-content/uploads/2020/09/thrill-rides-roller-ball-cart-res-rides.jpg)
is a visual reference. No engineering pivot location or control algorithm was
available from the inspected page. Therefore the following axis, four-seat grouping,
travel limits and smooth fixed route are explicit original game choices.

One native bogie carries one four-seat passenger assembly about **local X**, the
transverse axis perpendicular to the vertical YZ route plane. This is a gravity/
inertia driven pendular tumble, not Spinning's local Y rotation. Whole-car pitch
along the route and relative carrier angle are different state variables. The
single carrier spans the axle; pairs at both sides belong to the same physical
body and share angle/omega. Do not create two independently animated halves.

## Geometry and finite route

| Item | Game specification |
| --- | --- |
| Family / variant | `roller_ball / vertical_zigzag_v1`, dedicated N-contact track with transverse-rotor clearance and lift/station capture |
| Pivot | `(0,2.0,0)` at graph origin; axle X; carrier center of mass below axle at capture |
| Seats | Four seats at `(X,Y,Z)=(±3.25,1.0,±.50)` at capture; front/back pairs face outward ±Z, all on the same carrier; harness plus foot support |
| Rotating bounds | Each side pod X=[2.25,4.25] or [-4.25,-2.25]; occupied closed Y=[-.25,2.50], Z=[-1,1], hence radius≤sqrt(2.25²+1²)=2.46222 in YZ from pivot; reserved rotation radius2.50; axle radius .125 |
| Reserved envelope | half-width 4.50; aboveO 4.75; belowO .75; half-length 2.75; rail/support structure excluded from both pod cylinders |
| Relative motion | Full turns permitted, unwrapped angle; passive `c/I=.6 s⁻¹`; over-speed damping above 2 rad/s, fault/brake above 3 rad/s; no scenery target mode |
| Loading | Four equal .8-unit riders; each seat mass at its actual carrier-relative torso point; add 1 native mass unit of centered under-axle ballast at Y=1.0; log actual assembled inertia |
| Consist | One car in the adopted example; reject native links to any second car or recovery bearing until a separately qualified secured adapter exists |
| Route | One fixed closed path in a vertical plane, no bank/twist; up-leg plus rounded summit, alternating descending runs joined by smooth bends, bottom return and dock; tangent continuous; minimum curvature radius 8; non-adjacent centerline clearance≥7 in YZ |
| Speed / loading dock | Descent≤8 blocks/s; lift≤2; transition control≤3; level dock≥8 blocks; captured seat floor reached from two side platforms |

The 8-block radius and 7-block separation intentionally enlarge the real-world
compact impression to fit Minecraft riders and the 2.5-block pod sweep. The route
must be authored as smooth Bezier pieces, not endpoint cusps or instantaneous
180° reversals. World route direction and relative carrier turn count remain
continuous through each rounded bend. One car avoids claiming native couplers can
follow sharp zigzags without tension; this is a declared configuration, not an
unreported coupling assumption. Native couplers support distances but do not
supply route transitions. The wider structure may be themed around the reserved
sweep; no copied product support frame is required.

## Lift, return, capture and failure states

`DOCK_OPEN → SECURED → LIFT_GUIDED/LOCKED → SUMMIT_RELEASE → FREE_DESCENT → RETURN_DAMP
→ DOCK_CAPTURE → DOCK_OPEN`. The seat assembly captures to its upright dock angle
while stationary. The up-leg guide commands the relative angle `q=-pitch` on this
unbanked vertical-plane route, so passengers remain world upright during the
changing pitch at lift entry. On the straight up-leg, pitch and q are constant and
the physical latch engages after alignment/speed guards. Through a rounded summit
the guided motor takes over before releasing the latch; it tracks the same bounded
world-upright reference until the free-motion boundary. Guide speed/acceleration
limits are .35 rad/s and .5 rad/s²; slow/stop travel if the frame changes too fast.
Closed restraints are required throughout. Lift release
occurs only after the bogie has completely entered the validated free-motion
section and route fingerprint matches. At the summit, release the carrier latch
without applying an artificial spin impulse. Gravity and native inertia generate
motion. The lift's native Create-powered Chain owns forward travel; the passive
route brake caps descent speed. A return damping section reduces relative motion
before final stopped alignment and latch.

A Chain interruption on the up-leg stops travel and keeps an engaged latch or brakes the guided carrier at its current pose.
An unexpected reversal before summit release retains the same guide/lock policy; during free
descent it brakes travel and damps the carrier without snapping it to upright.
Missing route/support/body faults and retains restraints. Stopped recovery can
reacquire native support and explicitly resume a bounded return; never skip an
edge or reconstruct a car at the next switchback. Reload restores the exact route
edge, direction, phase, unwrapped angle, omega, guide target and latch state. A saved pending
release revalidates its location/authority before changing the latch.

Server operation authority owns state transitions; the physical joint owns rider
motion and the native seat owns passengers. Shared camera and comfort behavior
applies. No moving track, winch teleport or distance-clock animation is substituted
for native travel. Save/sync adds lift phase, release boundary and return-damping
binding to the common schema.

## Native feasibility and finite handoff

[Native inspection](../../evidence/0.5/specifications/native-interfaces.md) confirms
fixed Bezier edges, endpoint roll and arbitrary rotary normals; accepted core X-axis
free-carrier physics is reusable. There is no native discrete zigzag switch API
needed by this selected smooth closed route. Remaining FD1502A work is dedicated
functional track eligibility, the shared four-seat body, lift latch transitions,
route/speed validation and the original geometry, not a new graph solver.

The retained prototype plan is one occupied car on the full smooth up/zigzag/return
route: record native edge retention and signed speed, carrier angle/omega, actual
mass, restraint/latch state and transforms at summit, tightest bend and return.
Use empty then fore/aft asymmetric declared load to distinguish inertial response; stop
and restart once at a summit-release boundary, and confirm the same car/seat
identities. Inspect the full pod sweep at minimum radius; reject N ordinary/wrong
family and a requested second car. Pass means no edge hop, support loss, forbidden
latch release, rider/track overlap or duplicated rider, followed by normal capture/
unload. This is a falsifiable future occupied prototype plan, not executed proof.

C1–C6 plus those distinct lift/zigzag/return claims close A/B/C. Original content:
N-contact narrow vertical-plane track, outboard-clear supports, axle/bogie, one
four-seat carrier with harness/foot supports, latch and damping indicator, track/
car/lift-control items and recipes, guide and a player-buildable rounded zigzag
example. It is a separate vehicle/route contract, not an S1 skin.
