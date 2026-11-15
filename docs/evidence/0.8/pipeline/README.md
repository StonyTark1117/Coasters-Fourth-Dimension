# FD1401A Pipeline mechanics and occupied handoff

**FD1401A complete, 2026-11-15.** FD1401B/C remain with this owner for normal
construction and finite content reconciliation; they reuse this occupied proof.
Implementation integrated on primary `main` in
`cc094ada2382a597c8fcdba4590f38494803d31c`; lead owns commits and central status.
Owner: `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`. No new branch/worktree.

The adopted [seated P-gauge specification](../../../design/0.5/FD1401-pipeline.md)
is implemented: four fixed hips between original side rails, whole-body roll,
original running/upstop/side wheel ownership, one/two native cars eight blocks
apart, stopped direction validation, dock-only opening, and native physical
retention after deceleration. No standing compliance, rotating seat or moving
track is implied. The [guide](../../../PIPELINE.md) describes normal controls.

## Evidence and result

| Required claim | Evidence/result |
| --- | --- |
| P fit and affected native invariants | [Targeted native contract](native-contract.log), 1/1: ordinary track rejected; two P cars linked at8; exact fixed seats; asymmetric+.8/full+3.2 rider mass once; complete-dock guard; persisted P descriptor/dock/consist; missing-frame whole-train hold. Copied `FD_FIXED` marker text is historical naming; actual executed class/namespace is PipelineGameTests/fourthdimension_fd1401. |
| Actual player input | [Inputs](normal-occupied-input.json), [server events](server-events.log): same private client proved camera turning and vanilla crafting-table Use, then normal empty-hand seat boarding, Rotation Tool, Close restraints, Dispatch, Stop, Resume, Open and Shift unload. Example construction and setup teleports are disclosed fixture preparation; they earn no normal-construction claim. |
| Contacts, rider clearance and whole-body roll | [Open side view](pipeline-side-open.png), [occupied view](pipeline-rider-travel.png), [rolled first person](pipeline-roll-first-person.png). Original P wheel rows contact the dedicated rail gauge; ordinary N wheel/bottom rendering is suppressed only for Pipeline. Seats, hips, foot supports and restraints remain below/between the rails. Rider and camera inherit one native chassis frame. No rail crosses the default rider eye/head region. |
| Two cars, forward/fixed return | Normal occupied Dispatch completed the bank/full roll and fixed loop without fault. [Recovery return trace](recovery-return.jsonl), [ready dock](forward-return-ready.png), [unloaded view](forward-unloaded.png) and [camera/assignment readback](forward-unloaded.json) show same-dock stop, Open and Shift; no native seat orientation or assigned vehicle remains after unload. |
| Reverse through same fixed route | With both cars stopped/unloaded, normal held-use motor controls reversed all15 sources. [UI](normal-direction-control.png), [commands/readbacks](reverse-controls.jsonl), [remaining-source results](reverse-control-results.jsonl). Normal reboard, Close and explicit Resume validated directions; [87-sample reverse trace](occupied-reverse.jsonl) is fault-free and consist-eligible throughout. [Summary](reverse-summary.json) records native up Y from−.99894 to+.99919 across the roll. Same IDs return to the [ready dock](reverse-return-ready.png), [dock state](reverse-docked.json); normal Open/Shift gives [unloaded state](reverse-unloaded.json). No travel teleport or end reset. |
| Interrupted inclined stop/restart | [Before](interrupted-before.json), [restored](interrupted-restored.json), [implementation/failure record](IMPLEMENTATION.md). Shared damping left~.077blocks/s gravity creep, preventing <=.05 Resume. Pipeline-only native fixed support now acquires after deceleration<=.15 at the measured pose, rebuilds there on reload and releases on validated travel. Exact saved workshop retained chassis/link/rider/seat and closed restraints; stable residual~.00014blocks/s, then normal Resume supplied native Chain motion and returned/unloaded. No sibling case reopened. |
| Failure acknowledgment | Initial motor control aim hit housing and changed orientation; that fixture failure was diagnosed from value-box geometry and restored. The interrupted source correctly latched a power-loss fault/closed restraints. Restored power alone did not open/dispatch. Normal Stop acknowledged repair, then dock Open/reboarding succeeded. [Setup corrections](reverse-setup.jsonl) distinguish repairs from normal speed inputs. |
| Synchronization/camera ownership | Existing native fixed-seat/net/camera path is reused; Pipeline changes seat offsets/contact rendering, not passenger synchronization packets. Shared remote-rider proof carries forward. Actual third-person local rider, full roll, restored assignment and normal camera unload are recorded here. |

## Runtime identity and cleanup

Persistent root: `/mnt/NVME4/fourthdimension-runs/fd1401-pipeline-20261115`.
Java21 verified in direct launch and Gradle; heap1536M verified with jcmd;
Minecraft-only service MemoryMax6GiB. Private display317, server25652/RCON25653,
loopback dedicated world `server/PipelineWorkshop`. Default mouse bindings and
raw input stayed fixed; all input used the owned private-client helper.

Launch01 SHA256 `206659b5d0f5af0f6054f0f228e6d88f9e13a7f88f8a4f00058dbf484da98154`.
Launch02 SHA256 `c0a99ea1011a6f6460b5870d808bf70662ed1c24816a53de145621d28b3fc258`.
Both JARs, direct scripts, full logs, screenshots, source-control inputs and worlds
are preserved. Launch02 includes the targeted hold repair and shared UI correction;
unchanged native-contract evidence was not replayed.

At A completion all `fd1401-pipeline-20261115-{client,server,xvfb}.service` units
are **inactive/MainPID0**. Server saved every dimension. The stopped A world is
preserved at `checkpoints/A-qualified-world` outside builds. B/C may explicitly
resume the workshop for the remaining crafting/paid-track/car/Chain/station
construction; no A replay, full profile sweep or live runtime handoff is required.
