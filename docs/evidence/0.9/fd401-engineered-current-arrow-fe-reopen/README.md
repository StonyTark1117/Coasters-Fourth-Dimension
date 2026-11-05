# FD401C current-artifact Arrow motor, FE and occupied reopen

On 2026-11-04, an Engineered-only packaged client used the clean committed-source
`8f370d0` JAR selected in [current qualification](../../../CURRENT-QUALIFICATION.md).
The runtime JAR SHA-256 was
`cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`,
identical to the retained qualification JAR. This result covers one occupied
Arrow consist in an existing wired world, not the FD401C five-family matrix.

The client used Java 21, a private task-owned Xvfb `:217`, an isolated game
directory, `-PfdValidation -PfdIntegrations=engineered -PfdFeNetwork`, and a
1 GiB game heap. Each direct packaged Java launch ran in an active user
`systemd-run --scope -p MemoryMax=3G` scope. Both scopes exited successfully;
the client exited through the title screen and the owned Xvfb was stopped.
The selected JAR was built with `createClientLaunchScript` from a clean
`git archive` export. The generated classpath and VM arguments were used by
the direct launch. The world came from the frozen
[wired FE checkpoint](../fd401-engineered-exact-fe-2c5d9a7/README.md),
with its `requireEnergy=true` world configuration. Engineered 1.1.1 is the
optional integration; Crafts & Additions is only the disclosed FE supply
fixture. Relevant dependency SHA-256 values: Engineered
`eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d`,
Crafts & Additions
`41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9`,
Sable `da6c3b66238586603d1dcaa2afb012d36815fbce0a2d5938fbb2936701d42279`.

## Observed operator sequence

The saved controller at `(-506,142,-629)` remained linked to the motor anchor
at `(-500,142,-630)` and programmed `Launch 20 -> STOP at 12 -> End`.
`/fd validation availability` initially returned one chassis, two carriers
and zero rider assignments. `/fd validation board_locked Dev` boarded the
real connected player and closed the restraints. The operator used disclosed
`/setblock` commands to clear the rear station signal, pulse the controller,
and restore that signal for capture. The occupied Arrow moved at about
`(12.57, 0, 12.63)` blocks/s in the sampled motion command. The motor reached
`PassReached=19.396589/20`, reported `PassStarved=0`, and advanced to STOP.
Anchor FE fell from 100,000 to 75,211 during motion, then recovered to 82,607
through the saved wire. At the station the motion sample was about 0.01463
blocks/s before capture and below 0.001 blocks/s after capture. The vehicle
screen showed Ready and restraints closed. The player clicked **Open
restraints** and dismounted normally; assignments returned to zero and
`RootVehicle` was absent. The player then boarded again, and the game was
saved and quit with the rider still aboard. The
[occupied save](pre-reopen-occupied-world.tar.gz) is retained.

On the cold reopen of that occupied save, `FD_BOARDING_RESTORE` queued and
mounted Dev. Availability returned one assignment, the vehicle screen still
showed restraints closed, anchor FE was 100,000, and controller
`CoastersEngineeredDriving` was `0b`. It did not launch on reopen. The player
opened restraints through the screen and dismounted; assignments again
returned to zero and `RootVehicle` was absent. The
[final save](final-unloaded-world.tar.gz) retains the unloaded state. The
[first client log](pre-reopen-client.log.gz),
[reopen log](post-reopen-client.log.gz), screenshots and `SHA256SUMS` retain
the observations. Transient Sable missing-sub-level movement messages occurred
during startup; the occupied rider restoration and subsequent unload succeeded.

This is a bounded current-JAR Arrow/FE/cold-reopen pass. It does not cover
normal wire construction, FE starvation and restore, other families,
coupled Arrow, combined Styles + Engineered, or a dedicated multiplayer server.
