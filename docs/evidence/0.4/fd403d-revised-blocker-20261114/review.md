# FD403D blocker review — 2026-11-14 UTC

The earlier run did not establish that Revised vehicle binding is absent or that
the Rotation Tool integration is unworkable. Its no-response observations and
server stalls remain evidence; the inferred cause is superseded by this review.

## Direct inspection

Read-only decoding with `scripts/inspect-foundation-save.py` of the stopped run's
`/home/braydon/.local/state/fourthdimension-runs/fd403d-revised-20261113/server-new/world/data/fourthdimension_foundation.dat`
found:

- [Exact inspected SavedData](reviewed-foundation.dat), preserved before the
  owner resumes the world. Decode it with the same read-only script to reproduce
  the saved-binding inspection without launching Minecraft.
- SHA-256 `189ac17439b7bf36e7b695e989bde26e4118e6e8dd92e7e49cb8e922f8a90d01`, schema 6.
- Revised chassis `b2265459-c718-4b90-ad19-587764aef6aa`, family
  `zacspin_revised`, with an empty fault string, boarding readiness, open
  restraints and four captured, empty carriers with lower pivots at `-0.5`.
- Valid original Revised rail `ea93f4f0-c5a9-486f-8360-c621171542f5`, from
  `(260, -43, 250)` to `(272, -43, 250)`. The vehicle binds those endpoints.
- The chassis and all four carrier identities match the accepted
  [player construction save](../survival-client-revised/foundation-save.json).

The same Revised identities and rail also remain in the run's earlier
`server-new/world-demo-162739` save. Neither saved-data inspection supports the
claim that copying the Arrow-named world removed Revised binding.

The run screenshot `evidence/20261114T025438.521382Z-state.png` shows the player at
`(253.500, -60.000, 74.500)`. Its F3 targeted block is a native
`simulatedcoasters:coaster_cart` in Sable plot coordinates, not the original
Revised rail. The lower diagnostic text mentions aiming at a coaster anchor;
that instruction must not be confused with F3's actual block identity. This
position is beside saved Arrow rails at `(238, -60, 78)`–`(254, -60, 78)` and
`(254, -60, 78)`–`(262, -60, 70)`, roughly 176 blocks from the Revised plot.

## What remains unresolved

Saved records do not alone prove that Sable loaded every body correctly or that
the current client/server interaction reaches the editor handler. Those are the
next targeted observations, not reasons to discard the save.

`RotationTool.useOn` returns silently on the server when its distance check
fails, then tries a vehicle/carrier binding and an original-rail graph match.
Failure to find either target later emits a message. `RotationTool.use` also
supports opening the vehicle editor from a mounted carrier. No editor **and no
failure message** cannot identify which path ran, or establish that the server
received the block-use event. Temporary vanilla reach changes do not establish
the separate eight-block handler guard or correct world/local coordinates.

The earlier repeated clicks provide no traced handler result. Do not repeat them
unchanged or classify the tool/connector as broken from that observation alone.

## Owner's next action

1. Resume the existing owned runtime after checking current resource headroom,
   actual game directories, world and connection. Preserve both saves.
2. Inspect the retained Revised plot using the endpoints and chassis above;
   compare its loaded bodies and current `/fd status` with the saved record.
   Disclose any permitted setup positioning separately from normal player proof.
3. Use the already accepted normal-range Revised seat/operator procedure from
   [the construction checkpoint](../survival-client-revised/acceptance.md).
   Confirm the actual target, reach and state before one action and its readback.
4. If the correct target still fails, capture the actual item-use event, handler
   guard and coordinate space once; route a demonstrated shared defect to the
   shared-code owner. A silent click is not a missing-binding diagnosis.
5. Continue the missing player-built powered-route and teaching claims, reusing
   accepted construction, operation and persistence evidence. Do not rebuild or
   requalify the whole family because this diagnostic used the wrong plot.

No Minecraft process was launched or controlled for this review. FD403D remains
open; the separate save-stall observation also remains unresolved.

## Correct-plot follow-up (2026-11-14 UTC)

The owned runtime was recreated with its working directory set to the recorded
run root and the saved Revised plot was loaded. Positioning at `(259.5,-40,254.5)`
with one temporary stone support exposed the actual assembly. F3 then identified
the crosshair target as `fourthdimension:revised_seat` in the Sable sublevel; the
saved runtime reported all four `zacspin_revised` carriers captured and empty,
with validation availability reporting `assignments=0`.

