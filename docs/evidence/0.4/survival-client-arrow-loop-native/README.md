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
