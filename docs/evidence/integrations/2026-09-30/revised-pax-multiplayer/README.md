# Revised ZacSpin and PAX: occupied combined-pack restart and riding

Two local packaged clients ran against a separate packaged dedicated server with
all five optional adapters. Dev rode one revised ZacSpin car (eight seats); Observer
rode the lead carrier seat of the four-car PAX train (sixteen seats). There was one
human rider per ride. This adds bounded occupied schema-5 coverage for the two
families absent from the earlier Arrow/legacy/Free Spin combined-pack sessions.
No production Java or dependency versions changed for this acceptance run.

The exact packaged JAR hash and successful process exits are in `manifest.json`.
Every process records its loaded mod list: Minecraft 1.21.1, NeoForge 21.1.248,
Create 6.0.10, Coasters 0.1.5, Sable 2.0.3, Track Styles 1.2.0, Engineered 1.1.1,
Additions 0.2.0, Magnetized 1.1.0, Immersion 0.2.3, Aeronautics 1.3.2 and Interiors
0.6.1. Build coordinates remain pinned in the repository. Immersion used its
enabled camera, anchored head and 40 ms inertia; retained client configs identify
the remaining settings.

## Successful clean run

- Both players boarded through the native seat interaction, selected the Rotation
  Tool and closed restraints through the server-validated editor. Create Display
  Links reported `1 / 8`, `1 / 16`, then `Ready to dispatch` for both rides.
- The server froze, flushed its save and exited normally. A new frozen server
  process loaded the same world. The complete decoded foundation SavedData was
  identical before either player rejoined: 35 rail sections, eight vehicles and
  twenty carriers, including both occupied seat assignments.
- Dev rejoined first; the server resumed ticking; Observer joined several seconds
  later. Both reattached without forced mounting or teleportation. Mechanical and
  rider fields remained identical; only carrier angle/omega settled after ticking.
  All three native PAX couplings were restored between the original four cars.
  The native seat entity may be recreated; the saved carrier and seat assignment
  are the stable identity being checked.
- Both riders dispatched through the editor. Revised ZacSpin left the station,
  completed its native demonstration circuit, and returned to the powered boarding
  station. PAX lifted, released, ran outbound, returned, captured and reached
  `BOARDING` with `shuttle_cycles: 1`. The Create winch display reported each sampled
  phase. All five participating vehicle records were restrained and fault-free
  while running, then stopped and boarding-ready at return.
- Mouse movement changed the local look yaw. Opening restraints and pressing Shift
  dismounted both players normally. Both occupancy displays reached zero, all
  participating saved rider/seat maps emptied, and camera diagnostics returned to
  `body=none`, zero ride travel and the vanilla pitch/up relationship.

The verifier checks 232 Dev and 228 Observer received snapshots against server
hashes, with 209 snapshots identical on both clients. It checks 691 and 677 mounted
camera samples respectively. Maximum raw pose error is below 4.3e-8 radians;
pre/post restart camera orientation differs by about 0.00686° and 0.00482°.
Minimum carrier-up values were 0.944 and 0.718, so **this run does not prove full
inversion**. Screenshots show boarding, the closed-restraint editor, restored views,
running/capture views and normal exits.

## Invalid first attempt retained

`invalid-*` files preserve an earlier attempt that must not count as a passing
cycle. The automation clicked an action, then clicked the old Done location after
the editor had already closed. `RotationEditorScreen.addButton` closes the screen
on each action, so that second click became a creative-mode world interaction.
PAX subsequently lost a native coupling and faulted with a changed-consist message.
There was no logged tension snap. Accidental running-gear damage is the likely
cause; the precise destroyed block was not independently captured.

The copied baseline also had revised ZacSpin's Additions auto-dispatch enabled.
That station captured the manually dispatched car again. In the clean repeat it
was explicitly set to `Dispatch:0b`, retaining the station but leaving dispatch
ownership to our native boarding circuit. This run therefore does not certify
Additions automatic dispatch for revised ZacSpin. A fresh complete baseline copy
was used, rather than repairing the damaged world and calling it an original pass.
Only one click was issued per editor action in the successful repeat.

## Reproduction

1. Prepare a complete copy of the five-family parked baseline described in
   [the removal matrix](../removal-matrix/README.md). Revised origin is `(0,180,0)`;
   its station is `(-6,180,20)`. PAX origin is `(200,180,0)`, with winch
   `(194,180,8)` powered by the 128 RPM creative motor at `(193,180,8)`.
2. Start the packaged server with Java 21, `-PfdPackaged`, the pinned JAR,
   `-PfdFrozen -PfdValidation` and
   `-PfdIntegrations=engineered,styles,additions,magnetized,immersion`.
   Start `runClient` and `runObserver` with the same pack and validation flag,
   pointing `-PfdConnect` at the test server. The recorded clients used separate
   isolated Xvfb displays, 30 FPS and five-chunk render/simulation distance.
3. Follow `setup.txt` to disable revised Additions auto-dispatch and place the two
   Create Display Links/signs. Source serialization uses capital `Id`. Teleport
   only before boarding: Dev to `-5.5 180 17.5 -90 -12`, Observer to
   `194.5 181 8.5 -90 15`. Select the empty hand and right-click the visible seat.
   Select the Rotation Tool and right-click, then click Close restraints **once**.
4. Follow `occupancy.txt`, `ready.txt`, `stop1.txt` and `restarted-frozen.txt`.
   Decode each flushed `data/fourthdimension_foundation.dat` with
   `scripts/inspect-foundation-save.py`; compare the complete before/frozen saves.
   Rejoin Dev, resume ticking, then rejoin Observer. Do not force mounts or teleport
   players during recovery. Verify both native RootVehicles and ready readouts.
5. Open each mounted editor and click Dispatch **once**. Select the empty hand
   using a hotbar key. After revised ZacSpin is confirmed outside station range,
   power `(-6,180,21)` for its return capture. Observe the PAX phase and cycle
   counter. No PAX motor commands, injected velocities, recovery commands or
   manual capture were used during the successful ride.
6. At the returned ready state, open restraints through each editor, then Shift
   to dismount. Save the final data, stop all test processes normally and restore
   the test server properties. Both server processes and both clients exited 0;
   their owned displays were stopped.

```sh
python3 scripts/verify-revised-pax-multiplayer.py docs/evidence/integrations/2026-09-30/revised-pax-multiplayer
```

`verification.json` is the reproduced output. Client event excerpts are bounded to
21:26–21:31 of the clean session; full source-log hashes are retained. Startup
dependency client-class warnings and transient Sable “movement packet for a
non-existent sub-level” errors during rejoin remain in the warning files. Recovery
completed, but their underlying cause remains an open investigation.

This is one successful circuit/cycle per family with one occupied seat, two clients
on one host and the combined pack. It does not prove full loading, inversion,
all camera modes, live chunk interruption, occupied addon removal, individual-addon
multiplayer, rendered equipment coverage or measured performance. Engineered and
Magnetized were loaded but no active spans from those addons were added to these
two routes. The full FD901–FD904 release gates remain open.