A normal empty-hand right-click left occupancy unchanged. A Rotation Tool
right-click on the correctly targeted Revised axle/seat also produced no editor.
The temporary interaction range was restored to `4.5`, the temporary stone was
removed, and the services were stopped. This is a real correctly positioned
failure observation, but it still does not identify whether the click packet,
Sable coordinate conversion, `Boarding.canBoard`, or another handler guard
rejected the action. No connector/tool defect is claimed until the event receipt
and guard result are traced once.

## Traced editor proof (2026-11-14 UTC)

The validation-gated trace was loaded by the rebuilt server and client. One
correctly targeted Rotation Tool click on the live Revised assembly reached
`RotationTool.useOn` with `distanceSq=10.19`, resolved body
`b2265459-c718-4b90-ad19-587764aef6aa`, found the saved `zacspin_revised_v1`
vehicle, and dispatched `EditorNetwork` for that chassis. The client readback
in `20261114T041812.894054Z-after.png` visibly shows the Fourth Dimension editor
(`zacspin_revised | Ready | Restraints open`). The trace proves the connector and
Rotation Tool path work; the earlier silent result was a target/setup failure,
not a Rotation Tool defect. Trace lines are preserved in the run's
`server-new/logs/latest.log` and `evidence/server.log`.

## Powered-route continuation (2026-11-14 UTC)

The retained world was resumed with the recorded Java 21 client/server settings
and normal input bindings. The inventory contained the retained Revised track,
chain, and Creative Motor items. A calculated far-end approach was attempted
from a disclosed temporary support position near `(272.5,-42,253.5)`, but F3
read ordinary stone at the endpoint ray rather than a native anchor. A single
normal Chain click produced no chain or lift state change; `/fd status` remained
four captured, empty Revised carriers with zero motion.

During this continuation the dedicated server again logged Sable storage
corruption while loading the retained world: `Couldn't find sub-level at index 5
in storage file for chunk [53, 12]`, followed by repeated `Sub-level dependency
does not exist in chunk` errors. The retained route rendered with endpoint
support geometry but did not expose a verifiable native anchor at the expected
hit location. Temporary supports were removed and the world was saved cleanly;
all services are stopped. The missing player-built powered cycle therefore
remains open pending repair/rebuild of the retained Sable sub-level/endpoint
fixture. No powered or teaching acceptance is claimed from this attempt.

## Continuation fixture damage and recovery handoff (2026-11-14 UTC)

The owner resumed the retained run after the editor proof. Subsequent aiming
attempts did not establish an anchor hit. Its console record then logged
`setblock 272 -43 250 air` at 04:37:17 UTC (client readback at 04:37:18),
followed by removal of `(272,-44,250)`. The first position was the original
Revised endpoint, not an obstruction.

A read-only decode of `server-new/world/region/r.0.0.mca`, chunks `(16,15)`
and `(17,15)`, after those actions found air at the removed endpoint/support,
while `(260,-43,250)` remained a native anchor with `AnchorPeerCurves=[]` and
`track_connected=false`. In the retained `server-new/world-demo-162739`, both
original anchors remain UP-facing, connected by reciprocal 12-block curves,
with stone supports at y=-44. The retained Foundation identities were already
checked above. This establishes owner-induced fixture damage, not a product
interaction defect.

The owner was directed to preserve the damaged save and logs, validate and
restore a complete consistent retained world through its owned runtime, and
continue the powered-route and teaching assignment. Do not splice region and
SavedData files from different states. No restoration or family acceptance is
claimed by this review. Correct fixture recovery is preparation for the open
outcome; accepted construction, mechanics and persistence evidence stays closed.

The follow-up tool record confirms that the owner stopped its services, retained
the damaged world as `world-damaged-20261114` and
`world-damaged-live-20261114`, copied the complete `world-demo-162739` into the
active world path, decoded its FoundationData and restarted its owned services.
The connected player returned by 04:40 UTC. The owner is now working on native
anchor/Chain selection for the missing powered-route claim; this confirms
fixture recovery and continued actions, not completion of FD403D.

### Restored-anchor follow-up (2026-11-14 UTC)

The intact `world-demo-162739` copy was restored after preserving the damaged
world as `world-damaged-live-20261114`. Read-only live checks confirmed both
native `simulatedcoasters:coaster_anchorpoint` block entities at `(260,-43,250)`
and `(272,-43,250)`, with reciprocal 12-block `AnchorPeerCurves` and
`track_connected=true`. From the calculated far-end pose `(274.5,-41,250.5)`,
F3 identified the native far anchor and a normal Chain click produced the
visible pending chain/`Tracks are not connected` feedback when the second
endpoint was not yet validly targeted. No powered cycle was claimed. Temporary
supports were removed and the restored world was saved cleanly; services are
stopped. The remaining work is a fresh two-endpoint Chain selection/readback,
then motor dispatch and occupied travel.


