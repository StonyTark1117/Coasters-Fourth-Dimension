# Returning cold restart: native passenger attachment failure

This records the **original failed run**, not a compatibility pass. A later
[loading-boundary fix and fresh packaged recovery check](../passenger-entity-loading/README.md)
resolves the reproduced bug; the original reports below remain unchanged.
The validated `f8dbccb` artifact correctly applies cold-fault braking/capture, but
a later occupied returning cold restart restores only two of sixteen native rider
attachments. All sixteen pigs survive with their original UUIDs and health 10.
Fourteen become independent root entities instead of passengers of Create seats.

The preparatory Engineered-only process loads the previous occupied lifting world,
resumes through the winch state machine, observes the real returning phase, selects
rolling TRIM 1, freezes, reports and stops. The stopped world is copied into a new
Engineered-only server process with FE required. After 100 stepped ticks, the winch
is correctly faulted with interrupted `RETURNING`, all four cars brake, all eight
carriers are captured, the original carriage is loaded, and FE remains 12,345.
However, shared telemetry and actual carrier occupancy report only two riders.
The driver's strict sixteen-rider assertion stops the workflow immediately.

The series then freezes, saves and stops that server cleanly and restores the original
packaged settings. No recovery command or unload was issued in the failing process.
The combined returning restart and both capture restart cases were not run. The last
job's `state: running` in `series-status.json` is the driver's pre-cleanup label;
its server exit is 0 in `runs.json`, its operator exit is 1, both PIDs are gone,
and the series is failed with settings restoration confirmed.

Independent inspection of the stopped worlds' native entity-region NBT shows:

| Observation | Before restart | After failed restoration |
| --- | ---: | ---: |
| Original tagged pigs | 16 | 16 |
| Nested under `create:seat` | 16 | 2 |
| Independent root pigs | 0 | 14 |
| Health of every pig | 10 | 10 |

`before-entities.json` and `after-entities.json` retain UUID, health, position and
actual serialized parent identity. The inspector reads region-file entity NBT
without editing the world; it rejects duplicate pig UUIDs. `before-save.json` and
`after-save.json` independently record the corresponding 16-to-2 occupancy change.
Raw reports, commands, driver failure, mod list, warnings and log/artifact hashes
are retained. The same JAR passed the earlier outbound case and native matrix;
those results do not cover this returning attachment failure.

`Boarding.restoringSavedPassenger` currently requires the saved rider UUID to remain
in `carrier.occupants`, while `NativeLoading.update` replaces that list from live seat
entities each physics tick. This is a load-order hypothesis to instrument, not a
confirmed root cause. Do not bypass normal boarding validation or treat these riders
as absent from the save. Human-player behavior is not established by this pig fixture.

From the repository root, the retained inspector can reproduce the entity inventory:

```sh
python3 docs/evidence/integrations/2026-10-01/pax-returning-passengers/inspect-entities.py /absolute/path/to/stopped/world
```

The original local worlds were `FDPaxColdReturningBefore2` and
`FDPaxColdReturningEngineered2`. The temporary runtime was later cleared, so those
world files are no longer available. Their inspected entity inventories, saved-data
reports, command transcripts and hashes remain in this directory.
