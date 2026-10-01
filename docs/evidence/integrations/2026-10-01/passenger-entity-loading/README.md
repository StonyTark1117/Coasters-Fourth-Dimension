# Preserve passenger identity while native entity chunks load

The earlier [returning cold-restart failure](../pax-returning-passengers/README.md)
retains evidence of sixteen living pigs becoming only two seated passengers.
`NativeLoading.update` sampled query-visible Create seats and immediately replaced
saved occupant UUIDs. Sable can expose a carrier plot before Minecraft finishes
loading that plot's entities. Sampling during that interval returns no passengers;
subsequent native passenger deserialization then fails the saved-identity boarding
check through closed restraints.

The regression reproduces this ordering independently of asynchronous disk timing.
It uses the existing native vehicle fixture and occupied Create seat NBT, dismounts
the live rider normally, temporarily marks the relevant native entity-manager chunks
`PENDING`, and calls the production loading routine. The original code fails with
`Pending entity load erased saved passenger identity`. The test restores the actual
chunk statuses in `finally`; the test-only reflection changes no production access
rules or entity storage. This is a controlled loading-boundary regression, not a
complete server restart.

The fix waits until `ServerLevel.areEntitiesLoaded` is true for every carrier seat
chunk before changing occupancy or applied rider mass. Minecraft marks that state
loaded after adding the chunk's entities. Block/plot availability or ticking status
alone does not establish that state. Normal boarding validation is unchanged.

With the guard, saved identity and already-applied mass survive the pending interval;
the saved native passenger can attach behind closed restraints. Once entity loading
is complete, genuinely empty seats clear stale occupants and remove rider mass exactly
once, including a repeated update. Existing assertions still reject unknown serialized
passengers and preserve restrictions on live riders.

## Verified at this checkpoint

- The original code fails the new regression in an Engineered native run; its
  terminal process exits 1 with one required test failure.
- The fixed Engineered run exits 0 with all 33 registered native tests passing,
  including the four occupied PAX phase checks with FE required. Three absent-addon
  cases are explicitly skipped, not compatibility passes.
- All 19 unit tests pass, and the packaged artifact builds successfully.
- The compiled native and packaged `NativeLoading`, `Boarding`, and vehicle test
  classes have identical hashes recorded in `classes.json`.

Java 21, Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Sable 2.0.3,
Coasters Simulated 0.1.5 and Engineered 1.1.1 are pinned by the project. Loaded mod
versions, checks, startup warnings, terminal statuses and source/artifact hashes are
retained separately. Upstream startup warnings are not omitted from the evidence.

```sh
./gradlew build
./gradlew runGameTestServer -PfdIntegrations=engineered -PfdPaxEnergyReload
python3 scripts/verify-passenger-entity-loading.py docs/evidence/integrations/2026-10-01/passenger-entity-loading
```

The previous temporary server directory was cleared between turns. Its committed
failure reports and entity inventories survive, but its original stopped worlds are
no longer available. A fresh fixture is being prepared with the same commands and
pinned versions. Packaged returning/capture restoration and recovery, the remaining
native profiles, human clients and the broader FD305/FD901–FD904 matrix remain open.
`packaged-status.json`, if present, is a progress snapshot, not a compatibility pass.
The archived packaged driver must be copied to `build/passenger-load/packaged.py`
before use; it requires the prepared artifact and native-generated configuration,
creates an isolated loopback server, and refuses an existing `server.properties`.
