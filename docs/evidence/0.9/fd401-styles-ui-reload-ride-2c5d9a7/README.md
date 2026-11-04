# FD401 exact-candidate ride after UI style edit and resource reload

On 2026-11-04, a Styles-only packaged FANCY client reopened the saved world from
the [normal banked appearance edit](../fd401-styles-banked-ui-a23ae2d/README.md)
on the frozen `2c5d9a7` candidate. The retained JAR has SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
matching the [final candidate artifact](../fd401-final-candidate-2c5d9a7/README.md).
The client log identifies that isolated JAR path at load time. The run used a
fresh Xvfb server and isolated source export, build output, game directory and
world copy; no existing desktop or client session was reused.

The launch was:

```text
./gradlew runClient --offline --no-daemon --console=plain \
  --project-cache-dir /tmp/fd401-candidate-2c5d9a7-V9tBRB/project-cache \
  -PfdPackaged \
  -PfdBuildDirectory=/tmp/fd401-candidate-2c5d9a7-V9tBRB/build-output \
  -PfdClientGameDirectory=/tmp/fd401-ui-ride-2c5d9a7-pLHVlm/game \
  -PfdValidation -PfdRenderValidation \
  -PfdRenderValidationGraphicsMode=FANCY \
  -PfdIntegrations=styles -PfdWorld=FD401StopCycle
```

The saved 30-degree banked full-turn rail retained the double-spine choice made
through the normal appearance UI in the predecessor run. The player then pressed
F3+T. The log records the real resource-pack reload at 12:11:46, Veil loading 17
shaders, and Track Styles rebuilding its 55-entry catalog at 12:11:47. The
[post-reload view](after-reload-styled.png) and later moving frames show that the
styled route remained rendered. The render environment was FANCY with Flywheel
indirect enabled, and the diagnostics retained nonzero decoration pieces.

The copied world also contains an older Arrow circuit. The validation boarding
helper initially selected that old chassis at x≈−349.5; the player dismounted it,
and a second helper call selected the edited circuit's chassis
`4e207381-e5e7-4007-95ab-280620e85e7d` at x≈−499.5. The helper closed restraints.
An initial validation dispatch while the saved rear station signal was still
powered was immediately recaptured. The actual laps began when a disclosed
`/setblock` command removed that redstone block, exercising the station's normal
falling-edge dispatch path; the same command restored it for capture. These
fixture actions do not replace the predecessor's normal style UI edit or the
separate normal vehicle-UI dispatch evidence.

The first post-reload lap reported about 1.99 blocks/s with `capture=false`.
Client diagnostics kept Dev's UUID in carrier
`e2045c37-b445-4f95-bb82-e71bbcb33169` with `riding=true` and sampled large
controlled rotations including 5.7699, 6.0192 and 3.8012 radians. It returned to
the powered station with `capture=true` and velocity below 0.001 blocks/s. A
second lap used the contraption camera; the retained [banked inversion](banked-inversion.png),
[flat inversion](flat-inversion.png), and [27-frame series](visible-lap-frames.tar.gz)
visibly show the occupied assembly rotating against the UI-selected styled
route. The second lap also returned with `capture=true` and velocity below
0.001 blocks/s.

At the second return the [vehicle UI](captured-ui.png) displayed `Ready |
Restraints closed`. The player clicked **Open restraints** there, dismounted,
and the client then reported both edited-car carriers with `occupants=[]` and
`riding=false`; the [unloaded view](unloaded.png) retains that endpoint. The
world was saved through the menu, the client reached the title screen, and
Gradle exited `BUILD SUCCESSFUL`.

This closes the exact-candidate visible occupied ride after the separate normal
UI appearance edit, its cold saved-world readback, and the occupied F3+T ride
boundary for this Styles-only FANCY configuration. It does not add a continuous
between-tick clearance sweep, equipped/cosmetic rider clearance, injected draw
failure, tracing-disabled font claim, or repeated-reliability claim.

