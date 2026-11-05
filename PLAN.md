# Coasters: Fourth Dimension addon plan

The [2026-11-02 integration backlog and release allocation](docs/INTEGRATION-BACKLOG.md)
supersedes older dependency/version observations below for the staged candidates,
including Immersion 1.1.0 client-only/optional-Aeronautics metadata. It records
proposed optional expansion scope without resuming implementation or changing the
standalone 1.0 gate. Historical inspections remain evidence for their own artifacts.

Planning update (2026-11-02): foundation and the bounded 0.1, 0.2, and five-family 0.3 gates have acceptance evidence. FD401–FD403 remain open toward FD404 and the subsequent 0.9/1.0 gates. New ecosystem integration implementation is paused pending user approval; see [new-family acceptance and limits](docs/REVISED-SHUTTLE.md), [integration progress](docs/INTEGRATIONS.md), and [package statuses](ROADMAP.md). FD403 now includes original Ponder lessons for all fifteen craftable items and native survival crafting/placement checks for every family; complete survival operating exercises remain open. The design below remains the target for unfinished release work.

The initial design was researched September 30, 2026, using project pages, released JARs, and selected upstream source files. Research observations below describe that planning baseline; later inspection and runtime evidence are recorded in the implementation documents and roadmap. The addon has since been implemented and tested through the bounded gates listed above.

Use [ROADMAP.md](ROADMAP.md) for addressable work packages, dependencies, acceptance criteria, and release gates through 1.0. IDs such as FD107 identify implementation tasks; D01–D18 below identify design sections. Keep these IDs stable so later requests can name a precise scope. Completion and evidence belong to the individual package statuses; a development version number is not proof that a release gate has passed.

