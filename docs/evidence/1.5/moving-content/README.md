# Moving content implementation checks

Owner: `/root/fd13_spinning_check`. Primary `main` checkout, base HEAD
`f85f4a27`; concurrent 1.5 work remains uncommitted. This record combines source/resource verification with the specifically recorded
private player-input checks below. Occupied ride acceptance belongs to root.

## Scope

- Eight dedicated kinetic controller items/blocks and original mechanism badges.
- Eight structural modules: bed, overhead gantry, Pipeline bed and portal brace,
  guide, pivot, lock, socket. Actual native bed placement uses the selected structural module.
- Twenty-two distinct functional insert items, one per supported configuration.
- Thirty-eight survival recipes with deterministic generated exports.
- Server-owned construction preview, exact material bill, rollback/refund, chunk,
  static-world, native-body and entity clearance checks.
- Main construction/operation screen, compatibility list, guide, exact socket list,
  retained nearby server preview, explicit empty-service dispatch button.
- Original guide rails, stays, pivot columns, and races rendered from the real native
  bed pose. Shared geometry also contributes to construction and motion clearance.

## Initial source checks (superseded by final audit below)

The resource audit parsed 53 moving model JSON files. Every authored cuboid stayed
inside vanilla's `[-16,32]` coordinate limits, had strictly positive dimensions, and
belonged to one internally touching element graph. This checks serialization and
internal module connectivity; it does not establish visual continuity between every
module, track, actuator and rider in the running client.

The same audit parsed 37 shaped recipes, checked their 3-by-3 bounds, exact key usage,
and legal result counts. Registry-backed crafting remains a runtime check.

Running `python3 scripts/generate-moving-track.py` again left SHA-256 hashes of all
121 generated resources unchanged: blockstates, models, recipes, loot tables and the
language fragment. The generator writes only its owned `moving_*` exports and
`scripts/generated/moving-track-en_us.json`; central language integration belongs to
root.

Root reported combined-5 Java compile and pure moving tests passed before the final
client usability/setup additions. Those additions require the next coordinated
compile/package; no new native physics claim follows from that earlier compile.

## Source review corrections

The per-tick check uses the actual-to-next pose endpoint hull, conservative rotational
arc padding and one further 0.05-second travel margin. It checks loaded chunks,
static collision shapes, actual world-transformed other native plots and their
entities, and ordinary world entities. It excludes the explicit retained/admitted
body identities and their actual seat-root passengers. Capture supplies the just
inspected body set before native retention is installed.

Linear stays were moved to lateral `±1.4` to contact the overhead flange. Fixed
foundation feet were moved beyond the moving bed ends, from half-length plus two
to plus four, while guide contact stays at plus two. This avoids hiding a real
foundation collision with an indiscriminate owned-block exclusion. Fixed lock/socket
feet have connecting support beams.

The exact pinned Sable 2.0.6 `SubLevel.boundingBox()` implementation returns
`globalBounds`. The existing receiving-reservation checks already use world bounds
and were left unchanged; applying another transform would be incorrect.

## Setup and acceptance ownership

See [the player guide](../../../MOVING-TRACK.md) for normal construction and finite
examples. Validation-only `MovingExamples` commands create disclosed ingredients,
anchors, power, fixed Wing transfer approaches, or an empty native Wing. They never
perform Build, Capture, Move, Release, boarding or restraint controls. Normal GUI
construction and operation must be observed separately. Root owns the early occupied
client run and its exact artifact, world, displays and cleanup. The native owner owns
adapter and production GameTests. This owner subsequently started the isolated content run recorded below.

## Measured ordinary bed saddle correction

After the first occupied-client package froze, source inspection identified a
`0.62109375` block gap between the ordinary bed saddle and native center beam.
The pinned `create-coasters-simulated-1601871-8655742.jar` renderer
`AnchorPeerTrackCurveBerRender` uses rail-model anchor Y `0.24609375` and center-beam
offset `-0.4375`. The center-beam OBJ has minimum Y `0`, without a root transform.
The pinned Create `6.0.11-312` `BezierConnection.iterator` additionally offsets
rendered segment positions by `+0.1875` Y. Together with bed-local raw rail Y `-0.25`,
this places the beam bottom at `-0.25390625`. The original saddle ended at `-0.875`.
An intermediate calculation omitted the iterator offset and produced Y `17.25`;
that provisional pad was superseded before visual acceptance.

