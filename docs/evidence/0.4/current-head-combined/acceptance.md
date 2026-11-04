# Current-head combined-profile 0.4 checkpoint

On 2026-11-02, source `7d438710cfc0644d46119c49a650f026f9124900`
built and ran with the pinned Track Styles 1.2.0 and Engineered 1.1.1 JARs.
The resulting `0.9.0-dev.27` JAR has SHA-256
`39709f1e35870b7b5c5865e6c19920f6f8430d61cd8179dfe95d56428474a94c`.
The command was `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build
runGameTestServer -PfdIntegrations=styles,engineered --console=plain`.
Gradle exited successfully after 28 JUnit tests and all 47 required native
GameTests passed. The complete local command log has SHA-256
`bc948ebe46eb0ef41f8ca6bfee90e03944a4c88265ad139bafb33af5fe1856a6`;
[selected native results](native-summary.txt) retain the Engineered FE and
control observations, five occupied banked single-car recoveries, and occupied
Arrow/PAX train recoveries.

The native bank-frame export has SHA-256
`4393f27c6238063a23764d4681bccb77392643f0bb3ae1ff7d83c3b18c10254d`.
Independent [single-car route](recovery-route-geometry.json) and
[linked-train route](recovery-train-geometry.json) frame checks also pass.
The [installed Track Styles geometry audit](bank-style-clearance.json) passes
both Arrow decorations and their ties against the full supported control-rail
stroke on that recorded banked circuit. Its smallest conservative gap is
0.04098 blocks, above the required 0.01. The [level-straight wheel
audit](level-wheel-clearance.json) passes the pinned full-spin native wheel
envelopes against both installed Arrow styles and native supports; its legacy
placement negative control still detects the old tie conflict.

This is a current-head regression checkpoint for FD401 and FD402. The native
tests do not substitute for connected-client operator workflows. The geometry
audits cover pinned default assets and the recorded banked control rail or
level straight wheel path, respectively. Complete banked wheel/vehicle/rider
clearance, render modes, physical player/operator acceptance, and the broader
recovery lifecycle matrix remain open. FD401 and FD402 remain in progress;
FD404 remains open.
