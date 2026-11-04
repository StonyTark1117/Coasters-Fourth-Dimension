# FD402 rendered recovery-editor checkpoint

This bounded client checkpoint renders the actual linked-vehicle recovery editor
after the production attachment path. It is not a claim that a human manually
clicked every control or completed the broader ordinary player workflow.

The fixture boards the real local client player, closes restraints, retains the
Arrow car, and uses the production selection and attach operations. Once the
ordinary bearing is natively linked, it sends the normal `EditorNetwork.Target`
packet for that car. The client must render `RotationEditorScreen` while it is
in recovery mode; this is the screen that supplies the linked-vehicle `Start
tow`, `Brake tow`, and `Detach recovery` controls.

The fresh development-client run passed after 40 static samples with
`recoveryEditorRequired=true` and `recoveryEditorSeen=true`. It also observed
the native link, more than 5.69 blocks of chassis motion, brake, normal detach,
restraints-open, and rider unload; the launcher exited successfully.

Reproduce with a fresh validation world:

```sh
./gradlew --offline --no-daemon runClient \
  -PfdCarrierFixture -PfdCarrierFixtureRecovery \
  -PfdCarrierFixtureRecoveryEditor -PfdCarrierFixtureRecoveryCycle \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion \
  -PfdCarrierValidationRecoveryCycle -PfdCarrierValidationRecoveryEditor \
  -PfdCarrierValidationRider -PfdCarrierValidationRender
```

This scene remains an Arrow-only fixture-assisted workflow. It does not prove
ordinary player world selection, manual editor button input, manual propulsion,
manual seat interaction, all families, or multiplayer operation.
