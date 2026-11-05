# FD401C current-JAR fresh Arrow motor, car and controller construction

On 2026-11-04, a Java 21 Engineered-only packaged client ran the selected
committed-source `8f370d0` JAR, SHA-256
`cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`.
The [launch script](run-client.sh) uses the classpath and VM arguments generated
from that exact packaged JAR in the isolated Arrow build. It launched with a
1 GiB game heap inside a verified active `MemoryMax=3G` systemd scope on private
Xvfb `:217`; scope exit was `Result=success`. The client saved the world, returned
to title and quit normally, then its owned display was stopped. The add-on and
fixture dependency hashes are in [dependencies.sha256](dependencies.sha256).

The isolated world began as a copy of the stopped
[`fd401-engineered-exact-fe-2c5d9a7`](../fd401-engineered-exact-fe-2c5d9a7/README.md)
Arrow world. The player teleported to an empty area and used
`/fd validation engineered_loop` to build a **new empty original Arrow circuit**
at `(100,145,112)`. This fixture placed track/station but no car, motor or
controller. The older world car remained saved outside render distance; later
availability `chassis=1/2 carriers=2/4` means the newly built car was loaded
and the older one was not. The player temporarily set gravity to zero to hold
the item-placement camera at track height and restored it to `0.08` before quit.

With the actual Engineered motor item, the player clicked the new first-straight
anchor `(100,145,120)` and a rail point near `z=140`. The anchor then recorded a
native `CoastersEngineeredMotorSpan` from `T0=0` to `T1=0.5138889251`. A normal
right click with a `fourthdimension:arrow_car` item placed the visible chassis
and two carriers on that new route. Empty-hand station click at `(94,145,132)`
bound it and opened `arrow_controlled | Ready | Restraints open`. The player
selected the fitted motor with the Engineered controller item and placed the
controller normally on a fixture-provided stone support at `(90,145,121)`.
Its saved `linked_target` pointed to the newly fitted anchor `(100,145,120)`.
Through the provider GUI, the player set and confirmed
`Launch 20 → Stop at 12 → End`; a block query records that sequence.

For the ride, a disclosed fixture command filled the new anchor to 100,000 FE.
Two normal seat-click attempts did not establish a mount, so validation
`board_locked Dev` boarded the newly loaded car and closed restraints. The
in-ride Rotation Tool showed Ready and closed restraints; the player clicked
**Dispatch**. A disclosed adjacent redstone block pulsed the newly placed
controller. The connected rider's live chassis sample reached about 19.1
blocks/s, while the controller advanced to STOP with `PassReached=19.596214/20`,
`PassShort=0` and `PassStarved=0`. Anchor FE fell to 37,719. The car then held
near its new station at about 0.000012 blocks/s. Powering the station rear
input engaged `capture=true` below 0.001 blocks/s, and the editor returned
Ready with closed restraints. Dev clicked **Open restraints**, Shift-dismounted,
and availability returned zero assignments with no `RootVehicle`.

The [client log](client.log.gz), [console log](console.log.gz), screenshots,
[saved world](final-world.tar.gz) and [SHA256SUMS](SHA256SUMS) retain the bounded
record. This proves current-JAR normal item fitting/placement/linking and GUI
programming for one Arrow construction, followed by an occupied controller
cycle and normal UI unload. The validation-built track, direct FE fill and
validation boarding remain disclosed fixtures. It does not prove fresh
construction in every family, a normal seat click on this route, physical FE
delivery on this new motor, or a combined-profile construction pass. The
separate current-JAR wired FE run covers physical transfer on a saved route.
