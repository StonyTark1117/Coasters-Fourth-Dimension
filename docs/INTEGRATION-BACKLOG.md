# Integration backlog — staged, implementation paused

Verified on **2026-11-02** for **Minecraft 1.21.1 / NeoForge**. This catalog refresh
implements the user's request to collect current artifacts and refine plans only.
New ecosystem integration implementation remains paused pending user approval
after core mod progress is reviewed. Existing Create/Engineered/Track Styles
release acceptance retains its authorized scope. Completion of a core milestone
does not automatically resume the expansion backlog.

The [machine-readable catalog](../research/integration-catalog-2026-11-02.json)
records exact project/file IDs, download URLs, SHA-256 hashes, embedded mod versions,
dependencies, mixin declarations and local paths. All 12 JARs are staged under
`build/integration-catalog/2026-11-02/`, outside runtime mod folders and ignored by
Git. They are a research cache, not a runnable modpack; a build clean may remove
this cache, and the manifest preserves the URLs and hashes needed to recover it.
No JARs are bundled into our release. No source, build pins, runtime profiles or
existing acceptance evidence were changed by this refresh. Archive integrity and
metadata were checked; no mod was launched and no new compatibility is claimed.

## Planning document ownership

The [current qualification record](CURRENT-QUALIFICATION.md) owns the selected
source/JAR identity, intended profiles, reusable evidence and required retests.
The roadmap's 2026-11-04 reconciliation replaces chronological checkpoint prose
with finite acceptance tasks; historical catalog dates below remain unchanged.

This backlog owns the dated integration inventory, proposed release allocation,
dependency decisions and certification plan. [PLAN.md](../PLAN.md) defines design
boundaries; [ROADMAP.md](../ROADMAP.md) owns permanent FD packages and gate status;
[INTEGRATIONS.md](INTEGRATIONS.md) describes existing implementation and bounded
evidence. [README.md](../README.md) summarizes release scope,
[THIRD_PARTY.md](../THIRD_PARTY.md) records distribution boundaries, and
[MIGRATION.md](MIGRATION.md) scopes the documented schema-6 procedure separately
from deferred schema-7 reconciliation. Historical changelog, audit and evidence
records retain their original artifact-specific findings. New planning scope
does not rewrite their results or mark an open gate complete.

Consistency review on **2026-11-03** checked these seven documents plus the FD401
release handoff and both FD403 acceptance/preparation documents. All twelve cached
artifacts still match the catalog's SHA-256 hashes. This is a local consistency
check; the upstream version-discovery date remains **2026-11-02**.

## Current artifact inventory

“Latest” means the newest target-platform artifact found on the official project
and file pages on the research date. Beta releases are explicitly retained as
candidates. Website caches differed, so individual release pages and downloaded
JAR metadata take precedence over stale search snippets.

