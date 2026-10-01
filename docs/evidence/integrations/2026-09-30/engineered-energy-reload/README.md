# Inactive track loading and Engineered energy persistence

The energy lifecycle check exposed an idle chunk-loading problem. Coasters 0.1.5
builds its physics endpoint cache before iterating carts, even when no carts exist.
Sampling a 4D curve end reads its anchors through bank blending and renews Minecraft
`UNKNOWN` chunk tickets. The affected anchors remained loaded after force-load
tickets were removed. A second trace in the combined pack identified Additions
0.2.0 polling redstone on every registered anchor, even without a fin span. Six
rejected runs, both native request stacks and eight anchor ticket records are
retained here.

The fix excludes inactive 4D sections from that physics endpoint scan, using chunk
simulation availability without requesting chunks. It preserves the original graph's
junction counts: omitting a section must not turn an internal junction into an open
end. Ordinary graph edges retain native sampling. The periodic 4D geometry check
also waits until both endpoint chunks are ticking before reading their curves.
The base dependency remains pinned to Coasters 0.1.5. A separate exact-version
Additions guard reports no signal for inactive 4D anchors, preventing its global
physics poll from loading their chunks. Active anchors and ordinary tracks keep
native redstone behavior. The temporary diagnostic mixin is removed.

The extended native energy fixture supplies four distinct FE balances through the
real anchor capability, removes all its chunk tickets, and waits 430 ticks. It
requires both endpoints and each controller chunk to be absent from `getChunkNow`,
and the original anchor/controller instances to report removed. It then requests
the chunks again and verifies:

- Fresh anchor and controller instances, with balances of 12,345, 12,346, 12,347
  and 12,348 FE restored exactly for Arrow, legacy, Free Spin and revised tracks.
- Exact native span and complete controller NBT equality, including binding and
  launch sequence; each functional track resolves after reload.
- Simulated extraction preserves the restored balance, and actual extraction
  returns it through the native FE capability.
- The restored equipment passes the complete occupied hold, starvation, forward
  launch, regenerative brake, STOP, reverse launch/brake and recapture checks from
  the [original energy fixture](../engineered-energy/README.md). Five native pigs
  remain seated; the two-car Arrow coupling remains intact.

A separate native topology regression uses a synthetic three-edge graph: one active
4D section, its inactive continuation and an ordinary section. Sampling retains
the ordinary section's two endpoints and only the active 4D section's true open end.
Once the continuation ticks again, all four endpoints exactly match native sampling.
This topology check does not substitute for the actual chunk eviction fixture.

| Profile | Required tests passed | Explicit optional skips |
| --- | --- | --- |
| Engineered only | 27 | 3 |
| All five addons | 27 | 0 |
| Base | 27 | 5 |
| Track Styles only | 27 | 5 |
| Additions only | 27 | 3 |
| Magnetized only | 27 | 4 |
| Immersion only | 27 | 5 |

The build passes 19 unit tests. Optional skips are isolation evidence only. Exact
mod lists, result excerpts, warnings, full-log hashes, source/artifact hashes and
JUnit XML are retained. Dependency startup warnings are still present. The final
artifact is recorded separately from earlier successful builds made before the
topology regression and helper refactor.

Reproduce sequentially with Java 21:

```sh
./gradlew build runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
./gradlew runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=styles
./gradlew runGameTestServer -PfdIntegrations=additions
./gradlew runGameTestServer -PfdIntegrations=magnetized
./gradlew runGameTestServer -PfdIntegrations=immersion
python3 scripts/verify-engineered-energy-reload.py docs/evidence/integrations/2026-09-30/engineered-energy-reload
```

This proves unoccupied equipment chunk eviction and persistence, followed by
occupied operation. It does not prove occupied chunk interruption, a full process
restart with FE enabled, external generators/cables, partial-power/configuration
extremes, every graph topology, client rendering or performance. The earlier PAX
winch and separate lifecycle results remain bounded to their documented fixtures.
