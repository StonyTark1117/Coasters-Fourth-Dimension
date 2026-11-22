# FD2101 — original cart support defects and repairs

**Named FD2101 defect repairs verified 2026-11-17.** Primary checkout `main` at
`/home/braydon/Documents/Coasters: Fourth Dimension`; no new branch or worktree.
The first pass identified Fixed Inverted and Arrow. The subsequent assembled
source review found the same center support gap in Legacy, Free Spin, Revised
and PAX, plus missing axle-to-seat paths in those original families. Beyond
the accepted Fixed Inverted case, the other twelve have no newly proven defect,
but their full structural interfaces
were not established by the first visual review; see the
[structural follow-up](../structural-reference-follow-up.md).

## Fixed Inverted defect and correction

The four Fixed Inverted cushion pans began at model Y=12 while their narrow
underseat spars ended at Y=11: a visible 1/16-block separation. The rear
crossbars had a reinforced connection at the right post, but only a face-touch
at the left post. The thin left joint and the pan gap made the middle row appear
to hang from the back shells instead of a continuous frame. The affected original
occupied view is [FD917 open](../../fd917/art/fixed-inverted-occupied-open.png).

The [generator](../../../../scripts/generate-fixed-inverted.py) now extends the
four spars to Y=13, adds a narrow rear underseat crossmember in each seat block,
and bridges the left bottom frame cell to the existing rear crossbar. The load
path is native chassis → controller/upper crossbar → side posts → seat crossbars
→ underseat spars → pans. The unchanged restraint model rotates about
`[0,29,15]` on the same fixed seat blocks. No seat position, capacity, hull,
interaction shape, block identity or ride logic changed. The new bars remain
inside the old frame/seat envelope, behind the rider's dangling legs. The
whole-car item already shows a connected rear support and needed no change.

## Fixed Inverted checks

- `python3 scripts/generate-fixed-inverted.py` passed model bounds and UV checks.
  `python3 scripts/fd2101-support-audit.py` found a connected controller-to-pan
  path for all four seats in open and closed states; the HEAD asset baseline
  returned four disconnected pans under the same contact graph.
- The Java 21 packaged launcher build passed. Run artifact SHA-256:
  `a707cb7fc97216654088271038a5256a8c460844e98bd6989775c683f012e9e0`.
  The source was `main` at `5d385da0` plus this model correction and concurrent
  uncommitted 1.2 work; the run's actual artifact identity is retained.
- One copied FD917 `SkinWorkshop` on private display `:337`, server
  `127.0.0.1:25681`, client heap 1536M / 6 GiB scope, server 4 GiB scope.
  [Empty front](empty-front.png), [occupied open](occupied-open.png),
  [occupied closed](occupied-closed.png) and [returned open](returned-open.png)
  show the joined rail, unblocked dangling legs, head fit and truthful bar state.
  Normal right-click boarding, editor Close, Dispatch, Stop at the level dock,
  editor Open and Shift dismount all succeeded. No setup command altered the
  vehicle pose or restraint state during the occupied cycle.
- The [trace summary](trace-summary.json) records 6,996 occupied moving samples,
  minimum chassis up-Y `-0.999987` (full inversion), the same chassis seat owner
  throughout, closed restraints during inversion and no fault. Frame, seats and
  new bars are rigid blocks in that chassis, so the inverted pose cannot separate
  their joints. The final row is Ready at the level dock, upright, unoccupied,
  open and fault-free. No new geometry extends outside the accepted swept
  envelope; unaffected clearance and rider camera evidence remains accepted.

The full trace, input record, logs, screenshots and saved copied world are under
`/mnt/NVME4/fourthdimension-runs/fd2101-20261117`. After `save-all flush`,
client, server and Xvfb units all reached inactive/MainPID=0; see
[cleanup](runtime-cleanup.txt).

## Central integration reconciliation — 2026-11-22

The finite FD2101 source was reconciled in the primary `main` checkout for
selective integration. The affected source/output paths are the two generators,
`src/main/java/dev/fourthdimension/vehicle/VehicleFactory.java`,
`src/main/java/dev/fourthdimension/vehicle/VehicleParts.java`,
`src/main/java/dev/fourthdimension/client/fd917/OriginalCarrierSupportRenderer.java`,
the paired-spine blockstate/model assets, and the generated original-family
models. The Java factory/parts paths were reconciled with the 1.2 checkout;
their FD2101 changes are part of the selective central integration and are not
claimed as 1.2 behavior.

