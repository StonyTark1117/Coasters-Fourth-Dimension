# FD2104 — base-model reference review

Review date: 2026-11-17. Reviewer: `/root/model_reference_review`; coordinator
reconciliation on primary `main`, HEAD `5d385da0e3e1d5a65a3304cdf710a724783f4014`
with concurrent uncommitted FD2101 assets and 1.2 implementation preserved.
This is planning evidence for the [1.3 guide](../../roadmap/1.3-car-models.md),
not implementation acceptance or permission to begin FD2104/FD2102.

The reviewer inspected retained exterior/occupied views for all 17 styles,
distinctive moving/open views, current Fixed Inverted repair views, generators,
material definitions and item exports. Primary references below informed visual
comparisons. No game model was used as authority; sufficiently precise primary
game references were not needed for the recommendations. No generators, builds,
tests or game runtimes were launched by the reviewer. Its reference browser tab
was closed.

## Findings and limits

### Revision after further user findings

**First reference pass complete; assembled-structure follow-up active.** The
initial pass below is not sufficient to freeze the base-model improvement list
or declare structural coverage complete. The user subsequently distinguished
Arrow's stationary crossbeam-to-cart center gap from its rotating axle-to-bucket
gap. The first pass did not identify that center gap. FD2101 has since added a
central controller mast/saddle, and its owner is investigating the shared
controller/crossbeam construction in Legacy, Free Spin, Revised and PAX.
Shared source is a reason to inspect those assemblies, not yet proof that each
has the same defect under its actual block placement and transforms.

The completed [source-baseline structural follow-up](structural-reference-follow-up.md)
now records the assembled coordinates, family exceptions and facing hazards.
Implementation reconciliation and unresolved interfaces remain open; completion
of that read-only report does not mean base-model acceptance.

The initial review relied too heavily on retained views and local mesh features
for its prioritization. A local contact graph can prove cuboids touch while
missing an unexamined assembly interface, a hidden gap or a mechanically
incoherent joint. Adding a post can close one gap without establishing a
convincing bearing/axle arrangement. The earlier optional-polish recommendations
remain candidates, not a complete or frozen list of necessary base-model work.

The existing reviewer is now tracing the assembled rail/native chassis → parent
support/crossbeam → bearing/housing → rotating axle → seat/pan → restraint paths,
including factory offsets, actual pivots and parent/child ownership. It must
distinguish stationary beams from rotating axles and drive linkages, and check
that support additions do not rigidly bridge a moving joint. Original five-family
shared construction receives the focused review; other styles receive source
architecture triage for analogous patterns. Occluded or unexamined interfaces
remain explicitly unresolved. New defects go to FD2101; design inadequacies
without demonstrated disconnection feed FD2104's finite refinement list.

This follow-up changes review coverage, not unaffected accepted mechanics. It
does not authorize broader implementation or a new all-family runtime campaign.
FD2102 still waits for accepted base-model readiness.

The first pass added **no newly proven disconnection**. Fixed Inverted's known
pan/spar gap has accepted repair evidence. Arrow's separately identified
axle-to-bucket gap remains with the active FD2101 owner; current exterior-support
revisions require their own affected proof. Earlier images or completion wording
do not qualify a subsequent asset revision.

A retained image with no visible defect does not clear every hidden joint. This
review distinguishes an aesthetic recommendation from a fault and does not
reopen unaffected FD917 mechanics, materials or acceptance.

## Complete roster disposition

Each local link is an inspected retained view; motion/other-state views in the
same evidence directory supplement it. Confidence applies to the stated
observation, not comprehensive joint clearance.

