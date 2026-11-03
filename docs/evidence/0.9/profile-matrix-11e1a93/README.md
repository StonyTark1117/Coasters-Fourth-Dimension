# Native optional-profile matrix — partial passes and Engineered blocker

Recorded November 2, 2026 (America/Phoenix). Tested source:
`11e1a934da0f8c6cdafde818d290eb0fd17a1ffb`. The pushed candidate `4b2ed6e`
differs only in `ROADMAP.md` and `docs/evidence/0.9/README.md`; executable source
and build inputs are identical. Full revision identities and the comparison are
in `release-equivalence.json`.

Built JAR SHA-256: `b95327c8fbfdba02214669c4947ce578631e07088aeb6101a0d2d1649a3b88c9`.
All 216 compiled runtime classes were compared with that JAR and match.
These runs use Gradle's development GameTest runtime, **not** the packaged
source set. The JAR identifies the corresponding build; this is not a packaged
client acceptance record.

## Results

| Profile | Result | Native minutes | Timing ns/invocation, controlled / PAX / Free Spin | Timing ns/carrier update, same order |
| --- | --- | ---: | --- | --- |
| Base | 49 required tests pass; exit 0; normal save/shutdown | 5.243 | 54332 / 92570 / 102069 | 3217 / 2671 / 2751 |
| Track Styles 1.2.0 | 49 required tests pass; exit 0; normal save/shutdown | 5.067 | 53533 / 95569 / 100061 | 3155 / 2727 / 2667 |
| Engineered 1.1.1 | Native family-matrix capacity failure; run terminated | — | Not reached | Not reached |
| Styles + Engineered | Not launched after Engineered failure | — | Not measured | Not measured |

All launched JVMs enable `fourthdimension.performanceTargetValidation=true`.
The limits remain 120,000 ns per invocation and 6,000 ns per live carrier update.
Base and Styles pass every scene target. Hardware and Java versions are retained
in metadata. These are host-specific addon timings, not whole-server or FPS claims.

Base and Styles explicitly skip five Engineered-dependent scenarios by returning
success when Engineered is absent. Their 49-test totals are not evidence for those
addon operations. Styles reports `strokeCases=2`; base reports 0. Both report
25 family decisions and all 20 directed wrong-family coupling rejections while
preserving both endpoint bindings, appearance/mechanical separation and exact
PAX curve restoration. The test inventory includes valid foreign-material
provenance rejection, changed valid-Coasters geometry, and missing geometry as
separate synthetic historical-state cases. Genuine legacy-world results retain
the scope of the separate `307ec35-legacy-provenance` record.

## Engineered failure and runtime release

The isolated `fourthdimension_engineered_energy` batch first completes
`nativeEnergyStarvationSupplyBrakeAndRetention`, reporting
`FD_ENGINEERED_FE installed=1.1.1 nativeCapability=true unloaded=true`, then
`FD_ENGINEERED_FE restored requireEnergy=false`. The subsequent default batch
contains 32 tests. At 20:20:40 local its
`fivefamilyfactorylockedsnapandbindingmatrix` fails with `Vehicle capacity reached`.
The complete console is preserved; uncompressed source lines 112, 179–190 record
this sequence.

The native JVM (PID 1705853) was explicitly terminated with SIGTERM after the
failure. Gradle records native exit 143; the wrapper exits 1. **There is no normal
shutdown or final-save claim for this run.** Its copied interrupted world has an
empty data directory and does not establish live vehicle counts. The wrapper's
folder name `stopped-world` means the process was stopped before copying, not that
it shut down cleanly. `engineered/termination.json` records the distinction.

Static follow-up found that the FE fixture explicitly removes its registered
vehicles, native chassis/carriers and rails. The Engineered motor and ordinary
bearing tests also run in defaultBatch, increasing concurrent fixture population
relative to base/Styles. Core's diagnosis is shared test-batch capacity pressure;
this record does not establish an FE leak or justify raising production capacity.
The proposed follow-up is to isolate family eligibility into its own batch, then
validate a new source revision. No source or capacity change was made by this
matrix run. Runtime was released to core; combined was not launched.

## Evidence and reproduction

Use Java 21 and the pinned Minecraft 1.21.1 / NeoForge 21.1.248 core stack. Each
run uses `./gradlew build runGameTestServer --no-daemon --console=plain
-PfdPerformanceTargets`, with no integration option for base,
`-PfdIntegrations=styles`, or `-PfdIntegrations=engineered`. The combined command
would add `-PfdIntegrations=styles,engineered`, but was not run here.

The bundle retains full compressed console/latest/debug logs, runtime arguments,
resolved dependency hashes, source/compiled-file hashes, the built JAR identity,
and the native test inventory. The generated legacy classpath contains 67 library
files and omits mod artifacts. `resolved-launch-classpath.json` supplements it
with an **offline post-run resolution** of the unchanged main runtime configuration,
using the archived init script and each profile's options. Those hashes identify
resolved files; they are not a captured live JVM classpath. Runtime mod lists and
profile-specific completion markers corroborate the installed addon versions.
Local complete worlds for successful profiles and
the interrupted Engineered world remain at
`build/profile-matrix/11e1a93/<profile>/stopped-world` in the evidence worktree;
worlds are not distributed in this bundle. Dependency binaries are not bundled.

`run-profile.py` records one profile at an explicit `--source`, requires a clean
checkout, and refuses to overwrite evidence. For reproduction, put it under the
ignored `build/profile-matrix` folder of a checkout at the tested source. Coordinate
runtime ownership, run serially, and stop the matrix on a failed profile. Gradle
recreates its generated GameTest world. `check-profile.py` describes a successful
profile gate; it is expected to reject this Engineered failure.

Run `python3 verify.py` in this directory to verify payload hashes, both successful
profiles, the expected interrupted Engineered failure, unchanged source/classes/
JAR identity across the three launches, and the combined-not-run status. A verifier
PASS confirms this **mixed-result record**, not a green matrix.

## Acceptance limits

Base/Styles refresh their bounded native checks beyond the old 41-test checkpoint.
Engineered is blocked on a corrected candidate and combined remains untested.
FD901 and the 0.9 release gate remain open. Dedicated production startup, real
operator workflows, multiplayer/lifecycle races, historical rider restoration,
rendered rider/linkage clearance, graphics/shaders and target FPS require separate
evidence. No future addon version or external functional track is certified.
