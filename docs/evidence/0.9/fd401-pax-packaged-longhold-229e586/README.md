# FD401 packaged PAX long-hold runtime acceptance

On 2026-11-04, committed `229e586` was exported to `/tmp/fd401-229e586-source` and built as the distributable JAR with `fdPackaged`, `fdValidation`, `fdIntegrations=styles,engineered`, and `fdFeNetwork`. `candidate.jar` has SHA-256 `94c17e5040f13dfab9adebfe5b57b1687db875d8501bfd88a2eb0b31dcfb0828`.

The normal Dev client joined a dedicated server running this JAR and rendered the saved four-car PAX station. It exited through Disconnect and Quit Game. The server saved and stopped normally. This client session did **not** mount a rider, remove track support, or operate the recovery controls; it is a packaged client launch and join check, not a complete operator replay.

A fresh dedicated server using the **same packaged JAR** enabled the `fourthdimension` GameTest namespace and ran only `paxsupportrepairrealignmentresumespoweredwinch` with `fourthdimension.paxLongHoldTicks=4800`. The test assembled four native-linked cars and 16 mounted riders. It passed its assertions after 4,800 unsupported ticks, restoration, Repair, Realign, Recover, and powered relaunch. The log's result is:

`FD_PAX_SUPPORT_RECOVERY repaired=true realigned=true recovered=true relaunched=true riders=16 unsupportedTicks=4800`

The test world was saved after the pass. Its winch subsequently entered `FAULT` with `Shuttle winch chunk unavailable or winch removed` when the test fixture released its forced chunks, so this world is not a cold-restart recovery proof. The separate saved station world was cold restarted on the packaged server and reopened in `BOARDING` with the same four assigned car IDs. Native links were not independently queried after that restart.

Artifacts: [JAR](candidate.jar), [packaged gate log](packaged-gate.log.gz), [Dev client log](dev-client.log.gz), [cold restart log](cold-restart.log.gz), [client station screenshot](client-station.png), [stopped gate world](gate-world.tar.gz), and [SHA-256 inventory](SHA256SUMS). The named source run on the same PAX fix is recorded [separately](../fd401-pax-longhold-source-229e586/README.md).

This accepts the packaged **named runtime gate**. A full normal-client operator replay of the 4,800-tick sequence remains outstanding.
