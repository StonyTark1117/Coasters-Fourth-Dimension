# FD903 recovery schema migration matrix

Dev.27 extends the occupied five-family recovery regression from the prior
schema-5 case to every schema the current loader advertises as supported:
schemas 1, 2, 3, 4 and 5.

## Method

The native recovery fixture creates all five functional families, mounts its
native passengers, attaches the certified ordinary recovery bearing, tows under
native chain power, brakes and saves the live `FoundationData`. For each legacy
schema number, the test removes `recovery_cart` and `recovery_cart_removed` from
each saved vehicle record before loading it through `FoundationData.load`.

A separate GameTest reads the recorded schema-3 integrated-world extract from
the 0.3 acceptance evidence. CI regenerates its compressed NBT fixture from that
read-only JSON record, then the current loader reads it, writes schema 6 and
reopens the result. It requires the original 24 rail IDs, their complete
distance/angle/slope rotation-profile keys, and three chassis IDs to remain
unchanged, preserves the original Arrow/legacy ZacSpin/Free Spin families, and
confirms that no recovery state is invented.

Every loaded vehicle must retain its original chassis UUID and have neither a
recovery partner nor a removed-recovery-partner state. The same test retains the
negative control: a saved 4D chassis cannot be accepted as another 4D vehicle's
ordinary recovery partner.

## Packaged-server replay

A clean packaged dedicated-server world was created, stopped normally, then
seeded with the exact committed schema-3 `fourthdimension_foundation.dat`
fixture. The packaged server loaded that record, saved it, and was restarted
against the rewritten file. Its validation query reported the persisted
historical record on both loads:

```text
Validation availability chassis=0/3 carriers=0/8 assignments=0
```

The zero loaded native bodies are expected: the preserved historical artifact
contains the authoritative `FoundationData` file, not the associated old Sable
sublevel directory. The query proves the packaged server read the three saved
vehicle records and eight carrier records; it does not claim they are playable
without their matching native sublevels.

After the first save, the committed verifier compared the source and rewritten
files directly:

```sh
python3 scripts/verify-packaged-historical-migration.py SOURCE.dat UPGRADED.dat
```

```text
FD903_PACKAGED_MIGRATION sourceSchema=3 upgradedSchema=6 rails=24 vehicles=3 \
families=arrow_controlled,sns_free_spin,zacspin_legacy \
identitiesPreserved=true recoveryInvented=false profilesPreserved=true
```

The schema-6 file passed the same verifier after the packaged-server reopen.

## Complete historical world replay

A subsequent [schema-3 native-world replay](native-schema3-world-replay.md)
builds the historical `0.2.0` addon at commit `d97ea01`, creates three real
family circuits in its packaged server, and archives the complete world with
Coasters paths and Sable sublevels. The current packaged server loads that
world with all three saved chassis and all eight carriers available, saves
schema 6 with all 24 rail IDs and profile keys preserved, and reopens with the
same 3/3 chassis and 8/8 carriers available. This removes the missing-sublevel
limit of the earlier data-only replay for that one generated historical world.
The [connected-client Arrow ride](native-schema3-client-ride.md) also verifies
boarding, powered travel, capture, and dismount on one of its migrated cars.

## Unsupported provider conversion

`HistoricalMigrationGameTests` now copies the recorded schema-3 fixture and
replaces one original rail's provider with
`coasterssimulatedextratypes:arrow_4d`. The current loader must reject that
record as an unregistered functional provider. It must never infer mechanical
equivalence from the provider or the Arrow-like name, create a replacement rail,
or silently convert it to the original family.

## Result

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon -PfdIntegrations=styles,engineered -PfdPerformanceTargets
```

The current combined target-gated run passed all **41 required GameTests** in
**4.249 minutes**. The matrix covered five legacy schema declarations across the
actual five-family occupied recovery fixture, preserving all five chassis
identities in every case and inventing no recovery state. Its recorded historical
fixture now also rejects an injected third-party functional provider. The
schema-3 profile-key map is compared both by the native regression and the
packaged replay verifier, so the upgrade cannot silently flatten, rescale or
otherwise rebind the historical controlled-track profiles.

## Limits

The data-only packaged replay establishes one recorded historical
`FoundationData` file; the later native-world replay establishes one separate
complete schema-3 world. Neither proves upgrades from every released historical
world, provider removal or a supported external-block conversion, rider
operation for the other migrated cars, two-client migration, or the complete
FD903 release matrix. One connected-client Arrow ride after upgrade is recorded
separately.
Those remain required before the 0.9 candidate gate can close.