Reconciliation checks on 2026-11-22 reran
`python3 scripts/fd2101-support-audit.py` successfully. The audit reports the
controller-to-crossbeam, axle-to-seat and paired-spine paths connected in both
restraint states, including the revised bearing-yoke containment. The saved
FD2101 run evidence and inactive-unit records remain the authority for the
bounded visual/occupied claims. FD2104 remains a separate planned reference and
implementation review; no FD2104 source or evidence is promoted by this record.

## Arrow axle-to-bucket and center mounting gaps

The FD917 [Arrow connection](../../fd917/art/arrow-interface.md) fixed the
rail-to-transmission and actual-child crank-to-axle path, then passed occupied
motion. It did not bridge the axle to the bucket shell or visibly mount the
middle of the stationary crossbeam to the cart. In the original exported
seat-local coordinates, the axle occupies Y=22..27 and Z=-7.25..-3.75, while
the closest bucket part begins at Z=2. A three-dimensional contact check found
zero axle/seat overlaps. The apparent supporting bar stopped at the axle.

The first [Arrow generator](../../../../scripts/generate-content.py) edit added
hidden axle-to-shell rails. Its contact graph passed, but the
[rear view](arrow-back.png) supplied by the user still looked detached because
the opaque red shell occluded both rails. That view invalidates the first visual
claim; it is retained as the before image for this correction.

The revised mesh has two exposed outboard steel posts on the bucket's rear face,
top rails from axle Z=-5 to rear Z=17, and an underseat crossmember tying both
posts into the pan. The posts occupy X=0..1.5 and 14.5..16; the moving shoulder
bars occupy X=2..4 and 12..14, so they remain separate. The rear face extends
one model unit (1/16 block) beyond the old shell at Z=16. These parts belong to
the same rotating child as the seat and axle, so their joints follow the actual
axle at every angle. Seat pivot, rider position and restraint motion are
unchanged. The whole-car Arrow item was regenerated with the visible frame.

`python3 scripts/fd2101-support-audit.py` finds an axle-to-cushion path in both
open and closed exported Arrow meshes (25 reached elements) and now requires
both posts and the crossmember to project beyond the red rear face. Java 21
`createClientLaunchScript createServerLaunchScript -PfdPackaged` passed with
the visible revision and produced JAR SHA-256
`f797b2b858296ef483ab6166e14a1a7ee3cd4e762645ea672958e5cfd06a9091`.
On private display `:338` and the same copied `SkinWorkshop`, the
[rear after view](arrow-rear-exposed-after.png) at the user's camera angle and
[side after view](arrow-side-exposed-after.png) show the complete exposed frame.
The passenger was not mounted in this geometry-only comparison. The existing
[occupied moving view](../../fd917/art/arrow-occupied.md) covers the unchanged
rider/seat/restraint frame; the changed posts stay outboard of the restraint
sweep and rotate in that same child frame. This is a bounded visible-support
check, not a new occupied clearance run for the 1/16-block rear extension.

The user then identified a different floating point in that same rear view:
the middle of the long crossbeam hung above the native cart with open sky
between the beam and the controller. The controller model ended at its local
Y=16, while the wing strut in the block above began at controller-local Y=20.
The exposed bucket posts did not address this cart-to-axle connection.

The first center repair used a single mast, shown in the
[intermediate view](arrow-center-mounted-after.png). The user's question about
a beam on one post prompted a more complete distinction: this long beam is the
*stationary wing strut*, not the rotating seat axle. It now sits on two
controller stanchions and a continuous saddle. The rotating axles are at the
outboard carrier pivots, with their own bearing and crank. The
[final same-angle view](arrow-center-yoke-final.png) shows the two-post center
mount; the earlier [rear view](arrow-rear-exposed-after.png) shows the open gap.
The parent cart carries the yoke; the seat posts and axle rotate together on
the child. No pivot, collision shape or ride logic changed.

The Arrow run root is
`/mnt/NVME4/fourthdimension-runs/fd2101-arrow-20261117`. It retains the copied
world, full client/server logs, packaged artifact, input/console record and
screenshots. After the final comparison, `save-all flush` and clean server
shutdown, the owned client, server and Xvfb units are inactive/MainPID=0; see
[cleanup](arrow-cleanup.txt).
## Shared original-five follow-up

