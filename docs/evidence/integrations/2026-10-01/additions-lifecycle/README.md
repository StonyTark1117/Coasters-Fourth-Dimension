# Additions stations: occupied restart and wireless delivery

This checkpoint exercises five Additions 0.2.0 stations with one original car from
each family, five native pig passengers, two real players, and twenty receiving
Create Redstone Links. An occupied Additions-only seed is saved and copied into
separate Additions-only and combined-pack restarts. It extends FD902/FD903 evidence;
it does not close the full integration or release matrix.

## What changed

The prior five-family native station check used internal family controller blocks
as frequency items. Those blocks have no item form, leaving frequency slot A empty.
Assertions checked the station's channel values, not delivery through a working
wireless network. The test now uses distinct wool items per family and diamond,
emerald, redstone and gold per channel, with four real receiving links per station.
All twenty receiver strengths are checked during open boarding, occupied retention,
dispatch and cleared-span states. This corrects the evidence gap without changing
production station or fin behavior.

`AdditionsWirelessFixture` shares that receiver setup with the opt-in
`AdditionsLifecycleFixture`. Direct programmatic link placement needs the normal
block-entity NBT load path to create Create's deferred link behavior; the fixture
performs that load before assigning frequencies through the public API. No received
signal is assigned by the fixture. The lifecycle commands require validation mode,
permission two and exactly Additions 0.2.0; absent-addon startup remains isolated.

## Occupied restart and operation sequence

Five 60-block straight tracks have five matching cars and fourteen carriers.
A native pig occupies the first seat of each car; Dev occupies another Arrow seat,
and Observer another Free Spin seat. PAX is a parked single car for station
telemetry/operation-denial testing, without a bound winch. Free Spin retains a seat
magnet span at distances 36–45; this checks persistence, not active induction during
the short launch. Fins cover each complete track in BOOST mode at strength 10.

Each native station has auto-dispatch enabled, threshold 12, a three-second delay,
zero cooldown and four populated frequency pairs. The threshold exceeds the
fixture's occupied weight readings, so closed restraints alone cannot release it.
Each restart advances 200 ordinary ticks before clients connect. Players return
through their saved carrier/local-seat reservations, without reboarding commands.

| Phase | Optional pack | Result |
| --- | --- | --- |
| Seed | Additions and required dependencies | Seven riders held, five configured stations, twenty matching receiver signals |
| Restored | Additions and required dependencies | Original occupied identities/settings restored; guarded dispatch and normal unloading |
| Combined | All five addons and required dependencies | Same occupied seed restored; same operation checks |

The station and fin spans, settings and frequency items survive. Native foundation
and entity files independently retain functional rails, profiles, magnetic span,
car/carrier identities and native pig seat UUIDs. The offline foundation retains
both player seat reservations; live reports confirm exact carriers and local seats
when they return. Occupancy channels use actual passenger counts and capacities:
Arrow reports 2/4 with weight 8, Free Spin 2/8 with weight 5, legacy/revised 1/8 with
weight 3, and PAX 1/4 with weight 5. Every occupied/bar signal is 15 while held, and
all twenty receivers match.

After occupied readback, lowering the native threshold to 1 and delay to two seconds
leaves all cars held at 20 ticks. At 50 ticks the four non-PAX cars are released and
accelerating, with occupied channel 0 and bars 15. PAX remains held, its occupied
channel stays 15, and telemetry directs operation through its shuttle winch.
All passengers remain alive on their original carriers. The operator then disables
the fins and requests normal capture. After 150 ticks every car is ready and
fault-free. Opening restraints and normal dismount clears all seven riders. Both
players are grounded at platform height 116 with health 20, flight off, no
RootVehicle and gravity enabled; all five pigs remain alive and unmounted.

Both clients inspect all five stations before boarding and after the successful
restart/operation runs. Client-side station logs show the expected occupancy,
capacity, restraints and PAX denial, alongside matching server/client foundation
snapshot hashes. Ten seed goggle captures were visually inspected; representative
Arrow/Dev and PAX/Observer captures were inspected after each restart. Other
post-restart screenshots are retained, with their telemetry verified from client
logs. The HUD fits the tested 854×480 viewport at GUI scale 2. This is not a GUI-scale,
shader or render-backend matrix, nor a new Display Link playback certification.

## Retained failures and limits

- `invalid-frequency` preserves the initial five-pig fixture with empty frequency
  slot A. It is not wireless-delivery evidence.
