# Packaged standalone telemetry: native and two-client checkpoint

Tested on 2026-11-02 from `046ead9b564a55cf08f3e28b24eb22aa4f06d3fd`.
The packaged `coasters-fourth-dimension-0.9.0-dev.27.jar` SHA-256 is
`d271ea78298a2b421ea76ac0d5a348ecd6ab90beba00cc9abb5c79556f0741f2`.
Schema 6 / protocol 7 remain unchanged. This isolated artifact is not the final
integrated release candidate. [manifest.json](manifest.json) records dependencies
and evidence hashes; no third-party JAR or assets are included in this bundle.

## Native result

The packaged standalone server passed all **47 required GameTests** in a successful
5m34s Gradle run; 26 unit tests also passed. The telemetry batch covers all five
families, six registered sources, current controller authority, stale readiness,
linked-peer repair guards and PAX state-machine ownership. The native log is
[standalone-native.log.gz](standalone-native.log.gz).

Both recorded route verifiers passed. The unchanged source-pinned dense
single-car audit passed all five routes, with 5,764–5,873 one-tick samples per
route and no uncertified pairs. The linked-train audit also passed both original
train routes, with 6,548 Arrow and 6,973 PAX samples and no uncertified pairs.
Complete original frame files and compressed traces are retained
beside the reports. These are sampled native Pig/default-model bounds, not
player-mesh, continuous swept-volume or rendered clearance certification.

**Subsequent verifier finding:** this run used verifier `b7b704b5…`, which skips
rider-to-rack checks when a rider has no nearby track triangle. Its successful
exit and empty `uncertifiedPairs` therefore do not establish rack clearance for
every sampled rider. Preserve these reports as the original limited results;
complete rider-to-rack acceptance requires a corrected-verifier rerun on these
exact retained traces. Native tests and the client telemetry observations below
are independent of this audit coverage gap.

## Actual clients and controls

Two packaged clients, `Dev` and `Observer`, joined a fresh localhost dedicated
server. The client rendered through Xvfb at 854×480 using llvmpipe/Mesa, so this
is functional GUI evidence, not the hardware/FPS acceptance gate. The existing
`-PfdValidation` flag enabled read-only diagnostics. The new client trace records
the targeted block entity's received snapshot; it does not query the server,
inject tooltip text, control a ride or replace screenshots.

Fixtures were built with `/fd demo arrow`, `legacy`, `free_spin`, `revised` and
`pax`, using separate loaded corridors. Teleports and inventory commands supplied
test positioning and items. Signs/support blocks were fixture setup. Boarding,
restraint changes, maintenance retention, winch starts and dismounts used normal
client interactions. This does not prove survival construction or item acquisition.

| Observed case | Evidence and bounded result |
| --- | --- |
| Five-family goggles | Arrow shows 0/4 seats; Legacy, Free Spin and Revised each show 0/8; PAX shows four cars and 0/16 alongside Create's actual kinetic overlay. See the family `*-goggles.png` files and client snapshot logs. |
| Real Display Link setup | Dev selected an oak sign with the Display Link item, attached the link to the Arrow station, and selected sources in the normal GUI. [The source list](link-source.png) shows all six registrations. |
| Occupancy and late join | Dev boarded an Arrow seat normally. The sign changed 0/4→1/4. Observer joined afterward and saw 1/4 in both [goggles](observer-late-join-goggles.png) and the [sign](observer-sign-occupied.png). |
| Fresh control state | Closing restraints through Dev's editor changed Observer's goggles to [dispatch ready](observer-ready.png). Retain changed them to [retained/dispatch denied](observer-retained.png). The dispatch source showed the corresponding server text. |
| Empty output | Selecting the fault source while no fault existed [cleared the sign](sign-cleared.png), including its server text. This proves source-switch clearing, not recovery from an actual fault. |
| Reload | Observer used F3+T at 16:30:17; resource reload completed and the [occupied retained overlay](after-resource-reload.png) rendered again. One successful reload does not resolve the separate historical font defect. |
| Cold server restart | Saved and stopped the server with Dev seated in the captured, restrained, retained Arrow car. Both clients reconnected to a fresh server JVM. The same controller binding and occupancy source survived. Observer's received snapshot was 0/4 while Dev was offline and changed to 1/4 after rejoin; [goggles](after-cold-restart.png), sign and rider assignment agreed. Dev then opened restraints and dismounted normally. Native seat-entity UUID retention is not claimed. |
| Occupied PAX cycles | Observer boarded a PAX seat normally and closed restraints in its editor; Dev operated the bound powered winch. Two complete cycles preserved one assigned rider across four cars, then returned to Boarding. The second cycle's client-triggered screenshots show [Lifting](pax-client-phase-lifting.png), [Outbound](pax-client-phase-outbound.png), [Returning](pax-client-phase-returning.png), [Capturing](pax-client-phase-capturing.png) and [Boarding](pax-client-phase-boarding.png), paired with server state in `pax-client-phase-observations.jsonl`. |
| PAX unload and display | Normal restraint opening and Shift dismount reduced assignments to zero and [goggles to 0/16](pax-unloaded.png). A separately configured winch Display Link used `ride_phase` and visibly wrote [Boarding](pax-phase-sign.png). |

Server observations and full compressed client/server logs are retained. The
client-triggered PAX images were captured after receipt of each new phase to
avoid labeling the preceding rendered phase with the newer server phase. Both
clients exited through **Quit Game** and both server JVMs stopped normally; all
four Gradle sessions exited zero. The final world contains eight chassis and
twenty carriers with no rider assignments. A local pre-restart world backup and
final world remain in the isolated validation workspace.

## Reproduction

Use the exact source and dependency hashes in the manifest. From that revision:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -PfdPackaged
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PfdPackaged
```

Preserve frame/trace files before another GameTest run deletes them. Install
`scripts/requirements-geometry.txt` in an isolated Python environment. Run the
unchanged `verify-recovery-bank-frames.py` on each frame file (add `--trains` for
the train file), then `audit-recovery-mesh-trace.py` with the pinned Coasters JAR,
Minecraft client JAR, frame JSON and matching `.jsonl.gz` trace.

For GUI replay, launch `runServer`, `runClient` and `runObserver` with
`-PfdPackaged -PfdValidation`, adding `-PfdConnect=127.0.0.1:25565` to clients.
Use an isolated flat creative world; the fixture origins passed to `/fd demo`
were `(0,80,0)`, `(128,80,0)`, `(256,80,0)`, `(384,80,0)` and `(512,80,0)`.
Arrow station/link/sign are `(-6,83,32)`, `(-6,83,31)`, `(-6,84,38)`;
PAX winch/link/sign are `(506,85,73)`, `(506,85,72)`, `(506,86,79)`.
Follow the normal controls in the [client protocol](../client-acceptance.md).

## Remaining gates

This checkpoint does not certify the combined/individual optional profiles on
this artifact, dynamic visible output for every source, linked trailing-car rider
reporting in real clients, physical repair/fault-clearing telemetry, unloaded
controller availability, PAX power interruption or occupied PAX cold restart.
Legacy/Free Spin/Revised live occupancy/control transitions remain unexercised
here. Final integrated-artifact checks, full FD401–FD404 and FD901–FD905 remain
open. The earlier four-profile CI belongs to `177d4f6`; it is not reassigned to
this artifact, and GitHub's account restriction remains separate from these
local results.
