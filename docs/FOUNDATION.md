# FD001–FD008 foundation acceptance

Validated locally on 2026-09-30. This gate selects a native Sable architecture for the next development phase. It does **not** certify the unfinished 0.1/1.0 vehicle content or unrestricted operation in existing worlds.

## Selected stack (FD001)

| Component | Pin |
| --- | --- |
| Minecraft / Java | 1.21.1 / 21 |
| NeoForge | 21.1.248 |
| Create | 6.0.10-280 |
| Coasters Simulated | 0.1.5, CurseMaven project 1601871/file 8655742 |
| Sable | 2.0.3, including its published Rapier backend |
| Flywheel | 1.0.6 |
| Ponder / Registrate | 1.0.82+mc1.21.1 / MC1.21-1.3.0+67 |
| Sable Companion / Veil | 1.6.0 / 4.1.4, via Sable's published dependency graph |
| ModDevGradle / Gradle | 2.0.148 / 9.2.1 |

`build.gradle` supplies client, observer, dedicated-server, and GameTest runs. The wrapper distribution checksum and CI action revisions are pinned. A clean build and dedicated server start were exercised; two real development clients connected to the loopback server. Screenshots were taken in virtual X displays with software rendering. The final artifact checksum is in `evidence/artifact.sha256`.

## Eligibility and assembly (FD002, FD005)

`RailEligibilityMixin` filters the return of `CoasterCartRailSnapEligibility.filterHitForBearing(SubLevel, Level, GraphHit)`. This is the actual base-mod path used by ongoing engagement/re-engagement and its client engagement helpers. `InitialSnapMixin` guards `CoasterCartTrackSnap.applyInitialSnapAfterSpawn(ServerSubLevel, ServerLevel, GraphHit)`, which otherwise bypasses that filter. Both mixins require their injection targets to exist and use the exact pinned upstream version.

Our fixture creation binds the family before any initial attachment. Family-controller blocks follow an assembled chassis and are found through its block palettes. Missing metadata or mixed-family markers reject attachment; they do not turn the assembly into an ordinary cart. Broken/replaced metadata cannot change a saved vehicle's family. The last bound edge only permits that edge or connected endpoints, so an unrelated nearby crossing cannot steal the vehicle.

The prototype deliberately retains the **identical, unmodified Coasters main rail and bearing geometry**. Its ordinary-cart certificate is limited to a bare base bearing, with a bounded one-block assembly; arbitrary added bodies are rejected. The native test actually moves that ordinary bearing on the prototype track and checks it has no carrier record. This does not certify other adapter dimensions or recovery towing. Those remain FD108/FD402.

The general `TrackContract`/`VehicleContract` API separately represents provider, family, variant, main/side/upstop contact dimensions, clearance, mechanisms, and carrier groups. The contract tests exercise all 25 family combinations and equivalent providers. An Arrow vehicle always requires control rails and rotary clearance, even if its caller omits optional requirements. Appearance and lock state cannot grant eligibility. The numeric unit-test dimensions are synthetic fixtures, not claims about real coaster dimensions.

Schema 1 records the concrete prototype variant/provider/contact certificate, directed edge/profile binding, chassis/carrier UUIDs, unwrapped angle, angular velocity, capture state, and occupancy. Unknown families, schemas, variants, and provider/certificate identities fail explicitly. Runtime external-provider registration is intentionally later work (FD401/FD801); the contract already expresses equivalent providers.

## Native carrier and rider decision (FD003, FD004)

Use one Coasters Sable chassis and one additional native Sable sublevel per carrier group, connected with `RotaryConstraintConfiguration` and `RotaryConstraintHandle.setMotor`. The test chassis has two groups. Controlled targets derive from position on the fixture's straight edge and an unwrapped rotation profile. Free groups receive no profile motor stiffness; Sable owns gravity, block mass, inertia, and collision. Capture uses a damped motor rather than teleporting the carrier.

The pure `FreeRotor` implementation is a reference/test harness. Its gravity is **not** applied to the native bodies. Passenger loading presets and production occupancy mass treatment still belong to FD103; the prototype tests native block-body dynamics and real passenger transport separately.

Create seat entities live in each carrier's plot. Sable owns the passenger, first-person, third-person, and remote-player transformations. The addon does not apply a second camera or passenger rotation. See [first person](evidence/rider-first-person.png), [third person](evidence/rider-third-person.png), and [observer view](evidence/observer-carriers.png). The observer sees Dev inverted on the same seat. The occupied Arrow carrier reached 12.5457 radians (almost two complete turns), rolled back to 9.5182 radians, then captured at an upright equivalent of 2π. Dismount/rejoin leaves normal world-relative camera behavior; disconnected passengers do not leave ghost occupancy.

