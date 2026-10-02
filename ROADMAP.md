# Coasters: Fourth Dimension implementation roadmap to 1.0

This is the addressable execution plan for the design in [PLAN.md](PLAN.md). Foundation packages FD001–FD008 are **done for the bounded prototype**; their implementation, runtime evidence, and limits are recorded in [foundation acceptance](docs/FOUNDATION.md). FD101–FD109 and FD201–FD204 are **done for their bounded builds**; see [0.1 acceptance](docs/PLAYABLE.md) and [0.2 acceptance](docs/MAGNETIC.md). Version 0.3 is done for its bounded five-family gate toward the active 0.9.0 goal; later gates remain planned. Version numbers define scope and order, not dates.

Refer to a package by its permanent ID, for example “implement FD104” or “review FD303.” Keep IDs stable when adding work; split a package using a suffix such as FD104A rather than renumbering existing packages. Record its status, implementation references, and validation evidence under its heading as work proceeds. Statuses are planned, in progress, blocked, and done. A package is done only when its acceptance criteria have evidence, not merely when code exists.

## Release map

**Current stage: completing 0.4.** The five-family bounded 0.3 gate is complete.
Integrations, crafting and tutorials are implemented with partial acceptance;
complete clearance/render coverage, physical recovery and real-client survival
construction/operation remain 0.4 work. Lifecycle investigation has also begun
under FD902 toward 0.9.0; this does not close the earlier 0.4 gate.

**Current work package: FD402 recovery towing.** Dev.26 records actual native
chassis, rider and non-seat carrier-rack poses every ten ticks, replacing the
previous twenty-tick cadence. The five single-car families and two occupied
original train routes pass fresh dense audits with 577–697 samples per route,
at least 0.04602 blocks of body/track clearance, 0.08 blocks of rider/track
clearance and 0.0625 blocks of rider/rack clearance; see the
[dense-clearance checkpoint](docs/evidence/0.4/recovery/dense-clearance/acceptance.md).
This is still sampled default-model/native-Pig evidence: player meshes, linkage,
client rendering and continuous swept clearance remain open. Dev.22 moves Original visual
supports out of the native lower-bearing channel and independently samples that
native lower-bearing model against occupied banked recovery routes; see the
[bearing-clearance checkpoint](docs/evidence/0.4/recovery/bearing-clearance/acceptance.md).
Dev.24 adds sampled mounted native-rider envelopes at those frames; see the
[rider-clearance checkpoint](docs/evidence/0.4/recovery/rider-clearance/acceptance.md).
This bounded Pig-passenger check does not certify player meshes, intentional
seat contact or client rendering. Dev.23 adds sampled full-spin native-wheel envelopes at those banked frames,
excluding only intended running-rail contact; see the
[wheel-clearance checkpoint](docs/evidence/0.4/recovery/wheel-clearance/acceptance.md).
Dev.21 records actual native body poses and block states for
independent sampled default model/track/neighbor clearance; see the
[model clearance checkpoint](docs/evidence/0.4/recovery/model-clearance/acceptance.md).
Dev.20 validates original occupied
two-car Arrow and four-car PAX trains across the complete bend, bank and level
runout before normal braking, power cut, disconnection and unloading. All 24
original native seat mounts, carriers and train links survive. Four-second queued
native assembly supplies the new operator train demos. See the
[banked train checkpoint](docs/evidence/0.4/recovery/banked-trains/acceptance.md).
The next FD402 work is complete rendered vehicle/rider/linkage clearance and
occupied operator/lifecycle acceptance. Dev.19 validates occupied single-car
native towing across all five families through a quarter bend, a 32-block
sustained 30-degree bank, a bank-to-level transition and level runout. Original
seat mounts and nonzero Arrow profiles survive normal braking, power cut,
disconnection and unloading; native body anchor/frame offsets are measured.
See the [banked recovery checkpoint](docs/evidence/0.4/recovery/banked-tow/acceptance.md).
Complete linked-train mesh/rider and rendered clearance remain open. Dev.18 records a failed whole-train
compact PAX spike pull: the powered bare bearing stalls while an unlinked control
ascends. A new production guard brakes a driven stall and retains the original
occupied train; [evidence and limits](docs/evidence/0.4/recovery/chain-stall/acceptance.md)
keep whole-spike towing unverified. Dev.17 connects occupied physical
PAX winch handover to native-chain towing, braking, stationary disconnection and
unloading in one test, preserving the original sixteen seat mounts and four-car
train. See the [checkpoint evidence](docs/evidence/0.4/recovery/occupied-handover/acceptance.md).
FD401 and FD403 also remain in progress. The next release gate is FD404 (0.4);
the subsequent candidate gate is FD905 (0.9).

