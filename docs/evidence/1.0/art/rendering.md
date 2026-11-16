# FD904A — final rendering reconciliation

**Complete, 2026-11-16 UTC.** Accepted original-family rendering, later distinct
rider/body poses and FD917 production views combine with the current changed-path
mode/reload check below. No second all-family art review was required.

## Retained and affected inputs

The [exact FAST/FABULOUS gates](../../0.9/fd401-render-modes-exact-2c5d9a7/README.md)
and FD401B occupied/default/Styles results retain their original tested identities.
[FD917](../../fd917/README.md) supplies all 17 production styles, actual-pose
Arrow transmission, shared track, equipment/state, optional appearance ownership,
normal occupied fit and clean final cleanup. [Camera reconciliation](camera-reconciliation.md)
maps later family-specific body/seat/camera evidence and effects ownership.
FD402's accepted recovery views and measured solver/rider bounds remain scoped
to their recorded routes, equipment and samples.

FD917 changed shared track/Arrow rendering, so this run checks those current
paths on the immutable frozen JAR
`9c14b760e71c69295d697ccd2ef317144d817d96f944094ebb618382af2b406f`.
It does not relabel older mode runs as the final artifact. The
[frozen graphics set](../lead/freeze.md) is Minecraft FAST/FANCY/FABULOUS;
no Sodium/shader combination was advertised or added.

## Current nonempty geometry, reload and shutdown

The [migration run](migration.md) reused one scene with the historical Arrow,
Legacy and Free Spin native bodies/tracks. Main client used854×480, render and
simulation distance5,40FPS cap, vanilla resources, Flywheel indirect, Java21,
1536M heap in a6GiB owned scope. Renderer is Mesa26.2.6 llvmpipe/LLVM21.1.4,
OpenGL4.6; this is technical correctness evidence, not native-GPU performance.
Combined-expanded dependencies match freeze; Immersion remains client-only.

| Mode | Current counted geometry and readback |
| --- | --- |
| FAST | First launch explicitly logs graphicsMode=fast and flywheel:indirect. Final23,087 nonempty geometry stages,109,428 in-range rail submissions and361,889,384 quads. F3+T at00:30:39 rebuilds meshes8→16 and visible geometry returns; normal Quit Game at00:34:04. [Events](first-client-events.log). |
| FANCY | Normal [Video Settings selection](fancy-mode.png) in the instrumented main client's second launch. Counter4245→5371: **1126 new nonempty stages**. F3+T at00:37:14 rebuilds meshes8→16; both riders/track/Arrow remain visible. |
| FABULOUS | Normal [Video Settings selection](fabulous-mode.png), same instrumented client. Counter6494→8302: **1808 new nonempty stages**. F3+T at00:38:09 rebuilds meshes16→24; original track and Arrow/native wheel geometry return with both rider assignments intact. |

[Mode epoch markers](render-mode-markers.jsonl) and
[second-launch render/reload events](restored-client-events.log) distinguish
counter deltas from cumulative values. The startup ENV line remains FAST when a
mode changes live; the actual UI screenshots and saved options identify the later
modes. We do not attribute the whole cumulative count to Fabulous.

The separately restarted observer also selected Fabulous offline and showed both
restored riders. `runObserver` does not currently wire `fdRenderValidation`, so its
views are not claimed as stage-counter proof; the main client's normal mode
changes provide the counted evidence without another launch.

[FD917 appearance/ownership checks](../../fd917/art/appearances.md) preserve
provider assets and optional skin ownership. Current views show the expected
original shared-track geometry and Arrow mechanism/native gear, with no missing
or doubled owner. Historical geometry has no new optional appearance conversion;
this check is not another provider-art review. Mode reloads produced no new
render failure. Full logs also retain the unavailable Linux narrator, optional
Iris class probes, LAN/no-device warnings, vanilla shader sampler warning and
invalid resource paths in upstream Waterparked/Track Styles packs. Their repeated
startup/reload diagnostics are not described as clean logs or attributed to Fourth
Dimension geometry; the required current geometry/reload/shutdown readbacks pass.

Both clients then normally unloaded, disconnected and Quit Game. The server saved
and exited; [all owned units/displays are inactive](runtime-cleanup.txt).
[Log hashes/paths](retained-logs.json) preserve the complete originals. No runtime
is kept idle after acceptance.

## Limits and handoff

Per-stage averages describe only the addon render stage, not whole-frame GPU
cost, FPS guarantees or a full park. FD904B owns the representative scene,
hardware/settings, whole-frame/tick percentiles, memory/GC and sustained run.
All prior solver/frame/rider evidence retains its measured bounds. These records
close FD904A's finite correctness/ownership/reload/shutdown criteria; performance
acceptance and the FD904 aggregate remain lead-owned.
