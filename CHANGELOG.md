# Changelog

## 0.9.0-dev.24 — sampled occupied native-rider clearance

- Extend the source-pinned recovery trace with each live mounted passenger's eye point and native dimensions. The independent audit reconstructs its carrier-oriented volume and requires at least one mounted rider on every original car for every twenty-tick sample.
- The combined Track Styles/Engineered run passes all 37 required GameTests. Its five single-car routes and two occupied original train routes certify at least a 0.08-block sampled rider-to-original-track bound, above the 0.02 required margin.
- This is a bounded native Pig-passenger occupancy result. It does not certify player meshes/cosmetics, the client renderer, a rider's own rack/seat contact, linkage, continuous sweeping or real-client operation. FD402 remains in progress.

## 0.9.0-dev.23 — sampled banked wheel-envelope clearance

- Extend the source-pinned recovery audit with full-spin envelopes for the pinned native wheel model at each recorded banked recovery frame. Exclude only the two running rails as intentional wheel contact; check all other original track components and all neighboring original-car volumes.
- Record conservative sampled wheel-envelope bounds of 0.04602 blocks across the five single-car routes and 0.04608 blocks across original Arrow/PAX trains, above the 0.02 required margin. Retain continuous curve/bank motion, riders, linkage, decorations and real-client rendering as separate FD402 work.

## 0.9.0-dev.22 — native lower-bearing clearance truss

- Replace Original running-rail crossbars and diagonal mounts with an outboard, below-channel visual truss. Functional rails, track family eligibility and native vehicle physics are unchanged.
- Extend the source-pinned sampled recovery audit to include the separately rendered native lower-bearing model at the recorded chassis frame. Keep rotating wheels, players, linkage, decorations and continuous swept motion outside its stated scope.
- Correct the wheel-clearance audit so vertical and horizontal support hulls cannot become degenerate and pass by omission. Validate the resulting support geometry against the full native wheel envelope on a level straight.
- FD402 remains in progress: this clears a measured visual-support candidate and adds sampled lower-bearing evidence; it does not certify banked wheel sweeps, player clearance or rendered operator operation. Save schema/network protocol remain 6.

## 0.9.0-dev.21 — sampled recovery block-model clearance

- Record actual native chassis/carrier/ordinary-body poses and placed block states every twenty ticks during the existing occupied five-family and original Arrow/PAX train recovery tests. Recording does not change motion, forces or saved gameplay state.
- Add an independent source-pinned reconstruction of original track tube triangles and default block-model volumes. Check sampled track clearance and model volumes belonging to different original cars, including the ordinary recovery body. Reject missing pose intervals, replaced bodies, unsupported resources and intersecting negative controls.
- Run the audit for all four CI profiles and retain pose traces, native frames and clearance reports. Keep dev.20’s twelve passed CI jobs pinned separately.
- This is sampled default block-model evidence. Special native wheel/bottom rendering, player meshes, linkage, decorations, continuous swept clearance and real occupied operator/lifecycle acceptance remain open under FD402. Save schema/network protocol remain 6.

## 0.9.0-dev.20 — occupied train recovery across bends and banks

- Add longer original recovery layouts for two-car Arrow and four-car PAX trains, with four-second queued native coupling and `/fd demo recovery_bank_train_arrow` / `recovery_bank_train_pax` operator examples.
- Require every original car to traverse the full quarter bend, sustained 30-degree bank, bank-down and level runout before braking. Preserve all 24 original native seats, carrier IDs and original train links through external power cut, stationary recovery disconnection and native unloading. Measure every chassis and ordinary bearing against native track anchors/orientations.
- Share native route-frame export between single-car and train fixtures; independently check both extended routes in CI and retain their frames for later clearance work. Keep prior dev.19 acceptance and CI evidence pinned to its original revision.
- Keep FD402 and the 0.4/0.9 gates open for complete rendered vehicle/rider/rack clearance and occupied operator/lifecycle/propulsion coverage. The compact occupied PAX spike limitation remains. Save schema/network protocol remain 6.

## 0.9.0-dev.19 — occupied curved and banked recovery

- Add five original native recovery routes with a 90-degree bend, 32-block sustained 30-degree bank, transition to level and level runout. `/fd demo recovery_bank_<family>` uses the same tool/editor, eight-RPM motor and switchable chain-power workflow as the flat examples.
- Tow one fully occupied retained car from each family using the certified bare ordinary bearing and native coupler/chain propulsion. Preserve all 32 original native passenger seats and track profiles, including Arrow’s full controlled turn, through manual braking, power cut, stationary disconnection and native unloading.
- Measure native body/track anchor and orientation offsets. Export native frames; add an independent geometry check and CI artifact retention for quarter-turn, joint continuity, sustained bank and nonzero-profile evidence.
- Keep FD402 and the 0.4/0.9 gates open for linked-train curved/banked recovery, complete rendered clearance/operator acceptance and lifecycle/propulsion coverage. The compact occupied PAX spike limitation remains. Save schema/network protocol remain 6.

