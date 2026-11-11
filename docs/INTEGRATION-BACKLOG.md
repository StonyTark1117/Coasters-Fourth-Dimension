# Integration backlog — adopted expanded 1.0 allocation

**Roadmap adopted 2026-11-11 (America/Phoenix).** Operations capabilities are
allocated to 0.8; presentation, optional seats and coexistence to 0.9; final
qualification follows the 1.0 alpha feature freeze. The former blanket expansion
pause is superseded by explicit package prerequisites in [ROADMAP.md](../ROADMAP.md).
All agreed capabilities remain in the stable 1.0 target. Planning adoption is not
runtime activation, compatibility certification, a dependency upgrade or publication.

**Artifact inventory verified 2026-11-02** for Minecraft 1.21.1 / NeoForge.
This dated catalog is retained as research evidence, not a claim to current latest
versions. FD501 refreshes dependencies before implementation as described below.
Existing accepted Create/Engineered/Track Styles behavior remains accepted.

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

This backlog owns the dated integration inventory, adopted release allocation,
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

Stable 1.0 includes all agreed scope formerly proposed through 1.5. The
[roadmap release map](../ROADMAP.md#release-map) owns family delivery order;
this backlog owns addon capabilities and dependency decisions. Existing FD IDs
and acceptance are preserved. FD404 is the original five-family checkpoint;
FD1000 freezes the expanded scope at 1.0 alpha, followed by FD901–FD905 and
FD1001–FD1003. No family or integration depends on first releasing FD1003.

### Current baseline and branch reconciliation

As of the adopted 2026-11-11 plan, foundation through bounded FD305, FD401A–D
and FD402A/C/D retain acceptance. FD402B/E/F and unfinished FD403 player workflows
remain before FD404. Current status belongs to the roadmap and qualification
record; old checkpoint prose below must not reopen completed tasks.

The initial planning reconciliation used core revision `fcb142e` on 2026-11-02.
Release revision `67c99ef` subsequently integrated the reconciled Create telemetry
checkpoint, with bounded standalone client evidence from exact source `046ead9`.
See the [telemetry evidence and limitations](evidence/0.9/create-telemetry/local-046ead9/README.md).
This removes the pending telemetry merge task; it does not certify every optional
profile or close FD401/FD902. Persistence/networking remain schema 6 / protocol 7.

The former integration work was preserved by the
[2026-11-04 branch consolidation](BRANCH-CONSOLIDATION.md), including archived
snapshot `f3303c7` of the unfinished schema-7/protocol-8 provider work. Those
provider/schema changes require the FD501 decision and, only if needed, a
bounded implementation in FD501A for core mechanics or the consuming FD501B–H
package for an adapter. Archived code is not automatically the new baseline.
`main` is the active integration branch. Historical
evidence stays attached to its actual artifact; neither a version string nor one
passing checkpoint proves a complete release gate.

If the selected expansion needs a provider registry, reconcile necessary pieces
against current `main`, preserving recovery/lifecycle fixes. FD501A owns only
core-required migration; FD501B–H own the changes their adapters consume. Reuse
shared changes and checks, including missing providers, matching multiplayer
builds and backup restoration where affected. Retain stable saved IDs; do not
discard unknown provider data for downgrade. A no-change decision requires no
migration run. A blocked optional adapter does not block a sufficient core baseline.

### Release allocation

| Milestone / gate | Integration deliverable | Prerequisite and acceptance |
| --- | --- | --- |
| 0.4 / FD401–FD404 | Preserve accepted Create/Engineered/Styles scope and finish remaining core recovery/player work | FD401 stays closed; FD404 records the bounded core, not final release readiness. |
| 0.5 / FD501, FD501A | Record core and per-adapter decisions; implement only core-required prerequisites | Resolve each consumer separately. A sufficient current core can close FD501A while an optional-adapter issue remains open. |
| 0.8 / FD501B–D, FD810–FD813 | Additions station telemetry/interlocks, selected CTNB tracks/controls, Magnetized equipment and actual authority conflicts | Can start after their integrated control/provider prerequisites, independently of Axis/Roller Ball. Add an operator example per capability and qualify the named operations profile. |
| 0.9 / FD501E–H, FD910–FD915; reuse FD501B | Coaster Skin, rotating-seat follow-up, Immersion, Waterparked and Create Coasters coexistence | Stable representative fixed/free/constrained seat/pose interfaces; normal input, rider state, fallback and combined-profile behavior. |
| 1.0 alpha / FD1000 | Freeze all agreed capabilities and supported profiles | All former-through-1.5 scope implemented with cumulative acceptance; no silent scope reduction. |
| 1.0 beta / RC / FD901–FD905 | Integrated qualification | Reuse accepted cases, test outstanding interactions and final workload, fix affected defects without resetting earlier gates. |
| 1.0 stable / FD1001–FD1003 | Accurate support docs and reproducible artifacts | Clean install/package checks; publication remains separate. |
| Independent FD801 | Inspectable functional Track Styles Arrow 4D adapter | Measured mechanism/geometry equivalence and applicable terms; unavailable upstream release never blocks standalone 1.0. |

FD501B supplies common Additions prerequisites to FD810/FD911; seat-only changes
belong to FD911. FD501C supplies CTNB (FD811), D Magnetized (FD812), E Skin (FD910),
F Immersion (FD912), G Waterparked (FD913) and H Create Coasters (FD914). Each uses
its own recorded FD501 decision, not completion of every other dependency package.
Record a shared prerequisite only when actually consumed, integrate it once into
`main`, and reuse its evidence. Final combined-profile compatibility remains required.

New integrations remain optional installations, but their agreed capabilities
are planned 1.0 deliverables. A staged JAR does not establish a capability. If a
required dependency or capability cannot be delivered, record a named blocker
and seek an explicit scope decision; do not silently omit it or wait indefinitely
on repeated identical launch attempts. Discovery, inspection and completed
bounded cases are retained rather than repeated for a new release label.

### What each integration means

| Candidate | Intended player result | Engineering boundary and acceptance |
| --- | --- | --- |
| Create 6.0.10 | Learn each family through Ponder, inspect stations/winches with goggles, display ride status | Retain accepted FD401D display/telemetry evidence, finish the open FD403 teaching steps, and add new-family guidance with its content; no optional addon is needed for core tutorials |
| Coasters Simulated 0.1.5 + Sable | Reliable native track, chassis, passenger mounts and moving structures | Retain required foundation; dependency updates are separate changes with five-family, recovery, persistence and client-render regressions |
| Engineered 1.1.1 | Powered dispatch, launch, trim, STOP, rollback, observers and FE workflows | Retain accepted FD401C operator/FE/PAX authority evidence. New family or adapter behavior adds only its distinct control/authority checks; do not repeat the completed original-family matrix |
| Track Styles 1.2.0 | Optional Arrow spine/tie appearance while original mechanics continue | Retain accepted FD401B ownership, geometry, edits, reload and fallback evidence. Test only affected new render/provider behavior; preserve running/control rails and magnetic hardware. FD801 stays separate |
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

Keep the current required core stack while closing unchanged core cases. FD501
records the core and adapter-specific upgrade decisions separately. FD501A and
FD501B–H implement only what their actual consumers need. Sable 2.0.6 and
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

In FD501, before activating an expansion dependency, recheck the official project/file
pages for Minecraft 1.21.1 and NeoForge, including release/beta status and required
dependencies. Record a new dated catalog with exact file IDs, hashes, embedded
versions and dependency closure; retain the old catalog for existing evidence.
Resolve Additions' published/runtime version discrepancy before enabling hooks.
Do not call the November 2 inventory perpetually latest or silently substitute a
new artifact under an existing supported-version claim.

Stage refreshed candidates outside runtime folders first. Choose and validate
any core dependency upgrade separately, then update the affected adapter guards,
runtime profile and certification matrix within the approved scope. Do not make
Wing or another unrelated family wait for an optional-adapter-only upgrade. Every changed
dependency needs a scoped impact review and applicable regressions before it can
join an advertised profile. Pin the resulting baseline; later refreshes require
a named blocker or planned change. A different addon/JAR hash alone does not
reopen unrelated acceptance. The adopted plan supplies the scope; prerequisites
control sequencing and publication remains separate.

### Certification and release evidence

Use FD901–FD904 as final expanded-release qualification after FD1000, with the
relevant checks performed during each feature's implementation. Apply [VALIDATION-POLICY.md](VALIDATION-POLICY.md): carry
accepted behavior forward, test changed integration paths, and do not expand
these categories into every family × profile × interruption combination. The
profile plan below applies to implemented capabilities at their stated stages;
it is not a reason to rerun the accepted 0.4 matrix. A successful load alone does not
qualify mechanics or rider synchronization.

| Gate | Required evidence for each advertised integration |
| --- | --- |
| Dependency/load | Exact files and runtime mod IDs/versions; resolved required dependencies; standalone and dedicated-server startup; addon absent and unsupported-version behavior |
| FD901 mechanics | Accepted original-family matching/rejection plus each added family/variant/provider contract through placement, assembly, snaps, re-engagement, transitions and dispatch; locked-seat rejection; ordinary carts receive no 4D behavior; declared consist/geometry constraints |
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
results and limitations. Classify findings against a named required criterion
using [VALIDATION-POLICY.md](VALIDATION-POLICY.md#gate-blockers-and-follow-up-work);
nonblocking polish goes to follow-up notes. New failures reopen only the affected case; the release
may be blocked by that case while other acceptance stays closed. Public status should
progress from **staged → inspected → adapter implemented → bounded tests → certified
for a named profile**. Existing evidence stays bounded until the remaining gates pass.

### Execution sequence and delivery boundary

1. Preserve accepted FD401 and FD402A/C/D results; finish only the open 0.4 recovery/player cases.
2. In 0.5, resolve new-family specifications and independent FD501 dependency decisions. Wing proves contract changes with an occupied client handoff; FD501A handles core-required upgrades/migration and FD904C1 closes baseline comfort. Adapter-specific prerequisites follow their consumers through FD501B–H.
3. Advance operations adapters after their own dependency and integrated control/provider prerequisites. They need not wait for the complex-family milestones; finish them and actual conflict-pair coverage in 0.8.
4. Inspect coexistence early. Complete presentation/seat adapters in 0.9 against representative stable mechanisms, with player guidance supplied by each feature owner.
5. Freeze the full scope at FD1000, reuse accumulated evidence through FD901–FD905 (including FD904C2 camera reconciliation), and complete final documentation/packaging through FD1001–FD1003.
6. Publish only when separately authorized. FD801 stays independently conditional on inspectable upstream functionality.

No integration coding, dependency activation, compatibility testing, version bump,
merge of implementation code or publication is performed by this planning update.
