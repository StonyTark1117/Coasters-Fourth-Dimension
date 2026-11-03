# Occupied PAX active-lift cold restart — bounded PASS

Executed November 2, 2026 (America/Phoenix; November 3 UTC) on exact checkout
`7958cdd5ffb5456978f7f9b55e73c5ff5ded7286`, whose production source is
`a1aeb1a6fc1a1739efc03cede059b0c118879bf7`. Packaged dev.27 JAR SHA-256:
`2a52a641f13ef78fa3c23c2fe466ff22bd359d3af33c4fb26b0f3773379285a2`.
Standalone Minecraft 1.21.1 / NeoForge 21.1.248, schema 6 / protocol 7; exact
inspected dependency hashes are in `artifact.json`. Both real clients used the
owned hidden Xvfb display :121. The other agent's display :99 was untouched.

This proves one real dedicated-server process restart during **active LIFTING**,
followed by two-client reconnect, explicit normal resume, completed shuttle cycle,
and ordinary unloading. Unlike the earlier power-loss checkpoint, this world was
saved with an active phase and no pre-existing shuttle fault. No integration scope,
source code, dependency pins, or release-gate completion status changes here.

## Reproduction and observations

1. Copy the complete clean stopped `311078e/after-clean-stop-world` described in
   `input-world.json`. Its four-car PAX train is unoccupied in Boarding, with one
   historical completed cycle. Two historical Arrow cars also exist, so global
   availability is six chassis and twelve carriers; PAX contributes four and eight.
2. Build using Java 21 and `./gradlew build --offline --no-daemon --console=plain`.
   Launch `runServer`, `runClient` and `runObserver` with `-PfdPackaged -PfdValidation`;
   clients also use `-PfdConnect=127.0.0.1:25565`. Do not load loose mod classes or
   optional addons. Server world folder is `pax-active-restart`.
3. While unmounted, position Dev at `(505.5,85,91.5)`, yaw -90, pitch -5. Normal
   empty-hand use boards the fourth car's second carrier, logical seat
   `274877911039`. Use the Rotation Tool's Close restraints button. Observer
   watches the winch at `(506,85,73)`; its source motor is `(505,85,73)`.
4. Set 16 RPM through Create's normal motor control. Earlier exploratory UI
   adjustments reached 120 and 15 RPM; the 15 RPM reading correctly denied
   readiness and no lift began there. Normal empty-hand winch use starts Lifting.
5. Record active state, `save-all flush`, then `stop`. Wait for actual process exit
   0. Preserve the complete stopped world before a fresh server JVM launch.
   The stopped winch is still LIFTING, fault empty, distance
   `61.07979295672889`; all PAX cars have stopping false and restraints/capture true.
6. Reconnect Observer first: goggles show Fault, 0/16 live riders, restraints
   closed and dispatch denied. Saved logical rider reservation still exists, but
   Dev is offline (`devMotion` says no player); its old saved RootVehicle is not
   evidence of a live mount. Every PAX car now has stopping/restraints/capture true
   and the explicit reload fault. Pending retention has been consumed.
7. Reconnect Dev: server log records restoration at gameTick **61754**, deadline
   **61953**, with accepted seat assignment. The live motion query, client logs,
   fresh player save and 1/16 goggles agree. All original four chassis, eight
   carriers, logical rider seat, winch lead/car list and carriage IDs remain.
8. Leave the powered winch untouched: the three fault observations span **636
   ticks**, with exactly the stopped winch distance and no automatic resume.
   Normal winch use explicitly resumes Lifting; normal motor UI then sets 127 RPM
   within its allowed range to finish the cycle. While Returning, Dev's Shift
   dismount is rejected with restraints closed. The train returns to Boarding,
   cycle count increases from 1 to 2, and the fault clears.
9. Normal Rotation Tool Open restraints followed by Shift unloads Dev. Both
   logical assignments and RootVehicle are absent, goggles show 0/16, and the
   final stopped world retains the same consist/controller identities. Stop
   normally; exit both clients through Quit Game. Both server runs and both
   clients exit 0. Xvfb is terminated only after it has zero client windows.

The pre-stop live observation precedes the final shutdown save, so its distance
is slightly earlier; the **stopped winch NBT** is the restart comparison baseline.
Raw foundation and winch-chunk NBT, decoded JSON, twelve observations, screenshots,
full compressed logs, action record, input provenance and shutdown record are
included. The two complete world backups remain local at the paths in `actions.json`.

## Limits

- This is one active LIFTING restart, one rider on the fourth car, two real clients,
  one fixture and the standalone profile. It does not prove all active phases,
  chunk interruption, every seat, repeated restarts, multiplayer races, recovery
  handover, external propulsion or optional integration profiles.
- Native Create seat-entity UUID **changes** on reconnect. Logical carrier/seat and
  rider identity persist; this checkpoint does not claim native seat UUID continuity.
- Stable winch distance is not mathematical zero physics velocity: tiny native
  solver residuals are visible in the motion samples. The result proves retained
  state and no phase/distance advancement during the sampled fault interval.
- This does not close the separate normal PAX support-restoration geometry blocker,
  rendered/player-mesh/continuous-sweep clearance, FD403 survival teaching, or the
  complete FD902/0.9 gate. The coordinator owns the separate native suite evidence;
  this bundle does not claim a fresh local GameTest run.
- Logs retain existing upstream mixin/dist-cleaner, offline fixture and LAN warnings.
  One client reports a connection reset during the first normal shutdown; the
  server logs and actual process result establish clean server exit. No warning-free
  claim or crash attribution is made.

Run `python3 verify.py` in this directory. It checks the recorded identities,
state transitions, logical mount continuity, restore deadline, normal unload,
clean shutdown logs and complete SHA-256 manifest. It verifies captured evidence;
it does not replace rerunning the real operator scenario on a later candidate.
