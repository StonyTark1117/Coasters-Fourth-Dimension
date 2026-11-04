# FD401 exact-candidate coupled Arrow operator replay

On 2026-11-04, an isolated Engineered-only client cold-opened a copy of the
stopped [coupled Arrow checkpoint](../fd401-engineered-coupled-arrow/README.md)
on the frozen `2c5d9a7` candidate. The log names the packaged candidate JAR at
load time. The retained JAR has SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
matching the [final candidate artifact](../fd401-final-candidate-2c5d9a7/README.md).
The run used private Xvfb display `:174`, an isolated game directory and a copied
world. It did not inspect, attach to or control another test session.

The launch used:

```text
./gradlew runClient --offline --no-daemon --console=plain \
  --project-cache-dir /tmp/fd401-candidate-2c5d9a7-V9tBRB/project-cache \
  -PfdPackaged \
  -PfdBuildDirectory=/tmp/fd401-candidate-2c5d9a7-V9tBRB/build-output \
  -PfdClientGameDirectory=/tmp/fd401-coupled-exact-2c5d9a7-4MLQuB/game \
  -PfdValidation -PfdIntegrations=engineered -PfdFeNetwork \
  -PfdWorld=FD401FamilyBare
```

The retained [configuration](coastersengineered-server.toml) has
`requireEnergy=true`. Crafts & Additions supplied only the validation FE
classpath selected by `-PfdFeNetwork`; it is not an advertised integration.

## Persisted consist and clean occupied cycle

Cold load restored the original native chain link between chassis
`8c3c2c3a-f035-47f6-99f9-bd67600a6c20` and
`9e7ce065-efcd-4475-a715-7fdfdde5c574` at distance 6.0. With only that route in
range, availability reported two loaded chassis, four loaded carriers and zero
assignments. Validation boarding selected a saved Arrow seat and closed
restraints, increasing assignments to one.

The copied checkpoint had exhausted its earlier validation energy. The first
controller pulse therefore released the captured train but correctly entered
`Starving=1`; after the disclosed command restored the same 100,000-FE fixture
input used by the predecessor, that pass resumed and returned. This setup
discovery is retained in [client.log.gz](client.log.gz) and
[command_history.txt](command_history.txt), but it is not the accepted pass.

For the accepted cycle, the motor held 100,000 FE before release. Disclosed
station and controller commands released the occupied consist and pulsed its
saved `Launch 8 -> Stop at 8 -> End` program. The linked train traversed the
circuit with `capture=false`; the controller reached
`PassReached=8.002645/8`, `PassShort=0`, `PassStarved=0`, while motor energy fell
to 86,115 FE. The [moving image](moving.png) retains the occupied consist and
controller state.

The two-car train returned stable in station range at about 0.015 blocks/s.
Restoring the station rear input changed it to `capture=true`; the controller
still reported the accepted pass with no starvation. The [vehicle UI](ready-ui.png)
then displayed `arrow_controlled | Ready | Restraints closed`. The player
clicked **Open restraints** in that UI, Shift-dismounted normally and returned
availability to `assignments=0`. [captured.png](captured.png),
[restraints-open.png](restraints-open.png) and [unloaded.png](unloaded.png)
retain the endpoint states.

The player saved through the menu and quit from the title screen. The launcher
exited 0 with `BUILD SUCCESSFUL`; the client log retains server shutdown and
`All dimensions are saved`. [world.tar.gz](world.tar.gz) is the final stopped,
captured and unloaded world.

This exact-candidate replay closes the frozen-artifact review for the coupled
Arrow occupied STOP/capture/UI-unload loop and persisted native link. The
predecessor remains the evidence for normal item placement, chain linking,
controller construction and programming. The disclosed energy command is a
family-test input, not physical FE-generation evidence; the separate wired-FE
checkpoint covers physical delivery. Literal repeated reliability remains
outside this single accepted cycle.
