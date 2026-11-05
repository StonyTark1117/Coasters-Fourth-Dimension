# FD401C clean-JAR combined Legacy cycle

On 2026-11-05, a Java 21 client loaded the selected clean `ec2fcda` JAR
(`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`)
with Track Styles 1.2.0 and Engineered 1.1.1. The JAR was built from an isolated
committed-source export; `createClientLaunchScript` used `-PfdPackaged
-PfdValidation -PfdIntegrations=styles,engineered -PfdFeNetwork
-PfdWorld=FD401FamilyBare -PfdClientMaxHeap=1536M` with `-x jar -x compileJava`,
so the launch did not replace the selected JAR. The retained launch script used
Java 21 and `-Xmx1024M`. The client ran on task-owned Xvfb `:243` inside an active
3 GiB systemd scope and exited `Result=success`; that display was stopped after
exit. The retained classpath and dependency hashes identify its runtime inputs.

The world copy came from the earlier, normally constructed Engineered-only Legacy
route in `fd401-engineered-current-legacy-fe`. It was not newly constructed in
this combined-profile run. `requireEnergy=true`; the anchor began at 93,460 FE.
The connected player Dev boarded and closed restraints using the disclosed
`/fd validation board_locked Dev` helper because the previous clean-JAR normal
seat-click attempt did not mount. This run therefore does **not** pass normal
seat boarding. Crafts & Additions was present only as a disclosed FE fixture;
the saved anchor FE had been initialized by a fixture command in the earlier run.

The operator cleared the rear station signal, pulsed the saved controller, and
restored the rear signal after the car reached STOP. The controller advanced to
step 1 (`STOP`), reported `PassStarved=0b`, and reached 7.9302735 of the literal
8 blocks/s target. Anchor FE fell to 86,629. With the rear signal restored,
`/fd validation motion Dev` reported `capture=true`, near-zero velocity and a
retained rider assignment. Dev opened the rotation-tool vehicle editor, clicked
**Open restraints**, then held Shift to dismount. `RootVehicle` was absent and
availability returned `assignments=0`. The client saved to title and quit
normally. Screenshots, command history, logs and the stopped final world are
retained here; `SHA256SUMS` and `dependencies.sha256` cover the bundle and
classpath files.

This is one bounded combined-profile Legacy launch, STOP, FE draw, capture,
UI release and unload on the clean selected JAR. Normal seat boarding, fresh
combined-profile construction, other-family client operator cycles, and full
profile qualification remain open. The 8 blocks/s literal target was not reached.
