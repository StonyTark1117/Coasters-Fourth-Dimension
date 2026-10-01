# PAX cold-restart fault retention

An occupied outbound cold restart exposed a missing transition: winch deserialization
converted the saved active phase to `FAULT`, but never applied the train-retention
side effects of `fail()`. All four cars retained `stopping=false` and all eight
carriers retained `capture=false`. The sixteen passengers stayed mounted, so an
occupancy-only check would not have detected the missing braking and capture.

## Reproduction and fix

The stopped outbound world was prepared from the previous occupied lifting baseline,
resumed through the normal winch operation, then stopped during real outbound motion.
The native Engineered span/controller/observer were present with energy required,
12,345 FE and a STOP command. An unchanged packaged JAR loaded a copy in a new process.
After 100 simulation ticks the winch reported the intended fault, but the live
carrier report and the subsequent saved data both proved that capture and braking
were disabled. `broken-checks.txt` and `broken-save.json` retain this failure state.

The native regression reproduces the deserialization boundary independently: it
reuses an existing four-car native fixture, deserializes each of the four active
phase names with released braking/capture flags, allows native physics to tick,
and requires retention without changing the interrupted phase. The original
runtime fails the first check with `Cold LIFTING fault did not retain every native
car and carrier`. Its class bytes match the runtime in the broken packaged JAR.
This early failure does not assert that the remaining three cases ran on old code.

`ShuttleRuntime` now checks a faulted winch's known vehicle metadata before chunk
availability handling and physics. If any known car lacks braking, restraints or
carrier capture, it calls the existing fault transition with the current reason.
Since the winch is already faulted, its interrupted phase stays unchanged. This
reapplies retention as metadata becomes available, including partial load order,
and does not engage a distant catch or authorize automatic resume/recovery.
The existing brake strength, readiness thresholds and control ownership are unchanged.

The native regression then restores its initial boarding state and continues the
existing remote-chunk interruption and physical recovery workflow. Reusing the
fixture preserves the suite's 33 registered tests and avoids adding another train
to the native world's vehicle-capacity budget. The new packaged report also includes
per-car `stopping`, physical carriage availability and carriage position.

## Occupied outbound recovery

New Engineered-only and combined-pack processes load independent copies of the same
stopped outbound world with the fixed JAR. Before any new controller command, they
compare exact FE, the complete span, controller `Anchor`/`Sequence`/`Step`, observer
`Watching`, all native chassis/carrier UUIDs and all sixteen rider-to-carrier mappings.
Every car must be braking and restrained, every carrier captured, and the original
carriage body loaded. Shared telemetry must report the same occupied fault state.

Dispatch, restraint opening, simple resume and premature manual recovery are rejected.
A forward LAUNCH 100 command opposes the fault while the released train brakes under
normal physics. Every periodic snapshot must retain all riders and exact FE, and the
empty carriage must remain within 0.3 blocks of its settled position. Recovery is
requested explicitly only after all cars report boarding readiness. The real catch
then returns the train to the station; one cycle completes and normal unload leaves
all sixteen native pigs alive. No body is teleported and no readiness limit is relaxed.

| Profile | FE retained | Riders | Braking ticks after initial 100 | Train travel while braking | Empty carriage drift |
| --- | ---: | ---: | ---: | ---: | ---: |
| Engineered 1.1.1 | 12,345 | 16 | 2,872 | 8.1432 blocks | 0 blocks |
| Combined pinned pack | 12,345 | 16 | 2,666 | 7.5305 blocks | 0.00000000 blocks |

The braking interval is expected to include movement on the slope. A gravity fault
is not a stationary mechanical hold. These are simulation ticks and periodic
snapshots, not an every-tick packaged assertion or a server performance benchmark.
All three saved rail records and selected mechanical identity/tuning fields compare
exactly after recovery. Intentionally changed route bindings, operating state,
angles/velocities and emptied occupancy are excluded from that comparison.

## Regression matrix

The final matrix passes in base, each of five individual addons, and the combined pack.
Engineered-only and combined profiles enable `-PfdPaxEnergyReload`, so their four
native occupied phase-eviction cases additionally check FE and equipment restoration.
Every profile must contain the four new cold-retention records and complete the
original remote-chunk/physical-recovery case. The build runs all 19 unit tests.
All seven processes pass 33 registered native tests, with 6/3/6/4/5/6/0 explicit
absent-addon skips in base/Engineered/Styles/Additions/Magnetized/Immersion/combined
order. All 19 unit tests and the complete independent evidence audit pass.
`verification.json` records the measured recovery results; `runs.json` identifies
the terminal processes, including the deliberately failing pre-fix regression.

## Reproduction

Use Java 21. Run native servers sequentially because they share one test world:

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=engineered -PfdPaxEnergyReload
./gradlew runGameTestServer -PfdIntegrations=styles
./gradlew runGameTestServer -PfdIntegrations=additions
./gradlew runGameTestServer -PfdIntegrations=magnetized
./gradlew runGameTestServer -PfdIntegrations=immersion
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion -PfdPaxEnergyReload
```

For packaged reproduction, use the opt-in `pax_energy_restart` fixture documented in
[the lifting checkpoint](../engineered-pax-fe-restart/README.md). Run with
`-PfdPackaged -PfdPackagedJar=/absolute/path/to/artifact.jar -PfdFrozen -PfdValidation`
and Engineered energy required. Prepare four occupied coupled cars, dispatch with
the server unfrozen, select `stop_motor` during outbound, then freeze, report, save
and stop. Make two copies only after the process has exited. Load each copy with
Engineered-only or the combined addon profile.

Step the already faulted server 100 ticks and report. Test rejected dispatch/unload;
select `forward`, unfreeze, test rejected resume and premature recover, then sample
reports until every car is ready. Issue `recover`, select `reverse` during capture,
wait for boarding, select `forward`, hold for 20 ticks, freeze, report and unload.
Step ten ticks, report, query tagged pigs, save and stop. Restore original packaged
settings after all processes end. Create reports zero kinetic speed during frozen
`tick step`; active ride and command phases must run unfrozen.

`*-commands.txt` retains the exact commands, including denials. `runs.json` records
full local log paths/hashes and terminal exits; retained excerpts include checks,
versions, warnings and driver output. `artifacts.json` records old and fixed JARs and
sources. `classes.json` proves the native/packaged runtime relationship before and
after the fix. Settings restoration is recorded by hashes without copying credentials.

```sh
python3 scripts/verify-pax-cold-retention.py docs/evidence/integrations/2026-10-01/pax-cold-retention
```

This closes the observed cold-fault retention bug and adds bounded occupied outbound
cold-restart evidence. Returning/capture packaged cold restarts, human clients,
external energy networks, alternate geometry and arbitrary timed controller-program
restoration remain open. Native phase-tag checks do not substitute for those complete
process-lifecycle cases. Broader FD305/FD901–FD904 acceptance remains open.
