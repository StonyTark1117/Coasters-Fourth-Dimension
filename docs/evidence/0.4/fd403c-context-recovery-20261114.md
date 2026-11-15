# FD403C context recovery — 2026-11-14 UTC

**Current goal verified at 2026-11-14 15:53 UTC:** `get_goal` reported the
active objective “Complete FD403C Free Spin player-built workflow and its
FD403F teaching contribution” for thread
`01a1d49d-7763-76b2-8b0c-6f5b337f0ea2`. The earlier `/goal Complete FD403A`
prompt is superseded history; FD403A remains closed. Do not recreate that goal
or repeat Arrow acceptance. Continue the open FD403C outcome and its Free Spin
teaching contribution.

This read-only review reconciles retained inputs after a context reset briefly
returned the family owner to its completed FD403A assignment. Active ownership
and remaining work stay in [the current queue](../../CURRENT-QUALIFICATION.md#current-work-queue).
The [FD403C execution card](../../roadmap/0.4-core.md#fd403c) remains the procedure;
this record adds no acceptance requirement or separate assignment tracker.

## Current resume checkpoint — 2026-11-15 00:07 UTC

Root refreshed this summary from the owner's retained console/input records
and latest substantive actions, without controlling the runtime. This checkpoint
supersedes the older current-state instructions below, including the 20:06 i7
repair. Earlier sections remain historical evidence, not work to repeat.

- Stay on **FD403C plus its Free Spin FD403F contribution**. The active goal
  remains C; the initial Arrow prompt/preview does not reopen FD403A.
- Same owned runtime: `:260`, endpoint `127.0.0.1:25612`, run root
  `/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113`.
  Keep the owner's existing processes, world and current `run.json`.
- The restored i2 motor had delivered the wrong direction. At 00:00:40 the
  owner corrected the disclosed north-facing power fixture at `(368,-60,173)`
  to `ScrollValue=-16`. At 00:00:50–57 motor and anchor both read `Speed=+16`
  on the same kinetic network. The 10.799449 m i2→i1 Chain span with
  `TravelSign=-1` remains intact. This correction is complete; do not rebuild it.
- Normal reboarding and restraints closure preceded Dispatch at 00:06:10.
  Carrier `a79f9650-1b2c-433c-8428-b94eb0ed765f` is released and occupied.
  Chassis `49305388-c9f5-4125-aaa1-12dbfc7c17a5` moved from approximately
  `(368.43,-57.018,172.43)` toward i1 to `(364.298,-57.018,168.298)`.
  The 00:06:37 and 00:07:21 samples remain near that second position, and the
  same carrier's `damperImpulses` remains zero. Correct direction is verified;
  passage through DAMP is not.
- Next: continue the owner's current diagnosis of this new stop using actual
  vehicle, route and propulsion state. Do not keep polling an unchanged pass
  or repeat the prior setup. Preserve the corrected span, fix the demonstrated
  cause and finish the remaining occupied DAMP claim, normal Capture/open/unload,
  outstanding local cleanup and Free Spin teaching reconciliation. Reuse
  accepted construction, input and unchanged persistence evidence.

Evidence: the run's `evidence/console.jsonl` and `evidence/input.jsonl` at the
timestamps above; no additional test campaign or acceptance condition is added.

## Resume the existing family and runtime

- Owner: **Complete FD403C Free Spin**, thread
  `01a1d49d-7763-76b2-8b0c-6f5b337f0ea2`.
- Checkout: `/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`,
  reviewed HEAD `2464155663791b77388e0d96941130be034944fa`. Shared uncommitted work
  remains in place; the lead retains Git integration ownership.
- [Run record](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/run.json):
  private display `:260`, endpoint `127.0.0.1:25612`, dedicated world
  `server/FD403FreeSpinPoweredRetry3`. The directory name is inherited from the
  accepted input world; it does not imply an active singleplayer world.
- Chassis: `49305388-c9f5-4125-aaa1-12dbfc7c17a5`.
  Occupied-pass carrier: `a79f9650-1b2c-433c-8428-b94eb0ed765f`.
  Other carriers: `cd2c8d97-af9f-4d13-acfd-32153171a41f`,
  `248eee39-97f3-4ff1-aa98-8ef744e1a46d`,
  `f1d8669c-2827-4c50-9cf8-475316f8a98e`.

## What the retained observations already establish

The run's [console record](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/console.jsonl)
and [normal input record](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/input.jsonl)
preserve these distinct steps:

1. Normal seat boarding at 05:11:46 and occupied dispatch at 05:16:18. The
   player-built loop and its Induce/DAMP spans were already installed. The car
   traveled but stopped short of DAMP; the same occupied carrier's damper
   counters were zero at 05:20. This is partial route evidence, not the missing
   damper observation.
2. Capture at 05:25:50 changed the intended carriers to `capture=true`.
   By 05:26:27, `RootVehicle` was absent after normal unload. Do not resume as
   if the rider still needs to be unloaded from that pass.
3. The new powered coverage is present at i6 `(384,-60,120)`:
   `ChainLiftCommittedLength=49.65973794511106`, shaft axis `x`, travel sign
   `-1`, source `(385,-60,120)`. The disclosed upstream motor fixture and anchor
   both read `Speed=16` at 05:49 and again at 06:02:10 after restart. A previous
   ineffective live `ScrollValue` edit is superseded by this working setup.
4. `save-on` succeeded at 05:55:29. The subsequent watchdog/save interruption
   and Sable dependency errors are retained for diagnosis. The owner restarted
   only its server and reconnected its existing client. At 06:02:10, the intended
   carriers were captured and empty, and the route and motor were queryable.
   The earlier queue statement that saving remained disabled was stale.

## Latest visual evidence supersedes the blank-view inference

The [06:04:03 screenshot](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/20261114T060403.158427Z-state.png)
shows the vehicle assembly and track. F3 targets `fourthdimension:wing_strut`
at sublevel block `(20481034,130,20597771)` while the player's world position
is approximately `(385.094,-58,117.137)`. An earlier camera view targeted grass;
that earlier view does not demonstrate the assembly is missing or justify
replacing the retained world. Visibility alone also does not prove that every
sublevel or seat is healthy: attribute any current failure to its actual target,
body and handler before deciding whether recovery is necessary.

The crosshair in the newer image is on a structural block, not a seat. Seat
selection uses the inherited Create `SeatBlock` lower-half shape, despite the
taller visible seat back. Use actual current geometry and reach when recovering
the seat target. Keep the established input settings and owned helper; do not
repeat generic mouse diagnosis or search for a hypothetical missing-seat error
without an observed error carrying that meaning.

## Live identity correction and targeting handoff

The earlier `evidence/sable-sublevels.json` position near `(355.64,-58.19,165.19)`
is historical. At about 06:36 UTC, the owner queried
`execute as Dev at @s run sable info @e[distance=0..100,limit=50,sort=nearest]`.
It returned the intended carrier `a79f9650-1b2c-433c-8428-b94eb0ed765f` at
approximately `(384.96085,-57.53148,116.56313)`, with the intended chassis
`49305388-c9f5-4125-aaa1-12dbfc7c17a5` at `(381.28366,-57.01786,119.00311)`.
The earlier screenshot established visibility, not that visible body's UUID;
do not infer vehicle identity from it alone. `data get sub_level` reports
auxiliary data, not the current transform.

Use the [moving-body target procedure](../../FD403-RUN-PREPARATION.md#recover-a-moving-body-target-without-angle-guessing)
to identify the seat's containing plot and aim through the live transform.
The new pose above is a timestamped observation, not a permanent approach
coordinate. This task is **FD403C Free Spin** after every context reset;
FD403A is already complete. The remaining occupied DAMP observation and its
teaching reconciliation do not require repeating Arrow, powered construction,
normal boarding acceptance or generic input discovery.

### Target recovered and normal boarding verified — 06:42 UTC

The owner resolved local seat `(20481032,127,20605960)` to the intended carrier
with `execute positioned 20481032.5 127.25 20605960.5 run sable info @i`.
The subsequent live-transform setup used world feet `(383.5,-60,118.5)`;
the command had an integer `118` and Minecraft centered that coordinate to
`118.5`. Future records should use explicit decimal standing coordinates.
The [06:41:55 readback](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/20261114T064155.944025Z-after.png)
shows F3 targeting `fourthdimension:free_spin_seat` at that local block,
`restrained=false`, with the player on stable ground and an empty hand.
One ordinary right-click at 06:42:17 produced `FD_BOARDING_EVENT action=mount`
and `FD_BOARDING_ASSIGN accepted=true` for that exact seat. The 06:42:30 queries
show the `create:seat` RootVehicle and `occupants=1` on carrier `a79f9650…`.
This closes the current positioning/reboarding interruption. Continue the
occupied DAMP observation; do not perform another target-discovery exercise.

## Evidence carried forward

The [accepted powered construction record](survival-client-free-spin-powered/acceptance.md)
and [accepted damper circuit record](survival-client-free-spin-powered/damper-demo/acceptance.md)
retain their stated scopes, including compatible mechanics and cold-reopen
evidence. The remaining claim is the occupied player-built shaped-route pass
through the installed DAMP span, with the same released carrier's damper audit
separated from Capture, followed by teaching reconciliation against those
observations. Do not convert this context repair into an Arrow rerun, a fresh
family campaign, or a requirement to repeat all accepted persistence checks.

No runtime was controlled, source code changed, or acceptance closed by this
review. The current owner continues the retained run.
