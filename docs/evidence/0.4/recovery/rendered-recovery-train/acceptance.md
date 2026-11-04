# FD402 rendered occupied recovery-train checkpoint

This bounded development-client checkpoint extends the occupied Arrow recovery
scene to the actual two-car original train. It is not a claim of all-family,
multiplayer, manual selection, or continuous mesh-clearance acceptance.

The opt-in fixture creates `BankedRecoveryExamples.createTrain(..., ARROW)`,
uses its real native two-car assembly, boards the local client player into the
recovery-end car, and attaches the certified ordinary bearing through the
production recovery operation. The client gate requires both original chassis
to be in the same native train graph as that bearing; only the recovery-end
car has a direct bearing edge, so the other car is correctly checked through
native train membership rather than an invented direct link.

The fixture holds the linked group until client replication is observed, then
uses the normal native chain, brake, disconnect, restraint opening, and unload
cycle. The fresh isolated run passed 40 static samples with
`recoveryTrainSeen=true` and `recoveryTrainMotionSeen=true`. It rendered six
fourth-dimension bodies and three Coasters native carts, and measured both
original Arrow chassis moving about 5.46 blocks before normal release. Gradle
reported `BUILD SUCCESSFUL`.

Reproduce with a fresh validation world:

```sh
./gradlew --offline --no-daemon runClient \
  -PfdCarrierFixture -PfdCarrierFixtureRecovery \
  -PfdCarrierFixtureRecoveryBank -PfdCarrierFixtureRecoveryTrain \
  -PfdCarrierFixtureRecoveryCycle -PfdCarrierFixtureRider \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion \
  -PfdCarrierValidationRecoveryCycle -PfdCarrierValidationRecoveryTrain \
  -PfdCarrierValidationRider -PfdCarrierValidationRender
```

This is an Arrow-only fixture-assisted client run. It does not prove PAX
rendered recovery trains, manual world interaction, a rendered editor action,
or a complete bend/bank traversal before the normal release.
