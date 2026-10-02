# FD902 packaged five-family client carrier-transform checkpoint

This bounded FD902 checkpoint verifies eventual static convergence between each
synchronized Fourth Dimension carrier snapshot and its client-native Sable body
for every currently implemented 4D family. It does not close the wider client
lifecycle work.

## Method

The packaged JAR SHA-256 was
`9afa33c1ce44b21959db4b99595a2155a5f913ef1609a4352c907a7d4557de12`.
A fresh disposable `FDCarrierMatrix` quick-play save was opened on Xvfb with the
opt-in fixture and transform gate:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged -PfdWorld=FDCarrierMatrix \
  -PfdCarrierFixture -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationSamples=40 -PfdCarrierValidationFamilies=5
```

`CarrierValidationFixture` rejects a non-empty world, preloads the fixture
corridor, and creates Arrow, Legacy, Free Spin, Revised, and PAX examples. The
scene contains eight vehicles, 20 carrier bodies, and 35 rails. The login hook
places the local player at the fixture so all bodies can be observed by the
client.

`CarrierTransformValidation` waits until the required native bodies exist, then
compares each carrier body's angle relative to its chassis with the replicated
snapshot. Each sample must be within 0.02 radians; the gate requires 40
consecutive complete samples and all five functional families.

## Result

The client passed 40 consecutive complete samples after 55 client ticks. It
observed all five families and all 20 carrier bodies, with a maximum angle error
of `0.005636176411582603` radians. The client then requested normal shutdown,
the integrated server saved `FDCarrierMatrix`, and Gradle completed
successfully. The selected source-log lines are retained in
[the five-family transform record](client-carrier-transform-five-family.txt).

## Startup observation and limits

Before the gate reached a complete native-body set, Sable emitted three
`Received a sub-level movement packet for a non-existent sub-level` errors. The
transform check subsequently converged and passed, but this checkpoint does not
claim that startup packet ordering is clean or resolved.

There were no rider assignments. This static single-client result does not prove
rider meshes or cosmetics, camera transforms, motion and interpolation,
multiplayer, reconnects, reloads, rendered clearance, shaders, or frame-rate
behavior. Those remain FD401, FD402, FD902, and FD904 work.
