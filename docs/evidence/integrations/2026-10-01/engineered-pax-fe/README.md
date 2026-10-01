# FE-powered Engineered equipment and occupied PAX ownership

This extends the compact PAX power-loss fixture to Engineered 1.1.1 with its real
`requireEnergy` option enabled. A separate native batch restores the original option
afterward. The original energy-free power-loss and four native eviction tests remain
in the suite. No gameplay drive policy or braking threshold is changed.

## Native test contract

The fixture boards sixteen native pigs on four coupled PAX cars with eight carrier
bodies. The Engineered controller owns a real three-segment span, its observer watches
the station, and the span's native block capability supplies FE. In every phase the
probe starts empty, fills to 100,000 FE at phase tick 5, drains at tick 10, then refills
to 12,345 FE at tick 15. Re-entering lifting after inspection repeats these stages.
Every intervening tick requires the exact expected balance: ignored external PAX
commands must neither consume energy nor regenerate it.

Reverse LAUNCH 100 opposes lifting/capture; forward LAUNCH 100 opposes fault and
boarding holds; STOP runs during outbound travel; rolling TRIM 1 runs on return.
The controller must report presence in all six phases and the observer must detect
the train. Empty and full buffers must leave the physical winch in control.

The test removes Create winch power during lifting, requires a fault and rejects
restraint opening. The lead-car position measured 40 ticks apart during the fault
must differ by less than 0.3 blocks in three dimensions. Restored Create power does
not resume the lift until an explicit command. The train must physically lift,
traverse the station under gravity, return, recapture and hold at boarding for
20 ticks before unloading.

Boarding occurs at tick 80; after the native physics occupancy sample, tick 81 must
report all sixteen occupants before dispatch. Every active tick verifies all original rider/carrier attachments and shared
occupancy, restraint and phase telemetry. All sixteen passengers must be alive and
normally unmounted at completion, before fixture cleanup. The native coupling and
track-engagement checks remain active. Completed fixture chunk tickets are released.

## Fixture timing correction

The first run with strict occupancy assertions failed in the base profile: the
fixture boarded all riders and began checking telemetry in the same tick. Carrier
occupancy is refreshed by native physics. The corrected fixture separates boarding
and dispatch by one tick and requires sixteen occupants at that boundary before
continuing; it does not relax the occupied-cycle assertion. The failed base run and
its accompanying JAR remain identified separately in the evidence. No production
telemetry or mount code was changed for this timing correction.

## Final results

| Profile | Registered native tests | Absent-addon skips | FE-powered PAX result |
| --- | ---: | ---: | --- |
| Base | 33 | 6 | Explicit absent-addon skip; original occupied cycle passes |
| Engineered 1.1.1 | 33 | 3 | One occupied cycle, all six controller phases |
| Combined pinned pack | 33 | 0 | One occupied cycle, all six controller phases |

The build and all 19 JUnit tests pass. Engineered-only hold drift was 0.000856 blocks;
combined drift was 0.000530 blocks. Each FE-enabled run recorded 28 buffer transitions
(the lift repeats after resume), exact balances between transitions, original mounts
for all sixteen riders, complete occupied telemetry and normal unloading. The original
four PAX chunk-recovery cases also pass in each profile. `verification.json` records
the checked measurements and native controller-presence counts.

## Reproduction

Run native processes sequentially; their GameTest world is shared. Use Java 21:

```sh
set -e
./gradlew build
./gradlew runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
python3 scripts/verify-engineered-pax-fe.py docs/evidence/integrations/2026-10-01/engineered-pax-fe
```

`runs.json` identifies full local logs, hashes and terminal exits. Filtered checks,
installed versions and warnings are retained. The exploratory run preceded the
stronger every-tick rider-attachment and occupancy checks. Final artifacts and
fixture source hashes are in `artifacts.json`; the native runs execute compiled
classes rather than a separately launched packaged server. `classes.json` records
byte-identical compiled/JAR hashes for the two changed fixture classes.

## Scope

This checks direct native FE capability input on one compact occupied PAX cycle,
including a Create-power interruption. It does not certify FE-powered PAX cold
restarts, chunk interruption, human-player clients, alternative routes or external
energy cables/generators. The combined profile includes Additions and Magnetized
shuttle checks, but no Immersion camera claim follows from a headless server test.
Broader FD901–FD904 acceptance remains open.