The generator and matching `MovingPartBlock` collision/selection shape now extend
only the central `0.375 × 0.375` saddle to model Y `20.25`, or bed-local Y `-0.234375`.
This gives `0.01953125` blocks of positive beam overlap. It is a real shape change,
not a noncolliding cosmetic bridge. The original root and flanges remain unchanged.

The cart plot bounds are not its active collision shape. Pinned `CoasterCartBlock`
implements `BlockSubLevelCollisionShape`; with `SNAP_ENGAGED`, the collider is
`box(0,0.5,0,1,1,1)`. The previous full-block bottom estimate (`-0.3775`) therefore
predicts active collider bottom `+0.1225`, leaving about `0.356875` blocks above the
corrected saddle. This is a source/geometry estimate, not a new observed result.
The native owner verified retention teardown does not clear the snap flag, and
retained bodies are filtered out of the ordinary snap loop. The actual bed/body
retention constraint also disables contacts for its intentional connected pair.
Ordinary snapped entry and release continue using native collision behavior.

Parent's already-running GUI artifact predates this correction. Its player-control
evidence and the prior native retention result are retained; corrected support
appearance requires the next affected view. Any new collision failure must be
checked against this exact saddle change. Local bytecode inspections are preserved
under `/mnt/NVME4/fourthdimension-runs/fd15-content-20261122/inspection/`.

## Small-window teaching panel correction

Root's 854×480 occupied-client materials view exposed background blur applied after
text. Bill, guide and socket screens now override the inherited background pass and
paint their own opaque panel. The guide paginates wrapped text against actual screen
height, including compatibility entries; the bill computes rows from available height.
The main panel's two narrow utility labels were shortened to “Refresh” and “Materials”
and buttons expose their full action labels as tooltips. Source correction is pending
the next coordinated client artifact/view; no screenshot of the old artifact proves
the corrected text.

## Pipeline and overhead mounting planes

The Pipeline family fixture owner identified that the original bed plane `y=-1`
intersected the accepted Pipeline cabin's central stem. Pipeline construction now
uses its distinct three-wide bed at local `y=-4`, below the cabin floor (`FRAME`
block Y `-3`, native shape Y `12..15`). Its new central pad is model
`(5,3,5)..(11,22,11)`, reaching local Y `-3.125`. The original side-rail truss's
spine is centered at sampled rail `-0.1875` plus `-3.0`, with radius `0.22`, so this
pad has positive overlap with that existing structural spine. Matching collision
shape, generator, placement, machinery mount (`-4.05`) and conservative preview
bounds were changed together. The unchanged three-wide X/Z count preserves the
material accounting. Admission's structural plane and targeted actual-shape family
fixture are coordinated with `/root/fd13_structure_reconcile`; the fixture owns
actual native collision evidence. The accepted car geometry is unchanged.

The overhead gantry's downward saddle minimum model Y changed from `10` to `5`.
At bed Y `+1`, this places its bottom at local `0.8125`; the original overhead spine
is centered at `-0.1875 + 0.90`, radius `0.18`, with top `0.8925`. Their overlap is
`0.08`, joining the saddle to that existing upper spine. Matching physical shape
and model use the same coordinates.

A new regeneration audit after these changes again found all 121 resources
byte-identical on repeat, and all 53 model files had bounded, positive cuboids and
one internally connected element graph. Native measurement remains separate.

## World-space passenger obstacle correction

Pinned Sable's `entity_rotations_and_riding.EntityMixin` transforms passengers out
of their seat's plot during `positionRider` and `rideTick`. The obstacle check now
queries the passenger's actual containing sublevel before transforming its bounding
box; world-space passengers keep their world AABB. Plot-local seat entities still
use the native pose. This matches the live player's world position reported by the
occupied-client owner and avoids applying a plot transform twice.


## Pipeline native connectivity repair

The family owner measured 4,896 bed/car collision pairs with zero overlap after the
low-bed correction, but cold restoration lost the two isolated hosted native
anchors. This was a voxel-connectivity failure, not evidence that the accepted
Pipeline cabin needed changing. `MovingConstruction.pipelineEndPortals(length)`
now adds 38 real `PIPELINE_BRACE` blocks: two end portals at Z ±half-length,
outboard uprights at X ±3 from Y -4 through +1, lower links at X ±2, and a header
across X -3 through +3 at Y +1. Corner states add a joining vertical stem; the
headers clear the native cart with underside Y +0.75. Both anchors at Y 0 are
voxel-adjacent to the center header. The low bed, corner beams and uprights share
connected actual collision/selection shapes and rendered geometry. Their exact
cost enters the material bill and refund, and preview includes X ±3.5 / Y -4.5..1.5.

