# FD501 decisions — 2026-11-14 America/Phoenix

The [core decision](README.md#core-decision--ready-for-lead) is usable immediately
by FD501A/Wing. This inventory completes the separate optional decisions; none
activates an addon or certifies interoperability. The [dated catalog](catalog-20261114.json)
records 14 exact artifacts, SHA-256, recursively embedded mod IDs/dependencies,
mixins, source URLs and persistent local paths. Ten unchanged candidates were
rehashed from the November 2 cache; newer Skin/Immersion and the two previously
missing Additions dependencies were downloaded outside runtime directories.
All 14 JARs opened successfully and matched recorded identities. The old catalog
remains historical evidence. Discovery used official project main-file listings
and exact file pages, not a claim to an exhaustive ecosystem census.

## Selected implementation candidates

Versions below are selected inspection baselines for the later consumer. Required
dependency ranges and sides are preserved verbatim in the JSON; this table names
the resulting closure and implementation decision.

| Package / consumer | Exact candidate and actual identity | Dependency / capability decision |
| --- | --- | --- |
| FD501B → FD810 station / FD911 seats | [Additions file 9243346](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions/files/9243346), published 0.2.5 beta, runtime `coasterfins` **0.2.2** | Keep current core. Add [Aeronautics bundle 1.3.2, file 8763471](https://www.curseforge.com/minecraft/mc-mods/create-aeronautics/files/8763471) and [Interiors 0.6.1 v3, file 8974633](https://www.curseforge.com/minecraft/mc-mods/interiors/files/8974633). Required IDs `simulated >=1.3.0` and `interiors >=0.6` are resolved, not unavailable upstream. Station signals/requests consume immutable whole-consist telemetry and validated dispatch; optional seats separately consume authoritative restraint/mount restoration. No core schema migration is required for read-only station signals. |
| FD501C → FD811 | [CTNB file 8718286](https://www.curseforge.com/minecraft/mc-mods/ctnb/files/8718286), `coasters_extras` 1.2 | Current core satisfies Create `[6.0.0,7.0.0)` and Coasters `[0.1.2,0.2.0)`. Same mod as former Extras; never install both names. Inspect actual materials and selected station/launch/reverse/crossing behavior before enabling eligibility. New external functional-provider identity, if selected, belongs to this package, not Wing. Unknown geometry/behavior stays rejected. |
| FD501D → FD812 | [Magnetized file 8668703](https://www.curseforge.com/minecraft/mc-mods/coasters-magnetized/files/8668703), `coastersmagnetized` 1.1.0 | Current core satisfies NeoForge >=21.1.231, Create >=6.0.10, Coasters >=0.1.2, Sable >=2.0.0 and client Flywheel >=1.0.6. No dependency upgrade selected. Implement directional longitudinal propulsion/braking and power/rollback ownership; do not substitute this for carrier-axis magnetic torque. |
| FD501E → FD910 | [Skin file 9409938](https://www.curseforge.com/minecraft/mc-mods/create-coaster-skin/files/9409938), `create_coaster_skins` 1.1.0 | Replaces the old 1.0.0 research candidate. Requires exact Create 6.0.10 / Coasters 0.1.5 / Sable 2.0.6 and NeoForge >=21.1.256. Select **21.1.256 + Sable 2.0.6 + its Veil 4.3.2** for this future upgrade. Current Fourth Dimension exact Sable/NeoForge/Veil guards reject it; review/repair the Veil scheduler compatibility mixin before changing the guard. Body/seat modifications need native ownership checks; Skin's WildSpin/SkyGlide mechanism must not replace an FD family mechanism. |
| FD501F → FD912 | [Immersion file 9542588](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion/files/9542588), `coasters_immersion` 1.1.1 | Replaces 1.1.0 research candidate. Current core satisfies its requirements; `IGNORE_SERVER_VERSION`, client-side Create/Coasters/Sable dependencies, optional Aeronautics >=1.3 and optional Sodium >=0.8 remain. Select client-only installation; server without Immersion is the required asymmetric profile. Compose camera/body effects once on FD pose, retain relative look and exit restoration. No data migration selected. |
| FD501G → FD913 | [Waterparked file 9058331](https://www.curseforge.com/minecraft/mc-mods/create-waterparked/files/9058331), runtime `create_waterparked` 0.2.8-beta-hotfix1 | File label adds `-coaster0.1.5`; metadata requires Create >=6.0.10, Sable >=2.0.0 and Coasters >=0.1.4, all met. No dependency upgrade. Coexistence only: inspect shared graph/mixin/render behavior; FD cars must reject slide eligibility. No amphibious conversion or saved provider migration selected. |
| FD501H → FD914 | [Create Coasters file 8110328](https://www.curseforge.com/minecraft/mc-mods/create-coasters/files/8110328), `createcoasters` 2.0 | Requires Create and NeoForge >=21; current core suffices. Distinct Create train/contraption utilities. Inspect seat-lock/camera overlap; do not treat train speed/schedule blocks as Sable ride authority. No schema migration or bridge selected without an applicable native hook. |

Core and unchanged integrations retain Create file 7963363 / Coasters 8655742 /
Engineered `coastersengineered` 1.1.1 file 8863559 / Track Styles
`coasterssimulatedextratypes` 1.2.0 file 9020601. The full Create distribution
in the catalog is for metadata; the existing build retains 6.0.10-280 slim with
explicit Ponder/Flywheel/Registrate pins.

## Additions identity and transitive closure

The publication/runtime discrepancy is real, not a missing download: file 9243346
is published as 0.2.5 but embeds `coasterfins=0.2.2`. Select this exact file and
SHA-256, and accept that runtime label only with a supported class fingerprint.
The catalog includes SHA-256 of `StationHandler`, `StationBlockEntity`,
`CoasterSeatBlock`, and `SeatLockTicker` class resources. FD501B's guard should
resolve those resources from the loaded `coasterfins` module/container and compare
them before enabling hooks. Runtime `0.2.2` alone is insufficient; unknown bytes
disable the adapter with an explicit unsupported-version reason while standalone
FD behavior remains. Pinning a downloadable JAR hash alone is not a runtime guard.

Aeronautics bundle 1.3.2 contains `aeronautics_bundled`, `aeronautics`, `offroad`,
and **`simulated`**, all 1.3.2. The latter three require Create >=6.0.10 and Sable
`[2.0.0,3.0.0)`; Aeronautics/Offroad require Simulated >=1.3.2. Simulated requires
client Flywheel >=1.0.6. JEI >=19.21.0.247 is optional; no JEI install is needed
for closure. Interiors declares only Create >=6, NeoForge >=21.1.0 and Minecraft
1.21.1. This satisfies Additions' declared dependency graph on the current core.
Do not install separate duplicate Simulated/Aeronautics/Offroad JARs alongside
the bundle. Seat sync is FD911 acceptance, not a prerequisite to station telemetry.

## Graphics, seats and profiles

Sable 2.0.6 bundles `sablecompanion` 1.6.0 and Veil **4.3.2**. It rejects Companion
>1.6.0, Sodium below 0.8.12-alpha.2+mc1.21.1, and all ScalableLux. Veil 4.3.2
rejects Rubidium, Embeddium and Celeritas and declares the same Sodium minimum.
These transitive constraints make merely updating Sable and leaving our Veil
4.1.4 exact requirement invalid. Keep the current accepted graphics profile
unchanged for Wing. Select **no optional Sodium/shader loader** for the initial
Skin dependency profile; no unavailable graphics download blocks that profile.
Any later accelerated/shader profile must select exact mutually compatible
Sodium/Iris/shader versions and verify the changed renderer path in FD904A.
Historical shader/Flywheel evidence on the old stack is not certification of
Veil 4.3.2. The NeoForge 21.1.256 POM was retrieved from official Maven and its
hash retained in the catalog; that future pin is available, not activated.

Keep four accepted current profiles: base, Styles, Engineered, combined. Proposed
adapter development profiles add each selected candidate individually (Additions
includes its bundle/Interiors), then risk-focused Engineered+CTNB+Magnetized,
Additions+Skin, Immersion+Skin, and Styles+CTNB+Waterparked. These profile names
describe intended coverage, not implemented Gradle switches. The final expanded
combined profile uses **one** upgraded 21.1.256/Sable 2.0.6/Veil 4.3.2 baseline
for both client/server, all selected server addons, and Immersion on clients only;
Create/Coasters/Styles/Engineered pins remain unchanged. All declared ranges in
this catalog admit that proposed combination. Runtime/mixin/seat and authority
compatibility remain the named consumer checks, including exact transformed
class fingerprint handling if needed by the Additions guard.

Provider/seat choices must preserve family/variant IDs, vehicle/carrier UUIDs,
profiles and rider assignments. Missing cosmetic providers fall back; a missing
functional provider must retain its saved identity and deny operation until
supported restoration. The current loader rejects unknown providers; it does
not yet implement an arbitrary external-provider registry. FD501C/E own any
required bounded schema/provider work and backup-restoration checks. Importing
archived schema 7 is not selected. Additive original families alone do not require
that registry. Current editor protocol 8 is independently implemented already.

## Unavailable capabilities and finite disposition

No required *artifact* in this selected dependency closure is missing upstream.
The following *capabilities* are not currently available in Fourth Dimension:
Additions validated whole-consist request/seat adapters; measured CTNB functional
eligibility; Magnetized authority arbitration; Skin body/seat ownership adapters;
Immersion composition on FD carrier poses; qualified Waterparked/Create Coasters
coexistence. They remain FD810–FD914 implementation/acceptance work, not a claim
that their downloads supply FD support. Native interoperability cannot be inferred
from metadata. No reviewed upstream API provides blanket compatibility for all
FD body/seat mechanisms; unsupported options must visibly refuse rather than
silently taking ownership. A newly proven upstream blocker belongs to its named
consumer; no such blocker is invented from the absence of our adapter.

FD801 functional Styles remains outside this assignment: only appearance is
accepted, and no inspected equivalent functional Arrow mechanism has been
selected. CTNB aliases, Skin spinning/folding seats, and Create train utilities
do not grant that missing capability. Optional decisions do not delay Wing.

FD501 is ready for canonical closure by the lead: core/per-adapter pins, actual
IDs, transitive closure, graphics/seat constraints, profile plan, provider policy
and unavailable capabilities are explicit. FD501B–H are not marked implemented.
No build, new JAR, profile sweep or native launch is justified by this metadata
and no-change core review.
