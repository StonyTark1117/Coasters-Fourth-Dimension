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

## Current completion checkpoint — 2026-11-15 01:56 UTC

The occupied DAMP pass, normal Capture/open/Shift unload and cold reopen are
complete. The current run is shut down; this checkpoint supersedes the earlier
01:43 resume checkpoint below. Older sections below remain historical.

- Assignment: **FD403C and its Free Spin FD403F teaching contribution**.
  FD403A/B/D/E remain closed. No Arrow rerun or second Free Spin teaching ride
  is required.
- Owned run: private `:260`, endpoint `127.0.0.1:25612`, root
  `/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113`;
  world `FD403FreeSpinRiderRepair20261114`. Dedicated server, client and Xvfb
  services are now inactive.
- Occupied released-carrier DAMP: assigned carrier
  `a79f9650-1b2c-433c-8428-b94eb0ed765f` went from 0 to 78 impulses while
  `occupants=1` and `capture=false`; energy was `-0.17315709154037506` and the
  DAMP speed-reduction counter was `0.47375498768252844`. See the compact
  [pass record](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/20261115-fd403c-occupied-damper-pass.json)
  and the [acceptance record](fd403c-shaped-occupied-20261115/README.md).
- Normal Capture and Open restraints were acknowledged at 01:39:39/01:40:38;
  normal Shift cleared the rider at 01:41:06. All four assigned carriers were
  captured/empty and `RootVehicle` was absent before shutdown.
- The world was cleanly saved at 01:45:13, cold reopened, and Dev rejoined at
  01:46:37. The 01:47:01 readback showed all four assigned carriers
  `capture=true, occupants=0` with no `RootVehicle`; DAMP counters reset on
  process restart and the pre-capture DAMP evidence remains separate. On the
  final shutdown, **all dimensions were saved** at 18:53:15 server-local time
  (01:53:15 UTC). All three owned systemd services are inactive.
- The new [FD403C shaped-route acceptance](fd403c-shaped-occupied-20261115/README.md)
  closes route, occupied DAMP, normal unload, persistence and Free Spin teaching.
  The Free Spin row is closed in the current queue and roadmap; the aggregate
  FD403F owner still owns package-level reconciliation.
- No code cleanup or additional runtime is required for this family outcome.
  Preserve unrelated dirty work; lead owns integration/staging/commit/push.

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

## Continued occupied-pass diagnosis — 2026-11-14 UTC

The existing `:260` client and dedicated server remained live and connected.
The correct assigned vehicle was reidentified by UUID, not appearance: chassis
`49305388-c9f5-4125-aaa1-12dbfc7c17a5`, with intended carrier
`a79f9650-1b2c-433c-8428-b94eb0ed765f`. Local seat
`(20481032,127,20605960)` resolved to that carrier; F3 confirmed the open
`fourthdimension:free_spin_seat`, and one normal empty-hand use produced a new
server mount event and `occupants=1`. Normal editor Close restraints and
Dispatch were acknowledged. The occupied car remained at approximately
`(361.7,-57.0,127.3)` on the approach to DAMP; its released-carrier counters
remained `damperImpulses=0`. Normal Capture was then acknowledged, restraints
were opened, and Shift dismounted. At the end of this observation the intended
carrier was captured, empty and open. This pass does not satisfy the required
DAMP observation.

The stalled approach is now localized. The route edge into the next driven
section is the curve `(360,-60,128) → (368,-60,120)`. Its first anchor's
existing powered span originates at `(360,-60,164)` and covers the long
`(360,-60,164) → (360,-60,128)` edge plus only `0.0454545` of this curve
(about `0.51 m`). The occupied chassis was about `1.8 m` into the curve. The
separate span from `(368,-60,120)` to `(384,-60,120)` is powered by the motor
at `(368,-60,121)` (`Speed=16`, anchor `Speed=-16`, travel sign `+1`), but
the unpowered gap between the existing i1 coverage and this i7 span leaves the
car short of the drive. A normal Chain click at i0 `(360,-60,128)` returned
`Chain lifts cannot overlap`; the i1 span's existing partial curve coverage
explains the rejection. No span was committed by that click.

