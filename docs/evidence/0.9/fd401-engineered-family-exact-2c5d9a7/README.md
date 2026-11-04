# FD401 exact-candidate Engineered family replay

On 2026-11-04, two isolated Engineered-only clients cold-opened a copy of the
stopped family-operator world on the frozen `2c5d9a7` candidate. Both logs name
the packaged candidate JAR at load time. The retained JAR has SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
matching the [final candidate artifact](../fd401-final-candidate-2c5d9a7/README.md).
The clients used a private Xvfb display (`:173`), isolated game directory and
world copy. They did not inspect, attach to or control another test session.

Both launches used the isolated candidate export and this shape:

```text
./gradlew runClient --offline --no-daemon --console=plain \
  --project-cache-dir /tmp/fd401-candidate-2c5d9a7-V9tBRB/project-cache \
  -PfdPackaged \
  -PfdBuildDirectory=/tmp/fd401-candidate-2c5d9a7-V9tBRB/build-output \
  -PfdClientGameDirectory=/tmp/fd401-family-exact-2c5d9a7-825772/game \
  -PfdValidation -PfdIntegrations=engineered -PfdFeNetwork \
  -PfdWorld=FD401Family
```

The retained [configuration](coastersengineered-server.toml) has
`requireEnergy=true`. Crafts & Additions supplied the validation-only FE
network selected by `-PfdFeNetwork`; it is not an advertised integration.

## Free Spin

Validation boarding selected the saved S&S-inspired Free Spin carrier and
closed its restraints. Disclosed `/setblock` commands removed the rear station
signal and pulsed the linked controller. The occupied car traversed the saved
circuit with `capture=false`; the controller reached
`PassReached=7.943883/8`, `PassShort=0`, `PassStarved=0` and advanced to STOP.
The car returned stable in station range, after which restoring the rear signal
changed it to `capture=true`. The vehicle screen displayed `Ready | Restraints
closed`; the player opened restraints in that UI and normally dismounted.
Availability then reported `assignments=0`. The [moving](free-spin-moving.png),
[captured](free-spin-captured.png) and [ready](free-spin-ready.png) images retain
the bounded states, and [free-spin-client.log.gz](free-spin-client.log.gz)
retains the exact-JAR load, controller data, boarding transitions, save and
server shutdown.

## Revised

The second cold open repeated the same bounded operator replay on the saved
Revised Free Rotor. Its occupied car reached
`PassReached=7.728904/8`, `PassShort=0`, `PassStarved=0`, returned stable at the
station and changed from `capture=false` to `capture=true` when the rear signal
was restored. The normal vehicle UI again displayed `Ready | Restraints closed`;
the player opened restraints there, Shift-dismounted and returned availability
to `assignments=0`. The [moving](revised-moving.png),
[captured](revised-captured-exact.png) and [ready](revised-ready-exact.png)
images retain those states. [revised-legacy-client.log.gz](revised-legacy-client.log.gz)
retains the exact-JAR load and the complete Revised transcript.

## Selection orchestration and Legacy boundary

The validation boarding helper selects the first ready loaded carrier rather
than a named family. To move from an earlier ready family to the next saved
route without changing production code, the earlier family was boarded, its
station rear signal was temporarily removed, and the validation player was
killed and respawned. This disclosed selector orchestration is retained in
[command_history.txt](command_history.txt). It is not part of either successful
ride and does not replace the predecessor record's normal car placement,
controller programming, boarding or unloading.

Legacy was also attempted, but this record deliberately does **not** claim an
exact-candidate Legacy pass. After correcting the saved world's relocated rear
station signal from `z=33` to `z=30`, the occupied Legacy car launched with
`capture=false` and the controller reached `PassReached=7.835203/8`,
`PassShort=0`, `PassStarved=0`. It then lost almost all speed around
`(225.1, 143.1, 12.52)`, away from station range, and never returned or
recaptured before the clean server shutdown. [legacy-moving.png](legacy-moving.png)
and the second log preserve that contrary result. The session ended with the
rider still assigned and therefore is not retained as a stopped/unloaded world.

This replay closes candidate identity and the occupied STOP/capture/UI-unload
loop for Free Spin and Revised. The earlier family-operator checkpoint remains
the evidence for normal item placement and controller construction. A
[standalone exact-candidate replay](../fd401-engineered-legacy-exact-2c5d9a7/README.md)
closes the retained Legacy boundary, and the
[coupled two-car Arrow](../fd401-engineered-coupled-arrow-exact-2c5d9a7/README.md)
loop; the failed cumulative-world Legacy attempt above remains part of this
bundle's scope and is not relabeled as a pass.