- `uninitialized-link` preserves the first real-receiver fixture failure and failed
  native run. The native block-entity load fix above resolves its missing deferred
  behavior. The full failing run completed before the corrected matrix began.
- Seed `overlapping-view-attempt` captures show the players obstructing each other.
  `feet-anchor-view-attempt` captures point above the stations because command
  facing used feet rather than eye height. Separate positions and explicit angles
  produce the reviewed goggle captures. These are observation corrections.
- `boundary-observation` retains an Additions-only restart that restored all seven
  riders but observed propulsion at 70 ticks. The accelerating cars approached the
  short track's guarded end, causing route-end faults. Normal capture stopped every
  car and all seven riders unloaded safely; faults were retained. It is not counted
  as a clean dispatch pass. The successful runs use independent copies of the
  unchanged occupied seed and observe at 50 ticks, before that guard engages. This
  does not relax the guard or change propulsion behavior.

The occupied restart is parked; subsequent powered operation is straight-track,
single-car evidence. Moving restart, chunk interruption, removal of Additions'
own blocks, other linked consists/geometries, seat replacement, wireless distance
limits and the wider family/pack matrix remain open. No PAX automatic dispatch or
fin propulsion support is advertised. The separate native PAX winch checks remain
part of the regression suite.

## Verification and reproduction

The final build passes nineteen unit tests. Base, Additions and combined native
profiles each pass 36 registered required checks, with seven, five and zero
absent-addon skips respectively. Additions and combined each add twenty logged
receiver observations across the four phases above. Combined retains
`-PfdPaxEnergyReload` for the existing occupied energy-reload checks.

Use Java 21 and one immutable packaged JAR. Start a fresh dedicated world with
`-PfdPackaged -PfdPackagedJar=/absolute/path/to/jar -PfdValidation -PfdFrozen
-PfdIntegrations=additions`. The retained scripts record all exact commands:

1. Run `fd validation additions_restart create`, step 100 ticks, run `cars`, step
   100 ticks, run `pigs`, step 40 ticks, then unfreeze.
2. Start Dev and Observer clients with the same pack. Inspect stations, then run
   `board_player 0 Dev`, `board_player 2 Observer`, and `close`, using the same
   command prefix. Report the occupied state, disconnect, flush and stop.
3. Copy the complete stopped world separately for Additions-only and combined
   restarts. Combined uses `engineered,styles,additions,magnetized,immersion`.
   Step 200 ticks, unfreeze and reconnect both players normally. Record occupancy.
4. Freeze, run `release`, step 20 and report, then step 30 and report. Run `capture`,
   step 150 and report; run `unload`, step 20 and report. Preserve flushed player
   saves before moving them to inspect the stations again. Disconnect/save/stop.

The drivers include this run's absolute paths and private loopback ports 25585/25586.
They use only owned hidden Xvfb displays. Complete worlds stay in ignored runtime
storage; committed evidence contains logs, screenshots, native foundation/player
NBT and compressed native entity/block-region files. Passwords and full server
properties are excluded. Final cleanup records exited processes, closed ports and
byte-identical restoration of server settings.

```sh
python3 scripts/verify-additions-lifecycle.py docs/evidence/integrations/2026-10-01/additions-lifecycle
```

The audit checks hashes, exact live-log reports, persisted mechanical data, native
riders and seats, decoded station/fin/receiver block entities, unchanged settings,
wireless strength, timing, PAX denial, client telemetry/snapshot agreement, ordinary
unloading, process exits and regression results. Live carrier angles/angular
velocities are excluded from stable-data comparison; logout and deliberate
unloading changes are explicitly asserted. `artifact.json` records source and class
hashes. The successful packaged JAR SHA-256 is
`16eab3fe39fde5402d63243f05eabf4b6f9472803ab7966e35ce26e1c7871968`.

Pinned baseline: Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Flywheel 1.0.6, Sable 2.0.3, Coasters Simulated 0.1.5,
Fourth Dimension 0.9.0-dev.1. Additions 0.2.0 / CurseForge 8772429 requires the
Aeronautics 1.3.2 stack and Interiors 0.6.1v3. Combined adds Engineered 1.1.1,
Track Styles 1.2.0, Magnetized 1.1.0 and Immersion 0.2.3. Known dependency startup,
client-resource and transient loading warnings remain in raw logs; successful
startup does not mean warning-free startup.