| Stage | Packages | Status | Required result |
| --- | --- | --- | --- |
| Foundation | FD001–FD008 | Done for bounded prototype | Viable carrier architecture, strict cart/track eligibility, persistence, and rider integration. |
| 0.1 | FD101–FD109 | Done for bounded build | Playable controlled Arrow/S&S and first-generation ZacSpin families, original matching tracks, and ordinary-cart access where physically valid. |
| 0.2 | FD201–FD204 | Done for bounded build | S&S Free Spin vehicle and track with magnetic seat-rotation equipment. |
| 0.3 | FD301–FD305 | Done for bounded five-family gate | Revised ZacSpin variant and functional PAX-style shuttle system. |
| 0.4 | FD401–FD404 | In progress | Optional integration infrastructure, recovery evaluation, crafting, tutorials, and family examples. |
| 0.9 release candidate | FD901–FD905 | In progress; release gate open | Complete compatibility, lifecycle, migration, and performance evidence. |
| 1.0 | FD1001–FD1003 | Planned | Verified five-family release with documentation and reproducible artifacts. |
| External compatibility | FD801 | Planned | Future Track Styles Arrow 4D integration when an inspectable release exists; does not block standalone releases. |

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

Implement approach alignment, angular damping/capture, restraints, ready/locked signals, and supported maintenance retention. Restore normal rider state on recovery/dismount paths. ready/locked signals, retention, etc can be handled by https://www.curseforge.com/minecraft/mc-mods/coasters-engineered, explore using it as dependency instead of reinventing stop/go cycling and launches

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

**Status: in progress.** Exact-version discovery, client-only catalog access, component ownership, and guarded appearance editing/persistence/rendering are implemented. The single-spine Arrow appearance has resource-reload and real provider-removal/reinstallation evidence without changing its controlled profile. Both inspected Arrow styles are restricted to controlled Arrow track. Exact-version Engineered adapters now have native evidence for four-family straight launch/STOP/capture/retention, ordinary-bearing operation, a loaded coupled Arrow full turn/STOP/rollback, and full PAX cycles with opposing motors. A measured upper-stroke tie conflict has guarded selection and Original fallback without profile changes. Packaged dev.4 now has double-spine GUI fallback/cold-reopen/restoration/reload evidence, a flat single-spine quarter curve, and an occupied styled circuit with station dispatch/capture and verified final dismount. An isolated dev.5 native FE batch now covers empty-buffer starvation, supplied launch, sampled braking regeneration, PAX ownership and supplied retention without FE consumption. A native banked circuit now checks joint continuity, loaded full turns, capture and unloading; an independent default-mesh audit covers its decoration/control-rail gap over the full supported stroke. Packaged dev.6 banked GUI/reload/reopen/occupied-circuit/unloading now has recorded evidence. The dev.7 PAX regression uses current readiness after cycle completion and verifies post-capture native-motion rejection/recovery. Dev.8 revises installed decoration placement after a native lower-wheel envelope conflict, retains native running-rail supports and adds an independent continuous-spin/longitudinal-travel wheel/support check on level straight track. The new packaged geometry renders, but opening chat after F3+T exposed a freed bitmap-glyph image crash; its cause remains open. Dev.12 records 64 completed resource/chat/query cycles, then a normal occupied revised double-spine level circuit, recapture, unloading and clean save/exit; this bounded replay does not establish a font fix or complete rendering coverage. Dev.12 fixes startup ordering and empty-schedule completion in the pinned Veil client scheduler, with an unmodified binary control and packaged native-scheduler validation. The original freed-bitmap crash remains unresolved; scheduler completion is not a font fix. Complete banked wheel/vehicle/rider clearance, render-mode coverage and the complete Engineered operator/FE-network workflow remain pending. See [integration scope and evidence](docs/INTEGRATIONS.md).

**Depends on:** FD005, FD305. **Design:** D04, D05, D07, D15.

Implement isolated optional adapters, support/version checks, component render ownership, and appearance fallback. Evaluate available Track Styles styling and operating-addon interfaces against their actual releases and terms.

