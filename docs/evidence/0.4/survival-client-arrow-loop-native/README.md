# FD403 player-crafted Arrow loop native test

## Attempt 1 — failed fixture

On 2026-11-03, the base-profile `runGameTestServer --offline --no-daemon
--console=plain` run on merge commit `d3a449a` plus an uncommitted JavaDoc-only
wording edit reported 51 required GameTests,
with 50 passing and `craftedarrowlooptraversesnativepoweredcircuit` failing
immediately: `Crafting table inaccessible`. The synthetic survival player was
not positioned near its crafting table. Commit `23ba6c7` places that player
within normal table reach; `compileJava` passes. This attempt overlapped an
Engineered GameTest in another checkout. The exact raw console output is
[attempt1.log.zst](attempt1.log.zst), SHA-256
`af021785fbcba29d6507dc934f160429b728d713627b8967f4a0b27462779c31`.

This is a failed test setup, not Arrow circuit acceptance. A native rerun on
the corrected source is required.

## Attempt 2 — cost assertion used the stored profile length

The corrected source at `ebd41f4` completed 50 of 51 required base GameTests.
The Arrow test crafted the track, car, Rotation Tool and station, then placed
all eight original loop sections, but failed its total cost assertion. The
assertion used the saved profile length after native graph insertion. Production
charges the preview curve length before insertion; later native graph lengths
can differ near a joined anchor. The corrected test calculates each section's
expected cost from the same preview curve and compares the actual inventory
decrease after normal item use. It logs both lengths and the spent count per
section. Another Engineered run started while this suite was near completion,
so this was not an isolated runtime. The raw console is
[attempt2.log.zst](attempt2.log.zst), SHA-256
`fa138ea23f69c5daaea35c44f6220f90331351bac7ea4cf686a68318eaf1d634`.

Neither attempt proves powered travel, rider retention or station capture. A
fresh run of the corrected test remains required.

## Attempt 3 — station binding before native settlement

On source `bb98598`, all eight player-clicked loop sections passed the revised
inventory check, including a first section that consumed nine items across a
two-item selected stack and reserve stacks. The logged per-section costs were
`9, 4, 5, 3, 10, 3, 5, 4` (43 items total). The test then placed its crafted
Arrow car and Boarding Station, but tried to bind the station in the same tick
as car assembly. The normal station click rejected that attempt. The existing
native survival test binds only after the car has settled, so the corrected
test now waits until tick 100 before clicking the station and logs its distance
to the original chassis. The suite again had 50 of 51 required tests pass. A
separate client started during the final portion of this run, so it was not
isolated. Raw output: [attempt3.log.zst](attempt3.log.zst), SHA-256
`155d27f01b8489b8c7db0316ac46ef213ad50394599dec589a86b3a282ad464b`.

Powered travel, rider retention and capture still require the next run.

## Attempt 4 — crafted car unavailable after loop construction

The isolated base-profile run on `0fee34b` again passed 50 of 51 required
GameTests. All eight player-clicked Arrow connections spent exactly their
preview costs (`9, 4, 5, 3, 10, 3, 5, 4`), including the split-stack section.
The normal result slot produced one Arrow car, but the test could not find it
in the synthetic player's inventory at delayed placement tick 10. It stopped
before placing a car or testing powered motion. The next source adds inventory
checks after crafting, anchor placement, every connection, editor use, and at
the delayed placement tick to locate where that crafted item disappears.
Raw output: [attempt4.log.zst](attempt4.log.zst), SHA-256
`3f06ac02e84c75487a1f4de361f271a055bd0d0578ffcef2c8b1a59191f39292`.

This is an unresolved fixture inventory failure, not circuit acceptance.

## Attempt 5 — inventory loss is delayed

The isolated base-profile run on `fdab174` passed 50 of 51 required GameTests.
The crafted Arrow car remained in the synthetic survival inventory after all
eight ordinary track connections and immediately after the Rotation Tool
opened the loop editor. It was absent at the scheduled car-placement tick 10,
before any car use action. All eight section charges again matched preview
costs. The next diagnostic records the inventory, selected hand and menu when
the count changes, so a fixture lifecycle error can be distinguished from an
item-placement failure. Raw output: [attempt5.log.zst](attempt5.log.zst),
SHA-256 `6d3876b9998a19c08a14854fad1c627f1af643eaaa94774c0c70cbe7c8b3106e`.

This run does not establish car placement, powered travel or capture.

## Attempt 6 — scheduled action ran twice

The isolated base-profile run on `29571ad` passed 50 of 51 required GameTests.
At tick 10 the first placement callback found the crafted car and station in
the inventory, consumed both through normal item use, and advanced beyond
station placement. The same callback then ran a second time: its new inventory
trace showed both consumed items gone, and the repeated preplacement assertion
failed. The earlier claim that the car disappeared while waiting was therefore
incorrect. The test had registered ticks 40, 100 and 260 inside its tick-10
callback, while Minecraft's GameTest scheduler iterated its action map. The
corrected test registers all timed actions before that iteration and shares
assembled state through references. Raw output: [attempt6.log.zst](attempt6.log.zst),
SHA-256 `fa14214d30a84206bd79c1610b00888a08a5c2f3ee1400315685b1313fdf03e6`.

