# Dive — FD1702 implementation and qualification

**FD1702A, FD1702B, FD1702C and the FD1702 outcome are complete (2026-11-15).**
Owner chat `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`, primary `main` at
`/home/braydon/Documents/Coasters: Fourth Dimension`. Lead owns shared files,
central queue and commits. Source fixes are integrated in `a3d6a2ef`; this evidence
and [player guide](../../../DIVE.md) are ready for the lead's final reconciliation.

**Current checkpoint:** native03 and the compatible occupied, restart, return,
Survival construction and content observations below cover the assigned outcome.
No remaining Dive action or live runtime. At 13:45 UTC, client/server/Xvfb317 were
inactive with MainPID0 after all dimensions saved. Persistent run root:
`/mnt/NVME4/fourthdimension-runs/fd1702-dive-20261115`; final world is
`server/DiveWorkshop`, copied to `checkpoints/completed-world`. Earlier released
and corner checkpoints remain. Ports25652/25653 are released. Pipeline A–C stays
closed. No unrelated family, networking path or profile is reopened.

| Package | Accepted coverage |
| --- | --- |
| FD1702A | Three native cars/18 fixed seats, eight-block coupling, slow approach, physical hold, tick dwell, validated atomic gravity release, interruption guards, preserved released state and occupied board/motion/return/unload. |
| FD1702B | Original six-seat/frame/restraint/controller and dedicated-track resources, all three recipes, normal controls, guide and buildable loop; finite visual review below. |
| FD1702C | Cumulative native03 + occupied/restart/return + normal Survival crafting/placement/controller operation. Accepted FD1701A and unchanged fixed-seat remote rider/camera contracts are reused. |
| FD1702 | A–C reconciled; no separate aggregate walkthrough. |

## Native mechanism — passed

Java21, namespace `fourthdimension_fd1702`, one required native GameTest passed
2026-11-15. [Raw log](native-02.log). Three actual native cars with18 fixed seats
coupled at8 blocks traversed the powered lift and slow crest, entered the60° hold
section, braked and acquired native constraints at their individual measured
poses. The held complete train stayed below.05 blocks/s through50 simulation
ticks of power loss; the remaining dwell did not advance.

Open restraints, invalid outgoing route and invalid current support denied
release. Serialized held state preserved the countdown; restoring power and the
serialized authority/state did not auto-release. Explicit Resume revalidated the
train; remaining dwell elapsed in server ticks, then DROP/authority epoch committed
before constraint removal. Chain was suppressed on the released hold, and measured
gravity motion was `(0,-6.142,+3.583)` blocks/s. Serialized DROP preserved phase and
release epoch. This is native state roundtrip evidence, not yet a full process
restart or occupied player claim.

The first run exposed the shared loader's generic four-seat ceiling. Lead changed
only Dive's ceiling to six; the same targeted case then passed. No older family
qualification was reset. Both test servers saved and exited; passing Gradle exit0.
Persistent logs/build: `/mnt/NVME4/fourthdimension-runs/fd1702-dive-20261115`.

## Owned-dispatch correction

Launch02's normal player Close followed by Dispatch exposed an unowned default
`authority.held=true` incorrectly treated as a physical hold. The native fixture
had closed and dispatched in one tick, before that constraint formed. Dive now
honors authority hold only with a non-null owner. [Native03](native-03.log) adds a
15-tick closed dwell before Dispatch and passes the complete same case again.
The hold controller also rejects dwell editing for moving cars on its connected
route before their first crest arrival; the targeted test verifies that guard.
This supersedes native02 for the affected dispatch behavior only.

Launch01's first placed view found missing visible seat-to-frame brackets.
Launch02 adds model-only brackets within the same adopted bounds, and the placed
side view confirms attachment. Physics blocks, seats and pivots did not change.
Normal private-client camera turn and reachable vanilla crafting-table use passed
with default bindings and raw input on. Normal empty-hand boarding, Close and
Dispatch input were received. The occupied three-car world was retained for the normal Resume continuation
below; launch02 alone does not claim a complete occupied cycle.

