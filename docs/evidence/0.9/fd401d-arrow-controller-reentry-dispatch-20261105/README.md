# FD401D Arrow controller re-entry, dispatch and movement

## Result

One focused, named headless GameTest passed on 2026-11-05. It removed the Arrow's native carrier and chassis through Sable's `UNLOADED` path, confirmed fail-closed telemetry and rejected dispatch in each unavailable state, then restored both bodies. The restored map-backed vehicle instance had closed restraints; station telemetry returned `available=true` and `dispatchReady=true`. A subsequent accepted dispatch cleared the captured/stopping state, and the test recorded 1.776 blocks of physical movement over 20 ticks.

The test applies controlled +Z physics velocity after dispatch because an unpowered level Arrow track is not self-propelled. This proves the restored vehicle can accept dispatch and move under physical input after its safety state is released. It does not prove that dispatch itself supplies propulsion, nor does it cover player-operated motion, rendered output, another family, or another profile.

## Artifact and run identity

The selected production artifact is the retained `0.9.0-dev.27` JAR with SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`. The server used an overlay that replaced only `dev/fourthdimension/gametest/RideTelemetryGameTests.class`; all 393 other entries were byte-identical. The overlay SHA-256 is `b959b57014f4ed51b86821693c7045c604a4f54a49f76b692145d08074b663e3`.

The run used Java 21, Track Styles 1.2.0, Engineered 1.1.1 and Create Addition FE network on a dedicated server. The headless test needed no display. Command lines, recorded console result, full game log and final test world are retained beside this file. The world is a fresh named-test server world and is not a reusable ride fixture.

## Attempt history

An earlier attempt on a reused server world reported `restraintsClosed=false` after restoration. That result was not reproduced on the two subsequent fresh named-test server worlds: both showed the same map-backed vehicle instance with restraints closed and telemetry dispatch-ready after the normal restraint operation. The first fresh-world diagnostic then stopped at the movement assertion because the level route had no drive input; the final fresh-world run added controlled physics input and passed. The initial restraint discrepancy is retained as a limitation; no production code was changed to mask it.
