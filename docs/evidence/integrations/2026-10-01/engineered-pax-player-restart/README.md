# Engineered PAX: occupied returning restart with two packaged clients

One actual Minecraft client player, Dev, survives a cold dedicated-server restart
while riding the returning PAX train with Engineered energy required. Dev reconnects
first and automatically recovers the original carrier and local seat assignment.
Observer reconnects later as a spectator. Explicit recovery completes one cycle;
normal restraint opening and Shift dismount clear the saved rider mapping. No
production code changes were needed for this run.

## Fixture and observed recovery

The packaged JAR is the passenger-loading fix tested by the
[seven-profile native matrix](../passenger-entity-loading/README.md). All four
processes load Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Coasters 0.1.5,
Sable 2.0.3 and Engineered 1.1.1. The other optional addons are absent. Both clients
run on separate owned Xvfb displays with software rendering, a 30 FPS cap and
five-chunk render/simulation distance. The host desktop is not used.

The opt-in `pax_energy_restart` fixture creates four coupled cars, eight rotary
carriers, a powered winch, real Engineered span/controller/observer and 12,345 FE.
Its `board` action is **not** used: Dev right-clicks the native seat, closes restraints
and dispatches through the normal Rotation Tool editor. There is one occupied seat,
not sixteen. Observer watches in spectator mode. A Create Display Link on the
winch drives an occupancy sign.

The operator selects STOP on outbound and rolling TRIM 1 on return, then freezes,
records and stops the server while the train is moving in `RETURNING`. Every sampled
lift/outbound/return report retains Dev on the original carrier. The stopped world is
copied before a new process starts. The before/frozen on-disk FoundationData reports
are identical; this alone does not establish runtime restoration. The subsequent
live reports and client data provide that evidence.

Dev joins the frozen server first, then normal ticking resumes. Vanilla's saved
RootVehicle is deferred by the existing player restoration path, which creates a new
Create seat entity at the same local position on the original carrier. No operator
force-mount, rider teleport or rider NBT edit is used during recovery. The actual player UUID, carrier,
seat position, original carriage, all four native consist memberships, selected
controller/observer/span configuration and exact FE are preserved. The new seat
entity UUID is expected to differ.

The winch remains faulted with interrupted `RETURNING`; all cars brake with restraints
closed and every carrier captured. Simple resume and premature recovery are rejected.
A forward Engineered command cannot take ownership. Observer joins after the rider's
restoration report, while the fault is still active. Once every car reports readiness,
explicit manual recovery engages the catch and returns the train to the station,
with reverse Engineered LAUNCH 100 opposing capture. One cycle completes. Dev opens
restraints through the editor and presses Shift to dismount normally.

## Independent audit

- All 489 Dev and 415 Observer received validation snapshots match corresponding
  server hashes; 398 snapshots are identical on both clients.
- Both clients' final native diagnostics contain all eight original carriers and
  their parent chassis. The rider's client reports the occupied original carrier
  before exit and an unmounted player afterward.
- Saved RootVehicle local seat coordinates match the restored live seat. The
  recorded local look differs by about 0.0874° yaw and 0.00119° pitch. This is a
  bounded look check, not complete camera-mode or inversion acceptance.
- The fault report precedes the ready recovery sample by 2,754 simulation ticks.
  Intermediate reports are periodic; no every-tick packaged guarantee is claimed.
  The empty carriage has zero measured drift across the retained fault samples.
- Final saved player data has health 20, no RootVehicle, no no-gravity flag and
  zero fall distance. Every saved rider-seat map and occupancy list is empty.
- Selected mechanical metadata and all three rail records are unchanged. Intentionally
  changing operating state, native angles/velocities and emptied rider fields are
  checked separately or excluded from the mechanical comparison.
- The Create sign changes from `1 / 16` to `0 / 16`.
- Both server processes and both clients exit 0. Private server settings are restored
  byte-for-byte; both owned displays are stopped after their clients exit.

Dev logs thirteen transient Sable “movement packet for a non-existent sub-level”
messages; Observer logs none. They are retained with startup dependency warnings.
Restoration completes, but this does not resolve the underlying transport ordering
issue documented in the earlier transport comparison. These clients do not reproduce
the earlier resource exit; its cause is not established by their successful exits.

One spectator teleport overlapped a background read-only report and its RCON reply
included the report acknowledgement. Both commands completed and the structured report
was independently located by its unique label. No ride operation was affected. The
local RCON helper subsequently uses a file lock to serialize requests; the original
command transcript is preserved. Only the unmounted spectator was repositioned during
recovery. Dev was not teleported while mounted or restored.

## Reproduction

Use Java 21 and the pinned packaged artifact. Prepare the isolated runtime with
Engineered `requireEnergy=true`, `-PfdPackaged -PfdFrozen -PfdValidation` and
`-PfdIntegrations=engineered`. Use separate `runClient` and `runObserver` processes
with the same JAR/profile and `-PfdConnect` pointing at that private server.

1. Create a fresh PAX fixture; step 100 ticks; assemble, equip and charge, without
   the pig-boarding command. Unfreeze. Give Dev the Rotation Tool and goggles.
2. Position Dev beside the first seat at `-605.5 181 608.5`, facing east and down
   about 40°. Right-click the seat, select the tool, then use Close restraints and
   Dispatch. Each editor action closes the screen; do not click Done afterward.
3. Follow `prepare-returning.py.txt` and the command transcript to sample motion,
   choose the controller commands and stop during actual returning motion. Copy the
   stopped world and start a new frozen server process.
4. Reconnect Dev through the normal multiplayer UI, unfreeze, and inspect the live
   restored rider/seat and fault. Request resume and premature recovery to verify
   denials. Reconnect Observer later, without changing Dev's position or mount.
5. Follow `recover.py.txt` to wait for actual readiness and request explicit recovery.
   At boarding, open restraints in the editor and press Shift. Inspect the sign,
   player state and saved rider maps. Save and stop; quit both clients normally.

The archived drivers contain local runtime paths and are investigation aids, not a
portable one-command runner. Screenshots capture the editor, ride, disconnect,
restoration and normal exit. `manifest.json` records complete log paths/hashes,
loaded artifact hash and terminal exits. Exact commands, loaded versions, warnings,
client/server events and selected player/save records are retained alongside them.

```sh
python3 scripts/verify-engineered-pax-player-restart.py docs/evidence/integrations/2026-10-01/engineered-pax-player-restart
```

This certifies only this bounded Engineered-only returning restart with one real
client rider and one late spectator on one host. Other occupied seats, restart phases,
combined-pack FE player recovery, adverse networks, resource limits, full camera
behavior and broader FD305/FD901–FD904 acceptance remain open. This is not a rendering
performance benchmark.

The later [combined-pack recovery](../combined-pax-player-restart/README.md) reuses
this run's stopped occupied baseline and verifies restoration with all five addons,
default Immersion camera tracking and normal dismount. It retains an unresolved
Sable UDP keepalive exception at server shutdown and does not close the wider matrix.
