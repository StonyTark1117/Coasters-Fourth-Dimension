# 1.3 finite base refinement and readiness

Activated 2026-11-22 by the human request “Continue 1.3”, from `b5e78f31`
on primary `main`. Coordinator owns this work and its generators/exports.
The reference review and structural follow-up are inputs, not acceptance.

## Frozen refinement targets

| ID | Before → target | Owned source/output | Affected checks |
| --- | --- | --- | --- |
| W1 | Wing's tall flat shell and uniform cushion/paint → stepped shoulder taper, inset contrasting upholstery, and readable fixed mounting shoes. Preserve its four seat anchors, fixed-body ownership, restraint hinges and envelope. | `scripts/generate-content.py`: Wing branch, Wing controller/beam, 32 colored seat outputs, whole-car item. | Assembled controller/beam/shoe/pan connectivity; unchanged cushion contact and restraint sweep; affected empty/occupied open/closed client and item views. |
| I1 | Fixed Inverted's visually dominant rectangular gantry and flat backs → flange/web structural hierarchy, tapered painted backs and separate padding. Preserve repaired rear spars, underpan support and continuous gantry load path. | `scripts/generate-fixed-inverted.py`: seat/frame/controller/item outputs. | Connected assembled frame/pans, occupied envelope and unchanged restraint sweep; affected client and item views. |
| P1 (new defect, 2026-11-22) | Pipeline floor-to-pan and pan-to-shell separations, each 1/16 block → continuous fixed support into all four pans and shells. | Pipeline-only branch of `scripts/fd917_articulated_geometry.py`, eight seat exports; `/root/fd13_structure_reconcile`. | Exported assembled connectivity, unchanged rider contacts/restraints/envelope, affected empty/occupied views. |
| F1 (source review, 2026-11-22) | Conventional/NG Flying posture shaft contacts upholstery without a metal cradle connection → two small child-owned saddles join shaft to existing pan. | Flying/NG Flying shell branches and eight seat exports; structure owner. | Metal-only connection at actual child pivot, unchanged cushions/hinges/envelope; affected occupied upright/prone views. |
| P2 (new defect, 2026-11-22) | S1/S2 stationary controller-to-fixed-race gap → parent-owned annular mounting sleeve, with rotating receiver clearance. | S1/S2 generic mechanism-renderer branch; structure owner. | Controller/race contact, .01 radial clearance from receiver, affected occupied rotation view. |
| P3 (new defect, 2026-11-22) | Axis/Suspended generic fixed brake assembly floats away from valid longitudinal fork bearings → display brake/race assembly placed at the existing front fork/journal interface. | Axis/Suspended generic mechanism-renderer branch; structure owner. | Complete fixed primitive path, coaxial child receiver and shaft contact, unchanged native pivot, affected neutral/roll views. |

W1/I1 are required refinements beyond gap repair. The first-pass optional
suggestions for other styles remain optional: no new mechanism or global
material overhaul is part of this list. New demonstrated defects revise this
record explicitly rather than expanding the aesthetic brief silently.

## Structural reconciliation

The original-five defects and Fixed Inverted repair retain the integrated
[FD2101 record](fd2101/README.md) and support audit. Other twelve architectures
were triaged in the [follow-up](structural-reference-follow-up.md); their
complete interfaces were not qualified by that triage. The completed
[assembled-source reconciliation](base-structure-reconciliation.md) now records
actual attachment paths and bounded dispositions for all twelve entries. It
identified only the finite P1/F1/P2/P3 repairs above; source review is not a new
occupied or all-family motion qualification.

## Readiness

