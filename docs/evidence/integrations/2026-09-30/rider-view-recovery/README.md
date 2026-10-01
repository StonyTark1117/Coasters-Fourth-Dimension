# Saved rider view and native tracking recovery

This follow-up fixes two failures found while extending the combined-pack lifecycle
checks. It is bounded Arrow evidence; FD902 and the full integration goal remain open.

## Changes and cause

A mounted player's saved yaw/pitch are carrier-local. Sable 2.0.3's
`LocalPlayerMixin.sable$onStartRiding` expects world-space angles and applies the
carrier inverse rotation. Our saved-seat restoration supplied local angles directly,
so a carrier facing the opposite direction caused a second 180° conversion. The
previous `8e21ccf` run's rendered-camera quaternions show **179.9994°**, proving this
was an actual view turn, not just equivalent quaternion signs or wrapped yaw.

`Boarding` now remembers the saved local mouse angles during its bounded restoration
wait. `RiderLook` transforms their look direction through the current carrier pose
for the native boarding teleport. Server state returns to the saved local angles in
`finally`, including rejected attempts. The existing readiness/seat authorization,
normal manual boarding and dismount behavior remain authoritative. Vanilla
`RootVehicle` seat replay is deferred when the player already has a saved 4D seat
assignment, including with open restraints; the guarded restoration owns that
reattachment and view conversion. No new persistent
fields, network message, client rotation override or schema migration is needed.

A separate reconnect while ticks were frozen left the carrier bodies absent on the
client (`rejoin-after.txt`, `live-rejoin.png`). Sable's tracking system stores UUIDs
and ordinarily prunes a disconnected UUID on the next simulation tick. Rejoining
before that tick reused membership from the old connection, so no initial full body
sync was sent. Logout now removes that UUID from the tracking sets of registered 4D
chassis and carriers. Sable sends its normal full sync to the new connection. This
cleanup does not change other mods' bodies or use a proximity-based ownership guess.
The inspected methods are `ServerSubLevel.getTrackingPlayers()` and
`SubLevelTrackingSystem.tick()` in the pinned Sable 2.0.3 artifact.

## Stack and procedure

Same exact stack as [combined two-client evidence](../combined-two-client/README.md):
Minecraft 1.21.1, Java 21, NeoForge 21.1.248, Create 6.0.10-280, Coasters 0.1.5,
Sable 2.0.3, Engineered 1.1.1, Styles 1.2.0, Additions 0.2.0, Magnetized 1.1.0,
Immersion 0.2.3, Aeronautics bundle 1.3.2 and Interiors 0.6.1 v3. No shaders.
One packaged client, a packaged dedicated server, separate 2 GiB caps, 30 FPS client
cap, distances 5, owned Xvfb display. Fresh flat world `FDHeadingQA`, normal Arrow
Engineered example at origin (-600,230,12), normal empty-hand boarding and tool
restraint controls. The open-restraint checks use a fresh `FDHeadingOpenQA` world
with the same layout and ordinary boarding. No runtime Java injection or forced `/ride` command was used.

Use the same packaged launch arguments as the sibling evidence, with
`-PfdValidation`. The client uses `-PfdConnect=127.0.0.1:25585`. Cold-restored servers
use `-PfdFrozen`; connect, inspect the save, then unfreeze. For a frozen live reconnect,
freeze while seated, disconnect, reconnect without allowing an intervening simulation
tick, then unfreeze. For moving reconnect, dispatch the normal controller sequence,
disconnect while the carrier is inverted, and reconnect while the server continues
simulating. Change mouse look before the live reconnect so this is not solely a
cardinal-heading test.

`artifact.sha256` distinguishes the two immutable development snapshots. Candidate 1
contains the view fix: initial cold restart, ordinary live reconnect and moving
reconnect used it. Candidate 2 adds tracking cleanup: frozen live reconnect, another cold
restart and normal exit used it. Candidate 3 additionally defers early vanilla seat
replay with a saved 4D assignment: open-restraint frozen live reconnect, cold restart
and normal exit use this final source. All three candidates use the same view
conversion. `source-files.sha256` records final inputs. All server/client runs exited
with status 0. `full-logs.sha256` identifies the complete transient logs; selected raw
camera/packet events and actual before/after samples are committed here.

## Results

| Check | Measured result |
| --- | --- |
| Initial cold restart | Rendered view changed 0.0167°; same carrier. |
| Live reconnect at noncardinal mouse angles | Rendered view changed 0.0026°; same carrier. |
| Disconnect while inverted, reconnect during motion | Local yaw changed 0.0187°, pitch 0.0769°; same carrier. World view necessarily follows the changing physical pose. |
| Reconnect while ticks remain frozen, then resume | Native bodies restored; rendered view changed 0.0081°. |
| Final cold restart | Rendered view changed 0.0178°; complete frozen saved ride data equal. |
| Open restraints: frozen reconnect | Rendered view changed 0.0081°; same carrier. |
| Open restraints: cold restart | Rendered view changed 0.0149°; saved state exactly preserved, then normal exit. |
| Normal open-restraint exit | Occupancy and seat assignments cleared, camera restored to ordinary mouse pitch, residual body travel zero. |

The verifier uses a **0.25° tolerance** for these bounded comparisons. Camera
quaternions in diagnostics are rounded; simulation settling and interpolation also
limit exact comparisons. This is not a universal latency or motion-error guarantee.

Final candidate-3 all-addon native run: all **24 required tests executed and passed**. Final
standalone run: **24 registered tests passed**, with Engineered/Additions/Magnetized
fixtures explicitly skipped because their artifacts were absent. **19 unit tests**
passed, including two orientation tests covering half-turn, quarter-turn, inverted
and identity carrier poses. The JOML test dependency matches Minecraft's 1.10.5.

```sh
python3 scripts/verify-rider-view-recovery.py docs/evidence/integrations/2026-09-30/rider-view-recovery
```

This reproduces `verification.json`, including the pre-fix failures, saved-state
identity and final normal exit. Broader family/consist/camera-mode/dimension/latency
coverage remains necessary. This session manually requested capture through the
normal vehicle editor before its final checks; it is not additional evidence of an
automatic station return after a moving disconnect. It does not replace the earlier
successful two-player Engineered return/capture evidence or certify that interruption.
