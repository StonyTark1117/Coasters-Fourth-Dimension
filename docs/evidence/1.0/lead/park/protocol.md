# FD904B fixed measurement protocol

Frozen before collection, 2026-11-16 UTC. This is the first measured candidate;
there is no duplicate baseline/candidate replay. Source checkpoint `a29c86c4`,
measurement helper-overlay JAR SHA256
`a198b49ee1307246b758d5eda508926089941bc889d821f662c7ac2e8b9a5a64`.
The original packaged checkpoint `a6d6ddc7…` and unsuccessful preparation
records remain immutable; [fixture corrections](preparation.md) change no
production class. This amendment precedes the first timed sample.
The opt-in park helper and setup commands are disclosed benchmark scaffolding,
not normal player construction/control acceptance.

## Workload and settings

The existing saved park contains17 styles,21 chassis,34 carrier records,
198 functional rails,1680 forced route/controller chunks and90 occupied native
seats. Arrow has2 linked chassis; PAX has4. Other styles have1. The connected
player replaces one fixture pig while mounted; no closed-restraint forced
unmount is used. The original example geometries remain separate within the
supported ±1024 X/Z area. S2/Powered governors and the Flying posture/show paths
use their normal operator APIs. Dive transfers its retained crest authority
to its bound station before the next dispatch; the station control is rebound
from(545,110,128) to(545,110,116), within8m of the unchanged capture point. Each group repeats dispatch, travel, capture,
posture/unwind where required and a40-tick station dwell.

Base uses Original appearance. Combined-expanded reuses a clean saved copy of
this world, alternates the two supported saved Arrow styles, and substitutes
one station-span Engineered controller at speed4/FORWARD for that span's native
Chain. Engineered's declared energy requirement is false for this benchmark;
its required positive FE behavior has separate FD901 acceptance. Record the
actual controller, profile dependencies and world hashes. Immersion is client-only.

Both profiles use the host Radeon RX7900XTX through the verified private-Xvfb
VirtualGL route,1280×720,FANCY,render12,simulation8,VSyncoff,unlimited FPS,
no shader, clear fixed daytime (time1000; daylight/weather cycles disabled),
normal HUD (spectator park views; Creative hotbar while mounted), debug
overlay off, and the recorded Flywheel backend. The client has1536M heap/6GiB
scope, dedicated server2048M heap/4GiB scope. Record actual hardware, driver,
launch flags and process groups. The user's normal desktop remains running;
no other task-owned Minecraft or Gradle work overlaps timed windows.

## Finite sequence

Style order: Arrow,Legacy,Free Spin,Revised,PAX,Wing,Spinning,Suspended,
Fixed Inverted,Axis,Roller Ball,Conventional Flying,New-generation Flying,
Pipeline,Dive,S2 Spinning,Powered Inverted Yaw.

Warm the fully occupied, cycling park and native client for at least300seconds.
Visit all17 fixed park camera positions in the listed order,10seconds each,
during warmup. Pause Arrow's next dispatch and wait for its normal station
capture before beginning the comparison window; this may extend warmup.

The base300-second sample and first300seconds of the combined sample share
this order:17 fixed park views of10seconds each, followed by normal Arrow
open/mount/close/dispatch and its actual occupied moving view for the remaining
window. Record exact transition timestamps; setup delays remain in raw samples.
The combined run continues as one uninterrupted1800-second mixed-family window,
with the player riding Arrow and all17 groups cycling. Freeze110%p95/p99 later
regression ceilings from the observed initial baseline; keep the initial tick
p95<=50ms/20TPS criterion and existing subsystem limits unchanged.

Actual mounted performance views for the other16 styles are finite supplemental
segments under the combined profile and the same settings. Preserve at least
100game ticks of actual occupied movement per style, selected to show that
style's mechanism, with normal safe capture/unload between transfers. If safe
transfer requires waiting for a route to return, retain that elapsed record;
there is no extra full-cycle acceptance assertion. These segments are timed
with the same frame/tick/resource collectors and reported separately. They are
not spliced into the1800-second sustained window, and prior camera acceptance
is not substituted for a missing timed occupied view. No further family/profile
cross-product is scheduled.

## Measurements and completion

Preserve client CPU-frame intervals (including VirtualGL transport, not physical
monitor presentation), whole-server tick work, one-second heap/RSS/cgroup,
JFR GC/post-GC events and actual per-style view timestamps. Report count,
median,p95,p99,max,frames>50/100ms,ticks>50ms,GC distributions and the final
three five-minute memory periods. Retain outliers and separate startup/warmup.
The initial hardware-tier frame result is observed, not a universal FPS promise.

Stop each JVM recorder via its `.stop` file and verify its lossless completion
footer before process exit. End with normal capture/open/unload and save/stop;
then verify client/server/display units inactive and preserve world/log/evidence.
Reuse FD902/FD903 cold-reopen acceptance; no additional restart is required
without a new persistence failure. Missing required views, continuing memory
growth, rider loss, orphan accumulation, save failure or sustained backlog stay
explicitly open until resolved.

## Combined measurement identity amendment

Runtime12 adds a bounded160-substep, read-only cold-restoration trace to the
helper; it finishes before warmup. Production classes remain unchanged from
`a29c86c4`. The trace's Roller hold lookup used the parent instead of the child
map key, so its `hold=none` does not establish absence; velocity samples remain
native reads. The combined collection uses the actual runtime12 execution
manifest and recorder names, rather than relabeling its output as runtime11.

### Failed-budget correction identity

Runtime13 adds the live-palette exclusion and endpoint-key reuse after the
runtime12 first300-second tick budget failure. Its two targeted native tests
passed; `runtime13-diff.json` records every JAR entry difference. The fixed
park and timing criteria are unchanged. The bounded Roller trace now uses its
child-body key and is disabled during this run. Fresh recorder names are
`client-combined13b.jsonl` and `server-combined13b.jsonl`; the successful warmup
attempt, if completed, is `warmup-combined13c.jsonl`. Earlier setup attempts
remain excluded. A failed first300-second p95 tick check terminates the
sustained attempt and stops the resource sampler through its explicit marker.

### Flying equipment correction and resumed sustained check

Runtime14 differs from13 only in `FlyingExamples.class`: the final roll endpoint
uses a west drive port with the corresponding negative-axis motor sign. Track,
Chain span,128RPM anchor speed and all other scenery remain unchanged. The saved
park received that exact block-layout correction before timing. This specific
layout repair invalidates the failed sustained interval, not the accepted base
or valid combined first300. A new continuous1800-second window follows a fresh
300-second fully occupied warmup; its first300 is also reported without deleting
the earlier accepted comparison.

`warmup-combined14.jsonl` and `combined14-windows.jsonl` use fresh JVM recorders
`client-combined14.jsonl`/`server-combined14.jsonl`. Both wrappers now stop when a
non-ready, non-stopping group remains within0.05blocks for60seconds; ready or
stopping states reset that timer. Replaying the prior failed raw window detects
Flying at60.32seconds stationary. This guards workload validity, not a new
vehicle timing requirement. [Execution](execution-combined14.json) and
[JAR diff](runtime14-diff.json) preserve the actual inputs.