The center controller-to-strut gap was the same ¼-block gap in all five original
families. Each controller model now has twin cart-mounted stanchions and a
saddle meeting the stationary crossbeam. PAX's lead-only receiver mast does
not substitute for this base support on its other three cars. The static audit
passes controller-to-crossbeam contact for Arrow, Legacy, Free Spin, Revised
and PAX.

The child axle sat away from the seat shell in Legacy, Free Spin, Revised and
PAX. Free Spin and PAX now have outboard axle rails, rear posts and underseat
crossmembers; Revised has rails to its existing bearing spines. Free Spin and
Revised rear-row seat models face south while the axle blocks do not rotate,
so their rails overlap the axle after either facing transform. Legacy's two
back-to-back rows instead share long rotating axle-mounted side rails. The
audit checks every seat pan in open/closed states, both sides and both paired
row facings; all pass. Updated whole-car items are generated where the changed
seat geometry appears.

The non-Arrow original families also lacked a visible parent-frame bearing on
the true physical hinge axis. The new
[carrier support renderer](../../../../src/main/java/dev/fourthdimension/client/fd917/OriginalCarrierSupportRenderer.java)
draws a stationary yoke from the real parent strut to a parent bearing, a
rotating child journal, and a child-frame crank to the baked eccentric axle.
Revised's actual pivot is ½ block lower, so its crank rises to the retained
axle height before reaching the bar. Free Spin's existing magnetic annulus
remains a separate feature. These rendered parts follow actual parent and
child poses, not a commanded angle.

Java 21 packaged launch preparation and compilation passed with final JAR
SHA-256 `8489b26075c965d427ef889aba445b3cae68fd9725c9523596788f45f937bb9a`.
One isolated copied world on private display `:338` supplied empty neutral
views for [Arrow](arrow-center-yoke-final.png),
[Legacy](legacy-support-final.png), [Free Spin](free-spin-support-final.png),
[Revised](revised-support-final.png) and [PAX](pax-support-final.png). The
Revised image was later challenged by the user: its center support still had
large visible openings, so it is a **before** view, not a Revised pass. The
rotating pivot hardware follows the real body render frames in source. The
occupied FD917 records remain accepted for
unchanged seat positions, restraints and ride behavior. The new outboard rails
stay outside the moving shoulder bars; the new cranks are on the child and the
bearing races on the parent. This source/neutral-view evidence does not by
itself establish occupied moving visual fit for the new geometry.

## Paired-car center spine and Revised support correction

The user's [close Revised before view](revised-support-final.png) exposed a
failure the earlier contact audit and selected angles missed. The seven
`wing_strut` cells running along the center of each paired car use a transverse
I-beam model whose Z extent is 4..12 model units. Adjacent cells therefore
leave an **8/16-block opening**. The controller joins only the middle cell;
the beams at the two seat rows were visually separated from it. Revised also
places its crossbeams in the block below the spine, where their top face merely
met the spine's bottom face. The visible center support could not be considered
remedied in the earlier screenshot.

An intermediate deep girder, flange strips, side rails and diagonal braces,
then a narrow web and a broad cover all failed the user's straight-on visual
check. The accepted source correction replaces the seven transverse-model
center cells of paired cars with a continuous low box beam and three genuine
transverse intersections. It has no raised center ribs or cover. The saved
RevisedMotion car was upgraded from the old center cells by the idempotent
`PairedSpineMigration` server tick; new paired cars use the same parts directly
in `VehicleFactory`. Revised's lower row beam extends its upper flange one
model unit and positively overlaps the new low beam. The cart controller's
two stanchions and saddle remain. The Revised seat cradle retains its
secondary connection webs and rear crossmember, while the long side rails
are lowered below the restraint hinge tips. These parts follow the parent or
child pose that actually owns them.

The user's further [front close view](revised-center-close-after.png) showed
the large exposed circular pivot ring in front of an inner seat. The
mathematical pivot still lies at that seat's X/Z, but the visible bearing and
child journal now sit 0.80 block **inboard along the same hinge axis**. Their
0.18-block axial width ends at least 0.21 block before the inner seat block;
the child crank runs at 0.60 block inboard and meets the existing axle at its
front edge. No seat location, rotation axis, restraint sweep or vehicle
mechanic moved. For all five original seat models the complete X extent is
within ±0.5 block from the inner seat center; the visible bearing and crank
remain outside that envelope. The parent and child members meet on the
unchanged actual axis.

