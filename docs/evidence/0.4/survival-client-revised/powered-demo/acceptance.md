# FD403 Revised powered example circuit

This is a connected survival-client checkpoint on the shipped `/fd demo revised`
circuit. It verifies occupied powered travel, four independent lower-hinge
carrier responses, recapture, unloading and a cold reopen. The command created
the route, car and motor; the separate [survival construction
checkpoint](../acceptance.md) covers normal crafting and placement. Together
these are bounded evidence, not a complete survival-built family walkthrough
or completion of FD403.

## Candidate and setup

- Source HEAD `26539fc`; packaged code `d0a3a92`, JAR
  `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
  `4a54b188d62331760ae5049b25425234ba6fb708a4e260df1f159d78c539b4ea`.
- Minecraft 1.21.1, NeoForge 21.1.248, standalone packaged profile. The
  copied survival world `FD403RevisedDemoPowered` has seed
  `1933455356643639169`, `GameType=0`, and commands enabled. The new plot is
  away from earlier family plots.
- The player used `/tp` to reach the plot and `/fd demo revised` to create the
  eight-section original-track powered circuit, car and native drive. The
  command is a fixture; it supplied Fourth Dimension content and does not
  establish survival acquisition or building on this route.

## Connected observations

1. The original seat accepted ordinary empty-hand right-click at survival
   reach ([boarded](revised-demo-board-emptyhand.png)). With the Rotation Tool,
   the mounted rider saw `Ready | Restraints open`, closed restraints
   ([closed](revised-demo-ready-closed.png)) and dispatched.
2. The rider started at `(404.50,-55.56,329.50)`, reached
   `(405.34,-49.75,360.39)` after approximately 20 seconds
   ([first sample](revised-demo-20s.png)), then
   `(428.50,-55.56,325.84)` after approximately 40 seconds
   ([far side](revised-demo-40s.png)). At the first sample, all four carriers
   were released with distinct angles and angular speeds; the occupied carrier
   `81b2b9c0` had angle `0.03319` and angular speed `-0.06445`. The saved car
   has four lower hinges (`child_pivot_offset=-0.5`). These measurements show
   powered travel over the climb and return side and independent lower-hinge
   response, without claiming a controlled rotation profile.
3. At approximately 60 seconds the rider was back on the first straight at
   `(404.50,-55.56,342.12)` ([return sample](revised-demo-60s.png)); the same
   carrier still reported one occupant and all four remained released. The
   Rotation Tool showed the moving state
   ([editor](revised-demo-moving-editor.png)). The rider requested capture;
   the car returned to `Ready | Restraints closed`
   ([captured](revised-demo-captured-editor.png)). Opening restraints and
   normal Shift dismount removed `RootVehicle`; all four demo carriers then
   reported `capture=true`, `occupants=0`
   ([unloaded](revised-demo-unloaded.png)).
4. The first client saved all dimensions and exited normally
   ([title](revised-demo-title-after-save.png)). A fresh process reopened the
   same world: the same four carrier identities remained captured and empty,
   and the player had no `RootVehicle`
   ([status](revised-demo-reopen-status.png)). That process also saved every
   dimension and exited normally
   ([title](revised-demo-reopen-title.png)). Decoded FoundationData retains
   eight valid connected demo rails and fault-free chassis
   `a649ce0b-aca6-4297-b7de-da70f0bfd4de`, with four empty, captured
   carriers and no rider-seat assignments.

The accepted run and reopen logs contain no invalid rider-seat assignment or
compatible-track endpoint fault. Sable's sublevel saves were slow, but both
client processes ended with `BUILD SUCCESSFUL`. [`verify.py`](verify.py) checks
the archived logs, screenshots, decoded state and world archive. This result
does not replace player-built powered Revised construction or the family
teaching walkthrough.
