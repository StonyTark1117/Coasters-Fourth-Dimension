# FD2104 assembled-source reconciliation

2026-11-22, primary `main`, integrated baseline
`b5e78f31b72a6c22dae25219d1eefa1f5de20a3e` plus the explicitly recorded 1.3
working changes. Reviewer `/root/fd13_structure_reconcile`. This follows the
[twelve-style triage](structural-reference-follow-up.md), rather than converting
its unanswered interfaces into acceptance. Source geometry is evidence about
attachment paths; it is not a new all-family occupied or motion qualification.
The original-five repairs retain the [FD2101 evidence](fd2101/README.md).

## Method and scope

Coordinates below are native plot-relative blocks unless explicitly labelled
model units (16 units/block). Factory cells, state-specific exported models,
the actual `childPivot*`/parent pivot, and procedural geometry ownership were
read together. A renderer `at(body,x,y,z)` adds the plot center plus `.5` on
each axis; overlooking that half-block would invalidate the comparison.
The axes of a procedural beam use half its declared width/height, whereas the
third ring dimension is total axial width. Fixed and child geometry are kept
separate at joints. Padding or a closed restraint is not used as a required
structural connector.

Source anchors live under `src/main/java/dev/fourthdimension/`:
`vehicle/*Vehicle.java` owns cell placement, carrier/body identity and pivots;
`client/fd917/VehicleMechanismRenderer.java` owns the procedural bearings,
brakes and Flying shafts; `client/fd917/ProductionMesh.java` defines their
actual primitive sizes. Exported JSONs are under
`src/main/resources/assets/fourthdimension/models/block/`.

The initial source-review phase launched no build or runtime. The same reviewer
subsequently completed the handed-off [P3 affected client check](brake-mount/README.md).
The coordinator reconciles the combined runtime evidence and final acceptance. The
source review names only the four finite newly evidenced issues below.
It does not add optional silhouette preferences to the required list.

## Twelve-style disposition

