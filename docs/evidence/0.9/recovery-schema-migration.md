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
reopens the result. It requires the original 24 rail IDs and three chassis IDs
to remain unchanged, preserves the original Arrow/legacy ZacSpin/Free Spin
families, and confirms that no recovery state is invented.

Every loaded vehicle must retain its original chassis UUID and have neither a
recovery partner nor a removed-recovery-partner state. The same test retains the
negative control: a saved 4D chassis cannot be accepted as another 4D vehicle's
ordinary recovery partner.

## Result

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer --no-daemon -PfdIntegrations=styles,engineered
```

The schema-migration checkpoint's recorded combined run passed all **38 required
GameTests**. The later dev.27 support-loss checkpoint raises the current combined
suite to 39; see [five-family support loss](five-family-support-loss.md). The matrix covered
five legacy schema declarations across the actual five-family occupied recovery
fixture, preserving all five chassis identities in every case and inventing no
recovery state.

## Limits

This is an in-memory native loader-contract regression. It does not establish a
packaged upgrade from each released historical world, provider removal or
external-block conversion, backup/recovery procedures, real-client migration, or
the complete FD903 release matrix. Those remain required before the 0.9 candidate
gate can close.
