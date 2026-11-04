# FD401 bare-loop car item placement

Source `61a9884` adds right-click-in-air placement when a player aims the car
item at an open 4D rail. The existing block-click path is unchanged. This
addresses the [earlier loop check](../fd401-engineered-network-loop.md), where
temporary stone click targets under the rail did not place a car and a
validation command spawned one instead.

## Identity and retained records

| Record | SHA-256 |
| --- | --- |
| Packaged Fourth Dimension `0.9.0-dev.27` JAR | `2c336eef8c7701fbc21cedcc9ac2c204c809a2394576cd91015c8bb09bc8f04f` |
| Engineered 1.1.1 JAR | `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d` |
| [Client log](client.log) | `c1f14b8ce5d3cdba7a53aa3db69fd1351d111c6d2ad9ce9b26646de4ea46c2e6` |
| [Native log](native.log) | `c54386b1541d8472cc453f9881e5830a998955d6fb2f291dbcd8d3af143e4eb7` |
| [Saved world](world.tar.gz) | `ef4beeff60a3d70596b4565e00e9d3ee344df80fe55db44fea4cdfbab470f1ac` |
| [Car view](car.png) | `3411d1126d1d901b762aa1a3b5d69a37567d4c720fa24b687b054b7c1ca49e22` |
| [Station editor](station.png) | `be718889078acf7a2c15f260bba89c6e85460f05c1fc4d77efce3a4e905d1f60` |

The packaged client command was `./gradlew runClient --offline --no-daemon
--console=plain -PfdPackaged -PfdValidation -PfdIntegrations=engineered`, with
Java 21 and an isolated Xvfb/llvmpipe display. Gradle exited 0 after the
client saved and quit. The world archive was made after that shutdown and
omits only `session.lock`.

## Observed operator actions

1. A new Creative world was created. The validation command
   `/fd validation engineered_loop` built an original eight-edge Arrow loop
   without a native chain drive or a preplaced car. The client received one
   Arrow car item through `/give`; no vehicle allocation command was used.
2. Initial air clicks missed the rail while the player fell below it. A
   validation trace recorded `Aim at matching 4D track to place this car`.
   The player gravity attribute was set to zero solely to hold the camera at
   the track height. With the car item selected and the crosshair aimed west
   at the first straight's interior, a normal right click placed the car.
   The [rendered view](car.png) shows its chassis and wheels over the rail.
3. `/fd validation availability` then reported `chassis=1/1 carriers=2/2
   assignments=0`. The player selected an empty hotbar slot and right-clicked
   the station normally. The [editor](station.png) displayed
   `arrow_controlled | Ready | Restraints open`. Client telemetry changed from
   `No bound 4D vehicle` to `available=1`, `cars=1`, `captured=1`,
   `boarding=1`, `capacity=4`, and `phase=BOARDING` for that station.

The native `normalItemPlacesCarOnBareEngineeredLoop` case also used the normal
server item-use dispatcher with a survival fake player. It recorded
`itemConsumed=true` and a newly allocated chassis. The GameTest server was
intentionally stopped after the Engineered batches, so the retained native log
is a focused check rather than a full-suite result.

This closes the ordinary item-placement gap on this bare-loop fixture. It does
not establish a motor/controller setup, redstone dispatch/capture, an occupied
ride, a noncreative FE producer, a wired interruption/restart cycle, or a cold
reopen of this new world. Those FD401 operator and FE actions remain open.
