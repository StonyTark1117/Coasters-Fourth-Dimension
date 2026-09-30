# Version 0.3 development evidence

The production GUI checks used the development artifact recorded in `development-artifact.sha256`, not the later test-only matrix build. Exact dependencies and original inspired geometry are described in [the family guide](../../REVISED-SHUTTLE.md).

- `production-startup.txt` and `production-frozen-restart.txt`: initial official dedicated-server assembly and boarding-state restart.
- `occupied-client-restarts.txt`: interrupted occupied PAX lift, explicit platform-operator resume, a second complete cycle, revised moving restart, and both families' normal platform exits. This also records the unexplained first client exit 137 and limits of the premature-unload GUI observation.
- `pax-occupied-final-save.json` / `pax-occupied-frozen-restart.json` and `revised-occupied-final-save.json` / `revised-occupied-frozen-restart.json`: paired complete addon saves, each equal exactly. These contain test-world UUIDs only.
- `multiplayer-dev.txt` and `multiplayer-observer.txt`: selected real-client carrier, occupant, and camera observations.
- PNGs: original geometry, both players, operator resume, and captured revised-car editor.
- `native-progress.txt` and `unit-tests.txt`: earlier 18-native/17-unit passing checkpoint. Later native matrix results have their own evidence file.

These records do not establish the five-family release gate or the complete FD902/FD904 lifecycle/resource/performance matrix. No external Track Styles compatibility is claimed.
