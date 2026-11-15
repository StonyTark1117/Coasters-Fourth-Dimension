<a id="fd917"></a>

# Pre-1.0 visual production roadmap

**Canonical package:** [FD917 — Deliver the production visual baseline](../../ROADMAP.md#fd917-deliver-the-production-visual-baseline).
Read current status there and assignments in the current queue.

**Requested:** 2026-11-15, following the model/texture review and controlled-4D linkage investigation.

**Baseline inspected:** `main`, `ff22baa1`, with Create: Coasters Simulated dependency `curse.maven:create-coasters-simulated-1601871:8655742`.

**Outcome:** production models, textures and mechanism presentation for every adopted pre-1.0 vehicle style, recognizable as its intended coaster type and visually at home beside Coasters Simulated.

This is new scope requested after the functionality-focused development work. It
does not declare the earlier goals unsuccessful, reopen FD916, or revoke FD404,
FD503, FD601, FD701, FD813 or FD915. Their accepted behavior and evidence remain
inputs. This roadmap raises the visual target beyond that accepted baseline,
especially by making the connection between the track and moving vehicle parts
visible and mechanically coherent.

The initial review was confined to this file. The follow-up roadmap review on
2026-11-15 adopted it as FD917 and made it a prerequisite of FD1000. Source,
resources, generators and historical evidence are unchanged by that planning
integration. The observations below come from source/assets, the pinned dependency,
retained client images and primary reference material. No new client run was
performed for this plan; proposed dynamic checks are not claimed as passes.

## Placement and authority

Schedule this work before the 1.0 release, with mechanism, dimension and renderer
changes integrated **before FD1000's feature freeze**. A texture-only correction
discovered later follows the normal affected-check policy; it does not restart
qualification.

**Start conditions:** accepted FD915 family/asset/provider contracts and the
relevant dimensioned specifications. `VP01`–`VP09` are ordered procedures within
FD917, not additional permanent FD IDs, independently required run gates or active
assignments. Record only the outcome actually assigned in the
[current queue](../CURRENT-QUALIFICATION.md#current-work-queue), with one owner for
each shared file. The planning adoption does not start implementation or 1.0
qualification. Leave completed packages complete.

**Finish and handoff:** all required production outcomes pass and are integrated
before FD1000. FD904A consumes their visual evidence, FD904B the measured renderer
cost and FD903/FD904C2 any deliberately affected persistence/rider contracts.
FD1002 checks the exported production resources in the release package. During
beta/RC, fix newly found failures of required visual criteria before FD905; a
texture-only correction needs its affected view, while contract/scope changes
require an explicit freeze revision and affected checks. No fresh all-family
campaign is implied.

The [execution-guide responsibilities](README.md#document-responsibilities),
[validation policy](../VALIDATION-POLICY.md) and [AGENTS.md](../../AGENTS.md)
continue to apply. The work packages below describe complete outcomes, not a new
qualification hierarchy or a requirement for a separate run at every step.

### Coverage reconciliation from the roadmap review

| Finding or requirement | Already covered | Remaining ownership |
| --- | --- | --- |
| UV bounds, asset completeness and existing family finish | FD916's accepted finite correction and family-content views remain valid for unchanged assets. | VP01/VP02/VP06 define and deliver the higher production target; an earlier static zero-flags result does not establish it. |
| Arrow control-rail connection | D13 and FD102 establish profile/rail/servo behavior and unwrapped angle semantics. | VP03 measures servo/capture mismatch and supplies the coherent visible follower–transmission–actual-axle connection; a full contact solver is not a prerequisite. |
| Track/wheel materials and lighting | FD904A owns technical rendering qualification; existing checks retain their original limits. | VP04 replaces the inspected debug-quad production paths and measures changed lighting/cost before the freeze. |
| Equipment, locks, restraint transitions and family silhouettes | Owning family packages prove operating authority, seats and adopted motion. | VP01/VP05/VP06 resolve reference choices and produce the newly specified visible interfaces without silently changing those contracts. |
| Items, teaching, Skin and optional seats | FD403, family content, FD910/FD911 and FD915 supply accepted player/adapter paths. | VP07 updates changed representations and checks only affected combinations and fallback. |
| Final acceptance and release | FD901–FD905 and FD1001–FD1003 already own release qualification, documentation and packaging. | VP08/VP09 hand them the integrated FD917 result; required visual defects block their consumer, while additional taste-only work remains optional. |

## 1. The production target

A player should be able to see what carries the train, where a seat assembly
pivots, what connects moving parts, and why the vehicle has the silhouette of its
coaster family. The result should work in a station, in motion and from a rider's
view. A polished inventory icon alone is insufficient.

The visual priorities, in order, are:

1. **Connected machinery:** running wheels, control followers, axles, bearings,
   moving joints, catches and restraint attachments remain connected in motion.
2. **Coaster identity:** seating arrangement, shell, suspension and pivot layout
   distinguish families without relying on their paint color.
3. **Coasters Simulated style:** restrained voxel geometry, compatible scale,
   intentional pixel textures, readable materials and consistent world lighting.
4. **Operational clarity:** restraint, capture, lock, hold and powered states
   correspond to actual authoritative state and are visible where useful.
5. **Complete presentation:** placed, held, inventory, Ponder and optional-provider
   appearances agree with the production vehicles.

The target is an original Minecraft interpretation. It is not an exact engineering
replica, a copy of a manufacturer's themed train, or a replacement for another
mod's artwork. Preserve the adopted vehicle capacities and motion contracts unless
a specific dimension or interface must change to achieve the new target.

### Scope boundary

Included: the original five families, Wing, S1 and S2 Spinning, Suspended, Fixed
Inverted, Axis, Roller Ball, both Flying variants, Pipeline, Dive and Powered
Inverted Yaw: **17 vehicle styles**. Include their authored bodies, seats,
restraints, wheels, pivots, required track, equipment, controllers, tools and item
representations. Include client rendering/state synchronization needed to depict
those mechanisms correctly, plus narrowly necessary mechanical corrections when
the prototype proves that artwork alone cannot achieve continuity.

Preserve the existing 16 Wing colors. A coherent default paint scheme for each
other style is included. New selectable livery systems remain FD1603 work.
Moving-track transfers, switches, tilt tracks and drop tracks remain post-1.0.
New coaster families, new ride operating systems, new optional integrations,
cinematic effects and a new sound-production project are outside this plan.

## 2. Evidence and newly identified gaps

### 2.1 Controlled 4D: the missing connection is more than a texture task

Arrow's original design describes rail-following wheel sets, a sliding rack,
pinion housing and axle connection. Relative displacement of the wheel sets
becomes vehicle rotation; the drawings also account for sliding support and
wheel-truck articulation. These are useful reference principles, not verified
dimensions for every production train.
([Arrow patent, description of Figures 4–7](https://patents.google.com/patent/WO2003022382A2/en))

S&S independently identifies additional rails as the means of controlling its
4th Dimension seat rotation.
([S&S product reference](https://www.s-s.com/ride-pages/4th-dimension-coaster))

The current implementation does the following:

- [TrackDesign](../../src/main/java/dev/fourthdimension/track/TrackDesign.java)
  maps the unwrapped rotation profile to control-rail height. The local rail
  center is `-0.85 + angle / (16π)` blocks high, at lateral offsets `±1.35`.
- [OriginalTrackRenderer](../../src/main/java/dev/fourthdimension/client/OriginalTrackRenderer.java)
  draws those rails in the track's local up/right frame, including banking.
  Track-side tubes joining the rails to the spine are supports, not vehicle
  followers. The height varies **along the track**; the rail itself does not
  animate vertically as the train passes.
- [CarrierRuntime](../../src/main/java/dev/fourthdimension/runtime/CarrierRuntime.java)
  samples the profile near the chassis center and drives a rotary constraint
  through a servo. Capture can instead target a neutral multiple of `2π`.
- [VehicleFactory](../../src/main/java/dev/fourthdimension/vehicle/VehicleFactory.java)
  builds struts and carrier axles, but no dedicated rail-following wheel carriage,
  moving rack or pinion connection to the control rails.

Consequently, the rails and seats share a numerical program, but the train does
not currently show the physical connection that explains that program.

There are three additional design problems to solve before production modeling:

| Problem | Implication for the production design |
| --- | --- |
| A complete revolution produces only `0.125` block of rail displacement. The allowed `±8π` range produces `±0.5` block of stroke. | A literal direct pinion would have radius `1/(16π)` block, about `0.318` model units. A normal-sized decorative gear would not match that motion. Choose a coherent transmission ratio or deliberately revise the rail-height mapping; do not paint teeth onto an incompatible mechanism. |
| The servo's actual angle can differ from the profile target, especially during capture and transients. | Following the rail with one end of a rigid linkage while attaching the other to a separately rendered target pose would conceal a real mismatch. Resolve actual pose, input displacement and operating mode together. |
| A real follower has a location and length; the current control sample is at the chassis center. | Curves, bank, gradients and boundaries can give a forward/rear follower a different height from a center sample. Specify the sampling geometry and articulation before adding multiple contact points. |

These are source-level findings and mathematical consequences. Their dynamic
size in current rides has not been measured in this review. VP03 owns that
measurement and the complete solution.

### 2.2 Other mechanisms need the same end-to-end review

| Area | Observed baseline | Production work and confidence boundary |
| --- | --- | --- |
| PAX lift/catch | [ShuttleRuntime](../../src/main/java/dev/fourthdimension/shuttle/ShuttleRuntime.java) creates a moving native carriage, guide constraint and train catch. The carriage model is a single iron-textured box; the winch is a static cuboid assembly. No dedicated winch/cable/catch animation renderer was found. | Show the catch faces, attachment to the train and carriage guidance, plus a plausible drive path. Determine cable routing from the supported placement rules before promising a continuous rope. A constraint is present; the missing part is its legible physical representation. |
| Restraints | [VehicleVisualState](../../src/main/java/dev/fourthdimension/vehicle/VehicleVisualState.java) projects boolean restraint states to block models; Flying separately projects body and leg states. | Give the restraints hinges, mounting points and travel arcs. Animate transitions where visible, while preserving the server's restraint authority. An animation must never unlock the rider or imply a different dispatch state. |
| Suspension and posture locks | Suspended and Flying expose lock state, but their generated locked/unlocked models largely change a colored part. | Show a plausible latch or stop engaging its receiver at the actual pivot. Keep a state cue as an additional signal. Audit damping and lift locks on Axis and Roller Ball by the same rule. |
| Dive hold | [DiveRuntime](../../src/main/java/dev/fourthdimension/vehicle/DiveRuntime.java) owns hold behavior. The equipment block has a single model variant. | Identify whether the equipment is a remote controller or a physical track device. If depicting a holding jaw, show its receiving surface and actual location; do not put a cosmetic pawl on a remote control box and imply contact. |
| Magnetic rotation | Free Spin and S2 have magnetic behavior; the original track renderer draws simplified magnetic spans. | Model both the relevant trackside equipment and the vehicle reaction surface, with a deliberate noncontact air gap. A magnet should not look like a rubbing brake or unexplained colored rail. S2 launch equipment is a separate interface from its rotation magnets. |
| Powered Inverted | Yaw and powered travel have working runtime paths and governor equipment. The vehicle is visually sparse. | Show distinct yaw-bearing and propulsion roles with an intentional housing/interface. Check the provider's actual drive abstraction before choosing friction wheels, a pickup or gearing. Do not invent a contact drive where the supported system supplies force differently. |
| Running gear and couplers | Much running gear and train linkage belongs to Coasters Simulated; Pipeline substitutes authored side-rail wheels. | Inspect visible native attachment points and keep them exposed/readable. Add only missing authored structure. Avoid duplicate wheels, floating coupler ends or a shell that conceals a required interaction. Treat curved linked trains as an assembly review, not a new train-physics project. |

Sources for this audit include the
[winch models](../../src/main/resources/assets/fourthdimension/models/block/shuttle_winch.json),
[carriage model](../../src/main/resources/assets/fourthdimension/models/block/shuttle_carriage.json),
[Flying runtime](../../src/main/java/dev/fourthdimension/vehicle/FlyingRuntime.java),
[magnetic runtime](../../src/main/java/dev/fourthdimension/magnetic/MagneticRuntime.java),
[Powered Inverted runtime](../../src/main/java/dev/fourthdimension/vehicle/PoweredInvertedYawRuntime.java)
and their owning generators. Entries calling for an audit or a choice are not
claims that existing physical behavior is broken.

### 2.3 Art and rendering gaps

The inspected authored inventory contains **299 models, 66 blockstates and 13
PNG textures**. This is a file count, not a count of unique designs. The files
match the retained [FD916 inventory](../evidence/0.9/visual-quality/asset-inventory.json).
The [accepted FD916 work](../evidence/0.9/visual-quality/README.md) corrected UV
bounds and other declared finish issues. Production art is a further design step.

| Gap | Evidence and consequence |
| --- | --- |
| Family identity relies heavily on color | The original five seat designs share a small cuboid vocabulary, and Wing derives from the Arrow seat. Distinct hardware, shell shapes and seating arrangements need to do more of the recognition work. |
| Flat, generic materials | Many surfaces use vanilla concrete or iron-block textures. The 13 custom sprites largely use procedural borders, corner dots and small noise variations. S2 and Powered Inverted share identical metal/harness/signal pixels. Shared materials are appropriate; identical upholstery, rubber and metal treatment is not a complete material language. |
| Uneven visible texel density | Many generated faces map an entire sprite onto very different dimensions. Being inside the atlas does not guarantee consistent density or sensible placement of bolts, seams and edge wear. Review intent face by face rather than declaring every full-sprite UV invalid. |
| Render-path mismatch | Original track and Pipeline wheels use `RenderType.debugQuads()` and vertex colors. Their emitted vertices do not supply the textured/lightmapped surface data used by ordinary models. Tube shading is baked from face position. This is a source-level lighting/style concern; night, cave and moving-light behavior still needs a targeted client comparison. |
| Incomplete vehicle silhouette | Several styles read as chairs on a structural frame. Production shells, bogie housings and localized brackets should connect the seating to the frame without hiding the mechanism or adding arbitrary bulk. |
| Material seams and support rhythms | Repeated track segments, curves, end joins, control supports and style transitions need consistent texture direction and structural spacing. Uniformly increasing tube sides would not solve these issues. |
| Item/world mismatch | Some original car items use a seat model as the whole car representation; magnetic items use plain cubes. Production items should depict recognizable compact vehicles/equipment with consistent GUI and hand transforms. |

The comparison baseline is the **pinned Coasters Simulated release**, not the
appearance of optional Additions seats or Skin examples. Its compact cart parts,
wheel assemblies, darker worked-metal textures, purposeful edges/fasteners and
textured track form the style reference. Some native track models use OBJ loaders;
zero JSON elements does not mean absent geometry. Native assets remain external.

## 3. Art direction and common contracts

### Coasters Simulated style sheet

VP01 will establish a small side-by-side reference sheet from the pinned native
cart, wheel assembly, bearing and track, together with an addon seat, joint,
equipment block and track section. Record the resource names and dependency
version so a future upstream update does not silently change the target.

Use these production rules:

- Build silhouettes with clean voxel forms and a limited number of angled faces.
  Spend geometry on the outline, hinges, wheel contact and moving interfaces;
  use pixels for seams, shallow fasteners and small wear.
- Use one consistent base density, starting at **16 pixels per block** on normal
  surfaces. A larger atlas is for more surface area or distinct details, not an
  automatic increase in density. Match any intentional exception to the adjacent
  native asset and document it.
- Separate painted metal, exposed structural metal, machined/contact metal,
  rubber, seat padding and restraint padding. Give each a distinct value range
  and surface pattern. Avoid applying the same riveted-border sprite to all six.
- Keep bright family paint as a useful accent. Use darker structure and readable
  wear/contact edges in proportions that sit comfortably beside the base mod.
  Do not turn every family into the same desaturated gray train.
- Put wear where the object would wear and fasteners where parts connect. Avoid
  noisy highlights, fake PBR, photographic textures and tiny detail that disappears
  at normal camera distance.
- Use geometry and position as well as color for locked/open and active/inactive
  cues. Emissive pixels belong to actual indicators, not whole metal assemblies.
- Preserve dark/light contrast under daylight and low light. Solid metal should
  receive world lighting; cutout materials should not require broad translucent
  passes simply to reveal the machinery.

### Geometry, state and ownership contract

Before finalizing each moving assembly, record its parent body, local origin,
rotation axis, attachment points, range, state source and model bounds in its
existing design record. Record whether the part is structural, visual-only,
interactive or supplied by another mod. Keep this concise; no new manifest
framework is required.

Render actual interpolated body/constraint pose for moving seats and their axle.
Use authoritative operation state for latches, restraints and catches. Profile
targets are inputs to mechanisms, not substitutes for actual output pose.
All parts of one connection must use compatible interpolation time and coordinate
frames. Handle entity recreation, reload and missing optional providers without
leaving stale moving parts behind.

Default to visual geometry on existing bodies. Adding a rendered wheel does not
require adding another physics body. Conversely, changing block shapes, native
mass/center of mass, seats, pivots or swept bounds is a mechanical change even
when initiated by art work. Some generators read Java shape arrays directly;
do not assume a new shell can be exported without a shape/clearance review.

Preserve stable item/block IDs, saved vehicle identities, seat ownership and
provider boundaries. [Track render ownership](../../src/main/java/dev/fourthdimension/integration/TrackRenderOwnership.java)
keeps functional running/control rails under their existing owner; optional
Track Styles decoration must not replace or hide them. Skin must retain required
moving/state parts, and optional seats must not acquire a second visual restraint
or duplicate attachment. Optional providers remain optional.

## 4. Family production briefs

The dimensions and movement in the existing [design specifications](../design/0.5/)
are the starting envelope. A necessary change must name the affected contract;
recognizing a reference difference does not automatically authorize changing
capacity, controls or ride mechanics.

| Style | Identity to preserve and strengthen | Planned production treatment / decision |
| --- | --- | --- |
| Arrow controlled 4D | Outboard rotating seats and a four-rail control system. | Complete the follower–slider–transmission–axle assembly first. Build a distinctive seat shell, restraint and compact mechanical housings around that proven layout. Preserve room to see the control-rail connection. |
| Legacy ZacSpin | Compact vertical-coaster vehicle, eight riders in back-to-back groups. | Give the shared rotor, paired backs and supports a coherent compact shell; make the paired seating legible from both sides. Do not use the same Arrow chair with yellow paint as the final identity. |
| Free Spin | Eight riders in four pairs, magnetic interaction and the adopted free rotation. | Distinct paired seat pods, rotor hubs and visible magnetic reaction surfaces. Verify real reference coupling before making a claim of exact manufacturer behavior; preserve the adopted carrier contract. |
| Revised ZacSpin | The mod's adopted revised arrangement and independently moving pairs. | Distinguish the separate pivots and supports from Legacy. Present it as an original inspired variant; do not imply an authenticated Intamin second-generation train specification. |
| PAX shuttle | Compact rotating shuttle cars, up to four cars/16 riders, with physical catch/lift behavior. | Cohesive car structure, visible catch receiver and carriage guidance. Produce a believable winch/drive/catch presentation for the existing route and placement model. Resolve incomplete real-world dimension references in VP01. |
| Wing | Fixed four-seat outboard row, no seat rotation, existing 16 colors. | Purpose-built fixed seat pods and wing brackets, visually distinct from Arrow. Reduce apparent scaffold bulk through shape and material before considering any change to the wide functional envelope. |
| S1 Spinning | Four riders, back-to-back grouping, vertical-axis rotation. | Compact car shell with a readable central turntable/bearing and footwell. The shell should read as a vehicle when empty. Avoid importing S2's launched/inverting hardware. |
| S2 Spinning | Four-seat inverting/launch-capable spinning car, distinct vertical-axis bearing. | Cohesive shell, bearing and distinct launch/rotation interfaces. Prefer a reference-consistent lap-restraint silhouette if it fits the rider contract; explicitly resolve the current shoulder-harness design in VP01 rather than silently changing it during texturing. |
| Suspended | Two-seat hanging cabin with lateral swing. | Make the cabin enclosure, overhead journal, hangers and swing stops coherent. Show where the lock engages, and retain clearance at the adopted swing limit. |
| Fixed Inverted | Four-abreast row rigidly suspended below the track. | Compact overhead structure, recognizable seat backs and open leg space. Prefer the feet-dangling reference silhouette; resolve existing foot-support geometry and its interaction/shape implications explicitly. |
| Axis | Two-seat outboard assembly rolling around a longitudinal axis. | Concentrate detail at its roll journal and connection to the long fork; give the fork an intentional engineered cross-section. Keep longitudinal roll visually distinct from Arrow's transverse rotation and Spinning's yaw. |
| Roller Ball | Compact transverse rocking assembly with the adopted shared carrier motion. | Enclosing seat-back shells, recognizable axle, damping and latch hardware. Do not animate the two sides independently or turn it into a free-spin clone. |
| Conventional Flying | Four-abreast hanging row, station changeover to prone posture. | Purpose-built body and leg restraints, supported pivot, visible posture lock and reduced apparent frame bulk. Check both seated and prone rider silhouettes and intermediate travel. |
| New-generation Flying | Two-abreast vehicle with nested posture and scenery-facing yaw. | Separate the yaw bearing, posture hinge and their housings so both axes read. Use a compact seat/deck shell and independent body/leg restraint cues. Keep the mod's combined motion explicit as its adopted interpretation. |
| Pipeline | Seated TOGO Ultra Twister-inspired vehicle inside side rails; adopted four-seat capacity. | Unified narrow body and side-wheel housings with visible running/side/upstop contacts. Retain the seated configuration; do not model a B&M stand-up Surf train or add a moving-track transfer. |
| Dive | Wide, fixed six-abreast row with a controlled hold/release. | Strong broad-row silhouette, localized support and intended leg openness. Give hold equipment a clear controller/device distinction and show any claimed physical holding interface at the train. |
| Powered Inverted Yaw | Two-abreast upright hanging pod, yaw, powered forward/reverse and show stops. | Cohesive passenger pod, distinct yaw/drive structure and reference-consistent restraint treatment where compatible. Differentiate it from both flying posture variants, including in item view. |

Two especially clear reference discrepancies inform the proposed direction:
MACK describes lap bars for Xtreme Spinning and Inverted Powered, while the current
S2 and Powered Inverted art has shoulder harnesses. B&M describes freely dangling
feet for its Inverted Coaster, while our fixed-inverted art includes foot supports.
These are visual design decisions to resolve, not evidence that the accepted game
restraint or boarding logic failed.
([MACK Xtreme Spinning](https://www.mack-rides.com/products/xtreme-spinning-coaster),
[MACK Inverted Powered](https://www.mack-rides.com/products/inverted-powered-coaster),
[B&M Inverted](https://www.bolliger-mabillard.com/inverted-coaster))

### Reference completion, without speculative replicas

Use the following sources with the existing specifications. Keep a short note
per family distinguishing observed reference features, intentional Minecraft
approximations and unresolved details. Do not infer an internal transmission or
exact dimension from one marketing angle.

| Reference group | Primary source / existing input | Remaining reference task |
| --- | --- | --- |
| Controlled 4D | Arrow patent and S&S product page linked above; current original-family contracts. | Select consistent front/side/underside reference views; establish the simplified connection topology and full stroke before detailed seat art. |
| ZacSpin / Free Spin | [Intamin ZacSpin](https://www.intamin.com/product/zac-spin-coaster/); [S&S Free Spin brochure](https://assets-global.website-files.com/60be9a2c8c73a085920bb514/636c251f0271e50223f23f92_4d%20Free%20Spin%20Coaster.pdf). | Confirm visible groupings, restraints, rotor housing and magnetic surfaces; mark revised ZacSpin as an original variant. |
| PAX | [PAX manufacturer](https://www.pax.ru/), existing PAX specifications and shuttle evidence. | Resolve which published shuttle configuration is the art reference; obtain useful vehicle/catch views before asserting precise fidelity. |
| Wing / Inverted / Dive / Flying | [B&M Wing](https://www.bolliger-mabillard.com/wing-coaster), [Dive](https://www.bolliger-mabillard.com/dive-coaster), [Flying](https://www.bolliger-mabillard.com/flying-coaster), Inverted page above. | Pin the reference generation and its seat/restraint silhouette; avoid combining incompatible generations accidentally. |
| S1 / S2 | [Gerstlauer Spinning](https://www.gerstlauer-rides.de/en/products/roller-coasters/spinning-coaster), MACK Xtreme page above; [S1/S2 specification](../design/0.5/FD1501-spinning.md). | Choose a coherent S1 shell vocabulary; resolve S2 restraint and equipment shape within the adopted contract. |
| Suspended / Axis | [The Bat, operator reference](https://www.sixflags.com/kingsisland/attractions/the-bat); [Suspended](../design/0.5/FD1301-suspended.md) and [Axis](../design/0.5/FD1302-axis.md) specifications. | Confirm suspension/stop layout and Axis journal details from primary material before finalizing hidden or ambiguous parts. |
| Roller Ball | [RES Roller Ball](https://resrides.com/project/roller-ball/); [specification](../design/0.5/FD1502-roller-ball.md). | Preserve the enclosing shell character while retaining the adopted single-carrier motion. |
| New-generation Flying | [Vekoma Flying](https://www.vekoma.com/flying-coasters); [specification](../design/0.5/FD1802S-new-generation-flying.md). | Separate visible seat/posture inspiration from the mod's additional yaw behavior. |
| Pipeline | [TOGO reference report with vehicle diagrams](https://www.mlit.go.jp/common/001281894.pdf); [specification](../design/0.5/FD1401-pipeline.md). | Translate the seated side-rail vehicle to the adopted four-seat scale; do not treat the reference's different capacity as a new gameplay requirement. |
| Powered Inverted | MACK product page above; [specification](../design/0.5/FD1901S-powered-inverted-yaw.md). | Settle the pod/restraint vocabulary and representation of the actual supported propulsion interface. |

## 5. Ordered delivery packages

<a id="vp01"></a>

### VP01 — Establish reference, dimensions and the finite art backlog

**Inputs:** this audit, pinned native resources, retained images, existing family
specifications and FD916 inventory. No fresh functionality campaign is needed.

1. Build the compact style sheet described above and choose primary comparison
   views for each family. Use existing captures when they show the required view.
2. Resolve the explicit reference decisions: Arrow topology/ratio, PAX reference
   and drive routing, S2/Powered Inverted restraints, Fixed Inverted leg space,
   Axis journal and the deliberate non-replica variants.
3. Map every authored resource and runtime mesh to a family or shared component,
   its generator/source, resource owner, relevant states and intended replacement
   or retention. Include item transforms, particles and equipment assets.
4. Record fixed native interfaces and each family's seat/pivot/clearance envelope.
   Mark the small number of proposed dimensional exceptions with affected behavior.
5. Turn this file's findings into one finite implementation list. Distinguish
   required identity/mechanism work from optional decorative refinements.

**Done:** every style and shared asset has a production brief, source of truth
and bounded work item; the above reference decisions have explicit dispositions.
This is a design handoff, not a requirement for another user approval meeting.

<a id="vp02"></a>

### VP02 — Make the asset pipeline and shared materials production-ready

**Depends on:** VP01's scale, material and ownership decisions.

1. Retain editable model/texture sources and deterministic exports. Blockbench
   source or existing procedural geometry can be used where appropriate; choose
   one authoritative source per asset. Existing generators must consume authored
   materials rather than overwrite them with placeholder tile algorithms.
2. Separate geometry/material export from recipe, language and Ponder generation
   where a current combined script would overwrite unrelated content. Keep the
   change limited to safe regeneration of this asset set.
3. Establish reusable metal, paint, rubber, upholstery, restraint and indicator
   regions. Share materials and small hardware, not whole silhouettes between
   mechanically different families.
4. Make UVs follow surface size and intended detail placement. Verify orientations,
   mirroring, repeated track lengths, atlas padding and particle textures. Keep
   the corrected UV bounds from FD916.
5. Add focused export/resource checks for missing references, valid blockstate
   coverage, bounded UVs and required pivots/materials. Do not snapshot every JSON
   face or create tests that merely duplicate the generator's implementation.

**Done:** one seat, structural joint, equipment block and track surface can be
edited and regenerated without drift, with intentional native-compatible
materials. Required runtime files ship in the JAR; authoring sources need not.

<a id="vp03"></a>

### VP03 — Complete controlled-4D mechanical presentation first

**Depends on:** VP01's Arrow envelope. Can use gray materials while VP02 proceeds.

This is the highest-risk package and the first moving production prototype.
Completing only a decorative rack is not its outcome.

1. Make a labeled cross-section and motion model showing the running rails,
   control rails, follower wheels, sliding guide/rack, gearbox and rotating axle.
   Assign each part to the chassis or carrier. Define actual contact points,
   lateral offsets, articulation, stroke and the transmission relationship.
2. Start with the existing profile semantics and native rail gauge. Prefer a
   coherent simplified gearbox that preserves saved angle programs. If the
   existing stroke cannot produce a readable and consistent assembly, revise the
   height mapping deliberately and check its changed support/clearance envelope.
   Do not silently reinterpret saved angles or mirror/reverse behavior.
3. Resolve rail sample versus actual body pose. Measure servo error through normal
   travel and capture. A small, bounded compliant connection may represent a real
   modeled degree of freedom; it cannot excuse arbitrary separation or teeth
   visibly slipping. Where rigid continuity is impossible, implement the bounded
   drive/capture correction needed, retaining rider and recovery authority.
4. Define station capture and recovery explicitly. A neutral seat pose on a
   non-neutral control profile needs a coherent engaged/disengaged mechanism or a
   compatible capture rule. Hiding the linkage during the mismatch is not a pass.
5. Implement the visible assembly from actual interpolated chassis/carrier poses
   and the shared rail geometry. Keep unwrapped winding for rack travel; wrapping
   the angle at `±π` must not teleport a slider. Followers sample the defined
   contact locations, including at section boundaries.
6. Prove the connected gray assembly before final textures. Use a finite route
   covering flat, rising/falling profile, banking/curve, a nonzero smooth join,
   reverse travel and multi-turn stroke. Add the distinct capture/recovery state.
   Include the actual articulated/linked arrangement where its geometry differs.
7. Finish the housings and materials, leaving enough of the working connection
   visible to explain the mechanism at normal inspection distance.

**Required result:** fixed profiled rails; train-mounted followers move relative
to the chassis as it advances; the slider/transmission visibly drives the actual
seat axle. No follower floats beside the rail, no rigid connection splits, and
no decorative angle overrides the rider's real pose.

For modeled contact, use the adopted geometric tolerance of **1/64 block maximum
unintended gap/penetration** at the specified contact surfaces, alongside no
visible detachment in the normal-distance motion view. Intentional roller tread
flattening or an internal gear clearance must be specified separately. This is a
new visual acceptance target, not a retroactive physics tolerance.

A full contact-driven rack/gear physics solver is not required if the existing
constraint architecture can satisfy this outcome. If it cannot, record and scope
the minimum required mechanical change here; do not silently defer the defining
visual connection beyond 1.0.

<a id="vp04"></a>

### VP04 — Unify track and running-gear rendering

**Depends on:** VP02; coordinate the Arrow interfaces with VP03.

1. Replace production uses of debug-quad rendering for authored track/wheels with
   an appropriate textured, world-lit path. Supply correct normals, light and
   material handling; avoid emissive-looking steel in dark surroundings.
2. Preserve native rail fit and functional ownership. Give original track styles
   intentional spine/cross-tie/support forms within the running and control-wheel
   envelope. Vary family structure only where it helps the reference identity.
3. Keep UV direction and scale continuous across curves and section joins.
   Review banked frames, normals, end caps, distant aliasing, culling bounds and
   joins to optional decoration. Cache static geometry rather than rebuilding
   entire routes for wheel or restraint motion.
4. Finish Pipeline's wheel treads, hubs and mounts. Check forward/reverse rotation,
   stops and teleport/reload resets against actual travel. Use the real side-rail
   contacts, not a second decorative native wheel set.
5. Reconcile magnetic spans, track equipment and mount geometry with VP05. Validate
   lighting/material consistency beside a native cart/track in the same scene.

**Done:** authored track and wheels look like part of the same mod family as
native gear, meet their defined contacts, retain valid ownership and have bounded
render cost. No need to rewrite upstream meshes to achieve this.

<a id="vp05"></a>

### VP05 — Complete visible equipment, locks and state transitions

**Depends on:** VP01 mechanism decisions; VP02 materials. Uses VP03's connection
and state conventions, without waiting for unrelated family art.

| Assembly | Concrete deliverable | Required observation |
| --- | --- | --- |
| PAX winch/carriage/catch | Modeled drum/shaft interface, guided carriage, physical catch receiver and meaningful engaged/released shape. Select and implement a drive presentation compatible with winch placement; if a cable is used, it needs defined endpoints, routing and phase-consistent travel. | Lift, release, return capture and a stopped/faulted state show the correct connected/disconnected parts. Drum motion follows the relevant drive state, not a free-running cosmetic clock. |
| Restraints | Hinge-based motion between authored endpoints; separate body/leg assemblies for Flying. Family-specific silhouettes from VP01. | Closed/restained state agrees with authority; opening and closing do not cross the player's head or leave a detached bar. Late join/reload shows the correct endpoint without replaying an unrelated opening. |
| Capture/locks | Paired latch/receiver or stop geometry for the mechanisms that claim one. Existing status colors gain positional meaning. | Locked hardware is engaged only in the matching state and pose; unlocked hardware clears the adopted motion range. |
| Magnetic equipment | Distinct induction/damping forms, track mounts and vehicle reaction surfaces; separate S2 launch treatment. | Equipment visually lines up with the affected assembly through its adopted sweep while maintaining its designed air gap. |
| Dive hold | Clear remote controller appearance; modeled track/train interface if the art presents a physical hold mechanism. | Held, armed/released and unavailable states tell the truth about the existing hold authority. |
| Governors, stations, recovery and yaw markers | Native-compatible housings, identifiable shafts/ports, readable retained/ready/fault cues and appropriate tool/item forms. | Cues reflect existing available state; missing state synchronization is added narrowly. No invented powered/ready signal overrides the actual operating guards. |

**Done:** each claimed physical connection has both ends, and each stateful visual
has an authoritative source. Continuous animation should be client presentation
where possible; any new synchronized state has a defined default, transition and
reload behavior. Functional controls and interaction targets remain usable.

<a id="vp06"></a>

### VP06 — Produce all vehicle families in bounded batches

**Depends on:** the shared art pipeline and relevant mechanism interfaces. Gray
shell exploration can start earlier; final dependent surfaces follow stable joints.

For each batch: build silhouette and joint placement first, fit a rider, check
the movement envelope, author final UVs/textures, then finish state variants and
items. Do not texture a chair that will be replaced when the frame is corrected.

| Batch | Styles | Shared work / special attention |
| --- | --- | --- |
| A — Original rotating vehicles | Arrow, Legacy, Free Spin, Revised, PAX | Replace recolor-led identity with distinct shells and supports; consume the finished Arrow and PAX interfaces. |
| B — Fixed and suspended seating | Wing, Fixed Inverted, Suspended, Dive | Row proportions, open leg space where selected, hanger/cabin identity, fixed versus swinging supports, native wheel attachment. |
| C — Other articulated vehicles | Axis, Roller Ball, Conventional Flying, New-generation Flying | Distinct axes, enclosing shells where appropriate, nested-joint clearance and actual restraint/lock motion. |
| D — Spinning, side-rail and powered vehicles | S1, S2, Pipeline, Powered Inverted Yaw | Vehicle shells, turntables, side-wheel housing, separate propulsion/yaw roles and resolved restraint direction. |

These are production groupings, not required serial gates. A batch can proceed
when its own inputs are stable. One owner at a time controls shared renderer,
state and generator interfaces; family content can be divided only through the
normal assignment process.

**Done per style:** production vehicle and required track/equipment consume the
specified shared materials, have a distinct silhouette, show their real pivots
and connections, and fit their seats and adopted movement envelope. Preserve
every supported state and existing Wing color without demanding separate full
ride runs for shared recolors.

<a id="vp07"></a>

### VP07 — Finish items, teaching and supported appearances

**Depends on:** each affected production model; can finish incrementally.

1. Produce recognizable compact car/track/equipment/tool items and consistent
   GUI, hand, ground and frame transforms. Avoid invisible items, extreme
   clipping and a car icon that shows only an unrelated seat.
2. Update Ponder assets and player-facing illustrations that actually depend on
   changed shapes or connections. Teach control rails and moving followers
   accurately. A schematic teaching scene is not dynamic ride evidence.
3. Preserve Skin material substitution and default fallback while retaining
   required mechanical/state parts. Confirm optional seat providers do not hide
   a required lock, double-render restraints, or shift rider attachment.
4. Confirm Track Styles decoration joins and material boundaries on the shared
   changed path. Keep native/provider assets external; do not copy them into the
   addon to force a style match.
5. Exercise one relevant appearance/seat combination on shared paths, adding a
   family only when its geometry or adapter behavior is different. Reuse the
   existing combined-profile evidence wherever its inputs are unchanged.

**Done:** the production look is complete across player-facing representations
and affected optional configurations, with original standalone fallback intact.

<a id="vp08"></a>

### VP08 — Reconcile visual, mechanical and performance acceptance

**Depends on:** completed changed paths from VP03–VP07. Collect useful evidence
during implementation and reuse it here; this is not a second art campaign.

Use the acceptance table in section 6. Retain a small set of before/after images
and motion captures that demonstrate the specific changes. Reuse existing worlds
and valid functional evidence. The new assets require new views where the old
images no longer show them, not new survival walkthroughs for unchanged controls.

Compare render cost in the same scene/settings before and after the shared
renderer work. Track visible geometry, draw submissions, frame/CPU cost and
memory where relevant; use the existing FD904 performance criteria as limits.
Set per-component budgets from measured native/addon baselines during VP02/VP04,
rather than prescribing an unsupported global polygon number in this document.
If additional geometry consumes the available budget, simplify invisible/internal
detail and batching before weakening a defining contact or silhouette.

**Done:** every required claim passes with adequate evidence, and accepted
unchanged mechanics are carried forward. A remaining required failure keeps this
package open with that specific claim named. No new decorative preference can be
added as a closing gate after these criteria pass.

<a id="vp09"></a>

### VP09 — Integrate the production baseline and hand off to 1.0

**Depends on:** VP08's reconciled result.

1. Integrate the assigned work into `main` under the existing worktree policy.
   Preserve stable resources and record any deliberately changed contracts or
   migration. Keep editable sources and export instructions available.
2. Update only the new package's status, relevant current assignment and affected
   design/teaching documentation. Preserve earlier milestones and raw evidence.
3. Hand FD1000/FD904 the production revision, affected paths, retained evidence,
   targeted new results and known nonblocking preferences. Release qualification
   consumes this result; it does not repeat every family art review.
4. Stop owned clients/servers/displays and verify cleanup under AGENTS.md. Preserve
   useful worlds, images and logs; do not leave idle runtimes as a handoff.

**Done:** the production look and its necessary mechanism presentation are part of
the pre-1.0 integrated deliverable, with no required placeholder or disconnected
defining mechanism deferred as unspecified polish.

## 6. Finite acceptance and verification plan

| Required production criterion | Passing evidence | Smallest affected verification |
| --- | --- | --- |
| Mechanical continuity | Arrow's complete connection and the other claimed contact interfaces remain coherent through their relevant poses; actual seats/riders share the shown output pose. | Gray mechanism motion plus final-material view; numerical contact/pivot checks where new geometry or state interpolation warrants them. Include capture/recovery only where the linkage changes those states. |
| Family recognition | All 17 styles show the arrangement, shell and suspension/pivot characteristics in their production brief. Color is not the only distinction. | Consistent three-quarter/side views at gameplay scale, with front/underside or grayscale views only where they resolve a specific ambiguity. Existing views count for retained assets. |
| Rider/restraint fit | No unintended head/body/leg penetration, floating restraint or detached hinge in seated and required operating poses; boarding interaction remains reachable. | Occupied view of each distinct changed seat/restraint or posture path. Reuse shared inputs for recolors and identical variants. Use a remote occupied view if interpolation/ownership changed. |
| Material and texture finish | Purposeful materials, matched density, valid UVs, no missing textures, distracting stretch, unintended seam or placeholder appearance in declared views. | Export/resource validation and a native/addon side-by-side in normal and low light for each changed material/render path. |
| Track and running gear | Valid contacts, readable supports, continuous material direction and no unintended intersections at joins/banking/style boundaries. | One representative scene per changed structural/render path, plus Arrow's control linkage and Pipeline's distinct side-rail gear. |
| Operational visual truth | Open/closed, locked/free, caught/released, held/running and relevant fault states reflect actual runtime state. | Targeted transition and reload/late-join observations only for newly changed state paths; no replay of unrelated ride operations. |
| Items and teaching | Items identify their placed asset at normal icon size; held transforms and changed teaching scenes remain usable and accurate. | Inventory/held inspection and the affected Ponder scene, combined where convenient. |
| Optional appearance and seats | Required mechanical cues survive substitutions; no duplicate renderer/seat owner; standalone fallback still works. | Shared adapter check plus any distinct new geometry case. Preserve existing provider/profile acceptance for unchanged paths. |
| Functional compatibility | Saved programs, seats, pivots, mass, support fit and interlocks retain their contracts or have a deliberately bounded migration/change. | Diff-based native and connected-client checks only for the affected contract. Texture-only changes carry physics/persistence forward. |
| Performance and lifecycle | New mechanisms and textured meshes remain within the existing performance criteria, cull/cache correctly and clean up after reload/removal. | Comparable scene measurement for shared changed renderers; expand only for a demonstrated hotspot or distinct path. Final release sweep remains in its existing owner. |

Treat the contact tolerance in VP03 as a specific criterion for its new assembly.
Do not apply it retroactively to all historical native contact evidence. Magnetic
air gaps, covers and loose clearances have different intended geometry and must
be judged against their own brief.

Use Java 21 and the project's private host Xvfb procedure for future connected
client checks. Apply the current default 1536M game heap and 6 GiB client-only
scope, distinct persistent game directories and owned displays/ports; check
available resources before launching. Follow the current script and validation
policy rather than copying historical run commands. No run is required merely
because this roadmap was written.

After one targeted pass, close that claim and continue. After two inconclusive
attempts with the same approach, diagnose the setup or measurement before another
launch. A material correction reopens only its affected new or existing claim.

## 7. Dependency order, risks and stopping decisions

The critical path is **reference/dimension decisions → Arrow connection prototype
→ shared rendering/state integration → dependent final art → affected visual
acceptance → pre-freeze handoff**. Asset-pipeline work and independent family
silhouettes can proceed once their own inputs are stable; unrelated unfinished
family art need not block them.

| Risk | Planned decision / response |
| --- | --- |
| A plausible Arrow rack conflicts with the current stroke or servo/capture behavior. | Resolve in VP03 before detailed art. Choose an explicit ratio/mapping and, where necessary, a bounded mechanical correction. A disconnected decorative rack does not satisfy this plan. |
| PAX winch placement does not define a plausible cable route. | Inspect supported placement/binding first. Define a compatible route/drive representation or scope the minimum required placement/interface change; do not draw a rope straight through scenery by assumption. |
| More accurate shells conflict with riders or clearance. | Fit gray geometry to the existing occupied/swept envelope first. Reduce cosmetic bulk where possible; name and test any necessary contract change. |
| A material/render update breaks optional providers or makes rails appear emissive. | Keep ownership explicit and validate the shared changed render path early, before exporting every family. |
| Generators overwrite authored art or drift from exported JSON. | Establish source ownership and deterministic export in VP02, before bulk production. |
| Scope grows into new ride mechanics, liveries or moving track. | Include only the mechanics necessary to make adopted features visibly coherent. Keep unrelated capabilities in their existing post-1.0 packages. |
| Reference material cannot establish a hidden detail. | Use a documented original simplified housing/mechanism consistent with known interfaces. Do not invent a manufacturer specification or block unrelated finished families. |
| Art keeps accumulating subjective refinements. | Close against the finite production briefs and section 6. Extra scratches, bespoke themed liveries and unseen internals remain optional once those requirements pass. |

The first implementation assignment should therefore deliver **VP01/VP02's
shared decisions and VP03's connected Arrow prototype**, with enough of VP04 to
verify it beside native gear. That answers the largest visual-fidelity risk
before investing in final textures across the fleet. Subsequent assignments
should own complete equipment or family outcomes from this plan, rather than
isolated screenshots or an arbitrary number of model files.
