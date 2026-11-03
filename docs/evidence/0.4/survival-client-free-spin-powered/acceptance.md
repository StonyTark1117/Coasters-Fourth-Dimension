# FD403 Free Spin bounded powered magnetic pass

This is a connected survival-client checkpoint, not completion of FD403. It
demonstrates result-slot crafting, a player-connected original rail, installed
magnetic spans, occupied powered travel, independent carrier motion, capture,
unload and a clean cold reopen. The route is a straight section; the two-leg
run does not isolate damper strength from the intervening capture action or
complete the family teaching walkthrough.

## Candidate and fixture

- Source `d0a3a92` (including long-section reach and live rider-assignment
  fixes); packaged `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
  `4a54b188d62331760ae5049b25425234ba6fb708a4e260df1f159d78c539b4ea`.
- Minecraft 1.21.1, NeoForge 21.1.248, standalone packaged profile. Survival
  world `FD403FreeSpinPoweredRetry3` is a copy of the accepted family world,
  with a nonoverlapping plot at x=300. The saved `GameType` is 0, commands are
  enabled, and the seed is `1933455356643639169`.
- Commands supplied upstream iron, copper, blue dye, redstone, quartz, Create
  red seats, iron bars, native Coasters cart/anchors, a Minecraft Chain and a
  crafting table. The final retry supplied another two red seats, one iron bars,
  four iron ingots and one native cart. Commands placed stone supports and an
  upstream Create creative motor. No Fourth Dimension output was granted. The
  fixture proves crafting from supplied ingredients, not unassisted gathering.

## Connected actions and observations

1. The player took 16 Free Spin Track and one Free Spin Car from ordinary
   crafting result slots ([track](free-spin-track-crafted.png),
   [car](free-spin-car-shift-crafted.png)). Native anchors at `(300,-60,20)`
   and `(300,-60,70)` were joined with crafted matching track
   ([route](free-spin-route-connected.png)); the installed native Chain
   committed 50 blocks ([chain](free-spin-chain-installed.png)). The final
   upstream motor input at the z=70 anchor was `Speed: -16.0f`
   ([motor](free-spin-motor-reverse-chat.png)).
2. A click beside the middle of this 50-block section originally failed with
   `Stand within eight blocks of the selected section` because the editor
   measured distance to anchors. The revised editor accepted normal inducer
   and damper item use at the clicked section
   ([inducer](free-spin-inducer-retry-result.png),
   [damper](free-spin-damper-installed.png)). The saved rail has one targeted
   `INDUCE` span at distances 28–32 and one targeted `DAMP` span at 13–17,
   each with target mask 1, strength 20, falloff 0.2 and direction 1
   ([editor](free-spin-spans-targeted.png)).
3. The first powered attempt traversed the magnetic section but reached the
   open endpoint before capture. Its saved car has the compatible-track fault.
   A second attempt mounted a newly crafted car; near the old faulted car, a
   stale rider-seat assignment was recorded on both cars and client snapshots
   failed validation. Both negative attempts and their logs are preserved.
   Production now binds a player to the actual live SeatEntity and clears that
   rider's assignments on other carriers. A regression check creates an
   orphan assignment and verifies the resulting snapshot loads with only the
   live assignment.
4. In the final retry, the player crafted and placed another Free Spin car,
   boarded an original carrier seat ([mounted](free-spin-retry3-boarded.png)),
   closed restraints and dispatched. Connected player z advanced from 61.50
   through 42.46 to 35.89. At z=42.46, `/fd status` reported the targeted
   carrier angle `-7.08495`, angular speed `3.03256`, and no occupant, while
   the separately occupied carrier angle was `0.30929` with angular speed
   `-0.50486` ([status](free-spin-retry3-capture.png)). This is measured
   independent response after the inducer.
5. The player captured at z=35.89; the car stopped at z=35.37 and returned to
   `Ready | Restraints closed` ([first capture](free-spin-retry3-postcapture-editor.png)).
   A short second dispatch carried it through the remainder of the damper to
   z=30.14. The targeted carrier then reported angular speed `0.00279`, while
   the three other carriers reported distinct values
   ([status](free-spin-retry3-damper-pass.png)). The first capture also reduced
   angular motion, so this is a span traversal and independent-response check,
   not a damper-only measurement.
   Final capture returned to `Ready | Restraints closed`
   ([captured](free-spin-retry3-final-captured.png)). Restraints opened and
   normal Shift dismount removed `RootVehicle`; all four new carriers reported
   `capture=true`, `occupants=0`
   ([unloaded](free-spin-retry3-dismounted.png)).
6. The first final-run client saved every dimension and exited normally. A
   fresh process reopened the same survival world, reported the same four
   carrier identities, all captured and empty, no `RootVehicle`, and then
   saved and exited normally ([title](free-spin-retry3-cold-title.png)). The
   decoded final FoundationData has the same rail spans, a fault-free new car
   with chassis `521f0aac-dbbc-4f63-ba66-376b3b497f91`, and no rider-seat
   assignments. The older faulted car remains distinct at chassis
   `96021dda-bccb-40f2-82c1-7caad0c2d96f`.

The client logs contain occasional Sable sublevel packet and server pacing
warnings, including long saves. The accepted run and cold reopen have no
`Invalid rider seat assignment` error or new endpoint fault. A first GameTest
run failed because the regression check briefly exposed its synthetic duplicate
to a concurrently running Revised snapshot test. The check now repairs that
state in the same callback. The rerun reached 39 successful cases without a
failure, then was stopped during a slow recovery-bank chunk load; it is not
claimed as a completed 50-test suite. Both test logs are archived.
The packaged build passed. [`verify.py`](verify.py) checks the archived logs,
screenshots, decoded state, raw state digest and world archive.
