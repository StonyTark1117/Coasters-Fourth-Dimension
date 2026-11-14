# FD403C Chain setup diagnosis — 2026-11-14

Owner remains **Complete FD403C Free Spin**,
`01a1d49d-7763-76b2-8b0c-6f5b337f0ea2`, on primary `main`.
Root inspected retained records and pinned dependency code; it did not control
C's runtime. This handoff supersedes the pending Chain repair directions in
[the earlier context record](fd403c-context-recovery-20261114.md).

## Verified setup at 12:22 UTC

The owner's [console record](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/console.jsonl)
contains the authoritative anchor readbacks. The eight connected route anchors
are i0 `(360,-60,128)`, i1 `(360,-60,164)`, i2 `(368,-60,172)`,
i3 `(384,-60,172)`, i4 `(392,-60,164)`, i5 `(392,-60,128)`,
i6 `(384,-60,120)` and i7 `(368,-60,120)`.

| Lift owner | Recorded coverage | Current disposition |
| --- | --- | --- |
| i1 | i1→i0, then 0.0454545 of i0→i7 | Removed through normal Shift+Wrench; 12:13 readback has no span and Speed 0. Its motor remains -16. |
| i3 | i3→i2; zero-length endpoint on i2→i1 | Retained, anchor Speed -16. |
| i6 | i6→i5→i4, then 0.0456534 of i4→i3 | Removed through normal Shift+Wrench; 12:22 readback has no span and Speed 0. Its motor remains +16. |
| i7 | i7→i6; zero-length endpoint on i6→i5 | Retained, anchor Speed -16. |

The i6 span did **not** intersect i0→i7. A red i0→i7 preview therefore did not
justify identifying i6 as the conflicting lift. Do not remove another span
without identifying the actual candidate path and positive-length intersection.
The latest [12:23 preview](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/20261114T122303.820271Z-state.png)
still reports overlap while F3 targets i7. F3 establishes the target block,
not the stored start or the preview's graph path.

Pinned Coasters `1601871-8655742` computes the shortest anchor-to-anchor path.
`ChainLiftSpanOverlap` compares parameter intervals on matching undirected
edges, excludes the start anchor and requires positive overlap above `1e-5`.
Zero-length endpoint segments do not themselves overlap. Its client index and
cached preview must be inspected before deciding whether this is selection,
path, synchronization or cache behavior. Server records alone do not establish
the client's rejected path.

The later chunk-refresh teleport to y=80 killed Dev: the server explicitly
reports `Dev fell from a high place` at 12:24:08 UTC. Weather is not the recorded
cause. Setting keepInventory afterward cannot recover already dropped items.
Recover the player/inventory locally and preserve the route; do not repeat the
far teleport as a refresh experiment.

## Concrete next observation

The opt-in [ChainLiftDiagnostics](../../../src/main/java/dev/fourthdimension/client/ChainLiftDiagnostics.java)
compiles with Java 21. It has no payload, persistence, placement, selection or
physics changes. With `fourthdimension.validation=true`, the owner's client
consumes `fd-chain-diagnostic.request` in its actual game directory and writes
`fd-chain-diagnostic.json`. A blank request captures the current selection;
six coordinates additionally compute a comparison path. For this case use:

```text
360 -60 164 368 -60 120
```

Select the intended i1 start and point at i7 once, then request the snapshot.
It records the actual selected anchor, cached key/status/path, hit block/entity,
computed comparison path, and each indexed lift's committed/resolved segments,
current block-entity identity and overlap result. Preserve that JSON. Do not
commit a rejected preview, bypass overlap guards or retry route removals.

C owns adoption of the diagnostic in its client and any required restart. An
isolated copy of its current client JAR containing only the added diagnostic
class preserves the exact existing gameplay code; record both artifact hashes.
The server/world can remain on the existing artifact because this adds no
network contract. Keep the owned display, memory limits, game directory and
proven controls. This is diagnostic preparation, not acceptance evidence.

After the snapshot identifies the cause, correct that cause and verify the
saved coverage/direction once. Account for the removed i6 lift before a route
segment needs it; do not silently assume its former coverage still exists.
Then complete the assigned occupied DAMP observation, normal capture/unload,
local cleanup and teaching reconciliation. Reuse accepted construction,
mechanics and persistence. No new family campaign or Arrow work is assigned.

## Verification so far

Root's isolated build under
`/home/braydon/.local/state/fourthdimension-runs/roadmap-chain-review-20261114/build`
passed `jar -PfdPackaged` with Java 21 and Gradle 9.2.1. C installed the added
class in a copy of its existing client artifact, preserving server/world state,
and reconnected at 12:30:30 UTC. The actual game directory is the run root
plus `/client` (the direct launch script changes directory); `--gameDir .`
does not refer to the systemd service working directory.

The 12:33:47 UTC live snapshot resolved i1→i7 through i0 at 50.4293 m.
All three indexed lifts had `overlapsProbe=false`, with current block-entity
identities. Selection was null, so this establishes that the intended current
path is clear, not the cause of the earlier rejected preview. At 12:35:13 UTC
a normal Chain click established the intended i1 selection. The subsequent i7 preview and normal placement are recorded below. Read
the snapshot timestamp after each request: an already-existing output file
is not proof that the new request has completed.