The first callback demonstrates crafted car and station placement, but this
failed run does not establish powered travel or final capture.

## Attempt 7 — checkpoint ended before the full circuit

The isolated base-profile run on `8b9eb9d` passed 50 of 51 required GameTests.
It completed the eight crafted track connections, normal crafted car and station
placement, station binding at 6.03 blocks, native rider seating, restraint and
dispatch controls, and powered travel across the far side of the route. The
tick-2000 check observed maximum x/z offsets of 32.52/53.23 blocks, but the
carrier's maximum angle was only 0.00559 radians and the car was not yet
captured. The controlled 360-degree profile is on the right-hand return leg,
so those maxima alone did not establish that the car had reached that leg or
completed the loop. The corrected test samples and records every live section,
keeps the full-turn and final-capture requirements, and waits until tick 6500
before judging the complete circuit. Raw output: [attempt7.log.zst](attempt7.log.zst),
SHA-256 `d4241cb4cbd4e4703d39b69322a51739105d8d0f7cf26fafd27248083a056995`.

This is bounded native powered-travel evidence, not a completed circuit or
connected-client acceptance.

## Attempt 8 — reverse native edge orientation broke the return-chain span

The isolated base-profile run on `f887c88` passed 50 of 51 required GameTests.
The car visited the interiors of sections 0–3. At tick 2000 its center was
`(332.493, 133.125, 544.520)`, at the start of section 4. At tick 6500 it
still had not visited section 4's interior or any return section; maximum
carrier angle remained 0.00559 radians, with no fault or final capture.
The saved FoundationData for this run shows the live Arrow car still dispatched
(`stopping=0`, `restraints=1`, both carrier captures false). It also reveals
that the player-clicked return rails 4–7 were stored in reverse native edge
orientation. In particular, section 4 was stored from `(332,132,508)` to
`(332,132,544)`, while the intended circuit enters at the latter anchor.
The fixture had committed its chain from that entry anchor toward `edge.to()`,
which was the entry anchor itself, and used a `0→1` path in the wrong travel
direction. The corrected fixture targets the actual next loop anchor and uses
`1→0` when the stored native edge is reversed. It re-resolves the edge after
profile editing and logs each committed direction/sign. All eight section
visits, controlled turn, rider and return capture remain required.

Raw output: [attempt8.log.zst](attempt8.log.zst), SHA-256
`ce6801423615baaffdfcb4acd598c53238c2d96be91d81faf2c0d2a755492b7e`.
Saved [FoundationData](attempt8-foundation.dat), SHA-256
`89548f3e125dc382d728d3b7f1fa6571c0fa33f7a8628cd6e6955f4f81eeb041`.
This is a fixture path correction awaiting a new native run; it does not yet
prove the full loop.

## Attempt 9 — full native player-crafted Arrow circuit passed

The isolated base-profile `runGameTestServer --offline --no-daemon
--console=plain` run on exact source `488ef031afac886c47a4f6da6b72758ca2cf7536`
completed **51 of 51 required GameTests** with normal server shutdown and
`BUILD SUCCESSFUL`. `compileJava` and `jar` had passed immediately before the
server run. The local JAR SHA-256 was
`2dd822a95d60a04fa09b33923b45225ea4bd0c519c930b7bb2fcfcff7392b378`.
The runtime used `JAVA_TOOL_OPTIONS=-Xmx3G` and was the sole holder of the
shared game-runtime lock.

The synthetic survival ServerPlayer took five Arrow track batches, one Arrow
car, a Rotation Tool and a Boarding Station from normal crafting result slots.
Ordinary item use placed eight native anchors and connected all eight original
sections. Their preview costs and actual inventory debits matched
`9, 4, 5, 3, 10, 3, 5, 4`, or 43 track items total; the first connection
consumed across a two-item selected stack and reserves. The player placed the
crafted car and station, bound the settled chassis at 6.03 blocks, authored a
nonzero controlled full-turn profile, seated an original native Pig rider,
closed restraints and dispatched. Native Coasters chains and Create creative
motors were disclosed upstream power fixtures, not player-crafted outputs.

The run recorded each of the eight section interiors. The stored native edges
on sections 4–7 were reversed relative to the player's connection order; the
direction-aware Chain spans drove through them. The occupied carrier reached
`6.281755307414869` radians. At tick 2000 all eight sections had been visited,
the chassis was back near the original station, `Boarding.ready=true`, and
the station fault was empty. The tick-6500 final assertions passed with all
eight visits, maximum x/z offsets `34.8655/53.2339`, rider still mounted,
restraints closed, stopping and ready state, and successful station capture.
The test then opened restraints and removed its native fixture bodies and
track. Raw output: [attempt9.log.zst](attempt9.log.zst), SHA-256
`757f6ba76111104471fdbab999f1cee8850100f9041cc9cda54151ca9d0ec293`.

This is native fixture evidence for a fully player-crafted Arrow loop. It does
not close FD403's connected survival-client five-family teaching gate, nor
does the upstream creative motor prove unassisted survival power production.
