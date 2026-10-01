# Changelog

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
