# FD403 Free Spin damper comparison on the shipped powered circuit

This connected survival-client checkpoint separates actual Free Spin damper
impulses from capture on the shipped `/fd demo free_spin` circuit. The command
created the route, car, magnets and motors. The separate [survival construction
pass](../acceptance.md) covers result-slot crafting and player-installed magnetic
spans on a straight powered section; it does not make this circuit player-built.

## Candidate and setup

- Source HEAD `95cccf1`; packaged JAR
  `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
  `462342f5dcb2054c5900ad94a02372817fb59cbc9570428db30d4baf0c9b7ccf`.
- Minecraft 1.21.1, NeoForge 21.1.248, standalone packaged profile. The copied
  survival world `FD403FreeSpinDemoDamper2` has `GameType=0`, commands enabled
  and seed `1933455356643639169`. The new plot is separate from earlier family
  plots. The final [world archive](world.tar.zst) and exact files are hashed in
  [SHA256SUMS](SHA256SUMS).
- `/tp Dev 600.5 -59 300.5` and `/fd demo free_spin` created eight connected
  Original rails, a four-carrier Free Spin car and native Create drives. One
  right-side rail contains `INDUCE 3–9` targeting carrier 1, `INDUCE 12–18`
  targeting carrier 8 and `DAMP 24–32` targeting all four carriers. The route,
  vehicle and motors were command-authored fixtures.

## Connected observations

1. An ordinary survival right-click on a seat mounted the player
   ([boarded](free-spin-demo-board-result.png)). The occupied carrier was
   `eb6fd2f2`; its three peers remained empty. The mounted Rotation Tool showed
   ready, open restraints ([editor](free-spin-demo-ready-open.png)). The rider
   closed restraints ([closed](free-spin-demo-ready-closed.png)) and dispatched
   ([moving](free-spin-demo-dispatched.png)).
2. Before the damper, the rider was at `(628.50,-55.56,344.04)` on the descending
   right side ([sample](free-spin-demo-before-damper.png)). All four carriers
   were released (`capture=false`) and had `damperImpulses=0`, `damperEnergy=0`,
   and `damperSpeedReduction=0`. Their angles and angular speeds were distinct.
   The occupied carrier had angle `-0.02946` radians and angular speed
   `-0.10610` radians/s.
3. Downstream at `(613.35,-55.56,316.50)`, before capture, the same carrier
   remained occupied and released ([sample](free-spin-demo-after-damper.png)).
   Its damper audit rose to **127 impulses**, **-0.03786** angular-energy units,
   and **+0.27469** radians/s of cumulative instantaneous speed reduction.
   The three empty carriers recorded 143, 133 and 129 impulses, negative energy
   changes and positive speed reductions. The audit counts actual damper
   impulses, so its change while `capture=false` isolates damping from the
   subsequent boarding lock. The two position samples also span route gravity
   and the preceding inducers; overall angular speed alone would not isolate
   the damper.
4. The mounted editor still showed movement
   ([editor](free-spin-demo-moving-editor.png)). The rider requested capture;
   the car returned to ready with closed restraints
   ([captured](free-spin-demo-captured-editor.png)), then opened them
   ([open](free-spin-demo-ready-open-final.png)). Shift dismount removed
   `RootVehicle`; all four carriers were captured and empty
   ([unloaded](free-spin-demo-unloaded.png)). The first client saved all
   dimensions and exited normally ([title](free-spin-demo-title.png)).
5. A fresh client reopened the same world and original four carrier IDs.
   Each remained captured and empty; the player had no mount
   ([status](free-spin-demo-reopen-status.png)). The live damper counters reset
   to zero on restart, as expected. The second save completed all dimensions
   and returned to title ([title](free-spin-demo-reopen-title.png)). The test
   process was then terminated from the harness to release memory, so that
   second Gradle invocation ends with exit 143 rather than a normal Quit Game.
   Final decoded FoundationData has eight connected demo rails, the three
   magnetic spans, and fault-free chassis `e98213db` with four captured,
   empty carriers and no rider-seat assignments.

At demo creation the first client logged **three Sable “non-existent sub-level”
movement-packet errors**. Their cause is unresolved; no such error appears in
the reopen log, and no rider-seat assignment or matching-track support fault
was observed for the demo car. Upstream Coasters also emitted missing-model
warnings during startup. This is a bounded mechanics observation, not an
error-free client acceptance. [verify.py](verify.py) checks the archived logs,
carrier samples, saved state, screenshots and world archive. The family teaching
walkthrough and a complete player-built shaped route remain open.
