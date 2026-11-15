# FD916 finite visual review

Owner: Skin/Visual, chat `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`, primary `main`.
Status: inventory, bounded UV correction and affected client comparisons complete;
FD911 appearance reconciliation remains open. Earlier family acceptance is
retained. This record does not claim that static checks establish visual quality.

## Reference and review method

The adopted [specifications](../../../design/0.5/) and each family's accepted
content record are the visual reference sheet. They fix seat counts, pivot
locations, rail contact, clearance, proportions and intentional Minecraft
approximations. The consistent authored treatment is simple metal structure,
family-colored upholstery, dark padded restraints and legible state cues. Native
Coasters running gear and external provider art stay external. No new livery or
manufacturer-replica goal is introduced.

Use the existing daylight, normal gameplay-scale inventory/held, placed three-
quarter/side and occupied operating views linked below. Geometry/lighting need
not be made identical across past captures: compare the specified component
relationship and finish, not screenshot pixel equality. New UV comparisons use
the same scene and camera before/after the resource change. Shared identical
geometry may share evidence across colors and families.

[Asset inventory](asset-inventory.json) enumerates 378 authored model, blockstate
and texture files, their groups, SHA256 values and local references. Native and
procedural track/body rendering is covered by the associated operating/content
records. Language/Ponder tutorial resources are outside this model/texture
inventory and retain their accepted teaching review. The finite set is the
original five families, Wing, S1/S2, Suspended, Fixed Inverted, Axis, Roller Ball,
Pipeline, Dive, Conventional/New-generation Flying and Powered Inverted Yaw,
plus shared stations, magnetic equipment and tools.

## Coverage reconciliation

The following records contain the accepted silhouette, alignment, texture,
inventory/placed/occupied readability and declared variant reviews. Unchanged
assets and mechanical inputs carry forward; only the named UV issue below is
reopened for the original families and Wing. These are prior observations, not
claims of rerunning every family on the 0.9 artifact.

| Declared group | Reference and accepted comparison views | Current disposition |
| --- | --- | --- |
| Arrow | [Current original teaching](../../0.4/teaching-current/acceptance.md), [Survival Arrow](../../0.4/survival-client-arrow-powered/) and accepted original carrier/track rendering | Seat/axle UV correction passed below |
| Legacy ZacSpin | Same original teaching; established yellow seats and single legacy carrier layout | Shared seat UV correction only |
| Free Spin | Same original teaching; blue seats/free carrier, damping/magnetic equipment and accepted operating poses | Shared seat/axle UV correction only |
| Revised ZacSpin | Same original teaching; purple paired-row geometry and revised strut | Shared seat/axle/strut UV correction only |
| PAX | Same original teaching and retained PAX winch/carriage/occupied capture evidence | Shared seat UV correction only; winch/carriage untouched |
| Wing, 16 cushion colors | [Construction](../../0.5/wing/construction/README.md), [occupied](../../0.5/wing/occupied/README.md) | Shared seat/item beam UV correction passed below |
| S1 Spinning | [Player/content](../../0.6/spinning/player/README.md), [later corrected finish](../../0.6/spinning/uv-correction/README.md) | Reuse corrected seat/bearing, not superseded pre-correction image |
| S2 Spinning | [Finite content review](../../0.8/spinning-s2/content/README.md); operating 00/07/09/11/15 and final empty capture | Accepted unchanged |
| Suspended | [B/C review](../../0.6/suspended/BC-acceptance.md), final placed/inventory/occupied views and corrected physical journal | Accepted unchanged |
| Fixed Inverted | [B/C review](../../0.6/fixed-inverted/BC-acceptance.md), placed row, occupied bank, open/closed harness | Accepted unchanged |
| Axis | [B/C review](../../0.7/axis/BC-acceptance.md), fork/journal placed and occupied roll | Accepted unchanged |
| Roller Ball | [B/C review](../../0.7/roller-ball/BC-acceptance.md), corrected held parent and operating pod | Accepted unchanged |
| Pipeline | [Construction review](../../0.8/pipeline/construction/README.md), corrected P rail-to-post saddle, occupied stand-up/contact views | Accepted unchanged; bespoke P wheel/contact appearance retained |
| Dive | [Complete cumulative review](../../0.8/dive/README.md), bracket-corrected placed row, occupied hold/drop/return, crafted item | Accepted unchanged |
| Conventional Flying | [Content review](../../0.7/conventional-flying/construction/README.md), seated/prone/open-leg/harness operating views | Accepted unchanged |
| New-generation Flying | [Content review](../../0.7/new-generation-flying/construction/README.md), corrected held arms/footlinks and occupied prone/yaw views | Accepted unchanged |
| Powered Inverted Yaw | [Finite review](../../0.8/powered-inverted-yaw/README.md), compact collar/spindle/governor and occupied reverse/show | Accepted unchanged |
| Shared equipment/tools | Original teaching inventory/Ponder and later family station/marker/lock/control views | Boarding indicator and carrier axle UV affected; all other assets unchanged |
| Optional Skin | [FD910](../coaster-skin/README.md) | Industrial/Classic/Antarctica/Hotdog, fixed/free/constrained interfaces, reload, occupied ownership and fallback views passed |
| Optional native seats | [FD911](../rotating-seats/) | Await owner’s selected seat/restraint/occupied appearance handoff |