**Done when:** standalone behavior is unchanged by missing optional mods; supported integrations preserve functional family and profiles. Publish only tested combinations. Future Arrow 4D support remains FD801 and cannot block this package.

### FD402 Evaluate and document recovery towing

**Status: in progress.** Dev.15 implements guarded production selection/coupling/start/brake/disconnect and stationary PAX winch handover. Native tests tow fully occupied cars from all five actual flat examples using Coasters chains/Create motors; forty actual wrong-family initial snaps are rejected. They preserve matching rail support after broken links, handle permanent bearing destruction, persist recovery identity and load schema-5 state. Actual native holding restores all five recovery groups and 32 passengers with unchanged component IDs/couplings; towing stays braked until explicit resume. A separate check hands over a stopped physical PAX carriage while preserving its four-car train. Fully occupied two-car Arrow and four-car PAX trains also tow, brake and disconnect while preserving original links and retention. A packaged empty Arrow check also completes real tool selection/coupling/motor controls, same-ID braked fresh-JVM restoration, explicit start, guarded route-end stop, brake/power stop, disconnect and restraint opening. Both clients exit normally with validation flags false; its saved identities/bindings/profiles persist. The brake action follows the automatic route stop. Dev.16 adds recovery-specific action guidance, live fault status with wrapped hover text, disabled incompatible ride controls and a lever-controlled native Create clutch for demo power. Standalone and combined checks validate native lever start/stop for all five occupied examples with motors retained at eight RPM. The final packaged Arrow example has independent manual braking before the boundary, normal lever power stop, fresh-JVM same-ID coupling restoration, inspection/resume, disconnect and restraint opening; [dev.16 evidence](docs/evidence/0.4/recovery/power-control/acceptance.md) records the surviving startup movement warnings and bounded scope; see [bounded client evidence](docs/evidence/0.4/recovery/gui/arrow-gui-acceptance.md). Dev.17 adds an occupied end-to-end physical handover/recovery test: all sixteen native passengers retain their original seat mounts through towing and braking; chain power is cut with the motor retained at eight RPM, then the original four-car links survive disconnection and normal unloading. See the [occupied handover checkpoint](docs/evidence/0.4/recovery/occupied-handover/acceptance.md) for native metrics and scope. Dev.18 attempts to extend the same fully occupied train onto the compact curved spike. The powered bare bearing stalls before whole-train entry while an unlinked control ascends; this is a measured propulsion limit, not a passing whole-spike case. Production now diagnoses insufficient displacement under active native chain input, brakes all original cars and retains the inspection fault. A separate regression rejects false stalls while power is cut and preserves sixteen original seat mounts through driven-stall braking, power cut, disconnection and unloading; see the [stall checkpoint](docs/evidence/0.4/recovery/chain-stall/acceptance.md). Dev.19 adds a native quarter-bend/30-degree-bank/level-runout recovery example per family, with all 32 original native seat mounts preserved until unloading. Each car traverses all five sections under actual bare-bearing chain power, stays retained while Arrow track keeps its full-turn profile, then manually brakes, cuts power with the motor retained at eight RPM, disconnects and unloads. Both native bodies are measured against track anchor positions and orientations; independent exported-frame checks verify route continuity, sustained banking and the nonzero profile. See the [banked recovery checkpoint](docs/evidence/0.4/recovery/banked-tow/acceptance.md). Dev.20 extends the native bend/bank test to original occupied two-car Arrow and four-car PAX trains. Every car crosses all five route sections and samples the sustained bank before the whole train reaches the level runout for normal braking, power cut, stationary recovery disconnection and native unloading. Original carriers, all 24 original native seat mounts and original train links are preserved. Both extended routes have independent exported-frame checks, and the test exercises the four-second queued native assembly used by the new operator demos; see the [banked train checkpoint](docs/evidence/0.4/recovery/banked-trains/acceptance.md). Dev.21 adds read-only native pose/block-state traces and independent sampled default-model clearance against original track triangles and neighboring cars. It covers all five single-car examples and the two original train examples while excluding special wheel/bottom rendering, player meshes, linkage, decorations and continuous sweep; see [model clearance evidence](docs/evidence/0.4/recovery/model-clearance/acceptance.md). Schema/protocol 6 protects the new state; older schemas 1–5 remain readable. Complete rendered operator acceptance and mesh/rider/rack clearance, broader strength/propulsion and recovery lifecycle evidence remain open. See [operator workflow](docs/RECOVERY.md) and [operator acceptance evidence](docs/evidence/0.4/recovery/operator-acceptance.md).

