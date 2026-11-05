# FD401 PAX two-client recovery replay

2026-11-04, primary `main` source `8f370d09015a7e782f0bd0a050cc311ea1bd0929`.
The retained [candidate.jar](candidate.jar) is `0.9.0-dev.27`, SHA-256
`253e6d92387807c1ff5e7768bd8cd5d9b71a0dd05e0097979f19b48539c2a73a`.
It was built with Java 21 from committed source into an isolated Gradle output.
This is a separate runtime identity from the `dd0a851` qualification baseline;
it does not promote that baseline or qualify every optional profile.

## Runtime and result

The dedicated server and two real clients ran from the project checkout with
Java 21. The clients used separate private Xvfb displays, `:214` and `:215`,
distinct game directories, `-Xmx1536M`, and verified owned systemd scopes with
`MemoryMax=3G` each. The server used an isolated port `25577`, RCON `25578`,
and `-Xmx1536M`. Memory and process headroom were checked before client starts.
The displays were stopped after both clients quit; another task's display was
left alone. Graphics were software rendered under Xvfb. This run does not
establish a native GPU or shader result.

The saved four-car PAX station came from the earlier
[station-hold world](../fd401-pax-station-hold-rebuild/README.md). Dev boarded
normally, closed restraints and dispatched with the Rotation Tool editor.
Observer removed only the native curve span during `LIFTING`, keeping the
shared anchor and its other curve. The train entered `FAULT` with
`Matching track support lost`, retained Dev's assignment and native links, and
remained in that state for **5,055 game ticks**. Observer rebuilt the matching
PAX curve with the normal track item; Dev used editor `Repair matching track`
and `Realign matching track`. The first subsequent winch interaction registered
`Resume`, so that cycle is **not** counted as an explicit `Recover` check.

The same occupied train was dispatched again. A second curve-only `LIFTING`
fault held **4,977 game ticks** with one assigned rider and intact links.
Observer rebuilt the matching curve and Dev repaired and realigned it. Observer
then held sneak and used the winch at `(894,85,73)`; immediate server query
reported `CAPTURING`, then `BOARDING` with an empty fault, cycle count 5 and
one assignment. Dev dispatched again through the normal editor. The server
later received its command-session `SIGTERM`, saved normally, and left the
occupied train in `BOARDING` with cycle count 6 and one assignment. This was a
process interruption **after** the active phase had completed; it is not an
active-phase cold-restart pass.

An intervening inspection startup used a formatting-only JAR. Its world state
was discarded. The pre-inspection saved world was restored, and the exact
committed `253e6d92…` JAR above was rebuilt and used for the final reopen.
Dev rejoined mounted with closed restraints and all three native car links
restored. Dev opened restraints in the editor and dismounted normally. Server
queries then showed zero assignments, absent `RootVehicle`, `BOARDING` and
cycle count 6. Both clients quit normally and the owned server stopped cleanly.
The retained final world is [artifact-matched-unloaded-world.tar.gz](artifact-matched-unloaded-world.tar.gz).

The earlier exploratory removal of the whole shared anchor also removed an
adjacent occupied curve and prevented repair. That invalid setup was discarded
before these two curve-only holds and is excluded from the accepted result.
Temporary stone support columns and creative items enabled Observer to reach
and replace the curve in the void-world fixture; they were not dispatch or
recovery substitutions.

## Retained evidence and boundary

The [first fault world](final-curve-only-fault-world-5055ticks.tar.gz),
[second fault world](final-second-curve-fault-world-4977ticks.tar.gz),
[post-recovery cycle-6 world](post-recover-cycle6-world.tar.gz), and final
unloaded world retain the state transitions. The [two-client server log](two-client-server.log.gz),
[Dev client log](dev-client.log.gz), [Observer client log](observer-client.log.gz),
[reopen server log](unload-server.log.gz), and [reopen client log](unload-client.log.gz)
retain startup, connection, boarding, native-link and shutdown events.
Screenshots include [faulted Dev](final-curve-only-fault-dev.png),
[faulted Observer](final-curve-only-fault-observer.png),
[winch before Recover](second-recover-winch-before.png),
[post-Recover dispatch](post-recover-dispatch-editor.png), and
[final unload](artifact-matched-dev-unloaded.png). RCON state queries and the
operator actions described above were observed during the live run; the
vanilla server log does not print every RCON response or UI click.

This closes a bounded normal-client PAX `LIFTING` support-loss and recovery
replay with **one occupied seat**. It does not cover sixteen simultaneous
riders, the separate `OUTBOUND` link-loss regression, cold restarts *during*
each active phase, all six Create display sources, or the Styles and Engineered
profile matrices. See [SHA256SUMS](SHA256SUMS) for every retained binary.