Next, extend the existing i1 span through i0 to i7 `(368,-60,120)` with the
normal Chain selection, checking F3 on each actual endpoint and confirming the
committed segment data before another dispatch. The overlap checker excludes
the current start anchor's existing span and rejects overlap with other
anchors, so the existing i1 anchor is the correct selection start; a second
new span started at i0 would repeat the rejected overlap. The i1 span should
end at i7, where the existing i7 span begins, without sharing positive-length
track. Before claiming ride acceptance, verify occupied progression through
the shaped loop and read the same released carrier's DAMP counters before
normal Capture. The supplied Creative Motor is an upstream fixture and earns
no crafting credit.

One separate old Free Spin chassis, `8a06a861-879d-43a8-9058-0d08fbb42b57`,
was mistakenly dispatched during target recovery. It is not the assigned
acceptance vehicle and contributes no evidence. Its carrier
`c23df0a6-3f46-4feb-babe-c704e5057e6f` remains `capture=false`,
`occupants=0`, with counters unchanged at 161. Restore that car through the
normal editor to captured/open before finalizing this run. The current work
also removed staging stone `(393,-57,172)` and added a temporary stone at
`(362,-57,164)`; remove the temporary support when the route repair is done.
A second temporary support at `(360,-56,165)` was removed after it blocked the
view. The latest F3 view from `(362.5,-60,164.5)`, yaw `90`, pitch `29.2`,
still hit `simulatedcoasters:coaster_anchorpoint` at `(366,-60,164)` with
chain-active rendering; `data get block` found no block entity there and
`sable info @i` found no sublevel. This is not a confirmed hit on i1
`(360,-60,164)`, so do not send another Chain click until the actual i1
endpoint is visible in F3. The intended a79 carrier remains captured/empty
with zero damper impulses; the separate c23 car remains uncaptured/empty and
still needs normal restoration.

## Mounted editor and released-pass continuation — 2026-11-14 UTC

The `:260` client remained connected. A new normal right-click on the
F3-confirmed seat at local `(20481033,127,20605960)` logged
`FD_BOARDING_ASSIGN accepted=true` for carrier
`a79f9650-1b2c-433c-8428-b94eb0ed765f`; `RootVehicle` read back as
`create:seat` and `fd status` showed `occupants=1`. The old F3-confirmed
controller point `(20481032,129,20600808)` resolves to the unrelated
`c23df0a6…` carrier. A corresponding guessed point on `a79f…` produced no F3
block hit; neither point was clicked as a controller.

While mounted, normal Rotation Tool use opened the Free Spin editor through the
ridden-seat binding. Its screen showed `sns_free_spin | Ready | Restraints
open`. The Rotation Tool had disappeared from this copied run's inventory, so
the server restored it to the selected hand with
`item replace entity Dev weapon.mainhand with fourthdimension:rotation_tool`;
this is run setup only and earns no acquisition credit. Previously accepted
normal recipe evidence remains in force. One normal **Close restraints** menu
click closed the editor; reopening it showed `Ready | Restraints closed`.

The first normal **Dispatch** click was rejected with the visible message
`Release maintenance retention before dispatch`. One normal **Release
retention** action produced `4D settings updated`; the editor still showed
`Ready | Restraints closed`. A subsequent normal **Dispatch** click closed the
editor and released the intended carrier. Readback showed
`capture=false occupants=1 damperImpulses=0` for `a79f…`, so it is occupied and
released but has not yet reached DAMP.

The current server `fourthdimension_foundation.dat` has an `sns_free_spin`
DAMP rail from `(392,-60,128)` to `(392,-60,164)`, with the installed section
at distances `9.9127407–13.9127407` and target mask `15`. After dispatch, the
intended chassis `49305388-c9f5-4125-aaa1-12dbfc7c17a5` read at approximately
`(358.6983,-57.0179,162.3723)`; the rider carrier was near
`(354.0374,-57.4808,162.0317)`. Two samples about 30 seconds apart were nearly
identical and the carrier still had zero damper impulses. This does not count
as the required DAMP pass. Continue by diagnosing the motion stall and route
position before any further dispatch; the DAMP segment remains roughly 33 m
east at x=392. Keep the carrier occupied and released during any successful
comparison, then capture/unload normally. The unrelated `c23df0a6…` car still
needs captured/open cleanup.

