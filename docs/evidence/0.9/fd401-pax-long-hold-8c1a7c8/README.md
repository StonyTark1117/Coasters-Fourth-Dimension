# FD401 PAX prolonged support-loss recovery source gate

On 2026-11-04, the isolated combined Styles + Engineered NeoForge development
server ran only `paxsupportrepairrealignmentresumespoweredwinch` with
`-PfdPaxLongHoldTicks=4800`. The four-car native-linked PAX fixture had 16
mounted riders and lost its matching station curve during an active lift. The
curve was absent for 4,800 server ticks before exact restoration, Repair,
Realign, winch recovery, and powered relaunch.

The first named run exposed an unbound winch telemetry crash: `owner(level,
null)` reached `List.of().contains(null)`. The source now returns no owner for a
null car ID. The next run kept all native links through the unsupported hold,
but independently snapping cars after Repair broke all three couplers. Giving
the support-loss FAULT phase the existing station-spaced snap prevented that
break, but its eight-block proximity interlock refused Realign after the train
had drifted too far. These outcomes are retained in the [crash](unbound-winch-crash.log.gz),
[independent snap](independent-snap-failed.log.gz), and [distance rejection](station-distance-rejected.log.gz)
server logs.

Committed source `8c1a7c8` adds transient native world holds for all four
chassis during the support-loss fault, releases them immediately before the
validated snap, and applies station-spaced alignment to the support-loss FAULT
phase. The final isolated run passed: its [server log](held-relaunch-passed.log.gz)
records `FD_PAX_SUPPORT_RECOVERY repaired=true realigned=true recovered=true
relaunched=true riders=16 unsupportedTicks=4800`. The named test also asserts
all four native links for each car before restoration and after powered
relaunch. The server stopped cleanly with exit code 0. The log files are listed
in [SHA256SUMS](SHA256SUMS).

This is a source-level GameTest with fixture riders and RCON invocation. It
does not establish a frozen packaged JAR, normal client Repair/Realign actions,
Display Link behavior during the prolonged hold, cold restart during that hold,
or recovery of a train whose native couplers have already broken. Those remain
separate operator and release-evidence boundaries.
