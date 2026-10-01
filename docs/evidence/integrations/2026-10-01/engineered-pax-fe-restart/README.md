# Occupied PAX lifting cold restart with Engineered energy required

A stopped packaged world containing four coupled PAX cars, eight rotary carriers,
a physical carriage and sixteen native pig passengers is copied into separate
Engineered-only and combined-pack worlds. New dedicated-server processes load each
copy with Engineered 1.1.1 energy required. Both restore the original passenger-to-carrier
assignments, native body UUIDs, 12,345 FE and selected equipment configuration, retain
a fault until explicit resume, complete one occupied cycle and unload all riders.
No gameplay or propulsion policy changed in this checkpoint.

## Fixture and scope

`fd validation pax_energy_restart` is registered only with `-PfdValidation`, operator
permission and the exact supported Engineered adapter. Its `create` action requires
an empty Fourth Dimension validation world and creates the compact PAX example at
(-600, 180, 600), with explicit validation-world chunk tickets. The actions assemble
and board through the existing native coupling/seat APIs; dispatch, restraints,
resume and recover use the existing validated operations and winch state machine.
Commands reconstruct their references from world blocks and persisted vehicle data.
They cannot retain old Java objects across processes.

The real Engineered controller owns the full three-segment native span and its
observer watches the station. Direct native capability input seeds 12,345 FE. The
saved controller starts with reverse LAUNCH 100 opposing the lift. On restart the
report compares controller `Anchor`, `Sequence` and `Step`, observer `Watching`, and
the complete serialized native span before changing any controller command. Native
live readout fields are not compared as configuration. This is one active command,
not arbitrary timed multi-step controller-program restoration.

Reports verify four coupled chassis, eight loaded carrier bodies, sixteen distinct
living mounted riders, each original carrier assignment, closed restraints, exact
FE, denied external propulsion and matching shared occupancy/phase telemetry.
Selected schema-5 mechanical fields and all three rail records also compare exactly
between the baseline save and both final saves. The comparison excludes explicitly
changing route binding, restraints, capture, live physical measurements and emptied
occupancy; it does not claim byte-identical complete vehicle saves after a ride.

## Packaged results

| Profile | Restored FE | Passengers | Largest initial chassis movement | Powered fault-hold drift |
| --- | ---: | ---: | ---: | ---: |
| Engineered 1.1.1 | 12,345 | 16 | 0.006823 blocks | 0.000000013 blocks |
| Combined pinned pack | 12,345 | 16 | 0.006823 blocks | 0.000000032 blocks |

Each new process restores `FAULT`, interrupted `LIFTING`, zero completed cycles,
and the original carriage UUID. Dispatch and restraint opening are rejected.
Forward LAUNCH 100 cannot move the held train during at least 60 live simulation
ticks. Explicit resume then permits lifting, outbound, returning, capture and
boarding. The controller changes to STOP on outbound, rolling TRIM 1 on return,
reverse LAUNCH 100 on capture and forward LAUNCH 100 at boarding. The 24 live cycle
samples per profile retain all sixteen mounts and exact FE; controller driving
reports cover each moving phase and the observer signals during the ride. These
samples are periodic snapshots, not an every-tick packaged-server assertion.

After final station capture all four cars are ready. Normal unload leaves zero
occupants and open restraints; native entity queries find all sixteen pigs alive.
Both server processes save and stop successfully. The original packaged world
selection and Engineered configuration are restored byte-for-byte, with hashes in
`settings-restored.json` and no credentials copied into the evidence.

The build passes all 19 JUnit tests. Base, Engineered-only and combined native
profiles each pass all 33 registered tests, with respectively 6, 3 and 0 explicit
absent-addon skips. The saved-passenger regression, four occupied PAX chunk recovery
fixtures, and existing Engineered power/energy checks remain active. The complete
independent audit passes and records measurements in `verification.json`.

## Test-driver corrections

The exploratory setup initially asserted dispatch readiness while Minecraft was
frozen. Create 6.0.10's `KineticBlockEntity.getSpeed()` returns zero whenever
`TickRateManager.isFrozen()` is true, including during `tick step`. The first
frozen dispatch was rejected; dispatching live and then stepping while frozen
correctly produced a power-loss fault. The driver recovered through normal explicit
resume, ran unfrozen, then froze and stopped during lifting. `before_live` is the
actual baseline. Earlier `ready`, `powered` and `before` records remain in the log
as investigation evidence and are not the cold-restart baseline.

The first resumed-process driver stopped after expecting the wrong text for the
correct restraint rejection. The server stayed frozen. Re-running the driver made
a second `restored` report after 200 total stepped ticks; the independent verifier
uses this latest record. The combined process made its report after 100 ticks.
The earlier driver's in-memory lookup could return the first duplicate label, so
its printed summary is diagnostic only; verified measurements are recalculated
from the retained final records. No production check was bypassed or relaxed.

## Reproduction

Use Java 21 and the same built JAR for every packaged run. Servers run sequentially.
Select fresh validation worlds and enable Engineered `requireEnergy=true`. Launch:

```sh
./gradlew build
./gradlew runServer -PfdPackaged -PfdPackagedJar=/absolute/path/to/artifact.jar -PfdFrozen -PfdValidation -PfdIntegrations=engineered
```

All fixture actions below use `fd validation pax_energy_restart`:

1. `create`; step 100 ticks; `assemble`; `equip`; `board`; `charge`; step two ticks.
2. Unfreeze, `dispatch`, run approximately 90 ticks, freeze, and `report before_live`.
   Require moving `LIFTING`, 16 occupants and 12,345 FE. Save, stop and copy the
   stopped world for the two recovery profiles.
3. Launch a copy frozen with Engineered only; launch the other copy separately
   with `-PfdIntegrations=engineered,styles,additions,magnetized,immersion`.
4. Step 100 ticks while already faulted; `report restored`. Compare body/rider
   identities, FE and equipment before changing any commands. Check rejected
   `dispatch` and `unload`.
5. `forward`; unfreeze for at least 60 ticks; freeze; `report held`. Require the
   fault to remain and every chassis to drift less than 0.3 blocks.
6. `reverse`; unfreeze; `resume`. Sample `report` roughly each second, selecting
   `stop_motor` on outbound, `trim` on return, `reverse` on capture and `forward`
   at boarding. Require one completed cycle and all sixteen original mounts.
7. Hold boarding for at least 20 ticks, freeze and report. `unload`; step ten ticks;
   report; query tagged pigs; save and stop. Restore original server settings.

`*-commands.txt` retains exact executed commands, including rejected operations.
`runs.json` identifies terminal process exits, full local logs and hashes; excerpts
retain reports, mod lists, startup warnings and diagnostics. The loaded source path
confirms the packaged JAR. `artifacts.json` and `classes.json` tie the source and
byte-identical compiled/JAR classes to the artifact. The native regression results
are recorded separately because those launches execute compiled classes.

The native regression reproduction is:

```sh
./gradlew runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
python3 scripts/verify-engineered-pax-fe-restart.py docs/evidence/integrations/2026-10-01/engineered-pax-fe-restart
```

This evidence is limited to a compact occupied lifting cold restart, native pigs,
and direct FE capability input. Cold restart during outbound, returning and capture,
human-player/client recovery, external power networks, alternate geometry and
arbitrary controller-program resumption remain open. Merely loading the combined
pack does not establish its client camera or rendering behavior. FD901–FD904 broader
acceptance remains open.