Raw inputs and screenshots are in the same run root under
`/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/`.
Key files: `20261114T130418.689186Z-after.png` (seat target and boarding),
`20261114T130515.000929Z-after.png` (mounted editor),
`20261114T130607.663596Z-state.png` (restraints closed),
`20261114T130649.231240Z-after.png` (first dispatch rejection),
`20261114T130728.332812Z-after.png` (retention response), and
`20261114T130753.290665Z-after.png` (released ride).

Follow-up samples through 13:13 UTC kept `a79f…` at `capture=false`,
`occupants=1`, `damperImpulses=0`. Chassis position remained approximately
`(358.6984,-57.0179,162.3724)` and carrier position approximately
`(354.0374,-57.4808,162.0317)` across the later samples. The running editor had
been closed after Dispatch, and the client remained mounted/connected. The
current saved foundation graph has these eight loop edges in order:
`(360,164)→(368,172)→(384,172)→(392,164)→(392,128)→(384,120)→(368,120)→(360,128)→(360,164)`
at y=-60. Its installed Induce interval is on `(368,-60,172)→(384,-60,172)`;
the DAMP interval is on `(392,-60,128)→(392,-60,164)`. Thus the occupied
released pass has not produced the required damping response, and the next
action is to diagnose why the chassis remains near the i1 approach at
`(358.7,-57.0,162.4)` before dispatching again. Do not count these repeated
stationary samples as route traversal or a damper comparison.

Read-only `data get block` checks after the stall confirmed the four current
drive anchors and committed lengths: i1 `(360,-60,164)` has `Speed=+16`,
`TravelSign=+1`, `49.9151 m`; i7 `(368,-60,120)` has `Speed=+16`,
`TravelSign=+1`, `17.0598 m`; i6 `(384,-60,120)` has `Speed=-16`,
`TravelSign=-1`, `49.6597 m`; and i3 `(384,-60,172)` has `Speed=-16`,
`TravelSign=-1`, `17.0920 m`. These readbacks match the recorded intended drive
signs. They do not establish delivered motion at the current chassis location;
diagnose route/body state there instead of repeating value-box edits.

## Chain direction correction — 2026-11-14 UTC

The pinned dependency decompilation at
`/home/braydon/.local/state/fourthdimension-runs/roadmap-chain-review-20261114/CoasterCartChainLiftDrive.txt`
shows `applySpeedMatch` calculating the desired cart tangent speed as the
negative of the projection of `chainWorldVelocityAtHit` (`dneg` immediately
after `projectOntoUnitTangent`). Therefore the sign of
`signedChainBlocksPerSecond` is opposite the cart's travel along the ordered
span. The 12:53–13:13 live observations corroborate this: the dispatched
chassis moved from approximately `(361.77,-57.02,127.23)`, near i0, to
`(358.70,-57.02,162.37)`, near i1, then remained there without reaching DAMP.
That is the reverse of the intended `i1→i0→i7→i6→i5→i4→i3→i2→i1` loop
direction. The earlier sign interpretation recorded above used the signed
span speed without this cart-motion inversion and was incorrect.

For the recorded spans, forward cart travel along that loop requires negative
signed chain speed at each lift. The corrective motor readbacks are therefore
i1 `(360,-60,164)` `Speed=-16` with `TravelSign=+1`; i7 `(368,-60,120)`
`Speed=-16` with `TravelSign=+1`; i6 `(384,-60,120)` `Speed=+16` with
`TravelSign=-1`; and i3 `(384,-60,172)` `Speed=+16` with `TravelSign=-1`.
These are planned correction values, not yet installed or verified. Capture
and unload the stationary occupied failed pass through the normal editor,
change the four values through the Create motor value boxes, and read back
each motor and anchor once. Then normally reboard and run the occupied pass;
do not claim DAMP until the same released carrier's counters rise before
Capture.

## Reconnect and safe failed-pass recovery — 2026-11-14 UTC