`python3 scripts/fd2101-support-audit.py` passes all original-family
axle-to-pan paths, both paired-row facings, both restraint states, and the
complete controller-to-row graph for Free Spin and Revised. Legacy has one
transverse chassis row with back-to-back seats, not paired z±3 carrier rows.
The audit checks that each
paired center cell joins its neighbor, no plain spine cell rises above model
Y=5.5, and Revised's lower rows positively overlap it. The Java 21 packaged
client/server launcher build passed for the final renderer with JAR SHA-256
`aa650d9cbf839786ddb94fa36c746e09a24c8db14b00e9e33574a6b8437acf07`.
The user's challenged [old same-angle center view](revised-center-side-clean-final.png)
has repeated raised blocks; the [low-spine same-angle view](revised-center-low-spine-open-final.png)
shows one continuous center support with the repeated blocks removed.
The [empty front](revised-front-clean-final.png) shows the pivot ring outside
the purple inner seat. Prior
[far](revised-center-far-after.png), [close](revised-center-close-after.png)
and [side](revised-center-side-after.png) girder images are intermediate
evidence, **not** the final model.

In the copied `RevisedMotion` world, normal right-click on an F3-identified
Revised seat mounted Dev (`riders=1`). [Occupied open](revised-occupied-clean-open.png)
and [closed](revised-occupied-clean-closed.png) show the rider and restraint
fit. Disclosed validation commands closed restraints and dispatched the car;
server inspection recorded the selected carrier at -2.015 radians, still
`riders=1` with restraints closed. The [occupied non-neutral view](revised-occupied-clean-nonneutral.png)
and [front third-person view](revised-occupied-clean-nonneutral-front.png) show
the bearing outside the rider. The copied short route reached its known
`End of compatible track` fault after ~0.2 block of travel; this bounded
moving fit check does not claim a full route cycle. Capture returned angles
near zero, opening restraints and Shift dismount returned `riders=0`.
The [cleanup record](revised-final-cleanup.txt) records saved worlds and
inactive owned client, server and Xvfb units.

## Revised bearing-to-row regression

The user's later close crop identified a different floating joint that the
center-spine check missed: the circular parent bearing had a short horizontal
stub but no visible connection to its stationary transverse row beam. The
renderer placed that stub at the physical pivot height. The Revised pivot is
below the lower row beam, while the other original families' pivots sit above
their beam flanges. A first repair attempt overshot upward because the
renderer helper adds a half-block plot-center offset; its screenshot was
rejected during the same-angle check and is not acceptance evidence.

The final parent yoke enters the row flange at model Y=2.28, extends inward
to the second transverse cell, then reaches the bearing vertically. Its inner
end is embedded in the stationary beam rather than ending in air. The
[open same-angle view](revised-bearing-yoke-connected-open.png),
[closed view](revised-bearing-yoke-connected-closed.png), and
[close joint view](revised-bearing-yoke-connected-close.png) show that joint
with no raised center ribs. The source audit checks positive containment of
both mount points in the actual Legacy, Free Spin, Revised, and PAX row model
flanges, accounting for the renderer's half-block offset. The renderer change
does not move the hinge axis, seat, restraint, child crank, or rider. The new
parent bracket extends inward from the bearing; its maximum X reaches only
2.295 blocks from the chassis center on a positive-side row, while the inner
seat begins at 2.5 blocks. Rotation about the X hinge cannot close that lateral
clearance. Earlier
occupied non-neutral Revised evidence therefore carries forward for those
unchanged parts; this new check covers the changed stationary joint at rest
in both restraint states. The [cleanup record](revised-bearing-regression-cleanup.txt)
confirms `save-all flush`, open/restored restraints, zero riders and inactive
client, server, and display units.

