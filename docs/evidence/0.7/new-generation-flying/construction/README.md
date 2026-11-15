# FD1802B/C, FD1802 and FD1803 acceptance — 2026-11-15

Owner `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`, primary
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`, mechanics
checkpoint `3de3d128`. Lead owns Git integration and canonical roadmap/queue.
No separate branch or worktree. This record closes B/C and reconciles the full
New-generation Flying outcome with [qualified A](../QUALIFIED-A.md).

## Normal construction

The owned Java21 client on private Xvfb318 used default right-click/left-click
bindings and raw input throughout. Dev was connected to the dedicated server,
in Survival, for the crafting and placement actions. The final mechanics JAR is
[artifact08](../client/artifact-08.json). The exact source magenta recipe was
loaded as a data-pack override, with [identity](recipe-identity.json). The isolated
construction site is separate from the already completed occupied demonstration.

Commands supplied raw ingredients/upstream seats and carts, anchors, two Boarding
Stations, work/access platforms, a crafting table, Chain and Rotation Tool. Setup
teleports positioned the player in reach; they do not claim ordinary camera
control, which the earlier occupied run already proved. No NG track/car/marker
was command-supplied for construction. Each item was crafted and placed through
normal input. A later extra car item was given solely for corrected visual review
and removed afterward; it is not counted as Survival construction.

| Required action | Observed result |
| --- | --- |
| Normal recipe-book/crafting-table use | [Track](track-recipe.png): 6 iron + 2 copper + 1 magenta dye produced 16 pieces. [Two cars](car-recipe.png): 8 iron + 6 red seats + 2 native carts + 2 crafted track consumed. [Marker](marker-recipe.png): 3 iron + compass + glowstone consumed. [Inventory](crafted-inventory.png) shows both cars, marker and 14 remaining track. |
| Paid native track connection | Normal two-anchor use at `(220,73,12)` and `(220,73,60)` created the dedicated 48-block curve, consuming 12 track and leaving 2. [View](paid-track.png). Native curve starts at Y73.75. |
| Crafted car placement | Normal car-item use placed chassis `8471e37d-9211-403f-abbd-de2d5832145b` and `4bde2e76-cf57-48a0-bf14-8224aa562f90`, at Z26.5 and34.5, consuming both car items. |
| Station and Chain | Each station bound via empty-hand right-click. Normal Chain selected the first bearing then linked the second at exactly8.000; native selection cleared. Empty-hand Shift-right-click rebound the complete two-car consist. [Native log](native-chain-and-cleanup.txt), [station](station-binding.png), [placed pair](placed-pair.png). Both saved cars are seated/open, empty, ready, fault-free and share the two-member consist. |
| Crafted marker placement | Normal use placed the [marker](placed-marker.png) at `(212,69,39)`, consuming it. Saved owner matches Dev; marker identity is retained in the edited interval. |
| Normal track authoring | Rotation Tool on the first native anchor opened the [finite form](track-form.png). Saved `Built scenery`, TARGET0–48, posture0→0, keys0:0;48:0, marker212 69 39, blend8, NEAREST, speed.6/acceleration.8. [Server acknowledgment](track-save-ack.png) says “4D settings updated.” [Saved data](saved-construction.json) confirms revision1, exact geometry fingerprint and marker UUID/dimension/owner. |

[Normal input record](normal-input.json) retains action, expectation, private window,
connection and raw screenshot paths. Raw console/input and all worlds remain in
`/mnt/NVME4/fourthdimension-runs/fd1802-new-generation-20261115`.

Two setup misses were diagnosed before retry: the copied Conventional placement
pose stood inside a station and moved the player out of the intended ray; a stable
position at X215.5 corrected it. Then the declared larger compound envelope
correctly rejected platform block `(217,68,23)`. Removing the owned X217 platform
strip kept the floor at X210..216, outside the NG2.75-side reservation; the same
ray placed both cars. No settings remap, physics bypass or generic input rediscovery.
The copied purple recipe initially overlapped another variant; the NG recipe now
uses magenta dye, and normal crafting above verifies this exact correction.

## Finite FD916 content review

Reference: stable FD1802S compound two-seat dimensions and the package's authored
purple upholstery/frame, cool metal, dark harness, amber latch and green state
cue materials. Comparison uses ordinary daylight at the native test site and
the accepted occupied views, at normal854×480 gameplay scale. Inventory includes
dedicated track, car miniature and marker; placed assets include the native
bearing, eight-cell yaw deck, inner seats, independent body/leg restraints,
marker, native track and shared station/Rotation Tool controls. The route form
and actuators provide the specified transition equipment; there is no separate
unimplemented transition machine.

The finite correction list contained one visual defect: the car item omitted
its suspension arms and foot links, leaving visibly disconnected parts
([before](held-car-before.png)). The generator and item JSON now include connected
outboard supports, lower crossbeam and foot links, plus fitted first-person
transforms. Native dimensions, body geometry and mechanics did not change.
The [exact source resource override](item-model-identity.json) was selected above
Mod Resources in the normal UI. [Reload order](resource-reload.txt) places it
after `mod/fourthdimension`; the [held result](held-car-after.png) visibly contains
the new supports. [Inventory result](inventory-after.png) remains recognizable.
Generator validation passed for all16 bounded models, eight deck cells and five
authored material tiles. This closes the affected item case without a ride replay.

| FD916 criterion | Acceptance |
| --- | --- |
| Silhouette, proportions, style | Purple two-seat compound frame is distinct from the four-seat Conventional car. [Placed pair](placed-pair.png) and corrected item show the outer suspension frame and inner seat row. Intentional block-scale approximation, not a manufacturer replica. |
| Assembly and motion alignment | Native wheel/bearing/deck/seat connection is continuous in the placed view. Existing [prone rider](../client/prone-rider-front.png), [combined transition](../client/combined-transition-camera.png), [late join](../client/late-join-native-rider.png) and [Show](../client/occupied-show-complete.png) show the actual hierarchy and rider/restraint relationships through operating poses. Native hinge-gap and clearance bounds remain accepted in A. Item gap corrected above. |
| Texture finish | Authored material tiles cover all declared NG surfaces; no missing-texture checkerboard or unintended placeholder in inventory, held, placed or occupied views. Bounded model UVs validated by generator. Shared native track/bearing materials are external and unchanged. |
| Player-facing appearance | Recipe names, held/inventory silhouettes, marker, target form/save feedback, actual angle/lock/fault labels and separate body/leg cues are legible. [Body](../client/body-closed.png), [legs](../client/legs-closed.png), [unload](../client/normal-unload.png) preserve visible restraint state. |
| Supported variants | This package declares the one original NG appearance. Shared unchanged station/Rotation Tool/native bearing reuse accepted evidence. Optional skin/seat integrations remain their roadmap packages, not additional B acceptance gates. |

No required visual criterion remains open. Later FD916 reconciles these retained
views; it need not repeat this family review.

## Complete workflow and outcome reconciliation

The [player guide](../../../../NEW-GENERATION-FLYING.md) documents normal recipes,
construction, Chain spacing, swept space, owner-only empty/stopped section editing,
local marker identity, winding, bounds, named Show, dispatch/retry, restraints and
explicit seated unwind before unload. The corrected optional powered example
demonstrates seated sideways presentation, seated target, simultaneous transition,
prone target through a full roll, clear-span named Show and seated return.

FD1802C combines normal construction above with the actual Dev occupied workflow
in [A](../QUALIFIED-A.md): both-posture targeting, nonzero-yaw posture sweep, full
bank/inversion, independent look/comfort, two-client late join, saved occupied
restart and explicit recovery, loaded native Show and normal returned unload.
Native four-car/reverse/endpoint, marker loss/owner/projection, winding and
power/hierarchy fault cases retain their named passing evidence. Shared restored
look is covered by the lead's representative occupied reconnect correction.
Those accepted paths are unchanged by the recipe and item-only correction.
Construction and occupied acceptance are cumulative; command-built demo geometry
is not offered as proof of player construction, nor setup teleports as camera proof.

FD1802 is therefore complete. FD1803 combines this full outcome with Conventional
Flying's already accepted A–C in `fe993053` and its
[acceptance record](../../conventional-flying/README.md). Conventional remains
four-seat station-only changeover; NG is the two-seat compound route-changeover
and shared-yaw consumer. Neither variant is substituted for the other. Both
outcomes close without a joint replay; FD701 may reuse them. No0.8 work started.

## Cleanup and preservation

All owned units are inactive/MainPID0: client, server, Xvfb, observer and observer
Xvfb. Client exited, console `stop` saved every server dimension, then display
stopped. [Verification](cleanup.txt). Temporary Chain scaffold and anchor access
pads were removed, all ten construction force-load tickets released, and the
visual-only extra car item removed. No runtime remains for handoff.

Saved world `server/NewGenerationClearShow` contains the completed ride and the
separate two-car construction. Earlier diagnostic worlds, launch01–08 artifacts,
native test worlds, logs, screenshots and raw input remain under the persistent
run root. Client heap1536M/scope6GiB, server heap2048M/scope4GiB, Java21; no host
desktop input or another task's runtime was used. Lead receives only this owner's
final generator/item correction, guide and evidence for main integration.
