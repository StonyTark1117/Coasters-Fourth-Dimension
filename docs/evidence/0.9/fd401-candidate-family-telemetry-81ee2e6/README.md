# FD401 isolated-candidate family telemetry and trailing Arrow occupancy

On 2026-11-04, a dedicated server and two real clients ran from the isolated
`81ee2e6` source and build tree described in the [candidate freeze](../fd401-candidate-81ee2e6/README.md).
The combined packaged profile used `-PfdPackaged -PfdValidation
-PfdIntegrations=styles,engineered -PfdFeNetwork` and
`-PfdBuildDirectory=/tmp/fd401-candidate-81ee2e6/build`. The executable JAR
in that isolated output tree had SHA-256
`de64a6833fd0a4e5f7bdbc36ad40b89ff396d48941c7e6ae0ae2e52ca6ca3a70`.
There was no concurrent build in this tree. Both clients and the server ended
with Gradle `BUILD SUCCESSFUL` and exit code 0 after normal client menu exits
and a server `stop`. The complete [server](family-server.log.gz),
[Dev](family-dev.log.gz), and [Observer](family-observer.log.gz) logs, stopped
[world](world.tar.gz), and [SHA-256 inventory](SHA256SUMS) preserve the run.

The world started as a copy of the stopped combined telemetry world. Its
original Arrow route remained deliberately broken from that checkpoint.
Validation `/fd demo` fixtures supplied separate Legacy `(122,83,32)`, Free
Spin `(250,83,32)`, and Revised `(378,83,32)` circuits. At each, Dev boarded
one real eight-seat car normally, used the in-ride editor to close restraints
and dispatch, completed the circuit, was caught by rear station redstone, then
opened restraints and normally dismounted. Observer linked a Create Display
Link to a sign and selected occupancy, then phase, through the normal GUI.
The signed output showed `1 / 8` while occupied and Boarding → Running on
dispatch; goggles showed open/denied → closed/ready, then `0 / 8` after unload.
The received packets report `cars:1,capacity:8` for each family. The
[Legacy](legacy-sign-occupied.png),
[Free Spin](free-sign-occupied.png), and [Revised](revised-sign-occupied.png)
sign captures, their `*-ready-goggles.png`, `*-phase-running.png`,
`*-capture-editor.png`, and `*-unloaded-goggles.png` files document each step.
The raw Observer log contains the corresponding `FD_CLIENT_TELEMETRY` packets.

Dev initially reconnected still mounted in the intentionally faulted Arrow
fixture. Attempts to dismount or relocate that saved mount were unsuccessful,
so `/kill Dev` and a creative respawn were used as a **fixture reset before**
the Legacy run. This is not evidence of a normal unload from the broken Arrow
route. Subsequent family boarding, operation, capture, and unload were normal.

A fresh Arrow demo at `(794,83,32)` supplied its first car. Dev placed a
second Arrow car from the item on open rail and joined the two native carts
with the normal Minecraft Chain interaction. The server log records the
selection at 06:00:40 and a successful six-block link at 06:02:50. Observer
normally linked the station to a sign with `ride_occupancy` selected. The
[empty](trailing-observer-empty.png) goggles showed `0 / 8 (2 cars)`. Dev
right-clicked a real seat on the **second** car; its `RootVehicle` was a Create
seat. The [sign](trailing-occupied-sign.png) and
[goggles](trailing-occupied-goggles.png) showed `1 / 8 (2 cars)`, and the
Observer packet at 06:04:55 recorded `cars:2,capacity:8,occupied:1`. Holding
Shift normally dismounted Dev; the sign and packet at 06:05:10 returned to
`0 / 8`, as did the [goggles](trailing-unloaded-goggles.png). The trailing
train did not dispatch in this run; its evidence is occupancy propagation.

These circuits and station redstone were validation fixtures. The Display
Links, source selections, car-item placement, chain coupling, seat boarding,
editor controls, and dismounts were client actions. This checkpoint closes the
candidate-matched Legacy/Free Spin/Revised occupancy/control transitions and
trailing Arrow occupancy. It does not establish source-preserving fault clear,
repair-before-realignment denial, PAX authority phases, controller-unavailable
presentation, or individual optional-profile clients.