## Normal placement recovered — 12:36 UTC

The [live preview](fd403c-chain-diagnosis-20261114/valid-preview.json) records
selected i1 and `VALID`, with no overlapping indexed lift. The native preview
picked the curve near i7 (`T=0.9545455273`), not the exact anchor endpoint,
for 49.9151 m of coverage. C committed it through normal connected-client
input. The 12:36:31 server readback confirms the same saved segments and
length: all of i1→i0 and almost all of i0→i7. The earlier generic overlap is
not reproduced by this confirmed selection. No production overlap fix or
further span removal is justified by this record.

**Direction correction, superseding root's 12:38 advice:** the signed chain
speed is not the final cart direction. Pinned `CoasterCartChainLiftDrive`
negates its tangent projection when matching cart speed. Root's earlier
recommendation to change i1/i7 to `+16` missed that inversion. C traced the
full path and observed the resulting reverse travel; see the
[direction correction and live evidence](fd403c-context-recovery-20261114.md#chain-direction-correction--2026-11-14-utc).
For the recorded ordered spans, the intended cart direction requires i1/i7
anchor speed `-16` with travel sign `+1`, and i6/i3 anchor speed `+16` with
travel sign `-1`. Check delivered anchor speeds after normal motor edits and
confirm actual chassis travel. UI values or signed belt speed alone do not
establish cart direction. i6 has since been restored by C; use the current
queue and latest context record for live state.

Client overlay provenance: base JAR SHA-256
`e5f65c76289a018b64c56c1d8f845fe3b09e920bac93909d783a4b8373fb3972`;
diagnostic copy
`b7d9153b6dfc2cce7181b6be0acc8d2de8db07473ef2b307626764821b3228db`.
The only added ZIP entry is `dev/fourthdimension/client/ChainLiftDiagnostics.class`.
The server and existing client gameplay entries remain unchanged.

## Wrench targeting diagnosis — 2026-11-14

C owns the live runtime and the added read-only hover diagnostic. Root inspected
the pinned dependency and saved inputs without controlling that runtime.

The 16:28:36 UTC Shift+Wrench input left i6 unchanged. Its standing
[before image](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/20261114T162836.223852Z-before.png)
and held-Shift
[after image](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/20261114T162836.223852Z-after.png)
show the track move upward relative to the reticle as the camera crouches.
The helper takes the first image before pressing the modifier and the second
before releasing it. Standing aim therefore does not verify the clicked pose.
This observation alone does not identify the handler's failed guard.

The owner's fresh 16:37:04 UTC snapshot, after adopting the hover diagnostic,
reports `hitType=MISS`, `trackOutlineSelection="null"`,
`wrenchGraphHit="null"` and `wrenchSpanHit=null` even while standing. The
earlier visual inference of a curve hover was not a confirmed native target.
The owner's held-Shift, no-click snapshot at 16:38:00 UTC reports the same
missing selections; it is recorded in the run's
`evidence/crouch-hover-diagnostic.jsonl` with its screenshot and key release.
The traced `CoasterWrenchCurveAnchorOutlineClient.onUseItemKey` path requires
curve hover and a graph hit before `tryRemoveChainLiftFromCurveInteract` can
find a span and send its removal packet. It also excludes active cart editing
and hovered/dragged Bezier handles. A missing graph hit is not proof of a
missing input event or broken removal code.

Saved i5/i4 geometry explains a separate reach problem. Their endpoint x
coordinates are both 392.5, but the 12 m handles point outward. The cubic
midpoint calculated from their saved Starts/Axes/HLo/HHi is approximately
`(397.2113,-59.25,146.8767)`. From the recorded player feet
`(390.5,-60,146.5)`, this is 6.72 m away horizontally. Pinned
`AnchorPeerCurvePick` uses the player's `BLOCK_INTERACTION_RANGE` when making
its trace (bounded by that range plus one); the long server packet allowance
does not make a distant visible curve selectable. The normal survival range
does not reach this midpoint. The endpoint chord is not the rendered curve.
This calculation follows the pinned Create 6.0.10-280
`BezierConnection$Runtime` constructor: both control points **add** their
respective normalized axis times handle length to their endpoint. Coasters'
`BezierConnectionIndependentHandleRuntimeMixin` replaces the lengths with
HLo/HHi without negating the second axis. Subtracting the second saved axis
produces a different curve and an incorrect approach coordinate.

The next corrective action is one closer approach calculated from the actual
curve, followed by a fresh native graph/span readback while crouched. Click only
after that readback identifies the intended i6 span; then inspect its server
state once. If targeting passes but removal still fails, trace the remaining
event/packet guards rather than repeating aim or generic mouse setup. C retains
the existing world, motor state, family outcome and accepted evidence.

The new diagnostic had already compiled successfully with `compileJava` into
`build/classes/java/main`. The optional `packaged` source set is a runtime
wrapper, not the source location for this class. Reuse that compiled class in
the existing one-class diagnostic overlay; no additional build campaign follows
from the overlay hash. The current hover results narrow this failure; they do
not yet establish successful span removal.
