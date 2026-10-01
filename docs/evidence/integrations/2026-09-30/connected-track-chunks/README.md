# Connected track bank lookup and partial chunk interruption

This extends the [isolated energy equipment unload check](../engineered-energy-reload/README.md)
to ten unoccupied connected routes: flat and varying-bank tracks for Arrow, legacy
ZacSpin, Free Spin, revised ZacSpin and PAX. Each route has six 60-block sections.
The first section stays active while the distant continuation loses its force-load
tickets. Other native ride fixtures still exist in the shared GameTest world.

## Failure and fix

The existing endpoint filter skips inactive sections, but sampling an active section
can still walk its connected bank chain. In Coasters 0.1.5,
`BezierBankBlend.orderedAnchorsAlongJointBankChain` follows neighboring anchors and
reads their block entities. The captured request stack and chunk ticket table show
fresh `UNKNOWN 33` tickets on all five distant banked anchors. Neighboring flat
routes also remain resident through the chunk dependency neighborhood.

`BankBlendAvailabilityMixin` guards the two native bank lookup methods. For a
registered 4D anchor outside the chunk simulation range, it reads only an existing
block entity from `getChunkNow`, without requesting a chunk or creating an entity.
Absent or removed anchors use the native missing-data fallback. Active anchors,
ordinary anchors and client calls retain the original lookup. Coasters stays pinned
to 0.1.5; this is not a claim about other versions.

Seven failed attempts are retained. Attempts 1–2 used shorter two-section routes;
3–4 expanded to six sections with monotonic banks and identified the loading stack.
Attempts 5–6 introduced the guard and varied the banks. Attempt 7 waited 1,700 ticks:
all ten distant chunks were non-ticking, inaccessible and absent from `getChunkNow`,
but seven original anchor objects were still awaiting native removal. The diagnostic
table shows no tickets on the remaining inaccessible holders. Minecraft defers
pending unload/save callbacks under its tick budget; the accelerated GameTest server
did not finish these callbacks in that run.

The final fixture calls native `ServerChunkCache.save(true)` at tick 200, draining
pending save/unload work. It still requires actual chunk absence and original block
entity removal, then verifies fresh objects after reload. It never manually removes
block entities or simulates unload events. Attempt 8 passed before temporary debug
report code was removed; the final seven-profile matrix uses the cleaned fixture.
The temporary chunk-request tracing mixin is absent from the final source/artifact.

## Assertions and scope

- All sixty sections are valid before ticket removal. The banked anchors use
  `0, 0.2, -0.1, 0.35, -0.2, 0.3, 0.15` radians, exercising changes in bank direction.
- Each distant anchor is physically evicted while both first-section anchors still
  tick. Mechanical records remain valid.
- Production endpoint sampling exposes exactly one endpoint per partial route: its
  original start. It neither exposes an internal junction nor reloads the far chunk.
  Every route is checked before any reload is requested, because adjacent routes
  share chunk neighborhoods.
- Reload creates fresh terminal block entities, restores terminal bank exactly,
  preserves all sixty geometry fingerprints and restores two endpoints per route.

| Profile | Registered required tests passed | Explicit optional skips |
| --- | --- | --- |
| Base | 28 | 5 |
| Engineered only | 28 | 3 |
| Track Styles only | 28 | 5 |
| Additions only | 28 | 3 |
| Magnetized only | 28 | 4 |
| Immersion only | 28 | 5 |
| All five addons | 28 | 0 |

The build also passes 19 unit tests. Each profile runs the same connected-route
fixture; optional skips apply to other addon-specific fixtures. Engineered-only
and combined runs retain the occupied energy fixture after its separate equipment
reload. Exact startup mod lists, result excerpts, dependency warnings, full-log
hashes and final source/JAR hashes accompany this report.

This establishes bounded **unoccupied** partial eviction and restoration with an
explicit native save flush. It does not establish automatic eviction timing under
normal server budgets, occupied chunk interruption, full process restart, every
bank/graph topology, sloped or closed routes, client rendering or performance.
The varying-bank route is still straight in plan; it is not a curved-train rollback
or equipment acceptance test.

Reproduce sequentially with Java 21 (GameTests share one world):

```sh
./gradlew build runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=styles
./gradlew runGameTestServer -PfdIntegrations=additions
./gradlew runGameTestServer -PfdIntegrations=magnetized
./gradlew runGameTestServer -PfdIntegrations=immersion
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
python3 scripts/verify-connected-track-chunks.py docs/evidence/integrations/2026-09-30/connected-track-chunks
```
