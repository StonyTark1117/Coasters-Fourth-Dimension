# FD401 PAX normal-client long-hold blocker on `1f13a0e`

On 2026-11-04, an isolated export of committed `1f13a0e` built the combined
Styles + Engineered packaged profile with validation and the FE-network
dependency. The built [JAR](candidate.jar) has SHA-256
`c63ea2824cd007dea1859e2dd5d78d785517fffaccc22272f4f4f94502ced703`.
The dedicated server, Dev client and Observer client used that isolated build.
They continued a copy of the [stopped four-car client rebuild](../fd401-pax-station-hold-rebuild/README.md),
whose native links restored on server start at exactly six blocks.

Dev normally boarded the second linked car and closed restraints in the
[mounted editor](fd401-longhold-dev-dispatch-editor.png). Observer was supplied
a pickaxe as an inventory fixture and positioned by admin teleport. Dev selected
Dispatch; a live winch query returned `LIFTING`. Observer later normally mined
the original station curve at `(900,85,65)` through the client. The anchor's
`AnchorPeerCurves` became empty, the winch entered `FAULT`, and its fault field
read `Matching track support lost`. The stopped winch NBT records
`shuttle_interrupted=OUTBOUND`, so the train had advanced out of LIFTING before
the support was removed. A live game-time query just after removal returned
**189028**. No car or winch NBT was edited.

This run **did not pass** the long-hold gate. The [server log](server.log.gz) records
both native links involving middle car `ebd70587` unlinking at 17:03:33, about
13 seconds after the post-removal query. The stopped [world](world.tar.gz) has
game time **191424**, only 2,396 ticks after that query, short of the required
4,800. Its [decoded state](stopped-state.txt) shows the `ebd70587` chassis at
Y≈85.8 and its two carriers at Y≈86.35 and 84.47, while the other three
chassis are at Y≈89.26. The saved rider assignment remains on `7b7f0154`.
The client action, absent curve and fault occurred; native consist integrity
did not survive even this shorter interval.

Source inspection after shutdown found a timing gap to investigate:
`CarrierRuntime.tick` calls `ShuttleRuntime.tick` before it detects missing
matching support and calls `VehicleOperations.failForLostSupport`. The winch
can therefore receive one pre-physics callback after support removal before
the matching-support fault is applied. The saved world does not establish
that this gap alone caused the later unlink.

The two clients together exhausted the available host RAM during the hold.
The server performed `save-all flush` and `stop`, and the clients were stopped
to release memory. The compressed [Dev](dev.log.gz) and
[Observer](observer.log.gz) logs and [post-mining view](fd401-longhold-observer-mined.png)
are retained. This checkpoint is deliberately **not** a release pass. It
exposes an outbound support-loss limitation that the earlier lift-only fixture
GameTest did not cover. The intended LIFTING long-hold acceptance still needs
a precisely timed operator replay after memory pressure eases.
