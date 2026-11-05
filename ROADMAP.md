# Coasters: Fourth Dimension roadmap to 1.0 and proposed integrations

This is the addressable execution plan for the design in [PLAN.md](PLAN.md). Foundation packages FD001–FD008 are **done for the bounded prototype**; their implementation, runtime evidence, and limits are recorded in [foundation acceptance](docs/FOUNDATION.md). FD101–FD109 and FD201–FD204 are **done for their bounded builds**; see [0.1 acceptance](docs/PLAYABLE.md) and [0.2 acceptance](docs/MAGNETIC.md). Version 0.3 is done for its bounded five-family gate toward the active 0.9.0 goal; later gates remain planned. Version numbers define scope and order, not dates.

Refer to a package by its permanent ID, for example “implement FD104” or “review FD303.” Keep IDs stable when adding work; split a package using a suffix such as FD104A rather than renumbering existing packages. Record its status, implementation references, and validation evidence under its heading as work proceeds. Statuses are planned, in progress, blocked, and done. A package is done only when its acceptance criteria have evidence, not merely when code exists.

## Release map

The [integration release allocation](docs/INTEGRATION-BACKLOG.md#fit-within-the-current-release-plan)
plans the staged ecosystem against these gates. It retains Create and the existing
Engineered/Track Styles scope for 0.4–1.0, and proposes separate optional operations
and presentation expansions afterward. New integration implementation remains
paused pending user approval; staged artifacts do not expand advertised support.
Before an approved expansion begins, follow the backlog's
[version refresh policy](docs/INTEGRATION-BACKLOG.md#version-refresh-before-expansion).
Its November 2 catalog is a dated research snapshot; candidate stabilization
continues with the existing pins and artifact-specific acceptance evidence.

**Current stage: completing 0.4; no release candidate is accepted.** The bounded
five-family 0.3 gate remains complete. FD401–FD403 are open; FD901–FD904 may
prepare evidence, but cannot bypass FD404. Updated **2026-11-05 UTC**.

**Artifact being qualified:** committed source `8f370d0`, clean JAR SHA-256
`cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`.
The [current qualification record](docs/CURRENT-QUALIFICATION.md) owns identity,
profiles, evidence reuse and required retests. This build excludes the untracked
GameTest class present in a separate tested PAX JAR; all common entries match.
Later source/resource changes require a new recorded identity.
`2c5d9a7` is a historical frozen checkpoint, not the current candidate.

**Execution order:**

1. Maintain the artifact/evidence map in FD401A; reconcile new observations before scheduling retests.
2. Finish the finite FD401B–D and FD402A–F matrices below, including the packaged PAX recovery case shared with FD902C.
3. Complete FD403’s five connected-client build/ride/save walkthroughs and teaching review.
4. Close FD404 and freeze the feature-complete JAR and four intended profiles.
5. Qualify that identity through FD901–FD904, including migration, the park benchmark and camera comfort; review FD905, then FD1001–FD1003.

A [4,800-tick PAX source recovery](docs/evidence/0.9/fd401-pax-longhold-source-229e586/acceptance.md),
its [packaged named test](docs/evidence/0.9/fd401-pax-packaged-longhold-229e586/README.md),
[the bounded two-client PAX recovery replay](docs/evidence/0.9/fd401-pax-two-client-recovery-8f370d0/README.md),
[all-five-family one-player recovery restarts](docs/evidence/0.4/recovery/fresh-process-recovery/acceptance.md)
and [two-client Arrow recovery cycle](docs/evidence/0.4/recovery/two-client-recovery-lifecycle/acceptance.md)
have observed passes. Their records distinguish exact artifact identity from
historical development-classpath limits. The current PAX replay covers one
occupied seat and a long LIFTING support fault; active-phase cold restarts,
OUTBOUND link loss and the full multiplayer matrix stay open. Manual Arrow
boarding, bearing selection/attachment and physical lever operation also have
bounded evidence. The
[archived checkpoint history](docs/ROADMAP-CHECKPOINT-HISTORY-2026-11-04.md)
retains prior development milestones without making them the work queue.

Each active task records its scenario, configuration, pass condition, evidence
and next action. Parent FD IDs stay stable; suffix tasks refine their acceptance.
“Observed” means a bounded historical result exists; only an artifact-matched
acceptance or justified input-parity review closes a current qualification row.

| Stage | Packages | Status | Required result |
| --- | --- | --- | --- |
| Foundation | FD001–FD008 | Done for bounded prototype | Viable carrier architecture, strict cart/track eligibility, persistence, and rider integration. |
| 0.1 | FD101–FD109 | Done for bounded build | Playable controlled Arrow/S&S and first-generation ZacSpin families, original matching tracks, and ordinary-cart access where physically valid. |
| 0.2 | FD201–FD204 | Done for bounded build | S&S Free Spin vehicle and track with magnetic seat-rotation equipment. |
| 0.3 | FD301–FD305 | Done for bounded five-family gate | Revised ZacSpin variant and functional PAX-style shuttle system. |
| 0.4 | FD401–FD404 | In progress | Optional integration infrastructure, recovery evaluation, crafting, tutorials, and family examples. |
| 0.9 release candidate | FD901–FD905 | In progress; release gate open | Complete compatibility, lifecycle, migration, and performance evidence. |
| 1.0 | FD1001–FD1003 | Planned | Verified five-family release with documentation and reproducible artifacts. |
| Proposed 1.1 | Integration backlog; FD1101 fixed-seat Wing discovery | Planned; implementation paused | Separate operations adapters from a mechanically specified fixed-seat wing vehicle. |
| Proposed 1.2 | Integration backlog presentation/seat expansion | Planned; implementation paused | Coaster Skin, rotating-seat follow-up, Immersion, Waterparked and Create Coasters coexistence. |
| Proposed 1.3 | FD1301 Suspended; FD1302 Axis discovery | Planned; implementation paused | Independently specified swing/rotation, locks, seating, tracks and full models. |
| Proposed 1.4 | FD1401 Pipeline discovery | Planned; implementation paused | Resolve the intended pipeline design, then specify vehicle, seating and track. |
| Proposed 1.5 | FD1501 Spinning; FD1502 Roller Ball discovery | Planned; implementation paused | Separate mechanisms, prototypes and acceptance for each family/variant. |
| External compatibility | FD801 | Planned | Future Track Styles Arrow 4D integration when a qualifying functional representation is inspectable; does not block standalone releases. |

The dependency chain between release gates is foundation → 0.1 → 0.2 → 0.3 → 0.4 → 0.9 → 1.0. Preparation for later packages may start once their stated prerequisites are done; a release still requires all earlier release gates. Track Styles, a maintainer response, or a future external release is never a prerequisite for our original tracks.

## Foundation

### FD001 Pin the development stack

**Status: done (2026-09-30).** See [implementation and acceptance evidence](docs/FOUNDATION.md). This is the foundation prototype gate; production content and the documented coordinate limitation remain in later work.

**Depends on:** none. **Design:** D06, D07.

Create the development project with exact compatible versions and a reproducible build. Preserve the dependency audit and record actual selected artifacts. Add client, dedicated-server, and automated build configurations.

**Done when:** a clean build, client launch, and dedicated-server launch succeed with the base dependency stack; the selected versions and commands are documented.

### FD002 Prove cart and track eligibility hooks

**Status: done (2026-09-30).** See [implementation and acceptance evidence](docs/FOUNDATION.md). This is the foundation prototype gate; production content and the documented coordinate limitation remain in later work.

**Depends on:** FD001. **Design:** D02, D15.

Find the placement, assembly, snap/re-engagement, and engaged-curve hooks. Prototype family and running-gear checks before the generic Coasters snapper accepts a vehicle. Decide whether functional metadata suffices or distinct materials are necessary.

**Done when:** a wrong-family 4D prototype cannot attach, while a verified fitting ordinary cart can traverse the main rails without receiving 4D behavior. Record any required upstream accessors or mixins.

### FD003 Prove independent rotating carriers

**Status: done (2026-09-30).** See [implementation and acceptance evidence](docs/FOUNDATION.md). This is the foundation prototype gate; production content and the documented coordinate limitation remain in later work.

**Depends on:** FD001. **Design:** D09, D13.

Prototype driven and free rotary carriers on a Coasters chassis, including two groups with independent state. Evaluate the proposed Sable sublevel approach and the entity fallback if necessary.

**Done when:** a controlled full turn, free inertial motion, and smooth capture work without destabilizing the chassis. Choose and document the architecture and its limitations before detailed vehicle art.

### FD004 Prove rider and camera transforms

**Status: done (2026-09-30).** See [implementation and acceptance evidence](docs/FOUNDATION.md). This is the foundation prototype gate; production content and the documented coordinate limitation remain in later work.

**Depends on:** FD003. **Design:** D16.

Attach real passengers to the prototype. Compose chassis, carrier, seat, and look transforms consistently, including first-person and third-person views and dismount recovery.

**Done when:** a rider can invert and return to boarding pose, another client sees the same motion, and dismount restores normal camera behavior. No duplicate carrier transform is applied.

### FD005 Define functional track and vehicle contracts

**Status: done (2026-09-30).** See [implementation and acceptance evidence](docs/FOUNDATION.md). This is the foundation prototype gate; production content and the documented coordinate limitation remain in later work.

**Depends on:** FD002, FD003. **Design:** D01, D02, D04, D09.

Define stable family/variant IDs, running-gear contacts, required mechanisms, carrier groups, ordinary/4D roles, provider identity, clearance data, and versioned save formats. Separate appearance from functional eligibility.

**Done when:** the contract expresses all five planned families, ordinary-cart access, and external equivalent providers. An Arrow cosmetic style cannot grant Arrow mechanical compatibility, and locking a 4D carrier cannot bypass its family restriction.

### FD006 Build the simulation and regression harness

**Status: done (2026-09-30).** See [implementation and acceptance evidence](docs/FOUNDATION.md). This is the foundation prototype gate; production content and the documented coordinate limitation remain in later work.

**Depends on:** FD003, FD005. **Design:** D10–D13, D17.

Separate testable rotation math from game integration. Establish fixed-step fixtures for controlled profiles, free dynamics, multiple carriers, and loading presets; retain repeatable in-game prototype layouts.

**Done when:** tests detect erased full turns, incorrect rollback semantics, duplicated gravity/loading forces, and unstable timestep behavior. Record initial physics and rendering costs as a baseline.

### FD007 Implement authoritative state and persistence

**Status: done (2026-09-30).** See [implementation and acceptance evidence](docs/FOUNDATION.md). This is the foundation prototype gate; production content and the documented coordinate limitation remain in later work.

**Depends on:** FD004, FD005. **Design:** D09, D16.

Persist vehicle/carrier identities, angle and angular velocity, family binding, occupancy, profiles, and lock state. Implement initial tracking snapshots and corrections without relying on matching client physics.

**Done when:** two clients, late joining, disconnect/rejoin, and save/reload preserve correct state and eligibility. Side-specific classes do not break dedicated-server startup.

### FD008 Foundation gate

**Status: done (2026-09-30).** See [implementation and acceptance evidence](docs/FOUNDATION.md). This is the foundation prototype gate; production content and the documented coordinate limitation remain in later work.

**Depends on:** FD002–FD007. **Design:** D18.

Review prototype evidence and record the architecture decisions, version-sensitive hooks, unresolved risks, and revised effort estimate.

**Done when:** the selected architecture supports both controlled and free carriers with real passengers, strict family matching, and persistent multiplayer state. Unresolved failures in these essentials block vehicle-content work from being called release-ready.

## Version 0.1 controlled and legacy free spin

### FD101 Build matching original tracks

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD005, FD008. **Design:** D01, D02, D08, D13.

Build the original Arrow/S&S functional four-rail track and legacy ZacSpin-compatible track, including items, rendering, required mechanical data, and placement previews. Reuse graph infrastructure while preserving distinct family eligibility.

**Done when:** both tracks work without Track Styles, retain stable IDs through saves, and expose verified running-gear geometry. Dimensions and clearance are documented rather than assumed from gauge alone.

### FD102 Complete controlled rotation profiles

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD006, FD007, FD101. **Design:** D10, D13.

Implement arc-length sampling, per-axle evaluation, unwrapped angles, interpolation, continuity, rail-displacement mapping, and profile rebinding. Reuse one profile for visible control rails and mechanical targets.

**Done when:** stops hold the target, rollback retraces the profile, full turns survive interpolation, and closed circuits satisfy mechanical continuity. Crossing tracks cannot steal the binding.

### FD103 Complete free carrier dynamics

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD006, FD007, FD101. **Design:** D09, D11.

Implement gravity/inertia behavior, damping, occupancy mass, and the legacy pivot preset using the selected physics architecture.

**Done when:** motion responds to declared loading and chassis movement, retains angular state through reversals, and settles plausibly with damping. It is not a timed or randomized flip animation.

### FD104 Build the first two vehicle families

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD101–FD103. **Design:** D01, D02, D08, D09.

Create original controlled Arrow/S&S-inspired cars and legacy ZacSpin-inspired vehicles, including seats, restraints, pivots, and family-specific coupling rules. Validate the original controlled train linking requirement.

**Done when:** both families carry players through their demonstration layouts on matching tracks, with correct independent/per-car motion, clearance, linking, and persistent state.

### FD105 Add editing and ride previews

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD102–FD104. **Design:** D08, D10, D11.

Provide profile editing, free-spin tuning, copy/mirror behavior, ghost seats, and swept-clearance previews. Validate permissions, finite values, revisions, and payload bounds server-side.

**Done when:** edits survive reload, occupied sections reject unsafe edits, and previews distinguish prescribed controlled poses from free-spin examples under specified starting conditions.

### FD106 Add boarding and maintenance retention

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD104, FD007. **Design:** D03, D09, D16.

Implement approach alignment, angular damping/capture, restraints, ready/locked signals, and supported maintenance retention. Restore normal rider state on recovery/dismount paths. Engineered may optionally supply longitudinal stop/go and launch operations through FD401, while Fourth Dimension retains carrier capture, restraints, readiness and retention authority; it is not a mandatory dependency.

**Done when:** boarding requires stopped translation, aligned carriers, and low angular speed; restraints cannot release prematurely. Retention never changes a vehicle’s required track family.

### FD107 Complete eligibility enforcement

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD002, FD005, FD104. **Design:** D02, D17.

Enforce the contract across placement, assembly, packets, snapping, route transitions, dispatch, and reload. Check each vehicle in a linked consist and handle unexpected support loss.

**Done when:** the compatibility matrix passes, including wrong-family vehicles with seats locked, cosmetic-only tracks, mixed consists, and approach to an incompatible boundary in both directions. No automatic cart resizing or family downgrade occurs.

### FD108 Verify ordinary cart access

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD101, FD107. **Design:** D02, D03.

Measure and test ordinary-cart main-rail contact and clearance, including side-guide/upstop interfaces where applicable. Document at least one fitting and one non-fitting case.

**Done when:** accepted ordinary carts retain ordinary behavior and all 4D seat effects ignore them. Publish rail access separately from towing support; FD402 owns the towing evaluation.

### FD109 Version 0.1 gate

**Status: done (2026-09-30), bounded 0.1 build.** See [implementation and acceptance evidence](docs/PLAYABLE.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD101–FD108.

Package a playable build with both original tracks, both vehicle families, essential recipes/instructions, and small example layouts. Later documentation work expands this baseline rather than leaving early builds unexplained.

**Done when:** a clean install without optional addons completes both rides in single-player and on a dedicated server, with documented eligibility and ordinary-cart access. Record the artifact and test evidence.

## Version 0.2 magnetic free spin

### FD201 Build the S&S Free Spin family

**Status: done (2026-09-30), bounded 0.2 build.** See [implementation and acceptance evidence](docs/MAGNETIC.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD109. **Design:** D01, D02, D09.

Confirm the chosen seat-group arrangement and build the eight-seat vehicle, matching functional track, and independent carrier definitions. Keep uncertain real-world coupling details explicitly labeled until verified.

**Done when:** the vehicle carries eight riders with correct group transforms and family restrictions, and its full swept geometry clears the track and attachments.

### FD202 Implement magnetic spin equipment

**Status: done (2026-09-30), bounded 0.2 build.** See [implementation and acceptance evidence](docs/MAGNETIC.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD201, FD006. **Design:** D11, D12.

Implement bounded inducer and damper spans with direction, strength, falloff, carrier targeting, and traversal detection. Keep seat torque distinct from longitudinal propulsion.

**Done when:** high-speed and reverse traversal cannot skip or double-apply effects, passive equipment cannot indefinitely power a stationary vehicle, and ordinary carts receive no seat torque.

### FD203 Add tuning and capture workflows

**Status: done (2026-09-30), bounded 0.2 build.** See [implementation and acceptance evidence](docs/MAGNETIC.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD202, FD105, FD106. **Design:** D08, D11, D16.

Expose magnetic-zone editing, loading presets, motion previews, and boarding capture for all carrier groups.

**Done when:** users can compare declared loading/tuning cases, edits are validated, and every group must settle and align before restraint release.

### FD204 Version 0.2 gate

**Status: done (2026-09-30), bounded 0.2 build.** See [implementation and acceptance evidence](docs/MAGNETIC.md). The near-origin and capacity limits remain; later families and integrations retain their own gates.

**Depends on:** FD201–FD203.

Ship the magnetic family with original track, usable equipment, instructions, and an example layout.

**Done when:** multiplayer, reload, eligibility, energy-bound, and ordinary-cart exclusion tests pass; both 0.1 families remain functional.

## Version 0.3 revised carriers and shuttle operation

### FD301 Resolve revised ZacSpin design evidence

**Status: done (2026-09-30), inspired topology specification.** See [primary evidence, original geometry, and measured mass properties](docs/REVISED-SHUTTLE.md). Generation-specific replica fidelity remains unclaimed.

**Depends on:** FD005. **Design:** D01, D18.

Investigate primary references for revised seating, pivots, and carrier coupling. Document the chosen design and which details are confirmed versus original game approximations.

**Done when:** there is a reviewable topology/pivot specification. If exact historical geometry remains unverified, the deliverable must be labeled an inspired variant and must not claim exact replica fidelity.

### FD302 Build the revised ZacSpin variant

**Status: done (2026-09-30), bounded inspired-variant package.** Original matching track, four independent paired rotors, lowered pivots and measured inertia, eight native passengers, circuit traversal, all-family locked eligibility, two real clients, occupied moving cold restart, and final platform exits have evidence. See [acceptance and remaining release gates](docs/REVISED-SHUTTLE.md).

**Depends on:** FD204, FD301. **Design:** D01, D02, D09, D11.

Build the revised carrier preset, vehicle, and corresponding track contract/models. Reuse parts only where the physical interfaces match; do not silently allow cross-variant carts.

**Done when:** the variant behaves distinctly through its documented geometry and inertia, preserves independent carrier state, and passes boarding, multiplayer, and track-eligibility tests.

### FD303 Build the PAX shuttle train and track

**Status: done (2026-09-30), bounded shuttle train package.** Four-car native coupling, sixteen native passengers, original matching track, guarded ends, gravity reversal, retained engagement, and two taller-example cycles have automated evidence. Two real riders also completed an occupied dedicated-server restart/resume and a second cycle. Complete release lifecycle coverage remains FD902; see [acceptance](docs/REVISED-SHUTTLE.md).

**Depends on:** FD204. **Design:** D01–D03, D14.

Build a linked free-spin shuttle train, its original matching track, and guarded spike/end behavior. Prototype loaded coupling and reverse traversal.

**Done when:** the train reverses while retaining carrier state and track engagement, respects its track family, and cannot mistake an open endpoint jump for a valid shuttle turnaround.

### FD304 Implement shuttle lift and capture

**Status: done (2026-09-30), bounded lift/capture package.** The powered native carriage physically catches, lifts, releases, and recaptures the train. Power-loss hold/resume, station restraint checks, loss of route physics availability, real winch reload, and physical station recovery pass. Occupied mid-lift cold restart held the carriage coordinate, restored riders, required normal explicit operator resume, and completed capture/unloading. The unexplained first client exit 137 remains a release investigation in FD902/FD904; see [evidence and limitations](docs/REVISED-SHUTTLE.md).

**Depends on:** FD303, FD106. **Design:** D14, D16.

Validate existing lift compatibility; implement the catch/release and lift-carriage adapter if required. Add the boarding, lift, release, outbound, return, final capture, and unloading state machine.

**Done when:** the train completes a real lift-and-release cycle, passes through the station without premature unloading, and returns to controlled capture. Test interrupted lifting and loss of chunk availability. A launch-only fixture does not satisfy this package.

### FD305 Version 0.3 gate

**Status: done (2026-09-30), bounded five-family 0.3 gate.** Current packaged single-player evidence completes all five rides and normal exits, migrates the old three-family schema-3 world without losing profiles/mechanisms/IDs, and preserves all eight vehicles and twenty carriers through a world reopen. Together with native and occupied dedicated-server evidence, this closes the 0.3 gate. See [single-player acceptance](docs/evidence/0.3/integrated-acceptance.txt). Tutorials/integrations, transient native movement-packet messages, and the unexplained client resource exit retain their later release gates.

**Depends on:** FD302–FD304.

Ship both new families with instructions and examples, including compact and taller shuttle layout examples where supported.

**Done when:** all five families have original usable vehicles and matching tracks; complete shuttle cycles and revised-carrier behavior pass dedicated-server and save/reload checks.

## Version 0.4 integration and complete content

### FD401 Deliver optional provider infrastructure

**Status: in progress.** Infrastructure and substantial historical operator/render
acceptance exist. Qualification now follows the [artifact record](docs/CURRENT-QUALIFICATION.md),
not the old `2c5d9a7` label. The detailed [handoff](docs/FD401-RELEASE-HANDOFF.md)
and [historical milestones](docs/ROADMAP-CHECKPOINT-HISTORY-2026-11-04.md#fd401-deliver-optional-provider-infrastructure)
retain their original evidence boundaries.

**Depends on:** FD005, FD305. **Design:** D04, D05, D07, D15.

Current scope is Create guidance/diagnostics, Track Styles 1.2.0 Arrow appearance
and Engineered 1.1.1 operations, on schema 6/protocol 7. Missing or unsupported
optional mods must leave standalone behavior intact. New adapters and the
schema-7/protocol-8 registry remain paused; FD801 is independent.

| Task / status | Required scenario and configuration | Pass condition | Evidence / next action |
| --- | --- | --- | --- |
| **FD401A Artifact identity — done for `8f370d0`; refresh on input changes** | One immutable JAR, committed source, intended base/Styles/Engineered/combined profiles | Source/JAR/dependency identities and every reused claim have a bounded mapping; runtime profile hashes attach when launched | [Current record](docs/CURRENT-QUALIFICATION.md) and [clean source/JAR manifest](docs/evidence/qualification/2026-11-05-8f370d0/README.md). Build identity is established; four-profile qualification is not. The two-client PAX JAR has one extra uncommitted GameTest class and otherwise identical entries. |
| **FD401B Styles — in progress** | Both Arrow styles, single and coupled banked/full-turn rides; whole-section Original/single/double transitions; FANCY/FAST/FABULOUS and the pinned Iris/Solas setup | Normal UI changes preserve family/profile; one render owner per component; fallback, provider removal/reinstall, reopen and reload preserve mechanics; occupied ride/capture/unload and geometry checks pass | [Handoff](docs/FD401-RELEASE-HANDOFF.md#fd401-evidence-review-and-disclosed-boundaries). The retained-input re-audit passes five sampled style rows and five swept-interpolation rows; `replay.py --verify-only` now confirms the bundle, current source hashes, dependencies and retained input payloads against this checkout. This closes provenance/parity review for those retained poses, not current-runtime geometry. The direct stale removed-font branch has a bounded worktree-JAR pass ([evidence](docs/evidence/0.9/fd401-removed-stale-probe-worktree-20261105/README.md)); it does not qualify the selected artifact. Replay changed runtime paths on the selected JAR. Tracing-disabled reload/ride and three reload/ride repetitions per supported nonshader mode remain open. |
| **FD401C Engineered — in progress** | Engineered-only and combined profiles: all five families; single/coupled Arrow; loaded PAX opposing-motor authority; wired FE | Normal construction/controller operation, launch/STOP/capture/unload, starvation, power restore and cold reopen preserve riders and interlocks; all PAX chassis reject competing drive | Current-JAR [Arrow/FE/reopen](docs/evidence/0.9/fd401-engineered-current-arrow-fe-reopen/README.md), [wired FE interruption/restore](docs/evidence/0.9/fd401-engineered-current-fe-interruption/README.md), [Legacy](docs/evidence/0.9/fd401-engineered-current-legacy-fe/README.md), [Free Spin](docs/evidence/0.9/fd401-engineered-current-free-spin-fe/README.md), [Revised](docs/evidence/0.9/fd401-engineered-current-revised-fe/README.md), [coupled Arrow](docs/evidence/0.9/fd401-engineered-current-coupled-arrow-fe/README.md), selected-JAR [PAX opposing-motor authority](docs/evidence/0.9/fd401-engineered-current-pax-authority/README.md), and fresh selected-JAR [Arrow construction](docs/evidence/0.9/fd401-engineered-current-fresh-arrow/README.md) are bounded Engineered-only results. The fresh Arrow run covers direct placement, normal station binding, occupied dispatch, capture, and unload. The selected-JAR [combined-profile Arrow operator cycle](docs/evidence/0.9/fd401-engineered-current-combined-arrow/README.md) proves one occupied Arrow cycle, not the family/profile matrix. The selected-JAR [combined five-family motor fixture](docs/evidence/0.9/fd401-engineered-current-combined-five-family/README.md) proves bounded occupied launch/STOP/capture/retention for the four driven families and PAX motor rejection with Styles installed; it uses direct fixtures, not normal UI operation. The restored Arrow made a short automatic pass and needed manual capture away from the station. A [four-car native callback fixture](docs/evidence/0.9/fd401-engineered-fourcar-motor-guard/README.md) observes zero competing impulse on each occupied PAX chassis. The coupled-Arrow replay reuses a normally linked saved train; continuous link tracing remains unproven. Complete combined-profile normal-client operator coverage and current-JAR construction for the remaining families. FE fixture dependency remains disclosed, not advertised. |
| **FD401D Create telemetry — in progress** | All six display sources; each family, trailing-car occupancy, controller unavailable/re-entry and PAX phase/fault recovery; base and optional profiles | Goggles, signs and client snapshots agree with current whole-consist server state; unavailable equipment cannot advertise ready or authorize motion | [Telemetry handoff](docs/FD401-RELEASE-HANDOFF.md) and [bounded two-client PAX recovery](docs/evidence/0.9/fd401-pax-two-client-recovery-8f370d0/README.md). The PAX run observes phase/fault/recovery with one rider; complete the six-source and profile matrix. |

**Done when:** FD401A–D have artifact-matched acceptance for the advertised
profiles. Within-curve style segmentation, arbitrary cosmetic/resource-pack
meshes, accelerated Flywheel under the recorded shader fallback, and larger
unmeasured packet-delay envelopes are not certified by existing evidence.
A failed advertised feature must be fixed or explicitly disabled/withdrawn with
updated support documentation; a historical pass cannot waive it.

### FD402 Evaluate and document recovery towing

**Status: in progress.** Native towing, bounded geometry and strength, manual
Arrow operator actions, and all-family historical one-player restarts are
observed. Rendered occupied operation, artifact-matched lifecycle and multiplayer
acceptance remain finite tasks below. See [operator evidence](docs/evidence/0.4/recovery/operator-acceptance.md)
and [historical milestones](docs/ROADMAP-CHECKPOINT-HISTORY-2026-11-04.md#fd402-evaluate-and-document-recovery-towing).

**Depends on:** FD108, FD106, FD305. **Design:** D03.

**Recovery scope for 0.4–1.0:** pinned unmodified bare Coasters bearing, original
matching tracks, all five original single-car families, the original two-car
Arrow and four-car PAX banked examples, and the separately measured four-car
Arrow flat strength case. Four cars is a maximum rating, not certification of
every four-car route. Use the demonstrated native-chain eight-RPM drive and
0.75 blocks/s production ceiling. Keep captured/retained carriers and the
server-enforced vanilla equipment envelope. Include the stopped PAX winch
handover and the bounded Engineered ordinary-bearing FE case.

Added ordinary assemblies/survival locomotives, more than four cars, arbitrary
drive settings, steeper/unmeasured routes, compact PAX whole-spike towing and
modded cosmetic/resource overrides remain unsupported. Preserve rejection,
stall, overspeed and equipment-change guards. Extending this set requires an
explicit scope revision with its own acceptance; it is not hidden remaining work.

| Task / status | Required scenario and configuration | Pass condition | Evidence / next action |
| --- | --- | --- | --- |
| **FD402A Mechanics and clearance — in progress; bounded native results observed** | Five single-car + two linked banked routes; four-car Arrow flat strength; original native parts and certified equipment | Preserve all IDs, riders, couplings and Arrow profiles through tow/brake/power-cut/detach/unload; reproduce pinned minimum-clearance thresholds without relaxing them; five-car, wrong-family, bad-equipment, stall and overspeed cases fail closed | [One-tick](docs/evidence/0.4/recovery/current-tick-rider-clearance/acceptance.md), [substeps](docs/evidence/0.4/recovery/remaining-family-native-physics-substeps/acceptance.md), [wearables](docs/evidence/0.4/recovery/vanilla-wearable-clearance/acceptance.md), [strength](docs/evidence/0.4/recovery/strength-rating/acceptance.md). Compare complete input hashes with the qualification build and rerun changed inputs; retain sampled/discrete-solver limits. |
| **FD402B Rendered recovery — in progress** | The same seven banked routes in a connected client; real mounted vanilla player, rack, linkage and both linked trains | Observe moving player/body/rack/track relationships through bend, sustained bank, runout and release, with identity/mount retention; render observations and native-frame clearance evidence agree | [Rendered train](docs/evidence/0.4/recovery/rendered-recovery-train/acceptance.md). The render-probe code is instrumentation, not a pass. Capture the missing occupied moving views on the selected JAR, including certified vanilla wearable alternatives. |
| **FD402C Operator workflow — in progress; bounded manual Arrow actions observed** | One normal occupied flat workflow per family, linked Arrow/PAX, and stationary PAX handover; standalone | Real bearing selection, Attach/Start/Brake controls and physical lever power; brake before boundary; stationary detach, restraint opening and normal unload; no fixture dispatch substitutes for input | [Manual attachment](docs/evidence/0.4/recovery/manual-bearing-selection/acceptance.md), [lever](docs/evidence/0.4/recovery/manual-lever-propulsion/acceptance.md), [earlier moving brake](docs/evidence/0.4/recovery/power-control/acceptance.md). Join the established actions into occupied end-to-end rows on the selected JAR. |
| **FD402D One-player persistence — in progress; historical five-family observation complete** | Each single-car family plus original linked Arrow/PAX: save braked recovery, exit JVM, reopen same world, explicitly resume and normally release | Same chassis/bearing/carriers/native links/rider return; no automatic tow; final detach/unload clears assignment; both processes exit normally | [Retained five-family logs](docs/evidence/0.4/recovery/fresh-process-recovery/acceptance.md#retained-observations-and-artifact-boundary). Missing historical launch identity prevents exact-artifact promotion. Run packaged identity-backed cases, preserving rather than repeating the historical claim. |
| **FD402E Multiplayer interruptions — in progress; one Arrow cycle observed** | Two real clients in distinct seats across five families, plus linked Arrow/PAX; dedicated base and combined profiles | Disconnect/rejoin one rider while the other stays; cold restart braked recovery; chunk unload/reload; coupling/bearing loss and restore/removal all retain or safely release original identities with no duplication or automatic resume | [Two-client Arrow cycle](docs/evidence/0.4/recovery/two-client-recovery-lifecycle/acceptance.md) closes its bounded fixture-operated tow/release observation. Add artifact manifests and remaining family/interruption rows. FD902B consumes this same matrix; keep permanent removal distinct from temporary unavailability. |
| **FD402F Optional power — in progress; bounded native FE result observed** | Certified ordinary bearing, occupied Arrow recovery, Engineered-only and combined; declared FE producer/wire fixture | Manual controller and supply actions demonstrate braked/starved holds, explicit towing/consumption, capped speed, restored power without automatic motion, rebraking and cleanup | [FE checkpoint](docs/evidence/0.4/recovery/engineered-fe/acceptance.md). Record the missing rendered manual case. All-family Engineered recovery and stronger ordinary assemblies are excluded from the current claim. |

**Done when:** FD402A–F pass for this explicit scope, unsupported cases remain
rejected or clearly documented, and [RECOVERY.md](docs/RECOVERY.md) matches the
qualified artifact. This replaces the ambiguous “broader lifecycle/propulsion”
queue. It does not reduce any advertised original family or discard prior failures.

### FD403 Complete crafting and teaching content

**Status: in progress.** Crafting, placement, Ponder/tooltips and bounded rides
have evidence. The remaining gate is the
[connected-client acceptance matrix](docs/FD403-ACCEPTANCE.md), not another
synthetic-player or command-authored substitute.

**Depends on:** FD305. **Design:** D08.

| Task / status | Remaining standalone connected-client exercise | Evidence / pass condition / next action |
| --- | --- | --- |
| **FD403A Arrow — in progress** | Full player-built powered circuit with controlled full turn | Follow the Arrow row in the [matrix](docs/FD403-ACCEPTANCE.md#evidence-already-recorded): normal recipes, paid connections, client Chain input, boarding, full circuit, capture/unload and cold reopen. The earlier 50-block pass remains bounded. |
| **FD403B Legacy — in progress** | Player-built shaped powered route with visible free response | Follow the Legacy row; preserve independent occupied motion and normal capture/unload/reopen. A flat powered traverse or command-authored demo is not this gate. |
| **FD403C Free Spin — in progress** | Player-built shaped route with installed inducer and damper spans | Follow the Free Spin row; measure released-carrier damper impulses before capture, complete the normal ride and reopen. Retain existing demo/native damper evidence. |
| **FD403D Revised — in progress** | Player-built powered route with independent lower-hinge behavior | Follow the Revised row; complete normal construction/operation/reopen and the family walkthrough. The existing powered demo is historical support. |
| **FD403E PAX — in progress** | Complete the survival-built four-car tutorial, including ordinary client linking and winch setup | Existing survival-built cycle is observed; verify the walkthrough on the selected JAR, disclose upstream motor/material fixtures, and retain all original identities through recatch/unload/reopen. |
| **FD403F Teaching — in progress** | All fifteen item descriptions, nine unique Ponder lessons and 53 registrations on the selected artifact | Every family walkthrough is usable through normal inventory controls, distinguishes mechanics and exclusions, and has readable captions/UI. Reuse unchanged resource evidence only after parity review; fix and repeat misleading steps. |

**Done when:** FD403A–F meet the existing [recording procedure](docs/FD403-ACCEPTANCE.md#acceptance-record).
Each family has an acquisition/build path and tutorial that actually completes
its advertised mechanics without optional mods. Disclosed fixture ingredients
are not unassisted resource-gathering evidence.

### FD404 Version 0.4 gate

**Status: planned; awaiting FD401–FD403 acceptance.**

**Depends on:** FD401A–D, FD402A–F, FD403A–F.

Freeze the feature-complete source/JAR, profile dependency hashes and support
scope in [CURRENT-QUALIFICATION.md](docs/CURRENT-QUALIFICATION.md). Keep base,
Styles, Engineered and combined as the intended 0.9/1.0 set. A new integration
or recovery scenario requires an explicit scope revision and funded acceptance.

**Done when:** all named 0.4 tasks pass, five-family teaching is complete, advertised
integrations have evidence, and remaining work is stabilization and the explicitly
assigned 0.9 gates. Completed 0.3 evidence stays bounded to its own build.

## Version 0.9 release candidate

### FD901 Run the full compatibility matrix

**Status: in progress; historical pre-FD404 matrix observed.**

**Depends on:** FD404. **Design:** D02, D04, D17.

Run base, Styles-only, Engineered-only and combined on the frozen artifact.
Exercise five matching and twenty directed wrong-family cases through placement,
assembly, initial snap, re-engagement, dispatch, transition and save/load; locked
carriers cannot bypass family matching. Cover both directions of mixed-consist
coupling, ordinary bare-bearing fit/non-fit, cosmetic-only tracks and occupied
boundary braking. No third-party functional track is advertised.

**Evidence / next action:** the [historical profile matrix](docs/evidence/0.9/profile-matrix-abc4a9a/README.md)
passed its 49-test inventory. Inventory current tests and map every required
entry path before replaying four profiles; record actual test counts, logs,
worlds and hashes rather than retaining “49” as a current-suite claim.

**Done when:** every advertised combination and rejection path has results for
the selected identity; ordinary carts acquire no 4D effects and a rejected
operation does not steal a binding or partially mutate the consist.

### FD902 Validate multiplayer and lifecycle recovery

**Status: in progress.** Historical stationary, moving, support-repair and
active-restart checkpoints exist; their [history](docs/ROADMAP-CHECKPOINT-HISTORY-2026-11-04.md#fd902-validate-multiplayer-and-lifecycle-recovery)
is not a full current-artifact pass.

**Depends on:** FD404. **Design:** D14, D16, D17.

| Task / status | Required matrix and pass condition | Evidence / next action |
| --- | --- | --- |
| **FD902A Normal ride lifecycle — in progress** | Five families, two real clients in distinct seats, base and combined: late join, stationary/moving reconnect, chunk interruption, cold restart, disassembly/support loss, exact repair and realignment; retain correct identities, locks and family; no automatic restart or duplicate/lost riders | [Occupied lifecycle](docs/evidence/0.9/occupied-disconnect-reconnect.md), [ordered repair](docs/evidence/0.9/two-client-five-family-ordered-recovery.md). Map historical rows to current inputs, then close post-repair reconnect/restart and affected moving paths. |
| **FD902B Recovery lifecycle — in progress; bounded observations retained** | FD402D–E matrix, including original linked trains and permanent versus temporary missing components | Consume those same evidence bundles; the five-family one-player restart and two-client Arrow cycle are observed. Do not treat a normal ride restart as a recovery-mode restart or rerun an identical accepted case just to change its package label. |
| **FD902C PAX phase interruptions — in progress** | LIFTING support loss for 4,800 ticks; separate OUTBOUND link-loss regression; Repair → Realign → Recover → occupied full cycle/unload. Cold restart during each active phase LIFTING/OUTBOUND/RETURNING/CAPTURING; preserve all four cars, eight carriers, links, controller/carriage and riders, and require explicit recovery | [Source success](docs/evidence/0.9/fd401-pax-longhold-source-229e586/acceptance.md), [packaged named-test pass](docs/evidence/0.9/fd401-pax-packaged-longhold-229e586/README.md), [historical OUTBOUND failure](docs/evidence/0.9/fd401-pax-longhold-client-blocker-1f13a0e/README.md), [bounded active restart](docs/evidence/0.9/create-telemetry/a1aeb1a-active-pax-restart/README.md). Build artifact-linked packaged normal-client and two-client records; include two consecutive restart/recovery cycles per phase. FD401D observes telemetry in this same run. |

**Done when:** these supported-case matrices have no unresolved rider-state,
restraint, identity, rebinding or unintended-resume failures. Document supported
operator recovery for each interruption. Do not claim every timing race from a
single successful restart.

### FD903 Validate save migration and provider changes

**Status: in progress.** Schema-1–5 loader regressions, a real historical schema-3
world, appearance removal/reinstall and bounded provenance backfill have evidence.

**Depends on:** FD404. **Design:** D04, D09, D10, D16.

On the selected JAR, retain the loader matrix and real historical schema-3 upgrade,
sustained Arrow/Legacy/Free Spin rides, two-client seat restoration, save/reopen,
backup restoration and appearance-provider removal/reinstall. Preserve all saved
rail/profile/chassis/carrier IDs. Intact legacy rails may gain exact provenance;
changed/foreign/missing rails must not acquire invented geometry. Missing functional
providers and unsupported conversions reject clearly; schema-7 registry migration
is outside this release.

**Evidence / next action:** [migration procedure](docs/MIGRATION.md),
[historical world](docs/evidence/0.9/native-schema3-world-replay.md),
[provenance backfill](docs/evidence/0.9/create-telemetry/307ec35-legacy-provenance/README.md).
Complete sustained historical Legacy/Free Spin rides and two-client records;
record the exact backup and restored save hashes. Synthetic schema labels do not
stand in for a world produced by an older release.

**Done when:** declared supported upgrades preserve families/profiles and riders,
unsupported conversions refuse safely, and the documented backup recovery works.

### FD904 Meet measured performance and rendering targets

**Status: in progress.** Host-scoped addon timing limits exist; whole-frame/tick,
memory and sustained-park acceptance have not yet been measured.

**Depends on:** FD404, FD006. **Design:** D16, D17.

| Task / status | Required scenario and pass condition | Evidence / next action |
| --- | --- | --- |
| **FD904A Rendering — in progress** | Five-family moving rider/body/camera checks and FD401B’s advertised graphics profiles; nonempty `geometryStages` >=500, correct ownership and visible geometry, clean reload and shutdown; solver/frame/rider evidence retains its measured bounds | [Historical render evidence](docs/evidence/0.9/client-render-sampling.md) and [artifact reuse map](docs/CURRENT-QUALIFICATION.md). Exact `2c5d9a7` FAST/FABULOUS gates already exist; replay changed paths, not an obsolete “no geometry-stage replay ever” claim. FD402B supplies recovery views. |
| **FD904B Representative park — planned** | The fixed mixed-family scene, declared hardware/settings, frame/tick percentiles, heap/RSS/cgroup and GC measurements, baseline-derived budgets and 30-minute sustained run defined in [PERFORMANCE.md](docs/PERFORMANCE.md#representative-park-acceptance-fd904b) | Implement/validate whole-frame and whole-server collectors; freeze the baseline before judging the candidate. Keep 120,000 ns/runtime invocation and 6,000 ns/carrier limits in all four native profiles. No existing average or llvmpipe capture proves GPU smoothness. |
| **FD904C Rider camera and comfort — planned; required for 1.0** | Deliver D16’s optional stabilized camera without changing server seat motion; all five families, upright/banked/inverted poses, free mouse look, first-person arms, third-person/remote body, reconnect and dismount restoration | [D16](PLAN.md#d16-riders-and-synchronization). Inventory existing Sable support, implement the missing client option if necessary, and capture normal/comfort comparisons on the same ride state. Record both modes and restore the prior camera on exit. |

**Done when:** FD904A–C pass on stated configurations, the performance budget is
recorded before candidate comparison, and no unresolved regression prevents the
declared benchmark scene. Capacity ceilings are storage/admission limits, not a
promise that every maximum-sized world meets the benchmark timings.

### FD905 Release candidate gate

**Status: planned; awaiting FD901–FD904 and all earlier release gates.**

**Depends on:** FD901–FD904.

Prepare the candidate artifact, exact profile matrix, reproducible scenarios,
defect list and support limits for review. Publication remains a separate action.

**Done when:** all named gates pass; crashes, save corruption, rider-state failures
and eligibility bypasses affecting supported cases are resolved; affected cases
are retested after fixes. Every accepted claim points to the frozen artifact or
an explicit complete input-parity review. No stale “current” narrative substitutes
for that record.

## Version 1.0

### FD1001 Finalize the release documentation

**Status: planned.**

**Depends on:** FD905.

Finalize installation instructions, version/dependency matrix, five-family guides, recovery limits, attribution, integration terms, known issues, and changelog. Label inspired approximations honestly.

**Done when:** the documentation matches the release artifact and does not advertise unverified towing, historical fidelity, or future Track Styles support.

### FD1002 Reproduce and verify release artifacts

**Status: planned.**

**Depends on:** FD905, FD1001.

Build from the intended release revision, record checksums, verify packaged metadata/assets, and smoke-test clean client and dedicated-server installs using the documented dependencies.

**Done when:** the artifact is reproducible, contains only intended distributable content, and clean installs complete one full ride for every supported family with correct track restrictions.

### FD1003 Version 1.0 release gate

**Status: planned.**

**Depends on:** FD1001, FD1002 and all required earlier gates.

Review the finished release against the definition below. Publishing is a future execution action, not authorized or performed by this planning document.

**Done when:** the release checklist and evidence are complete; when publication is separately requested, the published artifacts and release notes match the verified build.

Version 1.0 requires all five functional vehicle families with original matching tracks, both controlled and free dynamics, magnetic assistance, shuttle operation, strict asymmetric cart compatibility, correct riders/camera, persistent multiplayer state, usable tutorials/recipes, tested migrations, and measured performance. Ordinary-cart rail access is verified; towing is claimed only where proven. Standalone completion does not require unreleased external features.

### Supported envelope and explicit 1.0 dispositions

These are release-planning decisions, not new runtime acceptance. FD1001 must
publish them consistently; FD1002 must verify the boundaries on the release JAR.

| Requirement | 1.0 disposition | Acceptance owner |
| --- | --- | --- |
| Horizontal world coordinates | Accept the existing ±1024-block x/z supported region for 1.0; arbitrary-distance operation is deferred, not silently promised | FD901 verifies placement/operation boundaries and safe refusal outside; FD1001 documents them. Raising the limit requires a separate native precision proof. |
| Dimension capacities | Retain 256 sections, 64 vehicles, 8192 total profile keys and 512 magnetic spans, at most eight spans per section | FD901 verifies exact-limit/one-over-limit admission and reload behavior; FD903 verifies no partial mutation or silent save truncation. FD904B separately bounds tested workload performance. |
| Recovery envelope | Accept only the FD402 declared bearing, family/consist/route, drive, equipment and provider set | FD402A–F certify it; unmeasured grades, assemblies, compact-spike pulls and cosmetic overrides stay unsupported. |
| Stabilized comfort camera | Deliver before 1.0 as already specified in D16; not implicitly deferred | FD904C implements/verifies the option. If it cannot pass, 1.0 stays open unless the design requirement is explicitly revised. |
| Five families, original tracks, supported upgrades and four intended profiles | Preserve existing release scope and evidence gates | FD404/FD905; future families and integrations remain separately paused. |

## External compatibility work

### FD801 Integrate a released Track Styles Arrow 4D

**Status: planned; awaiting a qualifying inspectable external release.**

**Depends on:** FD401 and an inspectable external release with applicable terms established. **Design:** D02, D04, D05.

Inspect the actual representation and implement an independently authored optional adapter. A cosmetic-only style must decorate matching functional track or undergo an explicit functional conversion; it cannot make ordinary track eligible by appearance alone.

**Done when:** our Arrow vehicles correctly use the verified equivalent functional track, ordinary fitting carts receive no 4D effects, control rails have one render owner, profiles persist, and same-family provider transitions pass multiplayer/lifecycle tests. Record supported versions. If the external release is unavailable, keep this package pending without blocking 1.0.

## Proposed vehicle discovery after 1.0

These packages scope the user's proposals; **implementation and prototype runs
remain paused pending explicit approval**. Their release labels are proposed
allocations, not dates or authorization. Completing 1.0 does not lift the pause.
The [integration backlog](docs/INTEGRATION-BACKLOG.md) owns addon adapters, while
the vehicle packages below own mechanics and assets. Neither substitutes for the
other. A discovery package completes with a reviewed specification and test plan,
not a claim that its vehicle is implemented.

For every package, retain references and distinguish confirmed mechanisms from
game approximations. Record dimensions, running/guide/upstop contacts, seats and
restraints, pivot axes, mass/loading, coupling, persistence, render/camera ownership,
ordinary-cart fit and family eligibility. Any external track needs its own verified
adapter. A proposed prototype must include occupied motion, capture, wrong-track
rejection, obstruction/clearance, save/reopen and two-client observations before
its implementation can later be called complete.

### FD1101 Fixed-seat Wing coaster discovery

**Status: planned; implementation paused.** **Proposed release:** 1.1.
**Depends on:** separate expansion approval; existing controlled-car geometry and FD1003 for release.

- **Motion/seating:** develop the requested wing arrangement with fixed seats;
  document seat count, restraint group and train links. Chassis banking applies,
  but there is no 4D carrier rotation or inherited Arrow rotation profile.
- **Track/capture:** target normal Coasters: Simulated track as requested; measure
  the full wing/rider envelope and running gear before allowing it. Specify ordinary
  station stop, restraints and safe exit without a rotary boarding lock.
- **Models and risks:** original chassis, wings, seats, restraints, wheels and
  configurable liveries; determine what existing controlled-car art can be reused
  without retaining functional 4D parts. Wide clearance and coupling are the main
  prototype questions.
- **Done when / next action:** produce a dimensioned nonrotating vehicle contract,
  normal-track fit/rejection matrix and occupied banking/station/coupling prototype
  plan. Reference the user's [Wing proposal](https://coasterpedia.net/wiki/Winged_roller_coaster)
  during approved discovery; no claim of universal normal-track fit is made now.

### FD1301 Arrow-style Suspended coaster discovery

**Status: planned; implementation paused.** **Proposed release:** 1.3.
**Depends on:** separate expansion approval and swing/track prototype; FD1003 for release.

- **Motion/seating:** specify lateral free swing driven by chassis motion and
  declared rider loading, including pivot position, limits, damping and seat layout.
  Keep this state distinct from the existing full-turn 4D carriers.
- **Track/capture:** specify an overhead running-gear contract and the requested
  lift-track swing prevention. Define how locks engage only within safe angular
  speed/alignment limits, release after the lift, and recapture at the station.
- **Models and risks:** original suspended bogie, hanger, cabin/train, wheels,
  restraints, lift locks and matching track. Prototype loading asymmetry, hanger
  clearance, banked curves, coupling and interrupted lock transitions.
- **Done when / next action:** resolve the [Suspended reference](https://coasterpedia.net/wiki/Suspended_Coaster_(Arrow_Dynamics_product))
  into a dimensioned swing/lock state machine and track/seat contract, with explicit
  limit and failure tests. Do not implement an animated cabin in place of dynamics.

### FD1302 S&S-style Axis coaster discovery

**Status: planned; implementation paused.** **Proposed release:** 1.3.
**Depends on:** separate expansion approval and independent rotation proof; FD1003 for release.

- **Motion/seating:** investigate the requested horizontal-rotation concept;
  settle the exact axis relative to chassis, seat grouping, rotation range and
  free-versus-driven modes before treating the proposal as a physical specification.
  Record which behavior is reference-supported and which is a game design choice.
- **Track/capture:** define running gear, functional track eligibility, propulsion
  ownership and any rotation-control interfaces. Specify upright capture, restraint
  release, power loss and rollback semantics independently of FD1301's swing locks.
- **Models and risks:** original rotating assembly, frame, train, seats and track;
  prototype off-axis inertia, multiple-car clearance, continuous rotation, camera
  composition and safe transition between rotation and capture.
- **Done when / next action:** review the [Axis reference](https://coasterpedia.net/wiki/Axis_Coaster_(S%26S_Worldwide_product))
  and produce an unambiguous axis/mode/seat/track contract with a falsifiable physics
  prototype plan. Shared release numbering does not imply shared Suspended mechanics.

### FD1401 Pipeline coaster discovery

**Status: planned; implementation paused.** **Proposed release:** 1.4.
**Depends on:** separate expansion approval and reference-selection decision; FD1003 for release.

- **Motion/seating:** first select the intended design from the user's
  [Pipeline reference](https://coasterpedia.net/wiki/Pipeline_roller_coaster).
  Specify seated/standing posture, restraint motion, axle placement and any seat
  compliance explicitly; the name alone does not settle these choices.
- **Track/capture:** define the pipeline rail arrangement, contact geometry,
  chassis position, functional family and station alignment. Identify lift/brake
  interfaces and whether any moving seat component needs a capture lock.
- **Models and risks:** original track, wheels, vehicle, restraints and train;
  resolve rider/track occlusion, clearance, coupling and posture/camera constraints
  before final asset work.
- **Done when / next action:** deliver the chosen reference/design rationale,
  dimensioned vehicle/track/seat contract and occupied clearance/station prototype
  plan. Other pipeline interpretations become separate suffix packages if requested.

### FD1501 Spinning coaster variants discovery

**Status: planned; implementation paused.** **Proposed release:** 1.5.
**Depends on:** separate expansion approval and per-variant motion proof; FD1003 for release.

- **Motion/seating:** enumerate intended variants from the
  [Spinning reference](https://coasterpedia.net/wiki/Spinning_roller_coaster);
  specify each spin axis, passenger layout, loading response, range, damping and
  free/driven mode. Assign suffix IDs to mechanically different variants.
- **Track/capture:** decide ordinary-track fit versus a dedicated family by measured
  contacts and swept clearance. Define orientation-independent stopping versus
  controlled station capture, restraint interlocks and lift/launch interaction.
- **Models and risks:** original spinning chassis/carrier, seats, bearings, restraints,
  skins and track only where required. Prototype asymmetric loading, uncontrolled
  spin limits, coupling and safe capture through rollback/power loss.
- **Done when / next action:** publish a variant matrix, per-variant physics and
  seating contracts, asset list and independent acceptance plan. A shared cosmetic
  body must not hide different dynamics or expand compatibility without proof.

### FD1502 Roller Ball coaster discovery

**Status: planned; implementation paused.** **Proposed release:** 1.5.
**Depends on:** separate expansion approval and dedicated route/vehicle prototype; FD1003 for release.

- **Motion/seating:** inspect the separate [Roller Ball reference](https://coasterpedia.net/wiki/Roller_Ball_(RES_Rides_product))
  to resolve pivot axis, seat count, free/controlled behavior and loading response.
  Do not inherit FD1501's spin axis or dynamics from the shared release label.
- **Track/capture:** specify the intended route geometry, running-gear contacts,
  functional family, lift/return transitions and station capture/locking. Identify
  whether existing graph and native couplers can represent the route and vehicle.
- **Models and risks:** original frame, rotating passenger unit, seats, restraints,
  wheels, supports and matching track where required. Prototype tight-transition
  clearance, acceleration, retention and restart behavior before detailed skins.
- **Done when / next action:** deliver a dimensioned reference-backed contract and
  a distinct occupied motion/route/capture prototype plan, including wrong-family
  rejection and persistence. Record unresolved mechanics rather than calling it a
  cosmetic spinning-coaster variant.
