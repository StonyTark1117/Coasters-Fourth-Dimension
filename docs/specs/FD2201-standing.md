# FD2201 — original standing configurations

Status: implementation contract, 2026-11-22. This specification adds two finite original configurations and preserves the accepted seated Pipeline. It does not certify either new configuration before affected runtime evidence exists.

## Reference and design boundary

[B&M Surf Coaster](https://www.bolliger-mabillard.com/surf-coaster) describes standing riding with a new restraint and airtime experience. [SeaWorld's Pipeline page](https://seaworld.com/orlando/roller-coasters/pipeline/) describes dynamic seats during launching, banking and curves. These primary references establish the distinction between a rigid standing arrangement and a moving standing support. They do not publish an engineering joint drawing, spring law, stroke or dimensions. All geometry, limits and dynamics below are original game design, not replicas or manufacturer specifications. The current `PIPELINE/seated_side_rail_v1` has fixed seated passengers and P side-rail contacts; it cannot represent this Surf mechanism by renaming.

## Finite roster and frame

Family `STANDING` (`standing`) has `standing_standard_v1` and `standing_surf_v1`, both exactly one native car and one two-place, side-by-side passenger group. No couplers, recovery bearing, other body choices, independent seat-height adjustment, inversion or mixed consists are supported. Both use original N running contacts above fixed world rails, with matching declared standing track variant. The bearing, wheels and native Chain remain travel authority; no invisible track motor is introduced.

Coordinates below are blocks in the native track frame: X right, Y up, Z along the rail; O is the graph rail origin. Native chassis block origins are O+(-0.5,-0.1875,-0.5). Rider foot centres are X=-0.65/+0.65, Y=1.25+q, Z=0.0. Two stance panels are authored around those centres, with standing feet, straight legs, a small rear support pad and shoulder restraints. They do not use a seated cushion/hip pose. Body, eye origin, restraint and footplate share the physical owner; optional existing comfort-camera orientation remains camera-only.

| Dimension | Both configurations |
|---|---|
| Reserved half-width / half-length | 1.75 / 1.75 |
| Reserved above / below O | 4.00 / 0.95 |
| Rider centre spacing | 1.30 |
| Foot platform top | Y=1.25+q |
| Standing body / eye approximation | feet+1.80 / feet+1.62; runtime uses actual native eye |
| Rear support plane | Z=+0.35 through +0.50 |
| Foot panel width / depth | 0.85 / 0.90 per place |
| Car spacing from other bearing | at least 8 blocks or the other's larger spacing |

Standard uses a rigid parent-owned beam and rear posts, with q=0. Surf uses fixed rear guide columns, a real child carriage with overlapping guide shoes, a crossmember, both moving footplates and both restraint supports. Surf q is local-Y translation, 0.00..0.40. Maximum occupied head height is 3.45; the 4.00 reservation includes clearance. Required geometry audit checks actual metal contact paths from running-gear support to posts and from guide shoes to platform/restraint hinges at both stroke endpoints, not just enclosing boxes. Guide clearance is a declared sliding interface; it must not be falsely described as welded solid contact. Assets use only `standing_`/`surf_` identities and existing original materials.

## Track, dock and propulsion

Matching `STANDING_STANDARD`/`STANDING_SURF` track offers the same N contact and above-rail mount with the reservation above; a car rejects the other variant even though contacts are alike. Fixed world hosting only. Minimum curve radius 16; absolute pitch <=45 degrees; absolute bank <=45 degrees; roll rate <=5 degrees/block; speed <=12 blocks/s. No inverted segment is accepted. Clearance samples the complete occupied reservation including Surf travel and both directions of stopping lookahead, including the next connection. Branch ambiguity, unregistered support, unloaded geometry, route-end risk and changed support geometry retain the car.

Propulsion is the existing native Chain authority. Save accepted Chain anchor directions on dispatch/resume and fault on missing power or reversal; changing power cannot implicitly authorize reverse travel. A level, straight-enough 24-block matching dock, with unchanged geometry, car speed <=0.05 and q near home is required for opening/boarding. At a level dock, dry sturdy side platforms lie outside the 1.75 half-width reservation, sampled 3.5 blocks to each side. Their surface must be 0.45..1.50 above the native bearing center (feet are bearing+0.9375), with 1.9 blocks of clear headroom. Authored examples use platform blocks one block above their anchor-block origin: platform top O+1.6875, 0.4375 above home feet. Dock binding and actual collision-free player exit reuse station/boarding authority. No automatic train-mode station, return teleport, force mount or new multi-train scope.

## Physical Surf state and interruption

Sable 2.0.6 `GenericConstraintConfiguration` locks local X/Z and all three angular axes; `GenericConstraintHandle.setLimit(LINEAR_Y,0,.40)` bounds physical translation. Parent and child contacts are disabled only for this designed guide overlap. A second all-axis latch uses measured q when retained. Standard has no second body/joint. No per-tick teleport establishes Surf travel.

During ordinary running the carriage has an original damped spring about q=.20, gravity compensation along the guide and equal/opposite parent impulse. The mass-normalized spring command is 64*(.20-q)-10*qdot, clamped to +/-6 blocks/s² before guide-axis gravity compensation. Dock return uses desired velocity clamp(-3*q,+/-.25), then acceleration clamp(10*(desired-qdot),+/-1.5). The physics solver owns actual q, velocity and passenger motion. Control and capture qdot is the finite difference of successive native anchor positions over the physics timestep; native point velocity is separately checked for gross faults because its pre-step gravity phase differs from carriage displacement. Once acquired, a valid home latch stays engaged while settling, with actual position/velocity still required for boarding. Returning at the level dock targets q=0 with a bounded speed/acceleration controller; capture requires |q|<=.015 and |qdot|<=.03. A hard stop, unsupported motion, missing child/seat/frame, invalid joint, q outside the tolerance-expanded stroke or excessive carriage speed closes restraints and stops travel. Away from the dock, interruption latches measured q; it does not drag the carriage home through an obstruction.

Persist versioned `StandingState`: accepted drive directions, last measured travel and velocity, interruption flag and held travel. Native body state remains the pose authority. Reload reconstructs constraints at the actual saved pose and requires retained/stopped state plus explicit validated Resume. A missing or incompatible state rejects unsafe restoration. Opening remains denied until a valid level dock and home capture; an interrupted mid-stroke car needs explicit Resume to return to that dock. Permanent removal recovers occupants through the existing collision-tested exit path.

## Integration ownership and APIs

The FD2201 owner adds `StandingVehicle`, `StandingTrack`, `StandingRuntime`, `StandingState`, `StandingSeatBlock`, `StandingFrameBlock`, `StandingContent`, standing rider/model mixins, original prefixed assets, `generate-standing.py`, focused audits/tests/examples and evidence. Standard and Surf items/tracks register through `StandingContent`'s own deferred registers. Existing files remain coordinated by the root owner.

Root integration points: Family/TrackDesign/TrackContract, register `StandingContent`, FoundationData field `standing` and versioned save/load, eligibility/factory, single-car ConsistRules, CarrierRuntime early dedicated branch, station operations and editor, FoundationSync, exact native rider mass/boarding offset handling, mixin list, loading/removal and example routing. Public API: `StandingVehicle.is(v)`, `surf(v)`, `fit(TrackDesign)`, `validate(v)`, `create(level,hit,TrackDesign)`; `StandingTrack.rejection(level,edge)` plus variant-sensitive overload; Runtime `ready(level,parent,v)`, `tick(level,parent,v,dt)`, `rebindDock`, `safety`, `resumeToDock`, `acknowledge`, `rememberConsist`, `fault`. Runtime owns Surf joint reconstruction and child loading; generic rotary carrier mechanics must not also drive this family.

## Affected acceptance and limits

1. Source/geometry checks establish dimensioned support paths, distinct standing silhouettes, endpoint sweeps, matching fit and state validation. They are not occupied runtime proof.
2. Native focused checks exercise bounded physical translation, retained mid-stroke reconstruction, explicit Resume and rejected malformed/mismatched support. Existing native travel/boarding acceptance is reused where unchanged.
3. One early occupied client proof for each configuration establishes standing feet/legs/eye origin, normal restraint use, physical Surf child motion and supported travel. Retain these observations for final acceptance; do not repeat merely because other 1.4 packages build later.
4. Affected interruption/recovery includes drive loss or invalid support, mid-stroke hold, normal dock capture/open/unload, and clean cold-process restoration with no automatic release. Evidence identifies actual bodies, q/velocity, selected variants, logs and screenshots.
5. Preserve exact artifact/source identities and cleanly stop owned runtimes. Failure diagnosis is bounded by the validation policy; no old-family campaign or additional release gates.
