# FD401C selected-JAR wired FE interruption and restore

On 2026-11-04, an Engineered-only packaged client opened the unloaded final
world from the [selected-JAR Arrow/FE run](../fd401-engineered-current-arrow-fe-reopen/README.md).
It used the same clean `8f370d0` packaged JAR, SHA-256
`cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`,
and the same Engineered 1.1.1 and validation-only Crafts & Additions wired
fixture. `requireEnergy=true` was retained. A Java 21 client with a 1 GiB heap
ran on a private task-owned Xvfb display in an active 3 GiB systemd scope.
The client and display exited successfully after a normal save and quit.

The fixture removed the middle water wheel at `(-507,142,-636)`. The
alternator at `(-507,142,-634)` then reported capacity 256 against stress 512,
down from capacity 768. Clearing the motor anchor's stored FE first exposed
5,040 FE remaining in the wire/network; clearing the anchor again left no
energy key. `/fd validation board_locked Dev` put the connected player aboard
with restraints closed. The station rear redstone was removed and the linked
controller at `(-506,142,-629)` was pulsed using disclosed `/setblock`
commands. The controller reported `Starving=1`, `Driving=1`, and a last-cart
speed of about 0.0064 blocks/s. The rider remained assigned and near the
station; a later anchor query still had no energy key.

Replacing the wheel with the normal `create:water_wheel` block restored
capacity 768 against stress 512. The retained wire replenished the anchor to
2,860 FE in the next sample. The controller automatically resumed, recorded
`PassStarved=1`, `PassReached=2.803458/20`, advanced to its programmed STOP,
and stopped driving. The real rider stayed assigned while the Arrow moved.
This was a short powered pass, not a complete circuit: the car slowed away
from station range. The rider used the vehicle screen's **Capture** action,
which brought the car below 0.001 blocks/s and showed Ready with restraints
closed. The rider then opened restraints and dismounted normally. Availability
returned to zero assignments and `RootVehicle` was absent.

The [client log](client.log.gz), [console log](console.log.gz), screenshots,
and [final saved world](final-world.tar.gz) retain the sequence. The world is
unloaded with the wheel and wired supply restored. The intervening anchor
buffer resets, water wheel removal/replacement and redstone pulses were
fixture commands; no command changed rider assignment, controller program,
car motion or the wire connection. This case establishes an occupied
depleted-FE hold, network restoration, a short automatic resume, safe manual
capture and normal unload on the selected JAR. It does not establish a full
post-restore circuit, a station capture after the short pass, or a requirement
that Engineered wait for a second operator pulse when power returns.