The contract owner's `rigid-4.log` subsequently reports Pipeline 5,232 actual
primitive pairs / maximum overlap 0, Fixed Inverted 2,766 / 0 and Flying Bike
5,532 / 0. The six-family native case completed motion, stopped native-handle
restoration, docking and release. This is that owner's raw native evidence;
no additional overhead shape correction was warranted by the measured result.

Final resource audit: 127 deterministic exports, 56 model JSON files with bounded,
positive, internally connected cuboids, and 38 valid shaped recipes. Raw audit:
`/mnt/NVME4/fourthdimension-runs/fd15-content-20261122/evidence/content-audit-final.txt`.

## Private player content run, 2026-11-22

Owner `/root/fd13_spinning_check`, private Xvfb `:353`, loopback server/RCON
25831/25832, persistent root
`/mnt/NVME4/fourthdimension-runs/fd15-content-20261122`. Java 21, client heap
1536M in 6GiB Minecraft-only scope, separate 2048M server heap / 4GiB scope;
Gradle preparation outside both, unique project cache. First artifact SHA-256
`a9da96c7de4de1797fb70f4e1b239e3dd02839ff6ff1765f7f38e26393a0ce50` includes the
UI background/pagination repairs and low-bed geometry, but predates end portals
and the subsequent full server-message view. No later geometry claim is made
from this artifact.

Disclosed setup commands supplied a platform, crafting table, ingredients, recipe
unlocks and aim/position. The actual craft and controller placement/open used
normal input in survival. Camera turning changed the server yaw, and reachable
right-click opened the vanilla crafting table in that same live client. Default
mouse bindings and raw input remained fixed.

- Bed recipe: normal recipe-book selection/output pickup produced four beams;
  iron 64→58, shafts 16→14.
- Wing insert recipe: produced eight inserts; iron 58→50 and native track 1→0.
- Transfer controller recipe: produced one controller; iron 50→48, beams 4→1,
  one linear chassis, one precision mechanism and two redstone consumed.
- The crafted controller was normally placed at (3,127,2), verified as the actual
  controller block entity, then normally opened. Preview returned a material bill
  and the precise missing-native-anchor denial without consuming materials.
- Materials view was sharp and readable at 854×480 / GUI scale 2, correcting the
  earlier blur. Screenshot `evidence/20261122T215236.811377Z-after.png`.

Raw helper events/readbacks remain in that root. Inventory readbacks are also in
`evidence/server-crafting.log`; representative crafting images end at
`20261122T215053.286357Z-after.png`, placement at `20261122T215118.147471Z-after.png`,
and normal panel opening at `20261122T215124.865076Z-after.png`.
The first server saved every dimension and exited cleanly; client/server units
were inactive before the next artifact preparation. The owned display was kept
only for the continuing final content check.

The first panel view exposed truncated long feedback and raw contact/mount IDs.
The next source adds translated human-readable mounting names, short button labels,
and a paginated Server message view carrying the full authoritative response.
These require the final visual check, recorded below when complete.

## Normal Pipeline construction and hosted renderer repair

Artifact `7d1926f17167c2922bd82518bcf5a7495023caa6fcbddfb529173d0311adaccb`
passed the full server-message view, exact socket list and portal-inclusive bill
at 854×480. Disclosed setup provided only the four listed native anchor blocks
and exactly the server-listed survival ingredients. Normal Validate returned clear;
normal Build consumed all 51 Pipeline beds, 38 braces, eight guides, six locks,
eight sockets and 16 Pipeline inserts, leaving the unrelated crafting inventory
unchanged. The resulting real bed UUID is `9aefa342-25bd-4854-a48f-2af03d02261f`,
configuration Pipeline, actual origin (17.5,127.5,2.5); after settling it reported
DOCKED, locks engaged and zero velocity. This is normal Build proof with disclosed
anchor/material setup, not a claim of manually mining or placing every anchor.
Relevant images: `215646.117993` full refusal, `215656.292382` sockets,
`215706.325065` bill, `215724.769206` validation, `215736.511231` build, all with
prefix `20261122T` and suffix `Z-after.png` in the run evidence directory.

