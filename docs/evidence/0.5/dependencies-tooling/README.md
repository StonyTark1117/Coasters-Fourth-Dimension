# FD501 / FD502 current handoff

Updated 2026-11-14 America/Phoenix. Owner: `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`.
Workspace: `/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`,
starting HEAD `a4ef96d7`; current lead integration HEAD `f7825405`. Earlier dirty files were inspected and preserved; this
owner edits this directory, `docs/INTEGRATION-BACKLOG.md`, and dedicated tooling
only. No staging, commits or branches. Lead granted exclusive creation of
`src/main/java/dev/fourthdimension/runtime/RuntimePerformanceRecorder.java`;
all other production files remain lead-owned.

- **Verified state:** core baseline decision below is ready for FD501A/Wing.
  FD501 optional decisions/catalog are complete and ready for lead closure;
  FD502 collector implementation is ready; native event-delivery smoke is
  assigned to the first useful Wing run.
- **Exact next action (lead):** call `RuntimePerformanceRecorder.register()`
  once from `FourthDimension` and enable the properties below for its useful Wing
  run; return the stopped output files for this owner's event-delivery review.
- **Last failed condition / changed precondition:** resource smoke first rejected
  a valid empty-valued `/proc/status` entry. Parser now preserves empty values;
  the single corrected three-sample check passed. No failed Minecraft attempt.
- **Evidence:** this record; [dependency decisions](dependency-decisions.md),
  [dated catalog](catalog-20261114.json), forthcoming tooling disposition. Existing accepted input/world handoff remains
  `docs/evidence/0.4/fd502-execution-repair-20261113/README.md`.
- **Runtime:** no Minecraft, display, port or world allocated. The small Java-only
  recorder check (PID 577999, 128 MiB heap) exited successfully. Temporary
  owned `fd502-resource-smoke-20261114-server.service` ran `/usr/bin/sleep` only
  (PID 571287, 64 MiB scope); stopped with `ActiveState=inactive`, `MainPID=0`,
  `Result=success`. No live owned services remain. No park-performance or
  compatibility claim is inferred from inspection.

## Core decision — ready for lead

Keep Minecraft 1.21.1, Java 21, NeoForge 21.1.248, Create 6.0.10-280 slim,
Coasters Simulated 0.1.5 (CurseForge 8655742), Sable 2.0.3, Ponder
1.0.82+mc1.21.1, Flywheel 1.0.6, Registrate MC1.21-1.3.0+67, and the current
client Veil 4.1.4 constraint. Preserve existing optional Engineered 1.1.1
(8863559) and Track Styles 1.2.0 (9020601) profiles. No core dependency upgrade
or archived provider/schema migration is required to begin fixed Wing.

This is a prerequisite decision, not a claim that Wing already exists.
`VehicleFactory` already consumes native `CoasterPathTrackFrame` basis/anchor
queries, `CoasterCartSpawner.spawnMinimalContraption`, Sable sublevel allocation,
and native plot blocks. `BankedRecoveryExamples` uses
`CoasterTrackMaterials.COASTER`; normal native track is available in this pinned
stack. `CarrierSeatBlock`, `Boarding`, `NativeLoading` and the existing renderer
provide the native seat/mass/pose substrate. The constraints currently rejecting
unregistered ordinary track in `VehicleFactory`/`EligibilityHooks`, the five-entry
`Family`/`TrackDesign` set, and rotary-specific construction are our own code.
FD1101A must extend these for measured fixed-seat Wing behavior; an upstream
upgrade cannot supply that feature or replace its required occupied proof.

Persistence is **schema 6** (`FoundationData.SCHEMA`). Families serialize stable
string IDs, vehicle/carrier UUIDs and seat assignments; add Wing without renaming
the accepted IDs. Existing schema-1–5 upgrades and identity checks remain.
**The live editor registrar is already protocol 8** (`EditorNetwork.register`),
from current editor work; historical prose saying protocol 7 is stale for this
checkout. This does not imply adoption of archived schema-7/protocol-8 provider
code. No additional protocol bump is requested by this dependency decision;
the lead determines any bounded packet change needed by Wing's actual contract.

No external functional provider is selected for Wing. Existing unknown functional
providers are rejected by `FoundationData.validateIdentity`; they must not be
silently rewritten as original track. Existing missing-appearance fallback
remains independent of mechanics. A future CTNB/Skin adapter that needs new saved
provider identity owns that migration and missing-provider retention behavior in
FD501C/E, with backup/restoration checks for the changed path. Do not import the
archived registry merely because it exists.

**FD501A disposition offered to lead:** no-change closure for the dependency and
migration prerequisite; no runtime replay warranted. Wing mechanics remains
FD1101A's work. Optional adapter discovery does not delay this decision.

## FD502 named gap — shared-runtime handoff proposal

Inspection found no whole-frame interval or every-server-tick collector.
`CarrierRenderMetrics` counts submitted bodies/layers; `OriginalTrackRenderer`
times only the addon stage; `/fd performance` times only carrier runtime work.
None supplies FD904B's frame/tick p95/p99/max raw samples. The existing private
helper checks ownership, live connection, geometry and controls, but does not
retain one-second RSS/cgroup measurements. This is the explicit unmet collector
need, not an invitation to rerun the park.

