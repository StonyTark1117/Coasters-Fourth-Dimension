# FD902 packaged occupied Arrow support-loss checkpoint

This bounded FD902 checkpoint sends a connected packaged client through an
actual matching-track support loss while it is seated in an Arrow 4D carrier.
It complements the existing five-family native-Pig support-loss regression with
a real player mount and replicated client state.

## Method

The packaged `0.9.0-dev.27` JAR SHA-256 was
`2ef1d98fab8a94b01c8d3fcac1c0ce592df219539d5be685c9cdae0ea8307a2a`.
A fresh disposable `FDArrowOccupiedSupportLossFinal` save ran on Xvfb:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FDArrowOccupiedSupportLossFinal \
  -PfdCarrierFixture -PfdCarrierFixtureRider \
  -PfdCarrierFixtureRiderOnly -PfdCarrierFixtureSupportLoss \
  -PfdCarrierValidation -PfdCarrierValidationRider \
  -PfdCarrierValidationSupportLoss -PfdCarrierValidationRender \
  -PfdCarrierValidationTimeout=1800 \
  -PfdCarrierValidationExit -PfdCarrierValidationSamples=40 \
  -PfdCarrierValidationFamilies=1
```

The fixture waits for a native Arrow carrier seat, boards the connected local
player through Create's `SeatBlock.sitDown`, then removes the Arrow vehicle's
actual Coasters anchor-peer curve. The carrier runtime must detect the missing
matching support, stop the vehicle, close restraints, retain the existing rider
assignment, and report `Matching track support lost`.

The client validation waits for the same mounted player and carrier assignment,
then requires the replicated Arrow snapshot to contain the exact fault with
`stopping` and `restraints` both true. It also requires the chassis and both
carrier bodies to enter Sable's vanilla section-render path.

## Result

The running server removed the real Arrow graph connection and reached the
fail-closed state while the player remained mounted. The client passed after
175 ticks with one retained rider assignment, all three native body sections
compiled and observed in 1,335 section-layer calls, and a maximum carrier-angle
error of `3.921752641583506E-6` radians. Client and integrated server exited
normally. The selected log is in [the support-loss record](client-occupied-arrow-support-loss.txt).

## Limits

This covers one occupied Arrow car and a support-loss stop. It does not cover
all five families, two clients, a moving route at loss, support repair or
re-binding, player mesh/camera behavior, or permanent disassembly. The native
five-family regression remains the evidence for those server-side family cases;
the remaining lifecycle matrix stays under FD902.
