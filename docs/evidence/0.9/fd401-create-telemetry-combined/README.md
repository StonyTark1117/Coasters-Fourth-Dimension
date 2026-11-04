# FD401 combined-profile Create telemetry client run

On 2026-11-04, one packaged dedicated server and two packaged clients ran the
combined Styles/Engineered profile with `-PfdValidation -PfdFeNetwork`. The
server loaded `coasters-fourth-dimension-0.9.0-dev.27.jar`; its file hash before
and after the run was
`de64a6833fd0a4e5f7bdbc36ad40b89ff396d48941c7e6ae0ae2e52ca6ca3a70`.
Concurrent builds shared the output directory, so that hash does **not** prove
which bytes the live JVM loaded. The source/JAR freeze remains a release gate.
The stopped [world](world.tar.gz), compressed [server](server2.log.gz),
[restart](server-restart.log.gz), [Dev](dev-reconnect.log.gz), and
[Observer](observer.log.gz) logs, and [SHA-256 inventory](SHA256SUMS) preserve
the run. The initial server and Dev segments are also retained as
`server.log.gz` and `dev.log.gz`; the first Dev Xvfb connection ended when its
display closed, then Dev rejoined. The second Dev process likewise ended when
its X display closed after the player disconnected; both Dev logs end with
Gradle `BUILD FAILED` from display loss, not a completed client exit.

The validation `/fd demo` commands created Arrow, Legacy, Free Spin, Revised,
and PAX fixtures. This run exercised **one Arrow car** at a bound station
`(-6,83,32)`; the other family fixtures were present but were not operated for
this telemetry checkpoint. Dev boarded the real Arrow seat by normal right
click, closed/opened restraints and dispatched through the in-ride editor.
Observer joined after Dev boarded. The received `FD_CLIENT_TELEMETRY` snapshot
at 04:58:14 recorded `occupied=1`, `capacity=4`, and open restraints;
[goggles](observer-occupied-goggles.png) and the normally linked
[sign](observer-occupied-sign.png) showed the same occupancy. At 05:02:42 the
observer received closed restraints and dispatch readiness without rejoining.

Observer selected a Create Display Board target, placed a Display Link on the
station, and chose all six sources in the normal [source GUI](source-list.png).
The four-wide board was kinetically powered at 64 RPM by a creative motor and
cogwheel. The motor's speed was supplied with block NBT as **fixture setup**;
no display text or telemetry was injected. Its source and output observations
were:

| Source | Server output and visible target |
| --- | --- |
| `ride_status` | Ready state; [board](board-status-ready-clean.png) displayed `READY TO DISPATCH`. |
| `ride_occupancy` | `1 / 4`; [board](board-occupancy-1-of-4.png) and earlier sign displayed the occupied value. |
| `ride_restraints` | Server Display0 changed from translated `Yes` to `No` after Dev pressed **Open restraints**. [Yes](board-restraints-yes-clean.png) and [No](board-restraints-no-settled.png) images show the board, although this four-wide target clips the final word. The earlier sign NBT held the full `Restraints closed: Yes` text. |
| `ride_dispatch` | Display0 changed from `All carriers must be captured and restraints closed before dispatch` to the translated ready text when Dev closed restraints. The long denial is clipped by the board width. |
| `ride_phase` | [Boarding](board-phase-boarding.png) changed to [Running](board-phase-running.png) after Dev pressed **Dispatch**. |
| `ride_fault` | Empty text changed to `End of compatible track; capture and inspect the route` after a demo track anchor was removed while the car ran. The [board](board-fault-track-end.png) clips the message, while [goggles](fault-station-aim.png) show it in full. Observer's received snapshot at 05:17:02 includes that fault, `occupied=1`, and `phase=RUNNING`. |

An overly broad temporary camera platform displaced the station and both
earlier links partway through the run. The station was restored as a fixture,
then Observer rebound it to the same Arrow car by normal empty-hand click and
placed a new Display Link normally. The 05:12:03 unavailable client snapshot
is an artifact of that displacement; it is **not** accepted as a controller
availability lifecycle test. The track anchor removal later intentionally
left this saved demo route broken to produce a real fault.

Both clients disconnected before a clean server stop. The same world then
cold-started. The server still reported the `ride_fault` link, board speed
`64.0f`, and the fault message. Observer rejoined and received the fault in
`FD_CLIENT_TELEMETRY` at 05:19:28; [goggles](cold-restart-goggles.png) showed
the same state. Occupancy became zero because Dev had disconnected. The
observer exited through Minecraft's menu and its log ends `BUILD SUCCESSFUL`;
the restarted server also stopped cleanly.

This run covers normal source selection and dynamic output for all six sources
on one combined-profile Arrow controller, a late observer join, and fault
persistence through cold restart. It does not cover trailing Arrow occupancy,
other-family occupancy/control changes, source-preserving fault clear,
repair-before-realignment denial, a healthy capture after this fault, or
individual optional-profile client acceptance.
