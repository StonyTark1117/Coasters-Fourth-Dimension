# Engineered motor span and FE block-entity reload

This FD401 checkpoint uses the separately installed, pinned Coasters: Engineered
**1.1.1** JAR (SHA-256
`eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d`).
It advances the [earlier native FE test](fe-acceptance.md) across one block-entity
serialization boundary. No optional implementation or asset is bundled.

## Method

`EngineeredEnergyGameTests.nativeMotorSpanAndEnergySurviveBlockEntityReload`
creates a real original Arrow rail and an installed Engineered motor span and
controller. Its native Arrow chassis boards a pig passenger, closes restraints,
and enters maintenance retention. With Engineered's energy requirement enabled
for the isolated batch, the anchor's public NeoForge FE capability receives
40,000 FE. The test saves the actual anchor and controller block entities with
full metadata, removes both live instances, deserializes fresh instances through
Minecraft's `BlockEntity.loadStatic`, and installs them at the same positions.

After ticking the restored instances, it requires the FE capability to expose
the same 40,000 FE, the controller to be linked to the original anchor with its
LAUNCH 10 step, and the occupied car to remain held with no longitudinal drift.
It checks the loaded rotary assembly mass, releases retention, dispatches the
native car, requires powered travel and FE consumption, commands STOP, captures
the carriers, opens restraints, unloads the passenger, and removes the fixture.

## Run and result

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer \
  --no-daemon --console=plain -PfdIntegrations=engineered -PfdPerformanceTargets
```

The [compressed full log](engineered-fe-reload-gametest.log.gz) is SHA-256
`358297f419bc850f8daa647a93a9c513ba1c33715e05e6a23e5f23aeefc746f9`;
its uncompressed SHA-256 is
`1ad012219b3f5112ec9522bdd2cfbbe9b47f6a55ef40554e62189d6193b3f356`.
It records:

```text
FD_ENGINEERED_FE isolatedBatch requireEnergy=true previous=false
FD_ENGINEERED_FE_RELOAD linked=true stored=40000 retained=true rider=true
FD_ENGINEERED_FE_RELOAD launched distance=19.48138427734375 remaining=35449
FD_ENGINEERED_FE_RELOAD stopped=true recaptured=true unloaded=true
FD_ENGINEERED_FE restored requireEnergy=false
All 45 required tests passed :)
BUILD SUCCESSFUL in 5m 23s
```

The separate simultaneous standalone target-gated run on the same host affected
timing measurements, so this run is used for the Engineered reload and functional
regression result, not an independent FD904 performance acceptance claim.

## Limits

The block entities are round-tripped inside one running GameTest server. This
does not establish a cold dedicated-server restart, a complete chunk unload,
an external FE generator/cable network, controller GUI interactions by a real
player, every motor layout, or all supported mod combinations. Those remain
separate FD401 acceptance work.
