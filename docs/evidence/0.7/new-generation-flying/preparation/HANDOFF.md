# FD1802 preparation and prerequisite contract

Preparation only; no native mechanism or player acceptance is claimed. Base
`b8db0f06b566ed5b465c758c005db11d5c7b9837`, primary `main` at
`/home/braydon/Documents/Coasters: Fourth Dimension`. Owner
`01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`. All paths here are evidence-only inputs;
production ownership remains with the lead/Conventional owner until transfer.

## Native pose seam requested from Conventional Flying

The Conventional owner confirmed a separately serialized `FlyingState` attached
through `Vehicle.flying`, with actual `Carrier.angle/omega` retained as native
measurements. Its native generic X range is[-π/2,0], design phi=-native angle.
Posture update receives explicit parent/child bodies so a yaw deck can be the
parent. Five valid endpoint ticks, torso/leg restraints, interrupted measured
state and explicit recovery remain shared. `FlyingRiderPose` counter-pitches the
neutral camera by+phi in native X while the physical body still pitches; player
look composes afterward. These are agreed proposed seams, not integrated proof.

Required composition is B*T(P)*Ry(psi)*Rx(phi), B=Qnative*Ry(π).
Consequently deck-native orientation=Qchassis*Ry(psi), posture-native
orientation=Qchassis*Ry(psi)*Rx(-phi). Native yaw sign is unchanged; posture sign
is inverted once. For this variant P=(0,-3,0), chassis-bearing-local pivot is
(0,-3.3125,0); Conventional's pivot is(0,-2.8125,0). An inner deck-relative pivot
must be derived from its actual plot origin, not have that offset subtracted again.

After A integration, reuse exact carrier seat/torso origins, restraint guard and
neutral head/body convention. Add real outer yaw body+joint and two-seat inner
carrier; do not mutate Conventional's four-seat geometry or station-only posture
semantics. Scope new vehicle classes under `NewGenerationFlying*`, with concrete
yaw state/marker/editor extensions only where this vehicle needs them. No FD1901
vehicle or generic scripting/authority framework is included.

## Math and geometry inputs

`reference.py` deterministically produces `report.json`, `combined-profile.json`
and `combined-envelope.svg`. The occupied box has exact radius1.93390796058;
rotation preserves its norm for every simultaneous yaw/posture path, so its
continuous occupied envelope is bounded independently of sampling. The program
also samples18,335 bank/posture/unwrapped-yaw combinations to check conversion,
heading continuity and projected-target math. Those samples do not prove native
physics, renderer alignment, fork clearance or default-player geometry.

A candidate24-block transition at2blocks/s has smoothstep phi0→π/2 while yaw
π/2→π, max reference rate.19635rad/s and acceleration.06545rad/s² for each.
It is inside specified phi(.25/.35) and yaw(.6/.8) command limits. Native actual
rates, measured servo tracking and five-tick endpoint lock remain authoritative;
travel must hold at exit if the endpoint has not settled. Acceleration bounds here
assume constant travel speed and do not replace time-domain limiting.

The occupied sphere spans graphY[-4.93391,-1.06609], leaving.36609 to the original
native-.70 underbrace. This is only the occupied bound. Yaw spindle, rotating
forks and the actual Conventional bogie alteration still need separate authored
mesh/native clearance checks. Fore/aft forks outside Z±2.25 rotate through lateral
space too, so the whole2.75 X/Z reservation must be retained. Do not substitute
independent endpoint boxes or fixed-yaw fork clearance for the combined sweep.

## Concrete program/control preparation

Reuse `RotationProfile`'s unwrapped bounded keys and zero slopes for smoothstep
segments; do not call `mirrored()` or construct a reversed profile when travel
reverses. Canonical route distance selects the same keys in either direction.
The existing common editor packet is revision-checked but lacks marker bindings,
owner IDs, show command IDs/epoch and compound transition state. Extend only the
New-generation branch when transferred; keep previous editor actions unchanged.