Initial proposal (implemented below under the granted file handoff): one opt-in diagnostic class
using NeoForge `RenderFrameEvent.Pre` to record consecutive CPU frame-start
intervals and `ServerTickEvent.Post` to read the existing native
`server.getTickTimesNanos()[server.getTickCount() % 100]` execution duration,
with raw monotonic timestamp, wall-clock anchor, and client level/server identity.
No render/body/pose code changes; no actual GPU-presentation claim. Use a bounded
recording window or buffered output, and expose Java MemoryMXBean plus GC
notifications for one-second heap and actual GC durations/post-GC pool usage.
This owner will supply dedicated external tooling for RSS/cgroup samples and
summary validation once that exact file handoff is accepted. Alternatively the
lead may implement the small diagnostic class directly. Only the subsequently granted recorder file has been edited. A short collector smoke on the lead's first owned Wing run can
validate event delivery; no additional park or old-family acceptance run is needed.

### Concrete collector wire format / available tooling

`fd-resource-sample.py` is implemented and passed its bounded owned-service check.
`fd-performance-summary.py` accepts JVM JSONL events with `kind`, `unix_ns`,
`process` (`client`/`server`): frame/tick/gc use `duration_ns`; heap uses
`used_bytes`; gc optionally includes `after_bytes` (heap-only sum after GC).
A manifest should identify wall/monotonic anchor, PID, Java version and measurement
scope. End with `kind=complete`. Use `frame` for consecutive RenderFrame Pre
intervals, not Pre→Post render-stage duration. The sampler preserves warmup and
raw samples; the summary supports an explicit half-open sample window and reports
missing sources rather than claiming a gate pass. CPU interval includes cap/wait
work between frames and is explicitly not GPU presentation.

Native source inspection confirms `tickServer`'s public ring captures work from
method entry through world/status/autosave work and is populated **before**
`fireServerTickPost`. Read that native value each Post instead of introducing
a second stopwatch. Both public getters exist in the pinned Minecraft source.
A one-class opt-in event subscriber plus its JVM diagnostics is enough; no mixin
or build pin change is needed for these measurements.

## Collector implementation ready — registration contract

The granted single file `RuntimePerformanceRecorder.java` now provides
`public static synchronized void register()`. **Lead action:** add
`RuntimePerformanceRecorder.register();` once in `FourthDimension`'s constructor.
No mixin/config/build edit is required. No listeners, threads or files are created
when `fourthdimension.performanceFile` is absent. Client event references live
in a nested class loaded only for `Dist.CLIENT`; common registration is safe
for dedicated servers. The isolated Java 21 single-file compile passed, with
output retained in `collector-compile.txt`; it writes no shared build outputs.

For the first useful Wing client and server, add these **JVM** arguments to their
owned direct Java launch commands (unique output paths per process):

```text
-Dfourthdimension.performanceFile=/absolute/owned/evidence/client-performance.jsonl
-Dfourthdimension.performanceSeconds=3600
```

Use `server-performance.jsonl` for the server. The 3600-second upper bound permits
startup plus a later useful window; no hour-long check is requested. After a
short useful connected window, create `client-performance.jsonl.stop` and
`server-performance.jsonl.stop` beside those exact files to finish collectors
without stopping gameplay. Normal JVM shutdown also finishes them. Preserve the
JSONL, sibling `.jfr`, resource samples and logs. Do not overwrite an earlier
recording. Durations and warmup selection are explicit analysis windows, not
performance acceptance.

The writer buffers through a bounded 8192-event queue. Frame/tick callbacks never
write to disk. Raw CPU intervals are in-world only and do not bridge a level
change; pause/screen/dimension/game-tick labels remain. Tick data comes from the
native tickServer ring once per Post. Heap used/committed/max is sampled once
per second. **Actual GC pauses**, not total concurrent-GC elapsed time, come
from JFR `jdk.GCPhasePause`; `jdk.GCHeapSummary` After GC supplies live heap.
JFR is stopped/dumped/read at collector completion so its final chunk is not
lost to streaming delay. Separate `gc` and `post_gc` rows preserve their event
time and GC ID. A `complete` footer reports dropped queue events; JFR failures
produce `collector_error`, and I/O failure leaves an incomplete file. Neither
counts as acceptance. No explicit GC is requested by the recorder.

The remaining native smoke needs nonzero client `frame`, server `tick`, JVM
`heap` records, and interpretable `gc`/`post_gc` if natural collections occurred,
with completed footer, no collector errors or dropped events. A short window
without GC is not a failure; raw JFR metadata plus the first natural GC during
the useful run can verify that path. This smoke is an FD502 collector check, not
FD904B's final park measurement or another Wing feature gate.

### JVM collector path verified separately

The actual recorder's private sink was exercised in a small isolated Java 21
process (128 MiB heap), using its compiled class and the existing dependency
classpath. This reflective smoke started no game/event bus/server/client/display.
A disclosed explicit `System.gc()` plus allocation supplied a known GC stimulus
outside any performance-acceptance window. Result: three heap samples, five real
JFR pause records, six post-GC records, complete footer, zero dropped events and
no collector errors; the Java process exited. Raw `jvm-smoke.jsonl`, sibling
`.jfr`, log and `jvm-summary.json` are retained. This verifies the actual JFR
field names/units and final-chunk extraction; it does not claim native Minecraft
frame/tick delivery, which remains the one useful Wing-run check.
