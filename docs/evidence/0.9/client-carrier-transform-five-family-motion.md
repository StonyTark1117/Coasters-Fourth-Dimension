# FD902 packaged five-family client carrier-transform motion checkpoint

This bounded FD902 checkpoint extends the five-family static carrier-transform
check with real, client-observed motion of one original controlled car. It does
not close the wider moving-rider or lifecycle work.

## Method

The packaged JAR SHA-256 was
`ff4b9d6c5a6b408825790d8355cf5226cf47fa088168e1500dbf1f0f60bfb9ec`.
A fresh disposable `FDMotionCarrierMatrix` quick-play save ran on Xvfb:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged -PfdWorld=FDMotionCarrierMatrix \
  -PfdCarrierFixture -PfdCarrierFixtureMotion \
  -PfdCarrierValidation -PfdCarrierValidationMotion \
  -PfdCarrierValidationExit -PfdCarrierValidationSamples=40 \
  -PfdCarrierValidationFamilies=5
```

The existing empty-world fixture creates Arrow, Legacy, Free Spin, Revised and
PAX examples: eight vehicles, 20 carrier bodies, and 35 rails. Its motion mode
waits for the Arrow car to become board-ready, closes its restraints, and uses
the normal production dispatch operation. The client gate continues to compare
every carrier angle relative to its chassis with its replicated snapshot,
requiring a maximum error of 0.02 radians for 40 complete samples across all
five families. In motion mode it also requires the Arrow chassis' client-native
center to move at least one block from its first observed position.

## Result

The fixture dispatched its Arrow chassis and passed after 128 client ticks. The
client observed all 20 carriers across the five families for 40 complete samples,
with a maximum angle error of `0.005636209134113855` radians and an Arrow
chassis displacement of `4.9857955498446485` blocks. The client requested normal
shutdown and the integrated server saved `FDMotionCarrierMatrix`; Gradle returned
successfully. The selected source-log lines are retained in
[the motion transform record](client-carrier-transform-five-family-motion.txt).

## Limits

This run had no rider assignments. It verifies one local controlled Arrow motion
path while the other four families remain loaded and their carrier transforms are
checked. It does not verify passenger meshes or camera transforms, motion of all
five families, interpolation quality, multiplayer, reconnects, reloads, visual
clearance, shaders, or frame rate. Those remain FD401, FD402, FD902, and FD904
work.
