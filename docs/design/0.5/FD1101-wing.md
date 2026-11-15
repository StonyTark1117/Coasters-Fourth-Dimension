# FD1101 — fixed-seat Wing

Status: specification complete; mechanics/content/player validation remain FD1101A–C.
Owner: `01a1dd1d-1468-7f81-8f31-5532ad7a178d`.

## Reference and deliberate game design

The user's [Wing proposal](https://coasterpedia.net/wiki/Winged_roller_coaster)
describes seats beside the track and distinguishes that arrangement from rotating
4D seats. This vehicle uses that arrangement. The dimensions, four-seat row,
coupler spacing and finite operating envelope below are original game-scale
choices, not manufacturer measurements. It has no carrier rotation, rack,
hinge, Arrow profile, magnetic spin or powered seat articulation.

## Stable dimensions and ownership

Coordinates are block offsets from the native bearing plot block, with lateral
X, up Y and longitudinal Z. Its center is `(0.5,0.5,0.5)`.

| Component | Contract |
| --- | --- |
| Native running gear | Unmodified Coasters 0.1.5 bearing and its wheels; native rail constraint owns chassis position/orientation. |
| Four seats | Block offsets `(-4,1,0)`, `(-3,1,0)`, `(3,1,0)`, `(4,1,0)`; all face local north. Four independently assigned passengers, one shared restraint group. |
| Wing beam | Rigid chassis blocks across X −4 through +4 at Y 1; seat blocks replace beam endpoints. Center controller at `(0,1,0)`; no second physical body. |
| Reserved rider/body envelope | Lateral half-width 4.8 blocks, longitudinal half-length 1.5, 3.6 above path centerline and 0.8 below. Includes open shoulder restraints and default standing-height rider bound; equipped models outside the bound are not certified. |
| Coupling | Native links, Wing-to-Wing only, 6-block bearing separation; one or two cars in the initial supported example. Mixed families, missing endpoints and closer coupling reject. |
| Identity | `wing_fixed` family, `wing_fixed_v1` vehicle, original provider; no new Wing track item or fake 4D rail record. |
| Seat transform | Create seat entity on the chassis; Sable owns passenger/body transform and normal relative mouse look. Fixed seat angle/relative angular velocity are always zero. |
| Persistence | Existing seat offsets and player-to-seat assignments; one strictly validated fixed group whose body is the chassis. Original rotary groups retain distinct body requirements. No general provider framework. FD501 decides whether a format change is necessary. |

The existing original seat/restraint meshes can be reused with a distinct Wing
livery; rotary axles and racks cannot. The native bearing supplies functional
wheels. The new rigid beam/chassis mesh must meet the envelope above. Cosmetic
livery selection cannot alter contacts, seat locations or authority.

## Fit and rejection matrix

The installed Coasters 0.1.5 JAR is the contact authority. The [retained mesh survey](../../evidence/0.5/wing/geometry-survey.json) measures
its original OBJ rail segments at local X bounds ±0.13125. The
[inspected native interface record](../../evidence/0.5/specifications/native-interfaces.md)
confirms the path-to-bearing offset .3125 and unchanged centerline guide. `TrackDesign`'s
existing bearing survey records half gauge .772, wheel radius .2, tread interval
.625–.9375 and running/upstop row spacing .55. Wing keeps this running gear.

| Input | Required disposition |
| --- | --- |
| Ordinary pinned Coasters track, fixed in world, clear envelope | Eligible after the measured native contact and swept-envelope check. |
| Banked ordinary track | Chassis and all seats bank together. Validate the full transformed wide envelope; a narrow centerline check is insufficient. |
| Nearby blocks or unloaded envelope chunks | Reject placement with a readable obstruction/chunk reason. Stop before an unsafe supported-route boundary. |
| Functional 4D section or unknown external material | Reject in this initial finite contract; ordinary rail identity must not grant an unmeasured provider universal access. |
| Moving-track host | Reject. Moving track is outside this milestone. |
| Mixed/oversized consist or missing native member | Reject coupling/dispatch; keep restraints closed during motion/fault. |

The finite route contract uses curvature radius at least 16 blocks and up-frame
change at most .05 radians per block (a conservative bank/grade bound), checked
at 64 equal curve parameters. The original seat mesh reaches lateral block edges
and 1.875 blocks above its seat origin when open; with the chosen offsets its
chassis-relative lateral half-width is 4.5, leaving .3 in the 4.8 reservation.
Its longitudinal half-length is below .5, leaving a full block inside the 1.5
reservation for rider pose and discrete sampling. Four default rider bounds
reserve .3 half-width and 1.8 height above the seated feet. The .3125 native
bearing offset is applied once. Running gear is unchanged, so native contacts
are inherited; wide rider/scenery fit is separately checked on both outboard
volumes and the raised center beam.

At six-block centers on radius 16, adjacent heading change is at most .375 rad;
the projected 4.8-by-1.5 reservation plus the adjacent half-length remains below
the center chord, leaving over one block of longitudinal separation. Native
coupled/banked and occupied checks verify the realized poses in A, rather than
claiming every arbitrary route from this bound. Placement and ahead-of-motion
checks reject obstructed/unloaded volumes. Functional 4D track and unknown
materials reject even if their gauge is equal.

## Operation and failure states

Place the craftable Wing item on eligible ordinary track. The stopped car starts
with restraints open. Normal seat use boards; close restraints before Dispatch.
Create/native Chain propulsion moves the chassis; seat logic supplies no traction.
Stop/Capture brakes the consist without seeking a rotary angle. Restraints may
open only when the native chassis is stopped and the track/seat state is safe.
Normal Shift unload uses the existing reachable collision-free exit search.
Unavailable support or incompatible coupling prevents dispatch and stops motion;
repair never silently dispatches or opens restraints. Disconnect/rejoin preserves
the exact occupied seat; permanent removal recovers riders and clears assignments.

## Content, prototype and finite acceptance

Ship the Wing car item and recipe, distinct open/closed fixed-seat/restraint and
rigid beam content, readable controls/tooltips, selectable livery, and an ordinary
track example with a banked section, propulsion, safe station platforms and stop.
The example guide explains wide clearance, coupling, fixed seats and normal use.

Targeted native checks cover ordinary eligibility/rejections, unchanged original
role rules, fixed-body identity/load/seat serialization, coupled motion and
restraint/stop guards. One connected occupied boarding → banked motion → stop →
open → unload proof checks body/camera ownership and is reused by FD1101C.
Normal crafting/placement and the guide complete C's uncovered construction
claim. Reopen/reconnect checks cover only the new fixed group; existing rotary
restoration evidence remains accepted. A second observer is needed only if an
actual changed synchronization path requires it. Comfort C1 follows this proof.
