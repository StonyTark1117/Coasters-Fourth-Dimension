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
