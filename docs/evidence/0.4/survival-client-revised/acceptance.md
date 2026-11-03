# FD403 Revised survival-client checkpoint

On 2026-11-02, a packaged standalone client exercised Revised Free-Rotor
crafting and construction in `FD403RevisedClient`, a copy of the earlier
survival `FD403FamilyClient` world. The copied world retained its Arrow, Legacy
and Free Spin examples; the Revised plot was separate at x=260–272. Commands
supplied only upstream ingredients, native Coasters anchors, stone supports,
position changes and daytime. No Fourth Dimension item was command-granted.

The connected player took 16 [Revised track items](track-recipe.png) from the
normal crafting-table result slot, using six iron ingots, two copper ingots and
purple dye. The [Revised car recipe](car-recipe.png) used two Create red seats,
iron bars, four iron ingots, one of those crafted tracks and a native Coasters
cart. The player took its result. The client showed a recipe-unlock toast after
the track craft. Ingredient-triggered recipe-book discovery was not separately
verified.

The current-source client loaded the packaged JAR built from source commit
`6dd7a8828f6a67e64bf3f634224b0621879803d5`, SHA-256
`dc60cbdefe35cf2737573e57b1509f790cf82342bea6a81764a49066b2b72fd6`.
The connected player placed two native anchors by ordinary right-click on
upstream stone supports, then joined them with the crafted track. The
[rendered section](track-built.png) used three items, reducing the stack from
15 to 12. A first car placement attempt was correctly refused by the
[wing/rider clearance guard](clearance-reject.png) while a temporary approach
platform occupied the sweep. After removing that obstruction, the player used
the crafted car on the matching section; the item was consumed and the client
displayed [Carrier capture engaged](car-placed.png).

The integrated server saved normally, and the client exited through the title
screen (`BUILD SUCCESSFUL in 7m 54s`). The construction client log has
SHA-256 `92fe27c3f7f92ca3fd8bfee775f78ad1bf770b62aa411382449a9ad2207f5331`;
[selected events](events.txt) include packaged JAR task ordering, four Revised
carrier status lines, and the completed save. The saved
`fourthdimension_foundation.dat` has SHA-256
`dc18236d525fb78d897685c9ad2a61908656b4e1a0406618bc1c0f3a99ab946c`.
The initial inspected schema-6 save contained one valid 12-block Revised rail
and its matching original vehicle with four captured, empty native carriers,
open restraints, boarding readiness and no fault. The
[saved inventory](saved-inventory.json) retains 12 Revised track items and no
Revised car or native anchor item.

The same packaged standalone client reopened this survival world for a connected
boarding and operator pass. A temporary event trace established that the first
boarding-platform clicks were outside the default 4.5-block interaction range:
they produced no block-use event. Raising the range to 100 and 10 blocks allowed
the same seat to be clicked, confirming the seat and readiness path. Those
diagnostic mounts were dismounted. The player reset the attribute to its
default **4.5**, stood on a closer upstream-stone step at x=259, y=-40,
z=254, and right-clicked the [targeted Revised seat](seat-normal-range.png)
with an empty hand. The [normal-range mount](seat-boarded.png) changed the
Revised carrier status from zero to one occupant. No mod item was granted;
the temporary trace added logs only and was removed from source afterward.

While mounted, the crafted Rotation Tool opened the
[operator editor](operator-ready-open.png), showing `Ready | Restraints open`.
The player closed restraints and reopened the
[closed state](operator-ready-closed.png), dispatched, and observed
[`Aligning / moving`](operator-dispatched.png). The player requested capture,
waited for [`Ready | Restraints closed`](operator-recaptured.png), opened
restraints and dismounted with held Shift. The subsequent
[/fd status](unloaded-status.png) showed all four Revised carriers captured
with zero occupants. The [selected event trace](boarding-events.txt) records
the restored 4.5 range before the client/server seat-use calls and a normal
integrated-server save and exit (`BUILD SUCCESSFUL in 4m 43s`). Its complete
client log SHA-256 is
`a8c6ce3b09db45b42fc1e2af52618e06c8a94c093317cbcda1f5d0d172eab353`.
The final `fourthdimension_foundation.dat` SHA-256 is
`8fafe30a7737632f675323347d9cbbe7d9403396f1edadef968c17757db8e9ca`.
The [final inspected schema-6 save](foundation-save.json) retains the valid
Revised rail and four original carrier identities, now with vehicle revision
four, `stopping=true`, `restraints=false`, `boarding_ready=true`, empty rider
maps and no fault. The saved inventory is unchanged. The nearest temporary
stone step was removed after unloading.

The first capped client session used an older packaged JAR
`39709f1e35870b7b5c5865e6c19920f6f8430d61cd8179dfe95d56428474a94c`
because `runClient -PfdPackaged` previously had no `jar` task dependency. It
completed the crafting and saved normally; its log SHA-256 is
`8775d859d9d65acd7a13758d5e2d2bf2b9bcd6f169d2926b1b847e257b98efe3`.
An earlier startup was stopped before world interaction when its default heap
limit appeared as 15 GiB. Both completed client sessions used `JAVA_TOOL_OPTIONS=-Xmx4G`
and `ALSOFT_DRIVERS=null`. The build now makes packaged client, observer and
server runs depend on `jar`; a dry run showed `jar` before all three run tasks,
and an explicit `jar -PfdPackaged` rebuilt the current artifact before the
second client session.

This checkpoint proves connected survival crafting, matching track and car
placement, clearance rejection, normal-range seat boarding, operator state
changes, unloading and a clean save/reopen cycle. The short flat section had no
propulsion; dispatch changed the operating state, but no powered travel or
measured lower-hinge motion is claimed. The complete five-family teaching gate
and PAX connected-client exercise remain open. FD403 remains in progress.
