# FD402 occupied banked recovery render/motion checkpoint

This is a bounded development-client checkpoint for one occupied Arrow recovery
car. It adds physical curved/banked traversal to the earlier flat Arrow client
checkpoint; it does not close FD402 or the broader 0.4 acceptance gate.

## Run

On 2026-11-03, a fresh local singleplayer world, `FD402RecoveryBank2`, ran the
opt-in fixture with Java 21:

```sh
./gradlew --offline --no-daemon runClient --console=plain \
  -PfdCarrierFixture -PfdCarrierFixtureRecoveryBank \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion \
  -PfdCarrierValidationRecoveryBank -PfdCarrierValidationRider \
  -PfdCarrierValidationRender -PfdCarrierValidationSamples=40 \
  -PfdCarrierValidationTimeout=8000
```

The fixture admitted the real local player, created and retained a native
ordinary bearing, mounted the rider, and used the guarded production
select/attach/start paths to begin native-chain towing. After 40 static samples,
the client validator passed at `waitedTicks=3352`. It observed all four expected
compiled client bodies, all three fourth-dimension bodies in Sable's
section-layer renderer, and the ordinary bearing in Coasters' native cart draw
path. The bearing moved `58.60942914976487` blocks and the Arrow chassis moved
`60.24208180549682` blocks.

The bank assertion derives from each native body's world-up vector after that
body has moved at least one block; it is not the Arrow carrier's controlled-spin
angle. Maximum observed chassis bank was `0.5000804782268888` radians (28.65°)
and maximum ordinary-bearing bank was `0.5235987367764136` radians (30.00°).
The log then records a normal integrated-server stop.

## Provenance

[`bank2-events.txt`](bank2-events.txt) retains the relevant client-log excerpt.
The preserved full local log has SHA-256
`b50679154b28e9664e9006e79bd66c5aeeda80f796d483e14d1ea2fd3d202731`.
The run used committed source `ec980bc` (`test: validate banked recovery
traversal [skip ci]`). Preserved compiled classes from that run hash to
`4be781638570bc1e226a25fbfd3c9baa35eda42aca1b86d8d22155897312f3d1`
(`CarrierTransformValidation`),
`b5aa0f9097ab9169646ef885d9ba901572906a4ee1c897e18f0fef2d642de9cc`
(`CarrierValidationFixture`), and
`54dc2a8e87b57b3ff2fa1dc214311aeaf5c75b9a7620840cfc8d6c16cbfd8edc`
(`BankedRecoveryExamples`). This is local development-client evidence, not
packaged or CI evidence.

## Scope and limits

This proves one occupied Arrow recovery group reaches the client render paths,
travels through the native bend, and reaches the sustained bank under production
recovery operations. It does not prove manual GUI/editor interaction, player/camera mesh
clearance, rider/rack clearance, full braking/cut/detach/unload behavior,
cold-process recovery lifecycle, other vehicle families, broad coupler strength,
or Engineered/FE operation. Those FD402 items remain open.