## 0.9.0-dev.18 — powered recovery stall detection

- Detect insufficient physical displacement during active native chain recovery: a commanded speed above 0.1 blocks/s with the bearing remaining within 0.08 blocks for four physics seconds brakes the entire group and requests propulsion inspection. Keep original seats, matching track, native couplings and retention; do not automatically resume.
- Reset the transient progress window on power cut, unavailability and normal start/brake/disconnect. Waiting with the clutch cutting power does not fault. Native drive force and speed cap are unchanged.
- Preserve the failed whole-train compact PAX spike diagnostic: the loaded bare bearing stalls despite active chain input, while an unlinked bearing ascends. Add a regression for automatic driven-stall braking, power stop, original-link disconnection and native unloading with sixteen original occupied seats.
- Keep FD402 and the 0.4/0.9 gates open. Whole-spike recovery, complete curve/bank clearance and rendered occupied recovery remain unverified. Schema/network protocol remain 6.

## 0.9.0-dev.17 — occupied PAX recovery handover

- Validate one fully occupied sixteen-seat PAX train from its powered physical winch through interruption, retained handover, native-chain towing, braking, power stop, recovery disconnection and normal unloading. Preserve original chassis/carrier IDs, native train links and passenger seat mounts; reject active/unretained handover and moving disconnection.
- Record standalone and combined native acceptance separately from rendered player operation. FD402 remains in progress in the 0.4 stage toward 0.9.0. Save schema/network protocol remain 6.
- Correct the README artifact version and make the active work package and remaining gate statuses explicit in the roadmap.

## 0.9.0-dev.16 — switchable recovery power and guidance

- Add a native Create clutch and vanilla lever to recovery examples: ON cuts chain power and OFF supplies it. The motor can remain installed at eight RPM after braking and power stop.
- Add recovery action guidance, live state/fault display with wrapped hover text, and disable incompatible ride controls while attached.
- Record five-family native power checks and an empty Arrow real-client manual brake, power stop, same-ID fresh-JVM restoration, disconnection and restraint opening. Startup movement warnings and the broader rendered/lifecycle gates remain open.
- Correct generated translation ordering after CI detected resource-generator drift; the corrected revision passes push/PR CI.

## 0.9.0-dev.15 — guarded ordinary-bearing recovery

- Add explicit selection, native coupling, start, brake and stationary disconnection for certified bare ordinary bearings recovering all five original families. Carriers stay retained, restraints stay closed, and every 4D car requires matching functional track. Ordinary bearings acquire no 4D state.
- Guard ride dispatch, restraint/retention release, stale requests, incomplete or incompatible consists, ordinary gear changes, overspeed and upcoming route ends. Keep disabled cars on matching rails when a recovery link breaks; missing equipment stops towing without automatic resume.
- Persist recovery identity and actual permanent-bearing removal; wait for an unavailable bearing to restore before disconnection. Extend loading dependencies to the whole recovery group.
- Add explicit stationary handover from a loaded PAX winch, removing its physical catch/guide/carriage while preserving the original four-car train and its links.
- Add five flat operator examples and production native checks for fully occupied chain/Create-motor towing, braking, failed-link retention, actual bearing destruction, normal disconnect/unloading, occupied native holding/restoration, save identity, schema-5 loading and PAX handover. Also tow fully occupied two-car Arrow and four-car PAX trains and preserve their original links through normal recovery disconnect.
- On the unchanged JAR, record an empty Arrow real-client workflow through same-ID braked fresh-JVM restoration, explicit start, route-end braking, power stop, disconnect and restraint reopening; preserve identities/bindings/profiles and normal client exits. Recovery-specific teaching and switchable demo power control remain needed.
- Use save schema/network protocol 6; read existing schemas 1–5 without assigning recovery state. Clients and servers upgrade together. Full FD402 rendered/curved/banked/strength/lifecycle acceptance and the 0.4/0.9 gates remain open.

## 0.9.0-dev.14 — caught boarding restoration

