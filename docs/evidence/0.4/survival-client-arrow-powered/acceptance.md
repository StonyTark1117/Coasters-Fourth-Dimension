# Arrow connected survival client: bounded powered pass

**Result:** A normally crafted and placed original Arrow car carried one rider under
Create motor power along a 50-block original Arrow rail, completed its authored
360-degree controlled turn, captured before the route endpoint, unloaded, and
survived a cold reopen. This is one occupied, one-way pass; it does not close
the full five-family FD403 gate or prove a complete Arrow circuit.

## Artifact and setup

- Source code: `a6e18c906a8e1f9f33880030dccd7b0be1b53b16` (the later
  `2d2e5bb` commit only added teaching evidence). Packaged JAR SHA-256:
  `34c2d6ce14fecaf9941303ff950ec9f8ef586a61456669a0d404b0be43672db1`.
- Minecraft 1.21.1, NeoForge 21.1.248, Fourth Dimension 0.9.0-dev.27.
  The connected survival world was a separate copy of the earlier FD403 PAX
  acceptance world, named `FD403ArrowPoweredRetry`; its saved copy is
  [`world.tar.zst`](world.tar.zst). Earlier vehicles were left in that copy;
  the new Arrow plot was at x=230, z=20–70.
- Commands supplied only upstream ingredients: 10 iron ingots, 2 copper,
  2 Create red seats, 1 iron bars, 1 native Coasters cart, 1 crafting table,
  2 native anchors, a fresh Minecraft Chain, 4 stone supports, and an upstream
  Create creative motor. The copy already held 12 crafted Arrow track and red
  dye. No Fourth Dimension output item was granted. This proves client result
  slot crafting from supplied ingredients, not material gathering.

## Connected actions and observations

1. The player took 16 Arrow track and one Arrow car from ordinary crafting
   result slots ([track](track-crafted.png), [car](car-crafted.png)). They
   placed two native anchors at `(230,-60,20)` and `(230,-60,70)`, joined them
   with matching crafted track, and spent 13 track items. The placed rail is
   original, valid, 50 blocks long ([route](route-placed.png)).
2. The player installed a native Coasters Chain lift over the route by normal
   Chain clicks; its committed length was `50.0d` ([chain](chain-installed.png)).
   The Rotation Tool profile was `0:0;15:360;50:360`, saved as `0, 2π, 2π`
   ([editor](profile-360.png)). The crafted car was placed on the route via
   ordinary item use ([car](car-placed.png)).
3. The player boarded its original Create seat, closed restraints, and
   dispatched with the mounted Rotation Tool. The motor powered the start
   anchor at `16.0f`. Connected player z samples advanced from `25.3918` to
   `33.6326`, then `54.8386`. At the first live sample, one original carrier
   was occupied and both were uncaptured at about `6.0772` radians. At the
   later sample, the occupied carrier reached about `6.2828` radians, close
   to a full turn ([ready](ready-closed.png)).
4. The player captured before the route end, settling near z=`55.5093`.
   The mounted editor reported Ready with no fault
   ([captured](captured.png)). The player opened restraints, dismounted, and
   `RootVehicle` was absent. Both original carriers then reported capture=true,
   occupants=0, angles near `2π` ([open](restraints-open.png)).
5. Normal Save and Quit saved all dimensions. The cold reopened world retained
   the 50-block Chain, start-anchor speed 16, rail/profile, chassis and both
   carrier IDs, and a captured, unrestrained, fault-free car. The player boarded
   an original seat again, saw `Ready | Restraints open`, dismounted and saved
   normally ([reopen](reopen-ready.png)). Both runs ended `BUILD SUCCESSFUL`.

The first 35-block attempt in `FD403ArrowPowered` is retained as a negative
case. Its saved car reported `End of compatible track; capture and inspect the
route` after reaching the endpoint. A proposed extension of an occupied older
section was correctly rejected by the connected-track occupancy guard.
The fresh 50-block retry captured before the boundary. Its acceptance does not
claim that an uncaptured car can safely traverse the endpoint.

There were client `Can't keep up` warnings and transient Sable sublevel packet
warnings around teleports. An initial client also lost its Xvfb connection;
host memory was healthy and no `OutOfMemoryError` appeared. The accepted run
and cold reopen exited normally. Logs, decoded FoundationData, selected
screenshots and the saved world are hashed in [`SHA256SUMS`](SHA256SUMS) and
checked by [`verify.py`](verify.py).
