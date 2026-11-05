# Current qualification record

Updated 2026-11-04. This is the single artifact index for the active roadmap.
The selected build is a **qualification baseline, not an accepted release candidate**.
FD404 and FD905 remain open. A later source change does not inherit this identity.

## Artifact and scope

| Field | Recorded value |
| --- | --- |
| Committed source | `dd0a85167f98fe944fe6ab41a3c65d77864db767` on `main` |
| Packaged artifact | [qualification.jar](evidence/qualification/2026-11-04-dd0a851/qualification.jar) |
| SHA-256 | `811090454924d86c234b4d72efa19c568bbea108b55b84fb3b82e697586288aa` |
| Version label | `0.9.0-dev.27`; this label alone never identifies an artifact |
| Persistence / networking | Schema 6 / protocol 7 |
| Toolchain | Java `21.0.13.0.3+1`, Gradle `9.2.1` |
| Intended profiles | Base; Track Styles 1.2.0; Engineered 1.1.1; Styles + Engineered |
| Core pins | Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Coasters 0.1.5, Sable 2.0.3 |
| Acceptance on this exact JAR | Build and packaged-entry correspondence only; no runtime profile has been qualified by this record |

The [machine-readable manifest](evidence/qualification/2026-11-04-dd0a851/artifact.json)
records every exported source input, every JAR entry, changes since `2c5d9a7`,
and the excluded working changes. The build used a committed-source export on
the development host and isolated output, invoked from the primary checkout.
It includes the committed two-client fixture and packaged-launch preparation
changes through `dd0a851`; ongoing uncommitted edits remain outside this identity.
`compileJava` used Gradle's build cache;
all packaged classes/resources were compared with the isolated outputs.
The [build log](evidence/qualification/2026-11-04-dd0a851/build.log.gz) records
successful packaging; no client or server was launched for this record.

**Later committed inputs reviewed through `d8dc673`:** `b4ad576` changes
`CarrierValidationFixture` to select the requested recovery-train family;
`d8dc673` adds an opt-in framebuffer capture to `CarrierTransformValidation`
and its launch properties. Neither is in the `dd0a851` JAR above. Before the
next linked-PAX or new render-capture qualification, refresh FD401A with these
committed changes and a new JAR identity, then retest affected FD402B/D/E rows. This record deliberately separates the immutable build
already verified here from a newer working source; it does not call them equal.

Before each runtime qualification, retain the actual launch command, dependency
hashes, flags, hardware, world hash, client/server roles and exit status. The
intended profile list above is not a claim that those runtimes were exercised.
Crafts & Additions remains an explicitly disclosed FE validation fixture only.
The deferred schema-7/protocol-8 registry and new integrations remain excluded.

## Evidence reuse and required retests

| Evidence | What remains established | Required for this artifact / owning task |
| --- | --- | --- |
| [Frozen `2c5d9a7` map](evidence/0.9/fd401-final-candidate-2c5d9a7/README.md) and [later-source review](evidence/0.9/fd401-release-review-54495a5/README.md) | Historical operator, FE, font, Styles and geometry results at their recorded identities | FD401A reviews affected source/resource inputs; FD401B–D and FD904A replay changed paths. No blanket whole-JAR equivalence. |
| [PAX source observation](evidence/0.9/fd401-pax-longhold-source-229e586/acceptance.md) and [packaged named gate](evidence/0.9/fd401-pax-packaged-longhold-229e586/README.md) | Both record repair, realignment, recovery and relaunch with sixteen riders after 4,800 unsupported ticks; the packaged gate has exact JAR identity `94c17e50…` | FD902C retains this named-test pass. The selected JAR differs in only three validation/fixture class entries, listed below. Normal-client operation, historical OUTBOUND regression and interrupted occupied cold restarts remain open; a stationary BOARDING restart does not close them. FD401D shares the operator/telemetry run. |
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

The manifest's `pax_229e586_comparison` compares every entry against the retained
packaged PAX JAR. Only `CarrierTransformValidation.class`,
`CarrierValidationFixture.class` and its `$1` class differ. The named
`ShuttleGameTests` fixture, shuttle/vehicle mechanics and every resource are
byte-identical. The changed client validation and opt-in recovery-cycle fixture
are not exercised by the named PAX server test. This supports scoped reuse of
that packaged test result, not whole-JAR equivalence, a complete profile pass,
or reuse of the changed client/multiplayer validation paths without review.

## Updating the record

After relevant production or fixture changes, select a new committed baseline,
build into isolated output, retain the new manifest/JAR, and update this pointer
and the affected-row list together. Keep earlier bundles immutable. Freeze the
feature-complete artifact at FD404, then qualify that identity through FD901–FD905.
Documentation-only commits do not require a new JAR when all packaged inputs match.

Verify the retained payloads from the repository root:

```sh
(cd docs/evidence/qualification/2026-11-04-dd0a851 && sha256sum -c SHA256SUMS)
```
