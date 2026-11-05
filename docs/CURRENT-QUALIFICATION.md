# Current qualification record

Updated 2026-11-05 UTC. This is the single artifact index for the active roadmap.
The selected build is a **qualification baseline, not an accepted release candidate**.
FD404 and FD905 remain open. A later source change does not inherit this identity.

## Artifact and scope

| Field | Recorded value |
| --- | --- |
| Committed source | `8f370d09015a7e782f0bd0a050cc311ea1bd0929` on `main` |
| Packaged artifact | [qualification.jar](evidence/qualification/2026-11-05-8f370d0/qualification.jar) |
| SHA-256 | `cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5` |
| Version label | `0.9.0-dev.27`; this label alone never identifies an artifact |
| Persistence / networking | Schema 6 / protocol 7 |
| Toolchain | Java `21.0.13.0.3+1`, Gradle `9.2.1` |
| Intended profiles | Base; Track Styles 1.2.0; Engineered 1.1.1; Styles + Engineered |
| Core pins | Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Coasters 0.1.5, Sable 2.0.3 |
| Acceptance on this exact JAR | Clean committed-source build and entry comparison; one bounded Engineered-only Arrow/FE occupied-reopen pass; no full runtime profile qualified |

The [machine-readable manifest](evidence/qualification/2026-11-05-8f370d0/artifact.json)
hashes all 287 exported build/source inputs and all 356 JAR entries, and lists
the changed inputs and classes since the [previous baseline](evidence/qualification/2026-11-04-dd0a851/artifact.json).
The JAR was built with Java 21 from a `git archive` of `8f370d0`, outside the
shared checkout, into isolated output. The [build log](evidence/qualification/2026-11-05-8f370d0/build.log.gz)
records successful packaging. The untracked `ShuttleBoardingHoldGameTests.java`
present in the shared checkout is excluded. Documentation-only commits after
`8f370d0` do not change the packaged inputs; future source/resource edits need
a new identity review.

**Post-baseline input review through `f4a5a6b`:** the only changed tracked
build/source input is `EngineeredInterlockGameTests.java`, expanded to a four-car
validation fixture. The [Engineered callback run](evidence/0.9/fd401-engineered-fourcar-motor-guard/README.md)
used that later development classpath and is attached to its own source/log
identity. The selected clean JAR above still contains the two-car version of
the fixture; no production, resource or build-script input changed. A packaged
run of the four-car fixture needs a newly built JAR identity. The untracked
`ShuttleBoardingHoldGameTests.java` remains excluded from this clean baseline.

Before each runtime qualification, retain the actual launch command, dependency
hashes, flags, hardware, world hash, client/server roles and exit status. The
intended profile list above is not a claim that those runtimes were exercised.
Crafts & Additions remains an explicitly disclosed FE validation fixture only.
The deferred schema-7/protocol-8 registry and new integrations remain excluded.

## Evidence reuse and required retests

