# FD401 occupied-reload fixture trace — 2026-11-03

## Scope

This is a bounded historical trace, preserved before a later source hardening pass. It records a
packaged combined-profile client that completed one programmatic resource reload, remained on a
fixture-created Arrow carrier and then observed the automatic dispatch. It is **not** normal
Engineered controller operation, physical FE evidence, a GUI ride, a Styles clearance result or a
graphics-mode certification. In particular, it reports `decorationPieces=0`.

The raw logs and JAR are retained beside this README. The versioned payloads use the `.txt`
extension so they are not excluded by the repository-wide `*.log` ignore rule; the duplicate local
`.log` copies remain only for direct log-tool consumption.

## Command and result

The packaged-client process exited 0 on 2026-11-03 (America/Phoenix):

```text
env DISPLAY=:103 ALSOFT_DRIVERS=null JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon --console=plain \
  -PfdPackaged -PfdIntegrations=styles,engineered \
  -PfdWorld=FD401OccupiedReloadStrict \
  -PfdCarrierFixture -PfdCarrierFixtureMotion \
  -PfdCarrierFixtureRider -PfdCarrierFixtureRiderOnly \
  -PfdCarrierFixtureRiderFamily=ARROW \
  -PfdFontValidation -PfdFontValidationReload -PfdFontValidationExit \
  -PfdOccupiedReloadValidation -PfdOccupiedReloadValidationExit \
  -PfdOccupiedReloadValidationStages=100
```

The client log records generation 2 / successful reload 1, then one fixture-created and mounted
Arrow carrier dispatch. The completion line reports 493 mounted ticks, 890 geometry stages and
1.0819767162269842 blocks of chassis displacement.

The separately run GameTest server also exited 0. Its exact invocation was:

```text
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon \
  --console=plain -PfdIntegrations=styles,engineered -PfdFeNetwork -PfdPerformanceTargets
```

`gametest.txt` records that all 50 required tests passed in 5.765 minutes. It is a distinct run
from the packaged client and does not establish client-rendered or normal-operator behavior.

## Identity and retention

| Artifact | SHA-256 |
| --- | --- |
| `coasters-fourth-dimension-0.9.0-dev.27.jar` | `8144d069cdbcff3d821fd5c8d5de39c07bcae37a134bb98fb38ab9bf29f9d6f8` |
| `client.txt` | `e1dcdf9fb028b6be8afc3d1340faf08636ae0f1295a3837ab9984fd434c4e31f` |
| `gametest.txt` | `24e147877522a21c44e0eb68d1bb04af31dec1bea68fe966342a195b03117cc1` |
| Track Styles 1.2.0 runtime dependency | `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38` |
| Engineered 1.1.1 runtime dependency | `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d` |

Track Styles 1.2.0 and Engineered 1.1.1 were installed development-profile runtime dependencies;
they are not bundled into the retained Fourth Dimension JAR. Both runs used an uncommitted working
tree that predates the subsequent `1a91416` occupied-reload hardening, and no full source snapshot
was retained with them. This trace must not be transferred to a later source revision solely by
version string, base commit or JAR name.