Planning reconciliation (2026-11-04): the [current qualification record](docs/CURRENT-QUALIFICATION.md)
owns source/JAR identity and evidence reuse. FD401A–D, FD402A–F and FD403A–F define
finite remaining 0.4 acceptance. FD904B adds the representative-park benchmark;
FD904C retains D16's optional stabilized camera as required pre-1.0 work. The
[1.0 supported envelope](ROADMAP.md#supported-envelope-and-explicit-10-dispositions)
explicitly retains the existing coordinate/capacity limits. Proposed Wing,
Suspended, Axis, Pipeline, Spinning and Roller Ball discovery has permanent IDs
FD1101, FD1301, FD1302, FD1401, FD1501 and FD1502; implementation remains paused.

Build an addon for Create: Coasters Simulated covering controlled, freely rotating, magnet-assisted, and shuttle 4D coasters. Ship original functional tracks, working vehicles, rotating seat carriers, spin-control equipment, and rider integration across several ride families. Reuse the base mod’s track graph, cart physics, linking, and lift system where suitable. Track Styles remains an optional integration; its release schedule must not block ours.

Retain the original X2, Eejanaika, and Dinoconda goal, and expand the roadmap to Intamin ZacSpin generations, S&S 4D Free Spin, and PAX Bumerang. The user’s [fourth-dimension coaster reference](https://en.wikipedia.org/wiki/Fourth-dimension_roller_coaster) supplies the initial family list; manufacturer evidence and proposed game behavior are distinguished below. No additional JAR or runtime compatibility testing was performed for this scope expansion.

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

The release roadmap through 1.0 commits to all five families, with the second-generation ZacSpin’s exact fidelity gated on further evidence. New ordinary fixed-seat wing coaster and unrelated spinning coaster models are outside that release scope. Later vehicle proposals remain separately paused in the [release map](ROADMAP.md#release-map); an integration adapter does not implement or approve those vehicles. Existing ordinary carts may use our tracks under the compatibility rules below.

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

Track definitions must expose a functional family/variant, main-rail contact geometry, supported running-gear profiles, required mechanical interfaces, and attachment/clearance envelopes. Vehicle definitions must expose their role (ordinary or family-specific 4D), actual running-gear profile, required track family, and supported mechanisms. Include running, side-guide, and upstop wheel contacts where applicable; gauge alone is insufficient. Fit tolerances and supported adapter dimensions must be measured and tested, not inferred from a style label. Unknown external combinations remain unsupported until registered and verified.

Gate placement, assembly, snapping/re-engagement, dispatch, route transitions, and save/load rebinding on the server. Check every vehicle in a linked consist independently. Reject an incompatible placement or planned connection with an actionable reason such as “Arrow 4D cart requires Arrow 4D track” or “Cart running gear does not clear the control rails.” Prevent planned operation across an incompatible boundary; detect upcoming transitions with enough braking distance rather than discovering a mismatch only after crossing it. Track edits while occupied remain prohibited. Unexpected support loss enters recovery behavior, never automatic conversion, teleportation, or snapping onto an unrelated nearby track.

Track geometry may share the existing Coasters graph internally, but eligibility must be enforced before its generic cart snapper can accept a mismatched vehicle. Prototype these hooks early. If metadata on the base material cannot enforce the rule reliably, use distinct functional materials or track types while retaining graph reuse.

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

The active development baseline pins Sable 2.0.3 and NeoForge 21.1.248 with Coasters 0.1.5 and Create 6.0.10-280. FD001 has bounded acceptance; current release gates still require their own evidence. Sable 2.0.6 is staged for a future expansion baseline. Coaster Skin requires Sable exactly 2.0.6 and NeoForge >=21.1.256, so activating it needs a separately validated dependency update. Aeronautics is not required by base Coasters or the staged Immersion 1.1.0 metadata; Additions lists Aeronautics and Interiors and requires their dependency closure before activation.

The historical `research/dependency-audit.json` records the September inspection. The current [2026-11-02 artifact catalog](research/integration-catalog-2026-11-02.json) records staged release coordinates, hashes, runtime versions and dependencies; staging is not activation or certification. `research/ride-variants.json` records the expanded family references and evidence gaps. Development should resolve dependencies through their distribution channels rather than commit third-party binaries into this repository.

## D07 What existing addons can provide

The November 2 catalog covers all seven pictured addons, CTNB, the base mod,
Create, Sable and the adjacent Create Coasters utility. The [integration backlog](docs/INTEGRATION-BACKLOG.md)
is authoritative for exact artifacts, dependency caveats, acceptance and proposed
release allocation. Historical code inspection remains attached to its original release.

| Integration | Current candidate and intended capability | Release allocation |
| --- | --- | --- |
| Create / Coasters / Sable | Required foundation; Ponder, goggles, Display Links and shared station/winch telemetry. Keep the active baseline until a separately validated upgrade. | Existing 0.4–1.0 scope |
| Track Styles | 1.2.0 Arrow spine/tie appearance with original mechanical geometry and fallback. Released static Arrow models do not prove functional control-rail support. | Existing 0.4–1.0 appearance scope; functional support stays FD801 |
| Engineered | 1.1.1 motors, braking, controllers and observers; complete operator/FE evidence while retaining our readiness and PAX authority. | Existing 0.4–1.0 optional operations scope |
| Simulated Additions | Published 0.2.5 beta embeds `coasterfins` 0.2.2. Station signals and validated automatic dispatch first; resolve Aeronautics/Interiors dependencies and artifact identity. | Proposed 1.1; rotating seats separately in proposed 1.2 |
| Tracks and Balloons / CTNB | 1.2, formerly Create Coasters Extras. Functional tracks, station/control behavior and cosmetic materials require distinct capability checks. | Proposed 1.1 |
| Magnetized | 1.1.0 longitudinal acceleration/braking; preserve separate magnetic seat-axis mechanics and explicit propulsion ownership. | Proposed 1.1 |
| Coaster Skin | 1.0.0 changes seating and cart behavior as well as appearance; review chassis eligibility and exact dependency requirements. | Proposed 1.2 |
| Immersion | 1.1.0 client-only release with optional Aeronautics metadata. Compose camera/body effects once with authoritative carrier pose; review interpolation mixins. | Proposed 1.2 |
| Waterparked | 0.2.8-beta-hotfix1 for Coasters 0.1.5; shared graph/mixin/render coexistence first. No assumption that our cars can use slides. | Proposed 1.2 coexistence |
| Create Coasters | 2.0 separate Create train/contraption utility; check seat locks and camera overlap. No assumed Sable control bridge. | Proposed 1.2 coexistence |

Optional addons stay separately installed. The Track Styles page/JAR license
mismatch remains unresolved; no asset reuse is authorized by this plan. The
artifact catalog records the other declared licenses. Our original track,
rotation mechanisms and five-family scope remain independent of optional addons.
Proposed 1.1/1.2 allocations are planning scopes, not dates or authorization to
resume implementation. FD801 never blocks standalone 1.0.
Before an approved expansion starts, refresh its dated artifact inventory and
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

Start with original generic models for each mechanical family rather than park-branded replicas. X2, Eejanaika, and Dinoconda liveries can share the controlled chassis later. Arbitrary player-built wing structures, general park traffic management, and exact engineering replicas remain follow-up scope. Free-spinning behavior and independent carrier groups are now core planned features.

## D09 Shared carrier model

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

A later version could add explicit follower-wheel contact. That would introduce extra collision and solver work and should follow a stable functional release.

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

FD904C owns delivery and five-family acceptance of that client option before
1.0. Existing Sable passenger transforms do not by themselves establish a
selectable comfort mode. A scope deferral would require an explicit revision
to this requirement and the release gate.

The server owns profiles, tuning, binding, restraints, shuttle state, and actual mechanical state. Synchronize each carrier’s angle, angular velocity, lock/mode, and occupancy revision with timestamps and stable identities. Include complete initial state for late joiners. Reuse Sable transforms where appropriate and avoid applying the carrier rotation twice. Preserve turn count when reconnecting or interpolating through full turns. Save free-spin angular state and pause unloaded assemblies consistently; do not simulate an arbitrary offline catch-up on reload.

Validate edit permissions, distance, payload size, finite numeric values, and revisions. Lock profile editing while its section is occupied, or require a stopped maintenance state. Take physics snapshots across thread boundaries using the pinned Sable API’s supported mechanism; never mutate Minecraft world state unsafely from physics callbacks.

Use a long, smooth approach to the station’s boarding pose. Free carriers first need angular damping and controlled capture. Restraints may release only when the train is stopped, every carrier is aligned, and angular speeds are below the boarding threshold. Provide a recovery/dismount path for broken or unloaded assemblies. If engagement or profile data is lost, suspend controlled profile advancement or apply bounded damping to free carriers, retain valid state, and request a stop through an available controller; never snap occupied seats to zero. A shuttle passing through the station is not a boarding event.

## Addressable roadmap

The implementation source of truth is [ROADMAP.md](ROADMAP.md). Each work package has a permanent ID, prerequisites, design references, and a completion criterion. Foundation through bounded FD305 are complete. Continue from the open FD401–FD404 gates; new ecosystem adapters remain paused until authorized; existing release acceptance work retains its current scope.

| Stage | Work packages | Release scope |
| --- | --- | --- |
| Foundation | FD001–FD008 | Stack, compatibility hooks, carrier/rider proof, data contracts, tests, and persistence. |
| 0.1 | FD101–FD109 | Controlled Arrow/S&S and legacy ZacSpin, matching original tracks, editing, boarding, and ordinary-cart rail access. |
| 0.2 | FD201–FD204 | S&S Free Spin vehicle/track and magnetic spin equipment. |
| 0.3 | FD301–FD305 | Revised ZacSpin and PAX shuttle with actual lift/release and return capture. |
| 0.4 | FD401–FD404 | Adapter/telemetry foundation, Create guidance, existing Engineered and Track Styles acceptance, recovery and teaching. |
| 0.9 release candidate | FD901–FD905 | Certify frozen base/Styles/Engineered/combined profiles, lifecycle, migration and rendering/performance; resolve defects. |
| 1.0 | FD1001–FD1003 | Accurate release documentation, reproducible artifacts, and final acceptance gate. |
| Independent integration | FD801 | Future Track Styles Arrow 4D adapter once an inspectable release and applicable terms exist. |

The original controlled coaster stays in the first playable release alongside a free-spin vehicle. Each release builds on the preceding gates. Version labels express scope ordering, not dates; estimates follow the physics, magnetic, and shuttle proofs. Each gate requires recorded evidence, and no task is marked complete merely because its code was written.

FD801 must preserve functional family/fit checks, profiles, and control-rail ownership, and reject cosmetic-only ordinary track. An unavailable external feature cannot block the five-family standalone 1.0. Requirements for ordinary recovery-cart access remain distinct from unproven towing capability.

Proposed post-1.0 work is allocated in the [integration release table](docs/INTEGRATION-BACKLOG.md#release-allocation): 1.1 operations (Additions/CTNB/Magnetized), then 1.2 seats/presentation and coexistence (Skin/Immersion/Waterparked/Create Coasters and rotating-seat follow-up). These expansions retain the current gates and require separate implementation approval.

## D17 Validation and release scope

Automate tests for profile interpolation, unwrapped turns, arc-length conversion, reversal, endpoint continuity, save-format migration, and malformed edit data. Add free-spin tests for equilibrium, damping, inertial response, independent groups, passenger mass changes, energy bounds, and timestep convergence. Test magnetic-zone traversal at high speed, reversal, and rest, plus boarding and shuttle state transitions. These cover behavior that is easy to regress without a visible crash.

Add a compatibility matrix covering every 4D family against matching, ordinary, and other-family tracks; regular carts with fitting and non-fitting running gear; cosmetic-only Arrow styling; and supported external functional providers. Verify that ordinary carts never receive seat profiles or magnetic seat torque. Exercise manual placement, packet requests, assembly, re-snapping, mixed consists, save/load, and approaching an incompatible boundary in both directions. Do not treat a locked 4D cart as an ordinary cart in any path. For recovery, test rail access separately from coupler fit and towing, keeping the disabled 4D unit on matching track throughout.

Use a small repeatable controlled layout with a straight flip, vertical track, banked turn, inversion, rollback, close track crossing, and boarding zone. Add a compact free-spin layout with balanced/unbalanced occupancy and magnetic zones, plus a shuttle with two spikes and repeated station passes. Exercise linked and individual vehicles, two clients, late joining, disconnects, chunk unloads, server restart, track deletion, and cart disassembly. Verify full swept seat clearance, not just the chassis envelope. Debug displays should distinguish track inversions from carrier rotation.

For 0.9/1.0, test the base stack, Track Styles 1.2.0, Engineered 1.1.1, and their combined profile. Newly staged addons belong to the proposed expansion releases unless FD404 scope is explicitly revised before the candidate freeze. Each expansion repeats individual, conflict-pair and named combined-pack certification, including client-only asymmetry. The [integration acceptance matrix](docs/INTEGRATION-BACKLOG.md#certification-and-release-evidence) defines the full cases. Any external functional provider must prove family/geometry equivalence and same-family joins. Appearance removal preserves mechanical data; external block conversions require explicit migration. Engineered, CTNB, Additions and Magnetized require one propulsion/control owner per section and phase; seat-axis magnetic assistance remains separate.

Budget runtime work per active carrier and constraint as well as per car: cache arc-length tables and rail meshes; invalidate on edits; avoid whole-network searches every frame. Measure a declared benchmark, such as two occupied seven-car controlled trains, several eight-seat free-spin vehicles, and a linked shuttle, against equivalent baseline scenes. Record hardware, carrier count, and frame/tick costs before setting performance guarantees.

The planned releases support fixed-world tracks, with ride families introduced at the release gates in ROADMAP.md. Track mounted on moving contraptions, arbitrary user-built wings, exact linkage replicas, and park-scale traffic control remain follow-up work. Preserve host coordinate information now so later extensions remain possible. Free-spin mechanisms, distinct carrier groupings, and shuttle operation are committed roadmap scope rather than unspecified stretch features.

## D18 Remaining decisions to resolve through prototypes

1. Can the pinned Sable release support driven, free, and independently rotating carriers accurately while preserving passenger and camera behavior?
2. Can the actual Coasters engagement result be exposed without replacing its existing rail solver?
3. Can a Track Styles adapter cleanly separate provider geometry from variable control rails and magnetic attachments, and is its licensing metadata clarified if copying is desired?
4. Can Additions station signals use shared telemetry and validated requests (proposed 1.1), and can its seats separately preserve carrier/restraint authority (proposed 1.2)? Coaster Skin and Immersion require their own seat/pose checks; no adapter becomes mandatory.
5. What family-specific pivot positions, carrier groupings, inertia, gear ratios, clearances, and operating limits produce convincing and stable motion?
6. Can primary material establish second-generation ZacSpin details and the exact carrier couplings for each free-spin vehicle before finalizing models?
7. Can the existing lift and cart endpoint behavior support a captured shuttle, or must the catch/release adapter supply those functions?
8. Which ordinary cart running-gear profiles physically fit each track, and can the base snapping/assembly paths enforce the asymmetric compatibility rules without exceptions?
9. Which existing couplers and ordinary carts can recover a disabled 4D unit while every vehicle stays on compatible track and its carriers remain secured?

These questions do not block the plan. They are the explicit acceptance gates for implementation rather than assumptions hidden inside the design.