A real passenger was also mounted on the free-family fixture and remained attached while the two groups moved independently; see [occupied free-carrier state](evidence/occupied-free-carrier.txt) and [remote view of the free rider](evidence/observer-free.png).

The entity fallback is unnecessary for this gate. Art is deliberately primitive: original controller model definitions, dependency-provided bearing/track/seats, and vanilla ballast blocks. Original functional four-rail visuals and finished vehicles remain FD101/FD104.

## Regression harness and baseline (FD006)

Seven JUnit tests cover unwrapped full turns, position-based rollback, five-family exclusion, ordinary access without 4D effects, side/upstop mismatch despite equal gauge, clearance/mechanism rejection, single loading contribution, gravity magnitude, timestep convergence, energy stability, and invalid data.

Two actual NeoForge GameTests run against Coasters + native Sable physics. They cover initial/ongoing wrong-family rejection, unbound-controller rejection, a moving ordinary bearing, a controlled full turn while the chassis travels, separate free-group motion, smooth capture, and save/snapshot round trips. Test motion applies a declared fixture drive; it is not the later production propulsion system. Results are in [unit evidence](evidence/unit-tests.txt) and [native evidence](evidence/automated-tests.txt).

On the AMD Ryzen 9 7900X3D (24 logical CPUs), two fixtures/four carriers/six native sublevels produced roughly 55–101 μs average addon callback time per physics event across the loaded dimensions during this session. This counter includes empty-dimension calls and is not a per-carrier or complete physics-engine benchmark. [The rendering baseline](evidence/render-baseline.png) shows 119 FPS at a 120 FPS cap, 854×480, Mesa llvmpipe, with two clients running. No universal FPS guarantee or large-train performance claim follows from this small fixture.

## Persistence and multiplayer (FD007)

`FoundationData` owns functional state. Sable owns native body pose/velocity persistence and authoritative pose replication; Create/Minecraft owns actual passenger entities. The addon sends a full functional snapshot on login, dimension change, and once per second. Clients render native server snapshots and do not independently integrate the reference rotor. The diagnostic functional snapshot may lag a moving native body; it is not used to render its rotation.

Save serialization refreshes physical angle/velocity at the save instant. Recreated joints restore saved relative angular velocity once, including low velocities Sable omits from its native save. Server-only classes do not reference the client diagnostic implementation.

Validation used Dev and Observer, late joining, disconnect/rejoin, and repeated dedicated-server restarts. [Saved NBT decoded as JSON](evidence/saved-state.json) exactly matches all four UUID/angle/angular-velocity records in [the frozen restart](evidence/after-restart-frozen.txt). The optional `runServer -PfdFrozen` starts paused for this comparison; `/tick unfreeze` resumes real physics. Free groups then continue moving. [Rider](evidence/rider-snapshots.txt) and [observer](evidence/observer-snapshots.txt) logs record the same carrier identities and native orientation. `scripts/inspect-foundation-save.py` reproduces the saved-data inspection without modifying the file.

## Gate decision and remaining risks (FD008)

The foundation gate passes for the bounded prototype: native controlled/free groups, real passengers, strict family gates, and persistent multiplayer state are demonstrated. Proceed to FD101–FD109 using native Sable carriers, not an entity simulation rewrite.

The implementation revealed a material upstream precision limit: native translation effectively stalled at GameTest's randomly selected positions millions of blocks from origin despite nonzero velocity. The same test passes near spawn. Prototype creation is therefore restricted to ±1024 horizontal coordinates and the native tests use dedicated near-origin corridors. General-coordinate support needs investigation with the physics provider before a production release; do not silently remove this guard.

Other tracked limits are production carrier/load tuning, precise finished wheel/rail/clearance geometry, general curve arc-length profiles, boarding interlocks, arbitrary reassembly repair, chunk unload/support-loss recovery, multi-car consists, native packet churn during reconnect, and large-scene/shader validation. These map to existing FD10x/FD90x work. Optional upstream mixin/model warnings occur in the pinned stack; dedicated startup and the required tests still complete. Track Styles availability or licensing is not a dependency of this foundation.

Revised planning estimate after the prototype: budget about 2–4 focused development weeks for FD101–FD109, with a separate 2–5 day investigation for the coordinate limitation before promising general-world support. Later magnetic/shuttle/content work retains the roadmap's gates; no 1.0 date is implied. This is an engineering estimate, not a measured delivery guarantee.
