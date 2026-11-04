# FD401 corrected client snapshot-pose capture

This is one corrective, focused Styles-only client ride using an isolated copy
of the exact-candidate styled `FD401StopCycle` world. It uses the opt-in
[`ClientSnapshotTrace.java`](../../../../src/main/java/dev/fourthdimension/client/ClientSnapshotTrace.java)
added after the [first packet-lag trace](../fd401-styles-client-snapshot-lag-2c5d9a7/README.md).
The source geometry remains the frozen candidate's, but this is an instrumented
development classpath, not the exact frozen whole JAR. The copied Sable client
config retained the default 1.5-tick delay.

Dev boarded the UI-edited Arrow car, closed restraints, dispatched it, and
removed one station redstone block in the copied world. The
[client log](client-console.log.gz) shows the occupied moving carrier rotating
through the ride. A fixture `/kill Dev` ended the trace after 5,984 moving
client samples. The client saved the copy, returned to title, and exited
cleanly. This run does not assert a normal unload; the earlier exact-candidate
ride retains that evidence.

The [complete raw trace](client-snapshot-pose-trace.jsonl.gz) contains 6,398
mounted client tick samples. The [lag audit](lag-report.json) found 202 samples
where Sable's pointer was ahead of the latest mounted-body snapshot tick.
**None** occurred while the chassis moved more than 0.001 block in that client
tick. Every such sample contains the previous and latest snapshot transforms
for all three mounted-car bodies, plus Sable's last and current tick poses.

The [pose audit](pose-report.json), produced by
[`audit-client-snapshot-pose-lag.py`](../../../../scripts/audit-client-snapshot-pose-lag.py),
measured 606 body snapshot pairs across those 202 samples. The largest
snapshot-to-snapshot plot-center motion was 0.003009 block; the largest
quaternion angular change was 0.0000233 radian. The pointer could lead a
latest snapshot by two ticks in the stationary interval, but the pinned Sable
interpolator caps its extrapolation to one additional snapshot interval.

This establishes that **this** occupied moving circuit had no observed
extrapolation at client tick boundaries. It does not undo the first trace's
0.005483-tick lead during faster motion, prove the render-frame packet state
between client ticks, or certify collision clearance for every possible
network delay. Both reports explicitly set `clearanceCertified=false`.

`SHA256SUMS` pins the archived trace, reports, log, diagnostic source and
auditors. The report hashes the raw trace and the uncompressed client log.
