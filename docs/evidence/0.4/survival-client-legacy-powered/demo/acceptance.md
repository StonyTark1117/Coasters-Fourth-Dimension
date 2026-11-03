# FD403 Legacy free rotation on the shipped powered circuit

This connected survival-client checkpoint observes visible, independent Legacy
carrier rotation on the shaped `/fd demo legacy` circuit, followed by normal
capture, unload and cold reopen. The command created the route, car and motors.
The separate [survival construction pass](../acceptance.md) covers result-slot
crafting and player placement on a flat section. This run does not establish a
player-built powered circuit or finish the family teaching walkthrough.

## Candidate and setup

- Source HEAD `434d954`; packaged code `d0a3a92`, JAR
  `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
  `4a54b188d62331760ae5049b25425234ba6fb708a4e260df1f159d78c539b4ea`.
- Minecraft 1.21.1, NeoForge 21.1.248, standalone packaged profile. The
  copied world `FD403LegacyDemoPowered` has `GameType=0`, commands enabled and
  seed `1933455356643639169`. Its new demo plot is separate from earlier
  family plots. The final [`world.tar.zst`](world.tar.zst) SHA-256 is
  `c7b72305afb7b80d31b1c029ff840ceef4a41494b6ca811f194706c213657670`.
- `/tp` positioned the player, and `/fd demo legacy` created eight connected
  original rails, the captured car and native Create drives. Temporary stone
  footholds at x=492–495 helped the player reach a seat; every block was
  removed before dispatch. The route and car were command-authored fixtures,
  so this run makes no survival acquisition claim.

## Connected observations

1. Empty-hand right-click on the seat cushion from the nearby foothold
   mounted the rider ([boarded](legacy-demo-seat-near-result.png)). The
   Rotation Tool showed `Ready | Restraints open`
   ([editor](legacy-demo-ready-open.png)); the rider closed restraints
   ([closed](legacy-demo-ready-closed.png)) and dispatched. Both carriers were
   captured and near zero angle before departure; the occupied carrier was
   `cba2a2d7`.
2. The rider started at `(497.50,-55.56,331.41)`, reached
   `(529.88,-51.18,366.01)` on the climb and far side
   ([sample](legacy-demo-20s.png)), then
   `(522.80,-55.52,309.50)` after descending
   ([sample](legacy-demo-40s.png)). At the first sample, both carriers were
   released and had distinct angles: `0.56471` and `0.64384` radians, about
   32° and 37°. Their angular speeds were `-0.14067` and `0.36998` radians/s.
   Later the empty carrier had angular speed `0.48952` radians/s while the
   occupied one was `0.02272`. This is a measured free response on a shaped
   route, unlike the near-zero angles on the earlier flat pass.
3. At the third sample the rider was back on the first straight at
   `(497.50,-55.55,344.29)` ([sample](legacy-demo-60s.png)); the same carrier
   still held one occupant. The mounted editor reported `Aligning / moving`
   ([editor](legacy-demo-moving-editor.png)). The rider requested capture; the
   car returned to `Ready | Restraints closed`
   ([captured](legacy-demo-captured-editor.png)). An immediate restraint-open
   attempt was rejected while the carriers finished settling. A later normal
   action showed `Ready | Restraints open`
   ([open](legacy-demo-ready-open-final3.png)); Shift dismount removed
   `RootVehicle`, and both carriers reported `capture=true`, `occupants=0`
   ([unloaded](legacy-demo-unloaded.png)).
4. The first client saved all dimensions and exited normally
   ([title](legacy-demo-title.png)). A fresh process reopened the same world:
   both original demo carrier IDs remained captured and empty, and the player
   had no mount ([status](legacy-demo-reopen-status.png)). The second client
   also saved all dimensions and exited normally
   ([title](legacy-demo-reopen-title.png)). Decoded FoundationData has a
   fault-free chassis `b859c201-4f92-4aca-a4d7-963cd57ce27d`, eight valid
   connected demo rails, both empty carriers and no rider-seat assignments.

The accepted run and reopen logs contain no invalid rider-seat assignment.
The copied world retains a separate older Legacy car with an endpoint fault;
it is not the demo car. Both client processes ended with `BUILD SUCCESSFUL`.
[`verify.py`](verify.py) checks the archived logs, screenshots, decoded state
and world archive. This checkpoint closes the *visible free-rotation example*
gap; player-built powered operation and the family teaching walkthrough remain
open.