| Style | Assembled path and ownership established from current source | Disposition / limits |
| --- | --- | --- |
| Wing | `WingVehicle.create` installs beam cells X=-4..4 at Y=1, then overwrites center with controller and four outer cells with seats. All are in `v.chassis`; there is no rotary bearing. `fd13-base-geometry-audit.py` traces controller → remaining beam → original arm fairings or new mounting shoes → all four pans. The baseline already connected through its arm fairings. | W1 is construction/silhouette refinement, **not a newly discovered gap repair**. The coordinator's affected occupied/item observations qualify the refined exports. |
| Fixed Inverted | `FixedInvertedVehicle` places the nine frame cells and four seats on the same chassis. The integrated FD2101 gantry → rear spars → lower rail → pan graph remains the source of attachment authority; I1 retains those members and changes frame section hierarchy/back shape. | Retain accepted FD2101 connection and full inversion evidence; I1 needs its altered-surface observations. Do not infer new mechanics qualification from frame styling. |
| Dive | `DiveVehicle` mounts six seats at X=-2..3,Y=1,Z=0 and two rails at Y=0,Z=±1. In `generate-dive.py`, each seat bracket spans model Z=-8..24,Y=-4..-2, becoming Y=.75..875, and reaches the two rail meshes (Y=.75..1). Its upright spans Y=.75..1.5 and touches the pan at Y=1.5. The controller occupies one rail cell and intersects the same bracket/rail chain. All remain chassis-fixed. | No new required repair found in the named rail → bracket → pan interface. Six-abreast/floorless silhouette suggestions stay optional. This is source attachment review, not a new six-rider test. |
| Pipeline | `PipelineVehicle` installs a complete floor at Y=-3, upper end crossmembers/posts, controller Y=-2 and four seats Y=-2. `PipelineWheelRenderer` constructs each side truck's C bracket and a beam to the chassis mount `(0.5±1.4, .7375, .5±2.3)`; those coordinates meet the upper frame. They do not support a disconnected seat by themselves. Exact export graph found two gaps: floor→pan and pan/footwell→main shell. | **P1 required repair** below. After repair, controller→floor→all four complete fixed shells/hinges is connected in both restraint export states; retain side-truck/track mechanics. |
| Suspended | `SuspendedVehicle` uses parent pivot `(.5,-1.3125,.5)` and child `(0,.25,.5)`, Z swing. `generate-suspended.py` journal shaft Z=-1..2 at Y=.1875..3125 reaches the fore/aft fork inner faces after the child-to-parent translation. Child rods at Z=-.75..-.625 and1.625..1.75 join the cabin rods; cabin side/back panels touch the two seat-back edges. The fixed hanger and rotating rod/cabin members are distinct. | Main shaft/fork/cabin load path is present; the **separate central stationary generic brake hardware floats**, P3 below. Do not call that a missing native joint or a detached cabin. Retain accepted swing mechanics. |
| Axis | `AxisVehicle` has parent pivot `(-3.5,1.6875,.5)` and child `(0,.9,.5)`, Z roll. `axis_journal` shaft is X=±.1875,Y=.75..1,Z=-.75..1.75. After translation its endpoints meet the fixed fork clamps: front Z=-1.5..-.75, rear1.75..2.5, with overlapping X/Y. Journal crossbar X=-1..1,Y=0..1875,Z=.6875..875 contacts the two fixed seat backs. The child journal/seat elements rotate together. | Main fork → shaft → seat path is present; **central fixed generic brake hardware is unsupported**, P3. No new fork or seat pivot is required. |
| Roller Ball | `RollerBallVehicle` places the one X-rotating child shaft at cells X=-3..2,Y=1,Z=0; each axial element spans two cells and therefore covers X=-3..4. Shaft Y=1.375..1.625,Z=.375..625. State-specific seat transforms (`left`, NORTH/SOUTH) place the bosses at the same Y, with front/rear Z bands .5..1 and0..5; both overlap the shaft. Fixed frame/controller intersects the central parent race at pivot `(.5,2.1875,.5)`. Boss → load spine → shell/pan is in the child body. | Both facings and all four seat groups have a source-supported route; no original-five crank fix is applicable. No new required refinement identified. |
| Conventional Flying | `FlyingVehicle` uses four child seats at X=-2..1,Y=0 and true child posture pivot `(0,.5,.5)`. `flyingMount` supplies an X shaft ±2.10, plus stationary endpoint connectors to the fork centerlines ±2.21875. Its fixed sockets remain with chassis; receivers/shaft with child. The shaft overlapped cushion but missed structural pan/cradle by .065. | **F1 required structural refinement** supplies child-fixed edge saddles. Preserve actual posture pivot, body/leg restraints and existing prone sweep. |
| New-generation Flying | `NewGenerationFlyingVehicle` allocates a separate yaw deck, then the two-seat X-posture child. `NewGenerationFlyingDeckBlock` central upper stem reaches the chassis controller, and its continuous crossbar/side arms reach `flyingMount` sockets at ±1.37. The posture shaft and child pivot are separate from the upper Y yaw attachment; neither drive nor restraint is a substitute for this hierarchy. Its seats share the same shaft/pan issue as Conventional Flying. | **F1** affects only the two fixed seat-edge saddles. Keep existing nested yaw/posture mechanics, deck and interfaces unchanged. The upper spindle/controller contact is source-established, not a newly observed yaw bearing performance claim. |
| S1 | `SpinningVehicle` has child Y pivot `(0,0,0)`, parent `(.5,1.9375,.5)`. Four seat cells Y=-1 have fractional X/Z layout authored before export. Footwell/sills/risers overlap the four bearing cells' upper mounting plates at Y=-.3125..-.1875; rotating drum and child receiver connect. Controller top Y=1.625 did not reach fixed race bottom1.7675. | **P2 required stationary mounting repair**. Retain fractional seat anchors, shared child turntable and lap bars. |
| S2 | Same controller, parent pivot, fractional seats and child turntable attachment as S1. Separate child reaction ring/launch equipment is not the stationary fixed-race support. | **P2**, same bounded repair. S2 nose/item styling remains optional. |
| Powered Inverted Yaw | Parent spindle occupies Y=-3..-1 below controller. Lower spindle flange Y=-3..-2.8125 overlaps the fixed race axial band about parent pivot `(.5,-2.8125,.5)`, with flange corners reaching the annulus. Child bearing at cell(0,-1,0) spans X=-.875..875 and reaches rear pod supports extending down toY=-.625. The child shoulders also meet the coaxial receiver. All rear supports/pod floor/pans remain in the child Y body. | No new required defect found in these named interfaces. Propulsion remains separately owned Chain/drive machinery; this source contact review does not certify arbitrary yaw clearances. |

