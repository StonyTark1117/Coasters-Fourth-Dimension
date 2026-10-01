# Native banked Arrow circuit checkpoint

This advances FD401 in **0.9.0-dev.6**, with schema/network protocol 5. It does
not close the integration, 0.4 or 0.9 release gates. Optional mods remain
separately installed and no provider assets or vertices are committed.

## Reproduce

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer \
  -PfdIntegrations=styles,engineered
python3 scripts/audit-arrow-style-geometry.py /path/to/installed-track-styles-1.2.0.jar \
  /path/to/build/runs/gametest/bank-native-frames.json
```

Use the actual build output directory reported by Gradle; colon-containing
Unix checkouts use the documented temporary build directory. The native test
writes its own frames there, and a fresh invocation removes any previous frame
export. The audit pins the installed JAR by SHA-256 and fails on incomplete,
non-finite, incorrectly spaced or discontinuous frame data. Styles/combined CI
repeats the audit and uploads the frames and aggregate result.

The player-facing `/fd demo arrow_bank` builds the same native geometry with a
level loading station. It reserves the existing clear circuit footprint, sets
the two upper anchors to 30°, and installs native curves, chains and motors.
Shared normals and lifted centers are resolved before adding incident curves;
banked motors sit on the high side with the corresponding facing/speed sign.

## Native motion

`BankedArrowGameTests` builds eight native sections near world origin, leaves
all control and rotary motion to the existing native physics, and boards a real
pig through Create's native seat API. It checks valid matching support, normal
restraint closing/dispatch, a complete controlled turn on the 30° upper straight,
another complete turn on the level right straight, rear-input station capture,
restraint opening and normal passenger unloading. No vehicle velocity, angle or
save-state write is used to move through those phases. Fixtures are removed
at completion.

All four final profiles pass **25 registered native GameTests**, and all **24
JUnit tests** pass. Without Engineered, its four operating scenarios explicitly
pass availability guards rather than certifying installed operation. The final
native logs record banked/level peaks of approximately **6.272018/6.281153
radians**, each within the required **0.08-radian** full-turn tolerance. The bank is approximately **30.0000008°**. Joint center error is zero;
maximum native tangent/up vector differences are approximately **0.00017347**
and **0.00008410**. The native frame API uses a one-sided parameter difference
of 0.0002 at endpoints; the test permits a vector difference below 0.001 while
requiring center error below 0.000001. This tolerates that measured frame
approximation while rejecting the earlier substantial joint discontinuities.
`bank-unit.json` and `bank-artifact.sha256` identify the unit result and final JAR.

## Installed decoration/control-rail geometry

`bank-native-frames.json` contains only our native sampled positions and axes,
not external geometry. `bank-clearance.json` records aggregate measurements for
the pinned default Arrow single/double spine and their ties. The Python script
reads each separately installed OBJ into memory, applies the same positive
model frame, spine segment scaling and tie origins as the renderer, and compares
every placed piece against every rendered control-rail segment in the circuit.

A convex volume contains the rendered square tube over the complete continuous
**-4..+2-turn** stroke, including its 0.09-block half-width and half-height.
Bounding-box, projected convex-polygon/triangle and separating-axis gaps are
conservative lower bounds on true surface distance. The audit checks a minimum
0.01-block margin; it does not report those lower bounds as exact closest-point
distances. The corrected circuit's smallest measured lower bound is about
**0.04331 blocks**. Both installed styles retain the default unshortened ties.
The result covers the rendered polygonal control segments continuously, rather
than only their discrete center samples.

## Failures that changed the fixture

The first fixture stored bank angles but supplied level curve normals, so its
bank-angle assertion failed. Resolving normals separately for approaching and
leaving axes then produced 60° disagreements at shared joints. Resolving each
normal once fixed that issue, but sequential world-center lookup shifted banked
lifted endpoints by about 0.25882 blocks. The mesh audit exposed a resulting
uncertified tie/control gap. Shared lifted centers eliminated that gap without
altering provider assets or their rendering.

The center-continuous fixture then stalled at the banked joint. Native position
and velocity readings showed a fixed low-side approach stall; moving the motor
to the high side exposed opposing drive at the join until its facing-dependent
speed sign was corrected. The final complete loaded circuit checks both drive
transitions and station capture. These earlier failures are retained in
`bank-investigation.txt`; passing a frame or mesh check alone did not end the
native ride investigation.

## Remaining scope

This is an actual native loaded ride and a recorded default-mesh geometry check.
It is not a banked real-player GUI/render observation. It excludes arbitrary
layouts, resource-pack replacements, wheels/chassis/rider geometry, linked-car
sweeps, Flywheel/backend/shader compatibility, and complete operator/FE-network
workflows. Those checks, recovery evaluation, teaching content and the remaining
0.9 stabilization gates are still open.
