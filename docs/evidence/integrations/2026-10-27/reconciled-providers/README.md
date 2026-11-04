# Functional-provider reconciliation

Parent: `f8298a426a9d08fee0ef252e60abbb9163f4d6ae` on
`codex/integration-reconcile`. This brings the registry concept from the older
integration branch onto the release baseline while retaining its recovery
fields, appearance/tint format, rendering fallbacks and shared telemetry.

## Contract and persistence

`TrackProviders` registers the original provider, supports deferred optional
factories gated by exact installed release metadata, and freezes registration
after mod loading. No actual external functional provider is registered by this
checkpoint. Test providers exist only under the explicit
`fourthdimension.providerFixtures` launch property, enabled by the GameTest run.
Normal client/server and cold-restart runs do not enable it.

External contracts must match provider identity, functional family/variant,
contact dimensions, required mechanisms and clearance. A resolver must return
the saved endpoints. Failed adapter initialization, contract linkage failures
and resolver exceptions leave the capability unavailable. Warnings are limited
to one per provider. The original contract is trusted after constructor
validation, avoiding repeated allocation in the native physics path.

Schema 7 stores the actual functional provider instead of deriving it from the
variant. It loads schemas 1–6, preserves missing providers without granting
mechanics, retains appearance/magnets/profile/revision, and preserves schema-6
vehicle recovery state. Prototype variant/provider identity remains inseparable.
Protocol 8 requires the matching new client/server loader. Unknown external
vehicle-provider declarations remain rejected; this API covers tracks.

Native snapping, placement, linked-car support, recovery support, dispatch and
boundary lookahead require provider resolution. Controlled carriers stop using
the unavailable track's profile. Missing providers do not cause saved geometry
to be rewritten as original or permanently invalidated merely by their absence.
Our renderer and editor now own only the original provider's tracks. Existing
cosmetic component ownership and fallback are unchanged.

## Scenarios and limits

The persistence case checks all five known families, unknown provider and
appearance identity, tint, magnetic equipment, exact save/load equality,
snapshot codec roundtrip, synthetic historical schema defaults, malformed and
prototype identity rejection, exact-version factory gating, initialization
failure isolation, duplicate/reserved IDs and registration freeze. The separate
recorded schema-3 world regression still checks its 24 rails, three chassis and
profiles; its foreign-provider control now proves retained identity with denied
mechanics rather than rejecting the whole world load.

The isolated native case creates five original curves and assigns a synthetic
compatible functional provider to them. Each family's real native car must
construct, settle captured and pass the native snap filter. Ten invalid provider
cases per family cover identity, family, variant, contact dimensions, clearance,
mechanisms, contract linkage failure, resolver failure, wrong returned endpoints
and absent registration. Each is rejected by native snapping and placement;
non-PAX dispatch also rejects it. Reinstating the matching saved provider
restores eligibility. Each family also roundtrips its newer recovery reference.

These synthetic providers test the adapter boundary, not a real addon's released
mechanics. Cars in this case are empty and stationary; provider restoration is
within one running server, and persistence removal checks use save/load data.
The packet test exercises the codec/loader, not a remote client. The matrix does
not certify an occupied external track after addon removal, external-provider
moving transitions, client rendering or a physical PAX external-control cycle.
FD801 remains separate and no Track Styles Arrow 4D functional adapter is enabled.

## Reproduction

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JAVA_TOOL_OPTIONS=-Xmx2G
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runGameTestServer -PfdPackaged
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runGameTestServer -PfdPackaged -PfdIntegrations=styles,engineered
python3 scripts/run-engineered-restart.py build/new-provider-restart --profiles styles,engineered
python3 scripts/verify-reconciled-providers.py
```

Run the GameTest profiles sequentially because they recreate the same world.
Pins are Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Sable 2.0.3,
Coasters 0.1.5, optional Engineered 1.1.1 and Track Styles 1.2.0. The build uses
Java 21.0.13.0.2+1. Exact artifact/source hashes and results are in the manifest.
The full six-integration objective and remaining FD305/FD401/FD901–FD904
acceptance items remain open.

The occupied restart regression uses the existing dedicated three-phase runner:
prepare the combined pack, restart with both addons, then reopen the prepared
world without Engineered. It retains the same five native pig riders/chassis,
closed restraints and captured readiness; the installed-addon restore exercises
four departures with native observers/COAST, then recapture and unloading. PAX
stays held. Forced new boarding remains rejected, and the removal phase verifies
that controls disappear while all five rides retain their passengers and unload
normally. Test-provider initialization is disabled throughout. This regresses
the new schema on real saved bodies/passengers; it does not test removal of an
actual functional track provider or human-player reconnection.
