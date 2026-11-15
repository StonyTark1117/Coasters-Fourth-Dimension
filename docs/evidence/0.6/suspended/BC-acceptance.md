# FD1301B/C complete — 2026-11-15

Owner `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`; primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`.
FD1301A is integrated at `ec27e982ece3e906bf144b25cbb5982b8d230d63`.
This handoff completes the remaining content and player-workflow claims; it
retains A's native and occupied evidence rather than repeating qualification.

## Normal construction and controls

The connected Java21 packaged client used private display312, default right-use/
left-attack bindings and raw input. [Input log](construction/input.jsonl) and
[disclosed setup commands](construction/console.jsonl) distinguish player input
from materials, scaffold/platform preparation, recipe discovery and positioning.
Materials and recipe unlocks were supplied; all three products were crafted
through the normal Survival crafting-table recipe book:

- Two iron + two copper + redstone produced the Swing Latch.
- Six iron + two copper + orange dye produced sixteen Suspended tracks.
- Two Create seats, the crafted latch, one crafted track, four iron and native
  Coasters cart produced the Suspended car. The output was taken and ingredients
  consumed. Screenshots: `construction/{crafted-latch,track-recipe,car-recipe,crafted-car}.png`.
- Two native anchors were placed by right-click at(-201,100,400) and(-201,100,440).
  Two matching-track clicks connected them; the stack changed15→5 for forty blocks.
  The crafted car item was then right-clicked onto the completed matching rail and
  consumed, creating the connected native overhead car. The temporary platforms
  were prepared setup, not claimed as player construction.
- A Boarding Station was placed outside the swept reservation, then normally bound
  to the stopped car. It reached Ready. Normal boarding and seated Rotation Tool
  controls worked. The corrected model also received normal Close/Dispatch/Capture/
  Ready/Open/Shift input for its finite state views; no full circuit was repeated.
- The normal Rotation Tool track screen changed an unoccupied Suspended span's
  Lift lock off→on→off (revisions0→1→2), restoring its original state. Shared
  anchor ties selected the next span; evidence explicitly shows the selected
  50.2968-block span, not an assertion that it was the rising segment.
- Guide now explains deliberate Shift rebind, ordinary bound-dock access, seated
  tool controls, matching track, station/lift dimensions, interruption and recovery.

A separate optional lift-control setup first attempted a scaffold in an unloaded
chunk. It failed; a subsequent positioning command put the unmounted player over
void and caused a setup death. It did not occur during riding or normal dismount.
The chunk was loaded, scaffold placement verified, and normal controls inspected
after respawn. Temporary scaffolds were removed. The earlier Survival dismount
and later reconnect remained safe on the construction platform. Final inventory
samples were granted only to review item appearance; they are not crafting credit.

## Finite FD916 content review

Visual reference: accepted FD1301 dimensions and the original family's iron,
orange seat/accent and dark cyan cabin palette. Native Coasters running gear is
external. No new livery/appearance variants are declared.

| Criterion | Evidence and result |
| --- | --- |
| Silhouette/proportions/style | `visual/final-placed.png` shows native overhead bogie, fore/aft fork, journal, suspended two-seat cabin and matching track at normal scale. Same specified seats, pivot and reservation. |
| Assembly/motion alignment | The isolated journal was removed by native structure splitting. A's integrated correction connects it at y=-1, preserving world pivot and bounding geometry. `journal-before.png` versus `journal-connected-motion.png` and final occupied views show the repaired connection. The affected mass/load/limit/capture check passed; no remaining gap obscures attachment. |
| Texture finish | `uv-before.png` showed automatic UVs outside a sprite sampling neighboring atlas textures. The generator now tiles large cuboids and assigns bounded UVs. `uv-after.png` and final views show intentional materials without the corruption. All12 model files/95 cuboids pass explicit UV bounds0..16 and element bounds[-16,32]; generator output is reproducible. |
| Player-facing appearance | `final-inventory.png`, `final-occupied-open.png` and `final-occupied-free.png` show readable miniature, normal seated pose, open/closed restraints and green/orange journal indication. State readbacks are `final-{free,captured}-visual.json`. |
| Supported variants | One declared Suspended variant; no extra family/livery/profile sweep is required. Shared native rendering and unmodified materials retain prior evidence. |

The final UV review used an exact model-only resource-pack override on the
journal-qualified packaged JAR. [Model hashes](visual/source-models.sha256) match
both source and override byte-for-byte; the client reload log places
`file/fd1301-visual-review` after `mod/fourthdimension`. An initial lower-priority
override was ineffective, diagnosed from that order, then corrected. This is
source-asset evidence, not a claim that a new JAR was built while Fixed owned
shared Java. Lead packaging includes these source model files normally.

## Cumulative acceptance

[A handoff](A-handoff.md) covers native Z dynamics, exact equal rider loading,
asymmetry, both physical stops, bounded capture, native four-car spacing/Chain,
retained power-loss locks and explicit resume, all-rear exit, pending entry saved
unlocked, pose/topology persistence, and the connected normal occupied banked loop.
The journal correction supersedes the old empty-mass4 input with measured mass5;
its targeted pass preserves unaffected train/input/camera evidence. The accepted
S1/Wing shared seat/lifecycle/synchronization paths carry forward; no new remote
rider implementation was introduced by Suspended. This record supplies the missing
normal construction and content finish. No FD1301B/C acceptance criterion remains
open; FD1302/other families and FD916 aggregate work remain their own assignments.

## Cleanup and integration

[Cleanup](construction/cleanup.json): owned client/server/Xvfb units inactive,
MainPID0; display socket312 absent. Server logged all dimensions saved at
06:24:07UTC and its RCON listener exited at06:24:10UTC. No owned runtime remains.
Saved world and full logs remain at
`/home/braydon/.local/state/fourthdimension-runs/fd1301-suspended-20261115/server/SuspendedWorkshop2`.
Old diagnostic worlds remain preserved. Compressed final server/client logs,
input history and selected captures are in this package.

Ready for lead integration: `scripts/generate-suspended.py`, Suspended-only
model JSON changes, `docs/SUSPENDED.md`, and `docs/evidence/0.6/suspended/` updates.
No Java or shared language edits after A's transfer to Fixed. Lead retains all
Git/central-doc operations. Other outstanding working-tree files belong to Fixed
or the lead and must remain untouched by this handoff.