| Named FD2101 case | Current disposition |
| --- | --- |
| Fixed Inverted frame-to-pan gap | Accepted occupied full-inversion repair above. |
| Arrow axle-to-bucket and cart-to-crossbeam gaps | Static graph and same-angle empty view pass; two-post center yoke supersedes the single mast. Earlier occupied Arrow motion remains accepted for unchanged seating and restraints. |
| Legacy, Free Spin, Revised, PAX shared cart-to-crossbeam and axle-to-seat gaps | Static graph across actual rows/facings passes. Paired cars now use a low continuous center beam, with positive Revised lower-row overlap. The old repeated center blocks are absent in the same-angle Revised views; the outboard triangles are the existing open restraints. |
| Original-five hinge housing and crank presentation, including Revised lap intrusion and bearing-to-row float | Bearing and crank sit inboard of the inner seat X envelope. The parent bearing yoke now embeds in the real row flange. Revised normal boarding, closed restraint, occupied non-neutral rotation, capture, reopen and dismount remain accepted for the unchanged moving components; the final parent connection passes same-angle open/closed visual and model-containment checks. |
| Other twelve assembled interfaces | No additional newly proven defect beyond Fixed Inverted; the first review did not comprehensively clear them. FD2104 structural follow-up owns their broader disposition. |

**The finite FD2101 defect list above is resolved by the retained source and
affected evidence.** The Revised occupied moving
check covers the changed shared hinge rendering at the family with the
reported seat intrusion. Accepted Fixed Inverted inversion, Arrow occupied
motion and unaffected FD917 behavior carry forward. FD2104's broader
reference refinements and FD2102/FD2103 remain separate later packages.

Selective integration for this follow-up: `scripts/generate-content.py`,
`scripts/fd2101-support-audit.py`,
`src/main/java/dev/fourthdimension/client/fd917/OriginalCarrierSupportRenderer.java`,
`src/main/java/dev/fourthdimension/vehicle/VehicleFactory.java`,
`src/main/java/dev/fourthdimension/vehicle/VehicleParts.java`,
`src/main/java/dev/fourthdimension/vehicle/PairedSpineMigration.java`,
the paired-spine blockstate/model JSON files,
the five original controller model JSON files, Arrow/Free Spin/Revised/PAX
open and closed seat models, `legacy_axle.json`, the changed Arrow/Free Spin/
Revised/PAX item models, `revised_strut.json`, and this evidence. The Fixed Inverted generator/assets
and audit portion are the previously accepted same-package repair. All these
paths were integrated alongside the accepted 1.2 package, with distinct scope
and evidence.
The task-owned `fd2101-arrow-20261117` client, server and Xvfb units are all
inactive with `MainPID=0`; its copied world and logs remain at
`/mnt/NVME4/fourthdimension-runs/fd2101-arrow-20261117`.

## Completion audit — 2026-11-22

The [exact integration manifest](integration-manifest.json) records the 35
source/audit/asset paths and the linked acceptance evidence. Its content hashes
identify this handoff while Lead stages the shared integration. There are no
remaining named FD2101 defects.

| Required outcome | Current evidence and conclusion |
| --- | --- |
| Finite evidenced defect list | The case table above includes the user-reported center gaps, raised ribs, seat intrusion and later floating bearing; each has a repair and affected evidence. |
| Supports follow actual moving attachments | The renderer uses parent and child render poses on the real hinge axis. Factory changes only the paired center support cells; seat coordinates, carrier count and physical pivot parameters remain unchanged. Saved paired cars migrate those same cells. |
| Empty and occupied fit at rest and affected motion | Same-angle final open/closed bearing views show the repaired static joint. Retained Fixed Inverted full inversion and Revised occupied rotation prove the affected moving frames. The Revised log records one rider at -2.0149843161117253 radians with restraints closed; inward parent-yoke changes cannot enter the seat's X envelope at any rotary angle. The known copied short-route endpoint remains a disclosed fixture limit. |
| Item/world consistency | Updated miniature items use the corrected seat generator. The paired miniature already has one continuous longitudinal member. All 28 affected exported assets match the retained final visual JAR byte for byte. |
| Validation and provenance | Java 21 build evidence is retained; the four affected Java class files match the final JAR. The current support audit passes using actual Legacy seat facings and PAX's factory-selected `free_spin_axle`. The complete final JAR hash is recorded above and in the cleanup file. |
| Runtime cleanup and handoff | Both owned FD2101 run sets were rechecked inactive with MainPID=0 on 2026-11-22. Worlds/logs remain saved. Source and central status are included in Lead's shared integration; FD2104 refinements remain separate. |
