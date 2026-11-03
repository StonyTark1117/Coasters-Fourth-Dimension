# FD401 evidence handoff for the 0.9 candidate

Read-only audit of release `783d4ba`, 2026-11-02. This checklist requests evidence
using existing behavior; it authorizes no runtime, production edit or new addon.
The coordinator owns runtime scheduling and implementation. Keep base, Styles
1.2.0, Engineered 1.1.1 and their combined profile as the declared candidate set.
FD801 and the [expansion backlog](INTEGRATION-BACKLOG.md) remain separate.

## Evidence already available

- [Exact native matrix](evidence/0.9/profile-matrix-abc4a9a/README.md): 49 required
  tests pass in each profile, with exact dependency hashes and class/JAR parity.
  Engineered-dependent scenarios run only in Engineered/combined. This is native
  development evidence, not four packaged-client certifications.
- [Standalone telemetry](evidence/0.9/create-telemetry/local-046ead9/README.md):
  five-family empty goggles, Arrow occupancy/readiness/retention, normal Display
  Link configuration, late join, reload and occupied restart; PAX phase views.
- Later standalone records add [trailing PAX occupancy](evidence/0.9/create-telemetry/de667f7-trailing-pax/README.md),
  [power-fault display, restart and actual fault clearing](evidence/0.9/create-telemetry/de667f7-pax-power-restart/README.md),
  [empty unload/tracking re-entry and unbound station](evidence/0.9/create-telemetry/de667f7-controller-lifecycle/README.md),
  [fresh Arrow repair/realignment display ordering](evidence/0.9/create-telemetry/effa1c6-realignment-retest/README.md),
  and [PAX normal repair/restart/cycle](evidence/0.9/create-telemetry/8a7ffdb-pax-normal-restoration/README.md).
  These cases must not be listed as wholly untested simply because the earlier
  telemetry README retains its historical remaining-work list.
- [Engineered network loop](evidence/0.9/fd401-engineered-network-loop.md): normal
  seat boarding, editor dispatch/capture/unload, wired FE delivery and a bounded
  occupied circuit return/STOP. The car was fixture-created, the generator was
  creative, the speed target was not attained, and final cold reopen was absent.
- Styles has historical selection/fallback/removal/reinstall and banked GUI
  evidence. Later geometry changes and the [font failure](evidence/0.4/wheel-placement-acceptance.md)
  limit reuse; automated clean reloads do not erase that failure.

## Remaining evidence, in execution order

| Priority / handoff | Concrete observation still required | Pass condition and boundary |
| --- | --- | --- |
| 1. Freeze evidence identity | Select exact candidate source/JAR and all dependency hashes; record any differences from each reused checkpoint. | One explicit evidence map. Never transfer a historical runtime result by version string alone. Native reruns are needed for relevant executable changes, not for every documentation commit. |
| 2. Engineered normal operator loop | On an original closed route with no native chain on motor start anchors, place the car normally; bind/configure motor/controller normally; use station redstone capture/dispatch; complete occupied launch, STOP, capture, open and dismount. | Successful ordinary car placement replaces the earlier failed clicks/fixture spawn. Observe interlock rejection while unready and success only after fresh readiness. Preserve actual target/reached speed; do not call the previous short pass full-speed attainment. |
| 2. Physical FE workflow | Keep `requireEnergy=true`; use a real noncreative power path, wire it normally, run the occupied loop and interrupt/restore supply. | Producer/network/anchor observations establish transfer, starvation/retention and explicit permitted restart; no direct buffer fill. Cold-reopen the completed loop with bindings, spans, controller sequence, wire and identities intact, then operate again. Crafts & Additions remains a disclosed validation-only dependency (`-PfdFeNetwork`), not a new advertised integration. |
| 2. Engineered family/authority coverage | Real operator exercises for the remaining advertised circuit families and a coupled Arrow train; PAX winch owns lift/release/return/capture despite external motor commands. | Pair real controls and client observations with existing native four-family, train rollback, ordinary-cart STOP and PAX opposing-motor assertions. Readiness, carrier capture, family eligibility and catch ownership remain intact. Native coverage alone is not GUI/operator coverage. |
| 3. Dynamic Create output | On the chosen candidate, normally configure each of the six Display Link sources and observe meaningful changes; add trailing Arrow occupancy and Legacy/Free Spin/Revised occupancy/control transitions. | Pair server state, received `FD_CLIENT_TELEMETRY`, visible goggles and actual target text. Include source-preserving fault clear, repair-before-realignment denial, PAX authority and current phases. Existing trailing PAX/fault-clear cases are regression references. |
| 3. Controller availability / optional clients | Keep a controller observable while its actual cars/carriers are unavailable, then restore; exercise late join, tracking re-entry, reload, reconnect and cold restart. Repeat relevant telemetry/operator/render checks in the optional profiles. | Show unavailable rather than stale-ready output on both goggles and target. The historical empty unload had a non-ticking blank sign and does not prove this presentation. Combined results do not automatically certify individual-profile clients. |
| 4. Styles geometry and visible ownership | For both supported Arrow styles, verify current placement on level, curved/banked, inversion/full-turn and style-transition sections, with occupied single/coupled cars. | Running/control rails, ties and spine each have one owner. Include banked wheel, chassis, carriers, riders and linked-car sweep; existing control-rail and level-wheel audits exclude these broader volumes. Preserve profiles/family/eligibility through changes, unsupported stroke fallback, missing models, removal/reinstall and reopen. |
| 4. Styles rendering / reload | Complete a fresh post-reload GUI ride on the revised placement; collect actual F3+T input/FontManager/catalog evidence, post-reload chat/editor, capture/unload and normal exit. Exercise declared graphics modes and a pinned shader setup. | Attribute each result to actual GPU/backend/pack. Flywheel falling back to `off` with a shader is not accelerated rendering. Keep the old font crash open unless diagnosis and acceptance justify closure; the auditor owns its current investigation. |
| 5. Release evidence review | Verify payload/world hashes, source/JAR correspondence and claims against raw records; update FD401 and support matrix only for proven combinations. | All advertised acceptance satisfied or an explicit coordinator/user scope decision, never silently waived criteria. Full FD402/FD403 and FD901–FD905 remain separate gates. |

## Work that can be done while runtimes are occupied

Review committed logs/screenshots/manifests, compare source and artifact identities,
decode copies of stopped saves, and rerun existing Python geometry/verifier tools
on retained inputs. These activities cannot prove fresh UI, packet delivery,
hardware performance or normal operation. The [retained geometry audit](evidence/0.9/fd401-retained-geometry/README.md)
records one such offline result and its frame-provenance limitation.

Use the [client telemetry protocol](evidence/0.9/create-telemetry/client-acceptance.md)
for the scheduled runs. It requires two real clients and normal Create controls.
For packaged profiles use `-PfdPackaged -PfdValidation`, adding only the applicable
`-PfdIntegrations=styles`, `engineered`, or `styles,engineered`. Do not launch from
this checklist until the coordinator hands over the runtime. Existing native
performance targets and geometry margins remain unchanged.

For each future run retain exact commands/configuration, normal item/GUI actions,
server/client observations, failed attempts, screenshots, complete logs, actual
process exit codes, and pre/post-restart whole-world copies/hashes. An RCON write,
fixture-created car, forced mount, direct FE fill or generated display text must
be disclosed and cannot stand in for the corresponding operator criterion.
If a required step exposes a mechanics defect, preserve it and return the narrow
reproduction to the implementation owner; this stream does not patch around it.