The preceding mechanical checkpoint proved fifteen slow, flat native-coupler pulls across all five families: empty captured, fully occupied captured and fully occupied maintenance-retained, at family-specific 6/12/14-block spacing. It used an artificial actuator on the ordinary bearing and a temporary admission policy; both were removed before production recovery was implemented. It establishes feasibility, while the dev.15 operator tests use actual native chains and production guards. A policy rejection is not a demonstrated physical coupling limitation. See [mechanical evidence and reproduction](docs/evidence/0.4/recovery/mechanical-acceptance.md).

**Depends on:** FD108, FD106, FD305. **Design:** D03.

Test couplers, strength, offsets, braking, retained carriers, and loaded towing with fitting ordinary carts. Validate every vehicle separately and keep disabled 4D units on matching track.

**Done when:** publish a proven recovery example if feasible, or document the demonstrated coupling limitation and supported rail-access-only workflow. Do not claim towing support merely because wheels fit. A purpose-built recovery vehicle is a separate scope decision if existing couplers cannot work.

### FD403 Complete crafting and teaching content

**Status: in progress.** Dev.9 adds localized guidance for all fifteen craftable items, vanilla recipe discovery and a five-family building/operating guide. A native check exercises all fifteen crafting outputs, missing-ingredient rejects and inventory-triggered recipe rewards. Dev.10 adds nine original Ponder scenes with 53 registrations across all fifteen items, client-only registration, generated translations/schematics and native resource checks. Packaged standalone playback/replay and a completed resource reload cover every registration; the rendered review uses normal inventory shortcuts. Dev.11 adds survival crafting through the native result slot and world right-click dispatch for all five track/car families, twenty wrong-family rejects, magnetic equipment and invalid PAX setups. It fixes ordinary stations accepting PAX and clears old invalid bindings on use; standalone and combined native checks pass. Complete survival building/operating exercises through a real client and the full teaching gate remain pending. See [survival checkpoint](docs/evidence/0.4/survival-acceptance.md), [building guide](docs/BUILDING.md), [teaching checkpoint](docs/evidence/0.4/teaching-acceptance.md) and [Ponder checkpoint](docs/evidence/0.4/ponder-acceptance.md).

**Depends on:** FD305. **Design:** D08.

Complete recipes, item descriptions, Ponder scenes, family demonstrations, diagnostic messages, and consistent terminology. Teach family matching, ordinary-cart exceptions, controlled versus free motion, and shuttle boarding.

**Done when:** each family has a usable acquisition/build path and tutorial; shipped sample layouts exercise the advertised mechanics without optional mods unless clearly marked.

### FD404 Version 0.4 gate

**Status: planned; awaiting FD401–FD403 acceptance.**

**Depends on:** FD401–FD403.

Produce the feature-complete candidate scope and list tested optional combinations, known limits, and deferred external support.

**Done when:** the five-family standalone feature set and teaching content are complete, all advertised integrations have evidence, and remaining work is stabilization rather than missing core mechanics.

## Version 0.9 release candidate

### FD901 Run the full compatibility matrix

**Status: in progress, bounded pre-FD404 checkpoint.** Dev.27 verifies bare
ordinary-bearing fit and deliberately widened-body rejection across all five
original tracks through both direct eligibility and the native snap filter. The
same run confirms ordinary traversal has no vehicle record, carrier rotation or
magnetic impulse, and rejects unassigned ordinary/4D train coupling without
mutating the 4D car; see the [ordinary snap matrix](docs/evidence/0.9/ordinary-cart-snap-matrix.md).
The current branch also passes all 39 native GameTests under Track Styles-only,
Engineered-only, and combined audited profiles; see the [optional-profile
matrix](docs/evidence/0.9/optional-profile-matrix.md). This advances the
regular-cart and audited optional-profile portions before FD404, but it does not
close the full compatibility matrix.

**Depends on:** FD404. **Design:** D02, D04, D17.

Exercise every family against matching, ordinary, wrong-family, cosmetic-only, and supported external tracks; test regular-cart fit/non-fit, mixed consists, and every entry path. Test optional addons individually and in the supported combined pack.

