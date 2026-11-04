# FD401 occupied Styles client snapshot-lag trace

This [focused client run](client-console.log.gz) used the saved styled
`FD401StopCycle` world from the [exact-candidate UI edit and ride](../fd401-styles-ui-reload-ride-2c5d9a7/README.md)
in an isolated X display and game-directory copy. The current development
classpath retained the frozen candidate's production geometry sources and
Track Styles 1.2.0, with an opt-in [`ClientSnapshotTrace.java`](../../../../src/main/java/dev/fourthdimension/client/ClientSnapshotTrace.java)
diagnostic added. This was **not** a rerun of the exact frozen whole JAR.
The [capture-time diagnostic source](ClientSnapshotTrace-capture.java.txt) is
retained byte-for-byte (SHA-256 `f5e753d5350eadfd2ac59a948f37d42188172e7a25ebba8db3ebb01096feecdf`).
The committed version resolves its reflective Sable field lazily when tracing
is enabled; that fixture-only change followed the run.

The fixture boarded Dev into the edited car
`4e207381-e5e7-4007-95ab-280620e85e7d`, closed restraints, dispatched it,
and removed one station redstone block in the copied world. The client log
shows motion at about 1.92 blocks/s with capture off and occupied carrier
`e2045c37-b445-4f95-bb82-e71bbcb33169` rotating through the banked and
flat portions. A fixture `/kill Dev` ended capture after the ride; this run
does not assert a normal unloading endpoint. The client saved, returned to
title, and exited with `BUILD SUCCESSFUL`.

The [raw trace](client-snapshot-trace.jsonl.gz) has a completion marker for
8,003 client tick samples, including 5,537 with over 0.001 block of chassis
movement per client tick. It records Sable's tick pointer, latest information
tick, and the newest two pose snapshot ticks for the mounted chassis and both
carriers. The [audit](report.json), produced by
[`audit-client-snapshot-lag.py`](../../../../scripts/audit-client-snapshot-lag.py),
found:

| Observation | Result |
|---|---:|
| Default Sable interpolation delay | 1.5 ticks |
| Samples after all three mounted-car pose pairs became available | 5,764 |
| Samples with pointer beyond at least one mounted-body pose tick | 229 |
| Moving samples with pointer beyond a mounted-body pose tick | 2 |
| Samples moving over 0.05 block/tick with pointer beyond pose tick | 1 |
| Largest pointer lead in that faster-moving set | 0.005483 tick |

The long stale interval was near the dispatch boundary: information tick
remained 2796 while all three latest body-pose ticks stayed 2794, and the
recorded chassis position was effectively stationary after its first two
samples. During the active lap, sample 3388 had a 0.10014-block chassis step,
with pointer 3934.00548 and the three latest pose ticks at 3934. This is a
small but **observed** entry into Sable's extrapolation branch. The source
[pointer review](../fd401-styles-client-pointer-2c5d9a7/README.md) explains
the one-snapshot-interval cap.

The trace records **snapshot ticks**, not the pose transforms in those two
snapshots or each render-frame pointer. It establishes that the prior
between-snapshot sweep cannot be used to claim clearance for every client pose
in this ride. It does not determine the exact extrapolated vehicle/player
envelope at sample 3388 or under larger packet delays. The prior
[native substep audit](../fd401-styles-physics-substeps-2c5d9a7/README.md)
remains separate server-side evidence. The report explicitly sets
`clearanceCertified=false`.

`SHA256SUMS` pins the archived trace, log, report, capture-time source and
auditor. The uncompressed log SHA-256 is in the report's `inputHashes`.
