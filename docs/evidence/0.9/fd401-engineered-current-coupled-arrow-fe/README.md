# FD401C selected-JAR coupled Arrow required-energy cycle

On 2026-11-04, an Engineered-only packaged client cold-opened the stopped
[coupled Arrow checkpoint](../fd401-engineered-coupled-arrow-exact-2c5d9a7/README.md).
The runtime was the clean committed-source `8f370d0` qualification JAR,
SHA-256 `cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`.
The checkpoint has a normally chain-linked two-car Arrow consist, native motor
span, linked controller, and `Launch 8 -> STOP at 8 -> End` program. The
retained [world config](coastersengineered-server.toml) set
`requireEnergy=true`; the client log records NeoForge loading that override.
Crafts & Additions was a validation-only dependency. Java 21 ran with a 1 GiB
client heap, a private task-owned Xvfb `:217`, and an active 3 GiB systemd
scope. The scope and client exited successfully after a normal save and quit.

Local availability began at `chassis=2/10, carriers=4/24, assignments=0`;
unloaded distant cars explain the larger denominators. The motor anchor at
`(800,142,20)` had no saved energy key. A disclosed fixture command supplied
100,000 FE. `/fd validation board_locked Dev` boarded the connected player
on the trailing chassis and closed restraints. A motion sample before release
showed the occupied chassis captured. Fixture `/setblock` commands cleared
the rear station signal at `(794,142,33)` and pulsed the saved linked
controller at `(794,142,22)`. The occupied chassis moved with `capture=false`
and a sampled speed of about 5.58 blocks/s. The anchor held 86,810 FE during
motion, down from the fixture's 100,000.

The controller advanced to STOP with `PassReached=7.946320/8`,
`PassShort=0`, `PassStarved=0`. The occupied chassis returned near the station
at about 0.015 blocks/s. Restoring the rear signal changed `capture` to true
and brought the motion sample below 0.001 blocks/s. The [vehicle screen](ready-ui.png)
showed `arrow_controlled | Ready | Restraints closed`. The player clicked
**Open restraints**, Shift-dismounted, and availability returned to zero
assignments; `RootVehicle` was absent. The [client log](client.log.gz),
screenshots and [final unloaded world](final-world.tar.gz) retain the sequence.

At the later capture query, the anchor's energy key was absent even though the
controller still reported `PassStarved=0`; this record does not claim a
remaining FE reserve or a second powered dispatch. The two loaded chassis
and saved native link are inherited from the source checkpoint; this client
sampled the trailing rider motion and did not independently trace both native
cart positions or link NBT throughout the ride. This is one current-JAR,
required-energy occupied operator cycle on a saved normally built consist,
not fresh current-JAR construction or repeated reliability.