## Occupied hold and post-release restart — passed through return entry

On packaged launch03 (`fd243b4f52e5963771cfb832622d707cd5f7f25a32f8c59c7e082b260e3087c0`),
normal Resume after the dispatch fix carried Dev on the original three-car train
through lift/crest, the measured physical hold and gravity release. The [held row](held.png)
shows the fixed occupied seat/harness and readable28-tick countdown. The [released
row](released.png) remains occupied; native motion was `(0,-12.91,7.530)` blocks/s.
[Saved held](held-save.json) and [saved released](released-save.json) state show all
three cars changing from HELD to DROP, remaining dwell0 and release epoch1.

A disclosed `tick freeze` interrupted the released moving train. Client exited,
server saved/exited, and Xvfb stopped. The complete world was preserved as
`checkpoints/released-world`. On full process restart, all three retained DROP,
zero dwell, release epoch1, closed restraints, and explicit-Resume hold. [Restored
save](restored-drop-save.json), [editor](restored-drop-editor.png). Normal Resume
continued downhill and onto the powered return without a second countdown or
teleport to the crest. [Trace](occupied-trace.jsonl). Same cars:
`2ae6fa16-a782-4ef4-9948-1b51f78408e1`,
`6f42eb48-96b1-4d1b-b221-8d06f37f59e5`,
`6b97bbf8-4430-47e2-9289-d381d325b443`; Dev UUID
`380df991-f603-344c-a090-369bad2a924a` remains assigned to the first.

The return stalled fault-free at the first negative-Z junction near(96,80,352).
Normal Stop retained it; the first Stop request correctly rejected a stale editor
revision, and the refreshed request succeeded. That launch was saved/stopped before the read-only drive, next-edge and collision
diagnostic below. Hold/release/restart evidence above remains accepted.

The [return diagnostic](return-diagnostic-01.json) isolated the cause: the front
car's controller block at local(0,0,-1) retained a full-cube collision shape while
its model was smaller. Its world AABB overlaps the first native shaft at
(97,80,352). All three native drive targets were forward, support and next-edge
eligibility were valid. The correction makes only Dive's controller shape match
its existing model, `box(2,4,2,14,16,14)`. The same stopped occupied train supplies
the affected continuation check; the accepted hold/release cases stay closed.

## Return, dock and unload — passed

Packaged launch05 SHA256:
`90689f37049228900abf1e61ac71ecafe49a1e57235e4065b6fb8a795876c00e`.
The [corrected diagnostic](return-diagnostic-fixed.json) has no controller/shaft
overlap. Normal Resume moved the same occupied train through the former junction
and along the return at8 blocks/s. Safety then correctly stopped for a separate
example platform intrusion at(4,79,-4). The example's platforms now start at Z4,
not Z-4; the identical disclosed block trim was applied to the saved world.
Normal Stop acknowledged the repaired obstruction and Resume crossed that curve.
The operator missed the short dock window during editor refresh, so the same train
made one further circuit with a single Stop monitor armed. This is a disclosed
operator timing miss, not a teleport, forced docking or new acceptance campaign.

One normal [Stop](returned-ready.png) retained all three on the original level
dock at center Z11.90/19.90/27.90. [Saved state](returned-save.json) has all three
`DOCK_CLOSED`, release epoch1, remaining0, stopped/captured and no fault. Normal
[Open](open-dock.png) released their restraints; normal [Shift](unloaded.png)
dismounted Dev to(-3.5,80,11.5). [Walking camera](unloaded-camera.png) and
[unloaded save](unloaded-save.json) confirm the original platform and empty train.
The [return trace](return-trace.jsonl), [input record](return-construction-input.json)
and [setup/readbacks](return-construction-console.json) retain the distinctions
between normal controls and disclosed fixture changes. The active native Chain
provided travel; no scripted vehicle velocity or position change was used.

## Normal Survival construction and controls — passed

