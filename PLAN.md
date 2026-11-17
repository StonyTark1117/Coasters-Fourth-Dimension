# Coasters: Fourth Dimension addon plan

The expanded roadmap was adopted **2026-11-11 (America/Phoenix)**. Stable 1.0
now includes all vehicle and integration scope formerly proposed through 1.5.
[ROADMAP.md](ROADMAP.md) owns permanent FD IDs, prerequisites, status and acceptance;
D01–D18 retain their stable design references; D19 defines the additional vehicle
styles and supporting tools, and D20 the post-1.0 moving-track program.
All adopted vehicle/cart and seat-motion styles deliver before 1.0. Moving-track
mechanisms deliver after 1.0; builder tools and multi-train management retain
their independent later allocations. A planning milestone or a
new artifact hash is not proof of implementation or a reason to reopen acceptance.

Foundation through bounded 0.3, FD401A–D and FD402A/C/D retain their accepted
results. The 0.4 cases are closed; prove extensions through Wing, Spinning
and Suspended before Axis/Roller Ball and the remaining content. Operations
adapters can advance after their interfaces; presentation/seat integrations follow
stable mechanisms. FD404 is the five-family development checkpoint; FD1000 is
the expanded 1.0 alpha freeze, followed by FD901–FD905 and FD1001–FD1003.