The dedicated server restarted after the earlier `save-all flush` watchdog
failure. Client `fd403c-free-spin-20261113-client.service` remained on owned
display `:260`; server `fd403c-free-spin-20261113-server.service` remained on
loopback port `25612`. The client reconnected twice through Direct Connection
to `127.0.0.1:25612`; both joins appear in `server/logs/latest.log` and the
input helper confirmed one connected player. The server later blocked for
about 31 seconds during its scheduled sublevel save and timed the client out.
No manual `save-all` was sent. `save-off` was applied for the short interaction
window and `save-on` restored automatic saving at 06:47 MST. The pre-recovery
world copy remains at
`server/FD403FreeSpinPoweredRetry3-before-recovery-20261114-0620`.

After reconnect, the saved rider assignment restored Dev to the intended
Free Spin seat at `(20481033,127,20605960)`. F3 hit a
`fourthdimension:sns_free_spin_controller` at `(20481033,128,20605960)`; the
normal Rotation Tool opened the `sns_free_spin` editor. The failed pass was
stopped normally with Capture. The editor acknowledged the action, and
`fd status` confirmed carrier `a79f9650-1b2c-433c-8428-b94eb0ed765f`
`capture=true, occupants=1, damperImpulses=0`. Normal Open restraints then
refreshed to `Ready | Restraints open`; Done closed the editor and ordinary
Shift dismount cleared `RootVehicle`, leaving the intended carrier
`capture=true, occupants=0`. This is cleanup of the failed pass, not a DAMP
observation.

The four Create motor anchor readbacks have **not** changed: i1
`(360,-60,164)` Speed `+16`, TravelSign `+1`; i7 `(368,-60,120)` Speed `+16`,
TravelSign `+1`; i6 `(384,-60,120)` Speed `-16`, TravelSign `-1`; i3
`(384,-60,172)` Speed `-16`, TravelSign `-1`. The corrective readbacks remain
the four values specified above.

Camera input needs diagnosis before motor interaction. F3 before and after
`turn -407 66` remained yaw/pitch `-15.0/-2.2`; server `data get entity Dev
Rotation` agreed. The pointer was at `(20,306)` despite the focused window.
Pause-menu Back to Game centered it at `(427,240)`, but a calculated
`turn -78 136` still left yaw/pitch unchanged and moved the core pointer to
`(349,376)`. The tutorial prompt still says “Use your mouse to turn.” The
current helper turn path has not turned the camera on this client; do not
repeat it blindly. Check the live focus/capture state and the cited FD502 input
record first. Any retry must follow a diagnosed condition change and be
verified by both F3 and server rotation. The client and server remain active.

The next motor target has a retained normal-UI pose: i1 motor Source
`(361,-60,164)`, approached from the south at `(361,-60,160.5)` with F3 on the
motor. Current dismounted position was `(359.966,-60,161.462)`. The recorded
movement/aim inputs did not target or click the motor. Do not interact until
camera response works and F3 confirms the exact motor block.

## i3 normal value-board reach diagnosis — 2026-11-14 UTC

The repaired client camera remains functional. At player position
`(382.6895,-60,171.0266)`, the i3 motor at `(384,-60,173)` faces north and its
neighbor to the north is the route anchor at `(384,-60,172)`. The anchor and
motor are in the same Create kinetic network (size 2). The motor remains
`ScrollValue=16`, `Speed=-16`; the anchor remains `Speed=-16`,
`TravelSign=-1`.

After the prior top-face aim failed to open the board, the client-level
`ChainLiftDiagnostics` snapshot was refreshed via its on-demand request. F3
initially identified the motor, but the normal right-click left the world open;
that helper's inspection-only Escape then paused the game. The game was resumed
and the aim was corrected using the motor's north-facing transform. The next
diagnostic reported the actual `BlockHitResult` at
`(384,-60,172)`, `simulatedcoasters:coaster_anchorpoint`, not the motor. A
calculated west-face aim then also resolved to the anchor; no click was sent.
The full current probe is retained as
`fd-chain-diagnostic-i3-anchor-hit-20261114T143331Z.json`, and the corresponding
screen is `20261114T143330.107026Z-state.png` under the run's evidence directory.

