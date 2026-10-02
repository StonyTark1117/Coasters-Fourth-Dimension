# FD904 client track-render instrumentation checkpoint

This checkpoint validates the client-side sampler introduced for FD904. It is
not a hardware performance baseline or an FPS/shader result.

## Method

The packaged `0.9.0-dev.27` artifact with SHA-256
`d1fb2c26425b77d9ce75026ad298ba26eddcd2c32456ec46af068ddb5cc2e49a`
was launched into the existing `FD16Recovery` single-player world with Track
Styles 1.2.0 and Coasters: Engineered 1.1.1 installed:

```sh
Xvfb :99 -screen 0 1280x720x24 &
DISPLAY=:99 JAVA_HOME=/usr/lib/jvm/java-21-openjdk timeout 50s \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdIntegrations=styles,engineered -PfdWorld=FD16Recovery \
  -PfdValidation -PfdRenderValidation
```

The run used Mesa llvmpipe under Xvfb. Its 50-second timeout intentionally
ended the interactive client after the sampler had logged multiple samples.
The client log contains no fatal exception or crash report entry.

`FD_CLIENT_RENDER` measures this addon's `AFTER_BLOCK_ENTITIES` original-track
stage. `inRange*` totals cover tracks within the renderer's 160-block distance
test; they are not a camera-frustum count.

## Result

The final cumulative sample was:

```text
FD_CLIENT_RENDER stages=3026 avgStageNanos=5950152 meshBuilds=3 avgMeshBuildNanos=3844204 inRangeRails=9077 inRangeQuads=80603760 inRangeDecorationPieces=0
```

The fixed camera had original geometry in range and rebuilt three meshes. Track
Styles was loaded and its catalog was discovered, but this saved scene used no
Track Styles decoration, so the decoration-piece count correctly stayed zero.

## Limits

llvmpipe is a software OpenGL renderer and cannot set a player-hardware target,
validate GPU cost, establish whole-frame timing or exercise a shader pack. The
stage time includes only this addon's procedural original-track work and buffer
submission. It excludes the rest of Minecraft rendering, Flywheel, Veil,
Sable, shader execution and FPS. Hardware and supported-shader measurements,
including a decorated Track Styles scene, remain FD904 work.

## Native GPU observation

The same bounded packaged-world procedure was repeated on the host's native
desktop OpenGL context with the final `0.9.0-dev.27` JAR SHA-256
`697f14eb9e58c38789587f7af1efc33b30d79fb10a4ef871dd08c4b82b446712`.
The early renderer log identified an **AMD Radeon RX 7900 XTX** using Mesa
26.2.3 on the standard renderer; Track Styles 1.2.0 and Coasters: Engineered
1.1.1 remained installed. The interactive client was again stopped by the
55-second timeout after sampling. No crash or fatal exception was recorded
before that intentional stop.

Its final cumulative sample was:

```text
FD_CLIENT_RENDER stages=2308 avgStageNanos=3156350 meshBuilds=3 avgMeshBuildNanos=2654895 inRangeRails=6924 inRangeQuads=61485120 inRangeDecorationPieces=0
```

This is a native-GPU observation of the original-track stage on this host. It
does not measure whole-frame time or FPS, record the desktop resolution, set a
cross-hardware target, validate a shader pack, or cover Track Styles decoration
because the saved scene contains no decoration pieces. Those render modes remain
required FD904 work.

## Native GPU decorated Track Styles observation

A fresh dedicated packaged server was started with Track Styles 1.2.0 and
Coasters: Engineered 1.1.1. A fresh original Arrow example was built at a
force-loaded location, then a local validation command selected an unoccupied
nearby Arrow section and called the normal server-side `AppearanceEditor`
transaction. The command was accepted as `Validation selected Track Styles Arrow
appearance`; it did not write the provider's assets or bypass the normal
permission, revision, original-track, profile, provider, or occupancy checks.

A native-desktop client connected to that server on the same AMD Radeon RX 7900
XTX / Mesa 26.2.3 host. Its final sample, from the packaged
`0.9.0-dev.27` JAR SHA-256
`605aa0c4d26913e213d6314df4bd2a99db9616240dd549ca8c3ac336723dc7e6`,
was:

```text
FD_CLIENT_RENDER stages=3463 avgStageNanos=330831 meshBuilds=9 avgMeshBuildNanos=2185150 inRangeRails=1874 inRangeQuads=6576984 inRangeDecorationPieces=21476
```

The nonzero decoration-piece count demonstrates that the sampled renderer
observed the inspected Track Styles decoration path on a genuine original Arrow
rail. It remains a cumulative stage-instrumentation observation: it does not
establish FPS, full-frame timing, a cross-hardware performance target, or shader
pack compatibility. Shader-mode and broader FD904 acceptance remain open.

## Native Iris/Solas shader-mode observation

The same packaged `0.9.0-dev.27` client and `FD16Recovery` world were launched
on the native desktop GPU with the audited Track Styles 1.2.0 and Engineered
1.1.1 addons. The disposable client directory additionally contained Iris
1.8.14-beta.1, Sodium 0.8.13, and Solas Shader V3.7b; none is packaged with
Fourth Dimension or declared as a release dependency. Iris logged `Using
shaderpack: Solas Shader V3.7b.zip` and created its Overworld pipeline on the
AMD Radeon RX 7900 XTX / Mesa 26.2.3 host.

The 55-second bounded run reached its planned timeout without a fatal exception or crash report.
Its final sampler entry was:

```text
FD_CLIENT_RENDER stages=2022 avgStageNanos=1865860 meshBuilds=3 avgMeshBuildNanos=1526743 inRangeRails=6066 inRangeQuads=53866080 inRangeDecorationPieces=0
```

This confirms that the packaged original-track stage rendered while Solas was
active. Flywheel reported a fallback from `flywheel:indirect` to
`flywheel:off`, so this run cannot certify the normal Flywheel backend, visual
quality, whole-frame performance, FPS, Track Styles decoration, or general
shader-pack compatibility. Those remain FD904 acceptance work.

The fallback is expected for the pinned Flywheel 1.0.6 release: inspection of
its `Backends` class shows that both `indirect` and `instancing` require
`ShadersModHelper.isShaderPackInUse()` to be false. With Iris reporting Solas
active, neither backend is supported and Flywheel selects `off`. This is an
upstream backend limitation, not a Fourth Dimension renderer failure. The
remaining FD904 shader check is therefore visual/direct-render validation with
the documented backend limitation, not an attempt to force a disabled backend.
