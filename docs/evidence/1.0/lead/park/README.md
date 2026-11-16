# FD904B — representative park measurement

**In progress, 2026-11-16 UTC.** The base five-minute sample and its cleanup
passed. The combined1800-second sustained workload/memory check is complete;
the bounded frame comparison and seven remaining actual mounted performance
views remain open. The [fixed protocol](protocol.md) defines the
finite workload; [preparation](preparation.md) records failed setup separately.

Mounted15 completed nine supplemental actual occupied views (Legacy, Free Spin,
Revised, PAX, Wing, S1 Spinning, Suspended, Fixed Inverted, Axis), each with
recorded movement and normal Open/Shift. PAX's initially premature Open was
correctly rejected; waiting for controller BOARDING completed its safe transfer
without repeating the ride. Raw15/15b windows and resource streams are preserved;
JVM collectors have stopped losslessly after the corrected comparison. Seven styles
remain: Roller Ball, Conventional Flying, New-generation Flying, Pipeline, Dive,
S2 Spinning and Powered Inverted Yaw. The valid corrected comparison passed tick
p95 (47.915487 ms) and frame p99 (21.164141 ms), but frame p95 (16.563280 ms)
still exceeds the fixed 15.9336771 ms ceiling. Longer warmup did not resolve it.
Normalized classpaths/settings match; recorded throttling and memory events do
not explain the broad per-view increase. No further acceptance timing retry is
scheduled. The remaining mounted-view run will include one separate 60-second
client render-thread/GPU diagnostic to identify the dominant cost before a fix.
The nine completed views and accepted sustained window remain closed. All15 riders
subsequently unloaded normally; save/checkpoint and all three inactive/PID0 units
are verified. Successor16 uses the same immutable runtime14, private323 and
ports25664/25665, with fresh collectors for the seven remaining mounted views
and separate diagnostic. Lead retains ownership through cleanup.

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
and stops the window. The subsequent sustained result is recorded below; mounted
views and the frame comparison remain open.

## Completed combined sustained window; comparison still open

Immutable runtime14 differs from13 only in the Flying example equipment layout.
[Execution identity](execution-combined14.json) and the [final summary](combined14-sustained-final.json)
retain the1800-second bounds, complete recorders and all raw inputs. All17groups
cycled with90riders and no running stall/fault. It records36000ticks (20TPS),
tick median/p95/p99/max27.172/49.070/72.919/137.429ms and1691ticks>50ms.
The146595frames have median/p95/p99/max12.373/18.368/23.113/51.201ms,
one frame>50ms and none>100ms. The sustained Arrow-heavy view mix is reported
separately from the fixed17-view first300 comparison.

Final three five-minute median RSS values are3427.66/3428.72/3428.80MiB(client)
and2867.16/2859.37/2860.16MiB(server). Post-GC medians are1249.11/1249.74/1249.29MiB
and1161.14/1161.27/1161.31MiB, respectively. Both plateau; no cgroup OOM/max event
occurred. GC p95/p99/max were2.497/3.120/20.530ms(client) and2.875/4.112/22.824ms(server).
Both JVM recorders ended losslessly with zero drops; the resource sampler completed
1800samples. Allocated native plots remained53 before and after. All riders
unloaded normally, the world saved and all three units were verified inactive/PID0.
The saved checkpoint is `park-combined01/checkpoints/world-drained14`.

The original14 first300 frame p95/p99=16.677071/21.489420ms exceed the retained
13r2×110% ceilings15.9336771/21.2381246ms. This comparison remains open. The
subsequent [`combined14-warm`](combined14-warm-invalid-summary.json) is **invalid**: after normal Arrow Open/Shift the
wrapper left Dev in Creative; fixed cameras fell to bedrock. Its numeric frame
pass cannot establish comparability or prove warmup maturity. The [failed view](park-1794802842491368164.png)
contrasts with the correctly elevated [original14](park-1794801031235084065.png)
and [13r2](park-1794798862251407678.png) spectator views. This later setup failure
does not invalidate the earlier sustained interval.

The [bounded replacement plan](frame-comparison-plan14.json) corrects the exact
mode transition: wrappers explicitly set Spectator and verify the server's
`execute if entity` selector before fixed views and dismounted transfers.
Fresh `combined15` collectors reuse the same immutable runtime14/settings for
finite supplemental mounted segments and one corrected300-second comparison.
There is no second sustained run and no threshold increase.

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
