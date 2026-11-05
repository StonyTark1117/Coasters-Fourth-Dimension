# Bounded manual recovery boarding checkpoint

This checkpoint closes only the manual-boarding part of the FD402 world-side
operator gap. It uses the production Arrow recovery example in a normal
development client with every Fourth Dimension validation and fixture flag set
to false. It does not claim ordinary-bearing selection, physical lever input or
the complete recovery lifecycle.

## Actual client path

The client loaded the saved `FD402ManualOperator` world on a task-owned Xvfb
display. The registered production command
`/execute positioned -700 -62 512 run fd demo recovery_arrow` built the normal
Arrow route, car, bare ordinary bearing, chain drive, clutch and lever. The only
subsequent commands were operator teleports and the Rotation Tool give; no
command changed a vehicle, carrier, seat, restraint, coupling, velocity or
recovery state.

With an empty hand, the operator aimed at the rendered addon seat and used the
normal block interaction. [The third-person capture](player-seated.png) shows
the real `Dev` player seated in the Arrow carrier; this is not a fixture-created
passenger. While still mounted, normal Rotation Tool use opened the
[production vehicle editor](mounted-editor-open.png), which reported `Ready |
Restraints open`. The normal **Close restraints** button closed the screen; a
second mounted Rotation Tool use reopened it with
[the header reporting `Restraints closed`](mounted-restraints-closed.png).

The example creation message is preserved in [example-built.png](example-built.png).
The client then used **Save and Quit to Title** and **Quit Game**. The final
[title capture](title-after-save.png) and [client log](client-latest.txt) record
the clean save/disconnect/shutdown sequence.

## RAM boundary

The first Gradle-wrapped client reached the enforced 3 GiB scope limit and was
OOM-killed at exactly 3,221,225,472 bytes. That launch was not repeated. The
accepted run launched the already-prepared NeoForge development classpath
directly, retained `-Xmx1536M`, and stayed inside a separate systemd scope with
the same 3 GiB hard limit. Its observed peak was 3,097,587,712 bytes, leaving
about 118 MiB below the cap, and it exited normally. The JAR SHA-256 was
`2da14200d2e33b806dc6bb454a65fb92912ae9d87ece345adff85c8bf1e277b0`.

All graphical input and captures used the task-owned private `:210` Xvfb
display and isolated game directory
`/tmp/fd402-manual-operator.JHeue4/game-direct`. No other agent display, client,
input stream, game directory or process was used or stopped.

## Bounded conclusion

This is one Arrow car and one local player. It proves normal world-side seat
targeting, explicit manual boarding, the mounted editor path and the real
close-restraints callback in a RAM-capped client. It does not prove manual
ordinary-bearing selection, physical lever propulsion, recovery attachment or
start/brake/detach, unload, multi-car/all-family operation, multiplayer, or
rendered clearance. Those remain FD402 work.
