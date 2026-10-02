# FD902 packaged five-family client carrier-transform motion checkpoint

This bounded FD902 checkpoint extends the five-family static carrier-transform
check with client-observed, controlled motion from all four circuit families:
Arrow, Legacy, Free Spin, and Revised. PAX remains loaded for the static matrix;
its normal winch-owned movement is outside this fixture.

## Method

The packaged JAR SHA-256 was
`be6ea4b55a277d69621ca2e471f6b50c0652cb17fe2b1c1af341f1224c5822c9`.
A fresh disposable `FDMultiMotionCarrierMatrixPass` quick-play save ran on Xvfb:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FDMultiMotionCarrierMatrixPass \
  -PfdCarrierFixture -PfdCarrierFixtureMotion \
  -PfdCarrierValidation -PfdCarrierValidationMotion \
  -PfdCarrierValidationMotionFamilies=4 -PfdCarrierValidationExit \
  -PfdCarrierValidationSamples=40 -PfdCarrierValidationFamilies=5
```

The empty-world fixture creates Arrow, Legacy, Free Spin, Revised, and PAX
examples: eight vehicles, 20 carrier bodies, and 35 rails. Before dispatch, the
client must compare every carrier angle relative to its chassis with its
replicated snapshot for 40 complete five-family samples, with each error at or
below 0.02 radians. The fixture then waits for all four circuit cars to become
board-ready, closes their restraints, and invokes the normal production dispatch
operation for each.

The subsequent motion phase only measures client-native chassis displacement.
It deliberately does not compare a live moving carrier angle with the replicated
snapshot because those sources can be one simulation tick apart. The gate requires
at least four non-PAX families to move one block from their first observed
position.

## Result

The client completed 40 pre-dispatch static samples with all 20 carriers and a
maximum angle error of `0.0002687598022542719` radians. It then observed more
than one block of movement for every required circuit family:

| Family | Maximum displacement (blocks) |
| --- | ---: |
| Arrow | 1.0834601098650583 |
| Legacy | 1.078991726770371 |
| Free Spin | 1.108615833446712 |
| Revised | 1.1063523290191744 |

The fixture passed after the controlled dispatch, requested normal shutdown, and
the integrated server saved `FDMultiMotionCarrierMatrixPass`.
The selected source-log lines are retained in
[the motion transform record](client-carrier-transform-five-family-motion.txt).

## Limits

The run had no rider assignments. Three early Sable packet-order errors occurred
before the static matrix converged. This is evidence of eventual pre-dispatch
transform agreement and controlled client-native movement for the four circuit
families. It does not verify PAX winch motion, passenger meshes or cameras,
interpolation quality, multiplayer, reconnects, reloads, rendered clearance,
shaders, or frame rate. Those remain FD401, FD402, FD902, and FD904 work.