The first external view exposed missing hosted Original rail geometry despite
present native anchors and bed. `OriginalTrackRenderer` culled using plot-space
endpoint coordinates (~20 million), and its cache was not designed to follow a
moving host. Root explicitly assigned that existing file to this owner. The fix
caches plot-local native samples/bases and uses an actual `ClientSubLevel.renderPose`
snapshot for distance, eight-corner frustum bounds, rendered positions, normals and
world light locations. Fixed-world geometry uses the identity path. Hosted beds
use the Original procedural mesh; this does not qualify optional third-party
track-style decoration on moving plots.

Java21 package compilation passed in `package-renderer.log`. On artifact
`7971ada648f09f3940639640dc8c7e616112bdacc8179c2548b6457e7fa2d29d`, the same saved
Pipeline bed at the same view now renders its complete side rails/truss/spine
connected to the low saddles. Compare missing rail image
`20261122T215749.743709Z-state.png` with repaired view
`20261122T220337.728057Z-state.png`. This reuses native motion/retention acceptance;
the changed case is hosted visible geometry only. No new Fourth Dimension model
load errors appeared. Pinned Coasters emits its existing missing variant warnings;
the headless narrator also remains unavailable.


## Final overhead construction, teaching and cleanup

On the same `7971ada6…` artifact, the normal survival panel selected Fixed Inverted,
validated the four disclosed native anchors at (57,127,-6), (57,127,10),
(61,127,-6), (61,127,10), then built the 16-block bed at (57.5,127.5,2.5).
Only anchor placement and providing exact ingredients were setup commands. The
normal Build consumed 51 overhead gantries, eight guides, six locks, eight sockets
and 16 Fixed Inverted inserts. Unrelated inventory remained iron48, shaft14,
ordinary bed1 and Wing inserts8. Image `20261122T220757.732111Z-after.png`
records the normal Build result. After settling, reopening the normal panel showed
DOCKED and measured locks true (`20261122T220836.128760Z-after.png`).

The external overview and side view show the hosted Original overhead rails and
spine supported by the gantry saddles: `20261122T220812.075461Z-state.png` and
`20261122T220825.302892Z-state.png`. Spectator positioning and daylight commands
were disclosed view setup. These views qualify the corrected hosted rendering and
support appearance, not an occupied overhead motion replay. The existing native
family fixture separately established zero intersecting primitive pairs and retained
restore/motion behavior for Pipeline, Fixed Inverted and Flying Bike.

Normal guide and compatibility controls render clear, fully readable text at
854×480, GUI scale2: guide pages1/2 at `20261122T220843.617696Z-after.png` and
`20261122T220850.919505Z-after.png`, translated compatibility at
`20261122T220859.654088Z-after.png`. The Server message, Socket positions and
Materials screens were already observed above; no blurred text remains in those
views. The panel feedback spacing correction is included in this final artifact.

Normal **Dismantle empty assembly** removed only the stopped empty overhead bed
and returned the panel to construction (`20261122T220908.714961Z-after.png`).
Inventory readback returned exactly 51 gantries, eight guides, six locks, eight
sockets and 16 Fixed Inverted inserts, with the unrelated baseline unchanged.
The Pipeline assembly remains in the preserved world. This is a normal successful
empty-disassembly/refund case; occupied refusal authority remains in the native
and root-owned acceptance records.

Final source SHA-256:
- `OriginalTrackRenderer.java`: `8fa4e3b028809904b3e1d8357bfdbb439c77b988778cc3ff18911974d6b80122`
- `MovingScreen.java`: `212711b5954187e53196d54079bef96bdff2fcb257811d4e0838087bd47b4ad5`

The client exited, then RCON stop saved players, sublevels and all three dimensions.
All owned units `fd15-content-20261122-{client,server,xvfb}.service` are inactive with
MainPID0, recorded in `evidence/cleanup-final.txt`. Final logs are
`evidence/server-final.log` and `evidence/client-final.log`; world, prior logs,
screenshots and all artifact manifests remain under the persistent run root.
No runtime was handed off, no source was committed, and all concurrent changes in
primary main remain preserved for root's selective integration.