**Done when:** each advertised combination has recorded results. Wrong-family 4D carts are rejected everywhere, and ordinary carts receive no 4D benefit.

### FD902 Validate multiplayer and lifecycle recovery

**Status: in progress.** The current packaged dedicated-server checkpoint covers
occupied client disconnect/fresh-client restoration, normal no-player
chunk/sublevel unload/reload, and full dedicated-server cold restart for each
stationary original Arrow, Legacy, Free Spin, Revised and PAX family. Every
case retains the one rider assignment, restores the original native carrier and
returns the player to a `create:seat` before the 200-tick deadline. The
combined audited integration profile now passes 40 GameTests, including a
server-side two-live-player regression that retains distinct seat entities and
UUID-to-seat assignments across a native update. Fresh-world real two-client
fixtures retain both riders in distinct seats of one carrier across all five
current families; Arrow and Legacy also restore one rider after a fresh-client
reconnect while the other remains mounted. The earlier second-rider removal was
a stale Sable tracking point in a removed sublevel. Native support-loss,
late-join, shuttle availability-gap behavior and occupied Revised moving-route
restart also have bounded evidence. Concurrent interruption coverage for Free
Spin, Revised and PAX remains open. See
[occupied disconnect/reconnect evidence](docs/evidence/0.9/occupied-disconnect-reconnect.md),
[support-loss evidence](docs/evidence/0.9/five-family-support-loss.md),
[two-client late-join evidence](docs/evidence/0.9/two-client-snapshot.md),
[caught shuttle acceptance and limits](docs/evidence/0.9/caught-shuttle-acceptance.md)
and [occupied boarding acceptance](docs/evidence/0.9/occupied-shuttle-acceptance.md).

**Latest FD902 checkpoint: two live riders across all five families.** Clean
dedicated-server worlds admitted Dev and Observer to distinct native seats in
the same carrier for Arrow, Legacy, Free Spin, Revised, and the four-chassis
PAX shuttle. Each retained two assignments and two live `create:seat` mounts
for fifteen seconds. Arrow and Legacy additionally retained Observer's
assignment during disconnect and restored a fresh Observer client to its
original seat before the deadline while Dev stayed mounted. This closes the
prior stale-tracking-point failure and the bounded initial concurrency matrix.
The existing five-family stationary normal unload/cold-restart and current
Revised moving-route checkpoints remain valid. Concurrent interruption
coverage for Free Spin, Revised and PAX remains open; see [occupied disconnect/reconnect evidence](docs/evidence/0.9/occupied-disconnect-reconnect.md).

**Depends on:** FD404. **Design:** D14, D16, D17.

Run two-client and late-join scenarios, disconnects, chunk unloads, server restart, support loss, cart disassembly, and repeated shuttle station passes across all families.

**Done when:** no passenger duplication, stale restraints, lost carrier state, or incorrect track rebinding remains in supported cases. Document supported recovery actions for interruptions.

### FD903 Validate save migration and provider changes

**Status: in progress.** Dev.27 expands the recovery loader regression from schema 5 to every supported legacy schema (1–5). It serializes the occupied five-family recovery fixture, removes the schema-6 recovery fields, loads each declared legacy schema and requires every chassis identity to survive with no invented recovery partner or removed-partner state. The existing invalid 4D-to-4D saved recovery reference remains rejected. A separate regression consumes the recorded 0.3 schema-3 world extract, writes schema 6, reopens it, and preserves all 24 original rail IDs, three original chassis IDs and their functional families. The existing real appearance lifecycle opens a saved world after Track Styles removal and again after reinstallation; the current schema-6 regression additionally preserves both known Track Styles appearance IDs and an unknown provider while the provider is absent. Cosmetic appearance loss therefore cannot change a functional rail's ID, family, variant or profile. The [world upgrade and recovery procedure](docs/MIGRATION.md) gives backup, staged-upgrade and restore steps, while explicitly declining third-party functional-block conversion. This is a loader-contract checkpoint and operational guidance, not packaged historic-world migration or provider conversion; see the [recovery migration checkpoint](docs/evidence/0.9/recovery-schema-migration.md) and [appearance-provider boundary](docs/evidence/0.9/appearance-provider-lifecycle.md).

**Depends on:** FD404. **Design:** D04, D09, D10, D16.

Test upgrades from released save formats, stable IDs, profile rebinding, appearance-provider removal, and explicit conversions involving external blocks.