## P1 — Pipeline fixed support repair

Authoring source: Pipeline branch of `scripts/fd917_articulated_geometry.py`.
The native floor top is Y=-2.0625. Previously each seat's cabin pan started at
Y=-2, leaving exactly1/16 block. The footwell/pan component was also separate
from the main shell, whose bottom is model Y=4 while the footwell tops at3.
This is established using all exported fixed parts and every controller/frame
cell, not a selected screenshot or one cuboid pair.

Repair lowers only the pan underside from model Y=0 to-1 and adds two fixed
rear risers `[3,2,10]..[5,5,14]` and `[11,2,10]..[13,5,14]` before the existing
fractional X transform. The pan, risers and shell all belong to the rigid native
chassis. Rider-contact surfaces, seat state, articulation, anchor positions and
native fit are unchanged. The whole assembled envelope is unchanged:
`[-1,-2.2125,-2]..[2,.8075,3]`.

Changed outputs are the eight `pipeline_seat_{0,1}_{0,1}_{open,closed}.json`
models. The existing Pipeline item is a schematic floor/four-seat/truck
miniature; it does not model these tiny seams or risers. No new silhouette or
mechanism was introduced in the item. Its schematic support omission is not
presented as literal full-size geometry proof.

`python3 scripts/fd13-pipeline-support-audit.py` reproduces all eight baseline
minimum separations `0.0625`, then reports current gap `0` and reachability from
controller to **every fixed seat part** for all four seats in both states. It
also checks exact old cushion/hinge parts, native factory, restraint meshes and
descriptor preservation and the unchanged assembled envelope. Result: **PASS**.
Affected runtime evidence still required: clear low-side empty/occupied views
of the floor/pan/riser connection; normal restraints open/closed suffice because
the additions are fixed below untouched rider surfaces. No side-rail campaign.

## F1 — Flying shaft saddles

The actual child shaft center is Y=Z=.5, total thickness .12. Its lower face
Y=.44 is above the metal pan's top .375; its rear face Z=.56 is ahead of cradle
front .625. Minimum structural separation was `.065` block, although cushion
material occupied the gap. Thus the issue is a credible metal support path,
not absence of a native posture joint or total visual disconnection.

Two child-fixed metal edge saddles `[0,4,6]..[2,7.5,10]` and
`[14,4,6]..[16,7.5,10]` connect the shaft to the pressed pan and existing posture
cradle. Saddles remain below cushion top7.8 model units and inside the original
whole seat bounds; every old element is unchanged. Four `flying_seat_00/01/10/11`
and four `new_generation_flying_seat_00/01/10/11` exports change. Restraint meshes,
leg meshes, pivots and descriptors do not change. Existing miniature support
frame is schematic and has no separately rendered full-size shaft saddle.

`python3 scripts/fd13-articulated-support-audit.py` excludes upholstery when
checking shaft→saddle→pan connectivity, reproduces the baseline .065 gap,
checks all four body/leg export states for both families, and checks preserved
cushions/hinges/bounds. Result: **PASS**. Affected runtime views: occupied
upright and prone/posture transition showing the edge attachments for each
family; reuse accepted drive/yaw/boarding behavior and do not rerun all routes.