## Chain selection review and resumed work (05:04 UTC)

Read-only inspection of the pinned Coasters 0.1.5
`ChainLiftAnchorOutlineClient` distinguishes first selection from installation.
With no selected anchor, a normal Chain click stores `selectedAnchor` and
`lockedShaftAxis` and cancels vanilla placement. A later click sends
`ChainLiftCommitPayload` only for a valid span preview with a hover marker and
locked shaft axis. A second endpoint can be a different connected anchor or a
valid connected-curve hit. Looking at the selected anchor itself can produce
the observed “Tracks are not connected” preview; that feedback alone does not
establish missing connectivity or require another world rebuild.

The owner stopped its runtime at approximately 04:48 UTC and preserved
`server-new/world-chain-attempt-20261114`. Its shell command subsequently failed
parsing, but the preceding move and copy had executed: read-only inspection
confirmed that `server-new/world` and `world-demo-162739` both held FoundationData
with SHA-256 `cacf6bfcad73c8bbebd1050f27228f931eb472985eac7ce0f382872397420f7c`.
The attempt copy had a different hash and remains preserved. This verifies the
baseline copy, not a new acceptance claim; do not repeat the move blindly.

The FD403D owner received a specific continuation to complete normal two-endpoint
Chain selection, native lift readback, propulsion, occupied travel and the family
teaching handoff. Subsequent commands show the owner restarting its own services,
reconnecting, restoring the disclosed support at `(274,-42,250)`, and performing
a fresh Chain click from `(274.5,-41,250.5)`. The retained screenshot
`evidence/20261114T050410.576877Z-state.png` shows the native far anchor,
`track_connected=true`, pending selection and the same-anchor preview message.
These actions verify resumed work beyond an active-thread flag. The different-end
preview, committed lift and player-built powered outcome remain unverified.
Leave the progressing owner working; the Rotation Tool diagnosis stays closed.

## Chain two-endpoint follow-up (2026-11-14 UTC)

The retained clean world was resumed on private Xvfb `:299` with Java 21. Both
native anchors remained reciprocal 12 m curves (`260,-43,250` and
`272,-43,250`), and the far anchor was repeatedly verified by F3 as a native
`simulatedcoasters:coaster_anchorpoint` with `track_connected:true`.

A normal Chain click while the far anchor was the current endpoint produced the
expected `Tracks are not connected` response; this was the same-anchor case and
does not establish a broken route. Attempts to form a different endpoint then
showed the actual remaining interaction condition: the near-anchor ray is
occluded by the native support/vehicle geometry (F3 reported stone or unrelated
vehicle blocks), so the client never produced a valid hover preview and never
sent a ChainLiftCommitPayload. Read-only anchor data remained unchanged after
each attempt; no ChainLiftFirstHop/segments/length was created.

Temporary supports were removed, `/save-all` completed, and the owned server,
client, and Xvfb services were stopped cleanly. This run adds no powered-cycle
or teaching acceptance claim. The next targeted action is to obtain one clear
hover on the different connected curve/endpoint, record the valid preview, and
then commit the lift before motor dispatch.

## Superseding save decode: Chain lift is installed

The prior paragraph's “no ChainLiftFirstHop” conclusion was wrong because it
queried the near anchor. The post-stop region decode in
`chain-lift-save-review.json` shows the far anchor `(272,-43,250)` contains
`ChainLiftFirstHop=71468256833493`, `ChainLiftCommittedLength=12.0`,
`ChainLiftShaftAxis=x`, `ChainLiftTravelSign=1`, and one committed segment with
`TStart=1.0`/`TEnd=0.0`. The original `world-demo-162739` far anchor has no lift.
This retained current world therefore already has the player-installed lift;
do not rebuild or repeat Chain selection. The next action is motor connection
and nonzero-speed readback at the far anchor.


## Saved Chain lift verification (05:18 UTC)

The preceding no-lift conclusion is superseded. Read-only decoding of the
cleanly stopped active world's region `r.0.0.mca`, chunk `(17,15)`, found a
committed Chain lift on the **far anchor `(272,-43,250)`**:

- `ChainLiftFirstHop=71468256833493` (near anchor `(260,-43,250)`).
- `ChainLiftCommittedLength=12.0`, `ChainLiftShaftAxis=x`,
  `ChainLiftTravelSign=1`.
