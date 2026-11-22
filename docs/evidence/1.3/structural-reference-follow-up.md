# FD2101 / FD2104 assembled-structure follow-up

2026-11-17, read-only reviewer `/root/model_reference_review`. Numeric findings
use source/assets at `5d385da0` on primary `main`, not concurrent repair edits.
No files, generators or game runtimes were changed by the reviewer.
This supersedes the [first pass](model-reference-review.md)'s implied structural
coverage, while retaining its aesthetic observations as candidates.

## Why the first pass missed the defects

Silhouettes, selected retained views and local mesh contacts did not establish
the assembled structure. Factory placements, blockstate facings and actual
body pivots were not reconciled across the complete chain. Perspective and
unrelated mechanism geometry could hide gaps; prior acceptance wording was not
independent proof of interfaces that the images did not expose.

The chain to establish is native chassis → stationary support frame → stationary
bearing housing → rotating member at the actual pivot → rotating seat support
→ pan/back → restraint hinge. An eccentric bar labelled an axle is not
necessarily at the rotary constraint. Touching boxes alone cannot establish a
coherent bearing, correct moving-body ownership or restraint-state fit.

Source anchors: `VehicleFactory.java` placement/facing, `TrackDesign.java` paired
rows and lowered Revised pivot, `CarrierRuntime.java` constraints,
`scripts/generate-content.py` baked parts, `VehicleMechanismRenderer.java`
family dispatch, `ArrowMechanismRenderer.java` actual-pivot crank, and
`PaxMechanismRenderer.java` conditional receiver mast.

## Original-five baseline findings

| Interface | Source-baseline finding | Qualification |
| --- | --- | --- |
| Controller to parent crossbeam | Controller at `(0,1,0)` ends at chassis-local Y=2; strut in block Y=2 begins at Y=2.25: **¼-block central gap**. | High confidence on assembled baseline; account for procedural additions by family. |
| Parent beam to actual carrier pivot | Beam spans X=-2..3 while carrier pivots sit at X=-2.5,+3.5: ends are **½ block inboard**. | High coordinate confidence; full rendered interface differs by family. |
| Actual pivot to baked axle | Normal child pivot is `(.5,.5,.5)`, Revised `(.5,0,.5)`. Baked bars are eccentric: most forward of Z=.5, Legacy behind it. | High; a joint housing and a crank/receiver are distinct from the eccentric member. |
| Baked axle to fixed shell | Positive minimum separations after factory offsets/facing, listed below. | Baked-geometry measurements, not measurements of every procedural surface. |
| Shell to restraint | Connection must reach fixed shell/frame, not depend on a moving restraint touching it in one state. | Architectural requirement; no additional restraint defect asserted. |

Minimum Euclidean AABB separations between baseline baked axle/collars and fixed
seat-shell elements, in blocks:

| Family | Baseline / architecture | Required interpretation |
| --- | --- | --- |
| Arrow | Stationary parent beam; X-rotating carrier. Dedicated renderer supplies rail mechanism, true-pivot coupling and child-frame crank to eccentric axle. | Reconcile the owner's known bucket and center repairs as a whole path. Keep structural support, crank and rotary bearing distinct; accepted follower mechanics remain valid. |
| Legacy | Two common X rotors, each with north/south back-to-back rows. North gap **0.671875**, south **0.203125**. Generic bearing renderer excludes this family; no equivalent pivot-to-eccentric-bar bridge found. | A coherent common-rotor cradle and pivot housing, not four unrelated supports implying separate joints. |
| Free Spin | Four X rotors at longitudinal Z=±3, parent spine and two crossbeams. North gap **≈0.83048**, south **0.234375**. Child magnetic annulus shaft reaches actual pivot, but connection to eccentric baked bar is not established. | Account for annulus/shaft/spokes separately. Intersection with stationary beam is not alone a bearing design. |
| Revised | Four X rotors at Z=±3; actual pivot lowered ½ block. North gap **≈0.80662**, south **0.234375**. No generic bearing branch covers it. | Preserve lowered independent pair pivots; a copied normal-family crank/hub is incorrectly located. |
| PAX | Two X rotors, shell gap **≈0.74886**. Shuttle catch/drive is chassis/carriage equipment, not rotary seat support. No generic bearing branch covers it. | Lead-only receiver mast runs chassis-local Y=2..3.5 and can mask center gap. Other cars and unbound cars cannot rely on it. Repair the base path without confusing receiver function. |

