# FD401 exact-candidate PAX authority replay

On 2026-11-04, an Engineered-only packaged client cold-opened the stopped
four-car PAX authority world on the frozen `2c5d9a7` candidate. The retained
JAR has SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
matching the [final candidate artifact](../fd401-final-candidate-2c5d9a7/README.md).
The run used a fresh Xvfb display (`:172`), an isolated source export, isolated
build output, a new game directory and a private copy of the saved world. It did
not observe or control another client session.

The launch was:

```text
./gradlew runClient --offline --no-daemon --console=plain \
  --project-cache-dir /tmp/fd401-candidate-2c5d9a7-V9tBRB/project-cache \
  -PfdPackaged \
  -PfdBuildDirectory=/tmp/fd401-candidate-2c5d9a7-V9tBRB/build-output \
  -PfdClientGameDirectory=/tmp/fd401-pax-exact-2c5d9a7-aED19a/game \
  -PfdValidation -PfdIntegrations=engineered -PfdFeNetwork \
  -PfdWorld=FD401FamilyBare
```

Engineered 1.1.1 was the advertised optional integration. Create Crafts &
Additions 1.6.0 supplied only the validation FE classpath selected by
`-PfdFeNetwork`; it is not an advertised integration. The retained
[configuration](coastersengineered-server.toml) has `requireEnergy=true`.

## Occupied authority cycle

The cold-opened fixture retained a four-car PAX shuttle, its 128 RPM winch at
`(644,144,73)`, an Engineered motor anchor at `(650,144,65)` holding 100,000 FE,
and a linked `Launch 20 -> End` controller at `(644,144,67)`. Those installed
components come from the earlier normal-construction record; this replay does
not make a new placement or linking claim.

The player targeted the PAX seat at normal interaction range and boarded it
with an empty hand. `FD_BOARDING_ASSIGN` accepted seat offset `(0,-1,0)`, and
client diagnostics placed the player UUID on the PAX carrier. An accidental
creative-inventory interaction while positioning had overwritten the saved
rotation-tool slot, so a disclosed `/give` restored that one normal tool. The
player then opened the in-ride editor, clicked **Close restraints**, placed a
disclosed redstone challenge at `(644,144,68)`, reopened the editor and clicked
**Dispatch**.

During the occupied outbound pass, [motion](outbound-motion.png) reported the
PAX chassis moving at about `(0,-7.805,13.85)` blocks/s with `capture=false`.
The [winch query](outbound-winch.png) reported `shuttle_phase=OUTBOUND`, four
cars and `shuttle_cycles=1`. Despite the continuously powered opposing
controller, the [controller query](outbound-controller.png) retained
`CoastersEngineeredDriving=0`, `CoastersEngineeredPass=0` and
`CoastersEngineeredPassReached=0.0`; [anchor energy](outbound-energy.png)
remained exactly 100,000 FE.

The shuttle returned without intervention. The [captured winch](captured-winch.png)
reported `shuttle_phase=BOARDING` and `shuttle_cycles=2`. A second
[motion query](captured-motion.png) placed the same chassis back at about
`(650.5,145.1,73.5)` with `capture=true` and near-zero velocity. The later
[controller](captured-controller.png) still reported pass zero and not driving,
and [anchor energy](captured-energy.png) was still 100,000 FE. Thus the
candidate-exact PAX winch retained exclusive drive authority across one complete
occupied cycle while the Engineered controller was powered.

The player removed the challenge, reopened the in-ride editor in its
[Ready / restraints closed](returned-ready-closed.png) state, clicked **Open
restraints**, and Shift-dismounted normally. The final
[availability](availability.png) query reported `assignments=0`. Save and Quit
stopped the integrated server with `All dimensions are saved`; Quit Game ended
the client with `BUILD SUCCESSFUL`. [client.log.gz](client.log.gz) retains the
exact-JAR load, normal UI/boarding transitions, phase/controller/energy queries,
unload, save and shutdown. [world.tar.gz](world.tar.gz) preserves the stopped,
unloaded result.

This closes the exact-candidate PAX authority-cycle boundary. It does not trace
an Engineered callback on every car during this four-car traversal. The separate
[two-car callback fixture](../fd401-engineered-pax-callback-663aa12/README.md)
remains the direct callback/cancellation evidence, and the predecessor
[normal PAX authority session](../fd401-engineered-pax-authority/README.md)
remains the construction/linking reference.
