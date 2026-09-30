# 0.2 acceptance evidence

Recorded 2026-09-30 for FD201–FD204. See the [package audit and scope](../../MAGNETIC.md).

- [Artifact SHA-256](artifact.sha256), [17 unit tests](unit-tests.txt), [14 native GameTests and measured assertions](native-acceptance.txt).
- [Production dependency installation/startup](production-startup.txt), [rejoining rider](multiplayer-rider.txt), [observing client](multiplayer-observer.txt), [observer at station](observer-station.png), [normal platform dismount](production-dismount.txt).
- [Final occupied save](occupied-final-save.json), [frozen restart](occupied-frozen-restart.txt), [exact migration/restart assertions](migration-restart-comparison.txt).
- [Final packaged single-player ride](singleplayer-ride.txt), [ready/closed station return](singleplayer-return.png).
- [Original track/car](original-free-spin.png), [final magnetic brackets](magnetic-brackets.png), [zone editor](magnetic-editor.png), [balanced reference preview](balanced-preview.png), [front-loaded reference preview](front-preview.png).

The dedicated multiplayer ride preceded the last changes to reference-preview coordinates/mass and the visible magnetic support brackets. The final archive passed all unit/native tests and a full real-player single-player ride, capture and dismount, followed by visual/editor checks. No server-side magnetic/boarding behavior changed between those sessions. The maximum snapshot encoded to 659,884 bytes; exact geometry and capacity limits are documented in the package audit. Logs use local development identities and retain body UUIDs to make rider/state matching reviewable.
