# FD401C selected-JAR Legacy occupied Engineered cycle

On 2026-11-04, an Engineered-only packaged client cold-opened the saved
standalone Legacy route from the [frozen-candidate Legacy run](../fd401-engineered-legacy-exact-2c5d9a7/README.md).
The runtime was the clean committed-source `8f370d0` JAR selected in
[current qualification](../../../CURRENT-QUALIFICATION.md), SHA-256
`cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`.
The source world retained the original item-placed Legacy car, native motor
span, linked controller and `Launch 8 -> STOP at 8 -> End` program.

The first cold open unexpectedly used Engineered's default
`requireEnergy=false`: the archived world contains no server config. It
completed an occupied operator cycle, but it is **not** used for any FE claim.
Its [log](initial-default-config-client.log.gz) is retained to disclose that
setup error. The game was saved and quit normally. Before the second launch,
the retained [Engineered config](coastersengineered-server.toml) with
`requireEnergy=true` was installed in both the isolated game config and world
serverconfig. The second client log records NeoForge loading the world override.
Both runs used Java 21, a private task-owned Xvfb `:217`, a 1 GiB client heap
and an active 3 GiB systemd scope; each scope and client exited successfully.
Crafts & Additions remained a validation-only dependency, not an advertised
integration.

On the required-energy run, loaded availability was `chassis=1/2,
carriers=2/4, assignments=0` because the world also holds an unloaded distant
family car. The Legacy motor anchor at `(200,142,20)` began with 100,000 FE.
`/fd validation board_locked Dev` boarded the connected player and closed
restraints. Disclosed `/setblock` commands removed the rear station signal at
`(194,142,30)` and pulsed the saved controller at `(194,142,22)`.
The occupied car moved around the circuit with `capture=false`, reaching a
sampled velocity of about 5.30 blocks/s. The controller advanced to STOP
without starvation; it reported `PassReached=7.494983/8` and `PassShort=1`.
The motor anchor fell to 94,482 FE during motion. The car returned stable in
station range at about 0.00052 blocks/s. Restoring the station signal changed
`capture` to true and left the car below 0.001 blocks/s; anchor FE was 93,460.
The [vehicle screen](fe-ready-ui.png) showed `zacspin_legacy | Ready |
Restraints closed`. The player clicked **Open restraints** and dismounted
normally. Availability returned to zero assignments and `RootVehicle` was
absent. The [required-energy client log](fe-client.log.gz), screenshots and
[final saved world](fe-final-world.tar.gz) retain the result.

This is one current-JAR, required-energy Legacy operator loop using a saved
normally built route. It does not establish a fresh current-JAR motor/car/
controller construction sequence, literal attainment of 8.0 blocks/s,
repeat reliability, or the other families. The saved world is unloaded.
