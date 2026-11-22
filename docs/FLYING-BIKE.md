# Suspended Flying Bike

The Flying Bike carries two riders face-forward in fixed prone cradles below
its own overhead track. A rigid car follows the track’s pitch and bank. Each
rider has a chest/pelvis pan, forward grips and a linked body-and-calf restraint.
It supports one car, two launches and one level boarding dock per ride.

## Craft and construct

Craft Flying Bike Track using iron, copper and cyan dye. Craft the car using
two cyan seats, iron, a native Coaster Cart and one Flying Bike Track. Each
Flying Bike Launch Governor uses iron, redstone, shafts and a precision mechanism.
The track recipe yields sixteen track tools; car and governor recipes yield one.
The car creates its own attached supports, seats and controller.

1. Use Flying Bike Track and native anchor construction to build a loaded,
   unambiguous closed loop. Use this track throughout. Minimum radius is sixteen
   blocks and roll change is at most five degrees per block. Keep the complete
   4.5-block width and five-block length clear, including 4.5 blocks below and
   1.1 above the track frame. Open restraints also need this reservation.
2. Make two distinct upright, level launch straights of at least 32 blocks. Put at least eight unpowered straight, upright, level blocks before each powered start; the drive shaft must be away from a curve joint so the native chassis is aligned before passing it.
   Commit a native Chain span on each using the Chain tool. Do not overlap the
   spans or add a third powered span. The first target is 8 blocks/s, the second
   12; both accelerate at most 3 blocks/s². A loop may have at most 32 sections
   and 1,024 blocks of track.
3. Connect each Chain anchor’s drive port through a straight shaft to the
   governor’s front output, at most sixteen blocks away. Feed each governor’s
   rear input from a real Create power source providing 192 RPM and adequate
   stress capacity. The governor consumes stress; it does not generate power. Create may choose a preferred placement orientation from adjacent shafts. Check the front output and use the normal Create Wrench to rotate a governor until its front faces the Chain and its rear faces the supply before binding.
4. Place one Flying Bike Car on the first level straight. Build side platforms
   outside the ride reservation, with solid footing and two blocks of headroom
   at each exit. Put the two exit columns four blocks to either side of the track center, with their walking surfaces 2–2.8 blocks below the track frame. Bind a Boarding Station at the first launch’s level dock.
   Leave at least sixteen clear blocks for the returning car to brake.
5. With the car empty and stopped, empty-hand use the first governor within
   twelve blocks of the car to bind it. Sneak-use that governor to select it,
   then sneak-use the unbound second governor within 256 blocks. The result
   must say that two launches belong to one ride authority. Both need power
   while binding. The launch identities and geometry must remain intact.

## Board, launch and return

At the stopped dock, use an open seat normally with an empty hand. Riders
remain prone while boarding. Use the normal Rotation Tool controls to close
restraints and Dispatch. Mouse look moves the view and head while the torso
stays aligned with its cradle. No separate flying transformation is required.

The first Chain accelerates to eight blocks/s. The second pre-spins to meet the
approaching car and then accelerates to twelve. The empty launch transmission
may turn while the other span is occupied; this prevents catching a moving car
on a stopped Chain. Both belong exclusively to this single car.

After the second launch and return, the car stops and brakes automatically
on the first straight. Wait for the dock-ready indication, open restraints,
then dismount onto the platform. The next cycle needs another Dispatch.
You may Stop earlier to stop and retain the riders. Power loss, obstruction,
missing support or a changed controller also stops the ride.

## Interruptions and saved worlds

Restoring power never launches the car automatically. Restore its exact support,
shaft and launch geometry, Stop to acknowledge the repaired state, wait for
five stopped ticks, then explicitly Resume. A held car on a launch can use its
powered Chain to continue; an unpowered level coast cannot start itself. For a coast stop, build dry world-horizontal exit platforms beside both actual
transformed seats, even when the car is banked or inverted. Each exit must be
within four blocks horizontally of its prone head, no more than 1.25 blocks down
or .6 blocks up, with solid footing and 1.9 blocks of clear headroom. Restore
missing matching support first, keep restraints closed, and bind a second
Boarding Station within twelve blocks of the stopped car. This explicitly
selects evacuation only; it preserves the original launch dock. After five
stopped ticks and successful exit checks, open restraints and dismount normally.
New boarding, Dispatch and Resume are denied in this mode. If an exit disappears,
opening and dismount are denied again. Once everyone has evacuated, safely remove
and rebuild the empty car at its original launch dock. No unpowered coast
recovery to the original dock is promised.

Saved worlds preserve the car, riders, dock and launch identities. Drive leases
are not restored. Inspect the ride and use Stop/Resume after loading an
interrupted ride. Do not couple cars, mix track families or connect competing
propulsion controllers to this profile.

## Original example

`/fd demo flying_bike` builds an original ten-section loop (two 56-block launches with eight-block straight approaches), level dock,
platforms, two Chain spans, supplied governors and one car in an empty area near
world origin. `/fd demo flying_bike_track` omits the car so you can place it
normally. Bind and link the governors, board and operate the controls yourself.
The example’s Creative Motors are disclosed setup power sources. The command
is not evidence of player construction or survival crafting.

The commercial flying-bike announcements inspired the concept; this two-seat
vehicle, its dimensions, supports and control system are original game design.
