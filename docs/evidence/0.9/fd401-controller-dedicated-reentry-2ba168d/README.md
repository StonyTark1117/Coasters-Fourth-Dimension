# FD401 dedicated controller late join, reconnect and tracking re-entry

On 2026-11-04, a dedicated server, Dev client and Observer client loaded
byte-identical combined packaged `2ba168d` JARs, each SHA-256
`7430b4f4abdb41e248efd778f5cd9f68929d04211d7837085bbae287565cbdcf`
([archived artifact](../fd401-controller-native-availability-2ba168d/README.md)).
The profile used `-PfdPackaged -PfdValidation
-PfdIntegrations=styles,engineered -PfdFeNetwork`. The server continued a
copy of the stopped [remote-controller world](../fd401-controller-live-unavailable-2ba168d/README.md).
The [server](server.log.gz), [Dev](dev.log.gz), [first Observer](observer-first.log.gz)
and [reconnected Observer](observer-reconnect.log.gz) logs,
[stopped world](world.tar.gz), screenshots and [hash inventory](SHA256SUMS)
retain the run. Both client sessions and the server exited 0 after normal
disconnect/stop and saved the world.

Dev joined first at the remote station and held its chunk loaded. The saved
Arrow car's distant native chunks were unloaded: server availability read
`chassis=0/1 carriers=0/2 assignments=0`, and the linked sign stored
`Vehicle chassis is unavailable`. Observer joined afterward. The first spawn
landed over water; Observer drowned before a small stone platform was added,
then respawned and returned to the controller. Its client log received the
unavailable station snapshot at 07:44:17. This first-join packet is retained,
but its live goggle overlay was obscured by Minecraft's new-client tutorial.

Observer disconnected normally, and the same client was relaunched with the
tutorial disabled. On reconnect, it received a fresh unavailable snapshot at
07:50:33 and [rendered the full message](reconnect-unavailable.png) in
Engineer’s Goggles. The station and Display Link binding were unchanged.

For tracking re-entry, Observer moved to `(900,200,-900)` while Dev stayed near
the remote station. The server force loaded only the saved car's route chunks;
availability became `1/1` chassis and `2/2` carriers, and the sign changed to
`Boarding`. When Observer returned, its [goggles](reentry-available.png)
showed the actual available one-car/four-seat BOARDING state and the expected
out-of-range dispatch denial. After the route force-load tickets were removed,
availability returned to `0/1` and `0/2`, the sign stored the unavailable
reason, and Observer's [goggles](reentry-unavailable.png) returned to the full
unavailable message. No car or controller rebinding occurred.

The remote station, vehicle binding and Display Link configuration were
command fixtures from the prior record. Server commands also added a small
spawn platform after the initial water spawn, changed Observer to Creative,
equipped goggles, teleported players and toggled route chunk tickets. This
does not certify an occupied unload, normal creation of the remote binding,
or standalone optional profiles. The earlier singleplayer record separately
shows the physical sign and goggles through F3+T and cold reopen.
