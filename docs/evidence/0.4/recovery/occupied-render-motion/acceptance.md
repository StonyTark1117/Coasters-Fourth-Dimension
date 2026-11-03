# FD402 occupied linked recovery render/motion checkpoint

This is a bounded development-client checkpoint for one Arrow recovery car. It
does not close FD402 or the broader 0.4 acceptance gate.

## Run

On 2026-11-03, a new local singleplayer world, `FD402RecoveryRender12`, ran the
opt-in fixture with Java 21:

```sh
./gradlew --offline --no-daemon runClient --console=plain \
  -PfdCarrierFixture -PfdCarrierFixtureRecovery \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion \
  -PfdCarrierValidationRider -PfdCarrierValidationRender \
  -PfdCarrierValidationSamples=40 -PfdCarrierValidationTimeout=3000
```

The fixture waited for the real local player, created a native minimal ordinary
bearing after the server container was live, waited for native axle alignment,
then used the production select/attach/start paths on an occupied Arrow car.
The bearing was mass-valid and retained by the native container; native coupling
was formed at the normal 6.000-block Arrow spacing. The local rider remained
mounted during the initial tow.

The client validator passed after 40 static samples (`waitedTicks=370`): it
observed the replicated native cart link, all four expected client bodies were
compiled, all three fourth-dimension chassis/carrier bodies reached Sable's
chunked section-layer renderer, and the ordinary bearing reached Coasters'
per-cell native cart draw path. It recorded over one block of world motion for
both the ordinary bearing (`1.0150664996733774`) and Arrow chassis
(`1.0236426005208221`). The integrated server then stopped normally.

## Provenance

The concise client events from this run are retained in
[`render12-events.txt`](render12-events.txt). They are an excerpt from the local
development-client log, not packaged or CI evidence. The preserved full local
log has SHA-256 `4ff06e025cf1b196b9d1546cd393452819dd5881932241dca4197c28f64b417f`.
The invocation's exit status was not retained; the log does record the normal
integrated-server stop. The working tree used for the run was captured
immediately afterward as commit
`fa5441746d3ad170cb2919225ca0aa78090abb57`; the relevant source blobs in that
commit are `CarrierRenderMetrics.java`
`2d12a136718b6b2403e2a27733794e2ce027dfc5`,
`CarrierTransformValidation.java`
`2fe863dfbdcd61a9b131a4aa230ad533dc7c52a8`,
`CoasterCartRenderMixin.java`
`cca728bd4d6483872c2c29f10c940c47900ff34e`,
`CarrierValidationFixture.java`
`5b853555f096dd2dce547c846080e46ad1a85205`,
`RecoveryOperations.java`
`c0be067d8bc74797956e9bd9efe601fbc066048d`, and
`fourthdimension.mixins.json`
`86d49d79e2a6cac6250c74b0c5065d84adcbdeaf`.

## Scope and limits

This demonstrates an occupied, physically linked Arrow recovery group reaching
the client render pipeline and moving under native chain power. It is not a
manual player GUI/editor interaction: fixture setup calls the same guarded
production operations after mounting the real local player. It does not prove
complete player/camera mesh clearance, curved or banked rendered recovery,
rider-rack clearance, braking/cut/detach/unload, cold-process recovery
lifecycle, or Engineered/FE operation. Those FD402 items remain open.
