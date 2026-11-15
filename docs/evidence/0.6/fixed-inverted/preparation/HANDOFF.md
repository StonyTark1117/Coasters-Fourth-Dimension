# Prepared Fixed Inverted inputs

Normative source: [FD1601S](../../../../design/0.5/FD1601S-fixed-inverted.md),
with [shared coordinates](../../../../design/0.5/specification-conventions.md).
Prepared against main `d73d43b8`; this is original game content, not manufacturer
engineering or a native acceptance result.

## Geometry and art

`design.json` uses graph sample O, X=-native right, Y=track up, Z=forward.
For a design vector `(x,y,z)`, the bearing-relative native vector is
`(-x,y-.3125,-z)`. The plot origin still needs the integrated native adapter:
do not round fractional seat origins to BlockPos or add .3125 twice.

Four separate purple seats have pale metal frames, dark shoulder harnesses,
orange latch accents and forward foot supports. The fixed row hangs from two
outer **rear** posts. Their nearest Z face is -1.25, giving .25 clearance behind
the occupied Z=-1 bound without pushing a solid post outside the 2.25 half-width
envelope. A rear crossbar and individual seat brackets are intended seat mounting
contacts; they are identified separately from free-clearance surfaces. The row
does not contain a joint, axle, damper or decorative rotor. Native N wheels will
come from the integrated overhead bogie; the OBJ deliberately does not duplicate
them or claim to model the prerequisite's as-yet-unqualified track/support mesh.

Closed/open art remains inside the declared whole envelope. The .80 specified
head-to-underbrace gap is a static design-frame comparison. The .25 post gap is
also static; neither proves banked/curved vehicle clearance. The actual native
mesh and rider sweep, body mass and loaded center of mass remain A measurements.

The current Wing path uses integer seat-block offsets and Create's sitDown mount.
The specified half-block X row and Y=-3.25 must be matched by the **actual** seat
mount and body transform. Determine those origins from the handed-off native body
before selecting block locations/model translation. Reusing Wing requires one
chassis owner, not four phantom carrier bodies or a zero-angle rotary joint.

Read-only Java 21.0.13 `javap` inspection is retained in `native-plot-bytecode.txt`
and `native-seat-bytecode.txt`. Coasters 8655742's `bearingCenterOffsetPlot`
rotates `(.5,.5,.5)`; Create 6.0.10-280's `sitDown` sets a SeatEntity at
`(block.x+.5,block.y,block.z+.5)`. Thus integer seat blocks on the fixed chassis
have integer transverse centers relative to the bearing, not the specified half
positions. Suspended's separate child-pivot solution cannot be copied to this
single-body fixed row. Its owner also confirmed the spec's Y=-3.25 is a **seat
center**, corresponding to nominal seat block origin Y=-3.75.

A concrete adapter candidate is native block offsets X=-1,0,1,2; Y=-4; Z=0,
with local model/mount translation `(-.5,+.4375,0)`. This gives native seat centers
X=-1.5,-.5,.5,1.5, graph Y=-3.25, Z=0 without moving the rail bearing. It retains
the mount inside the original block's floor coordinates, including X exactly at
the block boundary. It is **not implemented or accepted**: the seat use shape,
SeatEntity validity/persistence, camera attachment and mass placement must all
agree on the offset. Existing NativeLoading's integer `seat.above()` masses would
otherwise remain .5 sideways and .4375 below the translated torso points. Resolve
that narrow fixed-seat offset hook with the lead after the ownership transfer;
do not solve it by shifting the whole rail chassis or adding a carrier joint.

Inspection JARs are the same pinned artifacts as the integrated baseline:
`curse.maven/create-coasters-simulated-1601871/8655742` and
`com.simibubi.create/create-1.21.1/6.0.10-280`; exact SHA-256 values are in
`native-inspection-sha256.txt`. This inspection launches no Minecraft runtime.

## Playable example proposal

The route is an original lifted stadium: a 48-block level loading straight,
64-block gradual lift rising 24 blocks, 128-block straight full roll (four
32-block quarter-roll spans), gradual descent, and broad 48-block return turns.
Four eight-block-spaced centers span 24 blocks; with 1.75 end extents the train
occupies 27.5 blocks, leaving 8.5 blocks in the required 36-block dock and
20.5 in the proposed 48-block straight. This longitudinal arithmetic is not a
coupled native sweep. The four-car envelope must be checked at native transforms.

