# FD903 — historical migration and backup restoration

**Complete, 2026-11-16 UTC.** The outstanding historical Legacy/Free Spin
travel, two-real-client restoration and exact backup recovery cases passed on
the [frozen input](../lead/freeze.md). Accepted loader, Arrow, provider and
provenance evidence below carries forward. All owned runtimes are stopped.

## Inputs and preserved state

Primary `main` at `c13e51df`, Art owner
`01a1dd1d-6e99-70d2-aef3-6dcebac10d72`. The immutable qualification JAR SHA-256 is
`9c14b760e71c69295d697ccd2ef317144d817d96f944094ebb618382af2b406f`.
Java21 direct packaged launches used the frozen combined-expanded dependency
set. Immersion is present on client/observer, absent from server and every
LegacyClasspath. No mutable production class output was used.

The genuine [schema-3 archive](../../0.9/fixtures/schema3-native-world.tar.gz)
was produced by historical0.2.0 commit `d97ea010e842b931dc006aad7e9002122dfdc8e1`;
its archive hash remains
`3f4f5d820deb9ffde91e576df7a582bd0234c8bfd2ddff2e3fe70e4dc0dbfac3`.
The untouched source Foundation data hash is
`c27a35a14f5b6a3e098517c16780637d1eaa9adc49f45b4cbccd3c11e2c2aa7a`.
The [original manifest](schema3-backup-manifest.json) records a matching48-file
source/backup copy. This is a complete old world with native sublevels, not a
current world relabeled with an old schema number.

Persistent root: `/mnt/NVME4/fourthdimension-runs/fd10-art-20261115`.
`source/fd903-schema3-native-world` and `backups/schema3-original` stay untouched.
The working world is `server/HistoricalWorkshop`. Both original and upgraded
occupied backups remain outside the build directory.

## Historical travel and identity

Initial native availability was3/3 chassis,8/8 carriers. Normal client seat use,
Rotation Tool, Close restraints and Dispatch operated each historical family.
No helper mounted a player, dispatched a vehicle or moved it during travel.

| Case | Measured result |
| --- | --- |
| Legacy | [13 native samples over60.696s](legacy-sustained.jsonl), capture=false throughout; X1.087–32.5, Y144.1–150.1, Z12.5–64.5, speed1.388–2.342blocks/s. [Boarded](legacy-boarded.png), [Dispatch](legacy-dispatch.png), [returned open](legacy-open.png). |
| Free Spin | [13 native samples over60.704s](free-spin-sustained.jsonl), capture=false throughout; X150.5–182.5, Y144.1–150.1, Z12.5–64.5, speed1.683–2.087blocks/s. [Boarded](free-spin-boarded.png), [Dispatch](free-spin-dispatch.png). |
| Return | Normal Capture braked/aligned each; Ready then Open and Shift cleared its assignment. Final saved Legacy/Free Spin are stopped, captured, fault-free and empty. |
| Identity | [Read-only comparisons](migration-comparison.json) preserve all24 rail IDs/full profile and magnetic intent, three chassis IDs, eight carrier IDs, families and seat topology from schema3 to8, including the final save. No recovery state was invented. |

Command setup supplied initial approach positions, tools, daylight and a temporary
vanilla table/stone footing. The footing was removed before Free Spin dispatch.
Creative flight stabilized later approaches after an early falling observer
missed its previously observed seat target; that miss sent no boarding event.
The proven private helper retained raw input/default buttons. Camera motion and
reachable vanilla table use were observed in each client; the main client's
vanilla table check occurred after its successful Legacy cycle, so it is not
claimed as a pre-boarding diagnostic. Normal travel and unload used no teleport.

## Two actual riders, offline backup and cold restore

Both clients next normally boarded **the same historical Arrow carrier**
`6b758d48-53b8-4fa1-a430-98c73553e57d`, chassis
`2f3e3099-82f6-480d-ae2f-7d253cc63134`. Dev held logical offset(1,-1,0), Observer
held(0,-1,0). Normal Close retained both; assignments=2. Each client used normal
Disconnect and Quit Game. The server then saved and exited cleanly.

