# FD401 final candidate identity at `2c5d9a7`

This record selects committed source
`2c5d9a7663e7d0e04887d2ac1cbb40688eb73d96` and the retained
`coasters-fourth-dimension-0.9.0-dev.27.jar` with SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`
as the exact FD401 review candidate. It replaces `81ee2e6` as the candidate
identity; it does not rewrite the identities of older observations.

## Isolated build and correspondence

`2c5d9a7` was exported with `git archive` to
`/tmp/fd401-candidate-2c5d9a7-V9tBRB`, outside the checkout. The source tar is
1,029,007,360 bytes with SHA-256
`a32ec9e986fcc3e57a555e9da42d303764c60e8e46cc3165267e4afced4572a2`.
It is not retained because the commit fixes the same contents and the tar
contains the repository's large historical evidence.

The exported source built offline with Java 21 and Gradle 9.2.1:

```text
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew jar --offline --no-daemon --console=plain \
  --project-cache-dir /tmp/fd401-candidate-2c5d9a7-V9tBRB/project-cache \
  -PfdPackaged -PfdIntegrations=styles,engineered -PfdFeNetwork \
  -PfdBuildDirectory=/tmp/fd401-candidate-2c5d9a7-V9tBRB/build-output
```

The command exited 0 with `BUILD SUCCESSFUL in 4s`. It did not use the shared
checkout build directory, any game directory, a display, or a live client or
server session. The JAR contains 234 class files, 107 processed resources and
the generated manifest/license. At generation time, every JAR entry matched
the isolated class/resource output byte for byte; [candidate.json](candidate.json)
retains the aggregate entry hashes and toolchain identity.

The isolated JAR is byte-identical to the already retained `a23ae2d` Styles
coupled-test JAR. There are no changes to `build.gradle`, settings, properties,
the Gradle wrapper, or any source/resource input from `a23ae2d` through
`2c5d9a7`; the intervening commits only add evidence and handoff documentation.
This gives the final candidate both a fresh isolated build and an independently
retained exact-artifact checkpoint.

## Profile and dependency lock

The declared candidate set remains base, Styles 1.2.0, Engineered 1.1.1 and
Styles+Engineered. The JAR is common to all four profiles. Crafts & Additions
1.6.0 appears only in the combined validation classpath selected with
`-PfdFeNetwork`; it is not an advertised integration.

[runtime-classpath.json](runtime-classpath.json) records all 111 resolved files
for the packaged combined validation classpath. Important hashes are:

| Component | SHA-256 |
| --- | --- |
| Fourth Dimension candidate | `4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781` |
| Coasters Simulated | `f80b4c3cdf9fa6188804967147761b0373e150a6eadbdc53626e5ac9700b2049` |
| Track Styles | `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38` |
| Engineered | `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d` |
| Create | `192c7b0be13f523dcec9a7a2393bbc2614c3c9bd3edb1f7720a97bcd4f0b87a7` |
| Crafts & Additions, validation only | `41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9` |
| Sable | `da6c3b66238586603d1dcaa2afb012d36815fbce0a2d5938fbb2936701d42279` |
| Flywheel | `31dda15c205eb596d3b3449ef03f6af7363a6cd35b3da4bfe916b304f9e5337e` |
| Ponder | `0cf4611ad853042b689ac386184c5bbe02950efcffddb49e5f604e82baddb0dc` |
| Registrate | `510f4041c41739f1d8ea8850ab8364d3e3a5fada8529beed5d9f479e2523db52` |
| NeoForge | `974c295d4390599b20486f80d38a14c934cf773c07bb0dbacf92dc8eb04c6dc7` |
| Veil | `9c2899086a96339245b5abde82879011753871f25443df9581881f29596a7180` |

The candidate carries save schema 6 and NeoForge payload protocol `7`.

## Evidence mapping

The map below deliberately distinguishes exact whole-JAR evidence from scoped
class parity. “Production parity” means every non-GameTest class and every
resource is byte-identical; it does not relabel an older GameTest transcript as
an exact-candidate run.

| Checkpoint | Relationship to candidate | Permitted reuse |
| --- | --- | --- |
| `a23ae2d` coupled styled full turns and banked UI edit | **Exact whole JAR** (`4b23cf0e…`) | Exact-candidate evidence for those two bounded cases. |
| `9ab54cf` banked full turn, stationary view and occupied visible ride | Production/resources identical; only three `BankedArrowGameTests` class files differ. | Client rendering/ride observations are candidate-production equivalent; the named native-test transcript remains tied to its fixture JAR. |
| `b2403d3` midsection edit reach | Production/resources identical; only the same three banked fixture classes differ. | Scoped edit-transaction behavior is candidate-production equivalent; its packaged test execution is not a whole-JAR match. |
| `2ba168d` controller gap, visible unload/reload, dedicated re-entry and optional-profile clients | Production/resources identical; four GameTest class files differ. | Runtime controller/client observations are candidate-production equivalent; the focused native transcript retains its fixture identity. |
| `663aa12` PAX callback | Production/resources identical; five GameTest class files differ. | The motor guard/interlock bytes under test are exactly the candidate bytes; the test fixture byte is older. |
| `2e12e9d` PAX realignment, broken-consist replay and Launch 20 pass | **Not production-equivalent**: candidate changes `EngineeredInterlock` and `EngineeredMotorGuardMixin`, plus six test classes. | Retain as predecessor behavior. Do not call these exact-candidate operator runs. The later callback checkpoint covers the changed guard path only. |
| `81ee2e6` artifact/family/PAX/telemetry records and the older render-mode JAR | **Not production-equivalent**: five production classes and six test classes differ. | Retain historical observations only. Renderer-specific reuse is limited to the separate 18-class parity report chained through production-identical `9ab54cf`; no whole-JAR equivalence. |
| Wired FE, family operator, coupled Arrow and PAX authority sessions using shared build output | Runtime JAR identity was not established. | Their physical/operator observations remain evidence, but the freeze does not make them candidate-exact. |
| Exact-candidate Engineered operator and wired-FE replay | **Exact whole JAR** (`4b23cf0e…`) | Exact-candidate evidence for the saved Arrow STOP/capture/unload loop and persisted wire transfer, consumption and recharge. Normal construction and source interruption/restoration remain predecessor observations. |
| Banked vehicle/player clearance and transition reports | Source/input-pin evidence, not runtime-JAR evidence. The 19 pinned geometry inputs remain unchanged in the candidate. | Reuse only for the reports' sampled geometry scope and stated exclusions. |
| Occupied FANCY reload ride and FAST/FABULOUS/Solas mode runs | Older whole JAR; renderer classes have scoped parity, but later vehicle/telemetry classes differ. | Reuse graphics/reload observations within their recorded renderer scope. Do not call the combined reload+ride an exact-candidate replay. |

This mapping closes candidate selection and identity. Later exact-candidate
records now cover the saved Arrow operator/FE replay and visible post-UI-edit
ride. Tracing-disabled reliability, continuous/equipped-player clearance,
candidate-exact family/authority coverage and other acceptance items remain open
where their evidence is not exact or their scope is narrower.

## Verification

Run from anywhere inside the repository:

```text
python3 docs/evidence/0.9/fd401-final-candidate-2c5d9a7/verify.py
sha256sum -c docs/evidence/0.9/fd401-final-candidate-2c5d9a7/SHA256SUMS
```

The verifier checks the source commit, candidate and classpath hashes, dependency
lock, JAR entry aggregates, exact `a23ae2d` equality, every scoped historical
JAR delta named above, and the absence of packaged-input changes after
`a23ae2d`.
