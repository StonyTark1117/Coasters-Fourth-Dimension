# FD401 Track Styles render modes and Solas observation

These three packaged-client runs exercised the saved styled Arrow bank from the
[FANCY occupied-reload ride](../fd401-fancy-styles-reload-ride/README.md) in dedicated game
directories and on separate Xvfb displays. FAST used `:113`, FABULOUS used `:114`, and the
Iris/Solas run used `:115`; none shared a display, game directory, options file, save copy, log or
client process with another task.

All three runs loaded the retained packaged Fourth Dimension JAR in this directory. Gradle
reported `compileJava UP-TO-DATE` and `jar UP-TO-DATE` before every launch, and the file was not
rewritten during the runs. Its SHA-256 is
`de64a6833fd0a4e5f7bdbc36ad40b89ff396d48941c7e6ae0ae2e52ca6ca3a70`.
The working branch was `main` at `93c3547`, but another task had uncommitted validation/demo
source changes; the retained JAR, rather than the mutable working tree, is the executable identity
for this record.

## FAST and FABULOUS

The commands differed only by isolated directory, display and expected graphics mode:

```text
env DISPLAY=:113 LIBGL_ALWAYS_SOFTWARE=1 JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  PATH=/usr/lib/jvm/java-21-openjdk/bin:$PATH \
  ./gradlew runClient --offline --no-daemon --console=plain \
  -PfdPackaged -PfdClientGameDirectory=/tmp/fd401-render-fast-93c3547-2 \
  -PfdValidation -PfdRenderValidation -PfdRenderValidationGraphicsMode=FAST \
  -PfdIntegrations=styles -PfdWorld=FD401StopCycle

env DISPLAY=:114 LIBGL_ALWAYS_SOFTWARE=1 JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  PATH=/usr/lib/jvm/java-21-openjdk/bin:$PATH \
  ./gradlew runClient --offline --no-daemon --console=plain \
  -PfdPackaged -PfdClientGameDirectory=/tmp/fd401-render-fabulous-93c3547 \
  -PfdValidation -PfdRenderValidation -PfdRenderValidationGraphicsMode=FABULOUS \
  -PfdIntegrations=styles -PfdWorld=FD401StopCycle
```

Both strict mode guards passed on Mesa llvmpipe OpenGL 4.6 at render distance 12. FAST recorded
`flywheel:indirect`, enabled, and reached 638 geometry stages with 26,158 decoration pieces.
FABULOUS also recorded `flywheel:indirect`, enabled, and reached 610 geometry stages with 25,010
decoration pieces. Minecraft emitted its normal warning that some Create visuals are unavailable
under FABULOUS; the Fourth Dimension styled track remained visible in `fabulous.png`. Both clients
saved all dimensions, stopped normally, and Gradle exited 0 with `BUILD SUCCESSFUL`.

## Pinned Iris/Solas setup

The third game directory added the previously audited Iris/Sodium pair and selected Solas through
`config/iris.properties`:

| Component | SHA-256 |
| --- | --- |
| Iris 1.8.14-beta.1 development-fixed JAR | `49f783b85cda7fc75e05cf993b80db70bdb5dae55d5426d0536bbe831a44da7d` |
| Sodium 0.8.13 for NeoForge 1.21.1 | `575d187b15328d7bddd8a9b1bee64333c22c14735f5780ec49eac0cd5503c08d` |
| Solas Shader V3.7b | `b7dfe4c7353d677a92cc4c86b87bc0633f1014305d927ca6a482c303427bf825` |
| Track Styles 1.2.0 | `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38` |

The client used FANCY mode with its strict expected-mode guard. Iris logged the exact Solas pack
and created an Overworld pipeline. `solas.png` visibly records the shader-lit styled track on
llvmpipe. The renderer reached 109 geometry stages and 4,469 decoration pieces. As expected for
Flywheel 1.0.6 with an active shader pack, Flywheel fell back from `indirect` to `off`; the
environment marker therefore records `flywheelEnabled=false`. This is direct-render/shader
evidence, not accelerated Flywheel evidence. The client saved normally and Gradle exited 0 with
`BUILD SUCCESSFUL`.

Together with the linked FANCY occupied-reload run, this covers the three declared Minecraft
graphics modes and the pinned Solas setup. These two mode-only replays were stationary and
unoccupied, did not repeat F3+T, and do not establish native-GPU image quality, FPS, whole-frame
performance, tracing-disabled operation, or an occupied ride separately in every mode.

`evidence.sha256` covers every retained artifact except itself.
