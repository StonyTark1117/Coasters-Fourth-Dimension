# FD502 native collector delivery — accepted 2026-11-14

**Disposition: the remaining FD502 collector claim passes.** The completed
Wing recordings supply actual client frame and dedicated-server tick events;
the earlier isolated resource and JVM/JFR checks remain accepted. No further
collector launch, old-family replay or park reconstruction is required.
FD904B's final workload, GPU context and performance budgets remain separate.

Reviewed by `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da` on existing `main`, HEAD
`2128ae50`, without changing source or controlling a runtime. The originating run
belongs to Wing lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d` at
`/home/braydon/.local/state/fourthdimension-runs/fd1101-wing-20261114`.
Its private display is `:310`, ports `25630/25631`, unit prefix
`fd1101-wing-20261114`; client PID at capture `583632`, server `583335`.
Manifest Java is `21.0.13.0.3`; recorded heap maxima are 1536 MiB / 2048 MiB,
with lead-owned 6 GiB / 4 GiB scopes. The coordinator explicitly returned the
finished recordings for review while the lead continued Wing gameplay. No
ownership transfer or shutdown of those game processes is implied by this check.

## Evidence and interpretation

| Raw event | Client | Dedicated server | Verified interpretation |
| --- | ---: | ---: | --- |
| Frame | 8,705 | — | Consecutive in-world CPU `RenderFrameEvent.Pre` intervals in nanoseconds; includes intervening work/pacing, not GPU presentation timestamps. |
| Tick | — | 2,240 | Native `tickServer` execution duration in nanoseconds; exact consecutive ticks 1–2240, including 1,575 ticks with a connected player. |
| Heap | 102 | 115 | `MemoryMXBean` used/committed/max bytes, sampled nominally once per second; used ≤ committed ≤ max throughout. |
| GC pause | 61 | 32 | Actual JFR `jdk.GCPhasePause` durations, not elapsed concurrent-GC cycle time. |
| After-GC heap | 50 | 26 | JFR `jdk.GCHeapSummary` `After GC` heap bytes, not RSS. Pause and heap event counts need not match. |

Both footers have `reason=stop_file`, `dropped_events=0`, `lossless=true`; their
row counts match the files and neither contains `collector_error`. The recording
windows span 103.031 seconds client and 118.384 seconds server. They include
startup/loading and normal interaction; these are collector-delivery records,
not warmed benchmark windows. The first client frame still labels
`ReceivingLevelScreen`; screen/pause/game-tick metadata is preserved rather than
discarding this outlier or presenting it as steady gameplay performance.

All timestamps are integer Unix nanoseconds within the manifest/footer bounds.
Ordinary samples have strictly increasing monotonic timestamps and one exact
wall-minus-monotonic offset per process. The largest difference between adjacent
frame row timestamps and the recorded frame interval is 13,909 ns, consistent
with capturing the row immediately after the callback boundary. Heap cadence
is scheduler-dependent: client median 1.0016 seconds (0.9112–1.1281), server
median 1.0468 seconds (0.9949–1.1021). Raw timestamps carry that jitter; there is
no invented exact one-second clock or cross-process synchronization guarantee.

The Java 21 `jfr print --json` reader independently decoded the retained binary
recordings. Every one of the 111 client and 58 server pause/After-GC records
matches the JSONL multiset exactly by kind, Unix-nanosecond timestamp, GC ID,
nanosecond pause duration or heap byte count. JFR rows are appended at close and
need not appear in global timestamp order in JSONL. The summarizer selects by
event timestamp, not file order.

[Native source review](native-source-review.json) confirms that the pinned
Minecraft `tickServer` ring is populated before `ServerTickEvent.Post` and
includes world/status/autosave work. Idle pacing and later Post listeners are
outside that native metric. Frame intervals use consecutive Pre events, not the
addon's render-stage timing. This is sufficient, explicitly scoped collector
readiness; it is not a claim of total GPU frame time.

## Retained files and repeatable analysis

- [Validation and hashes](validation.json): original absolute paths, sizes,
  SHA-256, counts, timing checks, source/summarizer hashes and capture identities.
- [JFR cross-check](jfr-crosscheck.json): exact binary/JSONL event agreement.
- [Summary](summary.json): existing summarizer output, including percentiles,
  stalls, heap and GC values; no threshold or gate-pass inference.
- `client-performance.jsonl` / `server-performance.jsonl` and sibling `.jfr`
  files: byte-identical copies of the coordinator-returned completed recordings.
  The originals remain unchanged in the lead's persistent evidence directory.

From the project root, the existing analysis invocation is:

```sh
python3 scripts/fd-performance-summary.py \
  docs/evidence/0.5/dependencies-tooling/native-smoke/client-performance.jsonl \
  docs/evidence/0.5/dependencies-tooling/native-smoke/server-performance.jsonl \
  --output /absolute/persistent/evidence/native-delivery-summary.json
```

For future measured workload windows, add `--start-unix-ns START` and
`--end-unix-ns END` for the declared half-open interval. Preserve raw data and
choose the workload/warmup window before comparing results. The native summary
correctly reports `resource` missing: this run did not collect simultaneous
RSS/cgroup samples. The already-accepted [resource sampler check](../tooling-review.md#smallest-affected-verification)
verifies that separate tool; do not merge its unrelated process measurements
into this Wing run as if concurrent.

## Reuse and closure

Keep the registered `RuntimePerformanceRecorder.register()` call. Without the
`fourthdimension.performanceFile` JVM property it creates no recorder/listener
work. For an assigned future measurement, set unique absolute persistent client
and server output paths using `-Dfourthdimension.performanceFile=...` and a bounded
`-Dfourthdimension.performanceSeconds=...` (1–3600 seconds). The limit starts at
registration, so include startup/warmup allowance; it is not an instruction to
measure for the full limit. Finish early by creating the sibling `.jsonl.stop`
file, or allow normal JVM shutdown. Keep JSONL and sibling JFR together and
inspect the completion footer, errors and drops before interpreting a summary.
Use a fresh output name because overwriting an existing recording is refused.

For concurrent process resources, use the [owned-run sampler invocation](../tooling-review.md#resource-sampler-reuse)
with the same run and declared window. Reuse retained worlds and the accepted
private-input/targeting/connection procedure. Keep actual GPU-presentation
measurement unavailable unless a chosen display/collector supplies it; software
Xvfb cannot certify the native-GPU target. FD904B later owns the fixed park,
settings, warmed/sustained windows and frozen budgets.

FD502 closes cumulatively through the accepted 0.4 input/scenario repair,
reviewed native target/connection diagnostics, passed resource sampler,
Java/JFR checks and this native delivery proof. This owner has no live runtime
to clean up; Wing's exact client/server/display remain with the named continuing
lead task. Lead owns canonical roadmap/queue status and Git integration.

## Lead integration addendum — launcher PID correction

The lead did collect `resources.jsonl` during the Wing run, but generated launch
scripts retained a shell as systemd MainPID. Those rows measured shell RSS/CPU,
so they are not accepted game-process measurements. The JVM collector records
and JFR validation above are unaffected. `fd-resource-sample.py` v2 now selects
the unique Java process from the verified unit's cgroup and checks its identity
throughout. The targeted two-sample `resources-jvm-save.jsonl` measured actual
server PID583335 at1,522,324 KiB RSS during clean save; unit shell PID583334 was
excluded. This closes the named sampler defect without another gameplay run.
This is a shutdown window, not a steady performance benchmark.
