# FD2001 different-route and optional fixed propulsion handoffs

Verified 2026-11-26 on primary `main`, baseline `f85f4a277c7c7542a4f07a158f18f580beb41187` plus the shared uncommitted 1.5 implementation. This closes the bounded native different-dock Powered and positive Engineered/Magnetized handoff checks; it does not claim occupied input, FE consumption, a whole optional-profile campaign, or FD2008.

All raw runs are preserved in `/mnt/NVME4/fourthdimension-runs/fd15-moving-families-20261122`. Java 21.0.13.1, Gradle 9.2.1; isolated persistent build/project cache; `-PfdIntegrations=magnetized,engineered`. No graphical client or display was used. Exact source/log digests are in `sha256.txt`.

| Evidence | Actual coverage |
| --- | --- |
| `handoffs-12.log`, `MovingOptionalHandoffGameTests.magnetizedFixedToRetainedToFixed` PASS | Original Wing on authenticated locked native source bridge, original station Magnetized authority held, normal moving-controller Capture/Move, physically measured 4-block translation, destination lock, Release, explicit stopped receiving-station transfer, normal station dispatch and actual powered fixed-track departure with original identities/restraints. Chassis reaches z=-407.3 from source z=-419.5 and the complete vehicle clears service. Competing Engineered provider remains denied. The other two cases in this process failed and are superseded below. |
| `handoffs-15.log`, 3/3 PASS, BUILD SUCCESSFUL | `MovingPoweredHandoffGameTests.differentCircuitKeepsAuthorityAndActuallyDeparts`: actual Powered car/carrier physically travel 16 blocks between distinct closed circuits; destination governors/Chain/owned sections are inspected and adopted by the original authority; explicit normal governor release/dispatch drives onto fixed destination track and whole car/carrier clear service without fault. Chassis z=394.7 by tick645. `MovingEngineeredHandoffGameTests.engineeredFixedToRetainedToFixed`: original Arrow physically travels 4 blocks via normal production controls; held source Engineered station authority transfers explicitly at rest to fixed receiving station, then normal dispatch and real native motor propulsion clear the whole original assembly onto fixed track (z=-607.9 from -619.5), with competing Magnetized denied. Existing `ordinaryBearingUsesMotorWithoutRotaryMembership` is reused as the single affected fixed-motor regression: distance10.564453125, one native body/mass1, no FD vehicle, native STOP speed0.000248128. |
| `chain-seam-regression-16.log`, 1/1 PASS | Existing FD812 real native callback fence: exactly one Magnetized impulse; forward/reverse stationary launch, rollback/brake, held/open-restraint denial, power-loss latch, no automatic restart, competing providers denied. This is the affected callback-order regression, not a rerun of the accepted physical handoff. |

## Repair and diagnosis

Powered candidate validation uses ideal socket geometry. Adoption now commits the actual authenticated native bridge fingerprint, rejecting a missing or different inspected bridge. The same original governor still verifies the actual complete destination circuit and owns every real drive. Initial service dispatch requires a powered Chain span. Subsequent service departure refreshes that same validated governor lease while the chassis coasts onto an unpowered adjoining fixed curve and its trailing carrier clears the bed; it does not add force, grant another owner, or reduce whole-body clearance. Service hit selection uses the closest actual bridge/adjoining fixed edge. Partial service fault retention now preserves the original repair reason rather than replacing route errors with a misleading missing-body message.

The Engineered addon injects its real motor callback at the native Chain method HEAD. Fourth Dimension previously cancelled native Chain for Engineered-owned vehicles before that callback ran. `ChainDriveGuardMixin` priority1100 establishes the required transformed order. `ChainDrive-transformed15.javap.txt` records exactly one Engineered callback at bytecode9, the already-fenced Magnetized callback at21, Fourth Dimension guard at46, then cancellation before native Chain. The independent existing `EngineeredMotorGuardMixin` still enforces held/restraint/provider rules. No synthetic motor force or duplicate callback was introduced. The unsuccessful priority900 experiment is retained in `ChainDrive-transformed14.javap.txt` and `handoffs-14.log`.

Fixture corrections are disclosed: Creative world construction and fake operator; single-car whole consists; initial native velocity braking immediately before Powered/Engineered admission; temporary source bridge metadata only to satisfy existing vehicle creation APIs, removed immediately afterward. Powered motion uses the direct native adapter with a smooth physical target, not player-operated production controls; its harness now acquires the same real destination receiving claims required by production. Engineered and Magnetized use actual production Capture/Move/Release controls. Engineered's fixed motor has the recorded default `requireEnergy=false`; FE draw is not claimed. Magnetized's fixed destination power is installed after the sweep, beside the socket rather than obstructing the departure track; no propulsion equipment is placed on the moving bed. Earlier fixture failures/logs are preserved. Prior fixed-family eligibility and accepted coverage remain unchanged.

## Reproduction and cleanup

From the primary checkout with Java21 selected:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew --no-daemon --max-workers=2 \
  --project-cache-dir /mnt/NVME4/fourthdimension-runs/fd15-moving-families-20261122/project-cache \
  -PfdBuildDirectory=/mnt/NVME4/fourthdimension-runs/fd15-moving-families-20261122/build \
  -PfdIntegrations=magnetized,engineered \
  -PfdGameTestNamespaces=fourthdimension_fd2001_powered_handoff,fourthdimension_fd2001_engineered_handoff \
  runGameTestServer
```

The Magnetized handoff namespace is `fourthdimension_fd2001_optional_handoff`; the shared force-fence regression is `fourthdimension_fd812`. Matching empty structures are included with the new holders. To inspect transformation, use `JAVA_TOOL_OPTIONS='-Dmixin.debug.export=true -Dmixin.debug.export.filter=dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive'`; this was used only for the ordering runs.

The successful handoff world is preserved as `world-handoffs-15`. Each cited final process saved all dimensions and exited; no owned live runtime remains. Source/evidence changes are uncommitted for selective coordinator integration.

After these passes, the PAX owner diagnosed its distinct station-return criterion: `adoptFixed` cleared PAX carrier capture flags before the original winch's CAPTURING phase could satisfy station readiness. The current method preserves PAX capture/latched flags only; all three families proved here retain their tested behavior. PAX verification belongs to that owner's targeted final run. The pre-PAX-exception `MovingServiceRuntime.java` is preserved in `source-handoffs-15` and its digest is retained here, so this record does not imply these runs executed the later exception.
