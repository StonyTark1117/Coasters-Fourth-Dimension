# FD401 loaded controller during native body gaps

On 2026-11-04, an isolated combined packaged client ran the focused
`loadedcontrollerpublishesmissingnativebodies` GameTest from source `2ba168d`.
The archived [Fourth Dimension JAR](coasters-fourth-dimension-0.9.0-dev.27.jar)
has SHA-256 `7430b4f4abdb41e248efd778f5cd9f68929d04211d7837085bbae287565cbdcf`.
The build used `-PfdPackaged -PfdValidation -PfdIntegrations=styles,engineered
-PfdFeNetwork`, an isolated Gradle build/game directory, and
`-Dneoforge.enabledGameTestNamespaces=fourthdimension
-Dneoforge.enableGameTest=true`. The [raw log](client.log.gz),
[pass screenshot](pass-chat.png), [stopped world](world.tar.gz), and
[hash inventory](SHA256SUMS) retain the run. The client saved and quit normally;
Gradle exited 0.

The fixture kept a bound Arrow Boarding Station and its Create Display Link
source loaded. It temporarily removed one actual carrier through the native
`UNLOADED` callback while preserving the logical vehicle. After the station's
normal tick, its snapshot changed from available to `Vehicle or carrier
unavailable`; all six Display Link source fields and the station's goggle
formatter returned that message, and the block update tag contained the same
snapshot. Native serializer restoration returned the station to available.
The test then repeated the sequence for the actual chassis, which correctly
reported `Vehicle chassis is unavailable`, and again recovered after restore.
The log records both cases and `All required tests passed :)` for one named
test.

This checks the loaded server controller, source API, tooltip formatter and
packet payload. It does not show a remote client's actual goggles or a physical
Display Link target during the gap, nor does it cover late join, reconnect,
resource reload, cold restart or the standalone optional profiles. The first
run's carrier case passed but the chassis case failed a test assertion that
expected the carrier's wording; the assertion was corrected and the passing
run used a fresh world copy.
