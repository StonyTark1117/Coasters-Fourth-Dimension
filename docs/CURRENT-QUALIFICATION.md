# Current qualification record

Updated 2026-11-15 UTC. This is the single artifact index for the active roadmap.
The selected build is a **qualification baseline, not an accepted release candidate**.
FD404 is complete as the 0.4 five-family development checkpoint; FD905 remains
open for 1.0 release qualification. A later source change has its own identity,
but unaffected acceptance carries forward under [VALIDATION-POLICY.md](VALIDATION-POLICY.md).
Do not reselect the baseline or repeat a run just because the commit/JAR changed.

Planning allocation updated 2026-11-11: FD404 is the five-family development
checkpoint; FD1000 owns the expanded 1.0 alpha freeze. The identities and runtime
observations below are unchanged by roadmap adoption. New implementation records
will carry unaffected acceptance forward; no new JAR or run is required here.

## Current work queue

Use the [milestone execution guides](roadmap/README.md) for package procedures
and handoffs. This queue retains active ownership and next actions; the guides
do not create another tracker or reopen cases. A next action may reference a
package step without adding every future step to this queue.

**Milestone 0.4: complete 2026-11-15.** FD401A–D and FD402A–F remain closed;
FD403A–F and the FD404 aggregate are now reconciled. There is no remaining 0.4
acceptance queue. The sections below retain case closure records and artifact
history; they do not assign new work or add acceptance requirements.

### Active 0.8 assignments — 2026-11-15

The human's **Continue .8** instruction in coordinator chat
`01a1a961-64bb-7701-9a19-2acee5ff23a3` authorizes all 23
[0.8 execution cards](roadmap/0.8-operations.md), normal selective commit/push,
existing-owner coordination and final runtime cleanup. It supersedes historical
no-0.8 directions in completed 0.7 records. Baseline `main`
`cb46ea072d2c731948e4cac07e4d5350e269dbc5` is verified on origin/main, with
only pre-existing untracked `logs/`; all 0.7 runtimes are stopped.
Closed 0.4–0.7 cases remain closed. No 0.9, 1.2, moving-track implementation or
publication is activated.

