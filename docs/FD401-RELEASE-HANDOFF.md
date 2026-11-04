# FD401 evidence handoff for the 0.9 candidate

Initial read-only audit of release `783d4ba`, updated through `ff7c831` on
2026-11-03 for the corrected missing-font source and retest. This checklist requests evidence
using existing behavior; it authorizes no runtime, production edit or new addon.
The coordinator owns runtime scheduling and implementation. Keep base, Styles
1.2.0, Engineered 1.1.1 and their combined profile as the declared candidate set.
FD801 and the [expansion backlog](INTEGRATION-BACKLOG.md) remain separate.

## Evidence already available

- [Historical occupied-reload fixture](evidence/0.9/fd401-occupied-reload-fixture-20261103/README.md)
  preserves its earlier uncommitted-source client JAR and raw logs. The programmatic
  reload completed before automatic fixture dispatch; the gate records 493 mounted
  ticks, 890 global geometry stages and 1.082 blocks of chassis motion, with zero
  Styles decoration pieces. Normal shutdown is recorded. These are not 493 ticks
  of moving ride or evidence for the later `1a91416` through-reload identity gate.
  [Independent review](evidence/0.9/occupied-reload-historical-review-2026-11-03.json)
  verifies tracked payload hashes and preserves source/exit provenance limits.
  Its separate combined-profile 50-test development run does not replace the
  four-profile matrix or manual operator/render acceptance.
- [Isolated native capture](evidence/0.9/fd402-isolated-base-ff7c831/README.md):
  base-profile source `ff7c831`, 50 required tests passed, normal server shutdown,
  exact same-route decoration transforms and matching one-tick body traces.
  This run did not enable target-host performance limits and is not the optional
  four-profile matrix or a rendered-client result.
- [Prospective Styles recovery geometry](evidence/0.9/fd401-prospective-styles-ff7c831/README.md):
  both single-car Arrow styles pass 5,768 sampled poses across five sections with
  minimum separating bound 0.04891048 blocks against 0.02 required and no
  uncertified body/track pairs. Both two-car Arrow configurations also pass 6,548 poses with a minimum
  bound of 0.04878235 blocks against the same requirement. These
  default-model proposals are not observed Styles rendering, player meshes,
  track-to-decoration self-clearance, continuous sweeps or full-turn operation.
- [Prospective whole-section appearance transitions](evidence/0.9/fd401-styles-transitions-ff7c831/README.md)
  also pass 6,548 two-car Arrow poses with minimum bound 0.04878235 blocks against
  0.02 required. The plan is Original → single → double → Original → single.
  This adds sampled body clearance at those section transitions, not UI edits
  within a curve, decoration self-clearance, player meshes or visible rendering.
- Draw-fallback corrections `21bfd1a` / `d5132a3` catch provider draw failures,
  restore the suppressed Original spine, and quarantine that rail's styled mesh
  until rail/model revision. The failure frame may retain partial provider output;
  the next frame uses full Original geometry. Source/helper tests do not prove a
  rendered injected-failure/reload recovery, which remains acceptance work.
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
- [Bare-loop ordinary car placement](evidence/0.9/fd401-bare-item-placement/README.md):
  packaged client on `61a9884` aimed the Arrow car item at the open interior
  rail, placed it by normal right click, then bound the station by empty-hand
  click. A focused native survival item-use case consumed one car item. This
  new world had no motor, FE network or occupied ride; it does not convert the
  earlier fixture-created circuit into an ordinary-placement ride.
- [Normal Engineered loop with noncreative FE](evidence/0.9/fd401-engineered-normal-loop/README.md):
  the same item-placed car was boarded normally, driven by a client-fitted motor
  and linked controller with a three-water-wheel alternator, and captured by
  station redstone after an occupied circuit. Native state recorded 19.385 of
  20 blocks/s, FE consumption, and a starved pulse after power interruption.
  The restored low-buffer pass did not complete STOP; the saved car is safely
  unloaded away from the station. There was no FE wire or cold repeat operation.
- [Normal STOP and cold repeat](evidence/0.9/fd401-engineered-stop-cold/README.md):
  the same item-placed car, normally fitted motor, linked controller, and original
  station completed `Launch 20 → Stop at 12 → End`, held at STOP inside station
  range, was captured by station redstone, and unloaded. A cold reopen preserved
  the ride and repeated the operator cycle. Both passes remained just short of
  the 20 blocks/s target. This run used adjacent noncreative water-wheel FE.
- [Wired water-wheel FE and cold repeat](evidence/0.9/fd401-engineered-wired-waterwheel/README.md):
  a separated three-water-wheel alternator supplied the motor through a copper
  spool link made in the client. Reciprocal connector nodes and FE rise from zero
  survived cold reopen. Source interruption stopped transfer; restoration and
  recharge supported another occupied STOP, station capture, and unload. Exact
  runtime-JAR identity still needs a controlled freeze because concurrent builds
  shared the output directory.
