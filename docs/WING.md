# Fixed-seat Wing

The Wing car seats four riders beside ordinary Coasters track. All four seats
stay fixed to the chassis through bends and banks. It uses no rotation profile,
magnetic spin device or rotary boarding lock.

Craft it with the following grid:

| Red Seat | Iron Bars | Red Seat |
| --- | --- | --- |
| Iron Ingot | Coaster Track | Iron Ingot |
| Iron Ingot | Coaster Cart | Iron Ingot |

Use ordinary Coasters anchors and connections. Keep curves broad: radius at
least 16 blocks, with gradual bank/grade transitions (no more than about 2.9° per
block). Reserve 4.8 blocks on each side of the track center, 3.6 above and .8
below, including scenery and supports. The placement message identifies a
blocked or unloaded area. Functional 4D sections and moving tracks are not Wing
track.

1. Place the Wing item on a clear level section. Leave at least six blocks
   between bearings. A train may contain one or two Wing cars. Hold a Minecraft Chain and
   right-click the central native bearing block of each stopped chassis in turn,
   six blocks apart. Approach its exposed side at bearing height, with no track
   between you and it. A seat click boards instead of selecting a link. Wait for
   **Carts coupled**. Sneak-right-click clears a pending selection. This links
   the cars; a separate track Chain span supplies propulsion. Mixed families reject.
2. Right-click a seat to board. Use the Rotation Tool on the car to open its
   controls, choose **Close restraints**, then **Dispatch**. Dispatch permits
   travel; a Create-powered native Chain span supplies propulsion.
3. Use **Stop**, or a powered nearby Boarding Station, to brake. Wait for
   **Ready** on level track, choose **Open restraints**, then press Shift to
   unload onto a clear nearby platform.
4. To change the cushion livery, empty and stop the car, open restraints, then
   use a dye on one seat. All four seats change together; the color survives
   saving. Color does not change track compatibility or seat behavior.

If the car stops with a track, clearance or consist fault, inspect the named
condition, restore it, then use **Stop** to acknowledge the safe state. It will
remain stopped until a separate valid Dispatch. Do not remove track or seats
under occupied cars. No ordinary-bearing recovery adapter is supplied for Wing.

For an operator example, stand in an empty area and run `/fd demo wing`. The
loop occupies about 113 × 10 × 129 blocks, has broad turns, a sustained 30° bank,
Create-powered Chain propulsion and level side platforms. The command creates
the route and car as an example; it is not a survival build. `/fd demo wing_track`
creates the same route with an empty station so you can craft and place the car
yourself. Bind the Boarding Station normally after placing your car.

The controller's **Stop** action brakes the train; it does not rotate seats.
Restraints stay closed while moving or faulted. A stopped banked car is not ready
for unloading: return to its level boarding section before opening restraints.

Press **K** (changeable in Controls → Fourth Dimension) for a world-stable rider
view. It holds the world-upright heading from activation/boarding and keeps mouse
look free; it does not follow the ride's heading, banks or inversions. Press K
again for the normal seat-following camera. The saved preference affects only
your view; the seat, physical body and eye position still move with the ride.
It automatically stops applying on dismount, death, or leaving the world.
