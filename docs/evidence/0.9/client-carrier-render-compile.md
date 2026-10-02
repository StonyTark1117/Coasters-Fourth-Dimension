# FD904 packaged five-family vehicle section-render checkpoint

This bounded FD904 checkpoint verifies that each replicated original vehicle
body enters Sable's client block-render compilation pipeline and its vanilla
chunked section-layer renderer. It extends the existing five-family carrier
transform/motion fixture; it is not a camera-visibility, shader,
mesh-clearance, rider-render, or camera test.

## Method

The packaged JAR SHA-256 was
`7df53638615e7ae42e5705c873a1078761100535ae88519ae37e754cdac6fc1d`.
A fresh disposable `FDFiveChunkedSectionRenderMatrixFix` quick-play save ran on
Xvfb:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FDFiveChunkedSectionRenderMatrixFix \
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
that section to be compiled. A client-only mixin observes Sable 2.0.3's
`VanillaSubLevelRenderDispatcher.renderSectionLayer` call and records only
bodies using `VanillaChunkedSubLevelRenderData`, the branch that immediately
calls Sable's chunked-sublevel renderer. The fixture has eight chassis and 20
carriers: one each for Arrow, Legacy, Free Spin, and Revised, plus the four-car
PAX shuttle. It then repeats the existing movement gate, including one-block
movement for all four PAX chassis.

## Result

The client passed after 502 ticks. All 28 expected native body sections had
compiled, and the same 28 chunked bodies passed through 2,340 section-layer
dispatches (`compiledBodies=28`, `sectionLayerBodies=28`,
`sectionLayerCalls=2340`). The maximum static carrier-angle error was
`3.581027940612285E-4` radians. Every functional family moved, including all
four PAX chassis. The client requested normal shutdown and the integrated
server saved the disposable world. The selected log lines are in [the
section-render record](client-carrier-render-compile.txt).

## Limits

This establishes entry into Sable's vanilla chunked section-render path, not
that a vehicle was camera-visible after culling/occlusion or that all generated
vertices reached the framebuffer. It does not test shader modes, player meshes,
camera transforms, or vehicle/rider/linkage clearance. Those remain FD402,
FD902, and FD904 work.
