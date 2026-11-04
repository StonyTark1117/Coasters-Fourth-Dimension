# FD401 exact-candidate FAST and FABULOUS render-mode gates

On 2026-11-04, two Styles-only packaged clients cold-opened separate copies of
the saved world from the [exact-candidate post-edit ride](../fd401-styles-ui-reload-ride-2c5d9a7/README.md).
Both used the isolated `2c5d9a7` build and the retained JAR with SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
matching the [final candidate](../fd401-final-candidate-2c5d9a7/README.md).
The logs identify that isolated JAR path at load time.

The runs used fresh game directories under
`/tmp/fd401-render-exact-2c5d9a7-y31pT1` and a newly started Xvfb server on
display `:171`. No existing display, client, game directory, save copy or build
output was reused or controlled. FAST completed and stopped before FABULOUS
started. The display was stopped after both clients exited.

The commands differed only by game directory and expected graphics mode:

```text
./gradlew runClient --offline --no-daemon --console=plain \
  --project-cache-dir /tmp/fd401-candidate-2c5d9a7-V9tBRB/project-cache \
  -PfdPackaged \
  -PfdBuildDirectory=/tmp/fd401-candidate-2c5d9a7-V9tBRB/build-output \
  -PfdClientGameDirectory=<isolated-mode-game-directory> \
  -PfdValidation -PfdRenderValidation -PfdRenderValidationExit \
  -PfdRenderValidationStages=500 \
  -PfdRenderValidationGraphicsMode=<FAST-or-FABULOUS> \
  -PfdIntegrations=styles -PfdWorld=FD401StopCycle
```

The retained [FAST options](fast-options.txt) select graphics mode `0`; the
strict runtime guard reported `graphicsMode=fast expectedGraphicsMode=FAST`,
Flywheel `indirect` enabled and render distance 12. Its automatic gate passed
at 500 geometry stages with 14 mesh builds, 6,301 accumulated in-range rails,
21,724,672 quads and 84,901 decoration pieces. The client then saved every
dimension, stopped normally and Gradle exited `BUILD SUCCESSFUL`.
[fast-client.log.gz](fast-client.log.gz) retains the exact-JAR load, mode marker,
gate result and shutdown.

The retained [FABULOUS options](fabulous-options.txt) select graphics mode `2`;
the strict runtime guard reported `graphicsMode=fabulous
expectedGraphicsMode=FABULOUS`, Flywheel `indirect` enabled and render distance
12. Minecraft emitted Create's expected Fabulous-graphics limitation warning.
The automatic gate passed at 500 geometry stages with 14 mesh builds, 6,391
accumulated in-range rails, 22,032,832 quads and 85,639 decoration pieces. The
client then saved every dimension, stopped normally and Gradle exited `BUILD
SUCCESSFUL`. [fabulous-client.log.gz](fabulous-client.log.gz) retains the
exact-JAR load, mode marker, gate result and shutdown.

This closes exact-candidate identity and strict nonzero styled-geometry gates for
FAST and FABULOUS. The earlier [render-mode record](../fd401-styles-render-modes/README.md)
retains visible screenshots and the pinned Iris/Solas FANCY observation; its
Solas run correctly used Flywheel `off`. These exact-candidate passes were
stationary, unoccupied and llvmpipe-only. They do not add a native-GPU quality,
FPS, occupied ride in every mode, F3+T in every mode, tracing-disabled or
repeated-reliability claim.
