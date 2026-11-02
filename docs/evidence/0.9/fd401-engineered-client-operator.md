# FD401 Engineered connected-client operator check

This check used a packaged NeoForge dedicated server and connected client with
Coasters: Engineered 1.1.1 (SHA-256
`eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d`).
The server had `requireEnergy = true`. The tested Fourth Dimension JAR was
built from release commit `59f5507` (SHA-256
`40b9bd7616e81d95237ea0af386fb03612217d4f15821118f6c222da405db894`).
The saved world was cold-opened on release commit `2058cf1` (packaged JAR
SHA-256 `cd92a677745af335291575d59d3722b6eef3a9f884f91f8d1284b8f0934139c5`).

## Client-built fixture and first boundary stop

1. A temporary validation-only command prepared a 48-block original Arrow
   track between `(-650,143,-638)` and `(-650,143,-590)`. The connected client
   selected its start anchor with the Engineered linear motor item and clicked
   the rendered curve about seven blocks in. Anchor NBT recorded
   `CoastersEngineeredMotorSpan` from `T0=0.0` to `T1=0.14583333540325194`.
2. The client linked an Engineered controller to that anchor and saved
   `Launch 20 → Stop → End` through its GUI. The controller NBT recorded
   `coastersengineered:linked_target` at `[-650,143,-638]` and the three steps.
   A temporary validation-only command supplied 40,000 FE. After removing the
   temporary commands and reopening the world with the clean release JAR, the
   motor span, controller configuration, and FE survived. The GUI is shown in
   [fd401-engineered-controller-reopen.png](fd401-engineered-controller-reopen.png).
3. The connected client placed a first Arrow car at the start endpoint. Native
   availability reported `chassis=1/1 carriers=2/2 assignments=0`. The
   endpoint guard set `restraints=1`, `stopping=1`, and fault
   `End of compatible track; capture and inspect the route`. Both validation
   boarding commands found no ready unoccupied seat, so this first car did not
   demonstrate a ride. A redstone pulse reached the controller, but
   `Driving=0` and FE stayed at 40,000.

## Interior-car powered ride and safe recovery

1. The real client selected the far anchor `(-650,143,-590)` with the Engineered
   motor item and clicked the curve near `z=-629.5`. The saved native span ran
   from `T0=1.0` to `T1=0.16666666409360903`, covering about 40 blocks toward
   the start anchor. The client placed a linked controller at
   `(-656,143,-590)` and used its GUI to save
   `Launch BACKWARD 8 → Stop → End`; its NBT recorded that sequence.
2. With energy required, a local fixture command set the far motor anchor to
   40,000 FE via `CoastersEngineeredEnergy`. The client placed a second Arrow
   car on the interior of the original curve, near `z=-601.6`. A temporary
   stone click surface beneath the rail was removed immediately after car
   placement. Native availability became `chassis=2/2 carriers=4/4
   assignments=0`.
3. `fd validation board_locked Dev` mounted the real connected client in the
   second car with restraints closed. `fd validation dispatch Dev` dispatched
   that occupied car. Native motion reported a move from about `z=-601.6` to
   `z=-592.0`, approximately 9.6 blocks toward the far endpoint, while the
   rider remained assigned. Far-anchor FE fell from 40,000 to 37,001, a
   measured consumption of 2,999 FE. The controller recorded
   `CoastersEngineeredStep:1`, `PassShort:1`, `PassReached:5.814669609…`,
   `PassTarget:8`, and `Driving:0` after the endpoint guard stopped the car.
4. The connected client used the Rotation Tool GUI to capture the stopped car
   and open its restraints. The GUI showed the endpoint fault with restraints
   closed and then open in
   [fd401-rider-editor-captured.png](fd401-rider-editor-captured.png) and
   [fd401-rider-editor-open.png](fd401-rider-editor-open.png). The client then
   Shift-dismounted; native availability returned to `assignments=0`. After
   `save-all flush`, FoundationData retained the second car with
   `boarding_ready=1`, `restraints=0`, `stopping=1`, and the endpoint fault.
   The far motor anchor retained 37,001 FE.

## Current release cold reopen

The packaged Engineered build at `2058cf1` passed `./gradlew build` with
`-PfdPackaged -PfdIntegrations=engineered -PfdValidation`. Its dedicated
server cold-opened the saved world with `requireEnergy = true` and reported
`chassis=2/2 carriers=4/4 assignments=0`. The far anchor still held the
native span `T0=1.0 → T1=0.16666666409360903` and 37,001 FE; the linked
controller still held `Launch BACKWARD 8 → Stop → End`. A fresh packaged client
joined that server and displayed the saved controller fixture in
[fd401-engineered-2058-cold-client.png](fd401-engineered-2058-cold-client.png).
The client closed, then the server saved and stopped normally. This reopen
verifies persistence of the completed ride's saved world; it did not repeat
the ride on `2058cf1`.

## Acceptance boundary

The connected-client result establishes native motor placement, controller
programming, occupied powered motion, FE consumption, client-operated capture
and unloading, and persisted state. The ride ended at the short fixture's
compatible-track boundary. It does not establish a complete circuit, graceful
automatic station stop, or an external FE cable or generator network. The
validation boarding and dispatch helpers were used to command the real client
and native vehicles; they are not ordinary operator controls. The fixture
setup commands were not committed, and this evidence PR contains no production
code.
