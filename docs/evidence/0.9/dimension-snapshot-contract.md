# FD902 dimension-scoped snapshot contract

Fourth Dimension's full client snapshot now carries the source dimension
identifier before its serialized `FoundationData` payload. The client applies a
snapshot only when that identifier equals the client level that processes the
queued packet.

This prevents a snapshot sent during login or a dimension change from replacing
the newly active level's data after the client has already switched levels. The
server sends a new snapshot on login, dimension change, explicit broadcasts and
the normal 20-tick update cadence.

The changed payload is registered as network protocol **7**. Save schema remains
6. Clients and servers must both use protocol 7; an older protocol cannot decode
the added dimension field.

## Validation

`maximumFunctionalSnapshotFitsNetworkBudget` now encodes and decodes the
maximum functional snapshot with its dimension field. It requires the decoded
snapshot to match the current test level and rejects an otherwise valid payload
tagged for `minecraft:fd_stale` before loading its data.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --no-daemon
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon \
  -PfdIntegrations=styles,engineered
```

Unit tests passed, and the combined optional-addon GameTest profile passed all
**39 required GameTests** in **4.172 minutes**.

## Limits

This proves the wire contract and the client-side admission decision inside the
native GameTest snapshot path. It does not create two remote clients or replay
an actual packet arriving across a live dimension transition, disconnect, late
join or chunk unload. Those FD902 cases remain required acceptance work.
