# FD401C clean-JAR Legacy normal-seat retry

On 2026-11-05, a Java 21 combined Styles 1.2.0 + Engineered 1.1.1 client
loaded the selected clean `ec2fcda` JAR
(`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`)
and a copy of the stopped, normally built Legacy world from the [completed
helper-boarded cycle](../fd401-engineered-clean-combined-legacy-cycle/README.md).
It ran with a 1 GiB game heap on owned Xvfb `:248` in an active 3 GiB
systemd scope, saved normally and exited with `Result=success`. The display
was stopped afterward. The packaged classpath and dependency hashes are
retained here.

The connected player started unassigned. From `(197.5,145,33.5)` facing
north, F3 identified a visible `fourthdimension:legacy_seat` at native
sublevel block `(20481032,127,20491273)` with `restrained=false`.
`SelectedItem` was absent. An empty-hand right click did not mount:
`RootVehicle` remained absent and availability still reported zero
assignments. In the same client, `/fd validation board Dev` then mounted a
ready Legacy seat at `(20481032,127,20489223)` and logged
`FD_BOARDING_ASSIGN accepted=true`, with a Create seat `RootVehicle`. The
player Shift-dismounted while restraints were open, and `RootVehicle` was
again absent. Returning to the same visible targeted seat, a 200 ms held
right click also did not mount. The client then saved and quit.

The helper selected a different native seat block from the one F3 targeted,
so its success proves this vehicle had at least one ready, unoccupied seat;
it does not prove the targeted block was boardable. The earlier
[instrumented input run](../fd401-legacy-seat-input-diagnostic-20261105/README.md)
also saw no seat-method print from two targeted clicks, but used a diagnostic
JAR. Together these observations leave normal Legacy seat interaction open.
Next diagnosis should compare the targeted carrier's binding and occupancy
with the helper-selected seat and trace whether a click reaches the block-use
method; another complete operator cycle would repeat already established
motion behavior.

The [command history](commands.txt), [client log](client.log.gz), F3 target
images and [stopped world](final-world.tar.gz) retain the bounded failure.
This is not an FD401C normal-boarding pass.
