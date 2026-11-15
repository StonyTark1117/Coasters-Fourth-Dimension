# S1 conventional Spinning

S1 is one four-seat car on its dedicated orange Spinning Track. A physical vertical
bearing lets the carrier turn freely; rider positions change the response to curves
and acceleration. An evenly loaded car on straight level track may barely spin.
This is the conventional non-inverting variant: radius at least 12 blocks, bank and
slope at most 60 degrees, travel at most 10 blocks/s. It cannot couple to another car.

Craft Spinning Track from iron, copper and orange dye. Craft the car from three
Create orange seats, two Mechanical Bearings, iron, Spinning Track and a native
Coasters Cart. Use the track item on two Coasters anchors. Shape the curve with
native handles, then use Rebind geometry in the Rotation Tool before placing a car.
The car reserves 2.25 blocks to each side and fore/aft, 3.65 above the graph center,
and .95 below. Keep the full rotating rider envelope clear.

1. Add normal Create-powered Chain to the drive/lift spans. Keep the native speed
   within the 10 blocks/s S1 bound; 64 RPM gives a slow demonstration speed.
2. Before placing the car, use the Rotation Tool on a designated track section and
   enable **Lift lock** if the carrier should retain its chassis-relative angle there.
   A powered Chain must cover that section. Entry requires slow spin; start the
   locked section at a captured dock where possible. The lock releases after the
   whole car has left, including its 2.25-block rear reservation.
3. Place the car on matching track, place a Boarding Station within twelve blocks
   of the level stopping point, and use the station to bind it. **Capture** brakes
   travel first, damps spin, then slowly aligns the carrier. Wait for Ready.
4. Use a seat to board, use the Rotation Tool/controller to **Close restraints**, then
   **Dispatch**. The rider and camera follow the actual native carrier; look remains
   independent. Shift cannot unload while restrained.
5. Put the dock at the lift entrance and Capture the returning car before it enters
   the locked section. The example dock is close enough to capture from its final
   curve. If unsettled spin triggers a safe lift-entry stop, use Capture there to
   align before dispatching again. When angle, spin and travel are
   settled for five server ticks, **Open restraints**, close the editor, then Shift
   to unload. Rear redstone requests capture. Removing power never dispatches S1.

Loss of Chain power, route, body or dock stops travel and retains riders. Restore the
named condition, then explicitly Capture/Dispatch at the dock. If stranded away
from the dock on repaired valid track, **Resume to dock** explicitly authorizes
travel with closed restraints; Capture on return. No automatic restart occurs.
The ordinary recovery-bearing adapter is not certified for this family. A removed
native body leaves the surviving pose and fault retained for inspection.

`/fd demo spinning` supplies an original playable loop with powered Chain, a level
lift-lock section, banked curves, a car and its dock. `/fd demo spinning_track`
supplies the route for normal car placement and station binding. These commands are
optional Creative examples; generated outputs do not count as player construction.
S2 launch, inversion, magnets and multi-car Spinning remain later work.
