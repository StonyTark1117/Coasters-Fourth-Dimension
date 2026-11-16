# FD904B — representative park measurement

**In progress, 2026-11-16 UTC.** The base five-minute sample and its cleanup
passed. Combined sustained measurement and the other sixteen actual mounted
performance views remain open. The [fixed protocol](protocol.md) defines the
finite workload; [preparation](preparation.md) records failed setup separately.

## Combined failure and bounded correction

Runtime12's first300-second combined window retained90 riders without faults
and recorded6000 ticks, but p95 tick work was63.124ms against the initial50ms
criterion. Frame p95 was16.711ms. The nominal1800-second measurement was stopped
at approximately366seconds; it is not a sustained pass. The resource stream has
complete first300-second content but no completion footer. The frame/tick streams
both stopped losslessly with zero dropped events. [Exact statistics](combined12-first300-summary.json)
retain the nearest-rank calculation and bounds. Original recordings are retained
as `combined12-windows.jsonl.gz`, `combined12-resources.jsonl.gz`,
`client-diagnostic12.jsonl.gz` and `server-diagnostic12.jsonl.gz`.

A separate60-second JFR profile attributed752 of3625 server-thread samples to
full-section native bearing/rivet scans,366 to canonical curve-handle lookup,
and202 leaf samples to long-to-string conversion. These inclusive categories
overlap and are not additive. [CPU ranking](combined12-cpu-ranking.json) is
diagnostic evidence, not a replacement timing window.

The bounded correction skips a managed plot's section only when its live palette
cannot contain the requested block. Every potential match retains the native
cell scan and representative selection; ordinary plots retain native behavior.
The second change reuses a curve's pure endpoint string while its endpoint values
remain unchanged. Geometry, editable handle lengths and safety decisions are not
cached. [Two targeted native tests passed](performance-native13.log), including
24 same-tick bearing/rivet comparisons across chassis, carrier and ordinary plots,
endpoint mutation/reversal and a subsequent native handle edit.

After the failed measurement, all17 groups returned ready and fault-free. Dev
used normal Open/dismount, the fixture riders unloaded, and the world saved.
[Cleanup](cleanup-combined12.json) verifies client/server/Xvfb inactive with PID0.
The saved checkpoint is `park-combined01/checkpoints/world-drained12` under the
lead's persistent run directory. Runtime13 repeats the affected combined check;
the accepted base sample below retains its original identity.

Runtime13's first300-second sample numerically met the unchanged tick budget:
6000ticks, median26.687ms,p95=48.786ms,p99=72.055ms,max122.727ms,278ticks>50ms.
Its37865frames had median8.382ms,p95=14.536ms,p99=19.274ms,max48.063ms, with
no frames>50ms. All90 riders remained and no faults occurred. **This sample is
not accepted:** cycle-counter inspection revealed Conventional Flying remained
prone-locked at its station with zero dispatches. The earlier S2-triggered
warmup drain had cleared its helper preparation state. Thus the required17-group
cycling workload was incomplete despite the numeric result. The sustained
attempt was stopped, its resource sampler stopped through its marker, and all
raw evidence retained. Normal Flying dispatch corrected that state. A fresh
warmup must observe movement in every group before replacement timing. No
production change or accepted lifecycle case is implicated by this helper state.

## Flying obstruction during the sustained window

The corrected `combined13r2` first300-second interval passed:6000ticks,
p95 tick48.974220ms and frame14.485161ms, all17 groups moving and90 riders.
The sustained attempt stopped at843seconds when Conventional Flying cycle7
remained atZ333.812499 against the east drive shaft at the final roll endpoint.
This is a failed sustained workload, despite the retained valid first300.
Both JVM recorders ended losslessly with zero dropped rows; the resource sampler
ended through its marker with843 samples. The stalled world is preserved at
`park-combined01/checkpoints/world-flying-stall13`.

[Native contact geometry](flying-stall13-contact-geometry.json) places the bearing
within roughly4 micrometres of the first shaft's face. This is geometric evidence,
not a native contact-manifold dump. A controlled same-JVM equipment edit connected
the equivalent west motor and shafts, verified128RPM, then removed only the east
contact shaft. Within92ticks, Flying advanced onto the descent at about8m/s with
the same4 riders, unchanged native Chain metadata and no fault. Vehicle pose and
velocity were not edited. This is an equipment-removal diagnostic, not a normal
Resume test. The remaining old drive blocks were then removed; normal capture and seated
lock returned all4 riders to a ready, fault-free dock. After normal unloading,
the server saved and all runtime13 units stopped. Runtime14 now uses fresh
recorders on the saved corrected world. The source example
now mirrors this one port and compensates the negative-axis motor direction.

The documented running-gear clearance requirement is addressed by this example
layout correction. The approximate nominal-envelope shaft exemption is unchanged;
this record does not claim a generic swept-collision rejection repair. Subsequent
measurement checks running groups for60seconds without0.05block movement, while
ready/stopping states reset the timer; a detected stall records native diagnostics
and stops the window. Required sustained timing and mounted views remain open.

## Accepted base sample

Production source is `a29c86c41d8911c7a4c043b05c1131da9621fe95`; the opt-in
benchmark helper overlay has JAR SHA-256
`a198b49ee1307246b758d5eda508926089941bc889d821f662c7ac2e8b9a5a64`.
[Execution identity](execution-base11.json), [hardware/settings](hardware-and-settings.json)
and [exact bounds](base-bounds.json) preserve the actual input and window.
The native RX 7900 XTX client used1280×720,Fancy,render12,simulation8,
VSync off,1536M heap and6GiB process limit; the server used2048M/4GiB.

| Metric | Result |
| --- | --- |
| Server ticks / observed rate |6000 /19.99535TPS |
| Tick work median / p95 / p99 / max |30.515 /44.704 /56.635 /131.571ms |
| Ticks over50ms |129 |
| Client frames |41789 |
| Frame interval median / p95 / p99 / max |7.663 /12.431 /15.271 /38.551ms |
| Frames over50ms /100ms |0 /0 |
| Client / server GC max |17.359 /5.705ms |
| Client / server peak RSS |3427119104 /3030417408bytes |
| Allocated native plots before / after |53 /53 |

The initial p95 tick criterion was50ms. Outliers remain included; the observed
frame interval includes CPU pacing and VirtualGL transport, and is not a
universal physical-display FPS promise. [Full statistics](base-summary.json)
and [frozen110% later regression ceilings](base-baseline.json) preserve precision.
All90 seats remained occupied throughout the sample. After the seventeen
park views, Dev replaced one fixture pig and rode Arrow for the remaining
window. This benchmark helper mount is not normal player-input acceptance.

Both frame/tick recorders ended through their stop files with lossless footers
and zero dropped events. [Raw manifest](base-raw-manifest.json) identifies the
compressed original recordings and resource samples. The saved world remains
at `/mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/park-base02/server/world`.
After normal capture, Open and unloading, the server saved and exited cleanly;
client and owned display322 also exited. [Cleanup verification](cleanup-base11.json)
records all three units inactive with MainPID0. No runtime ownership is retained.
