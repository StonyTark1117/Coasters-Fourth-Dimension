# Bounded track vertex emission repair

The valid comparison15 retains a failed frame p95 of16.563280 ms against the
unchanged15.9336771 ms ceiling. Extra warmup did not resolve it. Its tick p95
and frame p99 passed; the accepted sustained14 workload/memory and completed
mounted views remain valid with their original artifact identities.

The separate60-second Pipeline diagnostic on immutable runtime14 ran after
warmup16 and before the remaining mounted segments. It is excluded from acceptance
timing. [Raw file identities](client16-diagnostic-files.json),
[commands/GPU observations](client16-pipeline-profile.jsonl) and
[sample ranking](client16-profile-ranking.json) preserve the finding. Of4478
render-thread execution samples,2185 ended in native vertex-buffer capacity
checking,1009 in OriginalTrackRenderer.draw and403 in HashMap.computeIfAbsent.
GPU utilization averaged approximately22%; native swap and buffer-upload samples
were a small fraction. This locates current CPU emission cost; it does not prove
why two earlier JVMs differed despite equivalent settings.

The correction uses Minecraft's full-attribute vertex overload, transforms
the shared quad normal once, reuses two frame-local vector scratch objects, and
avoids repeated map lookups for consecutive identical material/light positions.
It preserves vertex order, positions, colors, UVs, light, overlay, normals, render
types and mesh construction. Frame caches still clear every frame; it adds no
retained mesh, GPU buffer, light cache or resource lifetime.

Affected checks are the native byte-equivalence harness, one representative
nonempty rendering/reload observation, and one fixed300-second comparison in
each affected base/combined profile against its existing frozen ceilings. The
native harness covers both native vertex formats, direct and fallback consumers,
five poses including nonuniform/reflected scales, three alpha values, buffer
growth and mixed ordinary/bulk writes. Attribute bytes and draw state must match;
only unwritten native padding is excluded. All60 cases passed, comparing8,110,080 attribute bytes; see [native result](track-vertex-byte-check.log).

The change reduces temporary emission allocations without changing any retained
allocation or sustained-load lifecycle. The completed1800-second workload,
memory plateau, mechanics, all accepted body/camera/appearance views and earlier
graphics-mode ownership remain closed. The bounded render check answers the
changed emission path; it does not restart an all-family art review.

The Java21/Gradle9.2.1 [build passed](render17-build.log). Candidate JAR SHA-256
`77170d33e8a4a6993b3d4c2fc4f70ed3a491f19f83948fa75ced752ed485b5c5`.
The [entry comparison](render17-jar-diff.json) changes only OriginalTrackRenderer
and its nested class files plus TrackVertexWriter; all resource and mechanics
entries remain identical to runtime14. The representative Fancy reload rebuilt meshes8→16, restored visible track and
recorded4,992nonempty geometry stages by23:01:35 local time; [events](render17-reload-events.log)
and [view](render17-after-reload.png) preserve the readback.

The [base300-second comparison](comparison-base17-verdict.json) passes all frozen
frame/tick/RSS/post-GC limits: frame p95/p99=8.424988/10.974221ms and tick
p95/p99=34.852053/44.991893ms. Both recorders closed losslessly with zero drops.
[Raw evidence](base17-raw-manifest.json), normal Open/Shift, save/checkpoint and
[all units inactive/PID0](cleanup-base17.json) are preserved. The saved base world
received the already-accepted west Flying endpoint drive correction before warming;
no extra vehicle, rail or rider was added. The combined later-regression comparison passes under the existing budgets, as
reconciled below.


## Combined comparison and evaluator correction

The [final exact300-second summary](comparison-combined17-summary.json) contains
42,548frames (p95/p99=13.344202/17.867497ms; no frames over50ms) and6,000ticks
(p95/p99=50.088835/71.604131ms;303ticks over50ms). All17fixed views and actual
Arrow motion are present, with90riders and no faults throughout. All300resource
samples retain the6GiB/4GiB scopes with zero max/OOM events; RSS/post-GC medians
pass their existing110% ceilings. Both JVM recorders are lossless with zero drops.

The [original verdict](comparison-combined17-verdict.json) is retained unchanged.
It incorrectly reapplied the **initial-baseline**50ms tick-p95 limit to this later
candidate. Its live guard read5,988ticks before the writer flushed and reported
50.229132ms; the final lossless interval contains6,000ticks and50.088835ms.
[PERFORMANCE](../../../../PERFORMANCE.md#measurements-and-frozen-budgets) explicitly
separates initial acceptance from later110% regression ceilings. The already-frozen
later tick-p95 ceiling is53.871642ms; it has not been raised. Initial13r2 and
sustained14 retain their actual accepted <=50ms observations. Candidate17 changes
only client renderer classes, with every server production entry identical14.
This result is not represented as a new <=50ms initial-baseline pass.

Because the inherited guard exited after the full300seconds, the original helper
omitted its final success marker. The [read-only reconciliation](reconcile-combined17.py)
therefore checks actual coverage independently: seventeen ordered view starts/ends,
verified Spectator, occupied Arrow movement20.76blocks after100ticks,34state
observations,6001world-tick advance, exact6000recorded ticks,42,548in-world frames
with no menu/pause,300resource samples, expected scopes and clean lossless footers.
The [reconciled verdict](comparison-combined17-reconciled.json) passes every
pre-existing frame/tick/RSS/post-GC regression ceiling. Raw measurements and the
initial guard failure remain in the [manifest](combined17-raw-manifest.json).
No samples are removed, no budget is changed and no retry is needed.

[Original helper versions and corrected future budget selection](render17-classifier-correction.json)
keep the initial50ms default for initial scenes and pass the frozen tick ceiling
explicitly for later comparisons. This is a measurement-classification correction,
not a production mechanics change or waiver of a required criterion.

Normal Open/Shift, complete fixture unloading, save and checkpoint `world-drained17`
completed. [Final cleanup](cleanup-combined17.json) verifies client, server and
private Xvfb inactive with MainPID0. No runtime is retained.
