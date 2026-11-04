# FD401 PAX support repair and realignment retest

On 2026-11-04, the committed `2e12e9d` source was exported to an isolated
directory and built offline with Java 21 for the combined packaged profile
(`fdPackaged`, `fdValidation`, `fdIntegrations=styles,engineered`, `fdFeNetwork`).
The [JAR](coasters-fourth-dimension-0.9.0-dev.27.jar) has SHA-256
`1afa7066607313898a11ff628b762be86369a82a62423c3fcce4ffd9008d62f`.
The dedicated server, Dev client and Observer client all loaded this JAR and
exited 0 after normal menu exits and server `stop`. The [compile log](fd401-realign-fix-compile.log),
[server log](server.log.gz), [Dev log](dev.log.gz), [Observer log](observer.log.gz),
stopped [world](world.tar.gz), screenshots, and [SHA-256 inventory](SHA256SUMS)
retain the run.

The server resumed the stopped world from the earlier [candidate PAX fault and
phase run](../fd401-candidate-pax-fault-81ee2e6/README.md), before the failed
support-loss attempt. The existing PAX shuttle had four normally linked cars,
16 seats, a winch at `(894,85,73)`, and its original station section between
`(900,85,65)` and `(900,85,97)`. Dev normally mounted a fourth-car seat and
closed restraints in the editor. Smooth-stone footholds and one PAX track item
in Observer's hotbar were command fixtures prepared **before** support removal.
Observer then normally mined the station section and normally restored it by
using the track item on the two anchors. The original curve was absent after
the break and present again after the two item clicks, as checked through block
data. No car or winch NBT was edited.

After restoration, the [editor](editor-after-restore.png) offered Repair
matching track, not Realign. Dev selected Repair; the
[next editor](editor-after-repair.png) offered Realign. Dev selected Realign.
Observer's [goggles immediately afterward](goggles-after-realign.png) showed
**1/16, four cars, Boarding aligned Yes, dispatch ready Yes**. The
[ready editor](editor-ready-after-realign.png) also remained available. After
about 29 seconds, [goggles](goggles-after-hold.png) still showed the same
four-car ready state and Dev still had a Create seat `RootVehicle`. The server
logged no native coupler snap or unlink after its initial restored links.
Dev normally opened restraints and dismounted; the [final goggles](goggles-after-unload.png)
showed **0/16, four cars, Boarding aligned Yes**. Observer packets at 06:41:33,
06:42:11 and 06:42:13 record the occupied ready, open-restraint, and empty
states respectively.

This closes the **short support-loss → restore → Repair → Realign → normal
unload** case on `2e12e9d`. The earlier prolonged unsupported hold on
`81ee2e6` remains a separate failed record; this run does not prove that
arbitrarily long holds or every station geometry recover. The new telemetry
guard for a native consist that differs from the winch's saved cars was
compiled but was not exercised by a broken-consist runtime in this run.
