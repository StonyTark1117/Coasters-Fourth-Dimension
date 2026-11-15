# Conventional Flying — station changeover

Build with **Flying Track** and the **Conventional Flying Car**. Four people board
one row while seated; close the body and leg restraints separately. With the
whole train held at its bound level dock and the dock Chain powered, choose
**Set prone** in the Rotation Tool editor. Wait for **PRONE** lock, then
**Dispatch**. The physical row tilts; its torso, seats, restraints, rider loads
and eye position all move together. Head presentation keeps a neutral forward
gaze and preserves independent mouse look. K changes camera comfort only.

The prone lock stays engaged through curves, banks and inversions. On return,
use **Stop** or the powered station to stop all cars at the dock. Choose
**Set seated**, wait for **SEATED** lock, open both restraint sets, then Shift
to unload. Stopping somewhere else cannot open restraints or change posture.

A loss of Chain power during changeover holds the actual intermediate pose,
keeps restraints closed and reports **INTERRUPTED_TRANSITION**. Restore power,
clear the sweep and inspect the dock; **Resume posture** continues toward the
saved target, while **Set seated** takes the same bounded path back to boarding.
Neither choice teleports the row or auto-dispatches. A restart during changeover
also requires that explicit recovery. For power loss while traveling prone,
restore the route/power and choose **Resume to dock** with the lock retained.

Craft sixteen Flying Track from iron ingots around a center light blue dye with copper
left/right. Craft one Flying Car from four Create red seats, iron ingots, one
Coasters bearing and Flying Track, using the recipe book. Place anchors and
connect them by clicking both anchors with Flying Track, and place the car normally
on that track. Right-click a Boarding Station with an empty hand to bind the nearest
car within twelve blocks; Shift-right-click rebinds it. Give every car eight blocks
between bearing centers; normal Minecraft **Chain** links one to four identical
cars. Keep each car stopped, seated and dock-ready while coupling; rebind the
station to the final consist before operation.

Reserve a level dock at least 36 blocks long with all bearing centers at least 2.5
blocks inside its endpoints. Keep the full route clear: 2.25 blocks each side,
4.5 below the graph, 1.1 above and 2.5 ahead/behind each bearing. Platforms remain
outside that sweep. Fixed forks extend farther fore/aft than the moving row.
The authored curve limit is radius 16 or greater, bank change at most 5° per block,
and speed at most 16 blocks/s. Motors and supports must clear running gear as well
as seats. Put motors at least three blocks sideways from an upright anchor and
connect the drive port with two aligned shafts; drive a banked roll span from
its upright approach.

`/fd demo flying` creates an example with a powered, player-usable level dock,
climb, full roll and return circuit. `/fd demo flying_track` creates its track
without a car, for normal placement/construction practice. `/fd demo flying_train`
creates a disclosed four-car fixture; its programmatic links are not evidence of
normal Chain use. Examples require an empty area near world origin and operator
permission. Their setup commands are optional conveniences; normal tools remain
the construction workflow.

Conventional Flying changes posture only at the station. Route posture programs
and yaw belong to New-generation Flying, a separate roadmap package.