- One committed segment with `TStart=1.0`, `TEnd=0.0`,
  `EdgeFrom=71468256833493`, `EdgeTo=74766791716821`.
- `Speed=0.0`; powered travel is still unverified.

The [decoded comparison](chain-lift-save-review.json) records both this anchor
and the original `world-demo-162739` anchor, which has no installed lift. The
[exact compressed current chunk](reviewed-chain-anchor-chunk.nbt.zlib) and its
SHA-256 preserve this observation before continuation. Decompress with zlib,
then decode the NBT using the reader functions in
`scripts/inspect-foundation-save.py`. No runtime or world was changed for this
inspection.

The owner's last anchor readbacks queried the near anchor. Absence of lift
fields there did not establish absence at the far anchor that owns this lift.
This saved observation establishes installation; it does not by itself identify
which earlier click committed it or close the powered-route/teaching claim.
Do not reset the retained world or repeat Chain selection. The owner received a
specific continuation to confirm the far anchor live, normally connect its
x-axis drive, verify nonzero speed, then complete the occupied player-built
powered route and teaching handoff. Existing accepted evidence stays closed.


The owner's subsequent live `data get block 272 -43 250` confirmed these same
lift fields after reconnecting. Its 05:18 UTC actions began preparing normal
motor placement with the disclosed upstream motor fixture. This verifies
substantive continuation and preserves the installed lift; no powered ride or
family closure is claimed yet.


## Motor approach review (05:31 UTC)

The owner's next turn confirmed the installed lift live, but attempted motor
placement at x=257–259 beside the near anchor rather than the far anchor that
owns the lift. It then stopped and saved its runtime at 05:20 UTC. Its final
“falling into the Sable void” explanation is not established by the retained
`evidence/20261114T051842.127952Z-state.png`: F3 shows player
`(257.5,-41,250.5)` and targets `fourthdimension:wing_strut` in Sable local
coordinates. This is a vehicle-geometry target; it does not establish a missing
world or unworkable motor interaction.

Read-only inspection of the latest saved region confirms the far anchor at
`(272,-43,250)` still owns the lift and has `chain_lift_shaft=true`. Its support
is `(272,-44,250)`. The east approach at x=273–276, z=250 is air; the adjacent
stone row is at z=251. The owner received a specific continuation using a
supported east-side approach and a normal click on the far anchor's east face,
with a motor expected at `(273,-43,250)`. The suggested pose and angle are a
calculated candidate, not an observed successful placement: verify stable
position, native hit and reach first, then read motor facing and far-anchor
speed after one action. Preserve the installed lift and proceed to occupied
travel and teaching. No motor placement or powered acceptance is claimed by
this read-only review.

## Far-end motor placement and powered source readback (2026-11-14 UTC)

Resuming the retained world, the far anchor `(272,-43,250)` was read before
interaction and confirmed the installed 12 m x-axis Chain lift. A temporary
support at `(275,-43,250)` enabled the calculated normal east-side approach;
F3 identified the exact native far anchor with `chain_lift_shaft:true`. With a
Creative Motor selected, one normal right-click placed `create:motor` at
`(273,-43,250)`. Immediate readback showed motor `Speed:-16.0`,
`ScrollValue:16`, and the far anchor `Speed:-16.0`, `Source:[273,-43,250]`,
with the same committed lift fields and kinetic network ID. This proves normal
motor placement and powered source propagation to the installed lift.

The temporary support was removed and `/save-all` completed. An occupied
powered travel claim remains open; the player-built route and powered source
are preserved in the stopped save.


## Continued boarding diagnosis (05:41–05:51 UTC)

The user renewed continued execution after the owner stopped at the completed
motor step and again after acknowledging that no external blocker existed.
The owner then kept the current runtime running and resumed seat interaction.
The earlier unsupported teleport to `(270.5,-40,250.5)` had fallen to y=-60
before the click, as `20261114T053403.238169Z-state.png` shows. It did not
establish a tool failure.

Read-only code review found that `CarrierSeatBlock` inherits Create
`SeatBlock.getShape`, returning `AllShapes.SEAT`: `(0,0,0)`–`(16,8,16)` in model
units. This was verified in the project's Create 6.0.10-280 dependency. The
rendered Revised back extends higher, so aiming at its visible back need not
hit the selectable lower half-block. The owner used the supported approach,
corrected its aim and obtained an F3 `revised_seat` target. One normal-reach
click at 05:48:13 did not mount.