Create's pinned `ValueSettingsInputHandler` resolves the block entity at the
actual hit position before it checks that entity's value-box behavior. The
`CreativeMotorValueBox` also offsets by the motor's facing normal. Thus the
current ray lands on the adjacent route anchor, and the motor board cannot be
opened from this pose; its motor values are unchanged. This is a specific
geometry/input blocker, not a camera or generic right-click failure. Do not
repeat the same aim or send another click. Next, determine a safe way to expose
the motor value box while preserving/restoring the two route connections, or
classify the Creative Motor as a disclosed power fixture and set only its
fixture value if that does not violate FD403C's required normal player-built
route/Induce/DAMP interactions. Do not claim normal motor UI input unless a
connected-client screenshot shows the value board.

## i7 chain gap continuation — 2026-11-14 20:06 UTC

The current owned client/server are still active and Dev is connected. Run
root, display, endpoint, and identities remain those above; no service restart
occurred. After the previous failed pass cleanup, `a79f9650…` remains
`capture=true, occupants=0`, restraints open, with `damperImpulses=0` in this
world copy. Chassis `49305388…` is stopped near `(371.358,-57.018,174.295)`.

Live NBT showed i1 `(360,-60,164)` `Speed=-16`, `TravelSign=+1`,
49.915073 m; i6 `(384,-60,120)` `Speed=31`, `TravelSign=-1`, 60.517624 m;
i3 `(384,-60,172)` `Speed=16`, `TravelSign=-1`, 17.092002 m; and i7
`(368,-60,120)` had no committed span before this continuation. The motor at
`(368,-60,121)` existed, read `ScrollValue=16`, `Speed=-16`, and faced north
(axis Z). A normal F3-confirmed Chain start on i7 followed by a normal Chain
use at the valid preview endpoint installed a 0.5142589868 m segment on the
curve `(360,-60,128)→(368,-60,120)`, from `t=1` to `t=0.9545455273`. The
existing i1 span already covers the same curve from `t=0` to `t=0.9545455273`;
this normal i7 commit fills only the remaining gap. Its saved
`ChainLiftShaftAxis="x"` is wrong for the Z-axis motor, and the i7 anchor still
reads `Speed=0`. This span must not be credited as a functioning powered route
until its motor coupling works.

The exact aim and span records are preserved in
`evidence/fd-chain-diagnostic-i7-gap-preview-20261114T1956Z.json` and the
screenshots at `20261114T195614.661742Z-after.png` and
`20261114T195636.223229Z-after.png`. The client preview was `VALID`, length
0.5142589868 m, and not overlapping before commit. The motor's live facing was
confirmed read-only with `execute if block ... create:creative_motor[facing=north]`.
The i7 anchor axis X was selected while approaching the anchor from the west;
for a normal replacement, approach from the north side so the shaft axis is Z,
matching the motor and the proven i3 Z-axis coupling.

Removing the incorrect short span has a targeting complication. At the shared
endpoint `t=0.9545455273`, `ChainLiftSpanQuery.findAtGraphHit` resolves the older
i1 span, so do not use Shift+Wrench there. From the west and east ground views,
the client selected an adjacent edge; no Wrench input was sent. A temporary
stone support was placed at `(372,-59,118)` using `setblock ... keep`; Dev is
currently positioned on it at `(372.5,-58,118.5)`, with the Wrench selected.
The latest overhead diagnostic
`evidence/fd-chain-diagnostic-i7-overhead-remove-20261114T2006Z.json` resolves
to the neighboring i7→i6 edge at `t=0.0629998`, with `wrenchSpanHit=null`.
No removal click has been sent. Use only a diagnostic hit that resolves to
anchor `(368,-60,120)` on the unique interval `t>0.9545455` before Shift+Wrench;
then verify i1 remains 49.915073 m and i7 has no span. The support must be
removed after this targeting repair.

The normal Chain remains in hotbar slot 3; the Wrench is in slot 2. The player
is not mounted. Existing established input settings remain unchanged, and the
helper continues to record normal input. After correcting the i7 motor/axis
coupling, read back a shared kinetic network and `Speed=-16` at both motor and
anchor before resuming the occupied ride. Do not dispatch this route while the
i7 anchor remains at zero speed.
