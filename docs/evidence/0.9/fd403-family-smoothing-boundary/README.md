# FD403 family-track smoothing input boundary

Read-only inspection on 2026-11-02 of the family-placement source before
`2b67648`. That implementation did **not** pass the player's Left Alt smoothing
preference into upstream curve creation. Commit `2b67648` replaces the unsafe
five-argument bridge with Coasters' public explicit no-neighbour-mutation mode;
its focused GameTest checks that the preview uses that mode and leaves the
adjoining curve unchanged. Left Alt remains unsupported for family items: do not
describe it as a family-track station-curve control until normal-client
acceptance establishes such behavior.

This is a call-path finding, not a newly executed survival test or proof that
smoothing is the only cause of the previously observed station sag. No game or
JVM was launched. A Python class-file reader inspected the separately installed
Coasters 0.1.5 JAR; [inspection.json](inspection.json) pins its full hash, class
and method-code hashes, method descriptors/local names and the short forwarding
method's exact bytes. Our two placement source hashes are recorded separately.

## Observed call path

1. Before `2b67648`, `FamilyTrackItem.useOn` delegated the second normal anchor click to
   `TrackPlacement.connect`. That method calls
   `CoasterTrackPlacement.buildAnchorAnchorBezier(level, from, to, COASTER, false)`
   for new construction. It does not read player smoothing state. The distinct
   occupied-support restoration branch decodes stored original geometry instead.
2. The upstream builder forwards to the same five-argument method on
   `CoasterAnchorBezierOptimizer`. Its final boolean is named **girder**, not
   smoothing suppression.
3. The optimizer calls its five-argument `buildAnchorAnchorPlacement`. That
   11-byte method loads the five arguments, pushes **iconst_0 at offset 6**, and
   invokes the six-argument overload at offset 7. The sixth parameter is named
   **suppressNeighborSmoothing**. Thus this call path explicitly supplies false.
4. `CoasterTrackPlacement` separately has
   `suppressNeighborSmoothing(Level, Player)`: its client branch calls the native
   client preference getter; its server branch reads the player's UUID from
   `CoasterTrackPlacementNeighborSmoothSuppress`. The direct family builder
   described above bypasses that player-aware path.

The upstream ordinary-track Ponder lesson about Left Alt can be correct for its
own item while remaining inapplicable to the historical family-item path. The
original `false` could not simply be replaced with a keyboard value: it was the
girder flag.

## Exact exposed APIs in the inspected JAR

All three classes below are public. Access flags are included in the inspection
record; these are observed signatures, not an upstream API stability promise.

| Class | Method | Access and relevance |
| --- | --- | --- |
| `CoasterTrackPlacementNeighborSmoothSuppress` | `boolean isSuppress(UUID playerId)` | Public static server preference getter. |
| `CoasterTrackPlacementNeighborSmoothSuppress` | `void setServerSuppress(UUID playerId, boolean suppress)` | Public static setter used by the native preference payload. Directly forcing it would not prove a normal player input. |
| `CoasterTrackPlacement` | `boolean suppressNeighborSmoothing(Level, Player)` | Static, **not public**; selects client state or the server UUID getter. |
| `CoasterTrackPlacement` | `BezierConnection buildAnchorAnchorBezier(Level, BlockPos, BlockPos, TrackMaterial, boolean girder)` | Public static builder used by our item. Has no player, UUID or suppression argument; the downstream overload supplies false rather than reading the server preference map. |
| `CoasterAnchorBezierOptimizer` | `AnchorAnchorBuildResult buildAnchorAnchorPlacement(Level, BlockPos, BlockPos, TrackMaterial, boolean girder, boolean suppressNeighborSmoothing)` | Public static six-argument alternative. Its behavioral/neighbor-write and validation requirements must be reviewed before adopting it. |

Reading the public preference getter alone could not fix the historical
five-argument builder: the value needed to reach an appropriate creation path. Do not
drop normal validation or neighboring-curve bookkeeping to obtain a flatter
curve; establish the chosen API's complete semantics first.

## Follow-up: native client preference context

[client-gating.json](client-gating.json) records a separate read-only inspection
of `CoasterTrackPlacementNeighborSmoothSuppressClient` from the same pinned JAR.
It contains exact method bytes and resolved call/branch/type-check operands.

- `tracksPlacementContext(Minecraft)` accepts only a main-hand or offhand item
  that is an instance of native `CoasterTrackItem` (type checks at offsets 12 and
  32). Our `FamilyTrackItem` directly extends `Item`, so it does not satisfy that
  condition.
- `clientTick(Minecraft)` calls `syncSuppress(false)` when there is no player or
  the placement context is false. Only the accepted native context reaches
  `isSuppressNeighborSmoothing()`, which reads `AllKeys.altDown()`.
- `syncSuppress(boolean)` compares the last sent value, then sends
  `CoasterTrackPlacementNeighborSmoothSuppressPayload` on change. Thus holding
  only our family item does not normally send the Alt preference through this
  client path. The getter/server map alone cannot correct that missing input.

A native coaster track in the other hand could satisfy the native client context,
but did not alter the historical constant-false builder. It is neither a proven workaround
nor an acceptable substitute for a normal family-item control. This follows the
inspected type checks; no real keyboard, packet-delivery or reconnect test was
performed. A correction needs to define family-item context and state clearing
without competing client senders, then verify the resulting server geometry.

## Implementation-owner handoff

`2b67648` chooses the public server-validated no-neighbour-mutation mode so
family placement never applies an uncommitted neighbour-write batch. Future
Left-Alt support would still need to address the excluded family-item client
context above and verify packet delivery. Preview and committed geometry must
agree.

Acceptance needs normal survival family-track construction, a genuinely level
station, four normally placed PAX cars remaining correctly spaced during manual
coupling, winch binding and an occupied cycle. Preserve the prior sloped station
and failed attempts. Curve handles may offer an existing manual correction, but
that also needs normal-client evidence; it is not claimed by this source audit.

## Independent reproduction

With the exact JAR hash in `inspection.json`, a developer may inspect the same
methods using `javap -c -p -l -classpath <jar>` for
`dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement` and
`dev.silvergold.simulatedcoasters.track.CoasterAnchorBezierOptimizer`. This audit
did not run `javap` or Java. Check the five-argument forwarding method against its
recorded hexadecimal code and the six-argument local-variable table; compare the
recorded repository source hashes before applying the finding to another build.
Historical evidence and mechanics remain unchanged.
