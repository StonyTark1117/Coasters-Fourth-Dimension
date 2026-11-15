# Axis coaster

Axis carries two riders in one pod on an outboard longitudinal bearing. The pod
rolls around the direction of travel. It uses Axis track and ordinary native
Create-powered Chain travel. Carrier drive rotates the pod; it does not propel
the train.

Craft Axis Track from iron, copper and cyan dye. Craft an Axis Car from two cyan
Create seats, a Mechanical Bearing, iron, Axis Track and a Coaster Cart. Both are
also in the Fourth Dimension creative tab. Lay native anchor curves with the
Axis Track item, then place the car on matching track. Allow eight blocks between
bearings; use the native Chain item on both bearings to connect at most two
identical cars while they are dock-ready. Mixed families cannot couple.

Keep the full outboard sweep clear, including supports and the walking platform:
the pod occupies design X=1.5…6.5, Y=−1…4 and Z=±1.5 relative to the rail. Its
forks sit at Z=±1.75. Curves need radius at least 16, track roll change at most
5 degrees per block, and travel at most 12 blocks per second. A matching track
label does not waive the geometry or obstruction checks.

Bind a Boarding Station to a stopped car on a clear, level straight at least
16 blocks long. The whole train must be at that dock. Right-click an open seat
normally, close restraints with the Rotation Tool, then Dispatch explicitly.
Station power requests capture; removing power does not dispatch automatically.

Use the Rotation Tool on an unoccupied track section to choose its roll mode:

- **FREE** follows native gravity and the actual rider load with baseline damping.
- **DAMP** removes relative roll energy more strongly.
- **DRIVE** follows the section's unwrapped angle profile with a bounded target
  speed of 1 radian/s and acceleration of .75 radian/s². A `360` degree endpoint
  is one full turn, distinct from `0`. Native Chain power is required.

Profile keys use `distance:degrees:slope` separated by semicolons. Cover the
whole section, match adjoining programmed endpoints, and use gentle changes.
Reversing travel retraces the same canonical distances. Existing occupied-route
programs may change modes; editing occupied track is rejected. Lift Lock holds
a settled upright relative orientation through its authored section and clears
only after the complete pod leaves it.

Capture stops travel and aligns to the nearest upright turn at the declared
dock. Wait for Ready before opening restraints and leaving the seat. If drive
power disappears, the pod damps and the train stops with restraints retained;
it does not jump upright. Restore the supply, inspect the route, and explicitly
use Resume to dock. Stop/capture at the dock before unloading. A restart retains
the actual native pose and unwrapped angle; inspect and explicitly resume.

`/fd demo axis` builds a disclosed example with a car, level dock, powered
banked loop, one driven turn and a damp-to-free return. `/fd demo axis_track`
builds the track and station for normal car placement. These commands are setup
helpers; they do not replace player construction or boarding.

Implementation and acceptance are still in progress; current evidence is in
[the Axis record](evidence/0.7/axis/README.md).
