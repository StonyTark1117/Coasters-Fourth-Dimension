# Occupied PAX chunk recovery with Engineered FE enabled

The existing four phase-interruption fixtures can run with Engineered 1.1.1's real
energy requirement enabled using `-PfdPaxEnergyReload`. The property applies only to
the native GameTest launch. Each phase's batch enables the requirement and restores
the previous value afterward. The ordinary mode retains its energy-free checks;
requesting FE mode without the supported addon fails explicitly.

## Test contract

Lifting, outbound, returning and capture each begin with sixteen native passengers,
four coupled chassis, eight rotary carriers, a physical carriage, four anchors,
the Create motor/winch, and real Engineered controller/observer blocks. Native FE
capabilities receive distinct nonzero balances: 12,346, 12,347, 12,348 and 12,349 FE
respectively. The balances must remain exact during every fully available route tick.
Unlike the separate supply-cycling fixture, these balances stay constant across the
interruption to provide an unambiguous persistence check.

Every original body and block entity must physically unload. All native train bodies
must share one loading group. The carriage then loads with the winch outside ticking
range for 100 ticks before the complete route returns. The original phase-recovery
checks remain active: bounded carriage motion, original rider attachments, restrained
occupancy telemetry, explicit resume for lifting/capture, manual recovery after
braking/readiness for gravity phases, intact coupling, and normal unloading.

On the first fully available tick with fresh equipment, before the probe issues a
new command, it checks fresh controller/observer/energy instances, exact FE, the
complete native span, and serialized controller `Anchor`, `Sequence` and `Step`
fields plus observer `Watching`. Native live readouts and presence flags are not
compared as configuration. The command fixture uses one active command per phase;
this does not establish arbitrary timed multi-step program resumption.

The controller then continues opposing LAUNCH/STOP/TRIM commands through applicable
recovery phases while the physical winch remains authoritative. Presence and observer
checks must pass. One native equipment restoration is required per fixture. These
reads are gated on the whole route being in native ticking range so the probe cannot
keep the eviction target loaded. After unloading is complete, no stale FE capability
is used as evidence for the returned equipment.

## Final results

| Profile | Registered native tests | Absent-addon skips | Four interruption fixtures |
| --- | ---: | ---: | --- |
| Base | 33 | 6 | Energy-free behavior retained |
| Engineered 1.1.1, default | 33 | 3 | Energy-free behavior retained |
| Engineered 1.1.1, FE enabled | 33 | 3 | Exact FE and selected equipment state restored |
| Combined pinned pack, FE enabled | 33 | 0 | Exact FE and selected equipment state restored |

All four processes exited successfully, and the build passed 19 JUnit tests. Each
FE-enabled phase restored its seeded balance exactly and verified fresh equipment
before issuing its first recovery command. All sixteen original native passengers
survived each fixture, retained their mounts and unloaded normally after one cycle.
The largest initial lead-body reload step across all profiles was 0.54834 blocks,
below the 1.5-block fixture bound.

Gravity recovery remains deliberately explicit and slow: FE-enabled outbound cases
waited 4,900/4,911 ticks after the initial 100-tick full-load observation, and returning
cases waited 2,578/2,556 ticks (Engineered-only/combined). Lifting and capture held
for 60 ticks before explicit resume. These are native simulation ticks, not measured
wall-clock server performance. `verification.json` records per-phase measurements,
controller presence counts and restored FE values.

## Reproduction

Use Java 21; run native processes sequentially because they share one test world:

```sh
set -e
./gradlew build
./gradlew runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=engineered -PfdPaxEnergyReload
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion -PfdPaxEnergyReload
python3 scripts/verify-engineered-pax-fe-chunks.py docs/evidence/integrations/2026-10-01/engineered-pax-fe-chunks
```

The exploratory run preceded the explicit seed-value log line. `runs.json` identifies
full local logs, hashes and exit statuses; retained excerpts contain native checks,
versions and warnings. `artifacts.json` records the companion JAR and source hashes.
The native tests execute compiled classes, not a separately launched packaged server.
`classes.json` records byte-identical compiled/JAR hashes for both changed Java classes.

## Scope

This is direct-capability FE input and native pig-passenger evidence on the compact
PAX route. It does not establish cold process restarts, human clients, external
energy networks, alternate geometries or general controller-program continuation.
The existing slow gravity braking/readiness behavior remains: an interrupted
outbound/returning train is uncaught and can travel before recovery becomes available.
Broader FD901–FD904 acceptance remains open.