- [Legacy, Free Spin and Revised operator loops](evidence/0.9/fd401-engineered-family-operator/README.md):
  validation-only bare circuits allowed normal motor fitting, matching car-item
  placement and client-linked controller programs. Each family completed an
  occupied circuit and STOP, then station redstone capture and normal unload.
  All three `Launch 8` passes reported `PassShort=0` without starvation. Their
  FE buffers were fixture-supplied, while physical FE delivery is evidenced
  separately above. A controlled artifact freeze remains necessary.
- Styles has historical selection/fallback/removal/reinstall and banked GUI
  evidence. Later geometry changes and the [font failure](evidence/0.4/wheel-placement-acceptance.md)
  limit reuse; automated clean reloads do not erase that failure.
- The [isolated FANCY post-reload ride](evidence/0.9/fd401-fancy-styles-reload-ride/README.md)
  applies double-spine styling, performs a real F3+T reload, then normally boards,
  dispatches, completes the styled circuit, station-captures and unloads. The
  [render-mode record](evidence/0.9/fd401-styles-render-modes/README.md) adds strict
  FAST and FABULOUS mode-guard passes plus a visible Iris/Solas V3.7b run. All use
  isolated displays/game directories and clean exits. The mode-only runs are
  stationary and the Solas run records Flywheel's expected `off` fallback.
- The [final-source interactive font retest](evidence/0.9/fd401-final-source-retest-1a91c94/README.md)
  records three actual F3+T reloads on source `1a91c94`, combined profile,
  llvmpipe, with post-reload chat, inventory, pause menu and normal exit 0.
  Independent review at `f47f342` verified all 13 manifest hashes, apply generations
  1–4, one default-font redirect in generation 3 and the rendered generation-4
  pause menu. This replaces “no interactive final-source retest” with a targeted
  pass; it does not prove a post-reload occupied ride or other graphics modes.
- The [removed-custom-font retest](evidence/0.9/fd401-missing-font-retest-05ad4a6/README.md)
  runs the corrected source `05ad4a6` in the combined profile. The file-pack font
  loaded, supplied a custom-styled chat message, and disappeared after F3+T when
  its JSON was removed. Earlier styled chat rendered missing-glyph boxes; inventory,
  pause, save and title completed with client exit 0. The run did not naturally
  use the stale-removed-font `missingFallback=true` redirect, so that branch
  remains untested by runtime evidence.

## Remaining evidence, in execution order

| Priority / handoff | Concrete observation still required | Pass condition and boundary |
| --- | --- | --- |
| 1. Freeze evidence identity | Select exact candidate source/JAR and all dependency hashes; record any differences from each reused checkpoint. | One explicit evidence map. Never transfer a historical runtime result by version string alone. Native reruns are needed for relevant executable changes, not for every documentation commit. |
| 2. Engineered normal operator loop | Review the completed STOP/capture/unload and cold repeat against the exact frozen candidate. | The normally placed car completed occupied circuits, a programmed STOP hold inside station range, station redstone capture, and unload in initial and cold runs. An unready station release was rejected before launch. Launch 20 reached about 19.40 with `PassShort=1`; exact speed attainment remains unproven. |
| 2. Physical FE workflow | Freeze and review the wired evidence on the exact candidate. | With `requireEnergy=true`, the noncreative water-wheel alternator delivered through a normal copper-spool wire, stopped on source interruption, resumed on restoration, persisted through cold reopen, and powered another occupied STOP/capture/unload. Crafts & Additions remains a disclosed validation-only dependency (`-PfdFeNetwork`), not a new advertised integration. |
| 2. Engineered family/authority coverage | Freeze/review the coupled Arrow and PAX authority evidence against an exact candidate. | Legacy, Free Spin and Revised have normal car-item placement, client-fitted motor, linked GUI controller, occupied full circuit/STOP, station redstone capture, and unload. A [normally item-placed, chain-linked two-car Arrow train](evidence/0.9/fd401-engineered-coupled-arrow/README.md) completed an occupied circuit, programmed STOP, station capture and unload. An [occupied PAX winch cycle](evidence/0.9/fd401-engineered-pax-authority/README.md) completed against a powered `Launch 20` Engineered controller, with pass zero, FE unchanged, native capture and unload. Shared-build JAR identity and PAX per-car rejected callback remain evidence limits; pair with existing native assertions. |
| 3. Dynamic Create output | On the chosen candidate, normally configure each of the six Display Link sources and observe meaningful changes; add trailing Arrow occupancy and Legacy/Free Spin/Revised occupancy/control transitions. | Pair server state, received `FD_CLIENT_TELEMETRY`, visible goggles and actual target text. Include source-preserving fault clear, repair-before-realignment denial, PAX authority and current phases. Existing trailing PAX/fault-clear cases are regression references. |
| 3. Controller availability / optional clients | Keep a controller observable while its actual cars/carriers are unavailable, then restore; exercise late join, tracking re-entry, reload, reconnect and cold restart. Repeat relevant telemetry/operator/render checks in the optional profiles. | Show unavailable rather than stale-ready output on both goggles and target. The historical empty unload had a non-ticking blank sign and does not prove this presentation. Combined results do not automatically certify individual-profile clients. |
| 4. Styles geometry and visible ownership | For both supported Arrow styles, verify current placement on level, curved/banked, inversion/full-turn and style-transition sections, with occupied single/coupled cars. | Running/control rails, ties and spine each have one owner. Include banked wheel, chassis, carriers, riders and linked-car sweep; existing control-rail and level-wheel audits exclude these broader volumes. Preserve profiles/family/eligibility through changes, unsupported stroke fallback, missing models, removal/reinstall and reopen. |
| 4. Styles rendering / reload | Freeze and review the new occupied FANCY reload ride and FAST/FABULOUS/Solas records against the chosen candidate; add tracing-disabled and repeated-reliability acceptance if they remain release claims. | The bounded runs now retain actual F3+T/catalog evidence, post-reload styled operation, capture/unload, all three strict graphics-mode markers and the pinned shader pack. Solas correctly records Flywheel `off` and is not accelerated-rendering evidence. The mode-only replays are stationary, all observations use llvmpipe, and a tracing-enabled replay does not itself prove tracing-disabled operation. |
| 5. Release evidence review | Verify payload/world hashes, source/JAR correspondence and claims against raw records; update FD401 and support matrix only for proven combinations. | All advertised acceptance satisfied or an explicit coordinator/user scope decision, never silently waived criteria. Full FD402/FD403 and FD901–FD905 remain separate gates. |

