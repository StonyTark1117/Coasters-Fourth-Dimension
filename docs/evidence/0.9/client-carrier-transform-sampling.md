# FD902 packaged client carrier-transform checkpoint

This bounded FD902 checkpoint verifies that a real packaged client receives the
Fourth Dimension snapshot for each loaded carrier and that the corresponding
client-native Sable body has the same relative rotary angle. It is not rider,
camera, rendered-mesh-clearance, moving-ride, multiplayer, or all-family client
acceptance.

## Method

The packaged JAR SHA-256 was
`eb157f9c8900cf9e0255e1c56c31cd4a9a27387b44d7e07c5ff4b9f52955a934`.
It quick-played the existing `FDRenderMatrix` single-player world on Xvfb, with
Track Styles 1.2.0 and Coasters: Engineered 1.1.1 installed:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdIntegrations=styles,engineered -PfdWorld=FDRenderMatrix \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationSamples=40 -PfdCarrierValidationFamilies=1
```

`CarrierTransformValidation` performs no pose, rider, or camera writes. On each
sample it finds every loaded vehicle and carrier in the client snapshot, resolves
its native chassis and carrier bodies, derives the child orientation relative to
its chassis, and requires the shortest wrapped angular error against the snapshot
to remain at or below 0.02 radians. It requires the selected count of functional
families and all those carriers on every sample. `-PfdCarrierValidationFamilies=N`
accepts values 1 through 5; a five-family world can use the same gate later.

## Result

`FDRenderMatrix` is deliberately an Arrow-only render fixture. It contains six
loaded carrier bodies and no rider assignments. The client passed all 40 requested
consecutive samples after 40 world ticks, with a maximum angle error of
`7.879373870500663e-7` radians. It then requested normal Minecraft shutdown;
the integrated server saved `FDRenderMatrix` and Gradle exited successfully.
The selected source-log lines are retained in
[the transform record](client-carrier-transform-fdrender-matrix.txt).

## Limits

This confirms a static one-family client snapshot/native-body transform contract.
It does not prove the client receives all five families in one world, rider meshes
or player cosmetics, camera transforms, moving carrier interpolation, reload,
multiplayer, rendered clearance, shaders, or frame-rate behavior. Those claims
retain FD401, FD402, FD902, and FD904 acceptance work.