| Style | Evidence/reference and observation | Disposition and bounded target |
| --- | --- | --- |
| Arrow | [Neutral assembly](../fd917/art/arrow-interface-neutral.png). Parent/crank/child relationship remains legible; bucket attachment is the separately known gap. | **FD2101 defect, high confidence.** Finish current support repair and retain exposed transmission. Shell taper/padding is optional later polish. |
| Legacy | [Occupied](../fd917/art/legacy-occupied-closed.png); [Intamin gallery photo](https://www.intamin.com/wp-content/uploads/2019/09/MG_0737-1280x1920.jpg) distinguishes compact padded seats, fairings and exposed supports. Flat yellow fronts dominate the mod's paired capsule cheeks. | **Optional refinement, medium.** Break up cheek/crown mass within the envelope; retain back-to-back grouping. Do not copy themed fins. |
| Free Spin | [Closed](../fd917/art/free-spin-occupied-closed.png), [open](../fd917/art/free-spin-occupied-open.png); [S&S brochure](https://assets-global.website-files.com/60be9a2c8c73a085920bb514/636c251f0271e50223f23f92_4d%20Free%20Spin%20Coaster.pdf) supports the paired eight-seat/magnetic identity. Narrow pods distinguish the model, reaction rings dominate its silhouette. | **Retain; optional refinement, medium.** Improve pad/shell separation if assigned. Rings have accepted functional context; do not remove them for photographic similarity. Hidden joints remain uncertain. |
| Revised | [Closed](../fd917/art/revised-occupied-closed.png), [open](../fd917/art/revised-occupied-open.png). Low forks/bearing spines distinguish it, but the front seat silhouette remains close to Free Spin. | **Optional refinement, high for visual similarity.** Emphasize split cradle and separate bearing spine locally. This is an original interpretation, not a manufacturer replica. |
| PAX | [Occupied](../fd917/lead/observer/pax-occupied-closed.png). Boxed shell and receiver distinguish the shuttle; this distance does not reveal every attachment. | **Retain with evidence limit, low for hidden joints.** Preserve receiver reserve and remote-drive treatment. No demonstrated redesign prerequisite. |
| Wing | [Day](../fd917/art/wing-occupied-day.png), [open](../fd917/art/wing-occupied-open.png); [B&M Wing](https://www.bolliger-mabillard.com/wing-coaster). Tall narrow head fairing and uniform teal base look upright/chair-like compared with sculpted seat masses and tapered fairings around open legs. | **Priority base refinement candidate, high.** Compact shell taper, clearer upholstery and explicit fixed pedestal-to-wing junction; retain four seats, fixed mounting and anchors. |
| Fixed Inverted | [Repaired frame](fd2101/empty-front.png), [occupied](fd2101/occupied-open.png); [B&M Inverted](https://www.bolliger-mabillard.com/inverted-coaster). Corrected load path is visible, but tall rectangular rear gantry dominates. | **Accepted defect repair plus priority refinement candidate, high.** Clarify structural versus padded surfaces and reduce visual dominance of the gantry. Preserve corrected spars/rear rail; an open-frame variant must reveal, not delete, them. |
| Suspended | [Occupied](../fd917/art/suspended-occupied-closed.png); [The Bat operator page](https://www.sixflags.com/kingsisland/attractions/the-bat). Cabin panels, journal and hangers convey swinging cabin identity. | **Retain; optional refinement, medium.** Nose/side-panel shaping only if assigned; preserve doorway, rods and lock receiver. Do not substitute inverted chairs. |
| Dive | [Occupied](../fd917/art/dive-occupied-closed.png); [B&M Dive](https://www.bolliger-mabillard.com/dive-coaster). Six-wide floorless row reads clearly; flat backs/bars visually outweigh buckets. | **Optional refinement, medium.** Shoulder/head-pad shaping and bracket hierarchy; keep six seats and floorless opening. |
| Axis | [Closed](../fd917/lead/families/axis-occupied-closed.png), [roll](../fd917/lead/families/axis-occupied-roll.png). Longitudinal fork/journal differs clearly from transverse Free Spin. | **Retain with unresolved visibility.** Preserve readable roll hub. These views cannot clear all fork-to-seat interfaces; inconsistent manufacturer access prevented a precise reference claim. |
| Roller Ball | [Closed](../fd917/lead/families/roller-ball-occupied-closed.png), [swing](../fd917/lead/families/roller-ball-occupied-swing.png). High bucket and pronounced foot-tray stem provide identity. | **Retain, low reference confidence.** Foot-tray softening is only a possible aesthetic option; no primary close-up establishes a required correction. |
| Conventional Flying | [Prone](../fd917/mechanisms/flying-occupied-prone.png), [sweep](../fd917/mechanisms/flying-occupied-posture-sweep.png); [B&M Flying](https://www.bolliger-mabillard.com/flying-coaster). Rectangular perimeter frame dominates the prone silhouette compared with visible rider/body support. | **Optional refinement, medium.** Section/finish treatment could reduce frame dominance while retaining hinge/support paths. Geometry changes have posture-sweep risk. |
| New-generation Flying | [Prone](../fd917/mechanisms/ngf-occupied-prone.png), [changeover](../fd917/mechanisms/ngf-occupied-changeover.png); [Vekoma Flying](https://www.vekoma.com/flying-coasters). Yaw housing and posture hinge remain distinct; seated-to-prone transformation is a reference principle. | **Retain; optional refinement, medium.** Pad segmentation only if selected. Preserve explicit nested axes; do not claim a replica. |
| S1 | [Occupied](../fd917/mechanisms/s1-occupied-closed.png); [MACK Spinning](https://www.mack-rides.com/products/spinning-coaster). Compact body/footwell and lap bar read clearly. | **Retain; item polish candidate, medium.** Preserve rounded/bumpered family-car identity distinct from S2. |
| S2 | [Occupied](../fd917/mechanisms/s2-occupied-closed.png); [MACK Xtreme gallery](https://www.mack-rides.com/products/xtreme-spinning-coaster). Lap bars suit the type; boxy nose understates the separate inverting pod identity. | **Optional refinement, medium.** Stronger low nose/pod-cheek cues, retaining bearing and launch/reaction interfaces; differentiate its item too. |
| Pipeline | [Contacts](../fd917/mechanisms/pipeline-contacts-day.png). Connected side-rail trucks and world-lit materials carry identity; occupied curve view is substantially text-occluded. | **Retain with reference limit.** Keep TOGO-inspired seated side-rail design. B&M stand-up Surf imagery is not an appropriate substitute. No new specific defect established. |
| Powered Inverted Yaw | [Closed](../fd917/lead/families/powered-occupied-closed.png), [yaw](../fd917/lead/families/powered-occupied-yaw.png); [MACK Powered Inverted](https://www.mack-rides.com/products/inverted-powered-coaster). Spindle, lap bar and cabin floor read clearly. | **Optional refinement, medium.** Integrate floor/rear supports into pod silhouette. Retain truthful Chain propulsion; no invented drive wheels. |

## Finite planning list and order

The reviewer recommended focusing design effort on Wing and Fixed Inverted,
with other families individually selectable rather than a mandatory roster
redesign. The adopted roadmap additionally requires agreed **base-model** work
before variants: the reviewer's suggestion to express improvements directly in
variants does not supersede that user-requested dependency.

1. **FD2101:** finish the known Arrow attachment revision; retain accepted Fixed
   Inverted repair. Reconcile defect wording without claiming unseen joints were
   comprehensively cleared.
2. **FD2104 proposed priority W1:** refine Wing's base shell taper and cushion/paint
   hierarchy; make its fixed pedestal junction readable. Freeze existing seat
   anchors, restraint pivots, chassis interface and envelope before edits.
3. **FD2104 proposed priority I1:** refine Fixed Inverted's base structural/padded
   hierarchy and gantry presentation, retaining the repaired load path. Freeze
   the same contracts before deriving its open-frame choice.
4. **Optional, separately assigned candidates:** Conventional Flying frame
   section/finish treatment; S1/S2 item/world differentiation; family-specific
   shoulder padding; then the bounded family suggestions above. These are not
   new acceptance blockers and do not authorize implementation.
5. Finalize the finite selected base-work list, exact generator/output ownership
   and before/after targets when FD2104 implementation is assigned. Complete that
   list, freeze the resulting bases, then implement the two FD2102 choices.

Source cross-checks support two optional cross-family changes:

- `scripts/generate-content.py` uses the same `shoulder()` geometry for Arrow,
  Legacy, Free Spin, Revised, PAX and Wing. Family padding/edge treatment could
  distinguish fronts, but changing profiles requires head-clearance/open-sweep
  checks where affected.
- `scripts/fd917_articulated_geometry.py` distinguishes S1/S2 world panels/noses
  while sharing their miniature geometry branch with color differences. Add
  simplified respective nose/body cues if assigned. Accepted schematic items are
  not retroactively failed.

`scripts/fd917_materials.py` already provides coherent paint, structural metal,
machined-contact, upholstery and rubber bindings. Local material hierarchy is
the proposed improvement; no global texture overhaul is justified here.

## Affected verification only

For an altered support, inspect connected parts/source bounds and affected empty
and occupied open/closed views, plus only the touched moving sweep. Compare
affected items/previews. Enlargements near riders, track, adjoining cars or
restraints require an explicit envelope-risk check. Preserve accepted route,
linkage, camera and lifecycle evidence when their inputs are unchanged. New
variants still consume their planned selection/persistence/fallback checks.
This reference review does not require a 17-style runtime campaign.