### Font recovery source boundary

Read-only inspection at `f47f342` confirms that live-set publication in
`FontReloadTrace.applyEnd` occurs before its diagnostic guard, and all four
lookup redirects in `FontSetReloadTraceMixin` run without `fdFontValidation`.
The flag controls tracing, not the recovery path. This is source evidence;
both retained interactive replays enabled tracing.

Source `05ad4a6` changes `replacementForClosedSet` to return Minecraft's actual
`missingFontSet` when a removed custom ID is absent from the new map. This fixes
the source gap found at `f47f342`; it does not itself prove the branch at runtime.
The new removed-font run shows normal fallback rendering after a resource-pack
font disappears, but no stale custom `FontSet` lookup or `missingFallback=true`
redirect occurred. Keep direct stale-reference and tracing-disabled acceptance
open before claiming general recovery. Do not relabel the observed default-font
redirect (`missingFallback=false`) as a test of the removed-font branch.

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

For future native profile runs, use the [post-exit export capture helper](NATIVE-EVIDENCE-CAPTURE.md)
before another run deletes the five generated frame/trace files. Capture failures
remain evidence gaps; archiving cannot turn a failed runtime into a passing one.

### Same-route decoration inputs and remaining Styles clearance

The [historical offline sampling inventory](evidence/0.9/fd401-decoration-frame-gap.json)
found **151 missing spine and 37 missing tie frames** on each retained Arrow
recovery route. Those older records remain unchanged; interpolated frames still
cannot stand in for measured native placement inputs.

The [isolated `ff7c831` capture](evidence/0.9/fd402-isolated-base-ff7c831/README.md)
now supplies exact native spine/tie inputs on the same single-car and train routes
as their one-tick body traces. Both archived input-verifier reports pass, retaining
source/input hashes and chassis identity pairing. The observation-only exporter
preserves the original rail grid and adds native frames, renderer-oriented profile
values and exact model transforms. The export-input gap is therefore resolved for
these frozen fixtures; it is not a general vehicle-clearance pass.

The [prospective Styles mesh audit](NATIVE-EVIDENCE-CAPTURE.md#prospective-styles-recovery-geometry-audit)
includes altered control mounts, crossbraces and endpoint bridges alongside the
provider OBJ geometry. Both single-car configurations have
[bounded passing reports](evidence/0.9/fd401-prospective-styles-ff7c831/README.md);
both two-car configurations now have bounded passing reports as well. Proposed appearance is explicitly separate
from saved/observed appearance. Preserve each result, including any uncertified
pairs. Recovery towing does not substitute for a full-turn operating ride, player
meshes, continuous swept clearance, style transitions or visible render ownership.

The [post-fallback source review](evidence/0.9/post-fallback-geometry-pin-review-2026-11-03.json)
finds the normal geometry constructors unchanged by the draw-fallback correction.
That permits reviewed future audit pins, not relabeling frozen-source calculations
as post-fix runtime evidence. New source must still pass the applicable checks.

For each future run retain exact commands/configuration, normal item/GUI actions,
server/client observations, failed attempts, screenshots, complete logs, actual
process exit codes, and pre/post-restart whole-world copies/hashes. An RCON write,
fixture-created car, forced mount, direct FE fill or generated display text must
be disclosed and cannot stand in for the corresponding operator criterion.
If a required step exposes a mechanics defect, preserve it and return the narrow
reproduction to the implementation owner; this stream does not patch around it.