At 05:50:55, after a diagnostic reach increase, the current server logged
`FD_BOARDING_EVENT action=mount` and `FD_BOARDING_ASSIGN accepted=true` at
seat `(20481032,127,20509704)`, offset `(0,-1,0)`. The player also had a
`create:seat` RootVehicle. This establishes event delivery and a successful
boarding path under the disclosed diagnostic condition. It is not a new
normal-range boarding acceptance. The owner was directed to restore reach to
4.5, use the actual seat/world position for any needed closer approach, and
continue occupied powered travel and teaching while retaining prior accepted
normal-range boarding and persistence evidence. The family remains open.


The subsequent console record confirms reach was restored to **4.5** before
ordinary editor operation. The 05:51:43 Dispatch click followed the visible
`zacspin_revised | Ready | Restraints closed` state. At 05:52:41 UTC, `/fd status`
reports all four original Revised carriers released; carrier
`8f8dad7e-7975-4366-a74c-755c672fe61a` has one occupant and angle approximately
-1.403 radians, while its three siblings are unoccupied with different angles.
This verifies continued occupied operation beyond setup. It does not alone
establish route traversal or close the family walkthrough. The owner remains
working on those claims in the same retained run.

## Occupied powered dispatch and alignment guard (2026-11-14 UTC)

The retained player-built Revised world was used without rebuilding the lift or
motor. A normal empty-hand seat click was first diagnosed with temporary
`block_interaction_range=10` because the transformed seat was 4.6 blocks from
the supported pose; the server recorded `FD_BOARDING_EVENT` and
`FD_BOARDING_ASSIGN` with `accepted=true`. The attribute was immediately
restored to the normal 4.5 range. While seated, the Rotation Tool opened the
ordinary editor; its header showed `zacspin_revised | Ready | Restraints open`.
Closing restraints and dispatching through the editor produced the normal
`4D settings updated` response. Reopening the editor while the rider remained
seated showed `zacspin_revised | Aligning / moving | Restraints closed | End of
...`; the server status reported the Revised carrier with `occupants=1` and
`capture=false`. This proves occupied editor dispatch and the released-carrier state, but it does
not by itself prove translation along the track: the editor label is also used
whenever `boardingReady=false`. The earlier status showed one occupant and
`capture=false`; the later normal Capture action changed all four Revised
carriers to `capture=true`, returned the editor to `Ready`, allowed restraints
to open, and a subsequent Shift dismount cleared `RootVehicle` and reduced
the Revised occupant count to zero. Actual chassis or mounted-rider position
comparison is still required before claiming powered route translation. This
remaining evidence gap is distinct from the Rotation Tool connector, which is
already traced and working.

The mounted seat entity retained `Pos=[2.04810325E7,127.0,2.05097045E7]`.
Those are sublevel plot coordinates, not the chassis world position; their
constancy does not establish whether the chassis traveled. `Boarding` transforms
seat coordinates through the containing body pose when a world position is needed.

The owner subsequently collected a completed [occupied chassis trace](occupied-chassis-trace-20261114T0604.jsonl.gz)
for the intended chassis `b2265459-c718-4b90-ad19-587764aef6aa` and carrier
`8f8dad7e-7975-4366-a74c-755c672fe61a`. Its 2,640 samples cover client game ticks
215700–218339: chassis x ranged from 260.47935 to 260.58575 while z stayed
250.5. First-to-last x displacement was only 0.00485 blocks. The footer
reports 65 moving samples, but those small movements do not demonstrate
traversal of the 12-block route. This world-coordinate trace supports the
remaining propulsion/route investigation; the seat-local coordinates do not.
The owner is continuing drive-direction diagnosis in the retained run.
Capture/open/unload and compatible earlier evidence remain accepted.

Preserved trace SHA-256: `c1404e48e8f661754079594f937ab55a83ff749f808390089dab05114eaea8ad`.

## Motor reconnection attempt after occupied trace (2026-11-14 UTC)

The occupied snapshot trace above showed only 0.00485 blocks of chassis displacement, so the run continued. The retained source block at `(273,-43,250)` was found to be a command-recreated `create:motor` with `Network.Size=1`; the far anchor `(272,-43,250)` read `Speed=0` and had no `Source`/network id. This is a diagnostic mutation after the earlier normal placement evidence, not a connector failure. A normal client click was sent only after F3 identified the far anchor, but the block was already occupied and the click did not replace it. The motor was then removed and recreated while attempting to regain a valid unobstructed placement ray; the current diagnostic copy has been restored to a motor block and temporary supports/effects were cleared before `/save-all`.

