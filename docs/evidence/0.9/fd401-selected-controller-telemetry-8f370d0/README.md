# FD401 selected-JAR controller telemetry native-gap test

This headless combined-profile run used the selected clean Fourth Dimension
JAR (`cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`)
from `/tmp/fd401-c-combined-current/build/libs/coasters-fourth-dimension-0.9.0-dev.27.jar`,
with Styles and Engineered installed and the validation-only FE network. It ran
`RideTelemetryGameTests.loadedControllerPublishesMissingNativeBodies` on Java
21 via an isolated `runServer` directory and private RCON port. The server
stopped cleanly and Gradle exited 0.

The named test retained a bound Arrow station while removing a carrier body
through the native unload callback, then repeated the case for the chassis.
For each case all six Create Display Link source fields, the two-line goggle
formatter, and the update-tag snapshot reported unavailable state; restoring
the native body returned station telemetry to available. The compressed captured output in `server.log.gz`
records both unavailable messages and both restorations, followed by the test's
`complete` marker and `helper.succeed()` path.

This is a server-side selected-JAR controller/source/packet result. It does not
show remote client goggles or a physical Display Link target, optional-profile
client behavior, or the complete family/trailing-consist/fault matrix. In the
same selected-JAR session, the separate `sharedTelemetryRespectsConsistRecoveryAndControllerAuthority`
GameTest failed at its tick-100 initial PAX readiness assertion; that bounded
failure is preserved outside this pass record at
`/tmp/fd401-d-selected-telemetry/server.log` and must not be treated as a pass.
