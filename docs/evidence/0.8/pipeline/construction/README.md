# Pipeline normal construction and content

FD1401B and FD1401C complete on 2026-11-15. Normal construction and the finite
content review passed; FD1401A remains accepted unchanged.
Owner chat01a1dd1d-6e99-70d2-aef3-6dcebac10d72, primary main; lead owns commits.

Supplied fixture inputs were iron/copper/blue dye, Create red seats, native Coasters
bearings, normal Chain and Rotation Tool. Commands supplied upstream anchors,
crafting table, stations, safe scaffolding and setup positions. No Pipeline Track
or Car was supplied by command. Ingredient gathering/upstream scaffolding/station
crafting is not claimed. All construction actions used the Survival client.

1. Normal recipe-book/crafting input made32 [Pipeline Track](track-recipe.png)
   with `ICI/IDI/ICI`, avoiding the Free Spin blue recipe. Then two [cars](car-recipe.png)
   consumed eight red seats, six iron, two native bearings and two crafted track.
   [Inventory](crafted-inventory.png): two distinct car items and30 track. The
   [artifact record](recipe-artifact.json) verifies production recipe bytes exactly
   match the tested launch02 JAR; no runtime recipe overlay was needed.
2. Normal crafted-track use selected `(220,80,12)` then `(220,80,60)`; the native
   [48-block paid span](paid-track.png) consumed12 pieces, leaving18. Selection
   component readbacks distinguish the two clicks from the fixture anchors.
3. Normal car-item air-use placed two native cars at Z26.5/34.5. Both crafted
   car items were consumed. Native P contact models, four seats and floor/frame
   appeared. Normal station empty-hand use bound each; a subsequent click was
   initially delivered while the first station editor remained open, so it earned
   no binding claim. After closing that editor, the actual second station hit and
   successful binding were read back. No gameplay input diagnostic was repeated.
4. Normal Minecraft Chain use selected the first bearing and then the second.
   [Native preview](chain-eight.png) read8.0 blocks, and server logged
   `89f33d14-c982-42b1-a7ad-f4937c4e8f00` linked to
   `a04f910b-04c6-45b0-b617-b22e72ba358a` at8.000. Native Chain retains its stack;
   no unsupported consumption claim is made.
5. Normal Shift-right-click on the first station rebound the complete train.
   [Ready editor](bound-consist-ready.png) and [saved state](saved-built-consist.json)
   show both cars stopped, captured, fault-free and boarding-ready with one dock
   `(216,77,26)` and the same P dedicated rail. Temporary upper scaffolds were
   removed. The construction span is unpowered; the separate [A handoff](../README.md)
   supplies occupied forward/reverse/roll/recovery/unload without duplicating it.

[Inputs](normal-input.json), [setup](setup.jsonl), [readbacks](readbacks.json) and
[server events](server-events.log) preserve the distinction between setup and
normal construction. World checkpoint `BC-constructed-world` is preserved under
`/mnt/NVME4/fourthdimension-runs/fd1401-pipeline-20261115/checkpoints`.

## Finite visual review

Reference is the adopted seated P-gauge specification. The original blue/iron,
black harness and orange latch palette distinguishes seats, frame and retention.
Inventory silhouettes show between-side-rail seating and the deep U support.
Open/closed seats, original six-direction running gear, native occupied roll and
player camera clearance reuse A views. No optional visual variants are advertised.

| Required criterion | Evidence/result |
| --- | --- |
| Silhouette/proportions | Crafted inventory, placed pair and A occupied views identify four fixed seats below/between P rails. Dimensions match the adopted hips, floor and wheel layout. |
| Assembly/motion alignment | A contact/rider views and same-body rendering passed. The [final placed view](placed-before-support-joint.png) exposed a missing small connection from the P rails to outboard support posts. Lead added P-only L saddles in OriginalTrackRenderer; mechanics and all A evidence remain valid. The [corrected stationary view](placed-support-joint.png) confirms each rail joins the outboard post through the small saddle, with the wheel path and between-rail car clear. Passed. |
| Texture finish | Generated original cuboids have legal bounds and explicit UVs. Intentional vanilla metal/concrete surfaces are consistent in inventory, placed and occupied views, with no missing textures. |
| Player controls | Distinct car/track recipes and inventory silhouettes; readable restraints/Stop/Resume/station controls, same-dock opening and native motor direction UI observed. Shared editor overlap was fixed during the necessary A recovery restart. |
| Supported variants | One seated Pipeline variant. No standing spring, transfer track or arbitrary appearance coverage is claimed. |

Launch03 solely adds the missing saddle visualization: SHA256
`bba2a73ee76992d9d47fed8fb77224b9a6915b0efbafa9fcdc45db249c206477`.
Java21 preparation passed. A usage-limit interruption occurred after server/Xvfb
started but before client launch. Recovery verified owned cgroups/PIDs, loopback
server and zero players; the same saved world resumed for the bounded view.
No accepted check is reset by this interruption or new artifact hash.

## Closure and cleanup

The compatible A occupied evidence plus this normal construction and content
record completes FD1401B/C without replaying A. Launch03 inspected only the changed
stationary support joint. Client stopped, server accepted `save-all flush` and
`stop`, and all dimensions finished saving. Client, server and private Xvfb317
units are inactive with MainPID0 (2026-11-15 12:53 UTC). The saved workshop,
checkpoints and full logs remain in the persistent run directory above.