This leaves an internal fixture repair: the retained route has a valid 12-block ChainLift span, but the recreated motor is not connected and the anchor remains at `Speed=0`. The next read-only inspection below identifies the motor orientation as the failed precondition. Earlier normal placement evidence remains usable; restoring the disclosed upstream motor fixture does not require repeating that normal-placement claim. The Rotation Tool connector is independently verified and is not the cause.


## Motor orientation identified by supervision (2026-11-14 UTC)

Read-only decoding of the latest saved region `server-new/world/region/r.0.0.mca`,
chunk `(17,15)`, found the motor at `(273,-43,250)` in block state
`create:creative_motor[facing=north]`. Its block entity reported `Speed=-16`,
`ScrollValue=16` and `Network.Size=1`. The anchor to its **west**,
`(272,-43,250)`, retained its 12 m lift with shaft axis `x`, but had `Speed=0`
and no source. The owner's recreation commands omitted the facing property.

In the pinned Create `6.0.10-280` dependency, read-only Java 21 inspection of
`CreativeMotorBlock.hasShaftTowards` confirms that a motor exposes its shaft
only on its `FACING` side. The saved north-facing motor therefore cannot drive
the west-adjacent anchor. This is a demonstrated fixture orientation error,
not an external dependency or an established addon binding defect.

The owner was directed to restore an explicitly west-facing upstream motor,
initialize its intended generated-speed setting through the proper creation or
control lifecycle, and verify the kinetic network, anchor source and speed.
Do not inject `Speed`, `Source` or `Network` to imitate a connected result.
The already-disclosed fixture setup permits this repair; prior normal placement
is not reopened. Occupied route travel and teaching remain the outstanding
claims. Stopping after two inconclusive targeting attempts means diagnosing
and changing that approach, not ending the family assignment.


At 06:27:15 UTC the owner removed the stale motor and recreated the west-facing
upstream fixture with `ScrollValue:-16` and `Speed:16.0f`. The owner seeded
`Speed` despite the requested initialization-only procedure; that setup must
remain disclosed and is not evidence of normal motor initialization. The actual
readback then shows a connected motor network and anchor
`Source=[273,-43,250]`, `Speed=16`. Neither source nor network NBT was injected.
This restores the upstream drive for the remaining occupied route observation;
it does not establish that travel has passed. Earlier normal motor placement
evidence remains reusable. The owner continued boarding, with a reminder to
clear its temporary levitation effect and use stable current seat geometry.

## Classification correction (2026-11-14 UTC)

The word “blocker” in this evidence directory and its historical title does not mean an active external hard blocker. The north-facing motor was an internal diagnostic fixture error and has been corrected to a connected west-facing motor (`Network.Size=2`, anchor `Speed=16`, `Source=[273,-43,250]`). FD403D remains open only because occupied route traversal and the Revised walkthrough have not yet been evidenced.

## Teaching reconciliation while route evidence remains open (2026-11-14 UTC)

The current teaching artifacts already cover the Revised-specific guidance: the tooltip identifies independent seat pairs and lower hinges, requires Revised Free-Rotor Track, and instructs capture before boarding; the Revised Ponder lesson distinguishes its lower hinges/chassis from Free Spin, explains independent rider loading, and requires stopping, capture, alignment, and readiness before opening restraints. The shared boarding lesson covers empty-hand boarding, restraint closure, dispatch, and return capture. These rendered resources are accepted and unchanged. The only unclosed teaching contribution is the Revised family walkthrough tied to a real occupied powered route; no tooltip or Ponder defect is outstanding.

## Positioning and wrong-button diagnosis — 06:39–06:48 UTC

The owner resumed after its status-only final response. The exact historical
seat point `(20481032.5,127.25,20509704.5)` resolves through `sable info @i` to
carrier `8f8dad7e-7975-4366-a74c-755c672fe61a`. Sable's live transform maps that
point to approximately `(263.56017,-41.12499,253.50002)`. Unsupported approach
teleports subsequently fell to y=-60; those screenshots do not demonstrate
seat obstruction or a failed mod interaction. Body mass was 3 rather than the
intact carriers' 4, prompting inspection of geometry and input history.

The retained input log shows **button 1**, X11 left-click/Attack, repeatedly
used with boarding/editor expectations at 06:28:15, 06:28:59, 06:29:25,
06:31:20, 06:31:50 and 06:37:31. The
[06:28:15 before image](/home/braydon/.local/state/fourthdimension-runs/fd403d-revised-20261113/evidence/20261114T062815.196366Z-before.png)
shows `revised_seat` at local `(20481033,127,20511752)`; the
[after image](/home/braydon/.local/state/fourthdimension-runs/fd403d-revised-20261113/evidence/20261114T062815.196366Z-after.png)
shows break particles and the exposed `revised_axle` at y=128. This proves
diagnostic damage to that seat. It is a different local plot from the intended
`8f8dad7e…` target, so do not infer that both seats were removed at the same time.
The owner must identify and repair the actual damaged geometry, or select an
intact registered seat, before continuing. Preserve the original construction
evidence and disclose fixture repairs; do not recreate the whole world.