These findings explain why a good PAX lead screenshot could conceal a shared
base-model defect. They do not independently qualify the owner's current fixes.

## Facing and moving-joint hazards

Free Spin/Revised rear seats face SOUTH, while their baked axle blockstate has no
matching facing rotation. Seat geometry rotates around `(8,8,8)` model units:
`x' = 16-x`, `z' = 16-z`, Y unchanged. A north-seat rail reaching negative-Z
axle geometry reverses toward positive Z in the rear row.

Affected outputs include `models/block/free_spin_seat_{open,closed}.json`,
`revised_seat_{open,closed}.json`, their seat blockstates/articulation records,
and `free_spin_axle.json` / `revised_axle.json`. One intermediate read of
unfinished repairs found north contact but south gaps of 0.171875 and 0.234375
blocks respectively. These were relayed as diagnostic hazards, **not final
repair failures**. Legacy's separate row offsets also need explicit handling.

A rigid bridge from stationary parent to an eccentric rotating point will
separate or intersect during motion. Supports must stay within one rigid body
or meet the actual joint through an intentional bearing/articulated interface.

## Other twelve: triage, not clearance

| Architecture | Explicit interface to reconcile; no new defect asserted |
| --- | --- |
| Wing | Entire seat/beam assembly rigid on chassis. Check native chassis to center beam and overwritten outer beam cells/seat supports. Do not add false rotary hardware. |
| Fixed Inverted | Rigid frame/seats. Preserve accepted pan/spar repair and connected path. |
| Dive | Rigid two mounting rails and six seats: chassis → rails → brackets. Original-five rotary fix does not apply automatically. |
| Pipeline | Rigid cabin with procedural side-rail trucks: include their renderer brackets in the path. |
| Suspended | Longitudinal Z journal: fixed fore/aft fork → rotating shaft/rods → cabin. |
| Axis | Longitudinal Z roll hub on offset fork: fixed race-to-fork and rotating receiver-to-seat/journal endpoints. Ring presence is insufficient. |
| Roller Ball | Shared X rocker, multiple seat groups: long axle-to-group bosses and pans, including both facings. |
| Conventional Flying | Fixed outer fork and child X posture shaft; dedicated `flyingMount` connects endpoints. Inspect shell-to-shaft path; body/leg restraints are separate. |
| New-generation Flying | Chassis → yaw deck → X posture carrier; `flyingMount` uses deck as parent. Account for both joints separately. |
| S1 / S2 | Y turntable, fractional seats: mounting plate → fractional risers. Do not transplant X-axle repair; S2 launch/magnets are separate. |
| Powered Inverted Yaw | Overhead stationary spindle → fixed race → child Y bearing → rear pod supports. Remote propulsion does not support the seats. |

These interfaces remain unverified at the complete-assembly level. This is not
a new set of runtime failures or a blanket pass for the other twelve styles.

## Finite handoff

FD2101 retains the named original-five shared structural defects, with exact
family/output manifests and coherent parent support, true-pivot housing and
rotating seat-support paths. Validate source placements/facings, both sides and
affected motion extrema before targeted visual proof. Preserve accepted repairs
and unaffected physics evidence; do not launch a 17-style runtime campaign.

FD2104 reconciles the structural dispositions and proposed refinements before
freezing variant inputs. Its broader implementation remains planned. The first
pass is not an optional-only readiness list. FD2102 variants remain dependent
on accepted base-model readiness.