Commands supplied17 iron,6 copper,2 white dye,2 redstone,6 Create red seats and
one native Coasters bearing, plus upstream anchors, a crafting table, station,
scaffolds and setup positions. No Dive track, car or hold item was command-given.
Ingredient gathering and upstream station/anchor crafting are not claimed.

1. Normal recipe-book input crafted32 [Dive Track](track-recipe.png), one six-seat
   [Dive Car](car-recipe.png), and one [Hold Controller](hold-recipe.png).
   [Inventory](crafted-inventory.png) and readbacks show the car/controller consumed
   two track, leaving30. All16 packaged Dive resources match the source, and their
   JSON parses: [content record](packaged-content.json).
2. Normal crafted-track use selected native anchors(220,80,12) and(220,80,60).
   The [paid48-block span](paid-track.png) consumed12 pieces, leaving18.
3. Normal car-item use placed chassis `6c91fedc-6a27-4345-9c9d-ee9ef5cdc8ab` at
   (220.5,81.125,26.5) and consumed the crafted item. Normal station use bound it
   to(215,80,24). After native settling, the [editor](crafted-car-ready.png) reports
   Ready; [saved state](constructed-save.json) records six seats, capture, matching
   dock/support and no fault. [Placed view](crafted-car-placed.png) shows all six
   seats, brackets, frame and native running gear. Three-car coupling and occupied
   travel reuse the native/occupied evidence above; no second full ride is needed.
4. The original fixture hold block was removed while the train was empty and
   stopped. Normal item use placed the crafted replacement at(-5,152,280) on the
   supplied redstone pedestal and consumed it. Normal empty-hand use [bound it](hold-bound.png)
   to the existing60-degree section, with the same from/to and geometry fingerprint.
   A first sneak-use hit the pedestal because crouching lowered the camera. One
   calculated upward correction kept the controller targeted; the next single
   sneak-use changed its saved configuration from60 to80 ticks. The final world
   retains80. Its status reports the attached stopped train's phase/countdown;
   the configured dwell change is established by the authoritative block readback.

## Finite FD916 content review

The reference is the adopted six-abreast fixed-row specification. Original white
seats/iron frame, black harness and yellow latch/control accents distinguish the
parts. Vanilla material textures are intentional. No additional variants exist.

| Required criterion | Result/evidence |
| --- | --- |
| Silhouette and proportions | Crafted inventory and placed view show six distinct seats across the wide native N chassis; dimensions match the adopted seat hips and occupied envelope. |
| Assembly and motion alignment | Model-only brackets corrected the initial visible seat/frame gap. [Bracket view](seat-brackets.png), placed car and held/released occupied views show attached seats and fixed harnesses. Controller collision now matches its visible model; the affected native junction passed. |
| Texture finish | Explicit UVs and bounded cuboids use consistent original geometry and intentional vanilla surfaces; no missing textures or unintended placeholders in inventory, placed or occupied views. |
| Controls and readability | Distinct track/car/hold icons and recipes; normal binding, restraints, Stop, Resume and release status/countdown were visible and actionable. Saved80-tick configuration follows one accepted sneak-use. |
| Supported operating poses | Open dock row, closed occupied incline/hold/drop, restored DROP, ready returned dock and unloaded camera are covered by the linked captures. No rotary or alternate seating variant is advertised. |

## Closure and runtime cleanup

Java21 launch preparation passed. Client used1536M heap inside its own6GiB scope;
observed launch05 peak was2,451,513,344 bytes, below the cap. Gradle preparation
remained outside the client scope. The server had its separate4GiB unit. Private
Xvfb317, default mouse bindings and the proven private input helper were retained.

After the final construction/control result, the owned client exited, server
accepted `save-all flush` and `stop`, and all dimensions finished saving. Then the
owned Xvfb stopped. All three units are inactive/MainPID0; no live runtime was
handed off or abandoned. Full logs, input captures and worlds remain under the
persistent root; selected evidence is linked here. No further runtime check is
required by the source checkpoint or documentation reconciliation alone.