The helper now offers `input use` for right-click and `input attack` for
left-click, without a numeric argument. Records include the resolved button
and binding. Eight focused Python checks passed, including XTest button
mapping and refusal/error behavior; no Minecraft rebuild or restart occurred.
Use the [live target procedure](../../../FD403-RUN-PREPARATION.md#recover-a-moving-body-target-without-angle-guessing),
with a supported reachable approach, then one normal `input use`. Free Spin
verified that procedure with a normal mount at 06:42:17 UTC. Revised traversal
and teaching remain open; this diagnosis is local recoverable work, not an
external blocker or an assignment stopping condition.

### Intact registered seat reached — 06:47 UTC

Two attempted repairs used `out_sub_level ... setblock`, creating static world
seats at `(263,-42,253)` and `(263,-42,254)`. A Rotation Tool hit on the former
logged `body=null`; this diagnoses those added static blocks, not an original
vehicle binding defect. The owner was directed to remove only those additions
after checking their identity and keep the original vehicle. A diagnostic
release also left carriers uncaptured; restore capture/readiness before normal
boarding. These setup changes do not alter accepted construction.

The intact northern carrier `ab69d209-5200-467d-9d64-6e9d7d394d83` was verified
by live query with mass 4. The owner established support at `(263,-43,244)` and
used `centered_in_sub_level @n positioned ~ ~-1.25 ~ out_sub_level @i` from the
identity-checked carrier source, aiming from world feet `(263.5,-42,244.5)`.
The [06:47:59 screenshot](/home/braydon/.local/state/fourthdimension-runs/fd403d-revised-20261113/evidence/20261114T064759.743286Z-state.png)
shows that stable position and F3 on a registered `revised_seat`, local
`(20481031,127,20505608)`, with `restrained=true`. The immediate targeting gap
is resolved for this intact seat. Continue the normal editor/capture/open/board
flow and remaining occupied route observation. Retain damaged-seat repair and
cleanup as local follow-up; neither a normal mount nor route travel is claimed
from this screenshot alone.

### Heartbeat verification — 06:49–06:50 UTC

Normal boarding now succeeded on the intact northern carrier. The dedicated
server recorded `FD_BOARDING_EVENT action=mount` at 06:48:49.484 UTC and
`FD_BOARDING_ASSIGN accepted=true` for local seat `(20481031,127,20505608)`,
offset `(-1,-1,0)`. At 06:48:56, the owner queried a `create:seat` RootVehicle
and `occupants=1` on `ab69d209-5200-467d-9d64-6e9d7d394d83`. This resolves the
current targeting/normal reboarding interruption, not the remaining route claim.

The subsequent close/dispatch attempt did not prove travel: a 743-sample client
trace ranged only x=260.44135–260.57773, z=250.5–250.59091. The 06:49:35
status reported `capture=true`, `occupants=0`; the following RootVehicle
readback retained an attachment with an empty entity compound. Repeated
dismount-cancellation records also began at 06:49:27. These are observations
requiring attribution, not proof of an external dependency or a particular
product defect. The owner is actively comparing the client trace, live chassis
pose and motor/anchor readback. The motor and anchor still read `Speed=16`,
network size 2. No redundant continuation was sent and no runtime was controlled
by the heartbeat reviewer. Occupied powered traversal, local fixture cleanup
and Revised teaching reconciliation remain open.

## Input/fixture correction and intact-seat boarding (2026-11-14 UTC)

The previous “hard blocker” wording was incorrect. There is no external blocker. The retained input record shows the failed boarding attempts at 06:28–06:37 used X11 button 1 (attack) while expecting boarding; Creative attack can remove a targeted seat. The affected carrier later reported Sable mass 3.0, consistent with local fixture damage. A subsequent repair attempt mistakenly used `out_sub_level ... setblock`, which created two static outer-world seats; those blocks were removed immediately after the body-null Rotation Tool trace identified them. Temporary supports were also removed.

Using the intact registered Revised carrier `ab69d209-5200-467d-9d64-6e9d7d394d83` (Sable mass 4.0), `centered_in_sub_level` placed Dev at a live F3-confirmed `fourthdimension:revised_seat`. With captured/ready state, restraints opened, empty main hand, and the proven right/use binding, `input use` sent X11 button 3 and `fd status` recorded `occupants=1`; `Dev` had a Create seat RootVehicle. This closes the input/connector/boarding diagnosis.

Editor close-restraints and Dispatch were then issued. The occupied snapshot trace records the rider/chassis identity, but chassis translation was only x=260.5777..260.4413 and z=250.5..250.5909 (743 samples), so powered route traversal remains unverified. Motor/network readback remains connected (`Network.Size=2`, source [273,-43,250], committed chain length 12). The remaining work is local route-direction/start-state reconciliation and the Revised family walkthrough; it is not an external hard blocker.

## Correction: latest motor mutation invalidated connection (2026-11-14 UTC)

The attempted `data merge block 273 -43 250 {Speed:-16.0f,ScrollValue:16}` was not a valid reverse-drive test. The anchor remained at `Speed=16`, then the motor reset to `Speed=0` and lost its network. The earlier connected-motor statement must be withdrawn. No further sign/facing/NBT mutation is justified until native sign semantics are traced.

The dispatch interval also did not isolate powered travel: the valid mount occurred at 23:48:49 with `occupants=1`; editor dispatch was logged at 23:49:04; the first subsequent authoritative guard was repeated `FD_BOARDING_EVENT action=dismount restraints=true` / `FD_BOARDING_DISMOUNT_CANCEL`. Thus the later `capture=true occupants=0` state cannot be used as a motor-direction result. Preserve the current world and diagnose the dispatch/result path before restoring any motor state.


## Motor and editor lifecycle diagnosis — 2026-11-14

The 06:49 operation sequence did not send the intended ride dispatch. The
`20261114T064913.614867Z-after.png` screenshot shows gameplay and “4D settings
updated” immediately after Close restraints. Both the 06:49:19 Dispatch-intent
and 06:49:27 Done-intent inputs occurred in gameplay. Those left-clicks could
attack geometry; their menu labels in `input.jsonl` are intent, not screen
verification. `RotationEditorScreen.addButton` sends one edit and calls
`onClose()`. Reopen and inspect the editor before each subsequent operation.
The diagnostic `FD_EDITOR_DISPATCH` at 23:49:04 local time records
`EditorNetwork.open` sending an editor Target packet; it is not ride Dispatch
(edit action 13). The earlier wording calling this an occupied dispatch result
is superseded. Later dismount cancellation is an observation, not attribution
of the first failed ride-dispatch guard.

The original motor readback at 06:50:07 was connected (+16 RPM, UI -16).
The 06:50:28 live NBT merge did not perform a normal reversal: the anchor first
remained +16 and the motor dropped to zero without a network. At 06:56 both
motor and anchor read zero. Create 6.0.10-280, pinned by this run's classpath,
loads `ScrollValue` directly in `ScrollValueBehaviour.read`; changing it through
`setValue` instead invokes the motor's `updateGeneratedRotation` callback.
Restore the existing motor through that normal control, not another NBT merge.

The complete sign trace for this retained route is:

- Motor `(273,-43,250)` faces west; Create's `convertToDirection` negates its UI
  value. Keep that facing and the committed axis unchanged.
- Anchor `(272,-43,250)` has `ChainLiftTravelSign=+1`; the committed segment runs
  T=1 to T=0, from x=272 toward x=260. Native `signedChainBlocksPerSecond` is
  `anchor.Speed / 16 * travelSign` for the 256 RPM / 16 blocks-per-second scale.
- `ChainLiftSpanQuery.chainWorldVelocityAtHit` multiplies that signed speed by
  the segment direction. Crucially, `CoasterCartChainLiftDrive.applySpeedMatch`
  then **negates** its projection before applying physical cart velocity.
  Thus UI **-32** gives motor/anchor **+32** and a cart target of **+2 m/s in x**,
  toward the route interior from x≈260. The original -16/+16 used the same
  correct direction. Root initially omitted the final inversion in a thread
  message and corrected it before a motor-control mutation; that preliminary
  +32 UI instruction is superseded by this complete calculation.

Next action: use the normal speed control, verify matching +32 motor/anchor
speed and restored source/network, then use a freshly opened editor for each
operation. Actual dispatch clears `stopping`/fault and carrier capture;
`PropulsionInterlock` intentionally holds propulsion while stopping is true.
Retain the rider and observe the unresolved occupied route behavior, repair
only demonstrated local attack damage, and finish the existing teaching
contribution. These corrections add no acceptance cases or repeat requirements.
