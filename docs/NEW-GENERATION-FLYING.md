# New-generation Flying

This two-seat car runs on dedicated fixed overhead track. Its outer deck turns
around the chassis Y axis, and the seat row changes between seated and prone
inside that deck. Both postures support physical scenery-facing yaw, including
combined yaw/posture transitions and a stopped, named Show.

Craft the purple New-generation Flying track, matching car and Scenery Target
Marker. The recipe book lists these names. Six iron ingots, two copper ingots
and one magenta dye make sixteen track pieces. Four iron ingots, three Create
red seats, one Coasters cart and one matching track make a car. Three iron
ingots, a compass and glowstone dust make a marker.

Use the track item on two native anchors to connect them; the connection consumes
track according to its length. Aim the car item at the matching curve within
reach and right-click to place it. Only identical variants may couple: use
Minecraft Chain on each native bearing at eight-block spacing, with at most four
cars. After linking, Shift-right-click the Boarding Station with an empty hand
to bind the complete consist. Place the Scenery Target Marker with normal
right-click and enter its block position in the track form.

Reserve the complete rotating envelope: 2.75 blocks on each side, 1.1 above the
track origin, 5.25 below and 2.75 fore/aft. Keep motors outside that space; use
native shafts to reach drive ports. The level boarding dock needs at least
36 blocks. Bind a Boarding Station near the stopped car. Routes need radius 16
or greater, bank change at most 5 degrees/block, and fixed world anchors.

## Author a route

Use the Rotation Tool on the track to open its section form. Only the creator or
an operator may change it, while the ride is empty and stopped. Each named
section has canonical start/end distances on that track edge. Saving the same
name replaces that section; use Next section to select an existing one. Sections
must not overlap. Distance follows the edge's first anchor even when travel
reverses.

Choose PROFILE for angle keys such as `0:90;24:180`. Angles are degrees and keep
whole turns: 720 means two complete turns. Keys increase in distance and interpolate
smoothly with zero slope at their endpoints. Reverse travel retraces those same
keys. Stopping freezes the distance input.

Choose TARGET and enter the position of a yaw marker placed by the ride creator.
Keep each marker in loaded chunks near its section. The stored binding includes
that marker's identity and dimension; replacing the
block requires rebinding. Facing uses the marker's projection onto the chassis
plane. Its height outside that plane is ignored. The same presentation heading
applies when seated or prone and follows bank/inversion with the chassis.

The posture fields range from 0 degrees seated to 90 prone. A route changeover
needs at least 20 blocks and travel at most 2 blocks/s. Yaw and posture may change
together. Default blend distance is 8 blocks; allow enough distance at the chosen
travel speed for the native bodies to settle. Travel holds when a required
posture endpoint is not ready.

Yaw stays within four turns either way. NEAREST selects the closest reachable
winding, with a positive rotation for an exact half-turn tie. POSITIVE and
NEGATIVE select the permitted direction. No available branch produces a winding
fault, rather than an automatic unwind. The upper yaw bounds are .6 rad/s and
.8 rad/s²; the form may lower them.

A SHOW_STOP section also names a stop and a duration in ticks. Put its stopping
point on a clear span, away from anchor blocks and drive equipment. Stop the ride there,
then acknowledge Start with its named button. Its bounded yaw program advances
while route distance is held. It cannot open restraints or drive the train.
After it finishes, Dispatch authorizes travel again.

## Board, operate and unload

At the seated, unwound dock, right-click a reachable seat. Use the Rotation Tool
to open the car controls. Close body and leg restraints separately, then Dispatch
the authored route. The screen shows actual unwrapped yaw/turn count, posture,
endpoint lock and any fault. Your normal look remains independent of physical
yaw. The prone neutral head frame follows the deck while the torso stays on the
physical posture carrier.

Stop the returning train at the dock. Unwind + seated explicitly returns actual
yaw to zero and the seats upright; a 720-degree yaw is not already zero. Once the
native locks settle, open each restraint and use normal Shift to unload.

Marker loss, invalid geometry, unavailable power or hierarchy, degenerate target
projection and winding exhaustion hold the ride with restraints retained. Fix
the named cause, stop and use Retry / revalidate. Restart also requires explicit
revalidation. No target catches up by jumping directly to a new pose.

The Conventional Flying car retains its separate four-seat, station-only
changeover controls. Accepted construction, content and occupied qualification
records are in [the package evidence](evidence/0.7/new-generation-flying/README.md).

The optional `/fd demo new_generation_flying` example creates a powered loop in
an empty area. It starts seated sideways, targets nearby scenery while seated,
changes posture and yaw together, targets scenery through a full roll, holds a
named Show in clear space, and returns seated. Its generated track is an example,
not player-built construction. Open the car controls at the Show to acknowledge
Start; Dispatch after completion. Stop on the original station span and explicitly
unwind before opening body and leg restraints.
