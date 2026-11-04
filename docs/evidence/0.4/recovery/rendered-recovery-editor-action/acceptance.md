# FD402 rendered recovery-editor action checkpoint

This bounded client checkpoint advances the previous rendered-editor evidence:
the recovery editor must use its normal visible `Start tow` button callback,
send the production edit packet, and receive the resulting recovery state back
from the server before native chain power is supplied. It is not a claim that a
human manually completed the broader ordinary player workflow.

The fixture boards the real local client player, retains and attaches the Arrow
car through the production operations, then opens `RotationEditorScreen` for
the linked vehicle. The opt-in fixture waits for the client to observe the
replicated native recovery link and routes its click through the screen's
ordinary mouse dispatcher. The button callback, rather than fixture code,
sends `EditorNetwork.Edit(..., 21, ...)`. The server refuses to power the
native motor until that edit changes the attached car from braked to towing.

The fresh development-client run passed 40 samples with all of
`recoveryEditorSeen`, `recoveryEditorActionSeen`, and
`recoveryEditorActionApplied` true. It logged `editorStartTow=true`, then
observed more than 5.58 blocks of native bearing motion, brake, normal detach,
open restraints, and rider unload. Gradle reported `BUILD SUCCESSFUL`.

Reproduce with a fresh validation world:

```sh
./gradlew --offline --no-daemon runClient \
  -PfdCarrierFixture -PfdCarrierFixtureRecovery \
  -PfdCarrierFixtureRecoveryEditor -PfdCarrierFixtureRecoveryEditorAction \
  -PfdCarrierFixtureRecoveryCycle -PfdCarrierFixtureRider \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion \
  -PfdCarrierValidationRecoveryCycle -PfdCarrierValidationRecoveryEditor \
  -PfdCarrierValidationRecoveryEditorAction -PfdCarrierValidationRider \
  -PfdCarrierValidationRender
```

This remains an Arrow-only fixture-assisted screen click. It does not prove
ordinary player world selection, manual lever operation, manual seat
interaction, all families, or multiplayer operation.