| Evidence | What remains established | Required for this artifact / owning task |
| --- | --- | --- |
| [Frozen `2c5d9a7` map](evidence/0.9/fd401-final-candidate-2c5d9a7/README.md) and [later-source review](evidence/0.9/fd401-release-review-54495a5/README.md) | Historical operator, FE, font, Styles and geometry results at their recorded identities | FD401A reviews affected source/resource inputs; FD401B–D and FD904A replay changed paths. No blanket whole-JAR equivalence. |
| [PAX source observation](evidence/0.9/fd401-pax-longhold-source-229e586/acceptance.md) and [packaged named gate](evidence/0.9/fd401-pax-packaged-longhold-229e586/README.md) | Both record repair, realignment, recovery and relaunch with sixteen riders after 4,800 unsupported ticks; the packaged gate has exact JAR identity `94c17e50…` | FD902C retains this historical named-test pass. `ShuttleRuntime` and `Boarding` now differ in the selected JAR, so the old pass is not a current-artifact runtime pass. Normal-client operation, OUTBOUND regression and active-phase cold restarts remain separate. |
| [PAX two-client recovery](evidence/0.9/fd401-pax-two-client-recovery-8f370d0/README.md) | Tested JAR `253e6d92…`: two one-rider curve-only LIFTING holds over 4,800 ticks, normal repair/realign, explicit Recover on the second hold, occupied relaunch, BOARDING reopen and normal unload | The tested JAR has one extra uncommitted GameTest class; all 356 entries shared with the selected clean JAR are byte-identical. FD902C gains a bounded normal-client recovery observation on that exact tested JAR and unchanged production entries. This does not qualify a full profile or all FD401D telemetry sources. Active-phase cold restarts, OUTBOUND link loss and sixteen-rider client operation remain open. |
| [Current-JAR Engineered Arrow/FE reopen](evidence/0.9/fd401-engineered-current-arrow-fe-reopen/README.md) | Exact selected clean JAR `cf4fed9a…`: one occupied Arrow launched under wired FE, completed STOP and station capture, unloaded normally, then survived an occupied cold reopen without automatic launch and unloaded again | FD401C gains a bounded Engineered-only Arrow result. Combined profile and current-JAR normal construction remain open. Crafts & Additions was a disclosed FE fixture. |
| [Current-JAR wired FE interruption](evidence/0.9/fd401-engineered-current-fe-interruption/README.md) | Exact selected clean JAR `cf4fed9a…`: occupied Arrow held near station with depleted FE and `Starving=1`; restored water-wheel supply caused a short automatic pass with rider retained, followed by manual capture and normal unload | FD401C gains a bounded power-loss/restore result. The short pass did not complete a circuit or reach station capture; no second operator pulse was required by Engineered on restore. |
| [Current-JAR Legacy required-energy loop](evidence/0.9/fd401-engineered-current-legacy-fe/README.md) | Exact selected clean JAR `cf4fed9a…`: occupied Legacy reached STOP without starvation under `requireEnergy=true`, consumed FE, returned to station capture and unloaded normally | FD401C gains one bounded Legacy result on the saved normally built route. The 8.0 blocks/s target was short. The preliminary default-config run is disclosed separately and supports no FE claim. |
| [Current-JAR Free Spin required-energy loop](evidence/0.9/fd401-engineered-current-free-spin-fe/README.md) | Exact selected clean JAR `cf4fed9a…`: occupied Free Spin reached programmed STOP without starvation under `requireEnergy=true`, consumed FE, returned to station capture and unloaded normally | FD401C gains one bounded Free Spin result on the saved normally built route. The FE buffer was initialized by a disclosed fixture command. |
| [Current-JAR Revised required-energy loop](evidence/0.9/fd401-engineered-current-revised-fe/README.md) | Exact selected clean JAR `cf4fed9a…`: occupied Revised reached programmed STOP without starvation under `requireEnergy=true`, consumed FE, returned to station capture and unloaded normally | FD401C gains one bounded Revised result on the saved normally built route. The FE buffer was initialized by a disclosed fixture command. |
| [Current-JAR coupled Arrow required-energy loop](evidence/0.9/fd401-engineered-current-coupled-arrow-fe/README.md) | Exact selected clean JAR `cf4fed9a…`: occupied trailing Arrow reached STOP under `requireEnergy=true`, returned to station capture and unloaded normally with two chassis loaded | FD401C gains one bounded two-car operator result on a saved normally linked consist. The later capture query found no anchor energy key despite `PassStarved=0`; both native cart positions/link were not independently traced. |
| [Current-JAR four-car PAX authority](evidence/0.9/fd401-engineered-current-pax-authority/README.md) | Exact selected clean JAR `cf4fed9a…`: an occupied, route-loaded PAX winch cycle returned to BOARDING while the opposing powered Engineered controller stayed at pass zero and 100,000 FE; normal unload followed | FD401C gains a bounded Engineered-only PAX operator result. The first low-render-distance attempt faulted on an unloaded route chunk and is retained separately. Per-car callback cancellation is established by a later-source fixture, not this client run. |
| [Five-family fresh-process recovery](evidence/0.4/recovery/fresh-process-recovery/acceptance.md) | Historical development-client pause/resume/release observed in all five families; raw logs now retained | FD402D: attach artifact identity to a packaged replay. Historical source/JAR provenance was not retained and cannot be reconstructed from a version string. FD402E separately covers multiplayer. |
| [Two-client Arrow recovery](evidence/0.4/recovery/two-client-recovery-lifecycle/acceptance.md) | Dedicated server and two rendered clients complete fixture-operated attach/tow/brake/detach/unload; retained final save has no rider assignments or recovery link | FD402E retains this bounded observation. It is not a manual input, reconnect, interrupted restart or all-family result; attach full launch identity to remaining packaged matrix rows. |
| [Manual boarding](evidence/0.4/recovery/manual-boarding/acceptance.md), [bearing attachment](evidence/0.4/recovery/manual-bearing-selection/acceptance.md), [lever operation](evidence/0.4/recovery/manual-lever-propulsion/acceptance.md) | Real operator actions on JAR `2da14200…`; the later lever repeat stopped at the boundary, not by a timely moving brake | FD402C completes the occupied operator matrix with original IDs and normal pre-boundary braking on the qualification artifact. Preserve earlier successes without broadening their scope. |
| [Sampled wearables](evidence/0.4/recovery/vanilla-wearable-clearance/acceptance.md), [substeps](evidence/0.4/recovery/remaining-family-native-physics-substeps/acceptance.md), [strength](evidence/0.4/recovery/strength-rating/acceptance.md) | Bounded geometry, solver-pose and load results on pinned inputs | FD402A compares every geometry/policy input; rerun changed inputs. FD402B adds observed rendered rider/rack checks. Arbitrary cosmetics or solver-interior trajectories are not certified by sampled reports. |
| [FD403 matrix](FD403-ACCEPTANCE.md) | Existing crafting, partial powered routes and demo observations | Complete each listed connected-client family walkthrough and its save/reopen record; fixture-created layouts cannot substitute for player-built routes. |
| [Four-profile native matrix](evidence/0.9/profile-matrix-abc4a9a/README.md) | Historical 49-test result and its target-host limits | FD901 and FD904: rerun the actual current test inventory in four profiles, record its count and dependency hashes; do not call 49 the current suite size. |
| [Historical migration](evidence/0.9/native-schema3-world-replay.md) | The bounded saved-world upgrade at its original artifact | FD903: current-artifact historical rides, backup restoration and provider lifecycle, with unchanged IDs/profiles. |

Read-only replay of an offline verifier is reusable only when its complete input
hash set still matches. Source parity can justify reuse of a particular component;
it cannot prove a changed ride/lifecycle interaction. A failure reopens its owning
task. One evidence bundle may close multiple named rows when it actually exercises
each row; do not repeat a run solely because two packages reference it.

The manifest's `tested_pax_jar_comparison` compares every JAR entry against the
retained two-client runtime JAR. That runtime JAR contains exactly one extra
uncommitted GameTest class; all common entries match byte for byte. This
supports scoped transfer of the observed production behavior, not a claim that
the tested JAR itself came solely from committed source. The older `229e586`
named PAX test remains attached to its own JAR because the current
`ShuttleRuntime` and `Boarding` classes changed.

## Updating the record

After relevant production or fixture changes, select a new committed baseline,
build into isolated output, retain the new manifest/JAR, and update this pointer
and the affected-row list together. Keep earlier bundles immutable. Freeze the
feature-complete artifact at FD404, then qualify that identity through FD901–FD905.
Documentation-only commits do not require a new JAR when all packaged inputs match.

Verify the retained payloads from the repository root:

```sh
(cd docs/evidence/qualification/2026-11-05-8f370d0 && sha256sum -c SHA256SUMS)
```
