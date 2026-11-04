# FD401 exact-candidate Legacy operator replay

On 2026-11-04, an isolated Engineered-only client cold-opened the standalone
stopped [Legacy checkpoint](../fd401-engineered-family-operator/README.md) on
the frozen `2c5d9a7` candidate. The log names the packaged candidate JAR at load
time. The retained JAR has SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
matching the [final candidate artifact](../fd401-final-candidate-2c5d9a7/README.md).
The run used private Xvfb display `:175`, an isolated game directory and a copied
world. It did not inspect, attach to or control another test session.

The launch used:

```text
./gradlew runClient --offline --no-daemon --console=plain \
  --project-cache-dir /tmp/fd401-candidate-2c5d9a7-V9tBRB/project-cache \
  -PfdPackaged \
  -PfdBuildDirectory=/tmp/fd401-candidate-2c5d9a7-V9tBRB/build-output \
  -PfdClientGameDirectory=/tmp/fd401-legacy-exact-2c5d9a7-ZsVIxo/game \
  -PfdValidation -PfdIntegrations=engineered -PfdFeNetwork \
  -PfdWorld=FD401FamilyBare
```

The retained [configuration](coastersengineered-server.toml) has
`requireEnergy=true`. Crafts & Additions supplied only the validation FE
classpath selected by `-PfdFeNetwork`; it is not an advertised integration.

## Standalone occupied cycle

The cold-opened Legacy route reported one loaded chassis, two loaded carriers
and zero assignments. Its saved car was captured at the relocated station,
whose rear redstone input was correctly present at `(194,142,30)`. The motor
retained 92,021 FE and its controller retained the programmed
`Launch 8 -> Stop at 8 -> End` sequence. Validation boarding selected a real
Legacy seat and closed restraints.

Before release, a disclosed command restored the same 100,000-FE family-test
input used by the predecessor. Removing the station rear signal and pulsing the
saved controller released the occupied car. It traversed the circuit with
`capture=false`; the controller reached `PassReached=7.940176/8`,
`PassShort=0`, `PassStarved=0`, and motor energy fell to 93,578 FE. The
[moving image](moving.png) retains the occupied state and controller data.

The car returned stable in station range at about 0.000047 blocks/s. Restoring
the relocated rear signal changed it to `capture=true`; the controller retained
the accepted pass with no starvation and the motor retained 92,334 FE. The
[vehicle UI](ready-ui.png) displayed `zacspin_legacy | Ready | Restraints
closed`. The player clicked **Open restraints** in that UI, Shift-dismounted
normally and returned availability to `assignments=0`.
[captured.png](captured.png), [restraints-open.png](restraints-open.png) and
[unloaded.png](unloaded.png) retain the endpoint states.

The player saved through the menu and quit from the title screen. The launcher
exited 0 with `BUILD SUCCESSFUL`; [client.log.gz](client.log.gz) retains the
exact-JAR load, operator transcript, server shutdown and `All dimensions are
saved`. [world.tar.gz](world.tar.gz) is the final stopped, captured and unloaded
world.

This standalone replay followed the earlier cumulative-world attempt in the
[exact family bundle](../fd401-engineered-family-exact-2c5d9a7/README.md),
where Legacy launched from less energy but stalled away from station. That
contrary result remains retained. The bounded correction was to use the
standalone stopped checkpoint and initialize the documented family-test energy
before release; no production code changed.

This record closes the exact-candidate Legacy occupied STOP/capture/UI-unload
loop. The predecessor remains the evidence for normal item placement,
controller construction/programming and ordinary seat selection. The disclosed
energy command is not physical FE-generation evidence; the separate wired-FE
checkpoint covers physical delivery. Literal repeated reliability remains
outside this single accepted cycle.
