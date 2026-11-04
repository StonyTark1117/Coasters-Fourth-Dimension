# FD401 FANCY Styles reload and occupied ride

This focused packaged-client run used an isolated Xvfb display (`:112`) and the dedicated game
directory `/tmp/fd401-render-fancy-7ac5e58`. It did not share a display, game directory, save,
options file, log or client process with the concurrent FD401 session. The launch began from
`main` at `7ac5e58`; another task advanced `main` and rebuilt the shared output after launch, so
this record does not claim a retained runtime-JAR hash. Track Styles 1.2.0 was the only optional
integration loaded. The client saved normally, Gradle exited 0 and reported `BUILD SUCCESSFUL`.

The exact launch was:

```text
env DISPLAY=:112 LIBGL_ALWAYS_SOFTWARE=1 JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  PATH=/usr/lib/jvm/java-21-openjdk/bin:$PATH \
  ./gradlew runClient --offline --no-daemon --console=plain \
  -PfdPackaged -PfdClientGameDirectory=/tmp/fd401-render-fancy-7ac5e58 \
  -PfdValidation -PfdRenderValidation -PfdRenderValidationGraphicsMode=FANCY \
  -PfdIntegrations=styles -PfdWorld=FD401StopCycle
```

The log records `graphicsMode=fancy`, expected mode `FANCY`, Flywheel's indirect backend,
software OpenGL through llvmpipe and render distance 12. The Track Styles catalog inspected 55
decorative model entries. The player selected **Arrow double spine** with color `#4A90E2` in the
normal Rotation Tool editor. `style-applied.png` records the result.

The player then used Minecraft's normal `F3+T` resource reload. The second resource-manager load
began at 04:00:38 and the Styles catalog was rebuilt at 04:00:39. `after-reload.png` shows the
styled bank after that reload, while later render diagnostics report nonzero decoration pieces.
The player boarded an Arrow seat normally, closed restraints with the in-ride Rotation Tool and
dispatched from that editor. `occupied-moving.png` records the occupied chassis moving through
the post-reload styled circuit with nonzero velocity. It completed the loop, the powered station
captured it with `capture=true` and near-zero velocity, and `return-ready.png` shows the editor in
`Ready | Restraints closed`. The player opened restraints and Shift-dismounted; the log records
the normal dismount and subsequent diagnostics show no occupants and `riding=false`.

During setup, two Creative-mode focus clicks accidentally removed the generated first drive motor
and boarding station. The motor was restored by cloning the identical generated motor from the
opposite straight. The station was restored at its generated position with its original vehicle
UUID, then its normal rear redstone input performed the final capture. These corrections are
visible in `client.txt`; they did not directly assign a rider, dispatch the car, change its motion,
operate restraints or force capture. The retained world is the cleanly saved post-unload state.

This establishes one **FANCY**, Styles-only, normal-boarded ride after a real resource reload and
visible styled rendering. It is not FAST or FABULOUS coverage, shader-pack coverage, a combined
Styles/Engineered profile, a pristine no-correction operator run, or full FD404 verification.

The Track Styles runtime JAR SHA-256 was
`42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38`.
`evidence.sha256` covers every retained artifact except itself.
