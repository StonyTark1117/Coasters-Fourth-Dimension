# Fresh occupied PAX normal restoration and restart — bounded PASS

Executed November 2, 2026 (America/Phoenix; November 3 UTC), exact source
`8a7ffdbfa7b77c58230d997da47d9cc0e1ee75f4`. Packaged dev.27 JAR SHA-256:
`bb694007d157faa53eb7376de9d2558280239557e87081021cea579bf2dc0cba`.
Standalone Minecraft 1.21.1, NeoForge 21.1.248, schema 6/protocol 7. The local
build passed in four seconds with one executed task, three from cache and two
up-to-date. The release coordinator owns the separate 49-test native-suite
result; this record is the subsequent packaged real-client operator run.

**Result:** a fresh occupied four-car PAX train survives normal station-curve
removal, normal track-item exact restoration, explicit Repair and Realign, a
clean cold dedicated-server restart, two-client reconnect, a normal powered
cycle and ordinary unloading. All four chassis, eight carriers, original
logical rider/seat assignment, profiles and stored geometry fingerprints persist.
This does not establish migration or exact restoration for older worlds lacking
saved native-curve provenance, and does not close the complete FD902/0.9 gate.

## Fixture and normal operator sequence

The fresh creative flat world is `pax-curve-restore-8a7ffdb`, configured seed `0`.
`input-world.json` records setup commands. The actual demo command is
`execute positioned 512 80 0 run fd demo pax`, which authors a compact PAX example
at `(512,85,65)` and assembles its four cars after four seconds. The two station
anchors are `(512,85,65)` and `(512,85,97)`; winch `(506,85,73)`, motor `(505,85,73)`
at 128 RPM. Three isolated smooth-stone operator footholds were command-placed.
No historical world was modified or substituted. This is a command-authored
fixture with normal operator actions, not survival crafting/building evidence.

Launch the exact built JAR using `runServer`, `runClient`, `runObserver` with
`-PfdPackaged -PfdValidation`; clients use `-PfdConnect=127.0.0.1:25565`.
Both real clients run on owned Xvfb :121; the other agent's :99 is untouched.

1. Dev boards the fourth car's second carrier with an empty hand, then normally
   opens the Rotation Tool and closes restraints. Initial pitch -5 from
   `(505.5,85,91.5)`, yaw -90 misses; pitch **15** boards successfully in this fresh
   fixture. The logical seat is `274877911039`; the rider is Dev.
2. Observer normally mines the station curve from `(509.5,85,80.5)`, yaw -90,
   pitch 12, holding attack about 0.8 seconds. Both anchors and the other two
   spike connections survive. All four cars become stopped/restrained/captured,
   faulted with Matching track support lost and requiring realignment.
3. Initial overhead clicks at A and B do **not** select/reconnect the anchors.
   The observation named `normal-track-item-restored` is a failed attempt despite
   its label. The following `normal-repair-action` correctly rejects with
   “Restore the original matching track section first.” These records are kept.
4. Observer stands at `(511.5,85,65.5)`, yaw -90, pitch 55 and normally uses the PAX
   track item on A; its selection prompt and item glint appear. The equivalent
   position `(511.5,85,97.5)` selects B and places the missing connection. The full
   native station connection, including starts, axes, normals, primary flag,
   material and both handle lengths, now equals the initial connection exactly.
   Original rail profiles, stored fingerprints and provenance remain intact;
   normal item replacement gives the station rail a new ID/revision. Faults and
   pending alignment deliberately remain until explicit operator actions.
5. Dev's normal **Repair matching track** action clears all four support faults,
   leaving alignment required. Observer's normal empty-hand winch start is
   rejected with guidance to use Realign after Repair. Dev then normally selects
   **Realign matching track**: all four alignment flags clear, same rider remains
   mounted, and the winch stays Boarding without automatically starting.
6. Record occupied aligned state, `save-all flush`, `stop`, and wait for actual
   process exit 0. Copy the whole stopped world before a fresh dedicated JVM
   starts on the identical JAR and world. The winch was Boarding, not actively
   lifting, and had not yet created a native lift carriage.
7. Reconnect Observer first: live goggles show 0/16 while Dev is offline and its
   logical reservation remains saved. An old on-disk RootVehicle alone is not a
   live mount. Reconnect Dev normally: the server queues restoration with deadline
   **5637** and mounts at game tick **5438**, one tick after the window began.
   Fresh motion query, saved player mount, client diagnostics and goggles agree.
   All four original cars/eight carriers, repaired native geometry, profiles,
   winch lead/car list/route and rider mapping survive. Native Create seat-entity
   UUID is recreated; no UUID-continuity claim is made for that transient entity.
8. After the winch has ticked and registered, goggles show aligned/ready and
   1/16. Normal empty-hand winch use starts Lifting, creates its first carriage,
   and completes the lift/release/gravity return/capture cycle. Follow-up observes
   Capturing; then Boarding with cycle count 1. Normal Rotation Tool Open
   restraints followed by Shift unloads Dev: zero assignments, no RootVehicle,
   goggles 0/16, all four cars still present and fault-free.
9. Final save/flush and stop exit 0. Both clients return through menus and Quit
   Game, each exiting 0. Owned Xvfb is terminated after zero remaining windows.
   Full pre-restart and final stopped worlds remain at `input-world.json` paths.

All teleport setup affected only unmounted operators. No mounted-player teleport,
curve/vehicle NBT write, velocity injection, direct mount, direct repair or
validation dispatch was used. The motor stayed at the demo's 128 RPM throughout.

## Evidence and limits

Nineteen observations retain failed attempts alongside successful actions.
`attempt-labels.json` disambiguates labels. Raw stopped foundation and winch-chunk
NBT, decoded state, selected player state, full compressed server/client/build
logs and screenshots are included. Dependency identities/hashes are in
`artifact.json`; `manifest.json` hashes every bundle file except itself.

- One stopped-support-loss case, one fresh fixture, one fourth-car rider and the
  standalone profile. This does not cover active-lift support loss, all seats,
  every phase, repeated interruptions, alternate geometry, optional addons,
  recovery towing handover, or full rendered/continuous swept clearance.
- The first cold restart is **after** successful repair/realignment. It does not
  establish restart while support is missing or while alignment is pending.
  The earlier active-Lifting reload checkpoint is separate evidence.
- This version captures curve provenance on newly authored tracks. Pre-provenance
  worlds and any later lazy-backfill patch require their own exact-artifact tests.
- The generic placement confirmation still says to edit the curve and rebind its
  profile even after occupied restoration. That is misleading in this path:
  follow Repair then Realign, as the operating guide requires. This teaching/UI
  issue was reported to the coordinator; no source fix is claimed here.
- Existing upstream model/mixin/dist-cleaner/LAN warnings remain in full logs.
  Observer's first disconnect screen reports a connection reset; server save and
  actual exit 0 establish a normal shutdown. No warning-free or performance claim.

Run `python3 verify.py` here to validate geometry/provenance, state ordering,
identities, rider restoration deadline, cycle/unload, shutdown and hashes.
The verifier checks captured evidence; it does not substitute for a later
candidate replay or certify the broader release gate.
