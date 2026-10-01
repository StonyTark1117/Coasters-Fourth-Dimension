# Changelog

## 0.9.0-dev.15 — guarded ordinary-bearing recovery

- Add explicit selection, native coupling, start, brake and stationary disconnection for certified bare ordinary bearings recovering all five original families. Carriers stay retained, restraints stay closed, and every 4D car requires matching functional track. Ordinary bearings acquire no 4D state.
- Guard ride dispatch, restraint/retention release, stale requests, incomplete or incompatible consists, ordinary gear changes, overspeed and upcoming route ends. Keep disabled cars on matching rails when a recovery link breaks; missing equipment stops towing without automatic resume.
- Persist recovery identity and actual permanent-bearing removal; wait for an unavailable bearing to restore before disconnection. Extend loading dependencies to the whole recovery group.
- Add explicit stationary handover from a loaded PAX winch, removing its physical catch/guide/carriage while preserving the original four-car train and its links.
- Add five flat operator examples and production native checks for fully occupied chain/Create-motor towing, braking, failed-link retention, actual bearing destruction, normal disconnect/unloading, occupied native holding/restoration, save identity, schema-5 loading and PAX handover. Also tow fully occupied two-car Arrow and four-car PAX trains and preserve their original links through normal recovery disconnect.
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
