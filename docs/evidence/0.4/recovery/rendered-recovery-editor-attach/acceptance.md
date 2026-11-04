# FD402 rendered recovery-editor attachment checkpoint

This bounded development-client checkpoint covers the first actual recovery
editor operation: attaching the selected ordinary bearing. It does not claim a
human world-clicked the selection tool, manually operated the lever, or
completed the all-family/multiplayer operator matrix.

The fixture boards the real local client player, closes restraints and retains
the Arrow car, then calls the production selection guard for the bare ordinary
bearing. It deliberately does not attach server-side. Instead it opens the
ordinary vehicle editor, whose visible `Attach recovery` control is routed
through the screen's normal mouse dispatcher. That button's regular callback
sends `EditorNetwork.Edit(..., 20, ...)`; the fixture waits for the server to
form the retained native link before it can start chain power.

The fresh isolated client run passed 40 samples with
`recoveryEditorAttachRendered=true`, `recoveryEditorAttachSeen=true`, and
`recoveryEditorAttachApplied=true`. It then observed the native link, more
than 5.57 blocks of ordinary-bearing motion, brake, normal detach, open
restraints, and rider unload. Gradle reported `BUILD SUCCESSFUL`.

Reproduce with a fresh validation world:

```sh
./gradlew --offline --no-daemon runClient \
  -PfdCarrierFixture -PfdCarrierFixtureRecovery \
  -PfdCarrierFixtureRecoveryEditorAttach -PfdCarrierFixtureRecoveryCycle \
  -PfdCarrierFixtureRider -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion \
  -PfdCarrierValidationRecoveryCycle \
  -PfdCarrierValidationRecoveryEditorAttach \
  -PfdCarrierValidationRider -PfdCarrierValidationRender
```

This remains an Arrow-only fixture-assisted editor click. It does not replace
real player tool selection, manual propulsion/lever input, the later `Start
tow` editor action, other families, or multiplayer operation.