| Card | Owner | Current action / dependency |
| --- | --- | --- |
| FD501B | Lead | Complete: exact Additions fingerprint and Aeronautics/Interiors closure loaded in targeted FD810 native run; no core upgrade/migration. [Disposition](evidence/0.8/dependencies/README.md#fd501b--complete-no-core-change). |
| FD501C | Lead | Complete: selected artifact rehashed; current core sufficient, no baseline upgrade/migration. [Disposition](evidence/0.8/dependencies/README.md#fd501c--complete-no-core-change). FD811 retains capability qualification. |
| FD501D | Lead | Complete: selected artifact rehashed; current core sufficient, no baseline upgrade/migration. [Disposition](evidence/0.8/dependencies/README.md#fd501d--complete-no-core-change). FD812 retains capability qualification. |
| FD810 | Lead | Complete: [actual-addon native guards and normal station UI/occupied auto-dispatch](evidence/0.8/additions/README.md). All lead316 units inactive; saved banked stopped world retained. Additional drive conflicts remain FD813. |
| FD811 | Lead | [Native controls and actual provider joins passed](evidence/0.8/ctnb/README.md); connected-client construction/control pending on owned316. |
| FD812 | Lead | Complete: [actual Wing/S2 optional force and normal occupied operator evidence](evidence/0.8/magnetized/README.md). All lead316 units inactive; saved unloaded world retained. Installed combined conflicts remain FD813. |
| FD1701A | Lead | Qualified: targeted native 1/1 plus normal occupied Claim/dispatch/held transfer/receiving dispatch/unload passed. [Evidence](evidence/0.8/authority/README.md). World saved; client/server/display316 inactive. Complete; integrated with the coherent shared implementation checkpoint. |
| FD1401A | Pipeline / Dive | Complete: [qualified native and occupied handoff](evidence/0.8/pipeline/README.md), including full forward/reverse circuits, inclined stop/save/reload/resume, dock/open/unload. Same two IDs; all317 units inactive. B/C reuse this evidence. |
| FD1401B | Pipeline / Dive | Complete: [normal crafted content and finite visual review](evidence/0.8/pipeline/construction/README.md); P-only rail support saddle inspected with no mechanics replay. |
| FD1401C | Pipeline / Dive | Complete: crafted track/cars, paid span, native Chain coupling and whole-consist dock binding combine with accepted A occupied forward/reverse/recovery/unload. All317 units inactive; owner continues Dive. |
| FD1501D | S2 / Powered Inverted | Complete: [native governor, four-car inversion/rollback/loss and occupied handoff](evidence/0.8/spinning-s2/README.md#occupied-mechanics-handoff--fd1501d). Launch/roll/return/Capture/Open/unload passed; all318 units inactive. E/F reuse accepted mechanics. |
| FD1501E | S2 / Powered Inverted | Complete: [crafted content, paid construction, controls and finite operating views](evidence/0.8/spinning-s2/content/README.md). |
| FD1501F | S2 / Powered Inverted | Complete: content occupied cycle/capture/open/unload and actual restart combine with accepted D mechanics. All318 units inactive; saved empty world preserved. |
| FD1702A | Pipeline / Dive | Native hold/drop/recovery and occupied return/unload passed; owner reconciling content and evidence. |
| FD1702B | Pipeline / Dive | Prepare fixed-seat Dive content and hold/release example. |
| FD1702C | Pipeline / Dive | Complete normal hold/drop/return/unload workflow cumulatively. |
| FD1702 | Pipeline / Dive | Reconcile full Dive A–C outcome. |
| FD1901A | S2 / Powered Inverted | Native single/four-car travel/yaw/show/reverse/capture and actual power/controller-loss passed; connected-client workflow active on owned318. |
| FD1901B | S2 / Powered Inverted | Prepare powered drive/yaw content and scene-event controls. |
| FD1901C | S2 / Powered Inverted | Complete travelling/stop/reverse/unload workflow cumulatively. |
| FD1901 | S2 / Powered Inverted | Reconcile full Powered Inverted Yaw A–C outcome. |
| FD1902 | S2 / Powered Inverted | Reconcile Powered Inverted outcome gate. |
| FD813 | Lead | Integrate all 23 cards, actual combined-control conflicts, exact remote HEAD and all-runtime cleanup. |

**Owners:** lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d`;
Pipeline/Dive `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`;
S2/Powered Inverted `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`.
Use these existing chats only; no new chats/subagents. Lead alone edits central
ROADMAP/PLAN/queue and stages, commits and pushes. Stay in primary `main` and
preserve `logs/` and other owners' work; no archive worktree is a task baseline.

**Initial file ownership reset:** lead owns ALL existing production/runtime/
source/editor/registration/build/generator files, including former 0.7 transfers
(FoundationSync, EditorNetwork, RotationEditorScreen, LocalRiderRenderMixin,
Flying* and RiderComfortMixin). Existing-file changes require an exact transfer
recorded here and acknowledged before editing. Lead responds to shared-hook
requests promptly; one writer per file. Other owners may create package-specific
new classes/resources/tests/generators/guides/evidence: Pipeline*/Dive* and
`pipeline_*`/`dive_*`, `fourthdimension_fd1401*`/`fourthdimension_fd1702*`,
`generate-pipeline.py`/`generate-dive.py`, their guides and evidence directories
belong to Pipeline/Dive; S2Spinning*/PoweredInvertedYaw* and
`s2_spinning_*`/`powered_inverted_yaw_*`, `fourthdimension_fd1501_s2*`/
`fourthdimension_fd1901*`, their new generators/guides/evidence belong to
S2/Powered Inverted. No prior existing-file ownership silently carries forward.

**Explicit 0.8 transfers (acknowledged 2026-11-15):** Pipeline/Dive owns
`src/main/java/dev/fourthdimension/client/OriginalTrackRenderer.java` solely for
Pipeline P gauge/support placement until it hands that file back. Lead retains
`CoasterCartRenderMixin` and all other existing files. S2/Powered owns new
`S2SpinningGovernorBlock`, `S2SpinningGovernorEntity` and
`S2SpinningGovernorContent` in its package: the stress-supplied kinetic Chain
source speed/ramp/clutch consumer. Lead owns the shared validated authority
seam (owner, hold/release/dispatch and stale-request rejection); the governor
consumes it without replacing native Chain force with direct velocity writes.


**Additional Pipeline integration transfer — 2026-11-15:** acknowledged by the owner, Pipeline owns these existing files for Pipeline hooks only:
`contract/Family.java`, `track/TrackDesign.java`, `vehicle/VehicleFactory.java`,
`vehicle/VehicleParts.java`, `vehicle/ConsistRules.java`,
`runtime/EligibilityHooks.java`, `vehicle/Boarding.java`,
`vehicle/NativeLoading.java`, `FourthDimension.java` (all under
`src/main/java/dev/fourthdimension/`) and
`src/main/resources/fourthdimension.mixins.json`. Pipeline handed all these files back on 2026-11-15; lead now owns them and
adds S2 hooks. OriginalTrackRenderer remains with Pipeline. Lead retains FoundationData,
CarrierRuntime, VehicleOperations, BoardingStation/Entity, EditorNetwork,
RotationEditorScreen and FoundationSync.

**Runs and handoffs:** lead reserves persistent root
`/mnt/NVME4/fourthdimension-runs/fd08-operations-20261115`, display316,
ports25650/25651, prefix `fd08-operations-20261115` after checking live ownership.
Pipeline/Dive may reserve display317 and ports25652/25653; S2/Powered Inverted
may reserve318 and25654/25655. Each records its exact world/game/build paths and
units before launch. Java21, persistent storage, measured host/cgroup headroom,
1536M game heap and6GiB Minecraft-only scope apply. Native tests need no display.
Normal input must reach actual game handlers; verify connection/window/native hit,
use the proven private helper and diagnose two inconclusive attempts before retry.
A/D occupied proof feeds C/F; changed synchronization alone may need two clients.
The qualified authority A handoff is integrated and relayed immediately without
waiting for adapter/content polish. Dependent owners may prepare content earlier.

**Completion:** integrate qualified handoffs, then full outcomes through FD813;
reuse accepted native/player/content/persistence evidence by behavior. Do not
waive missing required adapter capabilities or invent full1.2 block management
as a prerequisite. Save/stop every owned client/server/Xvfb and preserve evidence;
only exact accepted continuing-owner handoffs permit a live runtime. Verify
selective commits/push, exact remote HEAD and cleanup before closing 0.8.

### Milestone 0.7 — complete 2026-11-15

The human explicitly requested **Continue 0.7** in the coordinator chat. This
supersedes the completed 0.6 assignment and its historical no-0.7 directions.
Complete all sixteen [0.7 execution cards](roadmap/0.7-complex-motion.md) under
canonical ROADMAP criteria, using the same existing owners, monitoring,
integration/commit/push and cleanup workflow. Accepted 0.4–0.6 cases remain closed
unless a named required criterion is invalidated. No new chats/subagents, 0.8
implementation or publication. The user authorizes coordination/continuation
messages among these three owners and coordinator `01a1a961-64bb-7701-9a19-2acee5ff23a3`.

Primary checkout `/home/braydon/Documents/Coasters: Fourth Dimension`, existing
`main`, dispatch baseline `b8db0f06b566ed5b465c758c005db11d5c7b9837`, verified on
origin/main. Preserve pre-existing untracked `logs/` and all owners' changes.
Lead alone edits ROADMAP/PLAN/this queue and stages, commits and pushes.

| Card | Owner | Current action / dependency |
| --- | --- | --- |
| FD1302A | Lead | Complete: [native and occupied handoff](evidence/0.7/axis/A-handoff.md), including return/unload and targeted dock-edge recovery. Integrated at `2b805fcb`; same-owner B/C continuation finished and exact :316 runtime saved/stopped. |
| FD1302B | Lead | Complete: [normal crafting, placement, editor and finite visual review](evidence/0.7/axis/BC-acceptance.md). |
| FD1302C | Lead | Complete: cumulative A+B workflow, all owned runtimes saved/stopped; no duplicate ride. |
| FD1502A | Lead | Complete: [native/occupied/summit restart and look correction](evidence/0.7/roller-ball/A-handoff.md); all owned runtime units saved/stopped. |
| FD1502B | Lead | Complete: [recipes, normal construction/Chain/controls and finite asset review](evidence/0.7/roller-ball/BC-acceptance.md). |
| FD1502C | Lead | Complete: cumulative A+B, normal full circuit/capture/unload, actual summit restart and targeted reconnect look. |
| FD1801A | Conventional Flying | Complete: [native and occupied handoff](evidence/0.7/conventional-flying/README.md), actual occupied transition save/restart and targeted offline-COM correction passed; A runtime saved and stopped. |
| FD1801B | Conventional Flying | Complete: [recipes, construction, controls and finite visual review](evidence/0.7/conventional-flying/construction/README.md). |
| FD1801C | Conventional Flying | Complete: construction combined with accepted occupied/restart workflow; all owned units saved/stopped. |
| FD1801 | Conventional Flying | Complete independently from accepted A–C; supplied the integrated prerequisite for New-generation Flying. |
| FD1802A | New-generation Flying | Complete: [qualified native/occupied/shared-yaw handoff](evidence/0.7/new-generation-flying/QUALIFIED-A.md), integrated at `3de3d128`; same-owner B/C continuation and cleanup now complete. |
| FD1802B | New-generation Flying | Complete: [normal recipes, paid track/car/marker/Chain/editor and finite content review](evidence/0.7/new-generation-flying/construction/README.md). |
| FD1802C | New-generation Flying | Complete: cumulative A+B, both-posture targeting, full roll, loaded Show, return/unwind/unload and distinct persistence; all owned units saved/stopped. |
| FD1802 | New-generation Flying | Complete from qualified A–C and final content/cleanup handoff. |
| FD1803 | New-generation Flying | Complete: both Flying outcomes reconciled independently; no joint replay. |
| FD701 | Lead | Complete: [all sixteen cards and four families](evidence/0.7/acceptance.md), integrated on primary main with verified cleanup; 0.8 not activated. |

**Owners:** lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d` (Axis, Roller Ball,
integration); Conventional Flying `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`;
New-generation Flying `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`.

**Initial exact file ownership:** lead owns existing shared production, registration,
serialization, editor/network/runtime paths, shared generators and `en_us.json`.
Lead also owns Axis*/RollerBall* new files/resources/tests/generators/guides and
`docs/evidence/0.7/{axis,roller-ball}/` plus `scripts/fd-private-client.py`.
Conventional Flying may immediately implement new
`src/main/java/dev/fourthdimension/vehicle/Flying*.java`,
`gametest/FlyingGameTests.java`, `src/test/java/dev/fourthdimension/Flying*Test.java`,
new resources with `flying_` basename under assets/fourthdimension and recipes,
`src/main/resources/data/fourthdimension_fd1801/`, `scripts/generate-flying.py`,
`docs/FLYING.md` and `docs/evidence/0.7/conventional-flying/`.
Conventional also owns the entire `src/main/java/dev/fourthdimension/mixin/RiderComfortMixin.java`
and new `src/main/java/dev/fourthdimension/mixin/Flying*Mixin.java` paths,
explicitly transferred on 2026-11-15. `LocalRiderRenderMixin` remains lead-owned.
It sends required shared hooks/fields to lead for prompt integration; no simultaneous
writers. Any additional exact transfer is recorded here before edits.
**New-generation production transfer (Conventional A integration, 2026-11-15):**
New-generation owns new `NewGenerationFlying*.java` family/runtime/client/physics/
track classes, new `Yaw*.java` state/controller/marker classes, its GameTests and
unit tests, new `new_generation_flying_*` and `yaw_*` resources, namespaces
`fourthdimension_fd1802*`, `scripts/generate-new-generation-flying.py`,
`docs/NEW-GENERATION-FLYING.md` and its evidence directory. Exact existing shared
files transferred exclusively to that owner: `runtime/FoundationSync.java`,
`editor/EditorNetwork.java`, `client/RotationEditorScreen.java` and
`mixin/LocalRiderRenderMixin.java` under `src/main/java/dev/fourthdimension/`.
The lead will request its Roller Ball additions to those files through that owner.
Lead retains other existing shared files, including FoundationData, CarrierRuntime,
VehicleFactory/Parts, contracts, NativeLoading, registration, language and mixin
configuration; New-generation sends concrete required hooks for lead integration.
Conventional retains Flying* and RiderComfortMixin; coordinate any pure reuse seam
with that owner. No simultaneous writers or runtime inheritance.
Do not wait for unrelated Axis/Roller acceptance to implement Conventional Flying.
Qualified Conventional A passes to New-generation before B/C decorative completion.

**Build/runtime coordination:** both active implementation owners announce a coherent
source checkpoint before packaging while the other is editing; use distinct persistent
build outputs and never consume an unfinished prerequisite. No live runtime is
inherited. Lead reserves root
`/home/braydon/.local/state/fourthdimension-runs/fd1302-axis-20261115`,
Its initial native worlds remain there for clean save; future lead build/client/server
root is `/mnt/NVME4/fourthdimension-runs/fd1302-axis-20261115` on a separate
persistent SSD after measured root-device IO stalls.
Private display316, ports25650/25651, units `fd1302-axis-20261115-{server,client,xvfb}.service`.
Conventional may reserve display317/ports25652/25653 and New-generation318/25654/25655
only after recording their exact roots/units and checking live ownership/headroom.
Java21, default1536M heap/6GiB Minecraft-only limit, host/cgroup checks and
persistent storage apply. Reuse proven private input; one expected result, one
input, actual connection/hit/reach/state readback. Diagnose twice-inconclusive
approaches before changing the demonstrated failed condition. Normal native Chain
input must be covered separately from direct link primitives where newly affected.
Keep motors clear of both rider and running-gear sweep; use bounded explicit UVs
and selection shapes matching geometry. Save and stop finished/abandoned owned
runtimes, verify inactive, preserve worlds/logs. Only explicit exact continuing-owner
handoff permits a live runtime after package handoff.

**Final closure:** [FD701 acceptance](evidence/0.7/acceptance.md) closes all sixteen
cards. Qualified mechanics were integrated early, followed by the complete
family outcomes. The `3de3d128` mechanics checkpoint is verified on origin/main;
the commit containing this final record integrates the remaining content and
aggregate closure. All owned clients, servers, observer units and private displays
are saved/stopped. No active 0.7 assignment remains; 0.8 and publication are not
activated. Pre-existing untracked `logs/` is preserved.

### Milestone 0.6 — complete 2026-11-15

The user authorized completing **0.6 with the same assignment, monitoring and
commit/push workflow**. Follow [all eleven 0.6 execution cards](roadmap/0.6-motion.md).
The canonical criteria and dependencies are unchanged. FD401–FD404 and all
completed 0.5 outcomes remain closed; FD503 is the integrated starting input.
S2 Spinning, 0.7+ implementation and publication are not activated.

Checkout: `/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`,
dispatch HEAD `d73d43b820b725fc9648c6a2daa9348dead16b22`. Fixed Inverted A was
integrated at `7a563e6d006fff8d80221f6e7d139b42bda452a8`, pushed and directly
verified on origin/main. This final reconciliation integrates B/C and its narrow
Chain input correction. Preserve the pre-existing untracked `logs/`. No new worktree.

| Complete assignment | Owner / chat | Ready work and prerequisites | Evidence / handoff |
| --- | --- | --- | --- |
| **FD1501A–C; FD601** | **0.6 lead — Spinning and integration**, `01a1dd1d-1468-7f81-8f31-5532ad7a178d` | S1 A–C and FD601 complete. All eleven 0.6 cards are reconciled; no remaining assigned work. | `docs/evidence/0.6/spinning/`; lead owns central ROADMAP/PLAN/queue and all Git integration/commit/push. FD601 aggregates the three accepted families without another campaign. |
| **FD1301A–C** | **0.6 Suspended**, `01a1dd1d-6e99-70d2-aef3-6dcebac10d72` | A–C complete. A integrated at ec27e982; B/C construction/content and runtime cleanup accepted below. No remaining Suspended run or source work. | `docs/evidence/0.6/suspended/`; qualified overhead and occupied A handoff consumed by Fixed Inverted; B/C reuses it. |
| **FD1601A–C; FD1601** | **0.6 Fixed Inverted**, `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da` | A–C and FD1601 complete. Normal crafting, construction, eight-block Chain coupling, content and cleanup are accepted; no remaining assigned work. | `docs/evidence/0.6/fixed-inverted/`; FD1601 reconciles its accepted A–C outcome, then feeds FD601. |

**Final closure:** [FD601 acceptance](evidence/0.6/acceptance.md) combines the
three families' native, normal-input, content and persistence evidence. Fixed
Inverted's [B/C record](evidence/0.6/fixed-inverted/BC-acceptance.md) closes its
outcome and the shared eight-block Chain input correction. All S1, Suspended and
Fixed Inverted clients, servers and private displays are inactive; worlds and
logs are retained. Fixed saved all dimensions at07:10:48UTC and stopped its final
unit by07:11:18UTC. Source ownership is returned to the lead; no family has pending
edits or a live runtime. Only unrelated untracked `logs/` remains outside this work.
No 0.7, release qualification or publication assignment is activated.

The assignment and handoff notes below are historical. Their earlier continuation
instructions are superseded by this closure and do not reopen work or runtimes.

**Shared-file ownership:** initially the lead owns all shared production source,
registration, asset generators and common resources. Other owners may write their
own evidence/preparation directories immediately. Before production edits by a
dependent owner, the lead records an exact file/path ownership transfer here,
including new family files and resources. One writer per shared file at a time;
the former owner stops edits to transferred files until returned. Integrate the
prerequisite contract into main before dependent implementation; do not build
against an unfinished checkout. A handoff can occur after A without waiting for
unrelated decorative polish or C reconciliation. The lead alone stages/commits/
pushes and edits central status documents; family owners supply ready evidence.
The user authorizes specific coordination/continuation messages among these three
owners and from the coordinator, including resuming a ready dependent owner.

**FD1601A complete — 2026-11-15.**
[Mechanics and occupied handoff](evidence/0.6/fixed-inverted/A-handoff.md)
close the fixed-seat native contract, four-car full inversion, banked save/reopen,
explicit recovery, dock unload and camera restoration. Fixed Inverted's owner
continues B/C on the same world `server/FixedInvertedWorkshop`, display314,
ports25646/25647 and `fd1601-fixed-inverted-20261115-{client,server,xvfb}.service`;
the root is `/home/braydon/.local/state/fourthdimension-runs/fd1601-fixed-inverted-20261115`.
This is an active same-owner continuation, not an idle completed runtime.
Accepted native and occupied cases carry forward; no 0.7 consumer is activated.
Shared source ownership remains with Fixed Inverted for any coordinated correction.

**Targeted player-coupling correction — closed 2026-11-15 07:07 UTC.** Normal Chain input reached both
correct Fixed Inverted bearings, but native `CoasterCartLinkPlacement` rejects
eight-block spacing before the family eligibility adapter. The native fixture
called the lower-level link primitive and did not cover this player guard.
The required normal construction/coupling criterion was isolated under FD1601C;
the same supported eight-block interaction reopened only that case under FD1301C.
All accepted native loading, rides, persistence and other content remain closed.
Fixed Inverted owns the correction and also receives exact registration-file
ownership of `src/main/resources/fourthdimension.mixins.json`. Retain ordinary
native distance limits and all family/readiness/consist checks. One affected
normal coupling proof with scoped shared-path review now closes the common adapter
case: the server linked both normally placed Fixed Inverted cars at distance8.000
at07:07:21UTC, the held Chain selection cleared, and the preview changed from
blocked to eligible. The shared helper admits only Fixed/Suspended pairs that pass
the existing family, variant, exact-distance, readiness and maximum-four guards;
other inputs delegate to unchanged native rules. FD1301C is fully closed again.
This does not request another family ride or qualification campaign.

**FD1301A–C complete — 2026-11-15 06:24 UTC.**
[Content, normal construction and cumulative acceptance](evidence/0.6/suspended/BC-acceptance.md)
close Suspended. Crafting, anchor/track/car/station placement and binding, lift
setting controls, connected journal, bounded texture UVs and state views pass.
The owner saved all dimensions at06:24:07UTC; client/server/Xvfb are inactive with
MainPID0 and display312 absent. The retained-runtime note below is now historical;
no live Suspended runtime is handed off. Fixed Inverted retains current shared
file ownership and is the only remaining family outcome for FD601.

**FD1301A handoff — 2026-11-15 06:01 UTC:**
[Suspended mechanics and four-car occupied cycle](evidence/0.6/suspended/A-handoff.md)
are complete. Native load/limits, train lift power loss/resume/release, pending
entry, capture/save and guarded dock rebind pass. Normal occupied mount, closed
departure, lift retention, free swing, Capture/Ready/Open and Shift unload pass.
The lead integrates this qualified handoff on main before resuming Fixed Inverted.
The connected-journal correction also passed its affected mass/loading, swing-bound
and capture/save check. Suspended B/C retain remaining content review and normal
construction; they do not reopen accepted A or delay its consumer.

**Historical production ownership during integration:**

- **Fixed Inverted** (`01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`) owns
  `src/main/java/dev/fourthdimension/**` except `vehicle/Spinning*.java`,
  `vehicle/Suspended*.java`, `gametest/SpinningGameTests.java` and
  `gametest/SuspendedGameTests.java`. It owns shared `lang/en_us.json`, new
  Fixed-Inverted-only resources/tests/generator/guide and its evidence directory.
- **Suspended** retains `vehicle/Suspended*.java`, `gametest/SuspendedGameTests.java`,
  Suspended-only assets/recipes/GameTest resources, `scripts/generate-suspended.py`,
  `docs/SUSPENDED.md` and its evidence directory for B/C. Shared-file fixes require
  coordination with the new owner; no simultaneous shared edits.
- **Lead** retains S1-only files, `scripts/fd-private-client.py`, central status
  documents and all Git staging/commit/push. Prior shared-path transfer to Suspended
  below is historical and superseded by this transfer.

Suspended explicitly retains root
`/home/braydon/.local/state/fourthdimension-runs/fd1301-suspended-20261115`, world
`server/SuspendedWorkshop2`, display `:312`, ports 25642/25643 and units
`fd1301-suspended-20261115-{client,server,xvfb}.service` for continuing FD1301B/C.
Fixed Inverted receives no runtime; reserve independent resources after checking
headroom. Final family completion requires saved worlds and inactive owned units.
The lead sends the exact integrated HEAD in the continuation message.

Fixed Inverted reserved its independent root
`/home/braydon/.local/state/fourthdimension-runs/fd1601-fixed-inverted-20261115`,
display `:314`, game/RCON ports 25646/25647 and units
`fd1601-fixed-inverted-20261115-{client,server,xvfb}.service`. Its owner rechecks
host/cgroup headroom before launch; this reservation does not transfer Suspended's run.

**S1 FD1501A–C complete — 2026-11-15 05:34 UTC.**
[Construction, content and power-loss/resume acceptance](evidence/0.6/spinning/player/README.md)
closes S1. Its A handoff `d5565a09ac0d6230b66c9dd332c7b35fb3840f2a` is pushed
and directly verified on origin/main. The lead now owns FD601 reconciliation and
integration of the remaining families. All S1 runtime units are inactive and
its world/evidence retained. Fixed Inverted now owns the shared paths under the current transfer above;
Suspended continues B/C in its family-only paths. Do not reopen S1
because a later family changes unrelated code or produces a new JAR.

**FD1501A handoff — 2026-11-15 05:14 UTC:** S1 mechanics and the normal
occupied cycle are [accepted](evidence/0.6/spinning/README.md). Native test and
twelve targeted math tests pass; occupied mount, lift/free motion, guarded stop,
capture, open and unload pass. All owned runtimes are stopped and worlds retained.
B/C subsequently closed in the record above. The original five and Wing stay closed.

Upon integration of this handoff on `main`, production ownership transfers to
**0.6 Suspended** (`01a1dd1d-6e99-70d2-aef3-6dcebac10d72`) for:

- `src/main/java/dev/fourthdimension/**`, except the lead-retained
  `vehicle/Spinning*.java` and `gametest/SpinningGameTests.java`.
- Shared `src/main/resources/assets/fourthdimension/lang/en_us.json` and new
  Suspended-only blockstates, models, textures, recipes and GameTest resources.
- New Suspended-only test source, asset generator and player guide; its existing
  `docs/evidence/0.6/suspended/**` remains its own.

This is a single writer transfer of the shared Java/registration/runtime paths;
the lead stops editing them until returned. The lead retains S1-specific files,
`scripts/fd-private-client.py`, all central status and Git operations. Existing
S1/original/Wing resources remain unchanged by this transfer. Fixed Inverted still
waits for integrated Suspended A and the next explicit shared-file transfer.
The lead sends the exact integration HEAD in the continuation message; no new
branch, worktree or runtime is handed off.

**Execution and monitoring:** complete compatible steps through the whole assigned
outcome. Each owner keeps a compact current checkpoint: FD IDs, verified state,
exact next action, integrated prerequisite commit, current file ownership, failed
condition/changed precondition and runtime identity. Start there after compaction;
old 0.4/0.5 goals and pending prose are history. Internal prerequisite waits are
valid; use independent preparation, then consume the ready handoff. No new chats
or subagents are needed for these assignments.

The coordinator monitors every five minutes. Two inconclusive attempts require
diagnosis before another attempt. Two checks with no concrete progress trigger
inspection, not an automatic restart of useful long operations. Intervene with
one changed actionable step and verify substantive action; avoid stacked prompts.
Accepted mechanics/content/interaction/persistence evidence carries independently.
No fresh hash, fixture or document change restarts acceptance. Name the violated
required criterion before reopening a specific regression, then close it after
the targeted fix passes. Native checks prove dynamics; occupied clients prove
missing input/rendering claims. Use two real clients only for a changed or still
uncovered synchronization claim. Gates reconcile evidence, never add test sweeps.

**Input and runtime safeguards:** use current AGENTS Java21, persistent storage,
1536M heap and 6GiB Minecraft-only scope defaults with measured adjustments.
Coordinate graphical runs with the lead; verify CPU/memory/cgroup headroom and
distinct owned display, world, port and scope. Reuse `scripts/fd-private-client.py`.
Confirm connection, geometry, actual interaction hit, reach and expected readback
before input. F3 alone is insufficient: 0.5 proved a track curve can replace the
interaction ray with MISS, and rendered seat backs can exceed cushion hit shapes.
Never click after an actual MISS. Calculate from current body/seat geometry after
a miss; diagnose before another guess. Internal setup/source defects are authorized
work, not permission requests. Save/stop owned clients, servers and Xvfb when done,
verify inactive, and preserve worlds/evidence. Long saves require I/O/state diagnosis;
do not kill a progressing save or repeatedly launch copies. Only a named prerequisite
unresolvable with authorized tools/access is external; report exact human action once.

**Completion:** lead reconciles FD601, integrates all accepted changes, commits and
pushes main normally, verifies remote HEAD and owned-runtime cleanup. Coordinator
then disables the monitor and reports completion. Do not activate 0.7 automatically.

### Milestone 0.5 — complete 2026-11-15

All nineteen assigned cards are reconciled complete in [FD503 acceptance](evidence/0.5/acceptance.md).
There is no remaining 0.5 queue or owned runtime. The user activated 0.5 only;
this closure does not activate 0.6–0.9 implementation. The table retains ownership
and accepted handoffs for reuse. Historical next actions below are not assignments.

Checkout: `/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`,
dispatch HEAD `a4ef96d775b389ae0a5e608ac80a37533c2592ac`. Existing dirty source,
documentation and untracked evidence are preserved. The new lead first inventories
and reconciles ready earlier changes, including evidence linked by the 0.4 closure;
this Git work does not reopen accepted cases or require a new artifact campaign.

| Complete assignment | Named owner / chat | Ready work and handoff | Scope boundary |
| --- | --- | --- | --- |
| **FD1101; FD501A; FD1101A–C; FD904C1; FD503** | **0.5 lead — Wing and shared runtime**, `01a1dd1d-1468-7f81-8f31-5532ad7a178d` | Complete: FD1101/501A/1101A–C/904C1 and FD503. Native, normal construction/coupling, occupied banked lifecycle, persistence and comfort evidence reconciled. All owned client/server/Xvfb services inactive with successful save/exit; world retained. No next runtime action. [Wing acceptance](evidence/0.5/wing/README.md), [comfort](evidence/0.5/comfort/README.md), [aggregate](evidence/0.5/acceptance.md). | Sole shared production-runtime, camera, asset/registration and Git integration owner. No speculative framework or later-family implementation. Wing C reuses A/B evidence. FD503 is reconciliation, not another run. |
| **FD1501, FD1301, FD1302, FD1502, FD1401, FD1601S, FD1702S, FD1801S, FD1802S, FD1901S** | **0.5 vehicle specifications**, `01a1dd1d-6e99-70d2-aef3-6dcebac10d72` | All ten assigned specifications reconciled complete; [completion mapping](evidence/0.5/specifications/acceptance.md) and analytic dimensions/heading/link evidence reviewed. No remaining specification work. Later A/B/C implementation remains planned. | Specifications and necessary feasibility proof only. Wing specification belongs to the lead. No production runtime changes without explicit bounded file ownership transfer. Both Flying variants and physical scenery yaw retain their adopted scope. |
| **FD501; FD502** | **0.5 dependency decisions and test tooling**, `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da` | FD501 and FD501A decisions are reconciled complete; the 14-artifact catalog is retained. FD502 complete: finalized native frame/tick/heap/GC evidence accepted; owned-JVM sampler correction verified. No remaining assigned tooling work. Current handoff: `docs/evidence/0.5/dependencies-tooling/`; dependency decisions also in `docs/INTEGRATION-BACKLOG.md`. | FD501A belongs to the lead; FD501B–H implementations remain later-stage work. Existing FD502 repairs remain accepted. Existing sufficient tools can close the remaining disposition by review. Shared runtime edits require an explicit file handoff. |

**Completed FD502 bounded handoff (historical):** The dependency/tooling owner may create only
`src/main/java/dev/fourthdimension/runtime/RuntimePerformanceRecorder.java`
for the opt-in frame/tick/heap/GC collector, plus its dedicated scripts/evidence.
Lead retains `FourthDimension.java` registration and all other shared runtime files.
The first useful Wing run included the accepted collector event-delivery smoke.

**Integration and communication:** After this dispatch, the 0.5 lead owns canonical
`ROADMAP.md`, `PLAN.md` and queue reconciliation, selective staging/commit/push,
and shared runtime edits. Other owners write their separate package evidence and
report ready paths/decisions; the coordinator relays concrete handoffs. Avoid
concurrent edits to these central files. Dependent code consumes integrated `main`
contracts, not another owner's unfinished changes. The coordinator is
**Audit and improve roadmap**, `01a1a961-64bb-7701-9a19-2acee5ff23a3`.

**Continuation and monitoring:** Every owner keeps a compact current checkpoint
at the top of its evidence record: assigned FD IDs, last verified state, exact
next action, retained evidence, failed condition/changed precondition if relevant,
and any owned runtime identity. This is a handoff, not another assignment tracker.
After context reset, start here and at that checkpoint; do not revive historical
FD403 goals, audit already-closed families, or reread entire historical logs.
Continue through compatible steps until the existing outcome is complete.

The coordinator checks these three owners every five minutes for substantive
actions and evidence. Two inconclusive attempts with the same approach require
diagnosis before retry. If two checks show the same unresolved action without
concrete progress, inspect the actual logs/code/state before intervening; elapsed
time alone does not invalidate a long-running useful operation. Intervene on
repeated setup/aim/connection failures, stopped unfinished work, duplicated
acceptance, scope drift, demonstrated regressions or abandoned runtimes. Send one
specific changed action, then verify the resulting action/evidence; do not stack
unchanged prompts while work is progressing. A new JAR does not reopen a case.
Name the required criterion and affected change before recording a regression;
close it after its targeted fix passes without restarting siblings.

An internal dependency is a named prerequisite owned by this team, not an external
blocker. A setup error or defect within authorized ownership is work to diagnose
and fix. Call a dependency external only when authorized tools/access cannot
resolve the named prerequisite; identify the exact human action once and continue
independent work. A valid prerequisite wait is not abandonment or permission to
start unrelated tasks.

**Runtime ownership:** No graphical runtime is assigned by this dispatch. Record
owner, live scope/PIDs, display, port, world/game directory and artifact before a
run; verify connection, geometry and native target/readbacks before input. Reuse
`scripts/fd-private-client.py` and current AGENTS Java/storage/memory rules. Check
host CPU/memory and cgroup headroom, including other owners' jobs. Coordinate
graphical work with the lead by default; independent private runs still require
separate owned resources and adequate measured headroom. Save and stop completed
or abandoned client/server/display services, verify inactive, and preserve worlds
and evidence; an explicit continuing-owner handoff is the exception. Never stop
another owner's process based only on its display number or age.

FD503 and required runtime cleanup are now verified; the 0.5 monitor stop condition
is satisfied. This record does not activate 0.6.

### Historical 0.4 queue and closure records

Case IDs below are stable children of their owning roadmap task. Open rows identify
remaining acceptance work; completed rows are retained as closure records. **Unrecorded**
means this document does not establish a current owner, not that no other agent is
working. Confirm/claim ownership before a run. The gap
column states the required outcome; the next action starts from retained evidence.
A case may close by evidence reconciliation without launching Minecraft.

**Choose the assigned package:** [FD402 recovery cases](#fd402-recovery-cases)
cover ordinary-bearing towing interruptions; [FD403 construction and teaching](#fd403-construction-and-teaching-cases)
cover normal player-built rides and walkthroughs. FD403 does not depend on FD402,
and an FD402 assignment does not authorize starting FD403 or creating its agent.
FD404 aggregates both. Use the [scope boundary](roadmap/0.4-core.md#choose-the-assigned-package)
to classify shared capture, persistence or PAX terminology.

FD402 completed 2026-11-11. Its cumulative evidence covers representative Arrow
and four-car PAX paths across base and combined profiles, with shared native
family coverage. **Improve FD402 workflow efficiency**
(chat `01a1caa7-1e6e-7d83-8890-6178f04957c3`) reconciled E1–E5 and the parent;
**Audit remaining 0.4.0 work** (`01a1ca54-e940-76d0-ad14-297ea8d18d8b`)
completed B1–B7 and E6/E7. Neither assignment has remaining FD402 execution work.

FD402F2 is now closed by the [combined rendered control record](evidence/0.4/recovery/engineered-fe-combined-pass-20261111/README.md),
which carries accepted F1 construction/wiring and native mechanics forward.
FD402F is complete. Its client and private Xvfb `:278` are stopped; retained
worlds and raw run data remain in
`/home/braydon/.local/state/fourthdimension-runs/fd402f2-finish-20261111`.
The prior **Complete FD402 integration coverage** chat
(`01a1a0bd-9ec4-7b63-a613-b82679660855`) is no longer an execution owner.
The inactive **Complete FD402** chat's old low-memory blocker is historical.
Future runtime assignments must verify live PID/cgroup ownership before display
reuse and measure current resource headroom. [E1–E5 passed](evidence/0.4/recovery/two-client-interruptions-20261111/README.md):
cold server restart, one-rider reconnect, native chunk/bearing return and temporary
coupling loss/restore retained both original seats and the native group with
explicit recovery. The Arrow server, clients and private displays :279/:280 are
stopped. Persistent worlds remain at
`/home/braydon/.local/state/fourthdimension-runs/fd402e-arrow-20261111`.
The [PAX combined permanent-removal record](evidence/0.4/recovery/permanent-pax-interruptions-20261111/README.md)
closes E6/E7 with retained native cleanup evidence. Its server, two clients and
private displays :281/:282 are stopped. The final braked world, destroyed-bearing
record and original rider assignments remain at
`/home/braydon/.local/state/fourthdimension-runs/fd402e-pax-e6e7-20261111`.
FD902B consumes the same completed D/E matrix at final freeze reconciliation.

The FD402B1–B7 matrix is now closed cumulatively on 2026-11-11. The seven
[current banked render slices](evidence/0.4/recovery/occupied-banked-render-motion/current-matrix-20261111.md)
cover each occupied family/train through the bend and sustained bank with
mounted riders, rendered chassis/carrier/rack/track bodies and native cart
motion. Existing [banked-train release evidence](evidence/0.4/recovery/banked-trains/acceptance.md),
[family substep release evidence](evidence/0.4/recovery/remaining-family-native-physics-substeps/acceptance.md),
[sampled model clearance](evidence/0.4/recovery/model-clearance/acceptance.md), and
[two-client rendered lifecycle evidence](evidence/0.4/recovery/two-client-recovery-lifecycle/acceptance.md)
supply the cumulative runout, brake/power-cut, detach/unload, identity and
native-frame agreement required by the roadmap. No B row remains open.

FD403E and its shared FD403F PAX walkthrough closed 2026-11-11 by
[combined evidence and corrected instructions](evidence/0.4/fd403-workflows-20261111/README.md#pax--fd403e-and-its-fd403f-walkthrough-accepted).
Normal linking/winch setup, the survival-built occupied cycle and reopen were
already recorded; no new client run was required. The fifteen tooltips, nine
scenes and 53 registrations remain accepted. FD403A and its Arrow walkthrough
closed 2026-11-13. FD403B and the Legacy walkthrough closed 2026-11-14.
FD403C and its Free Spin walkthrough closed 2026-11-15. FD403D and its Revised
walkthrough closed 2026-11-14.
The [FD403C family record](evidence/0.4/fd403c-shaped-occupied-20261115/README.md)
closes its occupied player-built route, pre-capture DAMP response, final
capture/unload/reopen and Free Spin walkthrough contribution. The current
rendered teaching review retains all fifteen tooltips, nine Ponder scenes and
53 registrations; all five family walkthrough claims are reconciled. FD403 and
FD403F are complete, and FD404 has recorded the aggregate checkpoint below.

### FD402 recovery cases

**No open FD402 cases.** A–F are complete for the declared recovery scope.
The [Arrow E1–E5](evidence/0.4/recovery/two-client-interruptions-20261111/README.md)
and [PAX E6/E7](evidence/0.4/recovery/permanent-pax-interruptions-20261111/README.md)
records close the remaining interruptions cumulatively with accepted mechanics,
operator and cleanup evidence. Only a named relevant change or defect reopens an
affected case; FD403 and FD404 remain separate assignments.

### FD403 construction and teaching cases

These are separate assignments. Work can start after FD305 and close independently
of FD402. Normal ride construction/operation evidence does not require the
ordinary-bearing recovery interruption matrix.

The records below preserve the 0.4 assignment and runtime history. They are
closed records, not active ownership or next actions. The FD502 tooling repair
was separately authorized in **Audit and improve roadmap** and did not expand
FD403 acceptance.

| Historical assignment | Owner / thread | Run record | Closure / evidence notes |
| --- | --- | --- | --- |
| **FD502 — current FD403 input/fixture blocker only** | **Audit and improve roadmap**, `01a1a961-64bb-7701-9a19-2acee5ff23a3` | `fd502-execution-repair-20261113`, private `:252`, ports `25602/25603`; [run record](/home/braydon/.local/state/fourthdimension-runs/fd502-execution-repair-20261113/run.json) | Repair complete 2026-11-13; all three services stopped cleanly. Executable input, owned launch/connection checks and route-leg traversal verified. Its handoff was consumed for FD403A; the later [cumulative Arrow record](evidence/0.4/fd403a-circuit-reconciliation-20261113/README.md) closes that separate acceptance. FD502’s later collector needs remain separate. The human-requested [editor and held-use repair](evidence/0.4/editor-workflow-repair-20261114/README.md) also passed a focused connected-client check on 2026-11-14; updated builds keep routine actions open, report server results, and close on accepted Dispatch. This does not reopen family acceptance. |
| **FD403A — Arrow acceptance — complete 2026-11-13; current editor path rechecked 2026-11-14** | **Complete 0.4.0 roadmap tasks**, `01a19ed5-0739-74b0-80d9-e8eb888e6fe0`; explicitly assigned concurrent contributor `01a1d49d-7763-76b2-8b0c-6f5b337f0ea2` | Owned `:254` and `:256` runs are stopped; records and persistent run roots are linked from the [cumulative reconciliation](evidence/0.4/fd403a-circuit-reconciliation-20261113/README.md). | Closed by compatible evidence: normal C–D Chain placement/traversal on `:256`; two occupied circuit returns, capture/open/dismount and cold reopen on `:254`; accepted normal crafting, paid connection, 50 m controlled turn and rendered Arrow teaching reused. The `:256` server-lag timeout before capture is explicitly excluded from final-state claims. The current shared editor response path was then checked on an isolated Arrow world: normal Close restraints refreshed the open editor and normal Dispatch succeeded then closed it. [Recheck record](evidence/0.4/fd403a-editor-ack-recheck-20261114/README.md) carries the changed editor path forward. |

| Closed case | Accepted outcome | Owner | Closure evidence |
| --- | --- | --- | --- |
| **FD403B — complete 2026-11-14** | Player-built shaped powered Legacy route, occupied free response, capture/unload, and Legacy walkthrough. | **Legacy lead**, `01a19ed5-0739-74b0-80d9-e8eb888e6fe0` | [Connected-client shaped powered route and final-state record](evidence/0.4/fd403b-legacy-shaped-powered-20261114/README.md) closes the former route gap using normal recipe-book crafting, normal car placement, normal Chain clicks, occupied powered travel and distinct released-carrier response. Normal capture/open/dismount and save completed; the accepted earlier Legacy cold-reopen evidence carries forward. The apparent reboarding miss was an F3-confirmed cart hit; one click on the confirmed Legacy seat boarded normally. [Teaching reconciliation](evidence/0.4/fd403b-legacy-teaching-reconciliation-20261112.md) closes the Legacy FD403F claim because its reviewed instructions/resources are unchanged. |
| **FD403C — complete 2026-11-15** | Player-built shaped Free Spin route, occupied Inducer/Damper response before Capture, normal unload, cold reopen and its walkthrough. | **Complete FD403C Free Spin**, `01a1d49d-7763-76b2-8b0c-6f5b337f0ea2` | [Shaped occupied-route acceptance](evidence/0.4/fd403c-shaped-occupied-20261115/README.md) closes the route and Free Spin teaching contribution. It discloses the upstream motor fixture and temporary support removal; neither earns construction credit. The occupied DAMP pass precedes normal Capture; final reopened state has all four assigned carriers captured/empty and no `RootVehicle`. |
| **FD403D — complete 2026-11-14** | Player-built powered Revised route, independent lower-hinge behavior, occupied operation, capture/unload, and Revised walkthrough. | **Complete FD403D Revised**, `01a1ca54-e940-76d0-ad14-297ea8d18d8b` | [Continuation review](evidence/0.4/fd403d-revised-blocker-20261114/review.md) combines normal construction, normal right/use boarding, restored normal motor UI (`ScrollValue=-32`, motor/anchor `Speed=32`), actual editor Dispatch after closing restraints, occupied powered travel, fresh Capture, open restraints, Shift unload, and final captured/empty status. Existing powered lower-hinge/reopen evidence and unchanged Revised Ponder/tooltip text are reconciled; no misleading Revised instruction was found. Historic fixture-damage notes remain diagnostic cleanup only. |
| **FD403F — complete 2026-11-15** | All fifteen item descriptions, nine Ponder lessons, 53 registrations and five family walkthrough claims. | **Complete 0.4.0 roadmap tasks**, `01a19ed5-0739-74b0-80d9-e8eb888e6fe0` | [Accepted common teaching](evidence/0.4/teaching-current/acceptance.md) retains the fifteen tooltips, nine scenes and 53 registrations. PAX/common, Arrow, Legacy, Revised, and Free Spin walkthrough claims are reconciled; [Free Spin's operating and teaching handoff](evidence/0.4/fd403c-shaped-occupied-20261115/README.md) closes its family contribution. |

**Historical reassignment — 2026-11-13:** The user reassigned the then-open 0.4.0 tasks to the
three owners above. Those family and teaching assignments have since closed;
their retained run evidence is linked in the closure rows.

**Historical FD403C goal — completed 2026-11-15:** The goal tool at 15:53 UTC returned
`active` with objective “Complete FD403C Free Spin player-built workflow and its
FD403F teaching contribution” for `01a1d49d-7763-76b2-8b0c-6f5b337f0ea2`.
Its original `/goal Complete FD403A` prompt and preview are superseded history.
That text did not require another Arrow review or another goal. The completed
Free Spin outcome is recorded in the closed C row above.

**FD403F / FD403 / FD404 aggregate owner:** **Complete 0.4.0 roadmap tasks**,
`01a19ed5-0739-74b0-80d9-e8eb888e6fe0`. Reconciliation completed 2026-11-15:
FD403A–F are closed, and FD404 records the five-family development checkpoint
against the existing artifact identity, dependency pins and bounded support
scope below. FD401A–D and FD402A–F remain closed. FD404 is not a new runtime
sweep or publication authorization.
**Historical scoped repair ownership — 2026-11-14, closed with FD403D:**
`01a1ca54-e940-76d0-ad14-297ea8d18d8b` owned the seat/editor interaction
diagnosis and targeted repair. Its result is recorded in the closed FD403D row.
The earlier `:283` preparation is stopped; prior diagnostic notes and runtime
listings are historical evidence, not active assignments.


### Case closure policy

When a case passes or existing evidence closes it, record the closure under its
owning roadmap row (case ID, evidence and date), then remove it from the active
queue in the same update. A partially covered case lists only its remaining gap;
completed subclaims stay accepted. One run may close several rows, including a
family walkthrough and its FD403F teaching claim. Do not require another run for
the second label or a new JAR. Close each parent when its required cases are covered;
FD404 then reconciles those closed parents without a fresh sweep.

Only add a case for an existing unresolved criterion, a named supported regression
or an explicitly adopted scope change. Use the [blocker classification](VALIDATION-POLICY.md#gate-blockers-and-follow-up-work).
Nonblocking refinements belong in the owning package's follow-up notes, outside
this acceptance queue. Later milestones add their unresolved cases when activated;
there is no retrospective cataloging task for every historical run. Roadmap rows
own criteria/package status; this queue owns case assignment and next action;
the records below preserve artifact provenance and observations.

## Artifact and scope

| Field | Recorded value |
| --- | --- |
| Committed source | `ec2fcda3082f5d9ce729641a6394fdc5c5d67a36` on `main` |
| Packaged artifact | [qualification.jar](evidence/qualification/2026-11-05-ec2fcda/qualification.jar) |
| SHA-256 | `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7` |
| Version label | `0.9.0-dev.27`; this label alone never identifies an artifact |
| Persistence / networking | Schema 6 / protocol 7 |
| Toolchain | Java `21.0.13.0.3+1`, Gradle `9.2.1` |
| Intended profiles | Base; Track Styles 1.2.0; Engineered 1.1.1; Styles + Engineered |
| Core pins | Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Coasters 0.1.5, Sable 2.0.3 |
| Acceptance on this exact JAR | FD401A artifact identity, FD401B Styles, FD401C Engineered, and FD401D Create telemetry are complete within the recorded bounds and cumulative validation policy. FD401C combines unchanged non-PAX production-path records with selected-JAR Arrow/PAX construction, operator, recovery, authority, coupled-continuity, and cold-reopen evidence. The post-starvation selected-JAR Arrow run completed a full circuit and station capture; `PassShort=1` is explained by its 19.4069/20 peak against the provider's 0.5 shortfall tolerance, and exact 20/20 is not a gate. FD401D has four-profile server formatting across all families and selected-JAR Arrow/PAX client records for both controller implementations and required output transitions. This does not claim a Cartesian client run across every family/source/profile. The related `ae6a699e…` telemetry JAR has one extra GameTest entry and 356 byte-identical common entries. |

The [machine-readable manifest](evidence/qualification/2026-11-05-ec2fcda/artifact.json)
hashes all 287 exported build/source inputs and all 356 JAR entries. The clean
Java 21 build came from a `git archive` of `ec2fcda`, excluding the untracked
`ShuttleBoardingHoldGameTests.java`; the [build log](evidence/qualification/2026-11-05-ec2fcda/build.log.gz)
records successful packaging. Against the [previous clean baseline](evidence/qualification/2026-11-05-8f370d0/artifact.json),
the only changed JAR entries are `EngineeredInterlockGameTests`, `ShuttleRuntime`
and `ShuttleWinchEntity`. The latter two change PAX initial station restoration;
review the affected PAX restoration paths; do not reopen every PAX scenario
solely because its runtime class changed. All other 353 entries
are byte-identical, supporting scoped reuse of unchanged non-PAX paths. The
fixed-source telemetry candidate has exactly one extra untracked GameTest
entry; all 356 common entries match this clean JAR byte for byte.

For a necessary runtime qualification, retain its launch command, actual
artifact/dependency identities, relevant flags/world/settings, roles and outcome.
Reference an unchanged existing launch/profile manifest instead of duplicating it. The
profile launches below establish runtime identity at their recorded scopes;
they do not qualify every scenario in those profiles.
Crafts & Additions remains an explicitly disclosed FE validation fixture only.
The deferred schema-7/protocol-8 registry and new integrations remain excluded.

FD401A's selected-artifact profile identity is mapped to four client records:

| Profile | Exact-JAR runtime identity record | Observation boundary |
| --- | --- | --- |
| Base | [Base client load](evidence/0.9/fd401-a-base-profile-ec2fcda-20261105/README.md) | Title screen and mod list only; no world join. |
| Styles 1.2.0 | [Tracing-disabled reload and occupied ride](evidence/0.9/fd401-styles-tracing-disabled-reload-ride-ec2fcda-20261105/README.md) | One FANCY saved-world run; later records close FD401B. No additional repetitions are required. |
| Engineered 1.1.1 | [Engineered-only client load](evidence/0.9/fd401-a-engineered-profile-ec2fcda-20261105/README.md) | Title screen and mod list only; no world join. |
| Styles + Engineered | [Combined client reopen](evidence/0.9/fd401-current-clean-combined-client-reopen-20261105/README.md) | One saved occupied-world reopen. FD401C's occupied Engineered-only Arrow cold-reopen and combined-profile recovery/cycle records are linked below; FD401D is closed by the consolidated selected-JAR telemetry evidence. |

Each linked record retains the selected JAR hash, runtime classpath, dependency hashes, Java 21 launch arguments, and exit evidence. This closes **FD401A identity**. FD401B Styles, FD401C Engineered, and FD401D telemetry acceptance are recorded in the bounded scenario rows below.

<a id="fd404-reconciliation"></a>
### FD404 reconciliation — complete 2026-11-15

| Required input | Accepted record |
| --- | --- |
| Five-family source and JAR identity | Source `ec2fcda3082f5d9ce729641a6394fdc5c5d67a36`; packaged `qualification.jar`, SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`; manifest covers 287 exported inputs and all 356 JAR entries. |
| Dependency identities | Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Coasters 0.1.5, Sable 2.0.3; optional profiles are Track Styles 1.2.0 and Engineered 1.1.1, individually and combined. Exact profile launches and hashes are linked above and in the scenario records. |
| Bounded support scope | The five original families and recorded base/Styles/Engineered/combined profiles; FD402 recovery remains limited to the declared original tracks, families, Arrow/PAX examples, drive and equipment envelope. New families/adapters, schema-7/protocol-8 registry, unsupported assemblies/routes and cosmetic/resource overrides are excluded. FD403 fixtures are disclosed in their accepted family records. |
| Parent deliverables | FD401A–D complete for the scope stated above; FD402A–F complete for its declared recovery envelope; FD403A–F complete, with connected-client family acceptance and all five walkthrough claims reconciled. Evidence links are in their canonical sections above. |

This closes FD404 as the five-family development checkpoint. It does not close
FD1000/FD905, qualify a release candidate, authorize publication, or require a
new build or runtime sweep.

## Evidence reuse and required retests

**Read the [current queue](#current-work-queue) and task status before scheduling a run.** FD401A–D are complete
within their declared scope. All four five-family server telemetry profile rows
below have passes. [FD401 current status](../ROADMAP.md#fd401-deliver-optional-provider-infrastructure)
supersedes old per-run FE/client-output gaps that later evidence closed. Accepted construction, normal-seat
cycles, rendering, and persistence may be combined across records when their
relevant paths match; a missing observation in one record does not erase another.

The table below is a **per-run observation history**, not a cumulative test queue.
Its older “remain open” notes describe what that run alone did not establish and
may be superseded by later rows. Apply the [validation policy](VALIDATION-POLICY.md)
and current task status first. Record new evidence here without inventing a new
cross-product matrix or requiring every earlier success to be replayed.

Rows naming `cf4fed9a…` are exact observations on the previous clean JAR. Their
non-PAX packaged classes remain byte-identical in the selected JAR, but the
changed PAX restoration paths need scoped review. Reuse unaffected PAX results;
only a changed or unresolved behavior requires further runtime evidence.

| Evidence | What remains established | Per-run limits and follow-up at the time (current task status takes precedence) |
| --- | --- | --- |
| [Frozen `2c5d9a7` map](evidence/0.9/fd401-final-candidate-2c5d9a7/README.md) and [later-source review](evidence/0.9/fd401-release-review-54495a5/README.md) | Historical operator, FE, font, Styles and geometry results at their recorded identities | FD401A reviews affected source/resource inputs; FD401B–D and FD904A replay changed paths. No blanket whole-JAR equivalence. |
| [PAX source observation](evidence/0.9/fd401-pax-longhold-source-229e586/acceptance.md) and [packaged named gate](evidence/0.9/fd401-pax-packaged-longhold-229e586/README.md) | Both record repair, realignment, recovery and relaunch with sixteen riders after 4,800 unsupported ticks; the packaged gate has exact JAR identity `94c17e50…` | FD902C retains this historical named-test pass. `ShuttleRuntime` and `Boarding` now differ in the selected JAR, so the old pass is not a current-artifact runtime pass. Normal-client operation, OUTBOUND regression and active-phase cold restarts remain separate. |
| [PAX two-client recovery](evidence/0.9/fd401-pax-two-client-recovery-8f370d0/README.md) | Tested JAR `253e6d92…`: two one-rider curve-only LIFTING holds over 4,800 ticks, normal repair/realign, explicit Recover on the second hold, occupied relaunch, BOARDING reopen and normal unload | The tested JAR has one extra uncommitted GameTest class; its 356 common entries match the previous `cf4fed9a…` clean JAR. The new selected JAR changes `ShuttleRuntime` and `ShuttleWinchEntity`, so this PAX observation remains attached to its original tested JAR. Active-phase cold restarts, OUTBOUND link loss and sixteen-rider client operation remain open. |
| [8f370d0 JAR Engineered Arrow/FE reopen](evidence/0.9/fd401-engineered-current-arrow-fe-reopen/README.md) | Exact previous clean JAR `cf4fed9a…`: one occupied Arrow launched under wired FE, completed STOP and station capture, unloaded normally, then survived an occupied cold reopen without automatic launch and unloaded again | FD401C gains a bounded Engineered-only Arrow result. Combined profile and current-JAR normal construction remain open. Crafts & Additions was a disclosed FE fixture. |
| [Selected-JAR Engineered-only Arrow operator cycle](evidence/0.9/fd401c-selected-engineered-only-arrow-cycle-20261105/README.md) | Exact selected clean JAR `656725bc…`, Engineered-only: normal empty-hand seat boarding and UI restraint closure preceded an occupied wired-controller pass with `PassStarved=0`; the controller stopped at `19.3966/20` with `PassShort=1`, then station capture, UI restraint opening, Shift unload, zero assignments and clean save/exit followed | Adds one current-artifact Engineered-only operator cycle on an inherited route. It does not establish fresh construction, measured post-pass FE draw, the 20/20 controller target, or other-family client cycles. |
| [8f370d0 JAR wired FE interruption](evidence/0.9/fd401-engineered-current-fe-interruption/README.md) | Exact previous clean JAR `cf4fed9a…`: occupied Arrow held near station with depleted FE and `Starving=1`; restored water-wheel supply caused a short automatic pass with rider retained, followed by manual capture and normal unload | FD401C gains a bounded power-loss/restore result. The short pass did not complete a circuit or reach station capture; no second operator pulse was required by Engineered on restore. |
| [8f370d0 JAR Legacy required-energy loop](evidence/0.9/fd401-engineered-current-legacy-fe/README.md) | Exact previous clean JAR `cf4fed9a…`: occupied Legacy reached STOP without starvation under `requireEnergy=true`, consumed FE, returned to station capture and unloaded normally | FD401C gains one bounded Legacy result on the saved normally built route. The 8.0 blocks/s target was short. The preliminary default-config run is disclosed separately and supports no FE claim. |
| [8f370d0 JAR Free Spin required-energy loop](evidence/0.9/fd401-engineered-current-free-spin-fe/README.md) | Exact previous clean JAR `cf4fed9a…`: occupied Free Spin reached programmed STOP without starvation under `requireEnergy=true`, consumed FE, returned to station capture and unloaded normally | FD401C gains one bounded Free Spin result on the saved normally built route. The FE buffer was initialized by a disclosed fixture command. |
| [8f370d0 JAR Revised required-energy loop](evidence/0.9/fd401-engineered-current-revised-fe/README.md) | Exact previous clean JAR `cf4fed9a…`: occupied Revised reached programmed STOP without starvation under `requireEnergy=true`, consumed FE, returned to station capture and unloaded normally | FD401C gains one bounded Revised result on the saved normally built route. The FE buffer was initialized by a disclosed fixture command. |
| [8f370d0 JAR coupled Arrow required-energy loop](evidence/0.9/fd401-engineered-current-coupled-arrow-fe/README.md) | Exact previous clean JAR `cf4fed9a…`: occupied trailing Arrow reached STOP under `requireEnergy=true`, returned to station capture and unloaded normally with two chassis loaded | FD401C gains one bounded two-car operator result on a saved normally linked consist. The later capture query found no anchor energy key despite `PassStarved=0`; both native cart positions/link were not independently traced. |
| [Selected-JAR coupled Arrow continuous trace](evidence/0.9/fd401c-coupled-arrow-continuous-trace-20261105/README.md) | Exact selected clean base JAR `656725bc…` with validation-only instrumentation: 720 paired position/velocity samples over 179.75 seconds for the same two Arrow UUIDs; native link was true in all samples during one occupied pass | Closes continuous coupled-link identity/continuity trace only. The controller reached `Pass=1`, `PassShort=0`, `PassStarved=0`, but the disclosed 100,000 FE fixture depleted afterward and `Starving=1`; fresh construction and the remaining FD401C matrix stay open. |
| [8f370d0 JAR four-car PAX authority](evidence/0.9/fd401-engineered-current-pax-authority/README.md) | Exact previous clean JAR `cf4fed9a…`: an occupied, route-loaded PAX winch cycle returned to BOARDING while the opposing powered Engineered controller stayed at pass zero and 100,000 FE; normal unload followed | FD401C gains a bounded Engineered-only PAX operator result. The first low-render-distance attempt faulted on an unloaded route chunk and is retained separately. Per-car callback cancellation is established by a later-source fixture, not this client run. |
| [8f370d0 JAR combined-profile Arrow](evidence/0.9/fd401-engineered-current-combined-arrow/README.md) | Exact previous clean JAR `cf4fed9a…`: a Styles + Engineered client loaded the saved wired Arrow route, drew FE without starvation, advanced the programmed controller, captured at station and unloaded the connected rider through the UI | FD401C gains one bounded combined-profile Arrow operator result. The first default-video attempt hit the enforced 3 GiB scope and is retained as an OOM failure; FAST/render-distance-4 succeeded under that same cap. Fresh construction and combined-profile all-family coverage remain open. |
| [8f370d0 JAR fresh Arrow construction](evidence/0.9/fd401-engineered-current-fresh-arrow/README.md) | Exact previous clean JAR `cf4fed9a…`: normal item actions fitted the motor, placed the Arrow car, bound the station, linked and placed the controller, and programmed Launch 20 → Stop 12 → End; an occupied pass reached STOP without starvation, captured and unloaded | FD401C gains one bounded Engineered-only construction and operator result. The new track was fixture-built, FE was directly filled, and validation boarding was used after two normal seat clicks did not mount. Other-family construction and combined-profile construction remain open. |
| [8f370d0 JAR combined five-family motor fixture](evidence/0.9/fd401-engineered-current-combined-five-family/README.md) | Exact previous clean JAR `cf4fed9a…`: Styles 1.2.0 + Engineered 1.1.1 loaded together; one named server test asserted held boarding, occupied launch/STOP/capture/retention for four driven families, and PAX rejecting motor drive while retaining its rider | FD401C gains bounded combined-profile family interaction. The test directly constructed its tracks, controllers, and pig riders; normal UI construction, physical FE, coupled Arrow motion, PAX winch operation, and five-family client operator cycles remain open. |
| [Selected-JAR combined five-family FE/retention guard](evidence/0.9/fd401c-selected-combined-five-family-fe-guard-20261105/README.md) | Selected production classes `656725bc…`, Styles + Engineered: current-source named test class overlaid on the packaged JAR; empty-buffer hold, native FE fill, powered motion, STOP/recovery and retained riders passed across four coaster families and PAX authority | Adds a current selected-production-class combined-profile fixture result. It is a one-class test overlay with direct fixtures; it does not establish UI construction, physical FE, client cycles, or full FD401C acceptance. |
| [Current-source Engineered-only five-family FE guard](evidence/0.9/fd401c-engineered-current-five-family-fe-guard-20261105/README.md) | Current development classes: bounded empty-buffer hold, powered movement, STOP/recovery and rider retention across the same five families | Source/runtime-only corroboration; not packaged-JAR or combined-profile evidence. |
| [8f370d0 JAR combined four-family construction](evidence/0.9/fd401-engineered-current-combined-construction/README.md) | Exact previous clean JAR `cf4fed9a…`: one Styles + Engineered client used normal items to fit motors, place Arrow/Legacy/Free Spin/Revised cars, bind each station, place linked controllers, and GUI-program `Launch 20 → Stop 12 → End` on four fresh fixture-built tracks | FD401C gains bounded combined-profile normal construction for the driven families. This run did not board or cycle the new cars, supply physical FE, or construct a PAX train. |
| [Clean-JAR combined Legacy seat blocker](evidence/0.9/fd401-engineered-clean-combined-legacy-seat-blocker/README.md) | Exact selected clean JAR `656725bc…` loaded the saved Styles + Engineered Legacy route; FE was initialized by disclosed fixture command, F3 targeted the physical `legacy_seat`, but normal empty-hand interaction did not mount and live assignments stayed zero | This failed attempt is not operator acceptance; a later selected-JAR continuous normal-board Legacy cycle closes the normal-seat cycle case. The blocker remains historical evidence and does not reopen FD401C, which is complete under the current cumulative validation policy. |
| [Legacy seat input diagnostic](evidence/0.9/fd401-legacy-seat-input-diagnostic-20261105/README.md) | An isolated diagnostic JAR changed only `CarrierSeatBlock.class`; two F3-targeted seat clicks emitted no instrumented `FD_SEAT_CLICK` log and did not mount | This isolated diagnostic is not acceptance. Subsequent selected-JAR normal boarding succeeded and the Legacy operator cycle completed; no further replay is required under the current cumulative validation policy. |
| [Clean-JAR combined Legacy cycle](evidence/0.9/fd401-engineered-clean-combined-legacy-cycle/README.md) | Exact selected clean JAR `656725bc…`: one occupied Legacy cycle reached STOP without FE starvation, drew 6,831 FE, captured, opened restraints in the normal vehicle UI, unloaded, saved and exited cleanly | FD401C gains a bounded combined-profile Legacy operator result. Validation helper boarding and a saved normally constructed route were disclosed; normal seat boarding, fresh construction and other-family combined-client cycles remain open. |
| [Clean-JAR Legacy normal-seat retry](evidence/0.9/fd401-legacy-clean-normal-seat-retry/README.md) | Exact selected clean JAR `656725bc…`: F3 targeted an unrestrained Legacy seat and two empty-hand input methods did not mount; a helper in the same client mounted a different ready seat on that vehicle | Historical failure from the earlier aim. The later [normal boarding pass](evidence/0.9/fd401-legacy-clean-normal-board-20261105/README.md) reached and mounted the same seat at a corrected position. |
| [Clean-JAR Legacy keyboard-use control](evidence/0.9/fd401-legacy-keyboard-use-control-20261105/README.md) | Exact selected clean JAR `656725bc…`: with Use rebound to V, F3-targeted Legacy seat did not mount, while the same key toggled a targeted test lever; `RootVehicle` remained absent | Excluded a mouse-only failure. A later diagnostic found the client Use hit result was `MISS` at this pose; see the [selected-JAR normal boarding pass](evidence/0.9/fd401-legacy-clean-normal-board-20261105/README.md). |
| [Clean-JAR Legacy normal boarding and unload](evidence/0.9/fd401-legacy-clean-normal-board-20261105/README.md) | Exact selected clean JAR `656725bc…`: default right click at a corrected pose mounted the targeted Legacy seat with `FD_BOARDING_ASSIGN accepted=true`, then Shift dismount removed `RootVehicle`; normal save and exit followed | Closes the bounded Legacy normal-seat gap. This earlier row isolates seat interaction; the later continuous normal-board-to-cycle result is recorded below, while other FD401C rows remain open. |
| [Clean-JAR continuous Legacy normal-board cycle](evidence/0.9/fd401-legacy-clean-continuous-normal-cycle-20261105/README.md) | Exact selected clean JAR `656725bc…`, combined profile: one default seat click mounted Legacy, normal UI closed restraints, a disclosed controller pulse reached STOP without starvation, FE fell 86,111→79,847, the rider returned to station capture, and UI restraint release plus held Shift cleared `RootVehicle` and the assignment before clean exit | Closes the split normal-board versus helper-boarded-cycle gap on the saved combined-profile Legacy route. Fresh construction and the wider FD401C profile/family/power-recovery matrix remain open. |
| [Clean-JAR combined Free Spin cycle](evidence/0.9/fd401-engineered-clean-combined-free-spin-cycle/README.md) | Exact selected clean JAR `656725bc…`: one occupied Free Spin cycle reached STOP without FE starvation, drew 11,588 FE, captured, opened restraints in the normal vehicle UI, unloaded, saved and exited cleanly | FD401C gains a bounded combined-profile Free Spin operator result. Validation helper boarding and a saved normally constructed route were disclosed; normal seat boarding, fresh construction and other-family combined-client cycles remain open. |
| [Clean-JAR continuous Free Spin normal-board cycle](evidence/0.9/fd401-free-spin-clean-continuous-normal-cycle-20261105/README.md) | Exact selected clean JAR `656725bc…`, combined profile: a normal seat click mounted Free Spin, the UI closed restraints, the saved linked controller launched and reached STOP without starvation, FE fell 74,075→60,904, the occupied car returned to station capture, and UI restraint release plus held Shift cleared `RootVehicle` and assignments before clean exit | Closes the split normal-board versus helper-boarded-cycle gap on the saved combined-profile Free Spin route. The saved car/controller/power fixture and command-pulsed redstone remain disclosed; fresh construction and wider FD401C profile/family/power-recovery coverage remain open. |
| [Clean-JAR combined Revised normal-seat cycle](evidence/0.9/fd401-engineered-clean-combined-revised-normal-seat-cycle-20261105/README.md) | Exact selected clean JAR `656725bc…`, combined profile: an empty-hand click boarded the Revised seat, normal UI restraint closure and disclosed redstone/controller pulse produced an occupied powered pass through STOP and station capture, and UI release plus Shift dismount cleared RootVehicle and assignments before clean exit | Closes the joined normal-seat-to-cycle slice for one saved combined-profile Revised route. Track/controller/power were inherited and the redstone pulse is disclosed; fresh construction, normal power-source construction, other profiles/families, recovery cases and broader FD401C coverage remain open. |
| [Combined-profile FE depletion diagnostic (negative)](evidence/0.9/fd401-combined-fe-depletion-negative-20261105/README.md) | Exact selected clean JAR `656725bc…`: removing one Create water wheel and clearing the anchor energy key did not establish `Starving=1` or `PassStarved=1`; the occupied Arrow still moved through two controller pulses. The setup was restored, captured, normally unloaded and exited cleanly. | This does not close FD401C power-interruption acceptance: the isolated diagnostic config had `requireEnergy=false`, so motor motion was not an FE-starvation test. The corrected selected-JAR run below supplies the positive occupied interruption/restore result. |
| [Selected-JAR combined wired FE interruption/restore](evidence/0.9/fd401-combined-wired-fe-recovery-selected-20261105/README.md) | Exact selected clean JAR `656725bc…`, combined profile with `requireEnergy=true`: normal Arrow seat boarding and UI restraint closure preceded one depleted-FE controller pulse; `Starving=1` held the rider near station. Restoring the north-facing water wheel replenished the wired anchor and automatically resumed the occupied car; the controller recorded `PassStarved=1`, then the short pass ended and normal UI Capture, restraint release and Shift dismount cleared the assignment before a clean save/exit. | Closes a bounded combined-profile occupied starvation hold, wired restoration, automatic short resume and safe manual unload. The pass reached 3.052/20 and stopped away from station, so full post-restore circuit and automatic station capture remain open. Wheel/anchor/redstone/teleport fixtures are disclosed. |
| [Selected-JAR recharge, hold and full-circuit recovery](evidence/0.9/fd401c-selected-recharge-hold-full-circuit-20261105/README.md) | Exact selected clean JAR `656725bc…`: after depleted FE and restoration, the occupied Arrow completed a full circuit; restoring the station hold signal produced `capture=true`, then normal UI restraint release and unload succeeded. `PassStarved=0`, peak was 19.4069/20, and `PassShort=1`. | Closes the bounded post-starvation full-circuit, station-capture and safe-unload case. The provider's `SHORTFALL_TOLERANCE=0.5`; this run was 0.5931 below its programmed speed target, so `PassShort=1` is accurate. FD401C does not require exact 20/20 or zero shortfall. |
| [Selected-JAR Engineered-only Arrow cold reopen](evidence/0.9/fd401c-engineered-only-arrow-cold-reopen-20261105/README.md) | Exact selected clean JAR `656725bc…`, Engineered-only: normally boarded rider and closed restraints were saved; after cold reopen, the rider and restraints restored at the captured station, motion remained stable through an idle interval, and normal UI release/dismount/save succeeded. | Closes the representative occupied Engineered cold-reopen/persistence path. This is an inherited route and does not claim a new launch or power-interruption run; the separate combined-profile recovery record covers those behaviors. |
| [Clean-JAR combined Revised cycle](evidence/0.9/fd401-engineered-clean-combined-revised-cycle/README.md) | Exact selected clean JAR `656725bc…`: one occupied Revised cycle reached STOP without FE starvation, drew 12,999 FE, captured, opened restraints in the normal vehicle UI, unloaded, saved and exited cleanly | FD401C gains a bounded combined-profile Revised operator result. Validation helper boarding and a saved normally constructed route were disclosed; normal seat boarding, fresh construction and other-family combined-client cycles remain open. |
| [Clean-JAR combined PAX authority](evidence/0.9/fd401-engineered-clean-combined-pax-authority/README.md) | Exact selected clean JAR `656725bc…`: one normal seat click boarded the four-car PAX train, the UI dispatched an occupied winch cycle from BOARDING through LIFTING and RETURNING back to BOARDING, and the powered opposing Engineered controller stayed at pass zero with 100,000 FE throughout sampled phases; normal unload and clean exit followed | FD401C gains a bounded combined-profile PAX authority result on the changed PAX runtime classes. The saved route was not newly built, OUTBOUND was not sampled separately, and per-car callback cancellation and active-phase restarts remain separate. |
| [Clean-JAR Engineered-only PAX authority](evidence/0.9/fd401-engineered-clean-engineered-only-pax-authority/README.md) | Exact selected clean JAR `656725bc…`, Engineered without Styles: normal seat boarding and UI dispatch completed a four-car PAX cycle through LIFTING, RETURNING and BOARDING, with cycles 3→4; the powered opposing controller stayed at pass zero with 100,000 FE at sampled phases, then normal UI unload and clean exit followed | FD401C gains a bounded Engineered-only PAX authority result on the changed PAX runtime classes. The saved route was not newly built, OUTBOUND was not sampled separately, and per-car callback cancellation and active-phase restarts remain separate. |
| [Fresh selected-JAR PAX normal construction and occupied cycle](evidence/0.9/fd401-pax-normal-construction-656725bc-20261105/README.md) | Exact selected clean JAR `656725bc…`: normal item/UI use built the PAX spans, four cars, three chain links and winch; one normally boarded rider completed an occupied cycle through LIFTING and OUTBOUND back to BOARDING (`shuttle_cycles: 1`, no fault), opened restraints, dismounted and saved cleanly | Closes one bounded FD401C fresh-PAX construction/cycle slice. Upstream coaster anchors/supports and the 16 RPM Create creative motor were command-fixtured. That run's log emitted BOARDING telemetry only; this separate [selected-JAR fault-resume run](evidence/0.9/fd401-pax-fault-resume-unload-selected-656725bc-20261105/README.md) now records phase telemetry through a full circuit, active-phase reopen/resume, and normal unload. Full normal power/support construction, per-car callback cancellation and the broader FD401C matrix remain open. |
| [Selected-JAR PAX fault resume, circuit, and unload](evidence/0.9/fd401-pax-fault-resume-unload-selected-656725bc-20261105/README.md) and [3 GiB-per-client repeat](evidence/0.9/fd401-pax-fault-resume-unload-3g-selected-20261105/README.md) | Exact selected clean JAR `656725bc…`: the saved occupied `FAULT/LIFTING` train resumed by ordinary empty-hand winch use after restoring 16 RPM; telemetry recorded LIFTING → OUTBOUND → RETURNING → CAPTURING → BOARDING, then UI restraint release and Shift dismount cleared the rider assignment. The repeat uses separate Java 21 client scopes capped at 3 GiB each and retains both worlds, logs, launch args and screenshots. | Closes one bounded selected-JAR PAX power-fault recovery/circuit/unload slice under the historical client memory caps; no memory-policy-only repeat is required. The route and upstream drive were inherited/command-fixtured, so fresh construction and the broader FD401C matrix remain open. |
| [Tracing-disabled Styles reload and occupied ride](evidence/0.9/fd401-styles-tracing-disabled-reload-ride-ec2fcda-20261105/README.md) | Exact selected clean JAR `656725bc…`, Styles-only: F3+T resource reload, normal empty-hand Arrow boarding, visible occupied lap, station capture, normal unload and clean exit with tracing/validation disabled | FD401B gains one bounded tracing-disabled FANCY reload/ride pass. This run covers one pass; the former three-per-mode requirement is retired. Later records close FD401B transitions and geometry. The pinned selected-JAR Iris/Solas observation is recorded separately below. |
| [Selected-JAR FANCY validation reload and occupied ride](evidence/0.9/fd401-styles-fancy-validation-reload-ride-ec2fcda-20261105/README.md) | Exact selected clean JAR `656725bc…`, Styles-only FANCY: F3+T reload while occupied, validation-helper boarding, falling-edge fixture dispatch, motion, station capture, UI restraint opening, Shift unload and clean exit | Adds one bounded FANCY reload/ride repeat. Helper boarding and redstone fixture actions are disclosed; it does not prove normal boarding or a UI-originated sustained dispatch. |
| [Selected-JAR FANCY validation reload and occupied ride, repeat 3](evidence/0.9/fd401-styles-fancy-validation-reload-ride-3-ec2fcda-20261105/README.md) | Exact selected clean JAR `656725bc…`, Styles-only FANCY: F3+T reload while occupied, validation-helper boarding, falling-edge fixture dispatch, motion, station capture, UI restraint opening, Shift unload and clean exit | Third complete bounded FANCY reload/ride repetition. Helper boarding and redstone fixture actions are disclosed; normal seat boarding and UI-originated sustained dispatch remain separate claims. |
| [Selected-JAR FABULOUS validation reload and occupied ride](evidence/0.9/fd401-styles-fabulous-validation-reload-ride-ec2fcda-20261105/README.md) | Exact selected clean JAR `656725bc…`, Styles-only FABULOUS: F3+T reload while occupied, validation-helper boarding, falling-edge fixture dispatch, motion, station capture, UI restraint opening, Shift unload and clean exit | Adds one bounded FABULOUS reload/ride repeat. Helper boarding and redstone fixture actions are disclosed; it does not prove normal boarding or a UI-originated sustained dispatch. |
| [Selected-JAR FABULOUS validation reload and occupied ride, repeat 2](evidence/0.9/fd401-styles-fabulous-validation-reload-ride-2-ec2fcda-20261105/README.md) | Exact selected clean JAR `656725bc…`, Styles-only FABULOUS: occupied F3+T reload, validation-helper boarding, falling-edge fixture dispatch, station capture and UI unload | Adds a second bounded FABULOUS repeat. Helper boarding and redstone fixture actions are disclosed; normal boarding and UI-originated sustained dispatch remain separate claims. |
| [Selected-JAR FABULOUS validation reload and occupied ride, repeat 3](evidence/0.9/fd401-styles-fabulous-validation-reload-ride-3-ec2fcda-20261105/README.md) | Exact selected clean JAR `656725bc…`, Styles-only FABULOUS: occupied F3+T reload, validation-helper boarding, falling-edge fixture dispatch, station capture and UI unload | Completes three bounded FABULOUS repeats. Helper boarding and redstone fixture actions are disclosed; normal boarding and UI-originated sustained dispatch remain separate claims. |
| [Selected-JAR FAST resumed ride completion](evidence/0.9/fd401-styles-fast-validation-reload-ride-resumed-completion/README.md) and [partial checkpoint](evidence/0.9/fd401-styles-fast-validation-reload-ride-partial-ec2fcda-20261105/README.md) | Exact selected clean JAR `656725bc…`, Styles-only FAST: occupied motion was saved mid-ride, then restored in a second client, captured, restraints opened, and normally unloaded | These paired records form one completed FAST scenario, not two; helper boarding and scratch-world signal changes are disclosed. |
| [Selected-JAR FAST reload and occupied ride, repeat 1](evidence/0.9/fd401-styles-fast-validation-reload-ride-1/README.md) | Exact selected clean JAR `656725bc…`, Styles-only FAST: F3+T, helper boarding, occupied motion, capture, UI restraint release, normal Shift unload, and clean exit | Adds one fresh completed FAST scenario. Normal seat boarding and sustained UI dispatch remain separate. |
| [Selected-JAR FAST reload and occupied ride, repeat 2](evidence/0.9/fd401-styles-fast-validation-reload-ride-2/README.md) | Exact selected clean JAR `656725bc…`, Styles-only FAST: F3+T, helper boarding, occupied movement following disclosed falling-edge fixture dispatch and signal restoration, capture, UI restraint release, normal Shift unload, and clean exit | Adds a second fresh completed FAST scenario. Together with the resumed completion above, FAST now has three completed bounded reload/ride scenarios. Normal seat boarding, sustained UI dispatch, and style/profile transition requirements remain separate. |
| [Selected-JAR pinned Iris/Solas rendering observation](evidence/0.9/fd401-styles-solas-selected-ec2fcda-20261105/README.md) | Exact selected clean JAR `656725bc…`, Iris development-fixed `49f783b8…`, Sodium `575d187b…`, Solas V3.7b `b7dfe4c7…`; FANCY styled track visible under the shader and Iris created the Overworld pipeline | Closes one bounded pinned shader/profile observation. Flywheel fell back from indirect to off, so accelerated Flywheel under Solas is not established; geometry diagnostics, provider transitions and the broader graphics matrix remain open. The retry required a documented temporary 4 GiB process scope after the 3 GiB cap killed shader activation; JVM heap remained 1536 MiB. |
| [Selected-JAR Styles appearance UI](evidence/0.9/fd401b-style-ui-transition-selected-20261105/README.md) and [provider lifecycle](evidence/0.9/fd401-styles-provider-lifecycle-selected-20261105/README.md) | Exact selected JAR `656725bc…`: normal UI changed one full-turn Arrow section double → Original → single → double → single without altering its profile/family; final choice survived reopen and F3+T. A separate base→Styles cold lifecycle preserved all 24 serialized rails, including styled IDs/profiles. | FD401B gains bounded whole-section edit and provider-reinstall persistence. The UI run did not ride the edited span, and the lifecycle run did not exercise a moving coupled inversion. |
| [Selected-JAR Styles fallback and occupied absence](evidence/0.9/fd401-styles-fallback-visual-selected-20261105/README.md), [occupied base ride](evidence/0.9/fd401-styles-absent-occupied-selected-20261105/README.md) | Exact selected JAR `656725bc…`: a retained styled Arrow section rendered as Original with Styles absent; a separate base-profile occupied ride captured, unloaded normally, and preserved every rail record. | FD401B gains bounded visual fallback and absent-provider mechanics. Passage over each styled section was not isolated in the base ride. |
| [Selected-JAR coupled Styles client](evidence/0.9/fd401b-coupled-styles-selected-20261105/README.md) and [focused named test](evidence/0.9/fd401b-coupled-named-selected-20261105/README.md) | Exact selected JAR `656725bc…`: two normally boarded FANCY client cycles retained two cars, styled decoration activity, capture and unload; the packaged native fixture measured both cars through banked single-spine and flat double-spine full turns within 0.08 radians of `2π`. | FD401B's coupled motion and exact selected-JAR peaks are bounded. The client camera did not visually isolate the moving inversion; the named fixture uses pigs and fixture-assigned styles. |
| [Selected-JAR Styles ownership parity](evidence/0.9/fd401b-ownership-parity-selected-20261105/README.md) and [coupled geometry sweep](evidence/0.9/fd401b-swept-selected-20261105/README.md) | Exact selected JAR `656725bc…`: packaged ownership/catalog/Styles renderer classes match the reviewed candidate; Original renderer adds only render metrics. Fresh coupled native traces pass default single, double and mixed-plan post-physics and between-tick 0.02-block clearance audits. | FD401B gains ownership-path parity and bounded default-model geometry checks. Individual pixels, equipped/cosmetic riders, arbitrary models, and an observed styled-client inversion remain outside these checks. |
| [Selected-JAR visible coupled Styles full turns](evidence/0.9/fd401b-visible-coupled-selected-20261105/README.md) | Exact selected JAR `656725bc…`: a Java 21 FANCY client on private Xvfb visibly rendered both styled coupled cars and riders rotating through the full-turn fixture; two named test invocations passed with all four near-`2π` peaks, retained link, capture, and unload. | Closes FD401B's visible moving inversion gap when paired with the separate normal UI, normal boarding, graphics, provider lifecycle, ownership, and swept-geometry records. This fixture used pig riders and assigned its styles directly; arbitrary cosmetics and individual pixels remain outside the bounded claim. |
| [Refreshed clean-JAR base-profile client load](evidence/0.9/fd401-a-base-profile-ec2fcda-20261105/README.md) | Exact selected JAR `656725bc…` launched with no optional provider; the Java 21 client reached the title screen, and the normal Mods screen listed Fourth Dimension 0.9.0-dev.27 among ten mods | FD401A gains an exact-artifact base-profile startup identity row. This was a title-screen load, not an in-world operator or telemetry scenario; Styles-only and Engineered-only runtime launches remain open. |
| [Refreshed clean-JAR Engineered-only client load](evidence/0.9/fd401-a-engineered-profile-ec2fcda-20261105/README.md) | Exact selected JAR `656725bc…` launched with Engineered 1.1.1 and no Track Styles; the Java 21 client reached the title screen and normal Mods screen listed both Engineered and Fourth Dimension | FD401A gains an exact-artifact Engineered-only startup identity row. This was a title-screen load, not an in-world operator or telemetry scenario; Styles-only profile reconciliation remains open. |
| [Fixed-source PAX bind restore candidate](evidence/0.9/fd401-pax-bind-restore-fix-candidate/README.md) | Candidate JAR `ae6a699e…` passed the combined-profile server telemetry test across Arrow (two-car consist), Legacy, Free Spin, Revised, and four-car PAX; all six source fields, goggle formatter/update-tag snapshots, recovery guards, and PAX BOARDING/LIFTING/FAULT passed after fixing initial bind restore | Bounded FD401D server evidence on a JAR with one extra untracked GameTest class; all 356 entries shared with the new clean `656725bc…` JAR are byte-identical. Exact clean-JAR client source GUI, rendered goggles/signs, and full profile checks remain open. |
| [Refreshed clean-JAR combined client reopen](evidence/0.9/fd401-current-clean-combined-client-reopen-20261105/README.md) and [Display Link UI reachability](evidence/0.9/fd401-current-clean-combined-display-link-ui-20261105/README.md) | Exact selected JAR `656725bc…` loaded in the Styles + Engineered client; the copied saved world restored Dev onto its occupied Arrow; a second run opened the normal Display Link configuration UI and showed its existing Ride fault → Line 1 mapping | Adds an exact-artifact combined-profile runtime row to FD401A, a bounded reopen observation to FD401C, and confirms FD401D source-UI reachability. These two runs alone do not capture rendered live sign/goggles output or a client telemetry snapshot. Base, Styles-only, and Engineered-only runtime profiles remain open. |
| [Selected-JAR occupied PAX goggles and Observer snapshot](evidence/0.9/fd401d-occupied-pax-goggles-snapshot-selected-20261105/README.md) | Exact selected JAR `656725bc…`, combined profile: normal fourth-car boarding and closed restraints; Observer received `occupied=1`, four cars, BOARDING, restraints closed and dispatch ready at 06:33:52; the goggles screenshot at 06:34:00 showed matching values, with unchanged snapshots repeated through 06:37:04 | Closes one bounded occupied PAX goggles/client-snapshot slice. The Display Link source remained Ride Fault and blank; the sign source UI was not reached in this run. Other families and profiles remain open; see the separate occupied sign-output row below for the bounded combined-profile PAX result. |
| [Selected-JAR occupied PAX Ride Occupancy sign output](evidence/0.9/fd401d-occupied-pax-sign-output-cross-20261105/README.md) | Exact selected JAR `656725bc…`, combined profile: normal source picker configured the link; while Dev remained mounted and the four-car PAX winch stayed in BOARDING, front/back sign NBT both read `1 / 16`; Observer packet and goggles had matching occupancy, phase, restraints, and dispatch values earlier in the same uninterrupted state interval | Closes the bounded combined-profile occupied PAX sign-output slice. No single screenshot shows sign and goggles together, and base, Styles-only, Engineered-only, other-family, unavailability/re-entry, and full source-matrix evidence remains open. |
| [Selected-JAR two-car Arrow occupancy output](evidence/0.9/fd401d-selected-arrow-occupancy-sign-20261105/README.md) | Exact selected JAR `656725bc…`, combined profile: normal Dev mount on the trailing Arrow; Observer received two-car `occupied=1` telemetry and matching goggles output; after server restart, Dev’s saved assignment restored on reconnect, the linked sign rendered front/back `1 / 8`, and normal Shift dismount returned both sides to `0 / 8` | Closes one bounded combined-profile selected-JAR Arrow occupancy/output and saved-seat restore/unload slice. Other families, base/Styles-only/Engineered-only client outputs, other-family controller transitions, other profiles, and full source/profile coverage remain open. |
| [Selected-JAR Arrow controller lifecycle rendered outputs](evidence/0.9/fd401d-controller-lifecycle-rendered-selected-656725b-20261105/README.md) | Exact selected JAR `656725bc…`, combined profile: one unoccupied one-car Arrow moved loaded → unavailable → re-entry → unavailable; Ride Fault → Line 1 was configured through the normal Display Link item and UI; server availability, received client telemetry, goggles, and physical sign output matched at each settled state | Closes one bounded combined-profile controller lifecycle-output slice. The loaded state retained a compatible-track fault; no rider, dispatch, or motion was tested. Other families/profiles and the complete source matrix remain open. |
| [Refreshed clean-JAR combined Arrow fault display](evidence/0.9/fd401-current-clean-combined-fault-display-20261105/README.md) | Exact selected JAR `656725bc…`; on the copied combined-profile Arrow fixture, Create goggles and a powered display board showed the existing compatible-track fault, while the validation client snapshot at `(-6,83,32)` recorded the matching full fault, `BOARDING`, zero riders, restraints open, aligned, and dispatch denied | Adds one bounded FD401D combined-profile output-agreement observation. The fixture was already faulted and unoccupied; board text is truncated and no transition was induced. No family/source matrix or other profile is closed. |
| [Arrow controller re-entry dispatch and movement](evidence/0.9/fd401d-arrow-controller-reentry-dispatch-20261105/README.md) | One selected-production-class Arrow GameTest rejects dispatch while the native carrier or chassis is unavailable, restores both, samples `dispatchReady=true`, accepts dispatch, and measures 1.776 blocks of physical movement under controlled velocity | Adds one bounded combined-profile headless recovery slice. Controlled physics supplies motion because dispatch authorizes but does not propel a level track; rendered outputs, normal player propulsion, family coverage, and other profiles remain open. |
| [Combined-profile family telemetry](evidence/0.9/fd401d-combined-family-telemetry-20261105/README.md) | Selected production entries with Styles 1.2.0 + Engineered 1.1.1: all five families, occupied consist snapshots, six Display Link formatters, goggles and update-tag packets; PAX phases and fail-closed guards passed | Closes one bounded combined-profile server-side formatter/output row. Rendered signs, remote client application, normal player operation and broader profile acceptance remain open. |
| [Engineered-only family telemetry](evidence/0.9/fd401d-engineered-only-family-telemetry-20261105/README.md) | Selected production entries with Engineered 1.1.1: all five families, occupied consist snapshots, six Display Link formatters, goggles and update-tag packets; PAX phases and fail-closed guards passed | Closes one bounded Engineered-only server-side formatter/output row. Rendered signs, remote client application, normal player operation and broader profile acceptance remain open. |
| [Styles-only family telemetry](evidence/0.9/fd401d-styles-only-family-telemetry-20261105/README.md) | Selected production entries with Styles 1.2.0: all five families, occupied consist snapshots, six Display Link formatters, goggles and update-tag packets; PAX phases and fail-closed guards passed | Closes one bounded Styles-only server-side formatter/output row. Rendered signs, remote client application, normal player operation and broader profile acceptance remain open. |
| [Base-profile family telemetry](evidence/0.9/fd401d-base-profile-family-telemetry-20261105/README.md) | Selected production entries in base profile: all five families with occupied consist snapshots, six Display Link formatters, goggles formatting and update-tag packets; PAX BOARDING/LIFTING/FAULT and fail-closed guards passed | Closes one bounded base-profile server-side formatter/output row. Actual sign rendering, remote client packet application, and current-artifact Styles-only/Engineered-only/combined profile rows remain open. |
| [Five-family fresh-process recovery](evidence/0.4/recovery/fresh-process-recovery/acceptance.md) and [selected-production linked trains](evidence/0.4/recovery/linked-train-fresh-process-656725bc-20261105/README.md) | Historical single-car pause/resume/release is retained for all five families; current selected-production entries plus a two-class validation overlay restore and release the exact two-car Arrow and four-car PAX graphs across fresh JVMs without automatic tow | FD402D is complete under cumulative validation. The overlay changes only `CarrierValidationFixture` and its synthetic switch helper over selected SHA `656725bc…`; all production entries are byte-identical. The completed FD402E matrix adds multiplayer interruptions in its Arrow and PAX records above. |
| [Two-client Arrow recovery](evidence/0.4/recovery/two-client-recovery-lifecycle/acceptance.md) | Dedicated server and two rendered clients complete fixture-operated attach/tow/brake/detach/unload; retained final save has no rider assignments or recovery link | FD402E reuses this cycle with the completed Arrow interruption and combined PAX removal records above. This individual observation is not relabeled as manual input, reconnect, interrupted restart or all-family evidence. |
| [Manual boarding](evidence/0.4/recovery/manual-boarding/acceptance.md), [bearing attachment](evidence/0.4/recovery/manual-bearing-selection/acceptance.md), [lever operation](evidence/0.4/recovery/manual-lever-propulsion/acceptance.md), and [occupied Arrow continuation](evidence/0.4/recovery/manual-occupied-arrow-operator-20261105/README.md) | Real operator actions cumulatively cover the normal bearing, Attach/Start/Brake, physical lever, occupied braked restore, selected-JAR detach/restraint-open/Shift-unload and cleared assignment; the production editor/recovery/state classes in the first process are byte-identical to the selected JAR | FD402C is complete together with the accepted linked-train mechanics and distinct occupied PAX handover. The temporary interaction-range and power-access diagnostics in the occupied run are disclosed and are not substituted for the separate normal-input passes. |
| [FD402A current-input review](evidence/0.4/recovery/current-input-review-95301062/README.md), [sampled wearables](evidence/0.4/recovery/vanilla-wearable-clearance/acceptance.md), [substeps](evidence/0.4/recovery/remaining-family-native-physics-substeps/acceptance.md), [strength](evidence/0.4/recovery/strength-rating/acceptance.md) | Bounded geometry, solver-pose, load and fail-closed policy results carry forward on current inputs; FD402A is complete | The completed FD402B matrix above adds the observed rendered rider/rack checks. Arbitrary cosmetics and continuous solver-interior trajectories remain outside the qualified scope. |
| [FD403 matrix](FD403-ACCEPTANCE.md) | Existing crafting, partial powered routes and demo observations | Complete each listed connected-client family walkthrough and its save/reopen record; fixture-created layouts cannot substitute for player-built routes. |
| [Four-profile native matrix](evidence/0.9/profile-matrix-abc4a9a/README.md) | Historical 49-test result and its target-host limits | FD901 and FD904: reuse applicable results and perform the outstanding release-level sweep once. Subsequent changes require affected tests/profiles only unless a cross-cutting risk justifies more; record the actual suite count. |
| [Historical migration](evidence/0.9/native-schema3-world-replay.md) | The bounded saved-world upgrade at its original artifact | FD903: current-artifact historical rides, backup restoration and provider lifecycle, with unchanged IDs/profiles. |

Reuse offline verifier results when their relevant geometry, settings, dependencies
and checker logic are unchanged; existing hashes can establish that without
rerunning the verifier. A concise diff review can carry unaffected behavior
forward; it cannot prove a changed interaction. A real failure reopens its case,
not every case in its parent package. One bundle can close several tasks, and
several compatible bundles can close one task. Do not repeat a passing case for
a new label, an unchanged test overlay, or a missing all-in-one screenshot.

The new manifest compares every entry in the clean JAR and the fixed-source
telemetry candidate. The candidate contains exactly one extra untracked GameTest
class and all 356 common entries match byte for byte. This supports scoped
transfer of that server observation to identical packaged production entries,
not a claim that the candidate itself came solely from committed source. The
older `229e586` and `8f370d0` PAX results remain attached to their own JARs
because they ran on those artifacts. Review the specific restoration changes
before carrying their PAX claims forward; class-name changes alone do not order
a replay of every PAX path.

## Updating the record

Update the active queue and owning roadmap row as cases close; preserve the
observation history below its recorded boundaries. Do not derive a fresh work
queue from older per-run limitations.

Keep this qualification baseline stable while collecting evidence. For a relevant
production fix, record the fix artifact and the affected cases; carry unaffected
acceptance forward with a brief change review. Test-only fixtures and documentation
edits do not require baseline reselection or a new qualification JAR. When the
expanded release artifact is frozen at FD1000, reconcile existing evidence and
run only outstanding final checks in FD901–FD905. FD404 records the accepted
original core and does not trigger final release qualification. Keep earlier bundles immutable and
retain their true identities. A freeze is not an instruction to start over.

Verify the retained payloads from the repository root:

```sh
(cd docs/evidence/qualification/2026-11-05-ec2fcda && sha256sum -c SHA256SUMS)
```

**2026-11-15 authority closure:** lead occupied run on display316 saved/stopped; all three units inactive. Pipeline owner returned `client/OriginalTrackRenderer.java` to lead. Lead Additions run uses `/mnt/NVME4/fourthdimension-runs/fd08-additions-20261115`, display316, ports25650/25651, unit prefix `fd08-additions-20261115`, after the previous authority run fully stopped. S2 missing-carrier fault hook and non-overlapping magnetic/lift-lock editor controls applied by lead; S2 owner owns their targeted checks.
