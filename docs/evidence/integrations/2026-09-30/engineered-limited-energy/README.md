# Engineered limited charging and regeneration boundaries

The existing occupied [energy and equipment reload fixture](../engineered-energy-reload/README.md)
now continues through limited charging, loss of supply, refill recovery and two
regeneration settings. It uses the real Engineered 1.1.1 anchor FE capability,
controllers and native physics: two linked Arrow cars plus one legacy, Free Spin
and revised car, with five native pigs seated throughout. PAX remains covered by
its separate winch-owned fixture; this is not an FE-driven PAX test.

The change extends acceptance tests and their configuration cleanup. No production
adapter behavior was changed to obtain the result.

## Sequence and assertions

After the original hold, empty-buffer starvation, forward launch/brake, STOP,
reverse launch/brake and recapture checks:

1. Dispatch with empty buffers, then supply exactly **100 FE** per family through
   the native capability. All cars move at bounded positive speeds, drain their
   buffers and report native controller starvation while retaining presence.
2. Wait forty more ticks without power. The buffer remains empty and the cars
   do not gain more than 0.25 blocks/s over their measured pulse speeds. Coasting
   is expected; power loss does not imply an immediate stop.
3. Supply **5 FE per tick for forty ticks**, exactly 200 FE per family. All cars
   accelerate again, remain below 3 blocks/s, exhaust the supply and report
   starvation. The feeder is a direct capability call, not an external cable.
4. Refill the original 100,000 FE buffers. Native starvation clears and every car
   gains more than 1 block/s over its trickle-powered speed.
5. Empty the buffers and set regeneration efficiency to **0**. Native trim still
   brakes every car below 1.3 blocks/s, and no FE is generated.
6. Refill, recapture normally, and dispatch in reverse along the available track.
   Once every car exceeds 3 blocks/s in reverse, empty the buffers at their old
   capacity, then set capacity to its configured minimum of **1,000 FE** and
   regeneration efficiency to **1**. Native trim brakes every car below 1.3
   blocks/s and saturates every buffer at exactly 1,000 FE. A further 1 FE input
   is rejected. Emptying before the capacity change prevents hidden old energy
   from masquerading as regeneration.
7. Recapture all cars, verify boarding alignment and retained restraints, then
   open restraints and unload all five passengers. Coupling, fault-free state and
   passenger retention are also checked at the intermediate phase boundaries.

The isolated energy batch snapshots and restores `requireEnergy`, buffer capacity
and regeneration efficiency, including on failed runs. The later default PAX
fixture confirms energy requirements are disabled again. The post-run configuration
snapshot also records the restored values.

## Rejected fixture attempts

All three failed runs are retained, including their restoration logs:

- Attempt 1 checked starvation on the same tick that the trickle feeder began.
  Minecraft stores scheduled test callbacks in an unordered map; the feeder could
  refill the buffer before the assertion. Starting the feeder on the following
  tick separates those operations. Subsequent pulse/coast/trickle checks pass.
- Attempt 2 added another forward launch near the end of the reused 60-block
  route; the relaunch setup assertion failed before testing full regeneration.
- Attempt 3 shortened that forward launch and added position/fault diagnostics.
  It confirms the legacy car reached the compatible-track boundary and correctly
  faulted. The setup now recaptures and reverses using normal operations, without
  moving vehicles artificially or relaxing the boundary guard.

These are rejected setup attempts, not passing compatibility evidence. The final
saturation and recapture assertions remain required for success.

| Profile | Registered required tests passed | Explicit optional skips |
| --- | --- | --- |
| Engineered only | 28 | 3 |
| All five addons | 28 | 0 |
| Base | 28 | 5 |

The build passes 19 unit tests. The same final suite also checks all ten connected
track routes from the [partial-eviction regression](../connected-track-chunks/README.md).
Exact mod lists, result excerpts, warnings, full-log hashes, JUnit XML and final
source/artifact hashes are retained. Existing dependency startup diagnostics remain;
a passing suite is not a clean-startup certification.

Reproduce sequentially with Java 21:

```sh
./gradlew build runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
python3 scripts/verify-engineered-limited-energy.py docs/evidence/integrations/2026-09-30/engineered-limited-energy
```

This covers these specific supplies and regeneration boundaries on straight tracks.
It does not establish all configuration extremes, generator/cable compatibility,
curved or sloped train behavior, gravity rollback, occupied chunk interruption,
full process restart with FE enabled, multiplayer energy synchronization or
performance. Partial payment inside a single physics substep is not independently
instrumented here; the measured result is native motion under a limited FE budget.
