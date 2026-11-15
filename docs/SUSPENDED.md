# Suspended coaster

The two-seat cabin hangs below its bogie and swings sideways through curves. Its
native longitudinal hinge has physical stops at ±45°. It is not a full-turn car.

## Build

Craft **Suspended Coaster Track** from iron, copper and orange dye, and a
**Suspended Coaster Car** from two Create red seats, iron, a Suspended Swing Latch, matching
track and a Coasters cart. Craft the latch from two iron ingots, two copper
ingots and redstone. Place ordinary Coasters anchorpoints, aim their axes
and bank, then use the Suspended track item on the two ends to connect them.
Place the matching car by aiming at the completed curve. Ordinary track does
not provide this car's required overhead reservation and lift locks.

Leave a clear reservation on each side of the graph center: three blocks
laterally, 4.70 below, 1.10 above and 1.75 fore/aft. Use curves with radius at least
16, bank at most 30° and slope at most 60°; no inversions. Put station platforms
outside the lateral reservation, at the cabin floor. Keep the station straight
at least 36 blocks long. The example platforms are four blocks to each side.

One to four identical cars may be linked with the native Wrench at exactly eight
blocks between bearings. Stop and capture both cars before linking. Initially
bind each car to a nearby Boarding Station; after coupling, bind the train to the
chosen station again. Do not mix families or add an ordinary recovery bearing.

## Lift and free swing

Use the Rotation Tool on the intended rising lift track and enable **Lift lock**.
Mark every rising lift segment. The level station should directly precede the
first marked section; a latched departure retains its lock along that approach, before any
uncontrolled approach. Native Chain and Create rotation provide travel. Keep
lift drive at no more than two blocks/second, and all other travel below twelve.

Bind the Boarding Station, wait for Ready, board by right-clicking a seat cushion,
close restraints and Dispatch. The stopped dock latch must settle for five server
ticks. The physical lift lock stays closed through the marked lift and releases
only after the rear of every linked car clears the exit. The cabin then swings
under gravity and inertia; equal rider masses affect the native center of mass.
Damping increases near the stops.

On return, request **Capture** or power the station input. Approach damping alone
does not allow unloading. The train must stop at its bound level dock, align,
and latch before **Open restraints** and normal Shift dismount become available.
The visible latch is green when physically retained and orange when free.

## Interrupted operation

An unsettled lift approach stops before the marked section. Keep restraints
closed. A pending entry is still unlocked and damped until angle, angular speed
and travel satisfy the five-tick guard. A previously engaged lift lock remains
engaged during power loss or rollback. Restore the Chain drive and inspect the
fault; use **Resume to dock** explicitly. At the dock, Capture and Dispatch are
separate actions. Restoring power never automatically dispatches Suspended.

Reload retains the cabin angle, angular speed, riders, limits and lift identity,
but holds travel for inspection. Missing bodies, changed support or changed
couplers stop the train and retain riders. Restore the matching route before
recovery; ordinary towing has no qualified overhead adapter.

## Example

`/fd demo suspended_train` builds the four-car demonstration; its native couplers
wait for real dock latches. `/fd demo suspended` builds a demonstration route and car in a clear
area near world origin. `/fd demo suspended_track` builds only the route so the
car can be placed normally. The loop includes a level station, a rising locked
lift and banked return curves. The command is a teaching fixture; it does not
replace normal crafting or construction evidence.
