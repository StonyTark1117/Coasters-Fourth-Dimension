# Selected-JAR combined-profile five-family motor fixture

On 2026-11-04 (America/Phoenix), a Java 21 dedicated server loaded the selected clean JAR from `8f370d0`, SHA-256 `cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`, with Track Styles 1.2.0 and Engineered 1.1.1. The server ran in an owned systemd scope with `MemoryMax=4G`; the server JVM used `-PfdServerMaxHeap=1536M`. The scope finished successfully. Server and RCON ports were 25587 and 25588 in an isolated world.

From the project checkout, the Java 21 Gradle launch used `runServer -PfdBuildDirectory=/tmp/fd401-c-combined-current/build -PfdPackaged -PfdValidation -PfdNamedGameTestServer -PfdIntegrations=styles,engineered -PfdServerGameDirectory=/tmp/fd401-c-allfamily-combined/game -PfdServerMaxHeap=1536M -x jar -x compileJava --offline --no-daemon`. After the server reached `Done`, RCON ran exactly `test run installedmotorsrespectcaptureanddrivedispatchedfamilies` (one test). RCON then stopped the server normally; Gradle returned `BUILD SUCCESSFUL`. The selected JAR hash was unchanged after the run. The full bounded launch output is [server.log](server.log).

The existing `EngineeredGameTests.installedMotorsRespectCaptureAndDriveDispatchedFamilies` fixture built five tracks, controllers, cars, and pig riders. It reached `helper.succeed()` after these assertions:

- Initial capture held all five families with zero longitudinal drift, ready boarding, linked controllers, and expected chassis/carrier mass.
- Arrow, Legacy ZacSpin, Free Spin, and Revised ZacSpin each launched more than 16 blocks with speed above 6 blocks/s, retained their riders and family bindings, then stopped below 0.02 blocks/s.
- PAX remained held at zero displacement despite its motor, with its rider retained.
- All five passed capture/boarding readiness, restraint release, retention, and final zero-drift assertions. The log ends the test with `FD_ENGINEERED installed=1.1.1 fourFamilies=launch/stop/capture/retention PAX=held`.

This is a combined-profile **server fixture** result. It does not establish five-family normal UI construction, physical FE supply or depletion, coupled Arrow link motion, PAX winch operation, client rendering, or a five-family occupied operator cycle in the combined profile. Those claims retain their separate evidence boundaries.
