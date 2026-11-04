# FD401 Engineered Launch 20 provider pass on the isolated candidate

On 2026-11-04, an isolated combined packaged client loaded the committed
`2e12e9d` JAR, SHA-256
`1afa7066607313898a11ff628b762be86369a82a62423c3fcfce4ffd9008d62f`,
and Engineered 1.1.1 JAR, SHA-256
`eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d`.
It used `fdPackaged`, `fdValidation`, `fdIntegrations=styles,engineered`,
`fdFeNetwork`, and the retained [Engineered config](coastersengineered-server.toml)
with `requireEnergy=true`. The client saved and quit normally, then Gradle
exited 0. The [raw log](client.log.gz), [stopped world](world.tar.gz),
screenshots, and [SHA-256 inventory](SHA256SUMS) retain the run. The Fourth
Dimension artifact is archived in the [candidate realignment
retest](../fd401-candidate-pax-realignment-2e12e9d/README.md).

The run resumed the stopped [normal STOP/cold-repeat
world](../fd401-engineered-stop-cold/README.md) with its normally item-placed
Arrow car, fitted Engineered motor, linked controller at `(-506,142,-629)`,
three-water-wheel noncreative alternator, and `Launch 20 → Stop at 12 → End`
program. The old motor occupied 20 blocks (`T1≈0.55556`) of the 36-block first
straight. In this isolated copy, a command removed only the old motor-span NBT
from the anchor at `(-500,142,-630)`; the client then used the normal Linear
Motor item to select that anchor and commit a new end point at about 28 blocks
(`T1≈0.77778`). The [anchor selection](motor-start-selected.png),
[end-point aim](motor-end-aim.png), and 06:54:58 block-data response in the log
record the new span. No car, controller, program or energy state was edited.
This span retuning includes one disclosed NBT-removal fixture; the replacement
span itself was a client item action.

Dev [normally boarded](board-result.png) the saved seat and closed restraints
through the in-ride editor. Commands toggled only the station and controller
redstone inputs, as in the source operator run. Dev selected Dispatch in the
editor and pulsed the linked controller. At 06:58:21 the controller reported
`PassTarget=20`, **`PassReached=19.500490188598633`**, **`PassShort=0`**, and
`PassStarved=0`; its next step was STOP. This is an accepted Launch 20 pass by
the provider's own `PassShort` result, with stored FE remaining. It is **not**
evidence that instantaneous speed reached 20.0 blocks/s. The rider's later
position was stable at `z=-617.501507`, inside the same station range.
Station rear redstone then captured the car; the [in-ride editor](target20-captured.png)
returned Ready with restraints closed. Dev normally opened restraints and
dismounted. The 07:00:35 native availability response reported one chassis,
two carriers and **zero assignments**. The anchor still held 87,014 FE after
the run, although the water-wheel alternator could recharge it between queries;
this record does not derive an exact FE draw from the two endpoint readings.

This closes the prior `PassShort=1` observation for one occupied, normally
controlled Arrow operator loop on the exact candidate JAR. Literal 20.0
blocks/s attainment, a newly placed physical wire in this resumed world, and
all other optional-profile combinations remain separate evidence limits.
