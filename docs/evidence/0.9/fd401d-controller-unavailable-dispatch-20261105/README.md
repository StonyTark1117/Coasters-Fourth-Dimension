# FD401D native-body unavailability rejects bound-station dispatch

## Result

One named headless GameTest passed on 2026-11-05: `RideTelemetryGameTests.loadedControllerPublishesMissingNativeBodies` (`/test run loadedcontrollerpublishesmissingnativebodies`). RCON reported `Running 1 tests...`; the server batch was `fourthdimension_telemetry:0 (1 tests)`. The server stopped cleanly and Gradle exited 0.

The test retained a bound Arrow station and independently unloaded its native carrier body and chassis through Sable's `UNLOADED` removal path. In each unavailable state it now requires `available=false` and `dispatchReady=false`, calls `RideOperations.request(..., DISPATCH)` and requires an `IllegalArgumentException`, then compares the serialized `FoundationData` before and after the rejected request and requires equality. No game tick occurs between the synchronous request and comparison. The existing checks also confirm all six Create Display Link sources, goggles text, and the station update-tag snapshot show unavailable state. Restoring each native body returns telemetry to available.

Completion markers in `server-gradle.txt` record both rejections, stable FoundationData, both recoveries and `complete`. This is one Arrow native-body denial/re-entry slice in the combined optional profile. The fixture has no PAX winch; it does not prove a winch state guard. It does not establish unavailable-state sign output, remote client telemetry, other-family dispatch denial, base-profile behavior or the complete FD401D matrix.

## Artifact and runtime identity

The selected clean production artifact is the retained `0.9.0-dev.27` JAR at `docs/evidence/qualification/2026-11-05-ec2fcda/qualification.jar`, SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.

Because the new assertion exists only in the GameTest class, the server loaded `focused-test-overlay.jar`: it was made from the selected JAR by replacing only `dev/fourthdimension/gametest/RideTelemetryGameTests.class` with the class compiled from this checkout. The parity report verifies 394 entries in both JARs and that this is their sole changed entry; every production class/resource is byte-identical to the selected artifact. Overlay SHA-256: `c1120ae8ef43c67d3e41745aca70d1a8c5e4e3e5485b35d1b5e8ee8dddaacfa9`. This establishes production-class parity for the GameTest run, not execution of the byte-identical original JAR file.

Java was OpenJDK 21.0.13.0.3; Gradle was 9.2.1. The server JVM used `-Xmx1536M`; a process sample during the test showed about 1.53 GB RSS. Host available memory was 16 GiB at that point. The run used these validation profiles, which were runtime dependencies and are not bundled in the selected add-on JAR:

- Track Styles 1.2.0, CurseForge file `9020601`, SHA-256 `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38`.
- Engineered 1.1.1, CurseForge file `8863559`, SHA-256 `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d`.
- Validation-only Create Addition FE network, CurseForge file `8188476`, SHA-256 `41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9`.

The compile task also saw a separate ignored `ShuttleBoardingHoldGameTests.class` in the checkout; it was excluded from the overlay and the run. The exact build and `runServer` commands are retained in `run.txt`; full Gradle output, game log, RCON test/stop responses, class parity report and compile output are retained beside it. The package checksum index covers these files.