## One finite correction list

The violated criterion is **Texture finish**: automatic UV coordinates outside a
texture sprite can sample unrelated atlas surfaces. The original Wing
[placed before](../../0.5/wing/construction/placed.png) shows stray orange pixels
on a cyan backrest. The inventory found 696 affected faces in exactly 51 models:
10 original-family open/closed seats, 32 Wing color/state seats, the Wing item,
four station indicator-state models, and four axle/strut models. The exact
[owned list](owned-model-paths.txt) freezes the correction boundary.

Only affected faces receive explicit UVs. Edges within one texture tile retain
their texel density; longer edges fit within the intended tile. Geometry,
rotations, textures, parents, display transforms and blockstates are unchanged.
The [per-file invariant record](uv-corrections.json) includes before/after hashes
and corrected face counts. The generator uses the same bounded mapping so a
future generation does not restore the defect. Unaffected generated outputs
were not rewritten.

`python3 scripts/review-fd916-assets.py` now reports **378 files, zero static
flags** (bounds, UV domain and local references). Client comparisons below close
this correction; no mechanics replay follows from it.

## Affected UV comparisons — passed

Private client launch01 used the upgraded FD501E baseline, daylight,854×480,
GUI scale2 and fixed camera/scene commands. A disclosed comparison resource pack
first loaded the 51 historical model JSONs from the pre-correction integration
HEAD, then the corrected files. No geometry, block state or camera changed
between each pair. The pack’s final files are byte-identical to the corrected
source/JAR resources.

- [Seats and held Wing before](seats-item-before.png) / [after](seats-item-after.png):
  red/cyan backrests lose unintended dark bands; open/closed shoulder-bar shapes
  remain distinct; Wing item beam/seat surfaces use the intended materials.
  Original-five seat geometry is shared; Wing’s16 colors share the same UV map.
- [Equipment before](equipment-before.png) / [after](equipment-after.png): the
  raised revised strut and both negative-Z axle forms lose dark atlas sampling
  and retain continuous iron surfaces. Station indicator/casing stays readable.
  Revised axle shares the Free Spin geometry; the four station-state models share
  the same corrected face mapping. Their color/state logic is unchanged.

The same live client proved camera motion and reachable vanilla crafting-table
right-click with raw input enabled and default mouse bindings. Static gallery
blocks and camera positioning are disclosed setup, not player construction.
One early view setup put the player beyond the platform and caused a fall; the
platform was placed and verified before continuing. This did not exercise a
vehicle feature and does not supply movement acceptance.

The UV correction case is closed. Accepted family occupied-motion and clearance
views carry forward because every mesh/seat/pivot/state definition is unchanged.
No repeated original-family ride or livery matrix is required. FD916 remains
open only for the optional native-seat presentation contribution.

## Skin contribution — passed

The [FD910 connected-client views](../coaster-skin/README.md#connected-client-choices-and-retained-ownership--passed)
show Industrial/Classic on Arrow, Antarctica on Free Spin and Hotdog on Wing.
Provider running gear remains aligned in the native track frame; authored bodies,
carriers and restraint silhouettes remain recognizable. The occupied Wing view
preserves the original rider/restraint relationship. Antarctica persists through
restart/resource reload, and missing resources/provider show intact Original
geometry rather than a missing-texture placeholder. PAX and Pipeline retain their
original purpose-built presentation. The finite mappings contain no new authored
textures and reuse the provider's existing resources. These affected views close
the Skin contribution across the shared fixed/free/constrained interfaces.