## P2 — S1/S2 stationary race mounting

In `VehicleMechanismRenderer`, a new **parent-owned** annular sleeve is placed
`.245` below the fixed pivot, with inner/outer radii `.18/.29` and axial width
`.19`. Its vertical interval `[1.5975,1.7875]` overlaps the controller top1.625
and fixed race bottom1.7675. The square controller top has half-width .1875,
so its corners meet the sleeve annulus. The child receiver radius is .17;
the sleeve retains .01 radial clearance. It does not add a solid post through
the rotating hub. Native pivot, turntable, seat geometry and mechanics do not
change. These dimensions remain within the existing controller/race envelope.

The articulated support audit checks these overlaps and radial clearance.
Result: **PASS**. Affected runtime views: low-side mounting view for S1 and S2
through yaw; retain their accepted ride/control evidence.

## P3 — Axis/Suspended stationary brake attachment finding

The generic Z-joint renderer originally draws its fixed ring, pads, caliper
brackets and indicator at the **central** carrier pivot. The valid long shaft
meets the separate fore/aft forks, but that does not attach stationary central
brake hardware to those forks. A complete primitive-bound check included the
fixed ring and the beam corners for open and locked pads/brackets/indicator.
Every stationary primitive is separated from all fixed frame/controller
cuboids: conservative minimum distances are `0.687261889` blocks for Axis and
`0.028284271` for Suspended. Positive distance between enclosing bounds proves
these are disconnected; it is not being used to infer contact from overlap.
The rotating shaft cannot serve as the brake's stationary mounting.

Implemented bounded correction in `VehicleMechanismRenderer`: move the
**displayed fixed and child brake assembly** coaxially along the existing Z
journal to the front fork. Axis uses offset-1.07 from pivot and collar width.36,
so both collars' front faces meet child-local Z=-.75. Suspended uses offset-1.29
and width.42, meeting Z=-1. Neither collar extends into the solid fixed fork.
The Axis fixed annulus uses radii.25/.32 to clear the rectangular rotating
journal's corner sweep; its rear plane Z=-.39 stops .015 before the existing
child front collar starts at-.375. The Suspended annulus retains.18/.29; its
child receiver's inner radius changes from.12 to.04 to grip its smaller square
shaft (outer.17 unchanged). Axis receiver dimensions otherwise retain.12/.17.

The two fixed caliper-arm roots are joined by a fixed beam at radial distance
.39, spanning axial offsets.06..54 and with .065 square cross-section. Its
inner face .3575 remains outside the complete .32-radius rotating disc sweep.
The near arm meets the fixed ring in both lock states, the bridge supports the
far arm, and the indicator attaches to that bridge. No rotating component is
used to support the stationary brake. Native factory placements, pivots,
angles, physics and seat assets are unchanged.

The articulated audit checks actual fork-plane contact points, near-arm/race
intersection in both lock states, receiver/shaft grip, and pose-invariant
coaxial alignment at Axis roll quadrants and Suspended ±45°/neutral. It uses
the actual 32-sided annulus bore's apothem: minimum fixed-ring/shaft clearance
is `.008679` for Axis and `.090745` for Suspended. Result: **PASS**. These
measurements establish the named joint interfaces; affected neutral and
roll/swing client views must still confirm presentation/clearance before case
acceptance. The renderer SHA256 at source handoff is
`bc47271faefa6376e184c76d55f7e657086fe176c5f959ab96a2228cfd85b49a`.

## Freeze boundary

W1/I1 plus the adopted finite P1/F1/P2/P3 repairs are the
current required base work. Other reference-review aesthetic suggestions remain
optional. No new capacity, track, camera, physics, restraint mechanism or family
profile is required by these findings. Base readiness must cite the affected
runtime checks before closure; this document alone is not an all-seventeen
structural or occupied certification. It does replace the earlier twelve-style
triage with named assembled paths, four finite findings and explicit limits.
