# Additions 0.2.0 station and fin development evidence

Baseline: Minecraft 1.21.1, Java 21, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Coasters 0.1.5 and Sable 2.0.3. Exact additional
artifacts and hashes are in `dependencies.json`; Aeronautics bundled 1.3.2 also
loads Simulated and Offroad 1.3.2. The combined profile adds Engineered 1.1.1 and
Track Styles 1.2.0, using the hashes in the parent integration evidence.

## Reproduction and observed results

Run `JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew
-Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer` with these
profile arguments. Each native run starts a fresh disposable GameTest world.

| Profile | Result | Scope |
| --- | --- | --- |
| `-PfdIntegrations=additions` | 22 registered tests passed; Engineered explicitly skipped | Initial station/fin acceptance, before adding the occupied PAX external-equipment probe. |
| `-PfdIntegrations=engineered,styles,additions` | All 22 tests executed and passed | Final adapter, occupied PAX interference probe, three Engineered powered circuits and Styles registry/persistence checks. |
| No profile | 22 registered tests passed; Additions and Engineered explicitly skipped | Standalone startup and native regressions with optional APIs absent. This is a clean world, not removal from an occupied addon save. |

After importing FD305 evidence and its family eligibility test, the current
Additions-only profile passed all 23 registered native tests (Engineered skipped),
including the final occupied PAX interference probe and all 25 family-pair decisions.
See `reconciled-additions-native.txt`, `reconciled-artifact.sha256` and
`fd305-reconciliation.txt`. Runtime integration code is unchanged from the combined
22-test pass; the extra registered test is the imported family matrix.

All 17 unit tests passed. Selected logs retain the original full-log hashes.
`artifact.sha256` identifies the immutable build copy; these native tests load
production classes in the development harness, not a packaged two-client session.

The real Additions station scanner, native wireless channel objects and powered
fin blocks were exercised for all five families. Each held car rejected propulsion
and auto-dispatch with open restraints or maintenance retention. One pig occupied
a carrier seat; capacity, analog weight, closed-restraint output, six Display Link
sources and client snapshot tags were checked. A cached station could not authorize
an operation outside a fresh native scan. Ready delay preceded automatic dispatch.

Arrow, legacy, Free Spin and revised cars traveled over six blocks under BOOST at
strength 10, retained their passengers, then stopped under actual STOP fins with
measured chassis speeds below 0.017 blocks/second. Normal capture restored boarding.
The PAX single-car fixture stayed held with the explicit winch-only dispatch denial.
Default-strength calibration on the short test track reached the existing track-end
guard; it was not accepted as a successful braking run.

The combined run also placed a powered default-strength BOOST fin across all three
segments of the existing sixteen-passenger PAX shuttle and enabled an Additions
station with zero dispatch delay. The probe asserted active power throughout the
occupied cycle. The real winch still lifted, held during a power interruption,
resumed, released, returned and captured with all sixteen riders and native couplers
intact. This proves external equipment cannot take control in that fixture; it does
not certify external PAX propulsion or permit bypassing the winch state machine.

## Limits

No Additions packaged visual inspection, real two-client/late-join run, occupied
restart/removal, mixed/partial-train span case, neighbor-force regression fixture,
wireless receiver wiring demonstration, or speed-control/rollback certification is
claimed. The goggles implementation and packet tags are checked indirectly here;
rendered UI and real-client synchronization remain pending. Rotating Additions seats
and separate sensors have no adapter. Full FD901–FD904 certification remains open.

The dependency stack emits client-class mixin warnings on dedicated startup, as in
the baseline addon inspection. Startup and these tests complete, but this is not a
warning-free pack claim. Interrupted development runs and rejected fixture setups
were excluded from passing evidence.
