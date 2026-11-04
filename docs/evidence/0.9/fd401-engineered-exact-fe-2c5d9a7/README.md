# FD401 exact-candidate Engineered operator and wired-FE replay

On 2026-11-04, two Engineered-only packaged clients cold-opened the stopped
world from the [wired water-wheel checkpoint](../fd401-engineered-wired-waterwheel/README.md)
on the frozen `2c5d9a7` candidate. Both logs identify the isolated candidate JAR
at load time. The retained JAR has SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
matching the [final candidate artifact](../fd401-final-candidate-2c5d9a7/README.md).
The run used a fresh Xvfb server plus isolated source export, build output, game
directory and world copy. It did not reuse or control another client session.

Both launches used:

```text
./gradlew runClient --offline --no-daemon --console=plain \
  --project-cache-dir /tmp/fd401-candidate-2c5d9a7-V9tBRB/project-cache \
  -PfdPackaged \
  -PfdBuildDirectory=/tmp/fd401-candidate-2c5d9a7-V9tBRB/build-output \
  -PfdClientGameDirectory=/tmp/fd401-engineered-exact-uiLY4z/game \
  -PfdValidation -PfdIntegrations=engineered -PfdFeNetwork \
  -PfdWorld=FD401Stop12
```

Engineered 1.1.1 was the advertised optional integration. Create Crafts &
Additions 1.6.0 supplied only the validation FE network selected by
`-PfdFeNetwork`; this does not add an advertised integration.

## Exact-candidate operator replay

The first cold open retained the controller-to-anchor link and its programmed
`Launch 20 -> Stop at 12 -> End` sequence. The motor anchor held 84,130 FE, and
the source and target connectors retained reciprocal wire offsets `(7,0,4)` and
`(-7,0,-4)`. Validation boarding selected the saved Arrow chassis and closed its
restraints. Disclosed `/setblock` commands removed the rear station signal and
pulsed the controller. The occupied car moved around the saved circuit, reached
`PassReached=19.397941/20` with `PassStarved=0`, advanced to STOP and returned
stable in station range at about 0.014628 blocks/s. Restoring the rear station
signal captured it below 0.001 blocks/s. The [vehicle screen](captured-ui-first.png)
displayed `Ready | Restraints closed`; the player clicked **Open restraints**,
dismounted, saved through the menu and exited normally. [operator-client.log.gz](operator-client.log.gz)
retains the exact-JAR load, controller/anchor/wire queries, boarding transitions,
save and shutdown.

The generated Engineered server configuration defaulted to
`requireEnergy=false` for that first session. It therefore replays the operator
loop but cannot be used as an FE-consumption claim. The value was changed only
in this isolated game directory before the second cold launch. The retained
[configuration](coastersengineered-server.toml) records `requireEnergy=true`.

## Required-energy replay through the saved wire

At the second cold open, before boarding, the saved wire had raised the anchor
from 84,130 to 90,026 FE without a command filling it. The source connector still
reported its reciprocal node. Validation boarding again selected the saved Arrow
and closed restraints; the same disclosed station/controller redstone commands
started the occupied run. While the chassis was moving, anchor energy fell from
90,026 to 63,482. The controller reported `PassReached=19.397939/20`,
`PassShort=1`, `PassStarved=0`, then advanced to STOP.

The occupied car returned stable in station range at about 0.014637 blocks/s.
While stopped, continuing transfer through the saved wire replenished the anchor
from 63,482 to 72,396 FE. Restoring station redstone captured it below 0.001
blocks/s. [fe-captured-ui.png](fe-captured-ui.png) again shows `Ready |
Restraints closed`; the player opened restraints in that UI, dismounted, saved
and exited normally. The [moving](fe-moving.png), [captured](fe-captured-ui.png)
and [unloaded](fe-unloaded.png) images retain the occupied-drive and endpoint
states. [fe-client.log.gz](fe-client.log.gz) retains the exact-JAR load, energy,
controller and boarding queries, save and shutdown. [world.tar.gz](world.tar.gz)
is the final stopped and unloaded save.

This exact-candidate replay closes the frozen-artifact review of the saved Arrow
operator loop and its persisted physical wire: cold transfer, occupied energy
consumption, no starvation, programmed STOP, station capture, UI release,
unload and clean save/exit. The earlier checkpoint remains the evidence for
normal copper-spool construction and source interruption/restoration; this run
did not rebuild or interrupt the network. Validation boarding and redstone
commands do not replace the predecessor's normal seat, restraint, wire or
controller actions. Literal 20.0 blocks/s was not reached, and this record does
not make Legacy, Free Spin, Revised, coupled Arrow or PAX exact-candidate claims.
