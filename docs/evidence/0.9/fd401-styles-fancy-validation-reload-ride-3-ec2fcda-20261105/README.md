# FD401B FANCY validation reload and occupied ride, repeat 3

This is a third complete FANCY-mode reload and ride repetition on the selected
clean `ec2fcda` JAR, SHA-256
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
The isolated `FD401StopCycle` world was loaded on the selected artifact. With
validation enabled, `/fd validation board_locked Dev` boarded Dev with
restraints closed. F3+T reloaded resources while occupied; the client logged
`FD_OPTIONAL Track Styles 1.2.0: inspected 55 decorative model entries`.
The riding trace retained a rider UUID after reload.

The station redstone block at `-356 210 -179` was cleared to start the lap and
restored shortly after motion began. Validation samples showed the chassis
moving from `(-349.5,211.1,-173.2)` to `(-319.2,211.1,-150.4)`, then later
returning to `(-349.5,211.1,-184.0)` with `capture=true`. The Rotation Tool UI
showed `arrow_controlled | Ready | Restraints closed`; its Open restraints
control was used. Dev then dismounted with Shift. The log records
`FD_BOARDING_EVENT ... action=dismount`, and
`/data get entity Dev RootVehicle` returned `Found no elements`. The world
was saved and the client exited normally.

This repeat used the validation command to board, and used a station redstone
falling edge to start motion. It does not claim normal seat boarding or UI
dispatch. Together with the two earlier selected-JAR FANCY bundles, this brings
the FANCY reload/ride count to three complete repetitions. It does not close
the broader style-transition, geometry, or shader qualification rows.

Evidence includes the full client console, a focused log extract, screenshots
of board/reload/capture/restraints-open/unload, exact launch classpath and
arguments, the selected JAR, and the saved final world. The game ran with
Java 21, a 768 MiB heap, an isolated game directory and a private Xvfb display;
the whole client launch was in a 3 GiB memory scope. The validation system
property is recorded in `clientRunVmArgs.txt`.
