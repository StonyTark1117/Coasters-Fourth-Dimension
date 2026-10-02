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
