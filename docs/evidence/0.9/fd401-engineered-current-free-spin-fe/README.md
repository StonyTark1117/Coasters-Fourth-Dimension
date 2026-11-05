# FD401C selected-JAR Free Spin required-energy cycle

On 2026-11-04, an Engineered-only packaged client cold-opened the saved Free
Spin route from the [family operator checkpoint](../fd401-engineered-family-operator/README.md).
The runtime JAR was the clean committed-source `8f370d0` qualification JAR,
SHA-256 `cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`.
The saved world retained the normally item-placed car, native motor span,
linked controller, and `Launch 8 -> STOP at 8 -> End` program. The retained
[world config](coastersengineered-server.toml) set `requireEnergy=true`; the
client log records NeoForge loading that override. Crafts & Additions was a
validation-only dependency, not an advertised integration. Java 21 ran with
a 1 GiB client heap, a private task-owned Xvfb `:217`, and an active 3 GiB
systemd scope. The scope and client exited successfully after a normal save
and quit.

Local availability began at `chassis=1/3, carriers=4/8, assignments=0`;
unloaded distant cars explain the larger denominators. A disclosed fixture
command set the motor anchor at `(350,142,20)` to 100,000 FE. The connected
player boarded the local Free Spin carrier with `/fd validation board_locked
Dev`, which closed restraints. Fixture `/setblock` commands released the rear
station signal at `(344,142,33)` and pulsed the saved linked controller at
`(344,142,22)`. The occupied car moved around the circuit at a sampled
6.139 blocks/s with `capture=false`. The controller advanced to STOP with
`PassReached=7.928360/8`, `PassShort=0`, `PassStarved=0`. The anchor held
86,639 FE during motion, down from the fixture's 100,000.

The car returned stable in station range at about 0.00017 blocks/s. Restoring
the rear signal changed `capture` to true and left it below 0.001 blocks/s;
anchor FE was 86,421. The [vehicle screen](ready-ui.png) showed
`sns_free_spin | Ready | Restraints closed`. The player clicked **Open
restraints**, Shift-dismounted, and availability returned to zero assignments.
`RootVehicle` was absent. The [client log](client.log.gz), screenshots and
[final unloaded world](final-world.tar.gz) retain the sequence.

This is one current-JAR, required-energy Free Spin operator loop on a saved
normally built route. It does not establish fresh current-JAR construction,
repeat reliability, or the other family/profile cases.
