# FD401C selected-JAR four-car PAX authority cycle

On 2026-11-04, Engineered-only packaged clients opened copies of the stopped
[four-car PAX authority checkpoint](../fd401-engineered-pax-authority-2c5d9a7/README.md).
The runtime was the clean committed-source `8f370d0` qualification JAR,
SHA-256 `cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`.
The retained [world config](coastersengineered-server.toml) set
`requireEnergy=true`; both logs record NeoForge loading the override.
Engineered 1.1.1 was the optional integration and Crafts & Additions was only
a validation FE classpath. Each Java 21 client used a 1 GiB heap, a private
task-owned Xvfb `:217`, and an active 3 GiB systemd scope. Both exited
successfully through the title screen. The checkpoint retained the normally
built winch, linked `Launch 20 -> End` Engineered controller, native four-car
PAX consist, and motor anchor holding 100,000 FE.

## Route-loading correction

The first copy launched with low client render distance and no route
force-loading. It faulted during LIFTING with `Shuttle route chunk is
unavailable for physics; load the complete route`. The rider remained
assigned, the opposing controller remained at pass zero and the motor held
100,000 FE. This incomplete run is retained in
[client-route-fault.log.gz](client-route-fault.log.gz) and
[fault-world.tar.gz](fault-world.tar.gz); it is not a completed authority
cycle. The route spans about `x=650, z=17..145`, beyond the locally loaded
station chunks at the reduced render distance. The second copy started from
the original stopped checkpoint and issued the disclosed fixture command
`/forceload add 632 0 672 160` before boarding. Minecraft reported 44 marked
chunks, and endpoint queries confirmed both route-end chunks were marked.
No ride, controller or rider NBT was edited to clear the fault.

## Occupied authority cycle with the route loaded

The local fixture reported four loaded PAX chassis, eight loaded carriers
and zero assignments. The winch at `(644,144,73)` was in BOARDING with four
saved car IDs and `shuttle_cycles=2`; the controller at `(644,144,67)` had
`CoastersEngineeredPass=0`, `CoastersEngineeredDriving=0`, and target anchor
`(650,144,65)` with 100,000 FE. `/fd validation board_locked Dev` boarded the
connected player and closed restraints. A disclosed redstone block at
`(644,144,68)` continuously powered the opposing Engineered controller.
The rider used the normal vehicle screen's **Dispatch** action.

The winch proceeded from LIFTING through CAPTURING and back to BOARDING with
`shuttle_cycles=3` and no fault. This run did not sample the brief OUTBOUND
phase separately. The rider remained assigned during the cycle. At the
CAPTURING sample and again after BOARDING, the powered opposing controller
still had pass zero and was not driving, and the anchor still held exactly
100,000 FE. The motor therefore did not take drive authority in this
observed four-car cycle. The player removed the challenge redstone, reopened
the [Ready / restraints closed screen](returned-ui.png), clicked **Open
restraints**, and dismounted normally. Availability returned to zero
assignments and `RootVehicle` was absent. The
[completed client log](client-loaded-route.log.gz), screenshots and
[final unloaded world](final-world.tar.gz) retain the result.

This is one current-JAR, Engineered-only PAX winch-versus-motor operator cycle
on a saved normally built four-car consist. The separate
[four-car native callback fixture](../fd401-engineered-fourcar-motor-guard/README.md)
directly traces cancellation on each occupied chassis; this client run did
not trace per-car native callback invocations. The force-loaded route is a
disclosed environment precondition of this accepted cycle.
