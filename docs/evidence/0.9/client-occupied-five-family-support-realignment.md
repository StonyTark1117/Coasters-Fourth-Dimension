# Packaged occupied five-family physical support realignment

**Scope:** FD902 packaged-client recovery for an occupied original 4D car after
its live matching Coasters connection disappears during normal movement. This
extends the native server checkpoint with the actual distributed addon archive,
a mounted local player, replicated fault/repair state, and a client-side bearing
position check after native reattachment.

## Scenario

Each fresh world contains one selected family. The fixture boards the local
player in a real carrier seat, dispatches the vehicle, and waits for physical
chassis movement before removing its exact bound Coasters curve. It verifies
that the rider remains seated through the retained fail-closed state, restores
the authored curve, runs **Repair matching track**, and then runs **Realign
matching track**. The client requires all of the following before passing:

- observed movement before support loss;
- mounted rider and the selected carrier binding still replicated;
- replicated stopped, restrained, captured support-loss and repair states;
- cleared repair fault and cleared `alignmentRequired`; and
- the native chassis bearing within 0.2 blocks of the repaired rail's expected
  physical bearing position.

PAX uses the real four-chassis train. A support loss captures its compatible
train as one safety unit and resets the native velocities of every train chassis
and carrier before physical realignment. This prevents coupled chassis from
feeding residual motion into the snap and breaking their links.

## Artifact and command

The archive under test was built from the current checkout immediately before
these runs:

```text
coasters-fourth-dimension-0.9.0-dev.27.jar
SHA-256 79cefc00d201e746fb3d2765e623637e97c1ed9b00bc6172e22fe8a89dd7b7dd
```

For each family, the packaged client was launched with Java 21, `-PfdPackaged`,
a fresh saved world, the rider-only fixture, moving support loss, repair,
realignment, and validation exit. The family substitutions were `ARROW`,
`ZACSPIN`, `FREE_SPIN`, `ZACSPIN_REVISED`, and `PAX`:

```bash
xvfb-run -a timeout 420s env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FD<Family>OccupiedSupportRealignmentFinal \
  -PfdCarrierFixture -PfdCarrierFixtureRider -PfdCarrierFixtureRiderOnly \
  -PfdCarrierFixtureRiderFamily=<FAMILY> \
  -PfdCarrierFixtureSupportLoss -PfdCarrierFixtureSupportLossMotion \
  -PfdCarrierFixtureSupportRepair -PfdCarrierFixtureSupportRealignment \
  -PfdCarrierValidation -PfdCarrierValidationRider \
  -PfdCarrierValidationSupportLoss -PfdCarrierValidationSupportLossMotion \
  -PfdCarrierValidationSupportRepair -PfdCarrierValidationSupportRealignment \
  -PfdCarrierValidationSupportLossFamily=<FAMILY> \
  -PfdCarrierValidationExit -PfdCarrierValidationSamples=40 \
  -PfdCarrierValidationFamilies=1 -PfdCarrierValidationTimeout=4000
```

## Results

| Family | Result | Raw packaged-client log |
| --- | --- | --- |
| Arrow | Passed; rider retained and native bearing realigned after 2.15 blocks of motion. | [log](client-occupied-arrow-support-realignment.txt) |
| Legacy ZacSpin | Passed; rider retained and native bearing realigned after 2.10 blocks of motion. | [log](client-occupied-legacy-support-realignment.txt) |
| Free Spin | Passed; rider retained and native bearing realigned after 2.15 blocks of motion. | [log](client-occupied-free-spin-support-realignment.txt) |
| Revised ZacSpin | Passed; rider retained and native bearing realigned after 2.18 blocks of motion. | [log](client-occupied-revised-support-realignment.txt) |
| PAX | Passed; all four chassis moved 2.60–2.62 blocks, the occupied rider remained mounted, and the train realigned. | [log](client-occupied-pax-support-realignment.txt) |

Every run logged `supportLossObserved=true`, `supportRepairObserved=true`,
`supportRealignmentObserved=true`, and `BUILD SUCCESSFUL`.

## Remaining work

This is a single-player packaged-client recovery boundary. Simultaneous repair
by multiple clients and post-repair unload/reload or subsequent PAX powered-lift
dispatch remain FD902 lifecycle work.
