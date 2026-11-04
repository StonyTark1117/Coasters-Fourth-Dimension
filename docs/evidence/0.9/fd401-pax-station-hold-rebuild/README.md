# FD401 saved PAX consist: station hold and occupied client cycle

On 2026-11-04, an isolated Styles + Engineered + validation FE-network
packaged-profile server and Dev client continued a **copy** of the displaced
four-car PAX world. The source snapshot was `2bb8fe8` plus the station hold
change in `ShuttleRuntime`; the built JAR SHA-256 was
`53a71f0e3d3b5ed4e2a65a3982b61cdda9d7b1183dc44c1198144878d4f3b5da`.
This was a focused source-fix replay, not a frozen release-candidate run.

The input was the earlier stopped checkpoint after rider evacuation, winch
detach and three guarded retirements, with the original lead and one prior
replacement still present. In this disposable copy, admin fixture preparation
removed smooth stone at x895/y85/z76–78. Dev placed the remaining two PAX
cars from normal items and made all three native chain links by client clicks.
The [server log](server.log.gz) records the links at exactly 6.000 blocks:
rear `3ba9c520` to `7b7f0154` at 16:32:43, `7b7f0154` to `ebd70587` at
16:33:53, and `ebd70587` to original lead `cd90df9d` at 16:36:27.

Dev used the [lead editor](lead-editor6.png) to release retention, then
sneak-used the existing winch with an empty hand. A live winch NBT query
returned four assigned cars and `shuttle_phase="BOARDING"`. Dev opened
restraints, normally boarded a seat on the linked `7b7f0154` car, closed
restraints in the mounted editor, and selected Dispatch. The server recorded
the accepted mount at 16:42:08. Live winch queries then returned `LIFTING`,
`OUTBOUND`, and finally `BOARDING` with `shuttle_cycles` advancing from 2 to 3.
The [returned mounted editor](return-editor.png) showed closed restraints;
Dev opened them and Shift-dismounted at 16:48:00, as recorded by the server.

The [stopped world](world.tar.gz) was saved and shut down cleanly. Its decoded
[pose summary](stopped-pose.txt) has all four chassis at Y≈87.696 with
six-block station spacing, open restraints and zero occupants. The server log
contains no native unlink after the three client chain links. The compressed
[client log](client.log.gz) and [unloaded view](unload.png) are retained.

This closes the focused saved-world physical rebuild, rebind and occupied
Dispatch/cycle action on the station-hold source. It does not establish the
separate frozen packaged normal-client long-hold acceptance or cold-restart
acceptance on a release candidate. Fixture block removal and teleport/item
provisioning were admin preparation; car placement, chain linking, editor
actions, winch binding, boarding, Dispatch and unloading were normal client
interactions.
