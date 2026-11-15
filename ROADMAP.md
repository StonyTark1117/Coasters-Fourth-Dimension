# Coasters: Fourth Dimension — expanded 1.0 and post-1.0 roadmap

This is the addressable execution plan for the design in [PLAN.md](PLAN.md). Foundation packages FD001–FD008 are **done for the bounded prototype**; their implementation, runtime evidence, and limits are recorded in [foundation acceptance](docs/FOUNDATION.md). FD101–FD109 and FD201–FD204 are **done for their bounded builds**; see [0.1 acceptance](docs/PLAYABLE.md) and [0.2 acceptance](docs/MAGNETIC.md). Version 0.3 is done for its bounded five-family gate toward the expanded stable 1.0 target. Version numbers define scope and order, not dates.

Refer to a package by its permanent ID, for example “implement FD104” or “review FD303.” Keep IDs stable when adding work; split a package using a suffix such as FD104A rather than renumbering existing packages. Record its status, implementation references, and validation evidence under its heading as work proceeds. Statuses are planned, in progress, blocked, and done. A package is done only when its acceptance criteria have evidence, not merely when code exists.

## Release map

The expanded release sequence was adopted **2026-11-11 (America/Phoenix)**.
All vehicle and integration scope formerly proposed through 1.5 now belongs to
stable 1.0. Development milestones follow shared mechanics; the first stable
release follows feature completion, integrated stabilization and qualification.
The clarified boundary is **vehicle/cart and seat-motion styles before 1.0;
moving-track mechanisms after 1.0**. Fixed Inverted, Dive, both Flying variants
and Powered Inverted Yaw are included in the 0.6–0.8 delivery and 1.0 gates.
Their former proposed 1.1–1.4 vehicle allocations are superseded; FD IDs remain
unchanged. Standalone builder tools retain 1.1 and multi-train operations retain
1.2; only their control interfaces actually needed by vehicles move earlier.
FD2001–FD2007 moving-track work never feeds FD1000, FD905 or FD1003.
Spinning's 1.0 list remains S1 conventional free-spinning and S2 launched/inverting
free-spinning with magnetic influence.
The [integration allocation](docs/INTEGRATION-BACKLOG.md#release-allocation)
uses the same sequence. This adopts the work plan; it does not certify new
capabilities, activate dependencies, change artifact versions or authorize publication.

**Current stage: 0.4 complete; no release candidate is accepted.** Foundation
through the bounded five-family 0.3 gate, FD401A–D, FD402A–F, FD403A–F and FD404
retain their accepted status. FD402 is complete for its declared recovery scope.
Early design research
and asset concepts may proceed independently; implementation follows the explicit
package prerequisites below. The old blanket expansion pause is superseded by
this adopted sequence. FD801 remains an independent upstream-dependent package.

**Artifact being qualified:** use the source/JAR and profile identities in the
[current qualification record](docs/CURRENT-QUALIFICATION.md#artifact-and-scope).
That record is authoritative; do not maintain a second baseline pointer here.
Artifact identity records provenance. A new hash does not reset accepted behavior.

**Testing and stop rule:** [VALIDATION-POLICY.md](docs/VALIDATION-POLICY.md)
governs every matrix below. Start with the [current remaining-case queue](docs/CURRENT-QUALIFICATION.md#current-work-queue),
not the historical evidence tables. Reuse accepted cases after a brief relevant-change
review; test only unresolved or affected behavior, once by default. Matrices
specify coverage, not every Cartesian combination of their dimensions. Older
per-run “remaining/open” prose is observation history, not a fresh work queue.
No policy-only change in this audit requires a Minecraft run.

**A new build never resets the roadmap.** Closed cases stay closed unless a
specific changed behavior or new defect invalidates their acceptance. Reopen
only that case; its siblings and parent gates retain their accepted results.
After its targeted check passes, close it again and continue. Combine compatible
records to finish whole tasks; “bounded slice” is an evidence description, not
a requirement to leave a fully covered task open or schedule another run.

**Execution order:**

1. Finish only the open 0.4 recovery and player-workflow cases; close FD404 as the five-family development checkpoint.
2. Resolve all new-family specifications and expansion dependencies during 0.5. Prove the necessary contract extensions through a complete fixed-seat Wing, establish seat/camera ownership and deliver the comfort option.
3. Build S1 conventional free-spinning, then Suspended and Fixed Inverted (0.6). In 0.7 implement Axis and Roller Ball, and Conventional Flying followed by New-generation Flying with scenery-facing yaw. The two 0.7 chains follow their actual prerequisites and share runtime ownership.
4. Complete Pipeline, S2 launched/inverting free-spinning, Dive, Powered Inverted Yaw and operations integrations (0.8). Establish the required hold/dispatch authority before Dive/powered travel; consume the Flying yaw handoff for Powered Inverted. Pipeline and adapters do not wait for unrelated families.
5. Complete presentation, optional seats, coexistence and the model-and-texture review (0.9) against stable representative vehicle interfaces. Review all original and added families; recipes, guides, examples and asset improvements accompany each feature.
6. Close FD1000 at 1.0 alpha to freeze the expanded feature scope. During beta/RC reconcile accumulated acceptance and finish FD901–FD905; FD1001–FD1003 own documentation, packaging and stable release.
7. Deliver the standalone preset library, colors/liveries and unified operator panel in 1.1, then ride-local multi-train operations in 1.2. These supporting tools do not postpone the vehicle families or add a blanket prerequisite to them.
8. Develop turntables, drop tables, tilt tracks and the finite additional moving-track styles through the separate post-1.0 program. Prove moving route connections and occupied retention first; assign release numbers after feasibility. These mechanisms never block 1.0.

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
“Observed” means a bounded historical result exists. A current row may close
with direct acceptance or scoped carry-forward review under the validation policy;
whole-JAR equality is not required. Keep the original run identity and limits.

| Stage | Packages | Status | Required result |
| --- | --- | --- | --- |
| Foundation | FD001–FD008 | Done for bounded prototype | Viable carrier architecture, strict cart/track eligibility, persistence, and rider integration. |
| 0.1 | FD101–FD109 | Done for bounded build | Playable controlled Arrow/S&S and first-generation ZacSpin families, original matching tracks, and ordinary-cart access where physically valid. |
| 0.2 | FD201–FD204 | Done for bounded build | S&S Free Spin vehicle and track with magnetic seat-rotation equipment. |
| 0.3 | FD301–FD305 | Done for bounded five-family gate | Revised ZacSpin variant and functional PAX-style shuttle system. |
| 0.4 | FD401–FD404 | Complete 2026-11-15 | Five-family core, recovery and player workflows reconciled at the FD404 development checkpoint; expanded 1.0 qualification remains FD1000. |
| 0.5 | FD501–FD503; six original discovery IDs; FD1601S, FD1702S, FD1801S, FD1802S, FD1901S; FD1101A–C; FD904C1 | Complete | Specify every 1.0 vehicle, prove Wing-backed extension contracts, establish comfort and reusable tools. Adapter-specific upgrades follow their consumers. |
| 0.6 | FD1501A–C, FD1301A–C, FD1601A–C, FD601 | Planned | S1 conventional free-spinning, Suspended and Fixed Inverted: axes, loading, capture, swing locks and fixed overhead seating. |
| 0.7 | FD1302A–C, FD1502A–C, FD1801A–C, FD1802A–C, FD1803, FD701 | Planned | Axis/Roller Ball and both Flying variants; New-generation Flying proves scenery-facing yaw in seated and prone postures. |
| 0.8 | FD1401A–C, FD1501D–F, FD1701A, FD1702A–C, FD1901A–C, FD1902, FD810–FD813 | Planned | Pipeline, S2, Dive, Powered Inverted Yaw, required operating authority and agreed Additions/CTNB/Magnetized capabilities. |
| 0.9 | FD910–FD916 | Planned | Skin, rotating-seat integration, Immersion, Waterparked/Create Coasters coexistence, and model/texture quality across original and added families. |
| 1.0 alpha | FD1000 | Planned | All adopted vehicle families, variants and 1.0 integrations complete; freeze feature scope and supported profiles. |
| 1.0 beta / RC | FD901–FD905 | Evidence in progress; final gates open | Integrated stabilization, compatibility, lifecycle, migration, rendering and performance; carry accepted cases forward. |
| 1.0 stable | FD1001–FD1003 | Planned | Final documentation, reproducible packaging and separately authorized publication. |
| 1.1 | FD1602–FD1605 | Planned | Shareable setting presets, colors/liveries and a unified operator panel; Fixed Inverted is already a 1.0 requirement. |
| 1.2 | FD1701B–C, FD1703 | Planned | Bounded ride-local multi-train operation using the earlier FD1701A authority; Dive is already a 1.0 requirement. |
| Post-1.0 moving-track program; release numbers after feasibility | FD2001–FD2007 | Planned | Transfer platforms, turntables, drop tables, tilt tracks, elevator, teeter-totter, side-drop and compound motion-table styles. |
| Independent compatibility | FD801 | Planned | Inspectable functional Track Styles Arrow 4D adapter; upstream availability never blocks standalone release. |

Milestone labels are delivery targets, not dates, JAR version changes or universal
start barriers. A milestone gate aggregates its named deliverables; individual
packages can start once their own prerequisites are met. Required 0.4–0.9
gates feed FD1000 and the 1.0 release, but unrelated unfinished work need not idle
an agent. Permanent FD numbers do not change when their release allocation moves.
Post-1.0 gates consume only their declared packages. Publication of an earlier
version is not a prerequisite for later design or implementation; integrated
contracts and their accepted handoff are. Keep the 1.0 qualification baseline
separate from later feature work and obey the one-owner shared-runtime rule.

## Agent execution and acceptance

Use the [milestone execution guides](docs/roadmap/README.md) for ordered package
steps and handoffs. This roadmap remains the scope/status/acceptance authority;
the current qualification queue remains the sole active assignment tracker.
Guide steps add no acceptance gates or automatic test runs.

- Assign one owner at a time to shared runtime, persistence, eligibility or seat/camera contract changes. Dependent work uses the prerequisite after integration into `main`, not another agent's unfinished checkout. Follow [AGENTS.md](AGENTS.md) for any genuinely parallel worktree.
- Assign complete outcomes with named prerequisites, implementation steps, pass conditions, reusable evidence and actual remaining gaps. Combine compatible observations to close the outcome; a bounded record does not impose a permanent “slice” ceiling.
- Claim the existing case in the current work queue before scheduling its run; confirm no other owner is already covering it. Close covered cases with their evidence, update the owning package once, and remove them from the active queue. Do not duplicate historical acceptance into a new tracker.
- Read-only discovery and asset concepts can proceed while 0.4 finishes. Final geometry/assets follow the relevant dimensioned specification and motion proof. Operations adapters can start after their control/provider prerequisites; Pipeline has no Axis dependency.
- Define fixed seats, carrier axis/pivot/motion, restraints, camera and propulsion ownership through concrete families. Do not build a speculative framework or reactivate archived schema/provider code wholesale.
- Reuse worlds, prototypes and measurements. Schedule graphical validation as shared host work, avoiding competing launches by default; a two-client case is one coordinated job within current Java/display/memory rules.
- Each new feature supplies its recipes, guide and example during implementation. Native tests cover dynamics, eligibility and failure injection; connected clients cover real input, rendering and rider synchronization. Add only distinct lifecycle cases.
- A mechanics package that supplies shared contracts includes its representative occupied client proof before dependent mechanics start. That proof counts toward later player acceptance; final decorative art need not block the handoff. Follow the [family handoff rule](#shared-contract-handoff).
- To hold a gate open, name its unmet required criterion and affected case. Apply the [blocker classification](docs/VALIDATION-POLICY.md#gate-blockers-and-follow-up-work); optional polish and diagnostics go to follow-up work without expanding the gate.
- [VALIDATION-POLICY.md](docs/VALIDATION-POLICY.md) governs all stages. A targeted passing fix closes its case without resetting siblings or earlier gates. Full release qualification belongs after FD1000; foundational fixes and useful tests may happen earlier.

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

**Status: complete for the declared scope.** FD401A–D are closed under the current
validation policy; infrastructure and operator/render acceptance are recorded in
the [artifact record](docs/CURRENT-QUALIFICATION.md),
not the old `2c5d9a7` label. The detailed [handoff](docs/FD401-RELEASE-HANDOFF.md)
and [historical milestones](docs/ROADMAP-CHECKPOINT-HISTORY-2026-11-04.md#fd401-deliver-optional-provider-infrastructure)
retain their original evidence boundaries.

**Depends on:** FD005, FD305. **Design:** D04, D05, D07, D15.

Current scope is Create guidance/diagnostics, Track Styles 1.2.0 Arrow appearance
and Engineered 1.1.1 operations, on schema 6/protocol 7. Missing or unsupported
optional mods must leave standalone behavior intact. New adapters belong to
FD810–FD915; necessary schema/provider changes belong to FD501/FD501A.
Neither reopens FD401. FD801 is independent.

**Current next actions (take precedence over per-run notes below):**

| Task | Stop / remaining action |
| --- | --- |
| FD401A–B | Complete. Do not repeat profile identity launches, Styles reload repetitions, shader runs, or visible inversion unless their relevant paths change or regress. |
| FD401C | **Complete on the selected `ec2fcda` JAR under the cumulative validation policy.** Consolidated evidence covers normal construction and operator cycles, coupled Arrow continuity, Engineered-only and combined five-family FE guards, PAX opposing-motor authority, FE interruption/restoration, occupied recovery, station capture and occupied Arrow cold reopen. Combined-profile client cycles cover all four driven families; Engineered-only client operation is represented by Arrow and PAX, while five-family FE guards run in both profiles. The policy requires this coverage of shared and family-specific paths, not every family/profile client combination; no family-specific divergence was found. The selected-JAR recovery run completed the post-restoration circuit and was captured when the station hold signal was restored; it reached 19.4069/20, so `PassShort=1` is an observed speed shortfall, not a missed circuit or failed capture. The pinned provider's `SHORTFALL_TOLERANCE=0.5`; the shortfall was 0.5931, and exact 20/20 is not a FD401C acceptance requirement. The Engineered-only cold-reopen record confirms rider/restraint persistence and no automatic launch. Non-PAX production entries are unchanged between the clean baselines; carry their accepted records forward. PAX authority and power-fault recovery have bounded passes; FD902C owns distinct phase interruptions. |
| FD401D | **Complete on the selected `ec2fcda` JAR within representative validation scope.** All four profiles have five-family server formatter/update-tag evidence. PAX client records exercise all six source choices and phase output; Arrow client records cover trailing-car occupancy and the loaded → unavailable → re-entry → unavailable Ride Fault lifecycle with sign, goggles and received snapshots. Source review confirms the four single-car families share `BoardingStationEntity` and common `RideTelemetry`/`RideDisplaySource`/`RideInformation` formatters; PAX uses the distinct `ShuttleWinchEntity` but the same registered sources and goggles formatter. Client evidence covers both controller implementations, while server matrix covers every family/profile. No family-specific client divergence was found, so the validation policy does not require a family × source × profile replay. |

The following evidence history preserves each run's limits. A statement that a
particular run did not cover construction, another profile, or another family
does not reopen a case covered elsewhere. Consolidate coverage before launching.

| Task / status | Coverage (apply validation policy) | Pass condition | Evidence history (current next actions above take precedence) |
| --- | --- | --- | --- |
| **FD401A Artifact identity — complete on `ec2fcda`** | One immutable JAR, committed source, intended base/Styles/Engineered/combined profiles | Source/JAR/dependency identities and every reused claim have a bounded mapping; runtime profile hashes attach when launched | The [clean source/JAR manifest](docs/evidence/qualification/2026-11-05-ec2fcda/README.md) selects `656725bc…` and records the changed PAX classes. The [current record](docs/CURRENT-QUALIFICATION.md#artifact-and-scope) maps exact-JAR launches and dependency hashes for [base](docs/evidence/0.9/fd401-a-base-profile-ec2fcda-20261105/README.md), [Styles-only](docs/evidence/0.9/fd401-styles-tracing-disabled-reload-ride-ec2fcda-20261105/README.md), [Engineered-only](docs/evidence/0.9/fd401-a-engineered-profile-ec2fcda-20261105/README.md), and [combined](docs/evidence/0.9/fd401-current-clean-combined-client-reopen-20261105/README.md) profiles. This closes identity only; B–D own the remaining in-world acceptance. The earlier `8f370d0` remains historical evidence. |
| **FD401B Styles — complete on selected `ec2fcda` JAR** | Both Arrow styles; single and coupled banked/full-turn rides; whole-section Original/single/double transitions; FANCY, FAST, FABULOUS and pinned Iris/Solas | Normal UI preserves family/profile; one render owner per component; fallback, provider removal/reinstall, reopen and reload preserve mechanics; occupied capture/unload and bounded geometry pass | Exact selected-JAR evidence: [normal UI transitions](docs/evidence/0.9/fd401b-style-ui-transition-selected-20261105/README.md), [provider lifecycle](docs/evidence/0.9/fd401-styles-provider-lifecycle-selected-20261105/README.md), [absent-provider visual fallback](docs/evidence/0.9/fd401-styles-fallback-visual-selected-20261105/README.md) and [occupied ride](docs/evidence/0.9/fd401-styles-absent-occupied-selected-20261105/README.md), [normal-boarded coupled client cycles](docs/evidence/0.9/fd401b-coupled-styles-selected-20261105/README.md), [named full-turn test](docs/evidence/0.9/fd401b-coupled-named-selected-20261105/README.md), [visible coupled inversion](docs/evidence/0.9/fd401b-visible-coupled-selected-20261105/README.md), [render ownership parity](docs/evidence/0.9/fd401b-ownership-parity-selected-20261105/README.md), and [fresh default-model geometry sweep](docs/evidence/0.9/fd401b-swept-selected-20261105/README.md). The [current qualification index](docs/CURRENT-QUALIFICATION.md) links the three completed reload/occupied-ride repetitions per graphics mode and pinned Iris/Solas observation. The visual full-turn fixture used pig riders and assigned styles directly; separate normal UI and boarding records cover operator paths. Arbitrary cosmetics/models and pixel-by-pixel ownership are outside these bounded claims. |
| **FD401C Engineered — complete on selected `ec2fcda` under cumulative validation policy** | Engineered-only and combined profiles: all five families; single/coupled Arrow; loaded PAX opposing-motor authority; wired FE | Normal construction/controller operation, launch/STOP/capture/unload, starvation, power restore and cold reopen preserve riders and interlocks; all PAX chassis reject competing drive | Current-JAR [Arrow/FE/reopen](docs/evidence/0.9/fd401-engineered-current-arrow-fe-reopen/README.md), [wired FE interruption/restore](docs/evidence/0.9/fd401-engineered-current-fe-interruption/README.md), [Legacy](docs/evidence/0.9/fd401-engineered-current-legacy-fe/README.md), [Free Spin](docs/evidence/0.9/fd401-engineered-current-free-spin-fe/README.md), [Revised](docs/evidence/0.9/fd401-engineered-current-revised-fe/README.md), [coupled Arrow](docs/evidence/0.9/fd401-engineered-current-coupled-arrow-fe/README.md), selected-JAR [PAX opposing-motor authority](docs/evidence/0.9/fd401-engineered-current-pax-authority/README.md), and fresh selected-JAR [Arrow construction](docs/evidence/0.9/fd401-engineered-current-fresh-arrow/README.md) are bounded Engineered-only results. The selected-JAR [Engineered-only normal-seat Arrow cycle](docs/evidence/0.9/fd401c-selected-engineered-only-arrow-cycle-20261105/README.md) now adds current-artifact boarding, required-energy controller operation, STOP, capture and UI unload; the inherited route reached 19.3966/20 with `PassShort=1`, and its post-pass FE draw was not sampled. The selected-JAR [combined-profile Arrow operator cycle](docs/evidence/0.9/fd401-engineered-current-combined-arrow/README.md) proves one occupied Arrow cycle, not the family/profile matrix. The selected-JAR [combined five-family motor fixture](docs/evidence/0.9/fd401-engineered-current-combined-five-family/README.md) proves bounded occupied launch/STOP/capture/retention for four driven families and PAX motor rejection with Styles installed; it uses direct fixtures, not normal UI operation. A selected-JAR [combined PAX authority cycle](docs/evidence/0.9/fd401-engineered-clean-combined-pax-authority/README.md) adds normal empty-hand boarding, occupied UI dispatch, four-car LIFTING/RETURNING, capture and unload against an opposing powered controller; the route was saved, not freshly built, and OUTBOUND was not sampled. A fresh selected-JAR [PAX normal-construction and occupied-cycle pass](docs/evidence/0.9/fd401-pax-normal-construction-656725bc-20261105/README.md) now adds player-placed track, four cars, three chain links and a bound winch, normal boarding/restraint/dispatch, one occupied LIFTING/OUTBOUND cycle, return to BOARDING, normal dismount, and a clean save/exit. The route supports and 16 RPM Create creative motor were command-fixtured and are disclosed; this closes only the fresh PAX construction/cycle slice, not the Engineered-only/combined family and power-recovery matrix. A separate selected-JAR [PAX fault-resume circuit and unload](docs/evidence/0.9/fd401-pax-fault-resume-unload-selected-656725bc-20261105/README.md) resumes the saved occupied FAULT/LIFTING train by ordinary empty-hand winch use after restoring 16 RPM, records LIFTING → OUTBOUND → RETURNING → CAPTURING → BOARDING, and confirms normal restraint opening and rider dismount. The 3 GiB-per-client [repeat](docs/evidence/0.9/fd401-pax-fault-resume-unload-3g-selected-20261105/README.md) now confirms the same circuit, capture and unload under the current memory caps; both records use an inherited route and close only this bounded recovery slice. The [combined four-family construction](docs/evidence/0.9/fd401-engineered-current-combined-construction/README.md) adds normal item motor/car/controller placement and controller programming for Arrow, Legacy, Free Spin and Revised on the earlier `8f370d0` JAR. The committed PAX bind restore changes the current baseline; the source-level cold-reload station hold test passes on the refreshed production classes ([record](docs/evidence/0.9/fd401-pax-boarding-hold-source-20261105/README.md)), though the containing 69-test run had 27 unrelated required failures. A [clean-JAR combined Legacy seat attempt](docs/evidence/0.9/fd401-engineered-clean-combined-legacy-seat-blocker/README.md) targeted the physical seat but did not mount; it is an acceptance failure, not an operator pass. A [single diagnostic input run](docs/evidence/0.9/fd401-legacy-seat-input-diagnostic-20261105/README.md) targeted the native seat but emitted no seat-method log. The later [clean-JAR combined Legacy cycle](docs/evidence/0.9/fd401-engineered-clean-combined-legacy-cycle/README.md) reaches STOP, draws FE, captures, opens restraints in the UI and unloads; helper boarding and a saved route are disclosed. A continuous selected-JAR normal-board-to-cycle Legacy pass joins boarding, STOP, capture and unload on the saved combined-profile Legacy route. A [continuous normal-board Free Spin cycle](docs/evidence/0.9/fd401-free-spin-clean-continuous-normal-cycle-20261105/README.md) now adds the same normal seat-to-powered-STOP-to-capture-and-unload path on the saved combined-profile Free Spin route, with FE draw and no starvation. A [continuous normal-board Revised cycle](docs/evidence/0.9/fd401-engineered-clean-combined-revised-normal-seat-cycle-20261105/README.md) now closes the same normal-seat-to-powered-STOP-to-capture-and-unload slice for the saved combined-profile Revised route; the inherited route/controller/power and redstone pulse are disclosed. Fresh construction, remaining family/profile runs, recovery coverage and profile-matched runtime evidence on the refreshed artifact remain open. The selected-JAR [coupled Arrow continuous trace](docs/evidence/0.9/fd401c-coupled-arrow-continuous-trace-20261105/README.md) now records both Arrow UUIDs, positions, velocities, and native link continuity for 720 paired samples over one occupied pass; the controller reached Pass=1 without PassShort, while its disclosed FE fixture depleted afterward. This closes only the continuous-trace slice; fresh construction, remaining family/profile runs, and profile-matched recovery/cold-reopen evidence remain open. FE fixture dependency remains disclosed, not advertised. A corrected selected-JAR [combined wired FE interruption/restore](docs/evidence/0.9/fd401-combined-wired-fe-recovery-selected-20261105/README.md) with `requireEnergy=true` now proves an occupied `Starving=1` hold, automatic resumed motion after the water wheel restores FE, `PassStarved=1`, manual Capture and normal unload. The pass stopped short at 3.052/20 away from station, so full post-restore circuit and automatic station capture remain open. A subsequent [recharge-hold full-circuit run](docs/evidence/0.9/fd401c-selected-recharge-hold-full-circuit-20261105/README.md) completed the occupied circuit and safe unload after restoration, but remained at 19.4069/20 with `PassShort=1`; the 20/20 controller target and no-short criterion remain open. The [selected-JAR combined five-family FE/retention guard](docs/evidence/0.9/fd401c-selected-combined-five-family-fe-guard-20261105/README.md) passes on current production classes with a single GameTest-class overlay and direct fixtures; it does not close normal UI construction, client cycles, or the full matrix. A separate current-source Engineered-only five-family guard is recorded [here](docs/evidence/0.9/fd401c-engineered-current-five-family-fe-guard-20261105/README.md). Remaining family/profile runs and profile-matched recovery/cold-reopen evidence remain open. A selected-JAR [live PAX winch power-fault run](docs/evidence/0.9/fd401-pax-live-rider-power-fault-656725bc-20261105/README.md) mounted a rider normally, entered LIFTING, removed/restored motor power, recorded the winch power-loss fault and refused restraint release while the fault held. The selected-JAR [3 GiB-per-client fault-resume repeat](docs/evidence/0.9/fd401-pax-fault-resume-unload-3g-selected-20261105/README.md) now confirms ordinary resume, occupied LIFTING → OUTBOUND → RETURNING → CAPTURING → BOARDING, normal restraint opening and unload; the inherited route and upstream power remain disclosed, and this bounded PAX recovery result does not close the wider family/profile matrix. |
| **FD401D Create telemetry — complete on selected `ec2fcda` JAR** | All six display sources; each family, trailing-car occupancy, controller unavailable/re-entry and PAX phase/fault recovery; base and optional profiles | Goggles, signs and client snapshots agree with current whole-consist server state; unavailable equipment cannot advertise ready or authorize motion | [Telemetry handoff](docs/FD401-RELEASE-HANDOFF.md), [bounded two-client PAX recovery](docs/evidence/0.9/fd401-pax-two-client-recovery-8f370d0/README.md), and the earlier selected-JAR [native controller/source gap test](docs/evidence/0.9/fd401-selected-controller-telemetry-8f370d0/README.md) cover bounded PAX recovery and native-body unavailable/re-entry behavior. A focused selected-production-class [dispatch guard GameTest](docs/evidence/0.9/fd401d-controller-unavailable-dispatch-20261105/README.md) now proves a bound Arrow station rejects dispatch with either its native carrier or chassis unloaded, publishes `dispatchReady=false`, leaves serialized FoundationData unchanged, and recovers telemetry after restoration; the run overlaid only the GameTest class onto the selected JAR. A selected-JAR [Arrow controller lifecycle run](docs/evidence/0.9/fd401d-controller-lifecycle-selected-656725b-20261105/README.md) adds a combined-profile, unoccupied server/client transition from unavailable to re-entry to unavailable, with Observer telemetry at all three states. The remote Display Link was unconfigured, and the run did not establish sign output, matched goggles, occupancy, dispatch/motion, or profile coverage. Its [rendered-output follow-up](docs/evidence/0.9/fd401d-controller-lifecycle-rendered-selected-656725b-20261105/README.md) now configures Ride Fault → Line 1 through normal Display Link use and records loaded → unavailable → re-entry → unavailable on the same one-car Arrow: server availability, received client snapshots, goggles, and sign output agree at each settled state. This closes that bounded combined-profile unoccupied Arrow lifecycle-output slice; the loaded state retained an existing compatible-track fault, and no rider, dispatch, or motion was tested. A [fixed-source combined-profile telemetry candidate](docs/evidence/0.9/fd401-pax-bind-restore-fix-candidate/README.md) passes six Display Link source formatters and packet snapshots across Arrow (including a trailing car), Legacy, Free Spin, Revised and four-car PAX, plus PAX BOARDING/LIFTING/FAULT telemetry and dispatch guards. Its production classes match the refreshed clean `ec2fcda` JAR, but the run JAR includes one extra untracked GameTest class. The selected-JAR combined-profile [client reopen](docs/evidence/0.9/fd401-current-clean-combined-client-reopen-20261105/README.md) reached the normal Display Link configuration UI and showed its existing Ride fault → Line 1 mapping ([record](docs/evidence/0.9/fd401-current-clean-combined-display-link-ui-20261105/README.md)). A later [selected-JAR Arrow fault-state client pass](docs/evidence/0.9/fd401-current-clean-combined-fault-display-20261105/README.md) matched the Create goggles tooltip and rendered board fault text to a received `FD_CLIENT_TELEMETRY` snapshot for one unoccupied BOARDING state. This closes one bounded combined-profile fault observation only: the fault was preexisting, the board text is width-truncated, and no source transition occurred. A [selected-source occupied PAX phase cycle](docs/evidence/0.9/fd401-pax-selected-source-phase-telemetry-20261105/README.md) now records received fixed-source snapshots through BOARDING, LIFTING, OUTBOUND, RETURNING, CAPTURING and final BOARDING on one normal four-car cycle, with clean UI restraint release and rider unload. This closes that bounded phase-cycle slice only. A [selected-JAR static PAX Display Link run](docs/evidence/0.9/fd401-pax-display-link-six-source-output-20261105/README.md) used normal source-field controls and a sign target to observe all six choices at idle BOARDING, including `0 / 16` occupancy, clipped restraint/dispatch text, BOARDING phase, and blank fault. A new [occupied PAX Ride Occupancy source run](docs/evidence/0.9/fd401d-occupied-pax-display-link-selected-20261105/README.md) used the normal UI to select Ride Occupancy on the exact selected JAR; final block data confirms the linked sign reads `1 / 16` on both sides while Dev is mounted and the winch remains BOARDING. An observer packet and goggles screenshot from the same uninterrupted run also show occupied=1, BOARDING, and 1/16; the three-minute gap before final source application and lack of one simultaneous sign/goggles screenshot are disclosed. A later [exact-selected-JAR occupied PAX run](docs/evidence/0.9/fd401d-occupied-pax-goggles-snapshot-selected-20261105/README.md) joins normal boarding, the received occupied PAX snapshot, and matching goggles values within one stable interval (packet 8 seconds before the screenshot, with the same packet state repeated afterward); its existing sign remained blank and the source UI could not be reached. This closes only the occupied PAX goggles/client-snapshot slice. A subsequent [selected-JAR occupied PAX sign-output run](docs/evidence/0.9/fd401d-occupied-pax-sign-output-cross-20261105/README.md) configured a normal Ride Occupancy Display Link on the selected clean artifact and confirmed front/back `1 / 16` output while Dev remained mounted; the earlier packet and goggles values agree with that sign within the same stable BOARDING interval, though no single screenshot shows both outputs together. This closes the bounded combined-profile occupied PAX sign-output slice, not the full telemetry matrix. A new [selected-JAR fourth-car PAX occupancy slice](docs/evidence/0.9/fd401-pax-trailing-car-occupancy-selected-20261105/README.md) proves normal empty-hand boarding on the fourth car, one server-wide assignment, a matching occupied client carrier diagnostic, normal Shift unload, and post-unload `0 / 16` goggles output. The fixed-source client snapshots were both unoccupied, and no sign fixture was present in that input copy; occupied PAX snapshot/goggle/sign agreement is now covered only for the separate combined-profile slice above. A focused selected-production-class [PAX winch unavailable-carrier GameTest](docs/evidence/0.9/fd401d-pax-trailing-car-unavailable-selected-20261105/README.md) now removes a trailing PAX carrier through Sable UNLOADED, observes unavailable/not-ready winch telemetry, rejects both bound DISPATCH and direct start without serialized FoundationData/winch changes, then restores available BOARDING telemetry with the fault cleared. The overlay changes only RideTelemetryGameTests.class relative to the selected 656725bc… JAR. After restore, dispatchReady remained false with an ordinary carrier-readiness denial, so this closes only unavailable-state denial and telemetry re-entry; it does not prove re-dispatch/motion, visuals, or the family/profile matrix. A new [selected-JAR two-car Arrow occupancy output run](docs/evidence/0.9/fd401d-selected-arrow-occupancy-sign-20261105/README.md) captures the combined-profile client packet and occupied goggles view, restores Dev's saved seat assignment after server restart, renders the linked sign at `1 / 8`, and verifies normal Shift dismount returns it to `0 / 8`; it closes only this combined-profile Arrow occupancy/output slice. The [selected-JAR family replay attempt](docs/evidence/0.9/fd401d-selected-family-telemetry-656725bc-20261105/README.md) preserved successful Dev/Observer joins and initial two-car Arrow telemetry, but normal Legacy seat mounting did not succeed; do not count it toward family occupancy. A [focused selected-JAR retry](docs/evidence/0.9/fd401d-selected-family-retry-656725bc-20261105/README.md) reused the prior approach coordinates but F3 targeted stone and then a Display Link; no interaction or mount was attempted. A follow-up [exact-seat target retry](docs/evidence/0.9/fd401d-selected-legacy-seat-target-656725bc-20261105/README.md) used the prior mount log target, but four bounded F3 probes still hit ordinary stone or station blocks, so no click was sent. The historical accepted seat target `(20481032,127,20489225)` came from the byte-identical input save but an older JAR; its successful camera rotation was not recorded. For another selected-JAR attempt, send no click until F3 identifies `fourthdimension:legacy_seat` at that exact native target. Complete the remaining family occupancy/output agreement, rendered controller re-entry visuals, and current-artifact base/optional-profile matrix. The focused [Arrow controller recovery run](docs/evidence/0.9/fd401d-arrow-controller-reentry-dispatch-20261105/README.md) now covers fail-closed carrier/chassis unavailability, restored dispatch readiness, accepted dispatch and measured motion under controlled physical input; it does not cover rendered output or propulsion. The selected-production-class [base-profile family telemetry run](docs/evidence/0.9/fd401d-base-profile-family-telemetry-20261105/README.md) now passes occupied formatter/goggle/update-tag cases across all five families, including PAX phase and fail-closed guards; actual signs, remote client application, and the Engineered-only/combined current-artifact profile rows remain open. The selected-production-class [Styles-only family telemetry run](docs/evidence/0.9/fd401d-styles-only-family-telemetry-20261105/README.md) passes the same bounded five-family server formatter/goggle/update-tag matrix with Styles 1.2.0 installed. The [Engineered-only family telemetry run](docs/evidence/0.9/fd401d-engineered-only-family-telemetry-20261105/README.md) passes the same bounded five-family server matrix with Engineered 1.1.1 installed. The [combined-profile family telemetry run](docs/evidence/0.9/fd401d-combined-family-telemetry-20261105/README.md) also passes on the selected production entries with both providers installed and without the FE validation network. The subsequent selected-JAR Arrow occupancy and controller lifecycle runs plus PAX six-source and occupied-output records close the representative rendered/client agreement checks; code review confirms the shared path and distinct PAX controller coverage described above. FD401D is complete within this bounded scope; no family × source × profile product remains as a gate. |

The clean-JAR combined [Free Spin](docs/evidence/0.9/fd401-engineered-clean-combined-free-spin-cycle/README.md) and [Revised](docs/evidence/0.9/fd401-engineered-clean-combined-revised-cycle/README.md) cycles add two FD401C client operator results with STOP, FE draw, station capture and normal UI unload. Both use saved normally built routes and helper boarding, so the remaining FD401C requirements above still apply.

The clean-JAR [combined four-car PAX authority cycle](docs/evidence/0.9/fd401-engineered-clean-combined-pax-authority/README.md) adds normal seat boarding, UI dispatch, an occupied winch cycle, return capture and normal unload on the changed PAX runtime classes. The powered opposing Engineered controller stayed at pass zero with no FE draw; the saved train and route were not newly constructed, and per-car callback cancellation was not directly traced. A separate [Engineered-only selected-JAR cycle](docs/evidence/0.9/fd401-engineered-clean-engineered-only-pax-authority/README.md) repeats normal boarding, UI dispatch, LIFTING/RETURNING, BOARDING return and normal unload on the saved four-car train without Track Styles; controller pass and FE stayed unchanged under the powered challenge. Per-car traces, normal power-source construction and active-phase interruptions remain open.

The earlier [Legacy normal-seat retries](docs/evidence/0.9/fd401-legacy-clean-normal-seat-retry/README.md) and [keyboard Use control](docs/evidence/0.9/fd401-legacy-keyboard-use-control-20261105/README.md) failed from the original aiming pose. A diagnostic readout found the client's actual Use hit result was `MISS` there, although the seat was ready and reachable. The [selected-JAR normal boarding pass](docs/evidence/0.9/fd401-legacy-clean-normal-board-20261105/README.md) moved closer, aimed down, mounted the same physical Legacy seat by default right click and unloaded normally. A later [continuous selected-JAR run](docs/evidence/0.9/fd401-legacy-clean-continuous-normal-cycle-20261105/README.md) joined normal boarding to the FE-powered STOP, station capture, UI restraint release and normal dismount in one client session. This closes the split Legacy operator gap on the saved combined-profile route; fresh construction, other profile/family rows and FE recovery remain open.

**Done when:** FD401A–D have direct or justified carried-forward acceptance for
the advertised profiles under the validation policy. Within-curve style segmentation, arbitrary cosmetic/resource-pack
meshes, accelerated Flywheel under the recorded shader fallback, and larger
unmeasured packet-delay envelopes are not certified by existing evidence.
A failed advertised feature must be fixed or explicitly disabled/withdrawn with
updated support documentation; a historical pass cannot waive it.

### FD402 Evaluate and document recovery towing

**Execution steps:** [FD402](docs/roadmap/0.4-core.md#fd402).

**Status: complete (2026-11-11) for the declared recovery scope.** FD402A
mechanics/clearance, FD402B rendered recovery, FD402C operator workflow, FD402D
one-player persistence, FD402E multiplayer interruptions and FD402F optional
power are complete. Reuse accepted observations across these tasks. See [operator evidence](docs/evidence/0.4/recovery/operator-acceptance.md)
and [historical milestones](docs/ROADMAP-CHECKPOINT-HISTORY-2026-11-04.md#fd402-evaluate-and-document-recovery-towing).

**Depends on:** FD108, FD106, FD305. **Design:** D03.

**Assignment boundary:** FD402 owns ordinary-bearing recovery attachment,
towing and its retained-state interruptions. It does not own player crafting,
normal ride construction or family teaching; those are FD403. Reuse accepted
setup/operator evidence and work only the open recovery cases. Close FD402 when
its own A–F criteria are covered, even while FD403 remains open. See the
[0.4 package boundary](docs/roadmap/0.4-core.md#choose-the-assigned-package).

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
| **FD402A Mechanics and clearance — complete (2026-11-05)** | Five single-car + two linked banked routes; four-car Arrow flat strength; original native parts and certified equipment | Preserve all IDs, riders, couplings and Arrow profiles through tow/brake/power-cut/detach/unload; reproduce pinned minimum-clearance thresholds without relaxing them; five-car, wrong-family, bad-equipment, stall and overspeed cases fail closed | [Current-input review](docs/evidence/0.4/recovery/current-input-review-95301062/README.md) carries forward the [one-tick](docs/evidence/0.4/recovery/current-tick-rider-clearance/acceptance.md), [substep](docs/evidence/0.4/recovery/remaining-family-native-physics-substeps/acceptance.md), [wearable](docs/evidence/0.4/recovery/vanilla-wearable-clearance/acceptance.md), [strength](docs/evidence/0.4/recovery/strength-rating/acceptance.md), rejection and interlock results against current geometry, policy, solver and dependency inputs. Sampled/discrete-solver limits remain explicit. |
| <a id="fd402b"></a> **FD402B Rendered recovery — complete (2026-11-11)** [Steps](docs/roadmap/0.4-core.md#fd402b) | The same seven banked routes in a connected client; real mounted vanilla player, rack, linkage and both linked trains | Observe moving player/body/rack/track relationships through bend, sustained bank, runout and release, with identity/mount retention; render observations and native-frame clearance evidence agree | [Current banked render matrix](docs/evidence/0.4/recovery/occupied-banked-render-motion/current-matrix-20261111.md) combines seven current client slices with the [banked-train release](docs/evidence/0.4/recovery/banked-trains/acceptance.md), [family substep](docs/evidence/0.4/recovery/remaining-family-native-physics-substeps/acceptance.md), [model-clearance](docs/evidence/0.4/recovery/model-clearance/acceptance.md) and [two-client lifecycle](docs/evidence/0.4/recovery/two-client-recovery-lifecycle/acceptance.md) records. Together they establish occupied rendered bend/bank/runout movement, mounted identity retention, native-frame agreement, braking, detach and unload for the seven declared routes. |
| **FD402C Operator workflow — complete (2026-11-05)** | Shared normal occupied Arrow workflow, linked-train behavior and distinct stationary PAX handover; standalone. Add another family only for a different operator path | Real bearing selection, Attach/Start/Brake controls and physical lever power; brake before boundary; stationary detach, restraint opening and normal unload; no fixture dispatch substitutes for input | The [occupied Arrow continuation](docs/evidence/0.4/recovery/manual-occupied-arrow-operator-20261105/README.md) combines the accepted normal [bearing attachment](docs/evidence/0.4/recovery/manual-bearing-selection/acceptance.md), [physical lever](docs/evidence/0.4/recovery/manual-lever-propulsion/acceptance.md) and [pre-boundary brake](docs/evidence/0.4/recovery/power-control/acceptance.md) inputs with an occupied Start/Brake and selected-JAR detach/open/Shift-unload. Existing [linked-train](docs/evidence/0.4/recovery/banked-trains/acceptance.md) mechanics and the distinct sixteen-rider [PAX handover](docs/evidence/0.4/recovery/occupied-handover/acceptance.md) complete the finite cumulative path; no family has a different ordinary-bearing UI workflow. |
| **FD402D One-player persistence — complete (2026-11-05)** | Each single-car family plus original linked Arrow/PAX: save braked recovery, exit JVM, reopen same world, explicitly resume and normally release | Same chassis/bearing/carriers/native links/rider return; no automatic tow; final detach/unload clears assignment; both processes exit normally | [Retained five-family logs](docs/evidence/0.4/recovery/fresh-process-recovery/acceptance.md#retained-observations-and-artifact-boundary) carry forward the bounded single-car observations. The [selected-production linked-train checkpoint](docs/evidence/0.4/recovery/linked-train-fresh-process-656725bc-20261105/README.md) restores the exact two-car Arrow and four-car PAX graphs in fresh JVMs, holds them braked until explicit start, moves every linked body, and clears the recovery link and saved rider assignment after normal release. Only the validation fixture entries differ from selected SHA `656725bc…`; all production entries are byte-identical. |
| <a id="fd402e"></a> **FD402E Multiplayer interruptions — complete (2026-11-11)** [Steps](docs/roadmap/0.4-core.md#fd402e) | Two real clients in distinct seats on representative Arrow and four-car PAX; native family coverage plus a base/combined adapter check. Add family/profile cases for distinct lifecycle paths | Disconnect/rejoin one rider while the other stays; cold restart braked recovery; chunk unload/reload; coupling/bearing loss and restore/removal all retain or safely release original identities with no duplication or automatic resume | [Two-client Arrow cycle](docs/evidence/0.4/recovery/two-client-recovery-lifecycle/acceptance.md) closes its bounded fixture-operated tow/release observation. [E1–E5 two-client interruptions](docs/evidence/0.4/recovery/two-client-interruptions-20261111/README.md) close braked cold server restart, one-rider reconnect, native chunk/bearing return and temporary coupling loss/restore with original seats/native links and explicit rendered recovery. [PAX coupling and bearing removal](docs/evidence/0.4/recovery/permanent-pax-interruptions-20261111/README.md) closes E6 and E7 cumulatively with accepted native cleanup. Both permanent-removal cases retain the original four-car train, eight carriers and two rider seats, report the production safety fault, and do not auto-resume. FD902B consumes the same evidence; keep permanent removal distinct from temporary unavailability. |
| <a id="fd402f"></a> **FD402F Optional power — complete (2026-11-11)** [Steps](docs/roadmap/0.4-core.md#fd402f) | Certified ordinary bearing, occupied Arrow recovery, Engineered-only and combined; declared FE producer/wire fixture | Manual controller and supply actions demonstrate braked/starved holds, explicit towing/consumption, capped speed, restored power without automatic motion, rebraking and cleanup | The [rendered manual Engineered-only record](docs/evidence/0.4/recovery/engineered-fe-manual-20261111/README.md) combines normal provider construction/wiring and recovery controls with measured occupied motion and FE draw, closing FD402F1. The [combined rendered control record](docs/evidence/0.4/recovery/engineered-fe-combined-pass-20261111/README.md) closes FD402F2 with zero-FE hold, supplied explicit motion, constant-FE braked holds and rendered Brake/Detach/Open/Shift cleanup. All-family Engineered recovery and stronger ordinary assemblies are excluded from the current claim. |

**Done when:** FD402A–F pass for this explicit scope, unsupported cases remain
rejected or clearly documented, and [RECOVERY.md](docs/RECOVERY.md) matches the
qualified artifact. This replaces the ambiguous “broader lifecycle/propulsion”
queue. It does not reduce any advertised original family or discard prior failures.

### FD403 Complete crafting and teaching content

**Execution steps:** [FD403](docs/roadmap/0.4-core.md#fd403).

**Status: complete 2026-11-15.** FD403A–F meet the existing
[connected-client acceptance matrix](docs/FD403-ACCEPTANCE.md), with family and
teaching claims closed from compatible cumulative evidence.

**Depends on:** FD305. **Design:** D08.

**Assignment boundary:** FD403 owns standalone player construction, normal
powered ride operation, save/reopen and usable family teaching. It does not
require FD402 closure, ordinary-bearing towing or FD402E's interruption matrix.
Existing compatible evidence can be shared without inheriting the other package's
unfinished work. FD404 later consumes both packages independently.

| Task / status | Acceptance outcome | Evidence / closure record |
| --- | --- | --- |
| <a id="fd403a"></a> **FD403A Arrow — complete 2026-11-13** [Steps](docs/roadmap/0.4-core.md#fd403a) | Full player-built powered circuit with controlled full turn | [Cumulative Arrow acceptance](docs/evidence/0.4/fd403a-circuit-reconciliation-20261113/README.md) combines accepted normal crafting/paid connections and controlled 50 m full turn with connected-client C–D Chain placement/traversal, two occupied circuit returns, normal capture/open/unload, cold reopen and Arrow teaching reconciliation. The separate `:256` timeout is retained as an incomplete attempt and contributes no final-state claim. The current editor response path carries forward under the [2026-11-14 action acknowledgement recheck](docs/evidence/0.4/fd403a-editor-ack-recheck-20261114/README.md). |
| <a id="fd403b"></a> **FD403B Legacy — complete 2026-11-14** [Steps](docs/roadmap/0.4-core.md#fd403b) | Player-built shaped powered route with visible free response | [Connected-client shaped powered route](docs/evidence/0.4/fd403b-legacy-shaped-powered-20261114/README.md) closes normal construction, connected drive, occupied travel/free response, safe capture/open/unload and save; accepted cold-reopen evidence carries forward. [Legacy teaching reconciliation](docs/evidence/0.4/fd403b-legacy-teaching-reconciliation-20261112.md) closes its FD403F walkthrough claim. |
| <a id="fd403c"></a> **FD403C Free Spin — complete 2026-11-15** [Steps](docs/roadmap/0.4-core.md#fd403c) | Player-built shaped route with installed inducer and damper spans | [Shaped occupied-route acceptance](docs/evidence/0.4/fd403c-shaped-occupied-20261115/README.md) combines retained normal crafting/construction with the occupied released-carrier DAMP pass before normal capture/open/unload and cold reopen. The disclosed upstream power fixture and temporary support removal do not receive construction credit. Free Spin's family contribution to FD403F is reconciled in the same record. |
| <a id="fd403d"></a> **FD403D Revised — complete 2026-11-14** [Steps](docs/roadmap/0.4-core.md#fd403d) | Player-built powered route with independent lower-hinge behavior | [Revised continuation review](docs/evidence/0.4/fd403d-revised-blocker-20261114/review.md) combines normal construction, occupied powered travel followed by normal capture/unload, independent lower-hinge response, capture/open/unload and the unchanged Revised walkthrough. The command-authored powered demo remains supporting evidence. |
| <a id="fd403e"></a> **FD403E PAX — complete 2026-11-11** [Steps](docs/roadmap/0.4-core.md#fd403e) | Survival-built four-car tutorial, ordinary client linking and winch setup | [Cumulative acceptance and corrected walkthrough](docs/evidence/0.4/fd403-workflows-20261111/README.md#pax--fd403e-and-its-fd403f-walkthrough-accepted) combine accepted crafting, paid construction, three normal Chain links, winch binding, occupied cycle and cold reopen with the unchanged rendered teaching. Upstream materials/supports and creative motor remain disclosed fixtures. |
| <a id="fd403f"></a> **FD403F Teaching — complete 2026-11-15** [Steps](docs/roadmap/0.4-core.md#fd403f) | All fifteen item descriptions, nine unique Ponder lessons and 53 registrations, using accepted unchanged resource evidence | [Current-artifact rendered review](docs/evidence/0.4/teaching-current/acceptance.md) retains the accepted descriptions, lessons and registrations. PAX, Arrow, Legacy, Revised and Free Spin walkthrough claims are reconciled in their family records; the Free Spin contribution is in the [FD403C acceptance](docs/evidence/0.4/fd403c-shaped-occupied-20261115/README.md). No further lesson replay is required. |

**Completion:** FD403A–F meet the existing [recording procedure](docs/FD403-ACCEPTANCE.md#acceptance-record), and each family has an accepted acquisition/build path and walkthrough that completes its advertised mechanics without optional mods. Disclosed fixture ingredients are not unassisted resource-gathering evidence.

### FD404 Version 0.4 gate

**Execution steps:** [FD404](docs/roadmap/0.4-core.md#fd404).

**Status: complete 2026-11-15; five-family development checkpoint.**

**Depends on:** FD401A–D, FD402A–F, FD403A–F.

FD402 and FD403 are sibling inputs to this gate, not steps inside one another.
An assignment to close FD402 does not include closing FD403 or FD404.

Record the accepted five-family source/JAR, dependency identities and bounded
support scope in [CURRENT-QUALIFICATION.md](docs/CURRENT-QUALIFICATION.md).
This is the core development checkpoint, not the final expanded feature freeze.
New families/adapters follow their own packages; FD1000 owns the 1.0 alpha freeze.

**Done when:** FD401–FD403 meet their finite criteria and the five-family core is
usable and documented. Reuse accepted results. Closing FD404 neither launches
final release qualification nor requires new JARs for documentation changes.

The accepted source/JAR, pinned dependencies and bounded support scope are
recorded in [CURRENT-QUALIFICATION.md](docs/CURRENT-QUALIFICATION.md#artifact-and-scope).
FD401A–D, FD402A–F and FD403A–F are closed against their existing criteria;
FD1000 retains the expanded 1.0 feature freeze and final release qualification.

## Milestone 0.5 — designs, extension contracts and Wing

All six original discovery IDs and FD1601S/FD1702S/FD1801S/FD1802S/FD1901S
belong to the active 0.5 work; completed specifications are linked below. Resolve Pipeline, S1/S2, Axis, Roller Ball, Fixed Inverted,
Dive, both Flying variants and Powered Inverted Yaw before detailed implementation. A specification
must identify confirmed references versus inspired design choices. Retain any
necessary feasibility prototype rather than requiring a new one in a later stage.

### FD501 Select the expansion dependency and migration baseline

**Execution steps:** [FD501](docs/roadmap/0.5-contracts.md#fd501).

**Status: complete (2026-11-14 America/Phoenix).** [Decisions and pinned catalog](docs/evidence/0.5/dependencies-tooling/dependency-decisions.md). **Depends on:** current core dependency inventory; read-only
review can begin before FD404. **Design:** D04, D06, D07, D09.

1. Refresh the dated addon catalog through the backlog procedure; resolve actual mod IDs, Additions dependency closure and the graphics/seat stack's version constraints.
2. Record the minimum core baseline for new vehicle mechanics separately from each optional adapter's dependency closure. Name the actual consumers of each upgrade or migration. Keep existing pins where sufficient; a Skin or Additions dependency problem does not block Wing by default.
3. Decide whether the archived provider registry or a new schema/protocol is necessary for core mechanics or only a named adapter. Record preservation/missing-provider behavior; do not assume archived schema 7/protocol 8 is ready.
4. Plan a compatible final profile set and pin each selected implementation baseline. Core and individual adapter decisions may be used as soon as recorded, without waiting for this whole discovery package to close. Later updates require a named blocker or planned change, not a routine “latest version” refresh.

**Done when:** core and per-adapter capability/dependency decisions, consumers and
remaining upstream blockers are explicit. FD501A owns core-required changes;
FD501B–H own adapter-specific changes. Recording an unavailable capability does
not certify it or drop it from 1.0; its consuming package remains blocked.

### FD501A Apply core-required dependency or persistence changes

**Execution steps:** [FD501A](docs/roadmap/0.5-contracts.md#fd501a).

**Status: complete — no core dependency/provider/schema change required (2026-11-14 America/Phoenix).**
[Reviewed disposition](docs/evidence/0.5/wing/core-baseline.md).
**Depends on:** FD404 and the recorded core decision within FD501.

Completion of the optional-addon inventory or FD501B–H is not required.

Implement only dependency/provider/format changes actually required by the new
core vehicle mechanics. Preserve stable family/provider/vehicle identities and
existing recovery fixes. Use current `main` and selectively review archived work.
Verify affected loads, supported upgrades, missing providers and backup restoration
where the change requires them. Optional-adapter-only changes belong to FD501B–H.

**Done when:** the required core changes are integrated with affected checks passed,
or the core decision records that the existing baseline is sufficient. A no-change
disposition requires no runtime replay. Later adapter upgrades do not reopen this
completed task; they validate their affected paths in their own package.

### Adapter-specific dependency packages

Each row depends on FD404 and its own recorded FD501 decision, not completion of
FD501A or other adapter rows unless it actually consumes their changes. Record any
such dependency explicitly. These packages deliver with their consumers in 0.8
or 0.9; FD503 requires their decisions, not all their implementations.

| Package / status | Consumer | Steps and completion condition |
| --- | --- | --- |
| <a id="fd501b"></a> **FD501B Additions prerequisites — planned** [Steps](docs/roadmap/0.8-operations.md#fd501b) | FD810, FD911 | Resolve embedded identity and common required dependencies; implement only necessary upgrades/migration. Seat-only prerequisites belong to FD911 and do not block FD810 or closure of this shared baseline. |
| <a id="fd501c"></a> **FD501C CTNB prerequisites — planned** [Steps](docs/roadmap/0.8-operations.md#fd501c) | FD811 | Resolve selected material/control dependencies and any functional-provider migration required by those capabilities. |
| <a id="fd501d"></a> **FD501D Magnetized prerequisites — planned** [Steps](docs/roadmap/0.8-operations.md#fd501d) | FD812 | Resolve the propulsion adapter's dependency and version requirements. |
| <a id="fd501e"></a> **FD501E Coaster Skin prerequisites — planned** [Steps](docs/roadmap/0.9-presentation.md#fd501e) | FD910 | Resolve required core/graphics versions and body/seat-provider changes for the agreed Skin capabilities. |
| <a id="fd501f"></a> **FD501F Immersion prerequisites — planned** [Steps](docs/roadmap/0.9-presentation.md#fd501f) | FD912 | Resolve the client effects stack and client/server installation constraints. |
| <a id="fd501g"></a> **FD501G Waterparked prerequisites — planned** [Steps](docs/roadmap/0.9-presentation.md#fd501g) | FD913 | Resolve the selected coexistence profile's dependencies and shared graph/render constraints. |
| <a id="fd501h"></a> **FD501H Create Coasters prerequisites — planned** [Steps](docs/roadmap/0.9-presentation.md#fd501h) | FD914 | Resolve the selected utility/coexistence profile's dependencies. |

Each row closes when its consumed baseline is usable on `main` and affected
dependency/load/migration checks pass, or a documented no-change decision suffices.
If a later consumer needs an additional change, give it a bounded case in that
consumer's implementation package; unrelated consumers use the already-ready
baseline. Keep the prerequisite closed unless its own accepted behavior is affected.
Reuse a shared upgrade
and its evidence rather than reimplementing or rerunning it per adapter. Keep one
integrated core and the declared compatible profiles; this split does not permit
incompatible forks to count as combined-profile acceptance. All agreed capabilities
still feed FD813/FD915 and FD1000 through their consuming packages.

### FD502 Make required validation tools reusable

**Execution steps:** [FD502](docs/roadmap/0.5-contracts.md#fd502).

**Status: complete (2026-11-14).** **Depends on:** FD006; changes require a named current blocker.

The [native collector acceptance](docs/evidence/0.5/dependencies-tooling/native-smoke/README.md)
combines the accepted input/fixture/resource checks with completed client frames,
server ticks, heap and actual JFR GC readback; no dropped rows or collector errors.
The lead corrected shell-launcher PID sampling to select the owned game JVM and
verified it during the saved Wing server's flush (see the sampler addendum).

The current narrow assignment and owner are in the [active queue](docs/CURRENT-QUALIFICATION.md#fd403-construction-and-teaching-cases).
It may run during 0.4: FD404 is not a prerequisite. The executable private
client procedure and usable retained Arrow circuit are recorded in the [handoff](docs/evidence/0.4/fd502-execution-repair-20261113/README.md).
The handoff supported the separate [FD403A acceptance](docs/evidence/0.4/fd403a-circuit-reconciliation-20261113/README.md);
it did not by itself close that gate or activate unrelated FD904 collectors.
Later named tooling needs retain their existing scope.

Repair only fixture, targeting, diagnostic or measurement gaps that prevent
reliable work. Retain reusable worlds, named scenarios and run identities. Add
or verify frame/tick/memory collectors needed by FD904B and take useful early
measurements when warranted. Do not rebuild a harness merely for this milestone.

**Done when:** needed scenarios can be reproduced without reconstructing a park
for every task, and required collectors report interpretable measurements. Existing
tools may close this task by review; final park acceptance remains after FD1000.

### FD503 Milestone 0.5 gate

**Execution steps:** [FD503](docs/roadmap/0.5-contracts.md#fd503).

**Status: complete (2026-11-15 UTC).** [Acceptance reconciliation](docs/evidence/0.5/acceptance.md). **Depends on:** FD404, FD501 decisions, FD501A, FD502;
FD1101, FD1301, FD1302, FD1401, FD1501, FD1502 specifications;
FD1601S, FD1702S, FD1801S, FD1802S, FD1901S specifications;
FD1101A–C and FD904C1.

FD501B–H implementation and FD904C2 are not prerequisites.

**Done when:** all new-family designs are explicit, fixed-seat Wing proves the
necessary extension contracts through normal construction/ride/unload, and seat,
restraint, pose/camera ownership and comfort are established. This is an aggregate
checkpoint; other work starts from its specific prerequisites rather than waiting
for unrelated specifications or examples.

## Milestone 0.6 — reusable motion mechanisms

Implement S1 conventional free-spinning (FD1501A–C), then Suspended
(FD1301A–C), then Fixed Inverted (FD1601A–C) using the integrated Wing and
Suspended contracts. Establish configurable axes, loading/damping, bounded swing,
lift/station locks and fixed overhead seating through these vehicles. Preserve the accepted
original mechanisms; changes to shared axis/capture paths need affected tests,
not new Styles or whole-release qualification.

### FD601 Milestone 0.6 gate

**Execution steps:** [FD601](docs/roadmap/0.6-motion.md#fd601).

**Status: planned.** **Depends on:** FD503, FD1501A–C, FD1301A–C, FD1601.

**Done when:** S1, Suspended and Fixed Inverted have usable vehicles, content and accepted
player workflows. Their distinct interruption/persistence cases are covered;
accepted shared behavior is reused. Remaining Spinning variants belong to 0.8.

## Milestone 0.7 — complex motion and route behavior

Implement Axis (FD1302A–C), then Roller Ball (FD1502A–C). Reuse established carrier
infrastructure without equating their axes, dynamics or routes. Resolve off-axis
clearance, capture and route transitions in these packages before presentation
integrations depend on their behavior.
Complete Conventional Flying (FD1801), then New-generation Flying (FD1802),
including scenery-facing yaw in both postures; FD1803 reconciles both variants.
Flying consumes FD1601A and need not wait for Axis/Roller Ball or later multi-train
operations. Integrate shared runtime changes one owner at a time.

### FD701 Milestone 0.7 gate

**Execution steps:** [FD701](docs/roadmap/0.7-complex-motion.md#fd701).

**Status: planned.** **Depends on:** FD601, FD1302A–C, FD1502A–C, FD1803.

**Done when:** Axis, Roller Ball and both Flying variants complete their specified occupied
operating cycle, player workflow and distinct interruption/recovery cases.

## Milestone 0.8 — remaining vehicles and operations

Complete Pipeline (FD1401A–C), S2 launched/inverting free-spinning
(FD1501D–F), Dive (FD1702), Powered Inverted Yaw (FD1901/FD1902), and the
operations packages below. FD1701A supplies required hold/dispatch authority;
FD1701B/C multi-train management remains post-1.0. Pipeline may start after its own
geometry/mechanism prerequisites; there is no artificial Axis dependency.
Adapters may start after their control/provider prerequisites reach `main`.
The [integration backlog](docs/INTEGRATION-BACKLOG.md#what-each-integration-means)
defines the capability boundaries, optional installation and dependency caveats.

| Package / status | Prerequisites | Implementation steps and completion condition |
| --- | --- | --- |
| <a id="fd810"></a> **FD810 Additions station operations — planned** [Steps](docs/roadmap/0.8-operations.md#fd810) | FD401, FD501B; integrated control/telemetry contracts | Resolve runtime identity, read whole-consist telemetry, submit validated station requests and reject stale/unsafe dispatch. Supply an operator example. Complete when the agreed station capabilities preserve restraints, faults and mechanics authority; rotating seats remain FD911. |
| <a id="fd811"></a> **FD811 CTNB tracks and controls — planned** [Steps](docs/roadmap/0.8-operations.md#fd811) | FD501C; reviewed functional provider/control contracts | Enumerate included materials/behaviors, implement measured eligibility and provider joins, preserve profiles and add the selected station/launch/reverse controls. Complete when agreed capabilities and unsafe/unknown rejections pass, with a usable example. |
| <a id="fd812"></a> **FD812 Magnetized propulsion — planned** [Steps](docs/roadmap/0.8-operations.md#fd812) | FD401, FD501D; integrated propulsion ownership contract | Implement directional propulsion/braking, rollback and power transitions. Keep longitudinal propulsion distinct from carrier-axis magnetic torque. Complete when the operator example and affected authority/interlock cases pass. |
| <a id="fd813"></a> **FD813 Milestone 0.8 gate — planned** [Steps](docs/roadmap/0.8-operations.md#fd813) | FD701, FD1401A–C, FD1501D–F, FD1702, FD1902, FD810–FD812 | Reconcile individual evidence and check actual control-conflict pairs plus the named combined operations profile. Complete when all agreed vehicles/variants and 1.0 operations capabilities are usable and documented. A staged JAR or startup alone is not acceptance. |

Shared control/ownership changes use one owner and integrate before dependent
adapters. One adapter's dependency issue need not idle independent vehicle work.

## Milestone 0.9 — presentation, optional seats, coexistence and visual quality

Presentation and seat integrations follow stable representative fixed, freely
rotating and constrained-motion vehicles. Read-only inspection and asset concepts
may happen earlier. Final adapters consume the established interfaces rather than
redefining mechanics or forcing every family to repeat acceptance.
FD916 reviews and improves the complete model/texture set, including the original
five families. Family content tasks use its checklist earlier; final review closes
before the 1.0 alpha freeze without reopening accepted mechanics.

| Package / status | Prerequisites | Implementation steps and completion condition |
| --- | --- | --- |
| <a id="fd910"></a> **FD910 Coaster Skin — planned** [Steps](docs/roadmap/0.9-presentation.md#fd910) | FD501E; accepted representative fixed/free/constrained seat and body interfaces | Select agreed chassis/skin options, preserve identity and restraint ownership, reject unsupported modifications, implement fallback/persistence and document choices. Complete when those capabilities work on the named profiles. |
| <a id="fd911"></a> **FD911 Rotating-seat integration — planned** [Steps](docs/roadmap/0.9-presentation.md#fd911) | FD501B; stable carrier/seat/restraint interfaces demonstrated by family packages | Integrate the agreed Additions seat capabilities separately from station telemetry; preserve mount ownership, rider count, restraints, restoration and unload. Complete with actual rider synchronization and the distinct lifecycle cases. |
| <a id="fd912"></a> **FD912 Immersion — planned** [Steps](docs/roadmap/0.9-presentation.md#fd912) | FD501F, FD904C1; stable representative pose/camera interfaces | Compose effects once, respect relative mouse look and comfort, handle first/third person and remote bodies, restore the camera on exit and verify supported client-only installation. Complete without altering authoritative seat motion. |
| <a id="fd913"></a> **FD913 Waterparked coexistence — planned** [Steps](docs/roadmap/0.9-presentation.md#fd913) | FD501G; representative accepted core rides | Inspect early, then check shared graph/mixin/render behavior in the named profile. Complete with independent ride coexistence and refusal of slide eligibility for our cars; no amphibious or transfer feature is implied. |
| <a id="fd914"></a> **FD914 Create Coasters coexistence — planned** [Steps](docs/roadmap/0.9-presentation.md#fd914) | FD501H; representative accepted core rides | Inspect utility, seat-lock and camera overlap; implement only agreed demonstrated bridges and otherwise coexistence. Complete when utilities and our rides operate together without granting Create-train authority over Sable rides. |
| <a id="fd915"></a> **FD915 Milestone 0.9 gate — planned** [Steps](docs/roadmap/0.9-presentation.md#fd915) | FD813, FD910–FD914, FD916 | Resolve actual seat/pose/render/control conflicts in the declared combined profile, include client-only asymmetry, and finish player configuration guidance. Complete when the agreed presentation/seat/coexistence capabilities preserve mechanics and rider state and FD916's model/texture criteria pass. |

### FD916 Review and improve models and textures

**Execution steps:** [FD916](docs/roadmap/0.9-presentation.md#fd916).

**Status: planned.** **Delivery:** 0.9. **Design:** D08, D16, D17.
**Depends on:** FD404; FD1101B, FD1501B, FD1501E, FD1301B, FD1302B, FD1502B and FD1401B;
FD1601B, FD1702B, FD1801B, FD1802B and FD1901B;
FD910/FD911 for affected optional appearance and seat configurations.

Review the original five families plus Wing, all agreed Spinning variants,
Suspended, Axis, Roller Ball, Pipeline, Fixed Inverted, Dive, Conventional Flying,
New-generation Flying and Powered Inverted Yaw. Include their vehicle bodies, seats,
restraints, wheels, pivots, required tracks, ride equipment and associated item
models/textures. This is required visual-finish work, not a claim that every
existing asset needs replacement or that exact manufacturer replicas are required.
Third-party resources remain external; this task improves our authored assets
and adapter presentation rather than redesigning another mod's artwork.

1. Inventory the declared assets and choose representative comparison views,
   lighting, scale and a consistent visual reference sheet from the agreed designs.
   Use stable specifications and existing original assets to establish the finish
   standard; do not introduce new liveries, families or redesign goals during review.
2. Review each family's assets against the checklist below. Reuse adequate existing
   captures and family-content reviews. Record one finite correction list with the
   affected asset, failed criterion and intended correction.
3. Improve the listed models/textures in their owning content package or FD916 for
   original-family assets. Preserve functional dimensions, seat/pivot definitions,
   identities and mechanics unless the specific correction requires a change.
4. Retain before/after screenshots for changed assets and representative inventory,
   placed-world and occupied-motion views. Use existing player/renderer evidence
   when it shows the required asset state; capture only missing or affected views.
5. Close corrected cases once their criterion passes, reconcile coverage across the
   declared set, and pass the result to FD915 and FD1000. Remaining preferences that
   do not violate a criterion become nonblocking follow-up work.

| Required criterion | Completion condition |
| --- | --- |
| Silhouette, proportions and style | Each family reads as its specified design, with proportions consistent with its functional dimensions and the recorded visual reference. Intentional game approximations remain documented. |
| Assembly and motion alignment | Seats, restraints, wheels, pivots and track/equipment components line up in the relevant stationary and operating poses; no unintended gaps, intersections or visible separation obscure their function. Reuse accepted clearance and pose evidence where applicable. |
| Texture finish | Declared surfaces have intentional textures; no missing-texture fallback, unintended placeholder, obvious UV stretching or unintended seam remains in the chosen views. Material treatment and detail density follow the recorded visual reference. |
| Player-facing appearance | Inventory/held representations where applicable, placed assets and occupied motion remain recognizable and readable at normal gameplay scale. Required state cues and rider/restraint relationships remain visible. |
| Supported appearance variants | Agreed variants and affected optional skin/seat configurations retain the intended finish and functional cues. Shared unchanged assets may share evidence; do not require every family × livery × graphics-profile combination. |

Per-family review and corrections may proceed once that family's dimensions and
assets are stable; they do not wait for every dependency above. Final closure
reconciles the complete set and affected optional configurations. The earlier B
content tasks use this checklist, not completion of FD916, as their standard.

Texture-only changes require only their affected visual checks and retain accepted
physics/persistence. Geometry, seat, pivot or clearance changes receive only the
affected rendering, rider/clearance and mechanical checks identified by the diff.
An asset edit or new JAR hash never resets a family or release campaign. FD904A
consumes this review alongside its technical rendering evidence; it does not order
another aesthetic review. Follow [VALIDATION-POLICY.md](docs/VALIDATION-POLICY.md).

**Done when:** every declared asset group has checklist coverage and all recorded
criterion failures are corrected with evidence. No unintended placeholder or
missing asset remains in release scope. Freeze the accepted visual baseline and
stop; further taste-based refinements are follow-up work. Existing mechanics and
earlier milestone acceptance remain closed unless an actual affected case fails.

## Version 1.0 alpha — expanded feature freeze

### FD1000 Freeze the complete expanded feature set

**Execution steps:** [FD1000](docs/roadmap/1.0-release.md#fd1000).

**Status: planned.** **Depends on:** FD404, FD503, FD601, FD701, FD813, FD915.

1. Reconcile all family/variant and integration acceptance, reusing existing results under the validation policy.
2. Finish missing original assets, recipes, guides, examples and usability work in their owning packages, including FD916's accepted visual baseline through FD915; do not rerun complete walkthroughs just to consolidate records.
3. Freeze the included capability list, named profiles/dependency identities, deliverable source/JAR and support limits in the qualification record. Preserve the original core baseline and all prior run identities.
4. Declare feature complete and move to stabilization. Additional scope needs an explicit revision; no agreed feature is silently deferred to make the gate pass.

**Done when:** all scope formerly proposed through 1.5 plus the adopted Fixed
Inverted, Dive, both Flying variants and Powered Inverted Yaw is implemented and
accepted within the agreed specifications. Discovery, a prototype or a staged addon alone
cannot close this gate. FD801 availability remains outside the mandatory gate.

## Version 1.0 beta and release candidate

FD901–FD905 qualify the expanded deliverable after FD1000. Their infrastructure,
measurements, focused tests and foundational fixes may proceed during development;
this scheduling does not defer known defects. Earlier accepted cases stay closed.

### FD901 Run the full compatibility matrix

**Execution steps:** [FD901](docs/roadmap/1.0-release.md#fd901).

**Status: in progress; historical pre-FD404 matrix observed.**

**Depends on:** FD1000 for final qualification. **Design:** D02, D04, D17.

Perform one release-level native sweep of the frozen supported profiles, retaining
base, Styles-only, Engineered-only and combined coverage and adding the agreed
expansion profiles. Credit an equivalent completed sweep after scoped review.
After fixes, rerun affected tests/profiles; repeat the full sweep only for a
justified cross-cutting change. Do not require this sweep for every task.
Retain the original five matching and twenty directed wrong-family cases; extend
native eligibility to each new vehicle/variant and verified provider through placement,
assembly, initial snap, re-engagement, dispatch, transition and save/load; locked
carriers cannot bypass family matching. Cover both directions of mixed-consist
coupling, ordinary bare-bearing fit/non-fit, cosmetic-only tracks and occupied
boundary braking. External functional tracks qualify only for the specifically
accepted FD811/FD801 capabilities; staging or cosmetic appearance grants nothing.

**Evidence / next action:** the [historical profile matrix](docs/evidence/0.9/profile-matrix-abc4a9a/README.md)
passed its 49-test inventory. Map accepted results and current tests to the
required entry paths, then run uncovered/affected checks or the outstanding
release sweep. Record actual test counts rather than treating “49” as current.

**Done when:** every advertised combination and rejection path has results for
the selected identity; ordinary carts acquire no 4D effects and a rejected
operation does not steal a binding or partially mutate the consist.

### FD902 Validate multiplayer and lifecycle recovery

**Execution steps:** [FD902](docs/roadmap/1.0-release.md#fd902).

**Status: in progress.** Historical stationary, moving, support-repair and
active-restart checkpoints exist; their [history](docs/ROADMAP-CHECKPOINT-HISTORY-2026-11-04.md#fd902-validate-multiplayer-and-lifecycle-recovery)
is not a full current-artifact pass.

**Depends on:** FD1000 for final qualification; affected lifecycle work starts earlier. **Design:** D14, D16, D17.

| Task / status | Required matrix and pass condition | Evidence / next action |
| --- | --- | --- |
| <a id="fd902a"></a> **FD902A Normal ride lifecycle — in progress** [Steps](docs/roadmap/1.0-release.md#fd902a) | Native lifecycle coverage for five families; two real clients in distinct seats on representative Arrow and PAX paths, with base/combined interaction coverage. Cover late join, stationary/moving reconnect, chunk interruption, cold restart, support loss, repair and realignment once per distinct path, not the full cross product; retain identities/locks and reject automatic restart or duplicate/lost riders | [Occupied lifecycle](docs/evidence/0.9/occupied-disconnect-reconnect.md), [ordered repair](docs/evidence/0.9/two-client-five-family-ordered-recovery.md). Map historical rows to current inputs, then close post-repair reconnect/restart and affected moving paths. |
| <a id="fd902b"></a> **FD902B Recovery lifecycle — original matrix complete; final freeze reconciliation pending** [Steps](docs/roadmap/1.0-release.md#fd902b) | FD402D–E matrix, including original linked trains and permanent versus temporary missing components | Consume the completed FD402D–E evidence: [one-player/linked-train persistence](docs/evidence/0.4/recovery/linked-train-fresh-process-656725bc-20261105/README.md), [two-client Arrow interruptions](docs/evidence/0.4/recovery/two-client-interruptions-20261111/README.md), and [combined four-car PAX permanent removals](docs/evidence/0.4/recovery/permanent-pax-interruptions-20261111/README.md), with accepted native family and cleanup coverage. FD1000 final qualification reconciles these same records against relevant changes; this handoff creates no new runtime case or replay. |
| <a id="fd902c"></a> **FD902C PAX phase interruptions — in progress** [Steps](docs/roadmap/1.0-release.md#fd902c) | LIFTING support loss for 4,800 ticks; separate OUTBOUND link-loss regression; Repair → Realign → Recover → occupied full cycle/unload. Cold restart during each active phase LIFTING/OUTBOUND/RETURNING/CAPTURING; preserve all four cars, eight carriers, links, controller/carriage and riders, and require explicit recovery | [Source success](docs/evidence/0.9/fd401-pax-longhold-source-229e586/acceptance.md), [packaged named-test pass](docs/evidence/0.9/fd401-pax-packaged-longhold-229e586/README.md), [historical OUTBOUND failure](docs/evidence/0.9/fd401-pax-longhold-client-blocker-1f13a0e/README.md), [bounded active restart](docs/evidence/0.9/create-telemetry/a1aeb1a-active-pax-restart/README.md). Reuse existing long-hold and recovery evidence after relevant-change review. Cover each distinct active-phase restart once; share native state/identity assertions with representative normal-client and two-client input/synchronization checks. No two-consecutive-cycle requirement. FD401D consumes telemetry from these same runs. |

**Done when:** these supported-case matrices have no unresolved rider-state,
restraint, identity, rebinding or unintended-resume failures. Document supported
operator recovery for each interruption. Do not claim every timing race from a
single successful restart. New families consume shared lifecycle evidence and add
only distinct motion, capture, coupling or route interruption cases from their
implementation packages; no new family × profile × interruption cross product.

### FD903 Validate save migration and provider changes

**Execution steps:** [FD903](docs/roadmap/1.0-release.md#fd903).

**Status: in progress.** Schema-1–5 loader regressions, a real historical schema-3
world, appearance removal/reinstall and bounded provenance backfill have evidence.

**Depends on:** FD1000 for final qualification; FD501A and the consuming FD501B–H packages own earlier migration implementation. **Design:** D04, D09, D10, D16.

On the selected JAR, retain the loader matrix and real historical schema-3 upgrade,
sustained Arrow/Legacy/Free Spin rides, two-client seat restoration, save/reopen,
backup restoration and appearance-provider removal/reinstall. Preserve all saved
rail/profile/chassis/carrier IDs. Intact legacy rails may gain exact provenance;
changed/foreign/missing rails must not acquire invented geometry. Missing functional
providers and unsupported conversions reject clearly. Include any schema/provider
migration deliberately selected and implemented in FD501A or the consuming adapter package; archived schema-7 work
is not presumed implemented or certified. Carry the accepted schema-6 results forward.

**Evidence / next action:** [migration procedure](docs/MIGRATION.md),
[historical world](docs/evidence/0.9/native-schema3-world-replay.md),
[provenance backfill](docs/evidence/0.9/create-telemetry/307ec35-legacy-provenance/README.md).
Complete sustained historical Legacy/Free Spin rides and two-client records;
record the exact backup and restored save hashes. Synthetic schema labels do not
stand in for a world produced by an older release.

**Done when:** declared supported upgrades preserve families/profiles and riders,
unsupported conversions refuse safely, and the documented backup recovery works.

### FD904 Meet measured performance and rendering targets

**Execution steps:** [FD904](docs/roadmap/1.0-release.md#fd904).

**Status: in progress.** Host-scoped addon timing limits exist; whole-frame/tick,
memory and sustained-park acceptance have not yet been measured.

**Depends on:** FD1000 for final qualification, FD006. FD904C1 has the early prerequisites below; FD904C2 reconciles later coverage. **Design:** D16, D17.

| Task / status | Required scenario and pass condition | Evidence / next action |
| --- | --- | --- |
| <a id="fd904a"></a> **FD904A Rendering — in progress** [Steps](docs/roadmap/1.0-release.md#fd904a) | Accepted original-family checks plus new mechanism-specific rider/body/camera coverage and the frozen advertised graphics profiles; nonempty `geometryStages` >=500, correct ownership and visible geometry, clean reload and shutdown; solver/frame/rider evidence retains its measured bounds | [Historical render evidence](docs/evidence/0.9/client-render-sampling.md) and [artifact reuse map](docs/CURRENT-QUALIFICATION.md). Accepted FAST/FABULOUS gates and FD401B visuals carry forward when their render inputs are unchanged; run only missing/changed paths. FD402B supplies recovery views; FD916 supplies the accepted model/texture finish without a second art review. |
| <a id="fd904b"></a> **FD904B Representative park — planned** [Steps](docs/roadmap/1.0-release.md#fd904b) | The fixed mixed-family scene, declared hardware/settings, frame/tick percentiles, heap/RSS/cgroup and GC measurements, frozen budgets and one combined-profile 30-minute sustained run defined in [PERFORMANCE.md](docs/PERFORMANCE.md#representative-park-acceptance-fd904b) | Implement/validate whole-frame and whole-server collectors; an initial baseline can close initial measured acceptance without a duplicate same-artifact run. Freeze budgets for later comparisons. Keep 120,000 ns/runtime invocation and 6,000 ns/carrier limits in all four native profiles. No existing average or llvmpipe capture proves GPU smoothness. |
| <a id="fd904c"></a> **FD904C Rider camera and comfort — planned; cumulative parent** [Steps](docs/roadmap/1.0-release.md#fd904c) | FD904C1 owns the independently closable 0.5 baseline; FD904C2 reconciles later family/effects coverage for final qualification | [D16](PLAN.md#d16-riders-and-synchronization). FD503 and FD912 depend only on FD904C1. Later cases do not reopen a completed baseline or require a second camera implementation. |

**Done when:** FD904A–C pass on stated configurations, the performance budget is
recorded before candidate comparison, and no unresolved regression prevents the
declared benchmark scene. Capacity ceilings are storage/admission limits, not a
promise that every maximum-sized world meets the benchmark timings.

### FD904C1 Deliver the baseline rider camera and comfort option

**Execution steps:** [FD904C1](docs/roadmap/0.5-contracts.md#fd904c1).

**Status: complete (2026-11-15 UTC).** [Baseline evidence](docs/evidence/0.5/comfort/README.md). **Depends on:** FD404 and integrated FD1101A seat/pose
contracts, including its occupied client proof. **Delivery:** 0.5.

Deliver D16's selectable comfort option without changing server seat motion.
Cover the original families and Wing's fixed seats, relevant upright/banked/inverted
views, mouse look, arms/bodies, reconnect and dismount restoration. Reuse accepted
normal-camera and shared synchronization evidence; test the new comfort behavior
and genuinely affected paths. Wing's initial handoff does not depend on comfort.

**Done when:** this finite baseline works and its cases have evidence. Close
FD904C1 for those cases and satisfy the camera prerequisite of FD503/FD912. Added
families and optional effects have their own cases; they do not keep C1 partially
open. Only a named regression in a baseline case can reopen that case.

### FD904C2 Reconcile expanded rider camera coverage

**Execution steps:** [FD904C2](docs/roadmap/1.0-release.md#fd904c2).

**Status: planned.** **Depends on:** FD1000, FD904C1, FD912.

Each later family's player-acceptance package supplies its distinct pose/comfort
cases during development; FD912 supplies optional-effects ownership and restoration.
At final qualification, link that coverage to the frozen family/variant/profile
list and resolve only identified missing or affected cases. This is reconciliation,
not a second implementation, a new all-family run or a prerequisite for FD503.

**Done when:** every declared camera capability is covered by C1, family and
integration evidence, with no unresolved required-criterion failure. Close the
FD904C parent from C1 and C2 without replaying either one's accepted cases.

### FD905 Release candidate gate

**Execution steps:** [FD905](docs/roadmap/1.0-release.md#fd905).

**Status: planned; awaiting FD901–FD904 and all earlier release gates.**

**Depends on:** FD1000, FD901–FD904.

Prepare the candidate artifact, exact profile matrix, reproducible scenarios,
defect list and support limits for review. Publication remains a separate action.

**Done when:** all named gates pass; crashes, save corruption, rider-state failures
and eligibility bypasses affecting supported cases are resolved; affected cases
are retested after fixes. Apply the [blocker classification](docs/VALIDATION-POLICY.md#gate-blockers-and-follow-up-work):
name the violated required criterion for each blocking defect. Nonblocking polish
is recorded separately and does not hold this gate open. Every accepted claim
points to direct evidence or a
brief scoped carry-forward review against the frozen artifact. Do not demand a
complete input-hash inventory for unaffected claims or relabel historical runs.

## Version 1.0 stable

### FD1001 Finalize the release documentation

**Execution steps:** [FD1001](docs/roadmap/1.0-release.md#fd1001).

**Status: planned.**

**Depends on:** FD905.

Finalize installation instructions, version/dependency matrix, all included family/variant guides, recovery limits, attribution, integration terms, known issues, and changelog. Label inspired approximations honestly.

**Done when:** the documentation matches the release artifact and does not advertise unverified towing, historical fidelity, or future Track Styles support.

### FD1002 Reproduce and verify release artifacts

**Execution steps:** [FD1002](docs/roadmap/1.0-release.md#fd1002).

**Status: planned.**

**Depends on:** FD905, FD1001.

Build from the intended release revision, record checksums, verify packaged metadata/assets, and smoke-test clean client and dedicated-server installs using the documented dependencies.

**Done when:** the artifact is reproducible, contains only intended distributable content, and clean client/server installs work with the documented dependencies. Reuse the accepted family/variant rides and restrictions when relevant packaged inputs and install context match; use a representative clean-install ride to check packaging, expanding only for a family-specific packaging risk. Do not repeat survival construction or the full qualification matrix here.

### FD1003 Version 1.0 release gate

**Execution steps:** [FD1003](docs/roadmap/1.0-release.md#fd1003).

**Status: planned.**

**Depends on:** FD1001, FD1002 and all required earlier gates.

Review the finished release against the definition below. Publishing is a future execution action, not authorized or performed by this planning document.

**Done when:** the release checklist and evidence are complete; when publication is separately requested, the published artifacts and release notes match the verified build.

Stable 1.0 includes the original five families plus fixed-seat Wing, agreed
Spinning variants, Suspended, Axis, Pipeline, Roller Ball, Fixed Inverted, Dive,
Conventional Flying, New-generation Flying with scenery-facing yaw, and Powered
Inverted Yaw, with their original
vehicles and required track contracts. It includes the agreed operations,
presentation, optional-seat and coexistence capabilities through FD915, including
FD916's complete model-and-texture review. Strict
eligibility, correct riders/camera, persistent multiplayer state, usable content,
supported migrations and measured performance remain release requirements.
Optional addons stay optional for standalone rides. Ordinary-cart access and
towing retain their separately proven bounds; FD801 availability does not block release.
Required earlier gates include these vehicle outcomes through FD601/FD701/FD813
and their visual finish through FD916. FD1602–FD1605 builder tools, FD1701B/C and
FD1703 multi-train operations, and FD2001–FD2007 moving-track mechanisms remain
post-1.0 and are not prerequisites for this release.

### Supported envelope and explicit 1.0 dispositions

These are release-planning decisions, not new runtime acceptance. FD1001 must
publish them consistently; FD1002 must verify the boundaries on the release JAR.

| Requirement | 1.0 disposition | Acceptance owner |
| --- | --- | --- |
| Horizontal world coordinates | Accept the existing ±1024-block x/z supported region for 1.0; arbitrary-distance operation is deferred, not silently promised | FD901 verifies placement/operation boundaries and safe refusal outside; FD1001 documents them. Raising the limit requires a separate native precision proof. |
| Dimension capacities | Retain 256 sections, 64 vehicles, 8192 total profile keys and 512 magnetic spans, at most eight spans per section | FD901 verifies exact-limit/one-over-limit admission and reload behavior; FD903 verifies no partial mutation or silent save truncation. FD904B separately bounds tested workload performance. |
| Recovery envelope | Accept only the FD402 declared bearing, family/consist/route, drive, equipment and provider set | FD402A–F certify it; unmeasured grades, assemblies, compact-spike pulls and cosmetic overrides stay unsupported. |
| Stabilized comfort camera | Deliver before 1.0 as already specified in D16; not implicitly deferred | FD904C1 delivers the 0.5 baseline; FD904C2 consumes later coverage. An unmet required camera criterion blocks 1.0, without reopening unrelated accepted cases. |
| Expanded families/variants, required tracks, supported upgrades and named profiles | Preserve original five-family acceptance and deliver every adopted vehicle/cart style, including Fixed Inverted, Dive, both Flying variants and Powered Inverted Yaw | FD503/FD601/FD701/FD813/FD915 feed FD1000, then FD905; optional addons remain optional for standalone use. Moving-track mechanisms are post-1.0. |

## External compatibility work

### FD801 Integrate a released Track Styles Arrow 4D

**Execution steps:** [FD801](docs/roadmap/external-compatibility.md#fd801).

**Status: planned; awaiting a qualifying inspectable external release.**

**Depends on:** FD401 and an inspectable external release with applicable terms established. **Design:** D02, D04, D05.

Inspect the actual representation and implement an independently authored optional adapter. A cosmetic-only style must decorate matching functional track or undergo an explicit functional conversion; it cannot make ordinary track eligible by appearance alone.

**Done when:** our Arrow vehicles correctly use the verified equivalent functional track, ordinary fitting carts receive no 4D effects, control rails have one render owner, profiles persist, and same-family provider transitions pass multiplayer/lifecycle tests. Record supported versions. If the external release is unavailable, keep this package pending without blocking 1.0.

## New-family discovery and delivery packages

All six discovery IDs are retained and scheduled during 0.5 under the adopted
plan. Reference inspection and asset concepts can start while 0.4 finishes.
The original discovery ID closes with a dimensioned specification, explicit
mechanics/seat/track contract, chosen scope and finite acceptance plan. It does
not claim an implemented vehicle. The suffix packages below own implementation,
original content and cumulative acceptance; all are **planned** initially.

Retain confirmed references versus inspired game choices, running/guide/upstop
contacts, dimensions, seats/restraints, pivots, mass/loading, coupling, persistence,
pose/render/camera ownership and eligibility. Preserve stable existing identities.
Any external track needs its own verified adapter. Native checks cover mechanics,
clearance and failure injection; client checks cover real input, visible motion,
riders and the player example. Use shared lifecycle evidence plus distinct cases,
not a mandatory new full matrix for each family. The source references below are
discovery inputs, not newly verified engineering claims.

For each A–C implementation chain, A owns mechanics and necessary shared changes,
including the occupied client proof below. B owns original assets plus recipes/guide/
example, and C closes the complete player workflow by combining valid evidence.
B may begin after specification/dimensions are stable; C waits for A and B.
Partial results stay accepted within their scope.
Apply [FD916's visual checklist](#fd916-review-and-improve-models-and-textures)
during B content work and retain useful captures. B can close independently;
FD916 later reconciles the complete original/new asset set and remaining fixes.
Every 1.0 family/variant also receives applicable existing editing/copy/mirror
controls, loading/tuning settings, clearance previews and readable readiness
reasons. Document mechanical differences rather than inventing unsupported
controls. Each B package supplies a buildable example and compatible settings;
include gentle/intense presets where the mechanism supports them. The reusable
save/share preset library is separately scoped to FD1602 after 1.0.

### Shared-contract handoff

Before closing a mechanics task that supplies a shared interface, demonstrate one
representative normal occupied boarding → motion → capture/stop → unload sequence
through a connected client. Check seat/restraint ownership, rider/body/camera pose
and restoration; observe the actual mechanism's changed behavior. When a new or
changed synchronization path is involved, use a second connected observer for that
path; otherwise reuse accepted representative remote-rider evidence.

This moves the already-required player check earlier. Use the functional geometry,
seat positions and restraints intended by the specification; decorative texture/
livery polish may remain unfinished. Record the consumed contract and proof in
the mechanics task, integrate it into `main`, then allow dependent mechanics to
start. The later C task cites this proof and adds only uncovered construction,
content, interruption or distinct pose cases. A later asset change invalidates
only the claims whose geometry or behavior it changes. Existing closed mechanics
packages need no retrospective handoff replay.

### FD1101 Fixed-seat Wing coaster discovery

**Execution steps:** [FD1101](docs/roadmap/0.5-contracts.md#fd1101).

**Status: specification complete (2026-11-14 America/Phoenix).** [Dimensioned contract and fit matrix](docs/design/0.5/FD1101-wing.md). **Discovery:** 0.5. **Delivery:** 0.5.
**Discovery inputs / decisions:** existing controlled-car geometry; normal-track fit and fixed-seat specification.
**Depends on:** no release gate for reference/design work. Native feasibility checks use the integrated core and declared run prerequisites.

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
  during discovery; no claim of universal normal-track fit is made now.

Completion evidence for A–C: [Wing acceptance](docs/evidence/0.5/wing/README.md), reconciled 2026-11-15 UTC.

| Delivery task / status | Prerequisites | Steps and completion condition |
| --- | --- | --- |
| <a id="fd1101a"></a> **FD1101A Mechanics — complete** [Steps](docs/roadmap/0.5-contracts.md#fd1101a) | FD404, FD501A, FD1101 | Extend fixed-seat eligibility and pose/seat contracts through the actual Wing vehicle; implement normal-track fit, restraints, coupling, placement and ordinary station handling. No rotary mechanism or Arrow profile is inherited. Complete when its specified mechanism, affected native invariants and the shared-contract client handoff pass. |
| <a id="fd1101b"></a> **FD1101B Original content — complete** [Steps](docs/roadmap/0.5-contracts.md#fd1101b) | FD1101 specification and stable A dimensions | Finish original vehicle/required track assets, restraints, item/recipe content, guide and a playable example. Complete when content represents the implemented behavior and normal player controls. |
| <a id="fd1101c"></a> **FD1101C Player acceptance — complete** [Steps](docs/roadmap/0.5-contracts.md#fd1101c) | FD1101A, FD1101B | Complete normal construction/boarding/operation/capture/unload for the declared example; combine accepted mechanics, eligibility, clearance, persistence and rider-synchronization evidence. Add only distinct interruption or camera cases. Complete when the whole feature is usable and documented; do not restart acceptance for an unrelated build. |

### FD1501 Spinning coaster variants discovery

**Execution steps:** [FD1501](docs/roadmap/0.5-contracts.md#fd1501).

**Status: specification complete (2026-11-14 America/Phoenix).** [Specification and prototype plan](docs/design/0.5/FD1501-spinning.md). **Discovery:** 0.5. **Delivery:** 0.6 S1; 0.8 S2.
**Discovery inputs / decisions:** dimensioned S1/S2 motion, seating, track and propulsion specifications; the two-variant list and order below are adopted.
**Depends on:** no release gate for reference/design work. Native feasibility checks use the integrated core and declared run prerequisites.

- **Motion/seating:** specify each adopted variant's spin axis, passenger layout,
  loading response, range, damping and capture. FD1501A–C deliver S1; FD1501D–F
  deliver S2. Additional variants require a separate scope addition, not a new
  condition appended after either passes.
- **Track/capture:** decide ordinary-track fit versus a dedicated family by measured
  contacts and swept clearance. Define orientation-independent stopping versus
  controlled station capture, restraint interlocks and lift/launch interaction.
- **Models and risks:** original spinning chassis/carrier, seats, bearings, restraints,
  skins and track only where required. Prototype asymmetric loading, uncontrolled
  spin limits, coupling and safe capture through rollback/power loss.
- **Done when / next action:** publish a variant matrix, per-variant physics and
  seating contracts, asset list and independent acceptance plan. A shared cosmetic
  body must not hide different dynamics or expand compatibility without proof.

| Adopted 1.0 variant | Finite scope | Delivery |
| --- | --- | --- |
| S1 Conventional free-spinning | One specified passenger/carrier arrangement rotating about the vehicle's local upright axis; load-sensitive free motion, damping, clearance and controlled station capture. | 0.6 / FD1501A–C |
| S2 Launched/inverting free-spinning | One specified vehicle configuration with free rotation through inversions, launch interaction, magnetic spin influence and station capture. Use [MACK Xtreme Spinning](https://www.mack-rides.com/products/xtreme-spinning-coaster) as a reference, with original assets. | 0.8 / FD1501D–F |

During 0.5 name S2's working longitudinal propulsion provider and supported
configuration, including a functioning standalone path; an unavailable optional
adapter is not an implementation. FD812 adds an optional propulsion alternative,
not a universal S2 prerequisite. Keep spin-axis torque separate from travel.
Deliberately programmed scenery-facing yaw belongs to FD1802A/FD1901, not either
free-spinning variant. Do not silently promote it into the 1.0 gate.

| Delivery task / status | Prerequisites | Steps and completion condition |
| --- | --- | --- |
| <a id="fd1501a"></a> **FD1501A Mechanics — complete (2026-11-15 UTC)** [Steps](docs/roadmap/0.6-motion.md#fd1501a) | FD501A, FD1101A, FD1501 | Implement S1 conventional free-spinning and the carrier-axis/loading/damping/capture support it actually needs. Preserve existing carrier behavior; prove new-axis motion through native occupied cases. Complete when its specified mechanism, affected native invariants and the shared-contract client handoff pass. [Accepted native and occupied handoff](docs/evidence/0.6/spinning/README.md). |
| <a id="fd1501b"></a> **FD1501B Original content — complete (2026-11-15 UTC)** [Steps](docs/roadmap/0.6-motion.md#fd1501b) | FD1501 specification and stable A dimensions | Finish original vehicle/required track assets, restraints, item/recipe content, guide and a playable example. Complete when content represents the implemented behavior and normal player controls. |
| <a id="fd1501c"></a> **FD1501C Player acceptance — complete (2026-11-15 UTC)** [Steps](docs/roadmap/0.6-motion.md#fd1501c) | FD1501A, FD1501B | Complete normal construction/boarding/operation/capture/unload for the declared example; combine accepted mechanics, eligibility, clearance, persistence and rider-synchronization evidence. Add only distinct interruption or camera cases. Complete when the whole feature is usable and documented; do not restart acceptance for an unrelated build. [Accepted construction/content/recovery](docs/evidence/0.6/spinning/player/README.md). |
| <a id="fd1501d"></a> **FD1501D Remaining variant mechanics — planned** [Steps](docs/roadmap/0.8-operations.md#fd1501d) | FD501A, FD1501, FD1501A | Implement S2 launched/inverting free-spinning, launch interaction and magnetic influence, sharing code only where mechanics match. Complete when its distinct motion/capture contract passes affected native checks and any new shared-contract client handoff; reuse identical paths and keep S1 accepted. |
| <a id="fd1501e"></a> **FD1501E Remaining variant content — planned** [Steps](docs/roadmap/0.8-operations.md#fd1501e) | FD1501 specification and stable D dimensions | Finish S2 original assets, recipes, guide and example. Complete when this variant is usable through its intended controls. |
| <a id="fd1501f"></a> **FD1501F Remaining variant acceptance — planned** [Steps](docs/roadmap/0.8-operations.md#fd1501f) | FD1501D, FD1501E | Close S2's player workflow and distinct lifecycle/pose cases using cumulative evidence. Complete when the adopted S1/S2 list is covered; do not add variants after each pass. |

### FD1301 Arrow-style Suspended coaster discovery

**Execution steps:** [FD1301](docs/roadmap/0.5-contracts.md#fd1301).

**Status: specification complete (2026-11-14 America/Phoenix).** [Specification and prototype plan](docs/design/0.5/FD1301-suspended.md). **Discovery:** 0.5. **Delivery:** 0.6.
**Discovery inputs / decisions:** overhead-track and dimensioned swing/lock specification.
**Depends on:** no release gate for reference/design work. Native feasibility checks use the integrated core and declared run prerequisites.

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

| Delivery task / status | Prerequisites | Steps and completion condition |
| --- | --- | --- |
| <a id="fd1301a"></a> **FD1301A Mechanics — planned** [Steps](docs/roadmap/0.6-motion.md#fd1301a) | FD501A, FD1501A, FD1301 | Implement the overhead bogie/hanger, bounded lateral swing, loading/damping and safe lift/station locks. Reuse integrated carrier infrastructure; do not substitute a cabin animation for dynamics. Complete when its specified mechanism, affected native invariants and the shared-contract client handoff pass. |
| <a id="fd1301b"></a> **FD1301B Original content — planned** [Steps](docs/roadmap/0.6-motion.md#fd1301b) | FD1301 specification and stable A dimensions | Finish original vehicle/required track assets, restraints, item/recipe content, guide and a playable example. Complete when content represents the implemented behavior and normal player controls. |
| <a id="fd1301c"></a> **FD1301C Player acceptance — planned** [Steps](docs/roadmap/0.6-motion.md#fd1301c) | FD1301A, FD1301B | Complete normal construction/boarding/operation/capture/unload for the declared example; combine accepted mechanics, eligibility, clearance, persistence and rider-synchronization evidence. Add only distinct interruption or camera cases. Complete when the whole feature is usable and documented; do not restart acceptance for an unrelated build. |

### FD1302 S&S-style Axis coaster discovery

**Execution steps:** [FD1302](docs/roadmap/0.5-contracts.md#fd1302).

**Status: specification complete (2026-11-14 America/Phoenix).** [Specification and prototype plan](docs/design/0.5/FD1302-axis.md). **Discovery:** 0.5. **Delivery:** 0.7.
**Discovery inputs / decisions:** unambiguous rotation-axis/mode and track/seat specification.
**Depends on:** no release gate for reference/design work. Native feasibility checks use the integrated core and declared run prerequisites.

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

| Delivery task / status | Prerequisites | Steps and completion condition |
| --- | --- | --- |
| <a id="fd1302a"></a> **FD1302A Mechanics — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1302a) | FD501A, FD1501A, FD1301A, FD1302 | Implement the selected Axis rotation/modes, off-axis inertia, propulsion ownership, capture and clearance, using the established motion infrastructure without equating Axis with Suspended. Complete when its specified mechanism, affected native invariants and the shared-contract client handoff pass. |
| <a id="fd1302b"></a> **FD1302B Original content — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1302b) | FD1302 specification and stable A dimensions | Finish original vehicle/required track assets, restraints, item/recipe content, guide and a playable example. Complete when content represents the implemented behavior and normal player controls. |
| <a id="fd1302c"></a> **FD1302C Player acceptance — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1302c) | FD1302A, FD1302B | Complete normal construction/boarding/operation/capture/unload for the declared example; combine accepted mechanics, eligibility, clearance, persistence and rider-synchronization evidence. Add only distinct interruption or camera cases. Complete when the whole feature is usable and documented; do not restart acceptance for an unrelated build. |

### FD1502 Roller Ball coaster discovery

**Execution steps:** [FD1502](docs/roadmap/0.5-contracts.md#fd1502).

**Status: specification complete (2026-11-14 America/Phoenix).** [Specification and prototype plan](docs/design/0.5/FD1502-roller-ball.md). **Discovery:** 0.5. **Delivery:** 0.7.
**Discovery inputs / decisions:** distinct route/vehicle/capture specification and native feasibility review.
**Depends on:** no release gate for reference/design work. Native feasibility checks use the integrated core and declared run prerequisites.

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

| Delivery task / status | Prerequisites | Steps and completion condition |
| --- | --- | --- |
| <a id="fd1502a"></a> **FD1502A Mechanics — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1502a) | FD501A, FD1302A, FD1502 | Implement the distinct Roller Ball route/vehicle, passenger mechanism, lift/return transitions, retention and capture. Preserve its own dynamics and route model; use integrated shared changes from Axis first. Complete when its specified mechanism, affected native invariants and the shared-contract client handoff pass. |
| <a id="fd1502b"></a> **FD1502B Original content — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1502b) | FD1502 specification and stable A dimensions | Finish original vehicle/required track assets, restraints, item/recipe content, guide and a playable example. Complete when content represents the implemented behavior and normal player controls. |
| <a id="fd1502c"></a> **FD1502C Player acceptance — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1502c) | FD1502A, FD1502B | Complete normal construction/boarding/operation/capture/unload for the declared example; combine accepted mechanics, eligibility, clearance, persistence and rider-synchronization evidence. Add only distinct interruption or camera cases. Complete when the whole feature is usable and documented; do not restart acceptance for an unrelated build. |

### FD1401 Pipeline coaster discovery

**Execution steps:** [FD1401](docs/roadmap/0.5-contracts.md#fd1401).

**Status: specification complete (2026-11-14 America/Phoenix).** [Specification and prototype plan](docs/design/0.5/FD1401-pipeline.md). **Discovery:** 0.5. **Delivery:** 0.8.
**Discovery inputs / decisions:** explicit Pipeline reference selection and track/seat specification.
**Depends on:** no release gate for reference/design work. Native feasibility checks use the integrated core and declared run prerequisites.

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

| Delivery task / status | Prerequisites | Steps and completion condition |
| --- | --- | --- |
| <a id="fd1401a"></a> **FD1401A Mechanics — planned** [Steps](docs/roadmap/0.8-operations.md#fd1401a) | FD404, FD501A, FD1101A, FD1401 | Implement the selected Pipeline contact/track geometry, chassis, seating or posture, restraints, propulsion/braking and coupling. Only its own mechanism prerequisites apply; no Axis dependency. Complete when its specified mechanism, affected native invariants and the shared-contract client handoff pass. |
| <a id="fd1401b"></a> **FD1401B Original content — planned** [Steps](docs/roadmap/0.8-operations.md#fd1401b) | FD1401 specification and stable A dimensions | Finish original vehicle/required track assets, restraints, item/recipe content, guide and a playable example. Complete when content represents the implemented behavior and normal player controls. |
| <a id="fd1401c"></a> **FD1401C Player acceptance — planned** [Steps](docs/roadmap/0.8-operations.md#fd1401c) | FD1401A, FD1401B | Complete normal construction/boarding/operation/capture/unload for the declared example; combine accepted mechanics, eligibility, clearance, persistence and rider-synchronization evidence. Add only distinct interruption or camera cases. Complete when the whole feature is usable and documented; do not restart acceptance for an unrelated build. |

## Additional vehicle and supporting-feature packages

Allocation clarified **2026-11-11**: all vehicle/cart styles below deliver before
1.0; standalone builder tools and multi-train management retain 1.1/1.2, while
moving-track mechanisms remain post-1.0. Permanent FD IDs do not change with
this correction. All packages remain planned, not current runtime capabilities.
Design boundaries are recorded in
[PLAN D19](PLAN.md#d19-vehicle-variants-tools-and-scenery-facing-yaw) and
[PLAN D20](PLAN.md#d20-post-10-moving-track-mechanisms).

The following 0.5 specification packages settle scope before implementation.
Reference inspection can start during 0.4; a necessary native prototype uses
integrated core prerequisites and is retained. Specification acceptance is not
vehicle completion, and FD503 does not wait for the later A–C implementations.

| Package / status | Prerequisites | Completion condition |
| --- | --- | --- |
| <a id="fd1601s"></a> **FD1601S Fixed Inverted specification — complete** [Specification](docs/design/0.5/FD1601S-fixed-inverted.md) [Steps](docs/roadmap/0.5-contracts.md#fd1601s) | No release gate for reference/design work | Define one measured overhead fixed-seat configuration, running gear, restraints, track fit, coupling, clearance and finite acceptance. |
| <a id="fd1702s"></a> **FD1702S Dive specification — complete** [Specification](docs/design/0.5/FD1702S-dive.md) [Steps](docs/roadmap/0.5-contracts.md#fd1702s) | No release gate for reference/design work | Define one fixed-seat configuration, approach/hold/release and interruption states, control/propulsion ownership, track fit and finite acceptance. |
| <a id="fd1801s"></a> **FD1801S Conventional Flying specification — complete** [Steps](docs/roadmap/0.5-contracts.md#fd1801s) | No release gate for reference/design work | Define seated boarding, station-based prone changeover, locks, posture/clearance transforms, recovery and finite acceptance. |
| <a id="fd1802s"></a> **FD1802S New-generation Flying specification — complete** [Steps](docs/roadmap/0.5-contracts.md#fd1802s) | No release gate for reference/design work | Define seated/sideways and prone sections, route-based transitions, physical yaw frame/pivot, target-facing in both postures, shared contract and finite acceptance. |
| <a id="fd1901s"></a> **FD1901S Powered Inverted Yaw specification — complete** [Steps](docs/roadmap/0.5-contracts.md#fd1901s) | No release gate for reference/design work | Define one upright overhead configuration, per-car yaw, powered travel/dwell/reverse, standalone provider path, scene events and finite acceptance. |

Apply the existing A–C ownership pattern: A supplies specified mechanics and the
[shared-contract handoff](#shared-contract-handoff), B supplies original models,
textures, items/recipes, player controls, guide and a buildable example, and C
closes the whole workflow from cumulative evidence. A specifies finite dimensions,
contacts, coupling, limits and supported profiles before final B geometry. B can
proceed once those dimensions are stable; C consumes A and B. These 1.0 vehicle
assets feed FD916; apply its checklist during content work and reuse accepted
views instead of re-reviewing unchanged art.
Shared-contract proof may close A while decorative work continues.

Vehicle group gates reconcile complete outcomes, then feed the 0.6–0.8 gates,
FD916 and FD1000. They do not order packaging or full release sweeps; FD901–FD905
and FD1001–FD1003 retain final 1.0 qualification. Later supporting-feature release
gates freeze their own finite lists and check only outstanding/affected packaging,
documentation and representative performance. Completed equivalent checks count.
Publication remains separate. Do not turn these additions into fresh 0.4 replay.

### Milestone 0.6 Fixed Inverted and version 1.1 builder tools

Deliver Fixed Inverted in 0.6; the three independent tools remain in 1.1. The vehicle is seated beneath
the track with fixed carrier orientation, using its own measured geometry and
eligibility; it is distinct from free-swinging Suspended. See the
[B&M Inverted reference](https://www.bolliger-mabillard.com/inverted-coaster).
Tools build on existing editing and telemetry rather than replacing accepted
systems. No arbitrary texture upload, scripting language or park-wide controller
is included. Original selectable colors/liveries are separate from visual-quality
acceptance; exact park-branded replicas are not a requirement.

| Package / status | Actual prerequisites | Ordered work and completion condition |
| --- | --- | --- |
| <a id="fd1601"></a> **FD1601 Fixed Inverted outcome — planned** [Steps](docs/roadmap/0.6-motion.md#fd1601) | FD1601C | Group the A–C outcome below; close from its accepted workflow without an additional run. |
| <a id="fd1601a"></a> **FD1601A Fixed Inverted mechanics — planned** [Steps](docs/roadmap/0.6-motion.md#fd1601a) | FD1601S, FD1101A, FD1301A | Implement the specified overhead fixed-seat configuration, restraints, track fit, consist limits and clearance. Complete construction/operation/capture, distinct recovery and the occupied handoff with correct rider/camera ownership. |
| <a id="fd1601b"></a> **FD1601B Fixed Inverted content — planned** [Steps](docs/roadmap/0.6-motion.md#fd1601b) | Stable FD1601A specification/dimensions | Deliver the original content, normal controls and playable example under the common B checklist. |
| <a id="fd1601c"></a> **FD1601C Fixed Inverted acceptance — planned** [Steps](docs/roadmap/0.6-motion.md#fd1601c) | FD1601A, FD1601B | Close construction, boarding, riding, stopping/unloading and distinct interruption/persistence cases using cumulative evidence. |
| <a id="fd1602"></a> **FD1602 Shareable setting presets — planned** [Steps](docs/roadmap/1.1-tools.md#fd1602) | FD105, FD203; integrated consumer setting contracts | Save, name, import/export and apply bounded motion/tuning settings. Check family/variant, format version, permissions, numeric bounds and edit state. World-specific bindings require explicit rebinding; incompatible imports leave existing state intact. Complete when presets round-trip and normal player application/rejection works. No blueprint/world-copy system is added. |
| <a id="fd1603"></a> **FD1603 Colors and original liveries — planned** [Steps](docs/roadmap/1.1-tools.md#fd1603) | Stable consumer material/appearance interfaces; FD910 only for its optional adapter | Provide a finite selectable palette/livery set, preview, persistence and fallback on a missing optional appearance provider. Preserve mechanical identity and seat ownership. Complete when declared original vehicles expose usable choices and affected visuals pass the existing checklist. No optional addon is required for original choices. |
| <a id="fd1604"></a> **FD1604 Unified operator panel — planned** [Steps](docs/roadmap/1.1-tools.md#fd1604) | FD401D, FD106; integrated control/readiness contracts | Present whole-consist occupancy, restraints, power, faults, recovery and actionable dispatch-denial reasons in one player interface. Commands use the server's existing authority, permission and revision checks; display unavailable optional controls accurately. Complete when representative normal input and distinct controller paths agree with authoritative state. |
| <a id="fd1605"></a> **FD1605 Version 1.1 builder-tools gate — planned** [Steps](docs/roadmap/1.1-tools.md#fd1605) | FD1602, FD1603, FD1604 | Reconcile the three tool outcomes and declared profiles, content, documentation and affected package checks. Fixed Inverted already feeds FD601/1.0 and is not held open for these tools. |

### Milestone 0.8 authority and Dive; version 1.2 multi-train operations

FD1701A authority and Dive deliver in 0.8. FD1701B/C multi-train management and
FD1703 remain 1.2; neither is a Dive or Powered Inverted prerequisite.
Limit the first multi-train system to one fixed-route circuit with a declared
train count and supported homogeneous consist configurations. Audit integrated
upstream capabilities before adding adapters; give occupancy, reservations,
braking and dispatch explicit owners. Shared-track networks, arbitrary junction
routing, automatic depots and mixed-family consists remain outside this release.
Dive adds one fixed-seat configuration and a defined approach, hold and release
sequence, as illustrated by the [B&M Dive reference](https://www.bolliger-mabillard.com/dive-coaster).
It consumes the hold authority without waiting for unrelated multi-train cases.

| Package / status | Actual prerequisites | Ordered work and completion condition |
| --- | --- | --- |
| <a id="fd1701"></a> **FD1701 Ride-local operations outcome — planned** [Steps](docs/roadmap/1.2-multi-train.md#fd1701) | FD1701C | Group the authority, implementation and acceptance below; no additional run is required to close the parent. |
| <a id="fd1701a"></a> **FD1701A Ride-local authority contract — planned** [Steps](docs/roadmap/0.8-operations.md#fd1701a) | Integrated FD106 control/stop interfaces; FD810, FD811 or FD812 only for the adapter actually consumed | Audit provider behavior, select ownership and expose validated hold/release/dispatch requests through the existing controller. Prove fail-closed authority transfer and one representative occupied handoff. Name finite supported profiles and missing native hooks before dependent work. |
| <a id="fd1701b"></a> **FD1701B Block operation and example — planned** [Steps](docs/roadmap/1.2-multi-train.md#fd1701b) | FD1701A | Implement whole-consist block occupancy, route reservations, train admission and station dispatch interlocks for the declared circuit/count. Preserve identities and occupancy across removal, unload, interruption and restart; unresolved occupancy must not grant entry. Supply normal controls, diagnostics, guide and a multi-train example. |
| <a id="fd1701c"></a> **FD1701C Ride-local operations acceptance — planned** [Steps](docs/roadmap/1.2-multi-train.md#fd1701c) | FD1701B | Close normal separation/dispatch and distinct interruption/restart cases at the declared train limit. Combine native reservation evidence with occupied interaction; no park-wide matrix is required. |
| <a id="fd1702"></a> **FD1702 Dive outcome — planned** [Steps](docs/roadmap/0.8-operations.md#fd1702) | FD1702C | Group the A–C vehicle outcome; close from accepted evidence without another walkthrough. |
| <a id="fd1702a"></a> **FD1702A Dive mechanics — planned** [Steps](docs/roadmap/0.8-operations.md#fd1702a) | FD1702S, FD1101A, FD1701A | Implement the specified fixed-seat vehicle, measured track fit, clearance, approach, timed hold, validated release, stopping/recovery and restraint interlocks through the established authority; include occupied handoff. |
| <a id="fd1702b"></a> **FD1702B Dive content — planned** [Steps](docs/roadmap/0.8-operations.md#fd1702b) | Stable FD1702A specification/dimensions | Deliver original vehicle/equipment assets, recipes, controls, guide and a hold-and-drop example. |
| <a id="fd1702c"></a> **FD1702C Dive acceptance — planned** [Steps](docs/roadmap/0.8-operations.md#fd1702c) | FD1702A, FD1702B | Close normal player operation and distinct interrupted-hold/release cases with cumulative evidence. |
| <a id="fd1703"></a> **FD1703 Version 1.2 multi-train gate — planned** [Steps](docs/roadmap/1.2-multi-train.md#fd1703) | FD1701 | Reconcile the declared multi-train outcome and applicable post-1.0 release checks. Dive is already consumed by FD813/1.0; do not replay it for this gate. |

### Milestone 0.7 — Conventional and New-generation Flying

Both variants are required for 0.7 and stable 1.0. Conventional Flying uses seated boarding,
station-based changeover to prone flight, fixed passenger orientation relative
to the chassis during travel, and return to seated unloading. New-generation
Flying adds seated/sideways presentation sections, defined route-based transitions
to/from prone flight and independently controlled yaw, including scenery targeting
in both seated and prone postures. Vekoma's [Flying reference](https://www.vekoma.com/flying-coasters)
supports the seated/sideways-to-flying inspiration; our scenery-facing yaw is an
explicit game-design requirement, not a claim of exact commercial mechanics.
These posture transitions use fixed track geometry and moving vehicle joints;
they do not require a moving rail segment or any FD2001–FD2007 mechanism.

Build the shared yaw contract inside the concrete New-generation vehicle work.
It belongs to FD1802A, not a speculative framework or a later vehicle prerequisite
for Flying. FD1901A may consume it as soon as that handoff is integrated into
`main`; it need not wait for all Flying art or FD1803's aggregate gate.

| Package / status | Actual prerequisites | Ordered work and completion condition |
| --- | --- | --- |
| <a id="fd1801"></a> **FD1801 Conventional Flying outcome — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1801) | FD1801C | Close this variant from A–C evidence independently of the New-generation variant. |
| <a id="fd1801a"></a> **FD1801A Conventional Flying mechanics — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1801a) | FD1801S, FD1601A; integrated seat/restraint/camera contracts | Implement the specified seated/prone transforms, pivot, station changeover, locks, running gear and clearance. Complete changeover/dispatch/unload and interrupted-transition recovery; prove occupied prone motion and camera restoration. |
| <a id="fd1801b"></a> **FD1801B Conventional Flying content — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1801b) | Stable FD1801A specification/dimensions | Deliver original vehicle/restraint/station assets, recipes, controls, guide and example with the required pose-specific visual checks. |
| <a id="fd1801c"></a> **FD1801C Conventional Flying acceptance — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1801c) | FD1801A, FD1801B | Close seated boarding, changeover, prone ride, return/unload and distinct interruption cases. Keep this variant closed while New-generation work proceeds. |
| <a id="fd1802"></a> **FD1802 New-generation Flying outcome — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1802) | FD1802C | Close the full sub-variant from A–C evidence; discovery or upright-only targeting is insufficient. |
| <a id="fd1802a"></a> **FD1802A New-generation Flying and shared yaw mechanics — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1802a) | FD1802S, FD1801A; integrated profile, marker binding and pose contracts (extend within this package where needed) | Implement the specified seated/sideways sections, route-based posture transitions, yaw pivot/frame and posture composition. Deliver the shared yaw requirements below and prove occupied scenery-facing behavior in both seated and prone states, transitions and distinct synchronization/failure cases. Supply the reusable handoff through this real vehicle. |
| <a id="fd1802b"></a> **FD1802B New-generation Flying content — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1802b) | Stable FD1802A specification/dimensions | Deliver original variant assets, transition equipment, marker/editor controls, recipes, guide and example combining seated scenery presentation, prone targeting and return to unload. |
| <a id="fd1802c"></a> **FD1802C New-generation Flying acceptance — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1802c) | FD1802A, FD1802B | Close the complete sub-variant workflow and distinct yaw/posture/persistence cases. Upright-only yaw proof cannot close prone targeting. Reuse FD1801 evidence for unchanged common paths. |
| <a id="fd1803"></a> **FD1803 Flying outcome gate — planned** [Steps](docs/roadmap/0.7-complex-motion.md#fd1803) | FD1801, FD1802 | Both Flying variants and their declared yaw/posture workflows are complete. Feed FD701 and 1.0; reuse their evidence and leave final release qualification to FD901–FD905. |

#### Shared scenery-facing yaw requirements

- Support authored unwrapped yaw-angle profiles and target-marker bindings over
  named route sections. Yaw physically moves the passenger carrier and riders;
  it is not a forced camera-look effect. Preserve independent player look and
  the comfort option, with one owner of each composed transform.
- Specify each mechanism's physical pivot, rotation frame, permitted range and
  posture composition. Project a scenery target into the allowed yaw plane;
  targets do not imply extra pitch axes or unrestricted three-dimensional aiming.
  Prove New-generation targeting in both seated and prone posture and on the
  declared banked/inverted track envelope.
- Define smooth section entry/exit, turn direction, winding, angular-speed and
  acceleration bounds. Authored path profiles retain canonical direction and
  retrace on reversal. Target-facing sections resolve the marker from the current
  vehicle pose. Designated stopped show sections may command bounded yaw changes;
  those commands must not advance distance-based profiles or actuate propulsion.
- Specify deterministic fallback for a missing/unloaded marker, unreachable yaw,
  degenerate target projection or lost authority. Retain a valid pose with bounded
  movement, expose the reason and deny dependent transitions where alignment is
  required; never snap occupied riders or silently release restraints.
- Persist marker identity/binding, angle/turn count, mode and transition state;
  validate edits/permissions and restore late-join/restart state. Test only changed
  synchronization and lifecycle paths. Clearances include the combined posture
  and yaw sweep, not just two independent static poses.

### Milestone 0.8 — Powered Inverted Yaw Coaster

This is the upright-seated, below-track [MACK-style powered inverted concept](https://www.mack-rides.com/products/inverted-powered-coaster).
It is a separate vehicle family, not another Flying variant or the free-swinging
Arrow-style Suspended mechanism. Deliver one configuration with one independently
controlled yaw carrier per car, full-turn yaw with explicit winding/unwinding,
and powered forward/reverse travel. Passengers remain seated upright relative to
their carrier; prone changeover and free lateral swing are outside this package.

Include authored yaw and scenery tracking while travelling, bounded orientation
changes at designated show stops, editable speed sections, stopping points and
dwell durations. One propulsion authority owns travel; it cannot be bypassed by
yaw or scene commands. Expose a finite set of ride events to existing light/audio
integrations, with standalone operation and no general scripting/animation editor.

| Package / status | Actual prerequisites | Ordered work and completion condition |
| --- | --- | --- |
| <a id="fd1901"></a> **FD1901 Powered Inverted Yaw outcome — planned** [Steps](docs/roadmap/0.8-operations.md#fd1901) | FD1901C | Close the A–C vehicle outcome using cumulative evidence, independently of Flying's remaining content. |
| <a id="fd1901a"></a> **FD1901A Powered Inverted Yaw mechanics — planned** [Steps](docs/roadmap/0.8-operations.md#fd1901a) | FD1901S, FD1601A, FD1802A, FD1701A; chosen powered-drive implementation | Implement the specified running gear, independent carrier configuration, swept clearance, powered-drive provider and standalone path. Deliver full-turn yaw, scenery tracking, travel speed/direction/dwell and station yaw capture. Prove restraint/dispatch interlocks, interrupted turns, power/controller loss and occupied handoff. Reuse yaw mechanics without substituting Flying posture behavior. |
| <a id="fd1901b"></a> **FD1901B Powered Inverted Yaw content — planned** [Steps](docs/roadmap/0.8-operations.md#fd1901b) | Stable FD1901A specification/dimensions | Deliver original vehicle/station assets, recipes, controls and guide. Supply one configurable example with travelling scenery tracking, a yaw-changing show stop, reverse travel and return to unloading. Define the finite scene-event list and optional adapter boundaries. |
| <a id="fd1901c"></a> **FD1901C Powered Inverted Yaw acceptance — planned** [Steps](docs/roadmap/0.8-operations.md#fd1901c) | FD1901A, FD1901B | Close the complete player cycle, declared event behavior and distinct upright pose/propulsion/interruption/persistence cases. Reuse accepted shared-yaw and operating evidence without replaying Flying acceptance. |
| <a id="fd1902"></a> **FD1902 Powered Inverted Yaw outcome gate — planned** [Steps](docs/roadmap/0.8-operations.md#fd1902) | FD1901 | Reconcile the specified vehicle, content and profiles, then feed FD813 and 1.0. Reuse accepted evidence; final release qualification remains FD901–FD905. |

### Post-1.0 moving-track program

Turntables, drop tables (drop tracks), tilt tracks and the additional styles below
are explicit post-1.0 roadmap work. Their delivery versions are assigned after
FD2001 establishes native feasibility; they are not requirements for FD1000,
FD905, FD1003 or the fixed-track vehicle outcome gates. This is implementation scope,
not discovery alone. Do not quietly replace a requested mechanism with research
or a decorative animation if the required route/passenger behavior is blocked.

Start with one retained short track segment moving between two fixed endpoints.
Prove graph disconnection while moving, exclusive destination reservation,
physical capture and vehicle retention, coordinate/pose ownership, connection
only when aligned and locked, multiplayer synchronization and save/restart.
External track-provider hooks must be inspected and working; a missing hook is
a named blocker for that mechanism, not a reason to reopen fixed-track acceptance.
Scope mechanisms to purpose-built assemblies; arbitrary track on player-built
moving contraptions remains a separate future feature.
When FD2001 allocates shipping increments, give each its own permanent FD2007
suffix gate and finite mechanism/profile list. Each increment gate closes once;
later styles never reopen it. The FD2007 parent aggregates the complete adopted
program while already accepted mechanisms may ship in those earlier increments.

| Package / status | Actual prerequisites | Ordered work and completion condition |
| --- | --- | --- |
| <a id="fd2001"></a> **FD2001 Moving-segment contracts and prototype — planned** [Steps](docs/roadmap/moving-track.md#fd2001) | Integrated provider/eligibility, passenger and persistence contracts; FD1701A authority interface | Specify one segment, two endpoints, vehicle envelope and ownership. Build a retained native/occupied prototype proving the graph/retention lifecycle above, including power loss, interrupted movement, reload and endpoint removal. Record required provider changes, finite limits and delivery allocation; discovery alone cannot close the working prototype. |
| <a id="fd2002"></a> **FD2002 Transfer platform — planned** [Steps](docs/roadmap/moving-track.md#fd2002) | FD2001 | Deliver a lateral transfer between two fixed endpoints, capture/lock and controller requests, original platform/track assets, controls, guide and example. Complete when occupied transfer, interrupted recovery and correct graph reconnection pass. No automatic depot system is included. |
| <a id="fd2003"></a> **FD2003 Turntable — planned** [Steps](docs/roadmap/moving-track.md#fd2003) | FD2001 | Deliver a yaw-rotating track table with full-turn geometry and a finite declared endpoint list. Reserve the chosen exit, retain the vehicle while disconnected and reconnect only when aligned/locked. Include original content, player routing controls, example and distinct occupied rotation/restart checks. |
| <a id="fd2004"></a> **FD2004 Drop table / drop track — planned** [Steps](docs/roadmap/moving-track.md#fd2004) | FD2001 | Deliver a retained track segment with upper/lower connections, specified vertical drop/braking/reset motion and controlled recapture. Include original content, player controls, example and distinct interrupted-drop, endpoint and occupied recovery acceptance. |
| <a id="fd2005"></a> **FD2005 Tilt track — planned** [Steps](docs/roadmap/moving-track.md#fd2005) | FD2001 | Deliver a retained segment pitching between declared entry/exit angles, controlled vehicle release only after locking, and return/reset. Include original content, player controls, example and distinct clearance, power-loss and interrupted-tilt acceptance. |
| <a id="fd2006"></a> **FD2006 Additional moving-track styles — planned** [Steps](docs/roadmap/moving-track.md#fd2006) | FD2006A–D | Aggregate the finite A–D list below with original content, player controls, examples and distinct operating/recovery acceptance. Keep completed styles closed; arbitrary additional styles need their own scope addition. |
| <a id="fd2007"></a> **FD2007 Moving-track release reconciliation — planned** [Steps](docs/roadmap/moving-track.md#fd2007) | FD2002, FD2003, FD2004, FD2005, FD2006; their allocated increment gates | Each increment gate freezes its supported mechanism/vehicle/provider list and reconciles graph/authority conflicts, visuals, persistence, measured workload and package/docs checks. Close this parent when all adopted packages are covered. Earlier increments may ship independently; unshipped named packages remain planned. No family × mechanism × provider product is implied without a real implementation distinction. |

FD2006's finite follow-on set is:

| Style package / status | Prerequisites | Required result |
| --- | --- | --- |
| <a id="fd2006a"></a> **FD2006A Elevator track — planned** [Steps](docs/roadmap/moving-track.md#fd2006a) | FD2004 | Controlled upward/downward travel between fixed height stops with retained vehicles, endpoint locks and restart/recovery; a powered lift cycle rather than an assumed drop-table setting. |
| <a id="fd2006b"></a> **FD2006B Teeter-totter track — planned** [Steps](docs/roadmap/moving-track.md#fd2006b) | FD2005 | Bounded repeated pitch/sway followed by a declared locked exit; retain passenger/vehicle state and accept interrupted oscillation recovery. |
| <a id="fd2006c"></a> **FD2006C Side-drop track — planned** [Steps](docs/roadmap/moving-track.md#fd2006c) | FD2002, FD2004 | A defined combined lateral/downward trajectory with its own clearances, capture, graph endpoints and recovery; passing the two independent axes alone is insufficient. |
| <a id="fd2006d"></a> **FD2006D Compound motion table — planned** [Steps](docs/roadmap/moving-track.md#fd2006d) | FD2003, FD2005 | One explicitly specified yaw-plus-pitch mechanism with a finite exit set; prove simultaneous transforms, swept clearance, route selection, retention and interrupted recovery. Additional axes require a separate specification. |

[Intamin's Multi Dimension catalog](https://www.intamin.com/product/multi-dimension-coaster/)
provides references for turntables, drop, tilt, lift and compound-motion concepts;
our bounded implementations and original assets do not claim exact replicas.
Research and independent content concepts may proceed early. Dependent runtime
work consumes the integrated FD2001 contract; no moving-track package requires
an unfinished unrelated Flying or Powered Inverted checkout.
