# FD401C clean-JAR combined PAX authority cycle

On 2026-11-05, a Java 21 client loaded the selected clean `ec2fcda` JAR
(`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`)
with Track Styles 1.2.0 and Engineered 1.1.1. The retained direct launch
script used the isolated packaged classpath, `-Xmx1024M`, task-owned Xvfb
`:248`, and an active 3 GiB systemd scope. The client saved and exited with
`Result=success`; the owned display was then stopped. The classpath and
dependency hashes identify the runtime inputs.

The world copy came from the stopped, unloaded four-car PAX authority result
in `fd401-engineered-current-pax-authority`, which was saved on the preceding
clean JAR. Its normally item-fitted motor and linked controller, four-car
native train, winch and route were not rebuilt here. The selected JAR changes
PAX bind restoration, so this cold-opened client is a separate runtime
observation on that changed path. The world retained force-loaded route
endpoints; `/forceload query` confirmed chunks at `(650,17)` and `(650,145)`
were marked. `requireEnergy=true`; Crafts & Additions remained a disclosed
FE fixture dependency.

At startup, availability reported four loaded PAX chassis, eight carriers
and zero assignments. The winch at `(644,144,73)` was in BOARDING with four
car IDs and `shuttle_cycles=3`. The opposing Engineered controller at
`(644,144,67)` had pass zero and was not driving, and the motor anchor at
`(650,144,65)` held 100,000 FE. After clearing one hotbar slot by a disclosed
`/item replace` fixture command, Dev targeted a physical `pax_seat` in F3
and made one empty-hand right click. `FD_BOARDING_EVENT` and
`FD_BOARDING_ASSIGN accepted=true` recorded a normal mount. `RootVehicle`
identified the Create seat entity. No boarding helper was used.

Dev used the in-ride UI to close restraints. A disclosed redstone block
continuously powered the opposing controller at `(644,144,68)`, and Dev used
the UI **Dispatch** action. The winch was sampled in LIFTING and RETURNING,
then returned to BOARDING with `shuttle_cycles=4`, no fault and the rider
assignment retained. The brief OUTBOUND phase was not separately sampled.
At the LIFTING sample and after BOARDING, the opposing controller still had
pass zero and the anchor still held 100,000 FE. This observes exclusive
winch authority through the completed cycle, without per-car callback traces.
`/fd validation motion Dev` reported station `capture=true` on return.

Dev removed the challenge block, reopened the Ready / restraints-closed
editor, clicked **Open restraints**, and held Shift to dismount. The server
logged normal assignment clearing; `RootVehicle` was absent and availability
returned `assignments=0`. The client saved to title and quit normally.
Commands, screenshots, logs and the stopped final world are retained here
with bundle and classpath hashes.

This is one bounded combined-profile PAX normal-board, dispatch, four-car
winch cycle, capture and unload on the selected clean JAR. It does not prove
new PAX construction on this artifact, a per-car motor callback trace,
OUTBOUND interruption or active-phase cold restart.
