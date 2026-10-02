# FD902 packaged five-family client carrier-transform motion checkpoint

This bounded FD902 checkpoint extends the five-family static carrier-transform
check with client-observed controlled movement for Arrow, Legacy, Free Spin,
Revised, and the PAX shuttle winch path.

## Method

The packaged JAR SHA-256 was
`b4a4ab40fa76bf229f1f1d95bc47324cfd957d78080b9f6d646ffd2e080ea6d7`.
A fresh disposable `FDFiveMotionCarrierMatrix` quick-play save ran on Xvfb:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FDFiveMotionCarrierMatrix \
  -PfdCarrierFixture -PfdCarrierFixtureMotion \
  -PfdCarrierValidation -PfdCarrierValidationMotion \
  -PfdCarrierValidationMotionFamilies=5 -PfdCarrierValidationExit \
  -PfdCarrierValidationSamples=40 -PfdCarrierValidationFamilies=5
```

The empty-world fixture creates Arrow, Legacy, Free Spin, Revised, and PAX
examples: eight vehicles, 20 carrier bodies, and 35 rails. Before dispatch, the
client compares every carrier angle relative to its chassis with its replicated
snapshot for 40 complete five-family samples, requiring each error to remain at
or below 0.02 radians.

After that static phase, the fixture waits for its four circuit cars and its
PAX four-car train to become board-ready. It assembles the PAX train through
`ShuttleExamples.assemble`, then uses production restraint closing and dispatch:
the four circuit cars dispatch directly and PAX starts through its bound,
powered winch. The motion phase tracks each chassis from its own first observed
position and aggregates the greatest displacement for every functional family.
Each of the five families must move at least one block.

The dynamic phase intentionally does not compare a live moving carrier angle
with the replicated snapshot because those sources can be one simulation tick
apart.

## Result

The client completed 40 pre-dispatch static samples with all 20 carriers and a
maximum angle error of `0.005636196234582461` radians. It then observed more
than one block of client-native movement for every family:

| Family | Maximum displacement (blocks) |
| --- | ---: |
| Arrow | 1.0340346209126012 |
| Legacy | 1.0305849438119161 |
| Free Spin | 1.056600768467801 |
| Revised | 1.0568885816473406 |
| PAX | 1.7131083083809422 |

The client requested normal shutdown and the integrated server saved
`FDFiveMotionCarrierMatrix`. The selected source-log lines are retained in
[the motion transform record](client-carrier-transform-five-family-motion.txt).

## Limits

The run had no rider assignments. It establishes eventual pre-dispatch transform
agreement and controlled client-native movement for all five families. It does
not verify passenger meshes or cameras, interpolation quality, multiplayer,
reconnects, reloads, rendered clearance, shaders, or frame rate. Those remain
FD401, FD402, FD902, and FD904 work.
