# Occupied PAX reconnects with the Sable queue guard

This follow-up runs the unchanged guarded artifact from the
[UDP lifecycle checkpoint](../udp-lifecycle/README.md) with a real player, closed
restraints and a second client remaining online. It covers two ordinary menu
disconnects: one while braking after a RETURNING cold restart, and one during a
new powered LIFTING phase. It adds acceptance evidence, with no production code
change or new claim for other families, packs or interruption phases.

## Procedure and result

The owned combined-pack dedicated server starts from a copy of the original
[Engineered occupied RETURNING baseline](../engineered-pax-player-restart/README.md).
Engineered requires energy. The server starts frozen so Dev can log in before
physics advances, then unfreezes. The original four cars, eight carriers, native
carriage, rider and controller/observer configuration restore. The winch enters
its expected RETURNING recovery fault with restraints retained and native braking
active. Observer joins later and remains connected through both Dev reconnects.

1. Dev disconnects using Escape → Disconnect while mounted. Observer stays online;
   the simulation keeps running. Live occupancy becomes zero, while a flushed
   schema-5 save retains Dev's original carrier and local seat reservation. Dev
   reconnects through Multiplayer → Direct Connection after about 50 seconds.
   Occupancy returns to one and the original seat is restored without a duplicate
   rider. The native seat entity itself has a new UUID, which is allowed.
2. Once the train and carriers meet normal readiness checks, the operator calls
   explicit winch recovery through the existing validation command. The train
   recaptures and returns to BOARDING, advancing the cycle count from zero to one.
3. The existing diagnostic dispatch command calls normal `VehicleOperations.dispatch`.
   Dev disconnects through the game menu during LIFTING and reconnects about three
   seconds later, still during LIFTING. Samples show the mounted train moving above
   0.8 m/s before disconnect and above 3.7 m/s after restoration. Offline occupancy
   again drops to zero, and the flushed save retains the original seat reservation.
4. The occupied train continues through OUTBOUND, RETURNING and CAPTURING and returns
   ready to BOARDING at cycle two. Engineered's opposing motor sequence is selected
   during capture through its existing fixture command. External drive remains
   rejected throughout, and the original **12,345 FE** remains unchanged.
5. Dev opens restraints with the normal Rotation Tool editor, then dismounts with
   Shift. All restraints open, occupancy and saved seat reservations clear, health
   stays at 20, and the player reaches the same safe station exit. The player has
   no RootVehicle or temporary no-gravity state and zero fall distance.

No mounted-player teleport, forced mount, rider NBT edit or synthetic UDP probe is
used. Only Observer is positioned with an operator teleport. The copied baseline
contains one human rider, not the fixture's sixteen pig passengers. Reports use the
normal runtime state and native seat entities. Full local launch/recovery scripts,
commands and logs are retained under `build/occupied-reconnect`; selected audit
inputs and screenshots are committed here. `lift-reconnect.py.txt` records the
timed ordinary GUI actions; `recover.py.txt` and `cycle.py.txt` record phase checks.

## State, view and synchronization checks

The strict audit checks every recorded report for the exact original four chassis,
eight carriers and carriage, four-car consist membership, closed restraints until
normal unloading, occupancy matching connection state, and exact FE. Saved rails,
functional family/variant and mechanical fields match the original schema-5 save.
The two offline saves preserve the same rider-to-carrier/local-seat reservation.
Every mounted player save uses the original native local seat position.

Local view drift is under 0.005° immediately after the first reconnect and under
0.23° after the later lift reconnect and completed cycle. The latter is a measured
tolerance, not a claim of bit-exact view preservation. Across **792** mounted camera
samples, Immersion's raw carrier orientation differs from the authoritative native
rendered carrier pose by at most **4.22 × 10⁻⁸ radians**. After dismount, the camera
has no carrier context, travel is zero and camera-up matches ordinary player pitch.
This checks carrier selection and restoration in the configured cinematic mode;
it does not certify every comfort mode, inversion or frame of interpolation.

All **243 Dev** and **276 Observer** Fourth Dimension snapshots match a corresponding
server-emitted hash, with **212** shared snapshot states. Both clients retain all
eight native carriers at the end. The guarded queued-packet exception and
NullPointerException do not occur during either reconnect or final server stop.
The server and both clients exit normally with status zero. The original server
properties and Engineered configuration are restored byte-for-byte; owned displays
are stopped only after their clients exit.

## Retained limitations

- Observer logs one `Received passengers for unknown entity` warning during its
  initial late join. It subsequently receives authoritative state and all carriers;
  the warning remains in `observer-warnings.txt`.
- Dev displays a TCP `Connection reset by peer` exception at final server shutdown,
  accompanied by Minecraft's `Negative index in crash report handler` log. Observer
  displays `Server closed`. Both clients still exit zero. Screenshots and warning
  traces preserve the distinction from the queued UDP keepalive failure. This run
  does **not** establish universally clean shutdown presentation or adverse-network
  behavior.
- Existing addon startup model/texture/shader and dedicated-side mixin warnings
  remain in the collected warning files.
- Live reconnects during OUTBOUND, RETURNING or CAPTURING, simultaneous rider
  reconnects, all-family/all-pack combinations, long loading delays, wire-level
  packet loss/reordering and wider FD902–FD904 acceptance remain open. The first
  reconnect here occurs in a RETURNING **fault**, not a fresh gravity RETURNING phase.

## Reproduction and artifact

From the repository root:

```sh
python3 scripts/verify-occupied-pax-reconnect.py docs/evidence/integrations/2026-10-01/occupied-pax-reconnect
```

The artifact is source commit `23b4fa2`, locally retained as
`/home/braydon/.cache/fourthdimension-48535694/udp-lifecycle-fixed.jar`, SHA-256
`daf8e9c039240c6bffa96e49f946d396934c334808c4b2fc31b4dedc0177fa81`.
The full loaded stack is recorded per process: Minecraft 1.21.1, NeoForge 21.1.248,
Create 6.0.10, Coasters Simulated 0.1.5, Sable 2.0.3, Engineered 1.1.1, Track Styles
1.2.0, Additions 0.2.0, Magnetized 1.1.0 and Immersion 0.2.3 with its dependencies.
Both clients enable validation logging and omit the deliberately disconnecting
`fdUdpProbe` flag. Client Immersion configs are included.

The preceding artifact checkpoint supplies its 19 unit tests and base/combined
33-registered native suites. Those are not rerun or presented as new tests here:
this checkpoint changes only evidence, documentation and its offline audit script.
