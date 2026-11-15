# Axis content and cumulative player acceptance

FD1302B/C complete, 2026-11-15. Owner `01a1dd1d-1468-7f81-8f31-5532ad7a178d`,
primary `main`. Mechanics and occupied cycle reuse [A handoff](A-handoff.md).
The same artifact04/runtime continued explicitly for these remaining content
claims; no second ride or unrelated native suite was needed.

## Normal construction and controls

Survival crafting used the normal recipe book and crafting table. Supplied raw
materials produced 16 Axis Track, then one Axis Car (consuming one track), with
iron64→54, copper16→14 and cyan dye8→7. [Track recipe](content/track-recipe.png),
[paid output](content/track-crafted.png), [car recipe](content/car-recipe.png) and
[car output](content/car-crafted.png) retain the observed result.

Two supplied native anchors were placed by ordinary right-click at(200,84,200)
and(200,84,240). The crafted track item selected and connected them normally;
15→5 tracks paid for the 40-block curve. [Anchor](content/anchor-placed.png),
[paid track](content/paid-track.png). The actual crafted car was right-clicked
onto the matching graph at09:09:50UTC and consumed. Native chassis
`e6d8aab4-9dc8-44ec-9e3b-7524a5eb33b1`, child
`7e85839f-a5d7-4ec4-8ba8-97099666c222`, mass4, no fault. See
[placed car](content/crafted-car-placed.png) and retained trace/input/console.
A already proves normal Chain coupling, seat boarding, restraint controls,
dispatch, driven/banked motion, retained restart, dock recovery and normal unload.
Those compatible observations complete the workflow without duplicating the ride.

Disclosed setup limitation: the first construction-area teleport preceded chunk
loading/platform placement, so Dev fell and died. After loading and verifying the
platform, normal Respawn was used. The existing dropped crafted car, 15 tracks
and remaining materials were inspected and moved to the safe platform by setup
commands, then picked up normally. No replacement crafted outputs were given.
This setup failure does not establish ordinary survival travel. Platforms, light,
positions and aim were command setup, not a camera-control claim; the same
client's normal camera and vanilla use were already established in A.

The normal Rotation Tool editor opened on an unoccupied DAMP section. Three
individual clicks and server acknowledgements changed DAMP→DRIVE→FREE→DAMP,
revisions0→3, restoring the original mode. [DRIVE](content/mode-drive.png),
[FREE](content/mode-free.png), [restored DAMP](content/mode-damp-restored.png).
The first native anchor overlay had no ordinary block hit. A later support click
arrived but was too far from the rail: nearestGraphHit's limit4 is squared
radius, not four blocks. A support within two blocks resolved the section and
opened normally. No input remapping/repeated unchanged clicks were used.
The temporary support was removed before saving. DRIVE accepted the adjoining
2π/0 physical orientation under Axis's existing modulo junction check; no new
unwrapped profile or continuity claim is inferred from this UI cycle.

## Finite FD916 review

The single declared longitudinal_roll_v1 asset set uses an original outboard
longitudinal journal/fork, two cyan seats, iron structural surfaces, dark padded
restraints and visible yellow closure cues. The proportions follow the stable
Axis specification; native bogie/Chain artwork remains third-party content.
[Model inventory](content/model-review.json) records 19 authored models and
hashes. Every authored element is inside[-16,32]; each face has explicit UVs
inside[0,16]. Parent/material references are deliberate Minecraft assets.

| Criterion | Accepted views/result |
| --- | --- |
| Silhouette/proportions/style | Placed car, recipe icon, A occupied roll show the offset pod and longitudinal journal rather than Suspended geometry. |
| Assembly/motion alignment | A bank/roll, closed restraints and ready/unload views agree with native hips/journal evidence. Iron forks/journal remain aligned, no unintended visible separation. |
| Texture finish | Cyan/iron/dark pad/yellow surfaces render intentionally; no missing fallback or unintended UV stretch in retained views. |
| Player appearance/state | Recipe/held output, placed car and normal seated/closed/open views are readable at gameplay scale. Tool modes and Ready/open rejection are visible. |
| Supported variants | One declared Axis variant; no extra liveries or appearance matrix is promised here. Shared unchanged track/seat presentation carries its existing review. |

No criterion failures remain; additional aesthetic preferences do not reopen this
review. Functional geometry did not change during B/C. The player guide now
explains track targeting and the proven dock-edge capture/resume sequence.

## Saved state and cleanup

Persistent root `/mnt/NVME4/fourthdimension-runs/fd1302-axis-20261115`, saved world
`server/world`, artifact04, display316, ports25650/25651. Client1536M heap/6GiB
scope; Java21. Client stopped first, then server received RCON stop. All dimensions
saved at09:19:11UTC; server exited, then Xvfb stopped. [Unit verification](content/cleanup.txt)
shows client/server/Xvfb inactive with MainPID0. PIDs852429,875781,875389 and old
native829672/838261 are absent. Worlds, original captures and native logs remain
on persistent storage. Curated input/console/client/server/native traces are
compressed under content/. No owned runtime remains live.