The **2026-11-15** [visual production plan](docs/roadmap/pre-1.0-visual-production.md)
adds FD917 before that freeze: production art and connected mechanism presentation
for the 17 adopted styles. Milestones through 0.9, including FD916, retain their
accepted baseline. FD917 completed on 2026-11-15 with its
[integrated production handoff](docs/evidence/fd917/README.md), targeted checks
and verified runtime cleanup. The [1.0 alpha freeze](docs/evidence/1.0/lead/freeze.md)
is complete, as are FD901–FD905 final qualification and FD1001–FD1003 release
readiness. The verified 1.0.0 file was separately authorized and promoted to
[Beta on both platforms](docs/publication/1.0.0-beta/README.md) on 2026-11-16.
Only the [stable publication actions](ROADMAP.md#remaining-steps-from-beta-to-full-100)
remain; there is no open 1.0 implementation or qualification assignment.

The [integration backlog](docs/INTEGRATION-BACKLOG.md) owns the dated artifact
inventory and adopted allocation. Its November 2 catalog and the initial
September 30 research remain historical inspections, not claims about latest
releases. Review dependencies through FD501 before activating expansion profiles.
The archived schema-7/protocol-8 registry is not automatically part of the build;
FD501 records core and per-adapter decisions. FD501A owns core-required changes;
FD501B–H own adapter-specific prerequisites without blocking unrelated families.

The active 0.8 implementation has qualified ride-local authority (FD1701A):
[shared handoff](docs/evidence/0.8/authority/README.md). Schema 8 retains controller
identity and requires explicit restoration; schemas 1–7 remain readable. Pipeline
and S2 are integrated implementation checkpoints with their remaining acceptance
owned in the current queue. Dive, Powered Inverted and optional operations adapters
continue under that queue; this does not close the whole milestone.

The [qualification record](docs/CURRENT-QUALIFICATION.md) retains actual run
identities. [VALIDATION-POLICY.md](docs/VALIDATION-POLICY.md) governs cumulative
acceptance and targeted retesting. New designs need specifications and working
vehicles; discovery alone does not complete a family. The
[1.0 supported envelope](ROADMAP.md#supported-envelope-and-explicit-10-dispositions)
retains current coordinate/capacity limits unless separately revised with evidence.
This planning adoption changes no runtime artifact, supported-version claim or
publication authorization.

Build on Create: Coasters Simulated's graph, physics and linking where suitable.
Ship original functional vehicles and required tracks, with distinct controlled,
free, magnet-assisted, fixed-seat, swing and shuttle behavior. Preserve the
original Arrow/S&S, ZacSpin, S&S Free Spin and PAX goals while implementing the
agreed new families below. Optional addons remain optional for standalone rides;
Track Styles' future functional-track availability must not block release.

## D01 Ride families and product scope

These are mechanically distinct vehicle families, not merely alternate textures. Seating topology, pivot placement, permitted attachments, train linking, and boarding alignment belong to the vehicle definition. Dimensions and coupling details require reference drawings or prototype measurements before final models.

| Family | Reference evidence | Planned implementation |
| --- | --- | --- |
| Arrow/S&S controlled 4D | S&S confirms rotation controlled through additional rails and exposed wing seats. [Manufacturer](https://www.s-s.com/ride-pages/4th-dimension-coaster) | Linked cars, paired wings, and a position-driven axle profile. Preserve the original four-seat starting car and X2/Eejanaika/Dinoconda inspiration. |
| Intamin ZacSpin first generation | Intamin lists Kirnu, Inferno, Insane, and Green Lantern as references, with variable flipping, alternate seating, and chain or LSM propulsion. [Manufacturer](https://www.intamin.com/product/zac-spin-coaster/) | Compact individual vehicles with back-to-back seating and a legacy offset-pivot carrier preset. Free inertial rotation; finalize exact seat count and mechanical grouping from visual references. |
| S&S 4D Free Spin | S&S confirms magnetic spin influence and variation with loading. Its brochure specifies eight riders in pairs, four on each side. [Manufacturer](https://www.s-s.com/ride-pages/4d-free-spin-coaster), [brochure](https://assets-global.website-files.com/60be9a2c8c73a085920bb514/636c251f0271e50223f23f92_4d%20Free%20Spin%20Coaster.pdf) | An eight-seat vehicle with separate rotating carrier groups and adjustable magnetic spin sections. Confirm physical coupling before fixing how many pairs share each joint. |
| Intamin ZacSpin second generation | The supplied reference identifies an updated concept. The primary Intamin material inspected does not establish its exact generation-specific pivot and coupling arrangement. | A planned independently rotating carrier variant with a pivot nearer the riders, implemented as a proposed preset pending primary-source confirmation. Do not claim an exact commercial replica or identify a newer installation as this generation without evidence. |
| PAX Bumerang | PAX/MIR describes freely rotating seats, a winch-lifted train, release, and backward return through the station. [Manufacturer brochure](https://www.pax.ru/upload/Бумеранг%20–%20две%20петли%2040м.pdf) | A linked free-spin shuttle train, lift catch/release equipment, and return/capture operation. Plan compact and taller layout examples, not separate physics engines. |

Keep independent carrier groups in the architecture from the start. A controlled car can couple groups to one profile; free-spin vehicles can carry different angular states. Do not force every family into the original shared four-seat axle design.

These original five families retain their accepted bounded implementations;
unverified historical fidelity is not claimed. Stable 1.0 also includes the
following adopted family packages. Discovery for all six occurs in 0.5, with
implementation ordered by shared mechanics. The specification chooses actual
axes, seats, dimensions and track contracts; release labels do not settle them.

| Added family package | Delivery milestone | Required behavior / specification boundary |
| --- | --- | --- |
| Fixed-seat Wing / FD1101A–C | 0.5 | Fixed passenger seats, measured ordinary-track fit, restraints, coupling and station workflow; no inherited 4D rotation. |
| Spinning / FD1501A–C, D–F | 0.6 S1; 0.8 S2 | S1 conventional free-spinning; S2 launched/inverting free-spinning with magnetic influence. Each has one specified configuration and distinct loading/damping/capture contracts where required. |
| Suspended / FD1301A–C | 0.6 | Overhead running gear, lateral swing, declared limits/damping and safe lift/station locks. |
| Axis / FD1302A–C | 0.7 | Explicit rotation-axis/mode, capture, propulsion and clearance behavior distinct from Suspended. |
| Roller Ball / FD1502A–C | 0.7 | Its own vehicle/route, passenger mechanism, lift/return transitions and retention/capture. |
| Pipeline / FD1401A–C | 0.8 | Chosen Pipeline interpretation, contact geometry, vehicle/posture/restraints and station workflow. |

An integration adapter does not implement a vehicle family. Ordinary carts retain
the compatibility rules below. New requested features cannot be silently dropped
to accelerate release; unresolved scope decisions belong to their 0.5 specifications.
For S2, 0.5 must identify a working standalone propulsion path and its declared
configuration; FD812 is an optional alternative. Scenery-directed yaw is a
required 1.0 mechanism. Additional families are Fixed Inverted (0.6), Conventional
and New-generation Flying (both 0.7), Dive and Powered Inverted Yaw (both 0.8).
The New-generation Flying sub-variant includes scenery-facing yaw in seated and
prone postures; Powered Inverted Yaw keeps upright seating. Their specification
packages FD1601S/FD1702S/FD1801S/FD1802S/FD1901S close during 0.5; D19 sets the boundaries.

## D02 Cart and track compatibility

User requirement: every 4D cart requires its matching functional track family. An Arrow 4D cart requires Arrow 4D track; equal running gauge alone does not permit it on ordinary track or another 4D family. Apply the same rule to ZacSpin, S&S Free Spin, and PAX variants. A track from an external provider can qualify as that same family only through a verified functional adapter.

Compatibility is asymmetric. An ordinary non-4D cart may use a 4D track if its existing running gear or fitted running-gear adapter physically matches the main rails and clears the extra structure. It remains an ordinary cart and receives no seat-rotation profile, free-spin carrier, or magnetic seat torque. This supports inspection or recovery work without turning track contact into an automatic vehicle upgrade.

| Cart | Track | Planned result |
| --- | --- | --- |
| Arrow controlled 4D | Functional Arrow 4D, ours or a verified equivalent provider | Allow when running gear, control-rail engagement, and clearance match; enable the cart’s supported controlled rotation. |
| Arrow controlled 4D | Ordinary, ZacSpin, S&S Free Spin, or PAX track | Reject, even if the main rail spacing matches or the seats are locked. |
| Another 4D family | Its corresponding functional family/variant | Allow only when that vehicle definition’s track contract is satisfied. Shared parts or a shared manufacturer do not make variants automatically interchangeable. |
| Another 4D family | Ordinary track or an unrelated 4D family | Reject. Do not downgrade a 4D vehicle to an ordinary cart to bypass the restriction. |
| Ordinary non-4D cart | Any 4D track whose main rails and surrounding structure fit its running gear | Allow ordinary rail travel only; no 4D effects. |
| Ordinary non-4D cart | Any 4D track with incompatible rail contacts, gauge, or clearance | Reject. Do not resize or reposition the cart automatically to make it fit. |
| Any 4D cart | Ordinary track with only a matching decorative style applied | Reject until an explicit functional track conversion installs the required family geometry and mechanisms. |

Separate three decisions: whether the main running gear fits, whether the vehicle is permitted on that track family, and which mechanisms may interact with it. A normal recovery cart can pass the first two without having any 4D capabilities. A 4D cart cannot pass family eligibility merely because its main wheels fit.

Track definitions must expose a functional family/variant, main-rail contact geometry, supported running-gear profiles, required mechanical interfaces, and attachment/clearance envelopes. Vehicle definitions must expose their role (ordinary access, fixed-seat vehicle or mechanism-bearing family), actual running-gear profile, required track contract, and supported mechanisms. Include running, side-guide, and upstop wheel contacts where applicable; gauge alone is insufficient. Fit tolerances and supported adapter dimensions must be measured and tested, not inferred from a style label. Unknown external combinations remain unsupported until registered and verified.

Gate placement, assembly, snapping/re-engagement, dispatch, route transitions, and save/load rebinding on the server. Check every vehicle in a linked consist independently. Reject an incompatible placement or planned connection with an actionable reason such as “Arrow 4D cart requires Arrow 4D track” or “Cart running gear does not clear the control rails.” Prevent planned operation across an incompatible boundary; detect upcoming transitions with enough braking distance rather than discovering a mismatch only after crossing it. Track edits while occupied remain prohibited. Unexpected support loss enters recovery behavior, never automatic conversion, teleportation, or snapping onto an unrelated nearby track.

Track geometry may share the existing Coasters graph internally, but eligibility must be enforced before its generic cart snapper can accept a mismatched vehicle. Prototype these hooks early. If metadata on the base material cannot enforce the rule reliably, use distinct functional materials or track types while retaining graph reuse.

Fixed-seat Wing extends the role/seat contract through a concrete working vehicle.
It must not be forced to declare rotary clearance or an Arrow profile merely
because the current implementation distinguishes ordinary and rotating roles.
The existing 4D family restrictions remain intact; each new family's ordinary-track
fit versus dedicated-family requirement follows its explicit specification.

## D03 Recovery cart use

An ordinary cart that physically fits may traverse the main rails of a 4D route for inspection or recovery. Ordinary longitudinal lifts, motors, or brakes may still work when their interfaces match. Rotation equipment must ignore carts without the relevant 4D mechanism; passing a magnetic spin zone must not rotate the whole ordinary cart or grant it a carrier.

Physical access does not imply towing compatibility. A proposed recovery consist additionally needs compatible couplers, offsets, strength, braking, and clearance for every vehicle. Establish this in a recovery prototype; do not claim stock carts can tow every 4D train. A disabled 4D car being towed must remain on its own matching track. Secure its seat carriers using the supported maintenance lock or retention mechanism before towing, and ensure any controlled linkage remains mechanically consistent. A lock does not authorize travel on ordinary or wrong-family track.

The first release should verify at least one ordinary-cart/main-rail fit case and a rejected non-fit case. A documented towing example follows only if coupling and loaded movement are proven. Recovery does not require adding a new maintenance vehicle if an existing compatible cart suffices.

## D04 Relationship with Track Styles

Ship a complete original Arrow/S&S-style four-rail track with the first controlled vehicle, including its spine, ties, running rails, and functional control-rail representation. Supply compatible original track geometry for each additional ride family as it ships, sharing components where suitable. These are supported standalone tracks, not temporary placeholders. Keep their saved identifiers stable even after Track Styles releases Arrow 4D.

Offer Track Styles as an optional appearance and track integration. Our distinctive scope is the working mechanisms and multiple 4D families; overlap in essential track geometry is acceptable so players can build complete rides immediately. Keep the decorative catalog focused on those families rather than unrelated manufacturer styles.

Keep functional track family, appearance, and ride behavior as separate identifiers. A Track Styles selection must not create, overwrite, or delete a rotation program or grant 4D compatibility. Switching a supported visual style must not silently change gauge, vehicle eligibility, or spin mode. A cosmetic Arrow style on ordinary track is not functional Arrow 4D track.

Once Track Styles’ Arrow 4D ships, add and test a provider adapter so our cars and seats can run on its representation of matching functional Arrow 4D track. If its feature is cosmetic only, allow it as the appearance of our functional track, or offer an explicit conversion that installs our required mechanisms and track-family data after validating geometry. Styling alone is insufficient. Do not assume its promise includes moving seats, adjustable control rails, a particular gauge, or a public extension API. Existing worlds keep our tracks unless the builder explicitly chooses another appearance or performs a validated conversion while the ride is stopped.

Define an internal track-provider adapter around canonical curve identity, functional family/variant, position and track frame, running-gear contact geometry, required mechanisms, vehicle clearance, attachment support, and rendered-component ownership. Register our implementation first, then adapters for verified external versions. Our seat motion consumes the common track/vehicle state only after family and fit validation; it need not require our particular provider or block ID. Cosmetic Track Styles curves can use the Coasters graph without acquiring 4D eligibility; a future distinct material or graph needs an explicit adapter.

The adapter must establish who renders the spine, running rails, ties, and each control rail so there are no duplicates. If an external static control rail cannot follow the profile, render a supported combination using our functional overlay, or report that combination as unsupported and retain our complete track. Free-spin and shuttle families do not require Arrow X rails. A style name alone does not prove physical compatibility.

Keep optional integration code isolated and load it only when a supported Track Styles version is installed. Retain our own provider when the dependency is absent or its API changes. Removing an appearance-only provider must preserve mechanical data and restore our appearance; conversion involving another mod’s actual blocks needs a separate explicit migration and cannot be promised to survive their removal automatically. No compatibility guarantee can be made for an unreleased version.

## D05 Track Styles licensing and integration approach

All Rights Reserved does not itself establish that an independently authored compatibility addon is forbidden, and it does not grant a right to copy another mod’s implementation or assets. Copyright protects software expression; ideas, systems, and methods of operation are treated differently. The specific license, distribution terms, integration technique, and jurisdiction still matter. This is a planning distinction, not definitive legal clearance. [U.S. Copyright Office](https://www.copyright.gov/help/faq/faq-protect.html), [GitHub licensing guidance](https://choosealicense.com/no-permission/)

Plan an independently written runtime adapter: players obtain Track Styles separately, and our code uses supported interfaces to select its styles or let its installed renderer display its own resources. Do not extract, repackage, shade, redistribute, or adapt its classes, models, or textures into our release without a license or permission covering that reuse. Merely declaring a dependency is not a substitute for checking applicable terms. Reflective access or mixins are not a licensing workaround.

Prefer a documented extension interface where available. The earlier JAR inspection identified possible integration points, not permission to copy or a supported API commitment. Resolve the conflicting MIT project listing and All Rights Reserved JAR metadata before relying on reuse rights; verify applicable terms before distributing the adapter. Our independently authored track and all ride mechanics can proceed without that integration. Proposed upstream collaboration is a future option; no maintainer contact or agreement is assumed.

## D06 Platform and dependencies

Target Minecraft 1.21.1, NeoForge, and Java 21. Start against Coasters Simulated 0.1.5. Its released metadata requires Create 6.0.10 or later, Sable 2.0.0 or later, NeoForge 21.1.228 or later, and Flywheel 1.0.6 or later on the client. These are declared minimums, not a tested compatibility matrix. Pin exact development artifacts and widen supported ranges only after testing. [Base release](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated/files/8655742), [NeoForge setup](https://docs.neoforged.net/docs/1.21.1/gettingstarted/)

The active development baseline now pins Sable2.0.6 and NeoForge21.1.256 with Coasters0.1.5, Create6.0.10-280, Companion1.6.0, Flywheel1.0.6 and Veil4.3.2. [FD501E–H](docs/evidence/0.9/dependencies/README.md) record the required Skin upgrade and affected API, native, persistence and client checks. Immersion1.1.1 is client-only and the dedicated server omits it; Waterparked requires KotlinForForge5.10.0. Accepted Additions dependency closure is retained from0.8. These development qualifications do not perform the1.0 release freeze or replace its remaining release checks.

The historical `research/dependency-audit.json` records the September inspection. The current [2026-11-02 artifact catalog](research/integration-catalog-2026-11-02.json) records staged release coordinates, hashes, runtime versions and dependencies; staging is not activation or certification. `research/ride-variants.json` records the expanded family references and evidence gaps. Development should resolve dependencies through their distribution channels rather than commit third-party binaries into this repository.

## D07 What existing addons can provide

The November 2 catalog covers all seven pictured addons, CTNB, the base mod,
Create, Sable and the adjacent Create Coasters utility. The [integration backlog](docs/INTEGRATION-BACKLOG.md)
is authoritative for exact artifacts, dependency caveats, acceptance and adopted
release allocation. Historical code inspection remains attached to its original release.

| Integration | Current candidate and intended capability | Release allocation |
| --- | --- | --- |
| Create / Coasters / Sable | Required foundation; Ponder, goggles, Display Links and shared station/winch telemetry. Keep the active baseline until a separately validated upgrade. | Existing 0.4–1.0 scope |
| Track Styles | 1.2.0 Arrow spine/tie appearance with original mechanical geometry and fallback. Released static Arrow models do not prove functional control-rail support. | Existing 0.4–1.0 appearance scope; functional support stays FD801 |
| Engineered | 1.1.1 motors, braking, controllers and observers; retain accepted FD401 operator evidence and finish only the declared recovery FE gaps, preserving our readiness and PAX authority. | Existing 0.4–1.0 optional operations scope |
| Simulated Additions | Published 0.2.5 beta embeds `coasterfins` 0.2.2. Station signals and validated automatic dispatch first; resolve Aeronautics/Interiors dependencies and artifact identity. | 0.8 / FD810; rotating seats separately in 0.9 / FD911 |
| Tracks and Balloons / CTNB | 1.2, formerly Create Coasters Extras. Functional tracks, station/control behavior and cosmetic materials require distinct capability checks. | 0.8 operations |
| Magnetized | 1.1.0 longitudinal acceleration/braking; preserve separate magnetic seat-axis mechanics and explicit propulsion ownership. | 0.8 operations |
| Coaster Skin | 1.1.0 selected cosmetic chassis options preserve FD bodies/seats/restraints; full provider mechanisms are refused on FD cars. | 0.9 presentation/seats |
| Immersion | 1.1.1 client-only release. Compose camera/body effects once with the physical passenger carrier; preserve comfort and relative mouse look. | 0.9 presentation/seats |
| Waterparked | 0.2.8-beta-hotfix1 for Coasters 0.1.5; shared graph/mixin/render coexistence first. No assumption that our cars can use slides. | 0.9 coexistence |
| Create Coasters | 2.0 separate Create train/contraption utility; check seat locks and camera overlap. No assumed Sable control bridge. | 0.9 coexistence |

Optional addons stay separately installed. The Track Styles page/JAR license
mismatch remains unresolved; no asset reuse is authorized by this plan. The
artifact catalog records the other declared licenses. Our original track,
rotation mechanisms and five-family scope remain independent of optional addons.
Release allocations are planning scopes, not dates or publication authorization.
Implementation follows the roadmap's integrated prerequisites; no blanket
expansion pause applies. FD801 never blocks standalone 1.0.
Before an expansion activates dependencies, refresh its dated artifact inventory and
dependency closure using the [version refresh policy](docs/INTEGRATION-BACKLOG.md#version-refresh-before-expansion).
New upstream files do not automatically replace the current release pins.

## D08 Player building experience

Build the following shared parts progressively through the release milestones:

| Part | Player purpose |
| --- | --- |
| 4D Track Sets | Complete original family-specific functional tracks, starting with Arrow/S&S four-rail and compact free-spin track. Measure running-gear fit; using the base gauge where suitable does not imply universal cart compatibility. Allow verified equivalent external providers later. |
| 4D Track Equipment Kit | Attach control rails or magnetic spin equipment to our tracks and verified compatible external curves. Keep equipment behavior independent from appearance. |
| 4D Vehicle Assemblies | Family-specific controlled cars, compact free-spin vehicles, and linked shuttle cars with appropriate carrier groups. Use base linking only where the vehicle family permits it. |
| Rotating Seat Modules | Original family-specific seats, pivots, animated restraints, and colors. Share passenger code while preserving each family’s seating arrangement. |
| Rotation Tool | Edit controlled angle profiles or free-spin tuning and magnetic zones, with distinct interfaces and previews. |
| Spin Inducer and Spin Damper | Influence seat rotation through track attachments. Keep their torque settings separate from train propulsion or braking. |
| Boarding Lock | Define the boarding pose and restraint state while stopped. Expose redstone input and ready/locked output for station integration. |
| Shuttle Lift Catch | Capture and lift a supported shuttle train, release it, and allow a controlled return and station capture. |

Proposed workflow: choose a vehicle family, build its matching functional track using existing anchors where supported, choose a compatible appearance, add that family’s mechanical equipment, and place the vehicles. Placement previews report track-family and running-gear mismatches. Author angles for controlled cars or tune carrier balance, damping, and magnetic sections for free-spin cars. Preview empty and occupied behavior before configuring boarding. Use familiar Coasters/Create interactions and family-specific Ponder scenes, including ordinary recovery-cart compatibility.

Every 1.0 family/variant receives applicable existing editing/copy/mirror tools,
loading/tuning settings, clearance previews and readable readiness reasons, with
intentional mechanical differences documented. Its content package supplies a
buildable example and compatible settings, including gentle/intense presets
where supported. The later saved/shareable preset library, selectable original
colors/liveries and unified operator panel are 1.1 features, not extra 1.0 gates.

Start with original generic models for each mechanical family rather than park-branded replicas. X2, Eejanaika, and Dinoconda liveries can share the controlled chassis later. Arbitrary player-built wing structures, general park traffic management, and exact engineering replicas remain follow-up scope. Free-spinning behavior and independent carrier groups are now core planned features.

### Model and texture quality

[FD916](ROADMAP.md#fd916-review-and-improve-models-and-textures) owns the required
0.9 visual review and improvements for the original five families and every added
1.0 family/variant, including their tracks, equipment and item representations. Review
silhouettes/proportions, assembly alignment, texture completeness and UV seams,
consistent material/detail treatment, and inventory, placed and occupied views.
Use a recorded visual reference and finite correction list; unintended placeholders
and failures of that checklist must be resolved before FD915 and 1.0 alpha.

Family content tasks apply the checklist earlier and retain comparison captures.
They can close without waiting for the final FD916 reconciliation. Reuse accepted
visual and mechanical evidence; texture-only edits do not reset physics acceptance,
while geometry/seat/pivot changes require only affected checks. Once the checklist
passes, further taste-based refinements remain nonblocking follow-up work.
Post-1.0 content packages use this same checklist for their new assets and affected
combinations. They do not reopen FD916 or require another review of unchanged art.

[FD917](ROADMAP.md#fd917-deliver-the-production-visual-baseline) owns the additional
pre-1.0 production target: connected machinery, distinct family silhouettes,
native-compatible textured/world-lit surfaces, truthful state transitions and
complete item/teaching representations. Its finite 17-style briefs include
reference decisions for S2/Powered Inverted restraints and Fixed Inverted leg
space. Use existing capacities and seat/pivot/clearance envelopes first; explicitly
scope any necessary contract change and verify only its affected behavior.
Preserve Wing's 16 colors and the selected optional appearance/seat boundaries.
New selectable liveries remain FD1603. FD1000 requires the completed FD917 baseline;
FD904 and FD1002 consume it for rendering/performance and release packaging.

## D09 Shared carrier model

Expansion extends these contracts incrementally through working families. Wing
proves fixed seats; the first Spinning variant proves configurable carrier axis
and pivot behavior; Suspended adds bounded swing and locking. Axis and Roller Ball
reuse only mechanics that actually match. Declare seat/restraint, camera and
propulsion ownership explicitly, and preserve existing persisted identifiers.
Use FD501A for core-required format changes and the consuming FD501B–H package
for adapter-specific migration; a new vehicle or artifact hash does not itself
require a schema change. One owner integrates shared runtime changes and the
[representative occupied client proof](ROADMAP.md#shared-contract-handoff) before
dependent mechanics use them. Decorative art can follow; later player acceptance
reuses that proof instead of ordering another cycle.

Each vehicle definition describes its role, required functional track family/variant, running-gear profile, seat positions, pivot transforms, carrier groups, permitted couplings, mass/inertia preset, attachment compatibility, and boarding pose. Each carrier persists its unwrapped angle, angular velocity, mode, and lock state. Modes are controlled, free, magnet-assisted, and boarding capture/locked. Ordinary carts have no 4D carrier state. Shuttle operation is a separate train state machine, not another way to compute seat angle.

Select compatible modes through the vehicle definition; changing modes on an occupied moving train is prohibited. Entering a boarding or controlled mode must smoothly capture the current pose and speed instead of resetting the angle. Optional adapters consume the same authoritative carrier transforms.

## D10 Controlled rotation model

Author a scalar rotation profile over distance along the selected track path. Each car samples the profile at its own axle location. The front and rear cars therefore perform a flip at the same place, at different times.

Use a canonical path direction that does not change when velocity reverses. A stopped car retains its target pose; a rollback retraces the profile. Do not drive the flip with elapsed time or a train-wide animation clock.

The profile stores unwrapped angles: 0, 180, 360, and 720 degrees carry different rotation histories even where the final orientation matches. Interpolate these scalar angles before converting to a rotation quaternion; shortest-path interpolation between endpoints would erase full turns. Use smooth interpolation with explicit endpoint slopes, plus limits on overshoot, angular speed, and acceleration.

Define angle zero as the documented boarding pose relative to the chassis. Compose chassis orientation, axle rotation, and the seat’s fixed mounting transform in that order. Track banking and seat flipping remain separate. The initial controlled car’s wings share one physical rotation program; mirrored model coordinates must not accidentally reverse one wing. Other vehicle definitions may use separate carriers.

Store a versioned profile using dimension, track-host identity, stable endpoint references, section direction, keyframe distance, angle, and interpolation settings. Assign addon-owned identifiers where upstream references are insufficient. Never persist only temporary graph object identities. Track edits, removal, schematic placement, and assembly moves must explicitly rebind or invalidate the profile.

Use the engaged track segment when available. At crossings and adjacent tracks, constrain fallback projection to the previously occupied segment and connected neighbors. A global nearest-track search can select the wrong rail beneath an overpass.

## D11 Free rotation

Free carriers respond to gravity, chassis movement, inertia, loading, and damping. Their orientation cannot be computed solely from track position: stopping the cart can leave seats swinging, and a rollback does not rewind their motion. Store and synchronize actual angular state. Do not substitute random flips or a hidden controlled profile for free rotation.

Prefer native rigid-body rotary joints so pivot placement, carrier mass, and chassis acceleration produce the motion together. Add explicit simulated passenger mass at occupied seat offsets if the engine does not already include it; never count it twice. Use a documented equal mass per rider and configurable test ballast, not inferred real player weight. Legacy offset pivots and revised near-rider pivots must be distinct geometry/inertia presets.

If native joints cannot deliver stable behavior, prototype a reduced angular simulation using carrier inertia, effective gravity at the moving pivot, chassis angular motion, and damping. Derive and test it rather than adding extra gravity torque on top of an already active native joint. Use fixed physics steps, bounded parameters, and convergence tests.

## D12 Magnetic assistance

Magnetic zones apply a bounded torque interaction only to carriers with the matching mechanism on an eligible family track. Ordinary carts are ignored by the seat-torque system even when their main running gear fits the track. This is a game approximation of magnetic equipment; exact manufacturer field geometry is unverified. Specify direction, strength, falloff, and whether the attachment induces or damps rotation. Account for direction of travel and relative motion. A passive zone must not continuously add energy to a motionless vehicle; translational-to-rotational energy transfer and damping should be bounded and measured.

Evaluate traversed spans, not only the current point, so fast vehicles cannot skip narrow zones. Avoid duplicate effects from both track endpoints or repeated entry callbacks. Reversing through the same zone must produce the defined physical interaction without resetting the carrier state.

Free-spin previews show trajectories for declared starting conditions and loading presets. They are examples, not guarantees of a fixed inversion count. A deterministic test harness should reproduce its own initial conditions, while live multiplayer uses authoritative server snapshots rather than assuming identical client physics.

## D13 Control rails and physical mechanism

The controlled family uses an idealized rail-driven mechanism: a server-evaluated profile sets a rotary constraint target, and the visible control rails are generated from the same data. This preserves position-dependent mechanical behavior without asking four separate wheel contacts to solve the chassis motion. Free-spin families do not use this profile or its rail-displacement mapping.

A later version could add a full contact-driven follower/gear physics solver.
The visible connected follower–slider–transmission–axle assembly is required
before 1.0 by [FD917 VP03](docs/roadmap/pre-1.0-visual-production.md#vp03); it is not
deferred with that optional solver. Use actual interpolated chassis/carrier pose,
defined rail contact samples and an explicit ratio. Resolve servo lag and neutral
capture through a coherent mechanism or the minimum bounded drive/capture change;
do not substitute the profile target for the real seat angle or hide separation.

For the initial visual mechanism, define a bounded monotonic mapping between control-rail displacement and unwrapped axle angle, with a configurable effective gear ratio. Treat this as a game approximation, not a verified engineering model of the real rides. Rail stroke, geometry clearance, and maximum rotation must be mutually consistent. Do not reset rail height whenever the angle crosses 360 degrees.

A closed circuit must join continuously in rail displacement and slope, as well as seat orientation. Matching the end pose modulo 360 degrees alone is insufficient for a direct rack-displacement model. Require the profile to return to the same mechanical displacement or explicitly design and validate a different linkage before allowing such a program.

Keep the chassis under Coasters Simulated’s existing rail constraint. Prototype separate Sable carrier sublevels joined to it by rotary constraints. A single carrier may serve the first controlled prototype, but validate two independent carriers before committing to the architecture. Sable’s inspected source contains rotary constraints and a physics pipeline for adding constraints. Motor-target control, free rotation, lifecycle behavior, and suitability in the pinned release still need a prototype. [Sable joint implementation](https://github.com/ryanhcode/sable/blob/6f2b321e9fa01f35e47b2258914e3e1a2490a608/common/src/main/java/dev/ryanhcode/sable/command/SableJointCommands.java)

If a driven sublevel proves unstable, evaluate a dedicated seat entity with an authoritative transform derived from the chassis and axle. That fallback must still solve passenger position, orientation, collision, camera, persistence, and networking, plus angular dynamics for free carriers; a rotating rendered model alone is not a working seat.

## D14 Shuttle operation

Implement PAX-style operation as boarding, capture, lifting, release, outbound travel, reverse travel, braking/capture, and unloading. Preserve seat angular state when the train reverses. Restraints remain locked during intermediate station passes; release only after the final capture and seat alignment.

First test whether the existing lift can engage, release, and permit reverse travel with a linked free-spin train. If it cannot, implement the planned catch/release block and a functional lift-carriage adapter. A motor-launch approximation can be a test fixture, but it does not satisfy the lift-and-release milestone. Use Engineered’s existing hold/brake functionality when compatible; keep shuttle lifecycle and restraint interlocks in this addon.

Closed or guarded spike ends must retain the cart in the track system. The base mod supports jumping off open ends, so do not assume an open track tip will automatically behave as a shuttle turnaround. Test rollback before the endpoint, overrun recovery, lift disengagement, and loss of power or chunk availability.

## D15 Integration boundaries verified in released code

The following names are observed implementation entry points, not a promise of a stable upstream API:

| Location | Verified finding and intended investigation |
| --- | --- |
| Coasters `CoasterPathTrackFrame` | Public `closestHitOnEdge`, `trackBasisAtHit`, and quaternion helpers. Its `GraphHit` contains an edge, point, and curve parameter `t`. Convert parameter to distance using an arc-length lookup. |
| Coasters `CoasterCartResolve` | Public cart/sublevel lookup and cart-center helpers. Use these to associate each carrier with its chassis. |
| Coasters `CoasterCartTrackSnap` | Has a pre-physics-tick handler and engagement checks. Establish update ordering and extract actual engagement through a narrowly scoped adapter if necessary. |
| Coasters guide constraint and hit cache | Relevant classes are package-private. Expect a small accessor/injection or upstream hook request; do not assume a ready public cart callback. |
| Engineered `TrackSpanGeometry` | Public `resolve`, `rebind`, `distanceAlong`, and `findEdge` methods provide concrete pointers for span authoring and rebinding. |
| Track Styles `MyTrackModels` | Public `STYLES` map and `TrackComponents` record. Registration timing, server-side style validation, render ownership, and reload behavior remain unproven. |
| Waterparked graph mixin | `CoasterPathGraphManagerMixin` injects into `isCoasterGraphBezier` to include its own material. This is a useful example if a separate 4D material becomes necessary. |

Prototype functional family metadata and original track rendering on the existing Coasters graph, with eligibility enforced in placement and snapping from the outset. The addon supplies its own track items, models, and equipment; graph reuse must not bypass family restrictions. Use separate functional materials if required to enforce eligibility or satisfy placement, collision, and rendering requirements. Persist provider identity and functional family separately from appearance, and apply the same compatibility contract to our track and external representations.

Implement the rotation math independently of Minecraft; keep Coasters integration, Sable physics, client rendering, networking, and optional addon adapters in separate packages. Keep version-sensitive mixins small and individually documented.

## D16 Riders and synchronization

The seat carrier, rider body, and camera must agree on one transform. Preserve normal mouse look relative to the rider’s seat. Test upside-down views, first-person arms, third-person rendering, and camera restoration after dismount. Offer an optional comfort view that stabilizes the camera without altering the server’s seat motion.

FD904C1 owns the independently closable 0.5 baseline for the original families
and Wing. FD503 and FD912 depend on C1, not the whole FD904C parent. Later family
acceptance adds distinct pose/comfort cases while C1 stays closed; FD912 supplies
optional effects during 0.9. FD904C2 reconciles those records after FD1000 without
a second implementation or full replay. Existing Sable transforms alone do not
establish a selectable comfort option.

The server owns profiles, tuning, binding, restraints, shuttle state, and actual mechanical state. Synchronize each carrier’s angle, angular velocity, lock/mode, and occupancy revision with timestamps and stable identities. Include complete initial state for late joiners. Reuse Sable transforms where appropriate and avoid applying the carrier rotation twice. Preserve turn count when reconnecting or interpolating through full turns. Save free-spin angular state and pause unloaded assemblies consistently; do not simulate an arbitrary offline catch-up on reload.

Validate edit permissions, distance, payload size, finite numeric values, and revisions. Lock profile editing while its section is occupied, or require a stopped maintenance state. Take physics snapshots across thread boundaries using the pinned Sable API’s supported mechanism; never mutate Minecraft world state unsafely from physics callbacks.

Use a long, smooth approach to the station’s boarding pose. Free carriers first need angular damping and controlled capture. Restraints may release only when the train is stopped, every carrier is aligned, and angular speeds are below the boarding threshold. Provide a recovery/dismount path for broken or unloaded assemblies. If engagement or profile data is lost, suspend controlled profile advancement or apply bounded damping to free carriers, retain valid state, and request a stop through an available controller; never snap occupied seats to zero. A shuttle passing through the station is not a boarding event.

## Addressable roadmap

[ROADMAP.md](ROADMAP.md) owns package status, prerequisites and finite completion
criteria. The [milestone execution guides](docs/roadmap/README.md) break each open
package into ordered procedures and handoffs without changing these contracts or
adding acceptance gates. IDs stay stable when allocation changes. The adopted
sequence is:

| Milestone | Work packages | Delivery |
| --- | --- | --- |
| Foundation–0.3 | FD001–FD305, existing bounded gates | Preserve accepted architecture and original five families. |
| 0.4 | FD401–FD404 | Complete remaining recovery and player workflows; core checkpoint only. |
| 0.5 | FD501–FD503; six original discovery IDs; FD1601S, FD1702S, FD1801S, FD1802S, FD1901S; FD1101A–C; FD904C1 | Specify all 1.0 vehicles/dependencies; prove Wing-backed contracts, comfort and reusable tools. |
| 0.6 | FD1501A–C, FD1301A–C, FD1601A–C, FD601 | S1 conventional free-spinning, Suspended and Fixed Inverted; [complete 2026-11-15](docs/evidence/0.6/acceptance.md). |
| 0.7 | FD1302A–C, FD1502A–C, FD1801A–C, FD1802A–C, FD1803, FD701 | Axis/Roller Ball and Conventional/New-generation Flying, including scenery-facing yaw. |
| 0.8 | FD1401A–C, FD1501D–F, FD1701A, FD1702A–C, FD1901A–C, FD1902, FD810–FD813 | Pipeline, S2, Dive, Powered Inverted Yaw, required authority and operations adapters. |
| 0.9 | FD910–FD916 | Presentation, optional seats, coexistence and the complete model/texture review. |
| Pre-1.0 production | FD917 | Production art, connected mechanisms and truthful state presentation for all 17 styles and required shared/player-facing assets. |
| 1.0 alpha | FD1000 | Complete all adopted vehicles/variants, 1.0 integrations and FD917; freeze features/profiles and the production visual baseline. |
| 1.0 beta / RC | FD901–FD905 | Integrated stabilization and outstanding final qualification. |
| 1.0 stable | FD1001–FD1003 | Documentation, packaging and separately authorized publication. |
| 1.1 | FD1602–FD1605 | Shareable presets, colors/liveries and unified operator panel. |
| 1.2 | FD1701B–C, FD1703 | Ride-local multi-train operation, consuming the earlier FD1701A authority. |
| Post-1.0; versions after feasibility | FD2001–FD2007 | Transfer platforms, turntables, drop tables, tilt tracks and the finite additional moving-track styles. |
| Independent | FD801 | Inspectable functional Track Styles adapter; unavailable upstream work does not block release. |

Milestone 0.5 is complete: [FD503 reconciliation](docs/evidence/0.5/acceptance.md).
Later implementation remains separately assigned.

Milestone gates aggregate completion, not start permissions for unrelated tasks.
Read-only discovery and concepts can advance during 0.4. Operations adapters start
after their own FD501B–H and control/provider prerequisites; Pipeline does not
wait for Axis. FD501A depends only on the core decision, so optional-addon blockers
do not delay unrelated vehicle mechanics.
Recipes, guides and examples accompany each feature. Shared runtime changes have
one owner and integrate into `main` before dependent work starts. Feature assignments
own complete outcomes, not perpetually open collections of evidence “slices.”
Use the [current case queue](docs/CURRENT-QUALIFICATION.md#current-work-queue)
for assignments and next actions; preserve historical evidence separately.

FD1003 is the final consumer of the expanded scope, never a prerequisite for its
families. The [integration allocation](docs/INTEGRATION-BACKLOG.md#release-allocation)
uses the same milestones. No new capability is advertised until its package passes.
FD1601, FD1702, FD1801/FD1802 and FD1901 vehicle outcomes feed the 0.6–0.8
gates and FD1000/FD905/FD1003. Their content also feeds FD916. FD1602–FD1605
tools, FD1701B/C/FD1703 multi-train management and FD2001–FD2007 moving tracks
remain outside 1.0. Flying starts after Fixed Inverted's mechanics handoff without
waiting for unrelated 1.2 operations; Powered Inverted consumes FD1802A without
waiting for Flying content or its aggregate gate. Keep the final qualification
baseline stable while later work uses integrated contracts.

## D17 Validation and release scope

Automate tests for profile interpolation, unwrapped turns, arc-length conversion, reversal, endpoint continuity, save-format migration, and malformed edit data. Add free-spin tests for equilibrium, damping, inertial response, independent groups, passenger mass changes, energy bounds, and timestep convergence. Test magnetic-zone traversal at high speed, reversal, and rest, plus boarding and shuttle state transitions. These cover behavior that is easy to regress without a visible crash.

Add a compatibility matrix covering every 4D family against matching, ordinary, and other-family tracks; regular carts with fitting and non-fitting running gear; cosmetic-only Arrow styling; and supported external functional providers. Verify that ordinary carts never receive seat profiles or magnetic seat torque. Exercise manual placement, packet requests, assembly, re-snapping, mixed consists, save/load, and approaching an incompatible boundary in both directions. Do not treat a locked 4D cart as an ordinary cart in any path. For recovery, test rail access separately from coupler fit and towing, keeping the disabled 4D unit on matching track throughout.

Use a small repeatable controlled layout with a straight flip, vertical track, banked turn, inversion, rollback, close track crossing, and boarding zone. Add a compact free-spin layout with balanced/unbalanced occupancy and magnetic zones, plus a shuttle with two spikes and repeated station passes. Exercise linked and individual vehicles, two clients, late joining, disconnects, chunk unloads, server restart, track deletion, and cart disassembly. Verify full swept seat clearance, not just the chassis envelope. Debug displays should distinguish track inversions from carrier rotation.

Preserve the accepted base, Styles, Engineered and combined-profile cases. Add affected checks for each implemented expansion capability and actual conflict pair, including client-only asymmetry. FD1000 freezes the full supported profile list; FD901–FD905 consume accumulated results and perform only outstanding final qualification. FD404 does not trigger a release sweep. A new JAR never resets acceptance. The [integration acceptance matrix](docs/INTEGRATION-BACKLOG.md#certification-and-release-evidence) defines the full cases. Any external functional provider must prove family/geometry equivalence and same-family joins. Appearance removal preserves mechanical data; external block conversions require explicit migration. Engineered, CTNB, Additions and Magnetized require one propulsion/control owner per section and phase; seat-axis magnetic assistance remains separate.

Follow [VALIDATION-POLICY.md](docs/VALIDATION-POLICY.md) for all checks. Improve
blocked fixtures and measurement collectors early in FD502, reuse worlds, and
diagnose after two inconclusive attempts with the same approach. Schedule client
runs as coordinated host work. Use native tests for mechanism invariants and
connected clients for real interaction, rendering and rider synchronization;
separate valid records may close a complete feature. A required criterion or
supported invariant must identify each gate blocker. Nonblocking cosmetic or
diagnostic refinements stay in follow-up notes, never as new acceptance work;
apply the [classification policy](docs/VALIDATION-POLICY.md#gate-blockers-and-follow-up-work).

Budget runtime work per active carrier and constraint as well as per car: cache arc-length tables and rail meshes; invalidate on edits; avoid whole-network searches every frame. Measure a declared benchmark, such as two occupied seven-car controlled trains, several eight-seat free-spin vehicles, and a linked shuttle, against equivalent baseline scenes. Record hardware, carrier count, and frame/tick costs before setting performance guarantees.

The expanded 1.0 vehicle set supports fixed-world tracks. Purpose-built
moving-track assemblies are separately committed post-1.0 through FD2001–FD2007
and D20; their release versions follow native feasibility. Arbitrary track on
player-built moving contraptions, arbitrary user-built wings, exact linkage
replicas and park-scale traffic control remain outside that bounded program.
Preserve host coordinate information for the defined extensions. Free-spin
mechanisms, distinct carrier groupings and shuttle operation remain committed
1.0 scope, not unspecified stretch features.

## D18 Remaining decisions to resolve through prototypes

The questions below retain the original design concerns; accepted original-family
answers stay accepted. During 0.5, the six original discovery IDs and five added
vehicle specification packages resolve all 1.0 families'
axis/mode, seating, track and reference choices, especially Pipeline, the adopted
S1/S2 Spinning specifications and Roller Ball native feasibility. Use a prototype only
for an unresolved constraint, then retain it for implementation. FD501 separately
resolves dependency and migration choices; do not reopen historical questions
merely because their wording remains here.

1. Can the pinned Sable release support driven, free, and independently rotating carriers accurately while preserving passenger and camera behavior?
2. Can the actual Coasters engagement result be exposed without replacing its existing rail solver?
3. Can a Track Styles adapter cleanly separate provider geometry from variable control rails and magnetic attachments, and is its licensing metadata clarified if copying is desired?
4. Can Additions station signals use shared telemetry and validated requests (0.8 / FD810), and can its seats separately preserve carrier/restraint authority (0.9 / FD911)? Coaster Skin and Immersion require their own seat/pose checks; no adapter becomes mandatory.
5. What family-specific pivot positions, carrier groupings, inertia, gear ratios, clearances, and operating limits produce convincing and stable motion?
6. Can primary material establish second-generation ZacSpin details and the exact carrier couplings for each free-spin vehicle before finalizing models?
7. Can the existing lift and cart endpoint behavior support a captured shuttle, or must the catch/release adapter supply those functions?
8. Which ordinary cart running-gear profiles physically fit each track, and can the base snapping/assembly paths enforce the asymmetric compatibility rules without exceptions?
9. Which existing couplers and ordinary carts can recover a disabled 4D unit while every vehicle stays on compatible track and its carriers remain secured?

These questions do not block the plan. They are the explicit acceptance gates for implementation rather than assumptions hidden inside the design.

## D19 Vehicle variants, tools and scenery-facing yaw

The [additional package allocation](ROADMAP.md#additional-vehicle-and-supporting-feature-packages) owns statuses,
implementation steps and finite acceptance. Preserve all 1.0 commitments and
accepted evidence while extending contracts through concrete features.

| Release | Product scope | Design boundary |
| --- | --- | --- |
| 0.6 / FD1601 | Fixed Inverted | One fixed below-track vehicle configuration with measured running gear, restraints and clearance. Required for 1.0. |
| 0.8 / FD1701A, FD1702 | Hold/dispatch authority and Dive | One fixed-seat Dive configuration; approach/hold/release consumes validated authority. Multi-train management is not a vehicle prerequisite. |
| 0.7 / FD1801 | Conventional Flying | Seated boarding, station-based changeover to prone flight, locked riding orientation and return to seated unloading. Own restraints, clearance, camera and interrupted-transition recovery. |
| 0.7 / FD1802 | New-generation Flying sub-variant | Seated/sideways presentation, defined route-based transition to/from prone flight, independent controlled yaw and scenery targeting in both seated and prone postures. Both Flying variants are required before FD1803/FD701/1.0 close. |
| 0.8 / FD1901–FD1902 | Powered Inverted Yaw Coaster | Upright-seated passengers below the track, one independently controlled full-turn yaw carrier per car, powered speed/direction/dwell and scenery-facing show sections. This is the MACK-style concept, distinct from Flying and Arrow-style free lateral swing. |
| 1.1 / FD1602–FD1605 | Setting presets; colors/original liveries; unified operator panel | Presets validate compatibility/version/permissions and explicitly rebind world references. Appearance preserves mechanics; panel commands use existing authority. No arbitrary texture uploads, scripting or park-wide controller. |
| 1.2 / FD1701B–C, FD1703 | Ride-local multi-train management | One fixed circuit, finite train limit and declared homogeneous consists; whole-consist occupancy/reservations and fail-closed dispatch. No shared-track network, arbitrary junction routing or automatic depot management. |

References: [B&M Inverted](https://www.bolliger-mabillard.com/inverted-coaster),
[B&M Dive](https://www.bolliger-mabillard.com/dive-coaster),
[Vekoma Flying](https://www.vekoma.com/flying-coasters) and
[MACK Inverted Powered](https://www.mack-rides.com/products/inverted-powered-coaster).
Use original assets and measured game contracts. Vekoma supplies the seated/
sideways-to-flying inspiration; scenery-facing yaw in both postures is our explicit
required game capability, not an assertion of exact real-world actuator design.
All 1.0 posture/yaw transitions use fixed track geometry and vehicle joints;
New-generation Flying's route-based changeover has no moving-track prerequisite.

### Implemented 1.1 tool boundary

[Builder/operator tools](docs/BUILDER-TOOLS.md) freeze the finite preset list:
Arrow/Axis profiles, Free Spin/Launched Inverting Spinning magnets, original
Legacy/Free Spin/Revised/PAX tuning, and one named New-generation Flying or
Powered Inverted route interval. World markers require fresh explicit binding.
Five original upholstery palettes apply to all 17 styles without replacing
mechanical meshes or optional chassis. The operator panel samples whole-consist
state and delegates guarded commands; it adds no physics or park-wide control.
[Qualification evidence](docs/evidence/1.1/README.md) records affected checks.

### Shared yaw and posture ownership

FD1802A delivers yaw through the New-generation Flying implementation; FD1901A
reuses the integrated handoff. Define each physical yaw pivot and coordinate frame,
the posture-change axis and transform order, and the swept envelope of simultaneous
motion. Test targeting in both seated and prone postures on the declared track
envelope; copying upright yaw math or moving only the camera cannot establish this.

Support authored unwrapped angle profiles and route sections bound to scenery
markers. Project the target into the mechanism's permitted yaw plane and enforce
range, winding, angular-speed and acceleration bounds. Preserve canonical path
direction and retrace authored profiles on reversal. Scenery sections compute
targets from the current pose. Define smooth entry/exit and deterministic handling
of missing/unloaded targets, unreachable angles and degenerate projections.

Designated show stops may request bounded yaw changes while travel is held.
These explicit requests do not advance distance-based profiles or bypass propulsion,
restraint or capture authority. The original D10 stopped/rollback behavior remains
unchanged. Specify the authored route/transition state machine that permits these
mode transitions; unrestricted player edits on moving occupied vehicles remain
prohibited under D09. Server authority validates edits, bindings and commands.

Synchronize physical carrier, passenger and camera transforms once. Preserve player
look and comfort settings; target-facing is physical orientation rather than forced
view direction. Persist target identity, binding, turn count, mode and transition
state and restore consistent late-join/restart behavior. Fault fallback retains
valid pose with bounded movement and blocks transitions requiring unavailable
alignment; it never snaps occupied seats or releases restraints implicitly.

Powered Inverted supplies a working standalone powered-drive path, explicit
propulsion ownership, editable speed sections, stops/dwell and forward/reverse
operation. Its finite scene-event interface can drive existing optional lighting/
audio integrations; no general animation or scripting engine is implied. Its
example demonstrates moving scenery tracking, a yaw-changing show stop, reverse
travel and normal station capture/unload. Shared yaw acceptance carries forward;
new upright pose, drive and failure behavior receives its distinct checks.

## D20 Post-1.0 moving-track mechanisms

FD2001–FD2007 deliver a separate moving-track program after 1.0. Turntables, drop
tables and tilt tracks are explicitly included. The finite additional set is
transfer platforms, elevator tracks, teeter-totter tracks, side-drop tracks and
one yaw-plus-pitch compound motion-table style. References include
[Intamin's Multi Dimension elements](https://www.intamin.com/product/multi-dimension-coaster/).
Additional arbitrary styles or axes require an explicit scope addition.

FD2001 first proves a retained track segment moving between two fixed endpoints.
Disconnect traversable graph edges before motion, retain and capture the vehicle,
reserve the destination, and reconnect only after measured alignment and locking.
Specify moving-segment versus chassis/carrier coordinate ownership, declared
vehicle/consist envelope, provider hooks, braking/retention and fault states.
Persist enough state to reconcile occupancy, endpoint reservations and motion
after unload/restart. Endpoint removal, power loss or incomplete alignment must
leave the vehicle retained and the route unavailable, not attached to a stale edge.

Build translation, yaw rotation, vertical drop and pitch tilt as separate concrete
mechanisms. Elevator and teeter-totter extend the relevant axis behavior; side-drop
and compound tables need their own combined-motion clearance and recovery proof.
Each supplies original content, player controls, an example and completed player
acceptance. A visual animation with unchanged graph/retention is insufficient.

Assign release numbers and finite shipping increments after the native prototype.
Accepted mechanisms may ship without waiting for the entire catalog; outstanding
named styles remain roadmap work. Each increment receives its own permanent
FD2007 suffix gate, closed once; the parent aggregates the adopted program.
No moving-track package blocks 1.0 or the fixed-track vehicle outcomes,
and no arbitrary moving-contraption support is implied. Reuse prototypes,
shared graph/retention evidence and per-axis checks; test additional combined
motion only where behavior actually differs. Do not reopen accepted fixed-track
families because moving-track research produces a new artifact.
