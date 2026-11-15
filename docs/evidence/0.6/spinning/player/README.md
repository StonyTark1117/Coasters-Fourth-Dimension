# FD1501B/C — S1 content and player acceptance

**Complete, 2026-11-15 UTC.** Reuses [accepted A](../README.md) for native Y loading,
free motion, capture, persistence and rider/camera ownership. This continuation
checks normal construction, the corrected example and distinct power-loss/resume.
No original-family, Wing or S1 A case was reopened.

Input main `d5565a09ac0d6230b66c9dd332c7b35fb3840f2a`; isolated packaged JAR
SHA256 `894c3e6efe8ff6414a99b0cf6d1eac51ac9a3a64b57953fdca6bdc1589682336`.
Same Java21/dependency pins and owned run root as A. This immutable run artifact
was retained before Suspended began changing the shared checkout.

## Normal construction and example

Setup commands supplied ingredients, unlocked the two recipes, placed a crafting
table/empty anchors/platforms and positioned Dev. The player was in Survival.
`/fd demo spinning_track` at `(200,90,0)` supplied the separate playable route,
without a car. Commands did not craft outputs, connect the tested two-anchor
section, place the car, bind its dock or mount the player.

- Normal recipe-book/table input crafted16 S1 track from6 iron,2 copper and1 dye;
  [recipe view](track-recipe.png). The car recipe consumed one of those tracks,
 3 seats,2 bearings,2 iron and1 native Coasters Cart, producing one S1 car;
  [recipe view](car-recipe.png). Server inventory readback retained15 track and1 car.
- Two normal uses on actual anchor hits `(180,93,40)` and `(180,93,56)` selected
  and connected `conventional_v1`, consuming4 track and leaving11;
  [placed section](normal-track-placement.png).
- Normal car-item use on the matching example anchor consumed the crafted car and
  created chassis `7df79686-972e-4bb3-a549-227152615d88`, carrier
  `2f84fa0d-187f-4846-9322-5535323ec839`; [placed car](normal-car-placement.png).
- Normal empty-hand use bound the example entrance dock `(197,94,36)` to that car
  and opened Ready controls; [dock](normal-dock-binding.png). Normal right-click
  boarded its actual reachable seat; [state](crafted-car-board.json). An initial
  aim identified the controller, so no use was sent until one image-based correction
  produced the actual seat hit. Mouse settings remained unchanged.

## Power loss and explicit recovery

Normal Close restraints and Dispatch started occupied lift travel. At center
`(200.5,93.625,54.948)`, [before cut](before-power-cut.json), a disclosed fixture
command removed the motor at `(201,93,36)`. The car braked toZ56.627 with
`S1 Chain power unavailable`, restraints and liftHeld retained, boarding denied,
exactly one rider and native engagement intact;
[state](power-loss-stopped.json), [view](power-loss-stop.png).

Restoration initially used a live NBT merge, which did not update Create's generator
callback. This was fixture setup, not a recovery result. Replacing the motor with
`ScrollValue:-64` in the same setblock command initialized its actual kinetic source
correctly; the anchor read64RPM. No restart action was sent while source speed was
unverified. With restored64RPM the car remained atZ56.627, stopped and faulted:
[no automatic restart](restored-power-no-restart.json).

One normal **Resume to dock** click cleared the fault and resumed with restraints
closed and liftHeld retained; [state](explicit-resume.json). The
[return samples](resume-return-path.json) show continued native motion without a
new fault. A single normal Capture click was sent when the returning car was inside
9.5 blocks of the corrected entrance dock, before lift entry. It stopped at
`(200.738,93.625,33.105)` and latched Ready at angle.00401, omega−.0000122, without
unloading or resetting the rider; [state](power-resume-return-captured.json),
[view](return-ready.png). Normal Open restraints, Done and Shift unloaded the rider;
[final state](power-resume-normal-unload.json) has passenger=false, no assignment
and no native seat orientation. No helper boarding or unload command was used.

## Finite content review

| FD916 criterion | Accepted S1 content |
| --- | --- |
| Silhouette/proportion | Original orange four-seat back-to-back turntable, visible vertical bearing/chassis and native N running gear; [placed view](normal-car-placement.png) and [occupied A view](../occupied/occupied-motion.png). |
| Assembly/motion alignment | Corrected plate sits below the cushions; explicit fractional mounts and native rider loading match the seats. Open/closed bars and the occupied rotating body are visible in A and this return. |
| Texture finish | Intentional vanilla orange concrete, iron and black surfaces; no manufacturer artwork. Corrected model coordinates load without errors or fallback. Native Coasters wheels/track resources remain external. |
| Inventory/held/placed readability | Two item models and recipes are recognizable in the crafting/placement views; orange functional track matches the dedicated item. The four-seat assembly, Ready and restraint state cues are visible. |
| Adopted variants | Only `conventional_v1` is delivered here. S2 remains0.8. No extra appearance variant is claimed. |

Inventory: three blockstates (seat, bearing, controller), sixteen seat state/facing
models plus bearing/controller models, two item models, two recipes and localized
names. [Guide](../../../../SPINNING.md) explains crafting, track limits, docking before
lift entry, normal controls and explicit power restoration. Both demo commands are
buildable Creative setup examples; the normal construction evidence above is separate.
The original-family/Wing renderer and lifecycle evidence carries forward where unchanged.

## Save and cleanup

[Server log](server.log), [client log](client.log), [console readbacks](console.jsonl)
and [input records](inputs.jsonl) retain the measured sequence. The
[final schema7 save](final-saved-state.json) contains both S1 cars, no occupants,
settled captured states and their dock bindings; the world remains in
`/home/braydon/.local/state/fourthdimension-runs/fd1501-spinning-20261114/server/SpinningWorkshop`.

Client1536M heap/6GiB cap; observed client peak2.56GB, server peak1.56GB/4GiB cap.
At launch host35GiB available, app.slice unlimited/current37.5GB,263GB disk free.
Final server saved all dimensions05:33:52 UTC and stopped RCON05:33:53. Client,
server and Xvfb`:311` units all inactive/MainPID0 verified. No runtime remains live.
FD1501A–C are closed; FD601 waits only for its other assigned families.
