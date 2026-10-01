# Combined pack: occupied PAX returning recovery with a late spectator

One real client rider survives restoration of an occupied returning PAX train in
the combined addon pack with Engineered energy required. The original rider, carrier
and local seat position are retained; explicit recovery completes one cycle and
normal restraint opening and Shift dismount clear every saved rider mapping.
Immersion uses the original carrier pose and resets its effects on dismount.
No production code or version pins changed for this run.

## Baseline and scope

This run copies the stopped, actively returning world from the
[Engineered player restart](../engineered-pax-player-restart/README.md), before that
run's recovery. The original preparation used normal seat interaction and the
Rotation Tool to close restraints and dispatch. The baseline has four linked PAX
cars, eight carriers, one occupied seat, an Engineered span/controller/observer,
the native carriage and 12,345 FE. Dev is a creative-mode rider; Observer is a
spectator. Health observations do not establish survival-mode fall safety.

The baseline process was **Engineered-only**. Three new packaged processes—the
restoring dedicated server and both clients—use Engineered 1.1.1, Track Styles
1.2.0, Additions 0.2.0, Magnetized 1.1.0 and Immersion 0.2.3, with Aeronautics 1.3.2
and Interiors 0.6.1. All use Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10,
Coasters Simulated 0.1.5 and Sable 2.0.3. The same packaged Fourth Dimension JAR
is used throughout; its SHA-256 is in `manifest.json`. The retained `server-*`
files describe the reused baseline, not a fourth newly executed process.

Each client runs on its own owned Xvfb display with software rendering, a 30 FPS
cap and five-chunk render/simulation distance. Default Sable UDP settings remain
unchanged. Saved camera configurations are included. This is not a performance
benchmark, a complete camera-mode test or proof of each addon's equipment behavior.

## Observed result

Dev joins the new frozen process before normal ticking resumes. Restoration creates
a new Create seat entity at the original local seat position on the original
carrier. The player UUID, carrier UUID and four-car consist are unchanged. No rider
teleport, force mount or player NBT edit is used. The on-disk before/frozen equality
is checked separately and does not by itself establish runtime restoration.

The winch enters `FAULT` with interrupted `RETURNING`. All cars brake with restraints
closed and carrier capture enabled. Simple resume and premature manual recovery are
denied. Observer joins 49 seconds after Dev, after the live restoration report.
Forward Engineered drive is selected during the fault. Once all cars are ready,
explicit recovery returns the train to the station; reverse LAUNCH 100 is selected
during capture. The winch keeps control and completes cycle 1. Every sampled report
preserves the original rider assignment and exact FE. The ready report follows the
initial fault report by 2,331 simulation ticks; samples are periodic, not every tick.
The empty carriage has zero measured drift across the retained fault samples.

Dev opens restraints in the normal Rotation Tool editor and presses Shift. The
Create occupancy sign changes from `1 / 16` to `0 / 16`. Final saved player data has
health 20, no RootVehicle, no no-gravity flag and zero fall distance. Every saved
occupancy list and rider-seat map is empty. Selected mechanical metadata and all
three rail records are unchanged.

The independent audit checks:

- All 183 Dev and 139 Observer received snapshots against server hashes, with 128
  identical snapshots shared by both clients.
- All eight original carriers and their parent chassis in both clients' final
  native diagnostics; Dev is mounted on the original carrier before normal exit
  and unmounted afterward.
- Saved versus restored local look, differing by about 0.00263° yaw and 0.00104°
  pitch. This does not cover all mouse-look, inversion or comfort settings.
- 599 mounted Immersion samples using the original carrier, with maximum normalized
  raw orientation error about 4.22e-8 radians. The observed mounted-to-unmounted
  transition clears carrier selection and body travel and restores ordinary pitch.
- Terminal exit 0 for the baseline and all three new processes, byte-for-byte
  restoration of private server settings, and termination of both owned displays
  after normal client quits.

## Retained limitations

Neither client records a missing-sublevel movement warning during this run. Observer
does record one `Received passengers for unknown entity` message at login. All
subsequent audited snapshots and final native carrier diagnostics pass; this run
does not resolve transport ordering issues from earlier evidence.

At server shutdown, **both clients log a Sable UDP keepalive NullPointerException**
because its channel is null. The traces point to
`SableUDPClientboundKeepAlivePacket.handleClient`, after UDP channel closure.
Both clients display the normal server-closed screen and subsequently quit with
exit 0. Full warning/error continuations are retained in their warning files.
This remains an unresolved shutdown limitation. Startup dependency/model/texture
warnings are also retained; successful recovery does not certify those assets.

Other occupied seats, combined-to-combined preparation/restart, other restart phases,
partly loaded routes, adverse networks, survival-mode safety, camera inversions and
mode/comfort extremes, removal, resource limits and broader FD305/FD901–FD904
acceptance remain open.

## Reproduction and audit

Follow the linked Engineered report to prepare and stop the original occupied
`RETURNING` world. Copy that stopped world into a new private runtime, require
Engineered energy, and start with `-PfdPackaged -PfdFrozen -PfdValidation` and
`-PfdIntegrations=engineered,styles,additions,magnetized,immersion`. Use the same
profile and JAR for separate `runClient` and `runObserver` processes.

Join Dev first, unfreeze, report restoration and inspect the rider's RootVehicle,
look and health. Request resume and early recovery to verify denials. Select forward
drive, then join Observer later. Follow `recover.py.txt` to wait for actual readiness,
request recovery and apply the opposing capture command. At the station, open
restraints through the editor and press Shift. Inspect the sign and player state,
save and stop, then quit both clients normally.

The archived drivers contain local paths and are investigation aids, not a portable
runner. `commands.txt`, process event/version/warning excerpts, screenshots, selected
save/player data and manifest hashes preserve this run. Audit it with:

```sh
python3 scripts/verify-engineered-pax-player-restart.py docs/evidence/integrations/2026-10-01/combined-pax-player-restart --combined
```
