# FD902/FD904 packaged occupied Arrow carrier checkpoint

This bounded checkpoint adds an opt-in packaged-client fixture that boards the
connected local player in an actual Arrow 4D carrier through Create's
`create:seat` path. It complements the existing five-family, rider-free
section-render/motion matrix. The production fixture never enables this mode.

## Method

The packaged `0.9.0-dev.27` JAR SHA-256 was
`5cd5b41299f3116c59b5bce55e906503ce132c97fd3de2250b705b729f1812db`.
A fresh disposable `FDArrowOccupiedCarrierMatrixFinal` save ran on Xvfb:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FDArrowOccupiedCarrierMatrixFinal \
  -PfdCarrierFixture -PfdCarrierFixtureRider \
  -PfdCarrierFixtureRiderOnly -PfdCarrierFixtureMotion \
  -PfdCarrierValidation -PfdCarrierValidationRider \
  -PfdCarrierValidationRender -PfdCarrierValidationMotion \
  -PfdCarrierValidationTimeout=1800 \
  -PfdCarrierValidationMotionFamilies=1 \
  -PfdCarrierValidationMotionPaxChassis=0 \
  -PfdCarrierValidationExit -PfdCarrierValidationSamples=40 \
  -PfdCarrierValidationFamilies=1
```

The server waits for a native Arrow carrier to be boarding-ready, locates an
unoccupied addon carrier seat in that sublevel, and calls `SeatBlock.sitDown`.
The client checker requires its local player to remain both mounted and present
in the replicated carrier-seat assignment. It also requires the chassis and
both carrier sections to compile and enter Sable's vanilla section-layer path,
then observes normal production dispatch moving the Arrow chassis at least one
block.

## Result

The player boarded one Arrow carrier before motion. After 40 static samples,
the client still had one rider assignment and a mounted local rider; three
native sections were compiled and entered the section-layer renderer. The
maximum pre-dispatch carrier angle error was `3.7507639416837666E-5` radians.
Production dispatch moved the chassis `1.0790031301446994` blocks and the
client and integrated server exited normally. The selected log is in [the
occupied Arrow record](client-occupied-arrow-carrier.txt).

## Limits

This is one local Arrow rider, one client, and one short controlled movement.
It does not establish player mesh or camera behavior, visual clearance,
interpolation quality, multiplayer rider synchronization, other 4D families,
or a complete ride lifecycle. Those remain FD402, FD902, and FD904 work.
