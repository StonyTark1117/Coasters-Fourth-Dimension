# FD904 dedicated-server scene baseline

This is a bounded server benchmark, not completion of FD904. It measures a
21-car scene with two seven-car Arrow consists, three independent eight-seat
Free Spin cars, and the existing four-car PAX shuttle. All 96 seats contain real
native pig passengers. The 48 carrier bodies use their normal rotary constraints;
the Arrow and PAX cars use 15 native inter-car links. The operating shuttle also creates its normal winch carriage and mechanical
catch; the never-dispatched parked seed has no carriage yet. No clients are connected.

## Method

`PerformanceFixture` is available only with `fourthdimension.validation=true` and
permission level 2. It creates original functional tracks through normal track
placement, vehicles through `VehicleFactory`, links through the native coupling
API, and passengers through Create seats. Boarding, restraints, and dispatch use
normal operational validation. PAX starts through the winch state machine.

The five straight 120-block lanes each have two legal 60-block spans. The shuttle
uses its standard three-span demonstration route. All 158 scene chunks are forced
before the track-only reference. The occupied seed world is saved once; every
loaded process starts from an identical copy. Warm-up is 600 ordinary stepped
ticks before each loaded measurement, followed by unfrozen 20-TPS operation.
Each recorded window contains 600 ticks. There are three track-only reference
windows, three parked windows per pack, and three operating repetitions per pack.
Operating repetitions alternate base and combined profiles and restart from the
same seed. Parked windows are consecutive in one process per pack.

The operating fixture releases the Arrow and Free Spin cars through dispatch,
adds one initial 0.5 block/second native velocity to their chassis/carriers, and
starts the PAX winch. It does not impose velocity every tick. The Arrow profiles
remain at zero angle; this is not a controlled-inversion or dense-profile stress
test. Free Spin carriers are released normally with symmetric full loading.
No external motor, brake, custom Track Styles appearance, or camera effect is
activated. The combined pack comparison measures installed-adapter overhead for
this scene, not every equipment configuration.

The timer covers the NeoForge `ServerTickEvent.Pre` to this listener's `Post`
callback, excluding the tick pacing sleep and the snapshot/log formatting after
the last measured tick. It is an event-bracket elapsed time, not a claim to cover
every task in Minecraft's outer server loop. All raw tick times are retained.
`CarrierRuntime` counters additionally measure its existing process-wide callback
time, including empty dimensions and shuttle work. Dividing this total by 48 gives
an amortized cost per carrier per server tick; it does not isolate a constraint,
a single loaded-dimension callback, or the entire native physics solver.

The track-only reference has ten rails and no vehicles; the loaded scene adds
three shuttle rails, its machinery, all bodies, passengers and constraints.
Consequently their difference is a whole-scene cost, not an isolated per-car cost.
Base and combined loaded scenes use the same saved identities and layouts.

## Hardware and repeatability

See `hardware.json` for CPU, memory, OS and Java details. This run uses a shared
AMD Ryzen 9 7900X3D host, 24 logical CPUs, Java 21, a 2 GiB game JVM heap,
view/simulation distance 4, and the pinned Minecraft 1.21.1 / NeoForge 21.1.248
stack. The host is not isolated or CPU-pinned; `host-load.jsonl.gz` records
concurrent load. Results are observations on this host, not universal FPS/TPS
or low-end hardware guarantees. See the manifest and raw startup logs for exact
artifact and addon versions.

`run.py` contains the full setup and measurement sequence. Run it only with a
fresh output directory and fresh private-world names. It uses the owned packaged
server runtime, immutable JAR, loopback game/RCON ports 25585/25586, and restores
the original server properties after terminal cleanup. Full native world copies
remain in the ignored `build/server-benchmark/worlds` directory. Raw saved
FoundationData, logs, commands and samples are retained here.

The initial fixture attempt used an unsupported single 120-block track span and
was rejected by normal placement validation. That attempt was stopped and its
settings restored; it contributes no accepted timing data. An earlier compile
attempt used the wrong Sable containing-body method and was corrected before any
native run. These failed setup logs are retained under `attempt-1`.

The first operating command was issued while ticks were frozen and was rejected
by the winch power check. Native NBT showed 128 RPM in both motor and winch.
Create 6.0.10 `KineticBlockEntity.getSpeed()` deliberately returns zero when the
level tick manager is frozen; this was a harness ordering error, not lost motor
state. A separate motor-replacement diagnostic also failed while frozen,
confirming that replacement was unnecessary. `continue.py` resumes the accepted
series after unfreezing before dispatch. No motor replacement is used in accepted
operating runs. The failed command, power inspection and original driver state
are retained under `attempt-2`; no rejected run contributes operating timings.

## Unprofiled results

Each row pools three 600-tick windows; percentile is the nearest-rank value. All
times are milliseconds. Individual windows, callback counters and raw samples are
retained in `verification.json` and the compressed sample files.