- Record FD402 mechanical feasibility: fifteen flat native-coupler pulls across five families, including fully occupied retained carriers. Archive the diagnostic fixture/policy separately; production mixed-consist policy and the dev.14 artifact remain unchanged. Normal recovery workflow and full towing acceptance remain open.
- Preserve a stationary boarding shuttle with its existing physical carriage while native bodies unload/restore, and rebuild its catch when both bodies return.
- Fault on actual permanent removal of an assigned native component; reject operations with a missing carriage before advancing phase or allocating a replacement.
- Record normal recovery of four inherited PAX faults, the failing dev.13 cold reopen, a native holding regression, and a fixed unoccupied default-flags boundary reopen with stable IDs/rails and zero saved faults.
- On the unchanged dev.14 artifact, record one real rider through two fresh-JVM caught-boarding restores, a full post-reopen ride and normal unloading, with preserved rider/carrier assignment and no duplicate loaded seat or saved fault. Complete multiplayer/lifecycle coverage remains open.
- Pass 25 unit tests and all 30 required native tests in standalone and combined profiles. Complete 0.4 and FD902 acceptance remain open, including the full occupied lifecycle matrix and the original bitmap-glyph crash.

## 0.9.0-dev.13 — idle shuttle availability

- Preserve BOARDING state while a never-lifted PAX train is in native holding. An idle winch without a carriage no longer faults or closes restraints merely because its lead is unavailable.
- Keep dispatch validation for native bodies, actual consist, matching tracks, restraints, readiness, route and power. Existing faults require explicit recovery; active and caught-carriage behavior is unchanged.
- Add a native regression that fails against the original controller, preserves idle state with the fix, restores all four cars/eight carriers, and rejects missing-lead or deleted-support dispatch. Standalone and combined runs pass all 29 native tests; all 25 unit tests pass.
- Record the packaged loading-boundary control/fix and default-flags cold reopen with stable IDs/profiles. Add an at-a-glance current-stage summary to the roadmap; full 0.4 and 0.9 gates remain open.

## 0.9.0-dev.12 — Veil scheduler initialization

- Defer native Veil 4.1.4 client workers until the constructor publishes its final worker count; complete zero-worker schedules. Pin this client integration to that exact release.
- Add opt-in installed-scheduler validation: 512 schedules, 128 empty schedules and 3072 tasks checked exactly once after native completion. Standalone and combined native regressions pass.
- Record the resource-reload investigation separately from the unresolved dev.8 bitmap-glyph crash. No font cache, image, render return or exception is replaced.
- Record 64 completed reload/chat/query cycles and a subsequent occupied styled Arrow ride. A default-client cold reopen exposes four PAX lead-unavailable faults; the lifecycle gate remains open.
- Keep save schema/network protocol 5 and all unfinished 0.4/0.9 gates.

## 0.9.0-dev.8 — native wheel clearance for installed Arrow decoration

- Lower and adapt separately installed Arrow decoration to clear the native main/upstop wheel swept envelopes on a level straight.
- Retain original main-rail supports with external styles and connect the lowered spine with a native crossbrace.
- Add a pinned default-wheel geometry audit, reproduce the previous tie-envelope failure, and repeat the recorded native-bank control-rail check with production placement constants.
- Full banked wheel, chassis, carrier, rider and linked-car clearance remains open under FD401.
- Preserve a packaged-client bitmap-glyph crash after F3+T; new GUI reload/ride acceptance remains open while its cause is investigated.

## 0.9.0-dev.7 — banked GUI evidence and current shuttle readiness

- Record packaged dev.6 editor styling on the native banked circuit, real resource reload, cold reopen, normal occupied ride, station recapture, dismount and clean save/exit. Preserve all profiles and existing mechanical identities; add a saved-observation verifier to CI.
- Correct the sixteen-rider PAX test's assumption that a completed cycle guarantees current readiness after native physics. Require ten consecutive ready ticks within a bounded four-second settling window, exercise real post-capture native motion, verify rejected opening leaves the whole consist unchanged, and unload normally after recovery. Production boarding thresholds and restraint checks are unchanged.
- Keep schema/network protocol 5. FD401 and the 0.9 release gates remain open for complete vehicle/rider clearance, render modes, Engineered operator/FE-network workflows and the remaining roadmap packages.

## 0.9.0-dev.6 — native banking and recorded mesh clearance

- Add `/fd demo arrow_bank`, with a level station, native 30° banked upper section and full controlled turns on both banked and level straights.
- Resolve shared endpoint normals and lifted centers before installing curves; place banked chain motors on the high side with the matching rotation sign.
- Check joint center/frame continuity, real passenger retention, both full turns, station capture and unloading in a native circuit test.
- Export our native render frames and independently audit both separately installed default Arrow spine/tie meshes against the rendered square control rails over the full supported stroke. Run that audit in Styles/combined CI. No provider vertices or assets are saved or bundled.
- Schema/network protocol remains 5. This advances FD401; banked GUI/rendering, full vehicle/rider clearance, operating workflows and the remaining 0.9 gates are still open.

