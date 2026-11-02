# FD401 Engineered connected-client operator check

This check used a packaged NeoForge dedicated server and connected client with
Coasters: Engineered 1.1.1 (`eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d`).
The server had `requireEnergy = true`. The tested Fourth Dimension JAR was
built from release commit `59f5507` and had SHA-256
`40b9bd7616e81d95237ea0af386fb03612217d4f15821118f6c222da405db894`.

## Observed

1. A temporary validation-only command prepared a 48-block original Arrow
   track. The connected client selected its start anchor with the Engineered
   linear motor item, then clicked the rendered curve about seven blocks in.
   Server anchor NBT recorded `CoastersEngineeredMotorSpan` from `T0=0.0` to
   `T1=0.14583333540325194`.
2. The connected client targeted that anchor with the Engineered controller
   item, placed the controller on a separate support, and saved
   `Launch 20 → Stop → End` through its GUI. Controller NBT recorded
   `coastersengineered:linked_target` at `[-650, 143, -638]` and the three
   sequence steps.
3. A temporary validation-only command put 40,000 FE into the motor anchor.
   The client and server exited, both temporary commands were removed, and the
   saved world reopened under the clean release JAR. The motor span, controller
   link and sequence, and `CoastersEngineeredEnergy: 40000` all survived.
   The reopened GUI is shown in
   [fd401-engineered-controller-reopen.png](fd401-engineered-controller-reopen.png).
4. The connected client placed an Arrow car on the original track. Native
   availability reported `chassis=1/1 carriers=2/2 assignments=0`.

## Open acceptance

The clean release replay did not complete a ride. Both
`fd validation board Dev` and `fd validation board_locked Dev` returned
`No ready unoccupied fourth-dimension carrier seat was found` after car
placement. A redstone pulse reached the controller (`PrevPowered=1`), while
`Driving=0` and anchor FE remained at 40,000. No FE cable or generator network
was exercised. These observations establish client placement, controller
configuration, and cold persistence; they do not establish motor-driven motion,
FE consumption, boarding, or a connected-client ride.

The fixture setup commands were not committed. This evidence change contains
only this note and the clean-release GUI screenshot.
