# FD401 PAX long-hold source retest

On 2026-11-04, the named `paxsupportrepairrealignmentresumespoweredwinch` GameTest ran against an isolated source export with the `229e586` shuttle hold fix and `fourthdimension.paxLongHoldTicks=4800`. It passed Repair, Realign, Recover, powered relaunch, four native links, and 16 retained mounted riders after 4,800 unsupported ticks. The [server log](server.log.gz) records:

`FD_PAX_SUPPORT_RECOVERY repaired=true realigned=true recovered=true relaunched=true riders=16 unsupportedTicks=4800`

The packaged JAR acceptance is recorded [separately](../fd401-pax-packaged-longhold-229e586/README.md).
