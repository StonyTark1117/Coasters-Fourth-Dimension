# FD502 reusable-tooling review

2026-11-14 America/Phoenix; same owner/workspace as [current handoff](README.md).

**FD502 complete:** [native delivery acceptance](native-smoke/README.md) closes
the remaining frame/tick path with the earlier accepted resource/JFR/input checks.
No new runtime check is required; FD904B park qualification remains separate.

| Named need | Existing evidence / disposition |
| --- | --- |
| Reusable circuit/scenario without rebuilding a park | Retained Arrow FD502 world and accepted FD403 family worlds already exist. Consume the linked run manifest, copy the stopped world to the next owner's persistent directory, and preserve identities. No new park or old-family replay needed. |
| Isolated input, binding/focus/geometry and connection | `fd-private-client.py` verifies exact owner, unit cgroup/working directory, Xvfb PID/display and client window ownership; `status` reads geometry, normal bindings and live server player list. Each in-world action checks connection first, records expected result and before/after images. `turn` uses discovered XTest device motion. Existing accepted repair is sufficient. |
| Native target/state rather than guessed clicks | Current `ChainLiftDiagnostics` request/readback reports client hit/type/block/state, held item, native Wrench graph/span hit, selected/cached Chain anchors, focus/cursor/raw-input state, yaw/pitch/passenger state and assigned vehicle restraint/capture state. It does not expose a numeric generic interaction-distance field. Use the actual native target plus player position/reach/geometry checks; missing number alone is not a new acceptance blocker. No duplicate target framework is needed. |
| Ownership, CPU/memory headroom and cleanup | Existing helper proves ownership and refuses another owner's run. New dedicated `fd-resource-sample.py` adds the concrete missing periodic RSS/cgroup/CPU readback, including parent constraints and raw timestamps. World/server save/stop procedure remains the accepted existing one. |
| Carrier/render subsystem counts | `/fd performance`, `OriginalTrackRenderer` and `CarrierRenderMetrics` retain their established scope. They do not measure whole frames/server ticks. |
| FD904B whole CPU-frame intervals and per-server-tick work | **Missing at inspection; implemented.** Bounded opt-in native event collector registered and [native delivery verified](native-smoke/README.md); the granted one-file implementation is `RuntimePerformanceRecorder.java`. Native GPU presentation is a distinct unavailable readback on a software Xvfb context, not a reason to claim those CPU intervals are GPU smoothness. |
| FD904B one-second heap / GC durations / post-GC heap | **Missing at inspection; implemented.** Java MemoryMXBean plus JFR pause/After-GC events now provide the native sources. Do not label RSS as heap or allocated heap as live post-GC heap. The completed Wing recordings contain actual heap, GC-pause and post-GC events from both JVMs, verified against binary JFR. |
| Percentiles / stalls / sustained plateau | Actual events are wired and summarized in the native delivery record. Required statistics are median, p95, p99, maximum/count; frames >50/>100 ms; ticks >50 ms; GC durations and post-GC medians, plus final three five-minute memory periods. No 30-minute workload is scheduled in FD502; final workload/budget acceptance belongs to FD904B after FD1000. |

## Resource sampler reuse

```sh
python3 scripts/fd-resource-sample.py /absolute/owned/run.json \
  --owner THREAD_ID --parts client server --seconds 300 \
  --output /absolute/persistent/evidence/resources.jsonl
```

The script imports the accepted private-run ownership verifier. It does not send
input, attach to Java, or create/stop services. It refuses owner mismatch,
inactive units, PID/cgroup change, PID reuse, existing output and `/tmp` output.
Each sample records wall and monotonic timestamps, PID/start time, process CPU
ticks (with clock ticks/sec in the manifest), RSS/high-water RSS in KiB, thread
count, and each cgroup ancestor's memory/current/peak/max/events and CPU quota,
statistics/pressure. Host `meminfo` and load averages are retained. Missing kernel
files are `null`, never fabricated zeros. `memory.peak`/VmHWM are lifetime peaks,
not window-only peaks. The final `complete` row distinguishes a completed window
from a partial/failed collector. Heap and GC must be joined from the JVM source.

## Smallest affected verification

[Raw smoke samples](resource-smoke.jsonl) contain three one-second samples of an
owned `/usr/bin/sleep` service, **not Minecraft performance evidence**. Ownership
used this thread, persistent root
`/home/braydon/.local/state/fourthdimension-fd502-resource-smoke-20261114`, unit
`fd502-resource-smoke-20261114-server.service`, PID 571287, 64 MiB memory limit.
No Xvfb was created; the unused manifest display value is never accessed by the
resource-only path. Maximum observed process RSS was 4,292 KiB.

First attempt diagnosed an empty-valued `/proc/status` field as a parser error;
the parser now retains that value. The corrected check passed once: three raw
samples >0.9 seconds apart, positive RSS, exact 67,108,864-byte cgroup limit,
ancestor/host measurements, and completed footer. The service then stopped;
`ActiveState=inactive`, `MainPID=0`, `Result=success`. This repair changes no
production behavior and invalidates no accepted product evidence.

## Summary and native collector check

`fd-performance-summary.py` reads runtime/resource JSONL and supports explicit
`--start-unix-ns` / `--end-unix-ns` half-open windows. It retains missing sources
and collector errors, reports nearest-rank p95/p99 and stall counts, and gives
per-process heap/RSS/GC statistics plus final three five-minute period medians.
It never declares a gate pass. GC pauses and post-GC heap are separate events;
input order is not assumed chronological because JFR rows are appended at close.

A deterministic check covered known outliers, p95/p99, 50/100 ms counts, window
exclusion, post-GC values and missing-source reporting. The [resource-only summary](resource-summary.json)
correctly leaves frame/tick/heap/GC missing. These synthetic checks are parser
evidence only. The [isolated Java 21 compile](collector-compile.txt) passed for
the recorder against the existing dev runtime classpath without writing the
lead's build tree. Actual event delivery now passes in the [useful Wing-run record](native-smoke/README.md);
no separate park or old-family replay was scheduled.

The actual compiled recorder sink also passed an isolated Java 21 heap/JFR
smoke (128 MiB heap, no game bootstrap). Explicit GC was a disclosed test stimulus,
not a performance measurement: three heap rows, five pause rows and six post-GC
rows reached the final output with no errors/loss; the process exited.
[`jvm-smoke.jsonl`](jvm-smoke.jsonl), its sibling JFR and
[`jvm-summary.json`](jvm-summary.json) preserve interpretable raw values. The
summary correctly reports frame/tick/resource as missing for that isolated JVM.
Native Minecraft frame/tick delivery now passes in the linked Wing-run acceptance.