W1 and I1 are accepted against the affected geometry and client checks below.
P1 Pipeline is also accepted ([affected record](pipeline-support/README.md)).
F1 Flying and New-generation Flying saddles are accepted against the
[source audit and occupied upright/transition/prone evidence](flying-support/README.md).
The coordinator independently checked the eight embedded export hashes against
current source, inspected both prone views and verified all three owned units
inactive with MainPID 0. P2 S1/S2 is accepted against
[occupied native yaw and normal controls](spinning-mount/README.md). P3
Axis/Suspended is accepted against [neutral and rotated geometry views](brake-mount/README.md),
measured pivot alignment and analytic clearance across the unchanged sweep.
P3 views are empty-car static fixtures; unchanged rider interfaces and accepted
occupied mechanics carry forward. The coordinator independently inspected the
rotated images and verified both runs inactive with MainPID 0.

**FD2101 finite follow-up and FD2104 base readiness are accepted, 2026-11-22.**
The assembled-source reconciliation and targeted geometry audits are complete. FD2102 implementation starts
against this frozen accepted base. Existing seat positions, camera, track
fit, capacities and propulsion are immutable inputs. Retain 1.2, 1.1 and unaffected
FD917 acceptance.

## W1 / I1 affected evidence — 2026-11-22

The [geometry check](base-refinement/geometry.txt) uses actual exported boxes and
native Wing placements, comparing cushion contact surfaces, outer bounds and
restraint descriptors/meshes with integrated baseline `b5e78f31`. **The baseline
Wing already had a connected path through its arm fairings.** The added shoe is
a clearer fixed mounting detail, not a newly established defect repair. All four
pans remain connected. All sixteen dye choices still select upholstery. The
[retained support audit](base-refinement/retained-supports.txt) also passes the
previous FD2101 repairs, including the Fixed Inverted frame/pan path.

Java 21 packaged private client/server build succeeded ([log](base-refinement/build.log)).
Run owner `fd13-coordinator`, root
`/mnt/NVME4/fourthdimension-runs/fd13-base-20261122`, private display `:341`,
client heap 1536M in a 6 GiB scope, server heap 2048M in a separate 4 GiB scope,
loopback server port 25713. Gradle ran outside those scopes. This copied the
stopped FD2101 SkinWorkshop world; Fixed Inverted was retained, Wing was created
with the disclosed `fd demo wing` setup at source `(500,160,500)`. Setup/aim
teleports are not player construction evidence. Camera turn and reachable vanilla
lever use were verified before mod interaction; default bindings/raw input stayed
unchanged.

Normal empty-hand use boarded each real car; the existing Rotation Tool editor
closed/opened restraints and normal Shift dismounted. The [server log](base-refinement/server.log)
records accepted seat assignment and actions 11/12 for Fixed Inverted
`c6093d81-77f5-4431-b35b-94cb1adf3025` and Wing
`ed6830d3-d18b-4053-8abc-0f502588c6d4`. Inspected views:

- Fixed Inverted [occupied/open](base-refinement/fixed-occupied-open.png) and
  [occupied/closed](base-refinement/fixed-occupied-closed.png).
- Wing [occupied/open](base-refinement/wing-occupied-open.png) and
  [occupied/closed oblique](base-refinement/wing-occupied-closed.png).
- Matching held miniatures: [Wing](base-refinement/wing-item.png),
  [Fixed Inverted](base-refinement/fixed-item.png).

These bounded checks establish the changed stationary geometry/material presentation
and occupied endpoint fit. They are not a new motion, construction or whole-roster
qualification. Unchanged restraint mesh/pivot/sweep and rider anchors reuse accepted
FD917 and FD2101 moving evidence. No new penetration was observed in the changed
surfaces; the overall seat bounds did not grow. The fixed gantry's flange/web
change preserves its outside envelope and repaired spars.

After normal dismount and disconnect, the server saved every dimension and exited;
client, server and Xvfb units are all inactive with MainPID 0
([cleanup](base-refinement/cleanup.txt)). The saved world, raw input/console records,
all screenshots and Fixed Inverted trace remain at the run root. The selected
generator/export inputs are hashed in [inputs.sha256](base-refinement/inputs.sha256).
