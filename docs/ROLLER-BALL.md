# Roller Ball coaster

Roller Ball carries four riders on one transverse axle above a dedicated narrow
track. Both seat pairs belong to the same physical carrier. Native gravity and
inertia produce the tumble; the lift guide keeps the seats upright until the
whole car enters the free descent.

Craft Roller Ball Track from iron, copper and lime dye. The car uses four lime
Create seats, a Mechanical Bearing, iron, Roller Ball Track and a native Coaster
Cart. Craft the Lift Control from iron, redstone and a clock. These items are in
the Fourth Dimension creative tab as well.

Build a fixed closed route in a vertical plane parallel to world YZ. Keep every
curve at radius eight or greater, connect tangents smoothly and leave at least
seven blocks between non-adjacent track centerlines. The whole route needs one
unbranched sequence: level dock, lift, rounded summit, descending zigzags and
return to the dock. Keep the dock level for at least eight blocks. Roller Ball
rejects other track families, moving track and all second-car or recovery links.

Use the Lift Control on a reachable solid anchor/support surface within two
blocks of an empty track section. Each use cycles Dock, Lift, Summit, Free and
Return; sneaking cycles backwards. The native anchor overlay may appear when
the ordinary block ray misses, so target an actual nearby block. Remove any
temporary support that enters the vehicle sweep before operating.

Power the lift and summit with native Create Chain spans. Chain supplies travel;
the passenger guide only rotates the carrier. The lift stays below two blocks
per second, transitions/return below three and free descent below eight. The
passive brake removes excess travel speed without applying a spin impulse.

Leave both outboard rotating pods clear. Their axle centers are 3.25 blocks
either side of the rail and two blocks above it, with a reserved 2.5-block radius
in the vertical route plane. The full reservation is 4.5 blocks wide on each
side, 4.75 above the rail, .75 below and 2.75 along travel. Place two boarding
platforms outside that sweep and within normal seat interaction reach.

Place one car on matching track. Bind a Boarding Station to the stopped car on
the level dock, then wait for Ready. Board an open seat normally, close restraints
with the Rotation Tool and Dispatch explicitly. Lift guidance hands over to a
physical latch on the straight up-leg, guides through the summit and releases
only inside the validated free section. Return damping prepares the carrier for
stopped upright capture at its original dock. The axle support shows green while
latched, yellow during added damping and metal gray when free. Open restraints
only after Ready.

Power loss retains closed restraints and the measured position. An engaged lift
latch stays engaged; an interrupted guide brakes to a held angle. Restore power,
inspect the route and use Resume explicitly. Removing station power does not
dispatch. A restart likewise retains the actual carrier state and requires
inspection and Resume. Support repair must restore the original matching curve,
then use Repair matching track and Realign matching track before Resume; it
does not move the car to a later route section.

`/fd demo roller_ball` builds the original example with a car and powered route.
`/fd demo roller_ball_track` builds its track and station for normal car placement.
Choose an empty area near the world center with space for the tall route. These
commands are disclosed setup helpers, not proof of player construction.

The [mechanics handoff](evidence/0.7/roller-ball/A-handoff.md) and
[construction record](evidence/0.7/roller-ball/BC-acceptance.md) record the
accepted behavior, disclosed example setup and current persistence status.
