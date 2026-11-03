# FD403 family-track smoothing input boundary

Read-only inspection on 2026-11-02. The inspected release's normal family-track
placement does **not** pass the player's Left Alt smoothing preference into
upstream curve creation. Do not describe Left Alt as a working fix for the
family-track station curve until an implementation change and normal-client
acceptance establish that behavior.

This is a call-path finding, not a newly executed survival test or proof that
smoothing is the only cause of the previously observed station sag. No game or
JVM was launched. A Python class-file reader inspected the separately installed
Coasters 0.1.5 JAR; [inspection.json](inspection.json) pins its full hash, class
and method-code hashes, method descriptors/local names and the short forwarding
method's exact bytes. Our two placement source hashes are recorded separately.

## Observed call path

1. `FamilyTrackItem.useOn` delegates the second normal anchor click to
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
own item while remaining inapplicable to our family-item path. The original
`false` must not simply be replaced with a keyboard value: it is the girder flag.

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

Reading the public preference getter alone therefore does not fix the current
five-argument builder: the value must reach an appropriate creation path. Do not
drop normal validation or neighboring-curve bookkeeping to obtain a flatter
curve; establish the chosen API's complete semantics first.

## Implementation-owner handoff

Choose an explicit, server-validated construction behavior that allows a level
station without sacrificing matching-family eligibility, source geometry,
profiles, item cost, occupancy guards or exact support restoration. If using
upstream suppression, first verify client preference delivery for our held item
and the correct player-aware/six-argument construction API; this inspection does
not establish that a payload is sent while holding a Fourth Dimension item.
Preview and committed geometry must agree. Keep that decision with the main
implementation owner.

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