| Scene | Mean | P95 | P99 | Maximum | Ticks over 50 ms / 1800 |
| --- | ---: | ---: | ---: | ---: | ---: |
| reference | 1.15 | 1.97 | 2.86 | 4.92 | 0 |
| base-parked | 35.54 | 42.32 | 49.60 | 60.89 | 16 |
| combined-parked | 39.11 | 47.12 | 53.71 | 75.39 | 42 |
| base-operating | 35.86 | 47.07 | 55.37 | 76.37 | 50 |
| combined-operating | 43.89 | 56.91 | 69.06 | 106.44 | 279 |

All occupied before/after snapshots preserve the same 21 chassis, 48 carriers,
96 riders and 96 native seat assignments, with no vehicle faults. Seven-car
Arrow and four-car PAX coupling counts remain correct. Every operating window
includes LIFTING, OUTBOUND, RETURNING and CAPTURING; it ends during capture,
so this benchmark is not a complete-cycle or final-unloading acceptance test.
The Arrow cars move about 7.3 blocks and Free Spin cars about 8.1 blocks over
each window. Their motion does not reach the adjoining track span, so the two-span
layout is not evidence of a moving transition in this benchmark.

The combined operating group exceeds the development budget below. All groups
finished their ordinary 600-tick windows in roughly 30 seconds, but that does not
erase the recorded long ticks. Observed pack differences are not isolated causal
estimates: the host is shared and the timing series also changes over time.

The existing carrier callback averages approximately 15.0–16.9 ms per server
tick in the loaded groups. Its counts are six process-wide physics callbacks per
server tick, including empty dimensions. Per-carrier normalization is reported
only as an amortized quantity; it is not a separately timed constraint cost.

## Profile findings and next optimization work

Four separate 600-tick Java Flight Recorder windows preserve the parked and
operating workloads for each pack. They are excluded from the timing table above.
`BenchmarkJfrSummary.java` streams the native recordings with a 256 MiB analysis
heap, reads full recorded stacks, and retains native server-tick average events.
It does not use `jfr print`'s default five-frame display limit. One of the 9,027
server execution samples has a truncated recorded stack. GC heap records confirm
2 GiB reserved by the game JVM.

| Recording | Server execution samples | Bearing-scan leaf samples | Stacks through `CarrierRuntime.tick` |
| --- | ---: | ---: | ---: |
| Base parked | 2008 | 632 (31.5%) | 909 (45.3%) |
| Base operating | 2068 | 614 (29.7%) | 926 (44.8%) |
| Combined parked | 2419 | 851 (35.2%) | 1046 (43.2%) |
| Combined operating | 2532 | 832 (32.9%) | 1091 (43.1%) |

The dominant leaf is native `CoasterCartPlotScan.scanBearingCells`. Full stacks
also identify `BoundaryGuard.check`, magnetic path sampling, and controller-family
scans as useful call sites to investigate. The carrier column is inclusive and
can overlap bearing scans; these percentages must not be added or treated as
exact method timings. Allocation sampling records substantial temporary array,
string and geometry-object allocation; its weights are estimates, not exact
allocation counters. `jfr-summary.json.gz` retains the method/class breakdown.

The next measured optimization should reduce repeated native chassis/track
lookups and unnecessary magnetic projections, then consider marker-scan caching
with explicit block-edit, assembly and lifecycle invalidation. Preserve all
eligibility and provider-removal guards and rerun the same scene and native
regressions. No production optimization was applied in this checkpoint. Host
one-minute load ranged from 12.21 to 38.66 during the unprofiled series and rose
further during profiling, so this is not an isolated attribution of addon cost.

## Verification

Run from the checkout root:

```sh
python3 scripts/verify-server-benchmark.py docs/evidence/integrations/2026-10-01/server-benchmark
```

The verifier checks hashes, normal process exits, exact raw-log/sample equality,
all identities and seat assignments, motion and phase observations, saved schema-5
ride data, property restoration, and 19 passing unit tests. It independently
re-reads all four native JFR files. The build passed; no new GameTest count is
claimed by this benchmark. Known upstream startup warnings remain in the logs.
All 25 tracked process handles were absent at cleanup, and the original server
properties were restored byte-for-byte.

## Development budget

Use a 50 ms 95th-percentile event-bracket budget for the next server optimization
pass on this same declared scene and hardware, with all rider/body/link checks
still passing. This follows the nominal 20-TPS tick interval. It is a necessary
server-development target, not a sufficient release guarantee: the timer excludes
some outer-loop work, this run has no clients, and the host is shared. Keep the
three-window results and outliers rather than reporting only the fastest window.
Client/rendering targets must be chosen from their own measured baselines.

## Remaining FD904 scope

The measurements do not establish frame times, Flywheel/shader support, network
cost for two clients, moving inversion profiles, larger track networks, active
optional equipment, or a release scene-size guarantee. Those remain required
before closing FD904. Separate Java Flight Recorder runs are kept out of the
unprofiled timing aggregates so profiler overhead cannot bias one pack's results.
