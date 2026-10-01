# Occupied Engineered chunk interruption

Five native pig passengers survive actual parked and moving world-chunk eviction
and reload in both Engineered-only and combined-pack dedicated servers. This extends
the [occupied cold-restart evidence](../../2026-09-30/engineered-occupied-restart/README.md)
with two interruption cycles per process. No production behavior changed: this
checkpoint adds opt-in fixture probes, a slow native launch setting and evidence.

## Fixture and proof of eviction

Each process starts from a fresh copy of the same stopped, occupied baseline world:
two linked Arrow cars and one each of legacy ZacSpin, Free Spin and revised ZacSpin,
on four straight 60-block tracks. Five pigs occupy their original native seats across
fourteen carrier bodies. Native Engineered energy is required. Starting balances are
12,345–12,348 FE. The initial controller sequence is forward launch at 12.

The existing `energy_restart` fixture now has `release_chunks`, `check_chunks`,
`restore_chunks` and `slow_launch` actions. Registration remains gated by
`-PfdValidation`, operator permission and the exact Engineered adapter. These are
validation-world operations, not a general ride chunk loader. Normal vehicle
operations and native controller APIs remain authoritative.

`release_chunks` retains original Java references to both anchors and the controller
for each track, plus every chassis and carrier, then removes the fixture's forced
chunk tickets. After 500 simulation ticks, the read-only `check_chunks` requires:

- All twelve anchor/controller positions have no resident chunk and are not ticking;
  every original block entity is marked removed.
- All five chassis and fourteen carrier bodies are absent from the native Sable
  container; every original body is marked removed.
- Saved occupancy still contains one UUID per car, restraints remain closed, and
  no vehicle fault is recorded. This is retained metadata, not a claim that live
  rider entities remain loaded while the bodies are absent.

The probe uses non-loading lookups. It never calls the normal fixture report during
the unloaded interval, since that report accesses anchor/controller block entities.
No explicit save flush is used to make either final eviction happen.

`restore_chunks` reinstates the fixture tickets. After 100 simulation ticks, every
anchor/controller and all nineteen bodies are different Java instances with their
original UUIDs. The old objects remain marked removed. A full report then verifies
the original five rider UUIDs, actual native seat-to-carrier mounts, alive status,
coupling, closed restraints and fault-free state.

## Parked and moving results

The first cycle starts captured and ready. After reload, all cars remain ready;
the four FE balances, native span records and full anchor NBT match exactly. Native
controller NBT matches except its live last-cart speed/timer readout, which changes
as the held bodies settle.

For the second cycle, `slow_launch` changes the native sequence to forward launch
at 4. Validated dispatch runs for 80 ticks before tickets are released. Every car
has advanced more than one block and is moving above 0.05 blocks/s. Following the
same 500-tick eviction and 100-tick reload, every car has advanced again, retains
its passenger, and continues moving without a fault. Saved capture and stopping
remain false through this cycle; reload does not grant a new dispatch request.

Both profiles produce these identical FE measurements:

| Family | Parked reload | Before moving interruption | After moving reload |
| --- | ---: | ---: | ---: |
| Arrow, two linked cars | 12,345 | 12,025 | 11,498 |
| Legacy ZacSpin | 12,346 | 12,186 | 11,832 |
| Free Spin | 12,347 | 12,117 | 11,507 |
| Revised ZacSpin | 12,348 | 12,113 | 11,481 |

Validated capture then runs for 130 ticks and requires boarding readiness. Normal
unload opens restraints and removes all passengers after 10 ticks; a native entity
query finds all five pigs alive. Every car retains its original chassis/carrier
identities and native consist membership throughout.

The final schema-5 save preserves all four rail records exactly, including provider,
appearance, family, geometry fingerprint, profile and magnetic data. Vehicle and
carrier configuration also compare exactly against the baseline, with explicit
exceptions for live angle/angular velocity, intentionally emptied occupant lists,
opened restraints and the expected operation revision increase from 1 to 4. These
exceptions are asserted separately; unrelated fields are not silently discarded.

## Reproduction

Use Java 21 and the baseline creation commands in the linked restart report. Back up
the stopped occupied world and start each profile from a fresh copy. Set Engineered
`requireEnergy=true`. Use the same built artifact with `-PfdPackaged`,
`-PfdPackagedJar=/absolute/path/to/artifact.jar`, `-PfdValidation`, `-PfdFrozen` and
either `-PfdIntegrations=engineered` or
`-PfdIntegrations=engineered,styles,additions,magnetized,immersion`.

Wait for each `tick step` to finish before continuing. All fixture actions below
use the prefix `fd validation energy_restart`:

1. Step 100 ticks; `report before_chunks`.
2. `release_chunks`; step 500; `check_chunks`.
3. `restore_chunks`; step 100; `check_chunks`; `report parked_restored`.
4. `slow_launch`; `dispatch`; step 80; `report moving_before`.
5. `release_chunks`; step 500; `check_chunks`.
6. `restore_chunks`; step 100; `check_chunks`; `report moving_restored`.
7. `capture`; step 130; `report captured`.
8. `unload`; step 10; `report unloaded`; query tagged pigs; save and stop.

The command transcripts contain the exact executed commands, including repeated
non-loading checks. `runs.json` identifies full local logs, their SHA-256 hashes and
exit statuses. Committed records retain every report/probe, mod lists, warning/error
lines, saved-data snapshots, unit XML and relevant native test output. Artifact and
fixture-source hashes are in `artifacts.json`.

The earlier exploratory combined run is retained separately. It proved parked
eviction and restored passengers using a weaker probe without native body identity
checks. Its attempted `debug report` was rejected by Minecraft; no debug report
was produced or used as evidence. The final runs use the richer probe above.

Original packaged-server world selection and Engineered configuration were restored
byte-for-byte after both processes stopped cleanly. Dependency startup warnings and
dedicated-server class-loading diagnostics remain in the retained logs.

All 28 registered native GameTests pass in the base, Engineered and combined
profiles, with respectively five, three and zero explicit optional-addon skips.
Each profile executes the saved-passenger deserialization regression and all ten
connected-route interruption checks. Both Engineered profiles also complete the
existing limited-energy sequence and restore its temporary energy configuration.
The build passes all 19 unit tests. The evidence verifier checks both dedicated
workflows, saved configuration, these native results and unit XML together.

```sh
python3 scripts/verify-engineered-occupied-chunks.py docs/evidence/integrations/2026-10-01/engineered-occupied-chunks
```

## Limits

This is native-passenger server evidence for the four documented straight-track
families. It does not establish human-player chunk/reconnect behavior, client body
tracking or camera recovery, PAX interruption, curves/slopes/rollback, partly loaded
occupied connected routes, active addon removal or delayed/damaged entity storage.
Natural eviction timing in this stepped server is not a performance baseline.
The broader FD305/FD901–FD904 lifecycle and multiplayer matrices remain open.
