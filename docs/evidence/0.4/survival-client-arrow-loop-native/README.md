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
