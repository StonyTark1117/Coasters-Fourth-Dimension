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
