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

## Packaged returning and capture recovery

Fresh stopped worlds now cover both returning and capture with Engineered-only and
combined-pack dedicated-server processes. Each active baseline is genuinely moving,
with all sixteen pigs mounted. A copy loads in a new process, steps 100 ticks, and
must restore `FAULT` with the correct interrupted phase, all four braking cars, all
eight captured carriers, the original carriage, exact FE, controller/observer/span
configuration and all original rider-to-carrier assignments. Dispatch and restraint
opening remain rejected.

Returning faults reject simple resume, reach boarding readiness and require explicit
manual recovery. These fresh returning cases are already ready after the initial 100
ticks; they do not demonstrate a long braking interval. Capture faults hold for 60
live ticks against a forward Engineered command, then require explicit resume. In
both paths, the controller opposes capture with reverse LAUNCH 100; the winch retains
control, completes one cycle and permits normal unloading at the station.

| Cold-restart phase | Profile | Original riders restored / unloaded alive | FE retained | Empty carriage drift during observed fault samples |
| --- | --- | ---: | ---: | ---: |
| Returning | Engineered | 16 / 16 | 12,345 | 0 blocks |
| Returning | Combined | 16 / 16 | 12,345 | 0 blocks |
| Capturing | Engineered | 16 / 16 | 12,345 | 0 blocks |
| Capturing | Combined | 16 / 16 | 12,345 | 0 blocks |

Independent inspection of each stopped entity-region save proves the same sixteen
UUIDs, health 10, and normal root-entity state after unloading. Baseline entities are
nested under Create seats. Selected mechanical fields and all three rail records
also compare exactly. Operating state, live angles/velocities and emptied occupancy
are excluded from mechanical comparisons. Periodic reports do not establish an
every-tick packaged assertion or a performance benchmark.

All six successful processes exit 0. `runs.json` records their versions, commands,
log hashes and exits; `packaged-status.json` is complete. `packaged-verification.json`
contains the independent audit results. Private runtime settings are restored by
hash. The full remaining native addon matrix is still running; this checkpoint's
native result above is specifically Engineered-only.

```sh
python3 scripts/verify-pax-returning-capture.py docs/evidence/integrations/2026-10-01/passenger-entity-loading
# After collecting the remaining native profiles:
python3 scripts/verify-passenger-entity-loading.py docs/evidence/integrations/2026-10-01/passenger-entity-loading --matrix
```

The previous temporary server directory was cleared between turns. Its committed
failure reports and entity inventories survive, but its original stopped worlds
are no longer available. These are newly prepared fixtures with the same pinned
versions. New stopped worlds are also archived under the worktree's ignored
`build/passenger-load/worlds` directory. The archived driver uses that directory and
the native-generated configuration; copy it to `build/passenger-load/packaged.py`
for local investigation. Command transcripts and the opt-in fixture in
[the lifting checkpoint](../engineered-pax-fe-restart/README.md) describe preparation
without relying on the archived local paths.

Two driver mistakes are retained separately: `investigation-ready` expected a carriage
before first dispatch, and `investigation-ready-recovery` expected rejection after
all cars had already reached recovery readiness. Both stopped and preserved their
worlds; neither changed production validation or counts as a recovery pass.

This resolves the reproduced loading-boundary bug and adds bounded native-pig
returning/capture cold-restart evidence. Human clients, external energy networks,
alternate geometry, arbitrary timed controller programs and the wider
FD305/FD901–FD904 matrix remain open. Loading the combined pack does not certify
its rendering or camera behavior.