The62-file offline world was copied to `backups/schema8-two-rider`; the working
copy was preserved as `qualified-before-restore`, then restored from that backup.
[Per-file hashes](two-rider-backup-restore.json) are identical before launch;
manifest SHA-256 is
`663cad04db58aadbdf1ee6836df0b7ec3675f2fbb531c198f332f4d4e6eecb3c`.
The backup/restored Foundation hash is
`b1f4ba05d3008d96f4c30f4b89e675ab438badf91eed2dd65aec267845b2ca43`.
This is restoration of a compatible current backup; the separate original
schema3 backup is retained for rollback. No in-place downgrade was attempted.

A new server process and two new client processes reopened the restored world.
Both automatically remounted their exact logical offsets with closed restraints;
the [server events](restored-server-events.log) record both `restoring=true`
mounts and accepted assignments at00:35:14 UTC. Both clients resolved the same
native chassis/carrier, and [the observer view](restored-two-riders.png) shows the
two real riders. The saved post-rejoin Foundation hash is
`ad42c3a362c6720982e3ecdedc69fdb40befa88798f9ec2115698cdf36c8fdc7`.
[Comparison](migration-comparison.json) proves exact logical seats/restraints
against the pre-disconnect record; ephemeral Create passenger-entity UUIDs are
not required to survive reconnect.

Normal [Open](restored-open.png) and each player's Shift then unloaded both:
assignments2→1→0. [Final view](restored-unload.png) shows both on the platform.
[Final saved state](migration-state.json) retains all historical bodies/rails,
no rider assignments, open restraints, captured carriers and no fault. Its
Foundation hash is
`81cebcddf546313b36756b552375dd8b0d9401acb6de37a01af4d399763e54bb`.
This supplies the shared actual-player cold restoration case to FD902A; it does
not claim PAX's distinct phase interruption cases.

## Required carried-forward cases

| Required contract | Accepted evidence and current disposition |
| --- | --- |
| Schema1–5 and schema6 | [Loader/recovery matrix](../../0.9/recovery-schema-migration.md), [historical native replay](../../0.9/native-schema3-world-replay.md), and current `HistoricalMigrationGameTests` retained in lead's FD901 scope. Current loader explicitly accepts1–8, writes8, and keeps strict identity/provider validation. |
| Historical Arrow travel | [Actual Arrow circuit](../../0.9/native-schema3-client-ride.md); current Arrow two-client restoration above adds only the outstanding lifecycle claim. |
| Appearance removal/reinstall | [Track Styles lifecycle](../../0.9/appearance-provider-lifecycle.md), [FD917 appearances](../../fd917/art/appearances.md). Appearance fallback preserves functional identity; no new provider-removal campaign is needed. |
| Exact legacy provenance, reject invented geometry | [Bounded backfill](../../0.9/create-telemetry/307ec35-legacy-provenance/README.md) and retained provider/rejection regressions. Changed/missing/foreign curves are not converted. |
| Adopted later contracts | Current schema7/8 family fields and [schema8 dependency upgrade](../../0.9/dependencies/README.md) retain their owning-package persistence evidence. [FD917](../../fd917/README.md) changed presentation without seat anchors or saved programs. Archived schema7/provider-registry work is not silently certified. |

No historical limitation becomes a new every-release-world or provider-subset
matrix. The supported backup procedure is [MIGRATION.md](../../../MIGRATION.md).
The exact comparisons, finite native traces and accepted provider records close
FD903's required criteria together.

## Runtime and evidence disposition

Owned displays320/321, loopback server25660/RCON25661, unique game directories,
Java21 client heaps1536M and6GiB scopes; server2048M/4GiB. Final measured peaks:
main3.697GB, observer3.193GB, server1.971GB ([memory](final-memory.txt)). These are
resource observations, not park budgets. [Cleanup](runtime-cleanup.txt) verifies
all five client/server/Xvfb units inactive/MainPID0/Result=success. Clients used
Quit Game and server logs show all dimensions saved. No failed stop or live
handoff remains. [Full-log locations and hashes](retained-logs.json), selected
input records and screenshots preserve reproducible evidence.
