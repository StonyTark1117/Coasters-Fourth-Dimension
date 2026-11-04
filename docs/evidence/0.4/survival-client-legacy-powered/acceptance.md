# FD403 Legacy bounded powered client pass

This is a bounded connected-client checkpoint, not completion of FD403. The
flat original rail demonstrates occupied powered travel, capture, unload and
cold persistence. It does not demonstrate a visible free-rotation response or
a complete circuit.

## Candidate and fixture

- Source `a6e18c906a8e1f9f33880030dccd7b0be1b53b16`, packaged
  `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
  `34c2d6ce14fecaf9941303ff950ec9f8ef586a61456669a0d404b0be43672db1`.
- Minecraft 1.21.1, NeoForge 21.1.248, standalone packaged dependency profile.
  Survival world `FD403LegacyPowered` was copied from the accepted Arrow
  survival world, with a fresh nonoverlapping plot at x=260.
- Commands supplied only upstream ingredients: iron, copper, yellow dye,
  Create red seats, iron bars, one native cart, a crafting table, two native
  anchors and a Minecraft Chain. Commands also supplied support blocks and an
  upstream Create creative motor. No Fourth Dimension output was granted. This
  proves crafting from supplied ingredients, not unassisted gathering.

## Connected actions and observations

1. The player took 16 Legacy ZacSpin Track and one Legacy ZacSpin Car from
   ordinary crafting result slots ([track](legacy-track-crafted.png),
   [car](legacy-car-crafted.png)). They placed two native anchors at
   `(260,-60,20)` and `(260,-60,70)` and joined them with matching crafted
   track. The track stack fell from 15 to 2 ([route](legacy-route-connected.png)).
2. Normal Chain clicks installed a 50-block native lift; the start anchor
   reported `ChainLiftCommittedLength: 50.0d`. An upstream Create motor at
   `(259,-60,20)` gave start-anchor `Speed: 16.0f`. The player used the
   crafted car on the connected rail; it was consumed and a captured car
   appeared ([placement](legacy-car-placement-result.png)). A first Chain
   attempt showed `Tracks are not connected`; clearing the pending Chain
   selection and selecting the new anchors corrected it.
3. The player mounted a native Legacy seat from a temporary foothold
   ([boarding](legacy-board-successcheck.png)). The three temporary foothold
   blocks were cleared before dispatch. `/fd status` reported one occupant on
   the new carrier, then the mounted Rotation Tool showed `Ready | Restraints
   closed` ([ready](legacy-ready-closed.png)).
4. Dispatch changed the editor to `Aligning / moving`
   ([moving](legacy-midride-editor.png)). Connected player position advanced
   from z=`25.1801` to `44.5406`, then `61.0539`. The player captured before
   the open end at z=70; the editor returned to `Ready | Restraints closed`
   ([captured](legacy-captured-editor.png)). They opened restraints and
   dismounted normally; `RootVehicle` was absent and both new carriers reported
   `capture=true`, `occupants=0` ([dismounted](legacy-dismount-retry.png)).
5. Save and Quit persisted all dimensions and the first client ended
   `BUILD SUCCESSFUL` ([title](legacy-title-final.png)). A cold reopened client
   retained Chain length 50, speed 16, both carrier identities and a fault-free
   car. Its editor showed `Ready | Restraints open`
   ([reopened](legacy-reopen-ready.png)); `RootVehicle` was absent. The second
   client also saved and quit normally.

The copied world contains an older, separate Legacy car with an endpoint
fault. The new car is identified by chassis and carrier IDs in the decoded
FoundationData record and has no fault. Its occupied flat-route pass kept
carrier angles near zero, so it is **not** evidence of meaningful free spin.
That requires a shaped route or another measured physical input and remains
open in FD403. Both client logs contain occasional `Can't keep up` warnings and
Sable sublevel packet warnings around teleports; neither run had an OOM or
lost the car. The archive, decoded data, screenshots and compressed logs are
checked by [`verify.py`](verify.py).
