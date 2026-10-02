# FD904 packaged five-family vehicle render-compilation checkpoint

This bounded FD904 checkpoint verifies that each replicated original vehicle
body enters Sable's client block-render compilation pipeline. It extends the
existing five-family carrier transform/motion fixture; it is not a frame,
shader, mesh-clearance, rider-render, or camera test.

## Method

The packaged JAR SHA-256 was
`66be4a958ee96db9067dd7c01f06fc521400e745b0e955066a23f8aff2a6cdd6`.
A fresh disposable `FDFiveCompiledCarrierMatrix` quick-play save ran on Xvfb:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FDFiveCompiledCarrierMatrix \
  -PfdCarrierFixture -PfdCarrierFixtureMotion \
  -PfdCarrierValidation -PfdCarrierValidationRender \
  -PfdCarrierValidationMotion \
  -PfdCarrierValidationMotionFamilies=5 \
  -PfdCarrierValidationMotionPaxChassis=4 \
  -PfdCarrierValidationExit -PfdCarrierValidationSamples=40 \
  -PfdCarrierValidationFamilies=5
```

During the same 40 pre-dispatch samples that compare each carrier's
client-native orientation to its synchronized snapshot, the opt-in checker
finds the Sable section containing every chassis and carrier body and requires
that section to be compiled. The fixture has eight chassis and 20 carriers:
one each for Arrow, Legacy, Free Spin, and Revised, plus the four-car PAX
shuttle. It then repeats the existing movement gate, including one-block
movement for all four PAX chassis.

## Result

The client passed after 509 ticks. All 28 expected native body sections had
compiled (`compiledBodies=28`, `renderRequired=true`); the maximum static
carrier-angle error was `0.005636141902137417` radians. Every functional family
moved, including all four PAX chassis. The client requested normal shutdown and
the integrated server saved the disposable world. The selected log lines are in
[the render-compilation record](client-carrier-render-compile.txt).

## Limits

Sable section compilation shows that these replicated block bodies reached the
client render pipeline. It does not prove that a particular frame submitted a
vehicle, that it was visible through the camera/occlusion path, or that shaders
rendered it correctly. It also does not test player meshes, camera transforms,
or vehicle/rider/linkage clearance. Those remain FD402, FD902, and FD904 work.