## 0.9.0-dev.5 — styled ride and native FE validation

- Record a real packaged dev.4 Arrow circuit with double-spine controlled track, a single-spine flat quarter curve, station dispatch/recapture and verified dismount.
- Preserve double-spine intent and a +1080° profile through Original fallback and cold reopen; restore +360° styling and verify resource reload.
- Add an isolated native Engineered FE batch using its real anchor capability: empty-buffer starvation, supplied launches, regenerative braking, PAX ownership and retained-car holds without energy consumption. Restore the previous energy configuration after the batch.
- Add committed GUI/save observation verifiers to CI. Schema/network protocol remains 5. Banking, complete clearance, rendering modes and the complete Engineered operator workflow still gate FD401.

## 0.9.0-dev.4 — powered controlled train and decorative stroke guard

- Validate an actual Engineered-driven coupled Arrow train through independent full-turn axle targets, STOP hold, powered rollback, recapture and passenger unloading.
- Audit the pinned Arrow tie/control-rail geometry over continuous stroke ranges. Both installed tie models intersect the upper +3..+4-turn range.
- Restrict decorative selection to profiles at or below +720°. Higher existing or newly edited profiles retain all angles and saved appearance intent while Original geometry is shown; lowering the profile restores decoration eligibility.
- Keep schema/network protocol 5. Curves/banking, styled GUI rides and broader rendering/operator checks remain open in FD401.


## 0.9.0-dev.3 — bounded operating integration

- Restrict both inspected Arrow decorative styles to controlled Arrow functional track; all other families retain Original.
- Add exact-version optional Engineered 1.1.1 adapters for station/maintenance propulsion holds, exclusive PAX winch ownership, and rotary-body assembly mass. Ordinary bearings retain native membership and behavior.
- Exercise real installed controllers through four-family launch, STOP, carrier capture, unloading and maintenance retention; test the ordinary bearing and opposing motor spans during complete PAX cycles. FE-enabled operation and complete powered rotation-profile rides remain unverified.
- Add standalone, Styles-only, Engineered-only and combined validation jobs to CI. Schema/network protocol remain 5.

This checkpoint leaves FD401 and the 0.4/0.9 gates open. See `docs/evidence/0.4/operating-acceptance.md` for the exact test scope and limitations.

## 0.9.0-dev.2 — partial optional appearance integration

- Add guarded section appearance editing, independent saved style/color intent, and optional installed Track Styles spine/tie rendering.
- Retain original running/control rails and all family, profile and eligibility behavior; restore original geometry when the appearance provider is missing.
- Verify a single-spine Arrow straight through resource reload and real provider removal/reinstallation. Other style geometry and operating integrations remain in FD401.
- Use schema/network protocol 5. Schema 1–4 load forward; clients and servers must use matching builds. Back up worlds and do not downgrade upgraded saves.

This remains a development build toward 0.9, retaining the bounded five-family 0.3 gate. It is not the release candidate; see `ROADMAP.md` for outstanding acceptance gates.

## 0.2.0 — magnetic Free Spin

- Add original matching blue track and an eight-seat car with four independent native carrier pairs.
- Add bounded, targeted passive magnetic inducer/damper spans with swept forward/reverse traversal and reciprocal energy loss.
- Add survival equipment installation/refunds, server-validated zone editing, loading presets and declared loading previews.
- Extend capture, restraints, family matching, ordinary-bearing access, persistence and multiplayer to all four groups.
- Add recipes and `/fd demo free_spin`; retain the original Arrow and legacy families.
- Upgrade saved data to schema 3 and networking to protocol 3. Both clients and servers must use 0.2.0. Schema 1/2 worlds load forward; back up before upgrading and do not downgrade an upgraded world.

The ±1024 horizontal coordinate limit remains. Per dimension: 256 sections, 64 vehicles, 8192 profile keys and 512 magnetic spans, at most eight per section. Free Spin cars require twelve-block axle separation and cannot couple. Four-pair topology and magnetic response are inspired game approximations.

## 0.1.0 — controlled and legacy families

Original Arrow/S&S-style controlled track/cars and legacy ZacSpin-style free track/cars, rotation editing, native loading, Arrow train linking, boarding/retention, ordinary-bearing certification and demonstration circuits. See `docs/PLAYABLE.md` for the historical gate.