Waypoint tangents and bank values are in `design.json`. Corners use a 48-radius
quarter-circle cubic approximation; lifts use horizontal endpoint tangents and
one-third horizontal handles. Quarter rolls are 90 degrees over 32 blocks:
2.8125 degrees/block for linear bank, 4.21875 at the peak of cubic smoothstep.
The native interpolation and unwrap through 180/270/360 must be measured after
the overhead handoff; no script claims native roll compliance in advance.
Chain drives the lift and low-speed dock, gravity/coast covers the remaining
route, and the accepted whole-train brake stops at the dock. Chain sign, achievable
travel speed and continuous traversal are native implementation checks.

Place the route high enough for the complete -4.50 lower envelope. A player
walkway approaches the **front** of each row, with a declared boarding opening;
any piece entering the travel envelope retracts or clears before dispatch.
Do not treat platform collision as proof of unsafe hangers or bypass it by
disabling clearance. The actual station geometry follows the Suspended dock
interface rather than creating a competing capture mechanism.

Proposed player instructions: craft/place the overhead track and fixed car,
connect up to four identical cars with native Chain, approach a visible cushion
from the below-track loading platform, use the seat, close restraints, Dispatch,
ride the lift/inversion, Stop at the level dock, open restraints, Shift to leave.
The guide must distinguish an emergency stop away from a dock, which keeps the
restraints closed. Use Recovery only after support/consist are valid; power
restoration alone must not dispatch. Demonstration placement is disclosed setup;
the normal construction evidence must separately show real recipe/place/link use.

Proposed recipe inputs use existing iron, seats, native cart/track and Chain.
Final resource IDs and recipe counts follow the lead's registration handoff.
The four-seat car item must be distinguishable from Suspended's two-seat cabin;
its tooltip explicitly says fixed below-track seating and matching overhead track.
Render inventory, placed row and occupied/inverted operation from the same
accepted run where possible. No additional presentation-only client campaign.

## Exact handoff requested

From Suspended: integrated FD1301A commit and occupied proof path; N contact-layout
and overhead mount IDs; four-scalar envelope and route-eligibility methods;
bogie/track render geometry and support exclusions; designated dock/whole-consist
capture checks; loading and fixed-seat dispatch hooks. Fixed Inverted needs no
swing range, lift swing lock, generic joint or rotary angle state.

Proposed new family paths (not yet assigned or created in production):

- `src/main/java/dev/fourthdimension/vehicle/FixedInvertedVehicle.java`
- `src/main/java/dev/fourthdimension/vehicle/FixedInvertedTrack.java`
- `src/main/java/dev/fourthdimension/vehicle/FixedInvertedRuntime.java`
- `src/main/java/dev/fourthdimension/vehicle/FixedInvertedSeatBlock.java`
- `src/main/java/dev/fourthdimension/vehicle/FixedInvertedExamples.java`
- `src/test/java/dev/fourthdimension/FixedInvertedContractTest.java`
- `docs/FIXED-INVERTED.md`
- Family-specific `fixed_inverted*` blockstate/model/recipe files, with exact
  names and generator ownership agreed before writing.

Shared integration seams to transfer explicitly or have the lead edit: Family,
VehicleContract/TrackContract, VehicleParts/FamilyControllers, family factories,
CarrierRuntime fixed routing, Boarding, NativeLoading, ConsistRules, operations,
sync/persistence, commands, creative items, language and asset generator. This is
a seam inventory, not permission to touch every file. Inspect the integrated
overhead implementation before requesting the minimum actual diff.

## Finite acceptance use

Reuse Wing fixed mount/look/comfort/restoration and Suspended overhead N-contact
proof. Missing family claims are rigid occupied bank/inversion, actual four-car
clearance, fixed level-dock capture, native non-upright save/reopen, ordinary/wrong
track or missing-overhead rejection, and its normal construction/operation content.
Native checks and player evidence may satisfy overlapping claims in one run.
Use a second real client only if a new or changed synchronization path needs it.
Do not requalify sibling families or introduce a run for the FD1601 aggregate.