Finite proposed operator inputs: named section/endpoints, posture target seated/
prone, yaw mode Profile/Target/Show Stop, angle keys in degrees with visible turn
count, marker selection, direction policy, lower rate/acceleration bounds, blend
length and explicit Retry/Resume/Unwind. Normal marker placement/binding and
profile editing need actual client interaction early, before treating native
method calls as evidence. Surface target plane projection and actual heading,
not a promise of arbitrary3D camera tracking.

Authoring requires empty, stopped and owner/operator permission. Vehicle creation
must record creator ownership rather than grant all riders editor permission.
Server validates marker UUID/dimension/binding, route fingerprint/revision and
controller epoch. Duplicate/stale event IDs are ignored. Authoritative snapshots
need tick/sequence/epoch plus full chassis→deck→posture hierarchy before riders;
the current FoundationSync replacement checks only dimension, so its comment
about stale snapshots alone is not proof of yaw sequence handling. Any necessary
change belongs to this concrete feature and its one changed late-join proof.

Fallback freezes the last valid target and brakes actual yaw at the bounded rate;
no branch snaps or implicit unwind. Retain direction/winding, last valid target,
actual angle/rate, mode/interval entry pose, blend distance, required posture lock,
marker status and restart hold. Missing, unloaded, wrong-dimension, unauthorized,
degenerate, winding-unreachable and authority-loss statuses must remain distinct.
Stop inside a profile/blend freezes distance progress. A named acknowledged show
stop may yaw on ticks while the longitudinal hold and distance remain unchanged.
Station unwind is explicit, restrained, stopped and cleared;4π is not zero.

## Finite verification mapping (existing criteria only)

| Existing claim | Smallest relevant proof after prerequisite integration |
| --- | --- |
| Fit, hierarchy, loading and dynamics | One native compound prototype measuring actual deck/posture bodies, finite axes, native masses/inertias, exact equal rider additions on inner body and parent reaction; reject wrong family/capability/envelope. |
| Combined clearance and train | Native transformed authored spindle/forks/bogie/rider bounds across overlapping nonzero-yaw posture change and declared bank/inversion; reuse four-car8-block link adapter and check this larger envelope/consist. |
| Both-posture target ownership | Real occupied seated-sideways section, simultaneous yaw/changeover, prone target section and bank/inversion. Actual projected heading dot≥cos2° once settled, body/seat/render/eye origins agree, independent look/comfort works. |
| Profile/reversal/show semantics | Native unwrapped crossing, canonical reverse retrace, stationary profile nonadvance, acknowledged show yaw at fixed route distance, explicit winding-zero dock capture. |
| Finite fault/recovery | One winding-limit and one degenerate/missing marker hold with smooth explicit recovery; native validation covers other deterministic binding/authority rejection reasons. No per-family×fault Cartesian replay. |
| New persistence/sync | One mid-transition save/reopen preserves all body identities, actual psi/turns/phi and restraints under held revalidation. One second real client late-joins this new hierarchy and observes the same occupied pose; no wrapped shortest-path interpolation. |
| Normal construction/content | Craft/place matching track/car/marker, ordinary Chain link, normal finite section/profile/marker editor, board/secure/operate, dock unwind/seated/open/Shift. Reuse early occupied proof rather than another full ride. |

The candidate example needs a level36+ dock, seated scenery section, at least24
blocks of overlapping transition at2blocks/s, prone target/banked roll section,
seated return and a named show hold. Motors stay at upright clear approaches;
continuous Chain spans traverse banked areas so hardware occupies neither seat
nor bogie sweep. Keep anchors/curve spans within normal construction limits.
Exact route coordinates depend on the integrated parent geometry and native
solver/clearance findings; preparation does not freeze a known-unqualified route.

No runtime is reserved or launched. Candidate display318/ports25654–25655 are
only a lead suggestion; actual ownership and headroom must be checked/reserved
before use. A second observer needs its own game directory/scope. Retained0.6
worlds and inactive units are untouched. No new branch, chat or subagent.