**Done when:** supported upgrades preserve functional families and profiles; unsupported conversions are rejected clearly. Backups and recovery instructions accompany any destructive migration.

### FD904 Meet measured performance and rendering targets

**Status: in progress, target-host runtime gate available.** Dev.27
adds the resettable `/fd performance` runtime snapshot, which reports this
addon's server-side carrier-runtime invocation totals alongside live-carrier
update totals, per-carrier timing and constraint rebuild counts. Its scope and
repeatable sampling procedure are documented in [performance
sampling](docs/PERFORMANCE.md). Automated sampling windows cover
controlled-train, free-spin and shuttle scenes, and two [integrated host
baselines](docs/evidence/0.9/runtime-baseline.md) are recorded. The current
checkpoint adds a third combined run and host-scoped limits of 120,000 ns per invocation and
6,000 ns per live carrier update; `-PfdPerformanceTargets` verifies them in
all three sampled scenes. Packaged real-client runs validate the opt-in
per-stage original-track instrumentation under llvmpipe and on the stated AMD
RX 7900 XTX host; [the hardware observation](docs/evidence/0.9/client-render-sampling.md)
measures 3.16 ms average original-stage work and separately observes 21,476
Track Styles decoration pieces without making an FPS claim. Shader measurements
and full acceptance remain pending.

**Depends on:** FD404, FD006. **Design:** D17.

Benchmark the declared controlled-train, free-spin, and shuttle scenes against recorded baselines. Profile per-carrier/constraint costs, caches, Flywheel modes, and the supported shader stack.

**Done when:** quantitative targets chosen from the baseline are documented and met on stated hardware; required render modes work; no unresolved performance regression prevents the advertised scene sizes. Record limitations rather than inventing universal FPS guarantees.

### FD905 Release candidate gate

**Status: planned; awaiting FD901–FD904 and all earlier release gates.**

**Depends on:** FD901–FD904.

Publish a candidate artifact for testing with a defect list, exact versions, reproducible scenarios, and explicit supported scope.

**Done when:** all core acceptance criteria pass and release-blocking crashes, save corruption, rider-state failures, and eligibility bypasses are resolved. Retest affected cases after fixes; regressions reopen the relevant package.

## Version 1.0

### FD1001 Finalize the release documentation

**Depends on:** FD905.

Finalize installation instructions, version/dependency matrix, five-family guides, recovery limits, attribution, integration terms, known issues, and changelog. Label inspired approximations honestly.

**Done when:** the documentation matches the release artifact and does not advertise unverified towing, historical fidelity, or future Track Styles support.

### FD1002 Reproduce and verify release artifacts

**Depends on:** FD905, FD1001.

Build from the intended release revision, record checksums, verify packaged metadata/assets, and smoke-test clean client and dedicated-server installs using the documented dependencies.

**Done when:** the artifact is reproducible, contains only intended distributable content, and clean installs complete one full ride for every supported family with correct track restrictions.

### FD1003 Version 1.0 release gate

**Depends on:** FD1001, FD1002 and all required earlier gates.

Review the finished release against the definition below. Publishing is a future execution action, not authorized or performed by this planning document.

**Done when:** the release checklist and evidence are complete; when publication is separately requested, the published artifacts and release notes match the verified build.

Version 1.0 requires all five functional vehicle families with original matching tracks, both controlled and free dynamics, magnetic assistance, shuttle operation, strict asymmetric cart compatibility, correct riders/camera, persistent multiplayer state, usable tutorials/recipes, tested migrations, and measured performance. Ordinary-cart rail access is verified; towing is claimed only where proven. Standalone completion does not require unreleased external features.

## External compatibility work

### FD801 Integrate a released Track Styles Arrow 4D

**Depends on:** FD401 and an inspectable external release with applicable terms established. **Design:** D02, D04, D05.

Inspect the actual representation and implement an independently authored optional adapter. A cosmetic-only style must decorate matching functional track or undergo an explicit functional conversion; it cannot make ordinary track eligible by appearance alone.

**Done when:** our Arrow vehicles correctly use the verified equivalent functional track, ordinary fitting carts receive no 4D effects, control rails have one render owner, profiles persist, and same-family provider transitions pass multiplayer/lifecycle tests. Record supported versions. If the external release is unavailable, keep this package pending without blocking 1.0.
