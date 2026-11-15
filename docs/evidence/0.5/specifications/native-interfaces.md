# Pinned native interface review

Inspected 2026-11-14 America/Phoenix, dispatch source `a4ef96d7` plus preserved
uncommitted work. [API signatures](native-api.txt) and [bytecode](native-bytecode.txt)
were collected with `/usr/lib/jvm/java-21-openjdk/bin/javap` (Java 21.0.13).
No Gradle build, server or client was needed for this read-only interface question.
JAR paths and SHA-256 identities are at the head of each retained listing.

| Measured/inspected input | Finding | Design consequence and limitation |
| --- | --- | --- |
| `build.gradle` | Coasters Curse file `8655742` (0.1.5), Sable 2.0.3, Create 6.0.10-280, NeoForge 21.1.248 | These are inspected interfaces, not a dependency-upgrade decision; FD501 owns that decision. |
| `TrackDesign` constants | Standard rail centers `(±.772,.325)`; rail half-size `.13125 × .075`; wheel radius `.2`; wheel rows `.55`; tread `.625… .9375`; original underbrace `-.70` | Dimensions in blocks. Copying this contact geometry is explicit fit evidence; it does not prove a new rider envelope or make a decorative style a functional provider. |
| `CoasterPathTrackFrame.pathAnchorForSnappedBlock` | Native bearing anchor is sampled graph point plus `.3125 * up`; track basis supplies right/up/forward and bank; native quaternion columns are `(right,up,-forward)` | Every spec uses graph point O; adapters must convert to bearing origin once. Avoid adding .3125 twice. Specs use +Z forward and therefore convert with `Qnative*Ry(π)`; raw native +Z is backward. |
| `CoasterPathEdge` | Fixed straight or Bezier edge, endpoint roll and tangent sign; frame includes vertical/banked paths | Smooth fixed Roller Ball zigzags and Pipeline roll sections are representable. A sharp cusp, instant edge teleport, or moving transfer table is not implied by this API. |
| `CoasterTrackGuideConstraint.updateAfterSnap`, `RailConstraintGeometry` | The native guide constructs a generic constraint from bearing position and track basis. Its interface has no per-wheel gauge parameter | Original side-rail Pipeline can retain the centerline rail solver but needs original contact geometry/render ownership and explicit eligibility. Merely stretching a texture while leaving native wheels visible would fail its contract. |
| `RotaryConstraintConfiguration(pos1,pos2,normal1,normal2)` | Two local pivots and arbitrary normals; bytecode rejects squared length above1.21, but does not validate zero/NaN itself. Add-on must validate finite unit axes | S1/S2 Y-axis, Suspended/Axis Z-axis and Roller Ball/Flying X-axis are representable by the pinned primitive. This is not an assertion that existing add-on persistence/render/camera code is axis-general. |
| `PhysicsConstraintHandle`, rotary and generic handles | Motors on common handle; **setLimit is on GenericConstraintHandle only**; generic configuration can lock the other five DOFs | Capture/damping use a rotary motor; bounded Suspended/Flying uses a generic joint with five DOFs locked and explicit angular limits, or endpoint latch. Do not call a nonexistent rotary setLimit. Axis-aware measurement/restore remains required. |
| Current `CarrierRuntime` | Joint normals, velocity projection, quaternion angle extraction and save restore use local X | Extend all of these together in the first axis consumer. Reusing only the constructor would corrupt Y/Z motion. Nested posture/yaw requires two joints and explicit hierarchy, not quaternion decoration. |
| `NativeLoading` | `.8` native mass units/rider; convention 100 kg/unit; additions at seat-above block; no native passenger mass automatically supplied | Preserve one mass authority. Flying needs a posture-correct load location; do not leave mass in the old seated location or add the same rider twice. |
| `CoasterCartTrainLinkConstraint` | Explicit `linkCarts(..., double)` distance; retained partner identity; restore; tension break at stretch .5 (additional force path .07/1800) | New declared distances are possible, but current add-on `ConsistRules` permits only Arrow/PAX at 6. New family limits must be implemented deliberately. No arbitrary tight-turn train claim follows. |
| `CoasterCartChainLiftDrive.applySpeedMatch` | Tangent speed matching reads Chain span/anchor signed kinetic speed and adds native velocity; the last double is time-step, not requested target speed; `PropulsionInterlock` gates drive | Working standalone longitudinal primitive exists via native Create-powered Chain. S2 uses this path; optional Engineered/FD812 is not mandatory. Spin torque cannot call travel propulsion. Programmed speed/direction must control the actual kinetic source/span through an interlocked governor, not pass speed into the time-step parameter. |
| `VehicleFactory`, `VehicleContract`, `TrackContract` | Original factory assumes lateral wings; contract has family/variant, scalar gear tuple and envelope, original mechanism enum | New IDs, contact layouts, seat poses and qualified envelopes are required. None of the new vehicles can be advertised as already supported. |

## Feasibility disposition

The unresolved **representation** questions are resolved: fixed graph curves,
centerline constraint, arbitrary local rotary normals, generic angular limits, motors and explicit
coupling distances exist in the pinned native inputs. The selected Pipeline has
fixed seated carriers and original side contacts; selected Roller Ball uses a
single bogie and smooth fixed track, avoiding unsupported moving graph operations.
No native API prerequisite is inaccessible. Simulation stability, occupied pose,
new contact visibility and exact curve clearances remain the named future
implementation prototype claims in their package plans. Do not relabel API review
as a successful live prototype, and do not require every future prototype now to
close a package whose canonical criterion is a specification and prototype plan.

Accepted original-family records remain unchanged. Relevant implementation must
consume the lead's integrated FD501A/FD1101A contracts rather than this snapshot's
hard-coded paths. A later dependency change only requires reviewing affected seams.