| Project / release source | Staged version | Published | Role |
| --- | --- | --- | --- |
| [Create](https://www.curseforge.com/minecraft/mc-mods/create/files/7963363) | 6.0.10 | Apr 21 | Foundation; full distribution for inspection |
| [Coasters Simulated](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated/files/8655742) | 0.1.5 | Aug 15 | Foundation; current pin still latest |
| [Sable](https://www.curseforge.com/minecraft/mc-mods/sable/files/9043865) | 2.0.6 | Oct 3 | Future dependency candidate |
| [Engineered](https://www.curseforge.com/minecraft/mc-mods/coasters-engineered/files/8863559) | 1.1.1 | Sep 12 | Motors, brakes, observers, sequencing |
| [Track Styles](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles/files/9020601) | 1.2.0 | Sep 30 | Appearance; functional adapter remains separate |
| [Simulated Additions](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions/files/9243346) | 0.2.5 **beta** | Oct 18 | Stations, restraint reporting, seats |
| [Magnetized](https://www.curseforge.com/minecraft/mc-mods/coasters-magnetized/files/8668703) | 1.1.0 | Aug 17 | Acceleration and braking |
| [Immersion](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion/files/9338330) | 1.1.0 | Oct 26 | Client motion, camera and sound |
| [Coaster Skin](https://www.curseforge.com/minecraft/mc-mods/create-coaster-skin/files/9191189) | 1.0.0 | Oct 15 | Appearance, seating and cart behavior |
| [Waterparked](https://www.curseforge.com/minecraft/mc-mods/create-waterparked/files/9058331) | 0.2.8-beta-hotfix1-coaster0.1.5 **beta** | Oct 4 | Slides and boats; coexistence first |
| [Tracks and Balloons / CTNB](https://www.curseforge.com/minecraft/mc-mods/ctnb/files/8718286) | 1.2 | Aug 23 | Functional tracks, controls and decoration |
| [Create Coasters](https://www.curseforge.com/minecraft/mc-mods/create-coasters/files/8110328) | 2.0 | May 18 | Adjacent Create train/contraption park utility |

All dates above are 2026. The screenshot's seven addons and both explicit URLs are
included. Create Coasters is a separate project, not another name for Coasters
Simulated. CTNB *is* the same project as the former Create Coasters Extras
(project 1649197, mod ID `coasters_extras`); do not install its aliases together.
Broad search and the official dependent listing informed discovery. The listing
also contains many modpacks and was not exhaustively paginated; this is not an
exhaustive census of all addons. Unrelated coaster systems and modpacks are outside
this integration scope.

## Findings that change the earlier plan

- **Coaster Skin is gated by a dependency update.** Its JAR requires NeoForge
  `>=21.1.256`, Sable exactly `2.0.6`, Create exactly `6.0.10` and Coasters exactly
  `0.1.5`. Our active NeoForge `21.1.248` and Sable `2.0.3` remain unchanged.
  Its [published features](https://www.curseforge.com/minecraft/mc-mods/create-coaster-skin)
  include seating and different vehicle behavior, so skin support needs mechanical
  review as well as rendering review.
- **Additions has conflicting version labels.** Published file 9243346 is 0.2.5,
  while its JAR declares `coasterfins` version `0.2.2`. A future adapter must retain
  the file/hash identity alongside the runtime version and resolve ambiguity
  before enabling hooks. Its required `simulated >=1.3.0` and `interiors >=0.6`
  entries also need dependency closure. The official
  [relations](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions/relations/dependencies)
  list Aeronautics and Interiors. Their exact artifacts are not staged here.
- **Immersion 1.1.0 changes the dependency assumption.** Its release notes describe
  client-only installation; the JAR declares `IGNORE_SERVER_VERSION`, client-side
  Create/Coasters/Sable requirements and optional Aeronautics. Replace the old
  mandatory-Aeronautics assumption for this candidate. Future testing must still
  cover its Sable/Coasters interpolation mixins and camera ownership.
- **Sable 2.0.6 needs its own baseline review.** Its declared Sodium/scalablelux
  incompatibilities are recorded in the catalog. Staging it does not authorize a
  dependency upgrade or establish graphics compatibility.
- **CTNB includes functional controls.** Keep material appearance separate from
  provider eligibility; the released JAR accepts Coasters `[0.1.2,0.2.0)`.
- **Track Styles licensing remains inconsistent.** The page says MIT while the
  JAR says All Rights Reserved. Keep local inspection separate from any future
  asset reuse or redistribution decision.

## Fit within the current release plan

This is a **proposed release allocation**, not authorization to resume implementation
or a promise of release dates. The existing 0.4 → 0.9 → 1.0 gates stay in order.
The proposed 1.1 and 1.2 labels below describe optional expansion scope; they do
not renumber existing FD packages or change their completion status.
The [roadmap release map](../ROADMAP.md#release-map) also retains separate later
vehicle proposals, including the fixed-seat wing coaster alongside proposed 1.1
and the proposed 1.3–1.5 families. This backlog allocates integrations only;
neither addon support nor shared release numbering approves those vehicle features
or adds them to the five-family 1.0 gate.

### Current baseline and branch reconciliation

On 2026-11-02, both roadmaps describe the bounded five-family 0.3 gate (including
FD305) as complete. The old instruction to finish FD305's occupied restart gate
is superseded: preserve that evidence and address broader lifecycle work under
FD902. FD402 rendered vehicle/rider/linkage clearance and occupied operator
acceptance, FD403 real-client survival teaching, and FD401 complete render/operator
coverage remain open before FD404. FD905 and 1.0 remain gated.

The initial planning reconciliation used core revision `fcb142e` on 2026-11-02.
Release revision `67c99ef` subsequently integrated the reconciled Create telemetry
checkpoint, with bounded standalone client evidence from exact source `046ead9`.
See the [telemetry evidence and limitations](evidence/0.9/create-telemetry/local-046ead9/README.md).
This removes the pending telemetry merge task; it does not certify every optional
profile or close FD401/FD902. Persistence/networking remain schema 6 / protocol 7.

The former integration work was preserved by the
[2026-11-04 branch consolidation](BRANCH-CONSOLIDATION.md), including archived
snapshot `f3303c7` of the unfinished schema-7/protocol-8 provider work. Those
provider/schema changes remain deferred, require a dedicated migration decision,
and are not prerequisites for 0.9. `main` is the active integration branch. Historical
evidence stays attached to its actual artifact; neither a version string nor one
passing checkpoint proves a complete release gate.

If a later approved expansion needs the provider registry, reconcile it against
that release's core and preserve recovery/lifecycle fixes. Test supported upgrades,
missing-provider state and matching multiplayer builds on the resulting JAR.
Restore backups for downgrade recovery; do not assume unknown provider data can
be discarded safely. Complete the current release gates without importing this
migration merely to prepare for future addons.

### Release allocation

| Release / gate | Integration deliverable | Required before release | Deferred scope |
| --- | --- | --- | --- |
| 0.4 / FD401–FD404 | Optional adapter foundation; shared operations snapshots; Create goggles/Display Links/Ponder; current Engineered 1.1.1 operations and Track Styles 1.2.0 Arrow decoration | Current schema-6/protocol-7 baseline; complete advertised operator/render/clearance evidence; FD402 recovery and FD403 survival teaching acceptance | Newly staged adapters and external functional tracks |
| 0.9 / FD901–FD905 | Freeze and certify base, Styles, Engineered, and Styles+Engineered profiles | Compatibility, multiplayer/lifecycle, migration, rendering/performance and defect closure on exact candidate artifact | No unapproved feature growth or routine dependency refresh during stabilization |
| 1.0 / FD1001–FD1003 | Ship the verified five-family mod and the optional combinations that passed the candidate gate | Accurate install/support matrix, tutorials, recovery limits, licenses, reproducible artifact and clean client/server rides | No prerequisite on every ecosystem addon or a future upstream Arrow 4D release |
| Proposed 1.1 operations expansion | Additions station telemetry/interlocks; CTNB functional-track/control adapters; Magnetized operating equipment | Independent dependency-baseline review, per-addon certification, conflict-pair tests and an explicitly supported combined operations pack | Rotating Additions seats unless separately proven; new car bodies/camera systems |
| Proposed 1.2 presentation and vehicle expansion | Coaster Skin support, rotating-seat follow-up, Immersion carrier-aware visuals; Waterparked and Create Coasters coexistence | Stable carrier/seat API; exact dependency closure; client pose/render/camera and lifecycle evidence | Fourth Dimension cars on water slides, native Create train controls driving Sable cars, or unproven body conversions |
| Independent FD801 | Real Track Styles Arrow 4D functional-track adapter | Applicable inspectable external release; geometry/mechanism equivalence; rendering ownership; persistence and same-family transitions | Never blocks standalone 1.0; appearance availability is not functional eligibility |

No new integration is promoted into 0.9/1.0 simply because its JAR is staged. If a
new addon becomes a must-have before 1.0, explicitly revise FD404's scope before
freezing 0.9, then fund its complete acceptance matrix. If an already-advertised
profile cannot meet its gate, either fix it or explicitly withdraw the claim and
disable the affected feature before release; do not silently waive the criterion.

### What each integration means

| Candidate | Intended player result | Engineering boundary and acceptance |
| --- | --- | --- |
| Create 6.0.10 | Learn each family through Ponder, inspect stations/winches with goggles, display ride status | Finish existing teaching work; prove whole-consist occupancy/readiness/fault/phase displays agree with current server state; no optional addon needed for core tutorials |
| Coasters Simulated 0.1.5 + Sable | Reliable native track, chassis, passenger mounts and moving structures | Retain required foundation; dependency updates are separate changes with five-family, recovery, persistence and client-render regressions |
| Engineered 1.1.1 | Powered dispatch, launch, trim, STOP, rollback, observers and FE workflows | Complete an operator-built Arrow demo, then remaining families; keep restraint/carrier guards; prove PAX catch/lift/release/return authority and power-starvation behavior |
| Track Styles 1.2.0 | Optional Arrow spine/tie appearance while original mechanics continue | One renderer per component; retain running/control rails and magnetic hardware; test limits, banked clearance, whole-section transitions, middle-of-section editor reach, resource reload and removal fallback; FD801 stays separate |
| Additions 0.2.5 beta | Station occupancy/restraint signals and controlled automatic dispatch | Stage 1 reads shared telemetry and submits validated requests; no direct mechanical-state writes. Stage 2 separately proves rotating-seat mounts, restraint links and passenger restoration. Resolve its embedded 0.2.2 label and Aeronautics/Interiors dependency closure first |
| CTNB 1.2 | Use selected equivalent tracks, station sensors and operating controls with family-correct cars | Inventory actual materials/behaviors, initially deny unverified variants; cosmetic material changes preserve profiles and family. Explicitly test station hold/release, reverse, launch, crossings and provider joins; exclude any behavior that cannot respect interlocks |
| Magnetized 1.1.0 | Alternative longitudinal launch/brake equipment | Prove directional force, rollback, braking, power transitions and controller ownership. Its track propulsion does not replace Fourth Dimension's magnetic seat-axis torque system |
| Coaster Skin 1.0.0 | Approved visual/seat options that preserve the rotating-carrier experience | First determine which options can coexist with our chassis; reject unsupported modifications. Test dimensions, rider count, restraint/seat ownership, dismount, persistence, buoyancy/skimmer behavior where offered. Native cart skins do not automatically apply to our custom bodies |
| Immersion 1.1.0 | Optional ride camera, sound and visual motion effects | Compose effects once on the authoritative carrier pose. Test inversion, relative mouse look, first/third person, rider body, comfort settings and dismount restoration; reconcile Sable interpolation mixins; test client-only installation with an unmodified server |
| Waterparked beta hotfix | Water rides and Fourth Dimension rides coexist in one park | First prove startup, shared graph/mixin and rendering coexistence, with slide eligibility rejected for our cars. Any transfer to boats/slides or amphibious 4D vehicle is a separate feature proposal |
| Create Coasters 2.0 | Park utility blocks coexist without interfering with Fourth Dimension rides | It targets Create trains/contraptions. Audit seat-lock and camera overlap; bridge only capabilities demonstrated to apply to Sable rides, otherwise advertise coexistence only |

For all rows, “full” means the explicitly accepted capability set, including
correct rejection and fallback. It does not mean every block, skin or external
track becomes compatible with every Fourth Dimension family.

### Shared design, reused by every adapter

- **One mechanics authority.** Fourth Dimension owns family matching, carrier
  motion, restraints, readiness and faults. PAX's bound winch owns shuttle phases;
  recovery retains its existing explicit handover and braked-resume rules.
- **Read-only telemetry and validated requests.** Publish immutable whole-consist
  snapshots. An external capture/dispatch request rechecks current server state,
  controller binding, permissions/range where applicable, occupancy, restraints,
  alignment, faults and recovery state. An old ready signal cannot authorize motion.
- **Explicit propulsion ownership.** Define one permitted control owner for each
  section/operating phase. Conflicting Engineered, CTNB, Magnetized or Additions
  commands must reject or leave the ride retained. If an addon bypasses interception,
  leave that combination unsupported until its force path can be controlled.
- **Separate function and appearance.** Keep functional provider, family/variant,
  geometry, rotation profiles and appearance distinct. Register only reviewed
  capabilities. Missing appearance falls back; missing functional equipment denies
  operation while retaining saved identity for supported restoration.
- **Isolate optional code.** Exact supported mod versions and capabilities guard
  adapters; optional/client classes cannot break a standalone dedicated server.
  The published Additions label discrepancy requires a reviewed runtime fingerprint
  strategy before enabling its adapter; a download hash alone is not a runtime guard.
  Follow [NeoForge lifecycle/side boundaries](https://docs.neoforged.net/docs/1.21.1/concepts/events/)
  when reconciling registration and client diagnostics.
- **One pose and render owner.** Body, carrier, seat and camera transforms compose
  once. Appearance owns only assigned components. Resource reload, missing models
  and unsupported styles have documented fallback without changing mechanical data.

### Dependency and distribution decisions

Keep the current required core stack while closing its gates. Sable 2.0.6 and
NeoForge >=21.1.256 are candidates for a separately validated expansion baseline,
not incidental upgrades to make Coaster Skin load. Skin pins Sable 2.0.6, Create
6.0.10 and Coasters 0.1.5 exactly. Check the entire selected graphics stack against
Sable's declared incompatibilities. Additions needs separately resolved Aeronautics
and Interiors artifacts, including their transitive dependencies and actual mod IDs.

The staged Immersion 1.1.0 metadata makes Aeronautics optional and marks client-side
behavior; do not reuse the older plan's mandatory-Aeronautics claim for Immersion.
Optional addons remain separately installed. Reuse code/assets only after applicable
terms are established; the staged archive cache is not redistributable release
content. Keep unknown versions visibly unsupported with base functionality intact.

### Version refresh before expansion

Before implementing an approved expansion, recheck the official project/file
pages for Minecraft 1.21.1 and NeoForge, including release/beta status and required
dependencies. Record a new dated catalog with exact file IDs, hashes, embedded
versions and dependency closure; retain the old catalog for existing evidence.
Resolve Additions' published/runtime version discrepancy before enabling hooks.
Do not call the November 2 inventory perpetually latest or silently substitute a
new artifact under an existing supported-version claim.

Stage refreshed candidates outside runtime folders first. Choose and validate
any core dependency upgrade separately, then update the affected adapter guards,
runtime profile and certification matrix within the approved scope. Every changed
artifact needs an explicit evidence review and the applicable regressions before
it can join an advertised profile. Reaching 1.0 does not authorize this work;
the expansion pause remains in force until user approval.

### Certification and release evidence

Use the existing FD901–FD904 categories for 0.9 and equivalent regression gates for
each later expansion. Apply [VALIDATION-POLICY.md](VALIDATION-POLICY.md): carry
accepted behavior forward, test changed integration paths, and do not expand
these categories into every family × profile × interruption combination. The
future-addon profile plan below applies only after that expansion is approved;
it does not add runs to current stabilization. A successful load alone does not
qualify mechanics or rider synchronization.

| Gate | Required evidence for each advertised integration |
| --- | --- |
| Dependency/load | Exact files and runtime mod IDs/versions; resolved required dependencies; standalone and dedicated-server startup; addon absent and unsupported-version behavior |
| FD901 mechanics | Five-family match/reject matrix through placement, assembly, snaps, re-engagement, transitions and dispatch; locked-seat rejection; ordinary carts receive no 4D behavior; declared consist/geometry constraints |
| FD902 lifecycle | Two real clients, late join, moving occupied disconnect/rejoin, restart, chunk interruptions, equipment loss, recovery handover and retained fault/resume behavior; no lost/duplicated passengers |
| FD903 persistence | Supported historical worlds, profiles, provider IDs, appearance fallback/removal/reinstall and explicit conversion refusal; schema upgrade and backup restoration on the final artifact |
| FD904 presentation/performance | Banked/full-turn vehicle/rider/linkage clearance, graphics modes, approved shader setup, resource reload, camera/dismount restoration and timing budgets on declared hardware/scenes |
| Teaching/operator | One reproducible build-and-operate example per advertised capability, including failure/recovery and optional dependency labels; core family examples remain standalone |

Run base + each addon individually, then risk-focused pairs: Engineered/CTNB/
Magnetized control conflicts; Additions/Skin seat-restraint conflicts;
Immersion/Skin/Sable pose conflicts; Styles/CTNB/Waterparked track/render conflicts.
Finally test every profile named in release notes, including the chosen combined
pack and client-only asymmetry. Pairwise tests do not substitute for the combined
pack. Record exclusions explicitly rather than claiming all possible subsets.

Every evidence bundle records source revision, built-JAR hash, all mod hashes,
configuration, world/fixture and reproduction steps, server/client roles, hardware,
results and limitations. New failures reopen the affected gate. Public status should
progress from **staged → inspected → adapter implemented → bounded tests → certified
for a named profile**. Existing evidence stays bounded until the remaining gates pass.

### Execution sequence and approval boundary

Steps 1–5 describe the existing release scope. Steps 6–7 are paused expansion
work and require separate user approval; reaching 1.0 does not grant it.

1. Review then-current core readiness and freeze the intended 0.4/1.0 scope.
2. Complete artifact-specific telemetry acceptance on the integrated candidate, including the advertised optional profiles. Preserve the bounded `046ead9` evidence and its limitations; keep the provider registry and schema/protocol migration deferred to a separate decision.
3. Close FD402/FD403 gaps and finish the existing Create/Engineered/Styles acceptance.
4. Close FD404, then complete FD901–FD904 and review FD905.
5. Complete FD1001–FD1003 for the verified 1.0 scope; publishing remains a separate action.
6. Resolve expansion dependencies and implement/certify the proposed 1.1 operations set.
7. Implement/certify the proposed 1.2 seat/presentation/coexistence set; evaluate FD801
   independently whenever a qualifying upstream release is available.

No integration coding, dependency activation, compatibility testing, version bump,
merge or publication is performed by this planning update.
