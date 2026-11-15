# Fixed Inverted

Four seats ride rigidly below the overhead track. The row follows the track bank
through complete inversions; it does not swing or spin independently.

Craft **Fixed Inverted Overhead Track** with iron, copper and purple dye. Craft a
**Fixed Inverted Four-seat Car** with four Create red seats, three iron ingots,
one native Coaster Cart and one matching track item. These items are in the
Fourth Dimension creative tab and the normal recipe book.

1. Place Coasters anchorpoints and connect them with the matching overhead track
   item. Native curve handles set the route. After changing an authored curve,
   use the Rotation Tool to rebind its profile. Ordinary and Suspended track are
   different functional providers and do not accept this car.
2. Leave the complete train corridor clear: 2.25 blocks either side, 1.10 above,
   4.50 below and 1.75 fore/aft from each graph sample. Use radius16 or larger and
   bank changes no faster than5 degrees/block. Keep travel at or below16 blocks/s.
   Supports and platforms must remain outside the moving rider/hanger reservation.
3. Aim at matching track and use the car item. Bind a Boarding Station beside a
   straight, level section at least36 blocks long. A four-car train needs eight
   blocks between bearing centers. Bind newly placed cars before coupling with
   the native Wrench; then sneak-use the station to rebind the complete train.
   Up to four identical Fixed Inverted cars can link; mixed families cannot.
4. Approach a cushion from below-track platform level. Use the cushion to board;
   the tall back and rear hanger are not boarding targets. Use the Rotation Tool
   or bound station to open controls. **Close restraints**, then **Dispatch**.
   Create-powered native Chain drives the lift/route. Camera comfort follows the
   same saved setting as Wing; look remains independent of the moving chassis.
5. On return to the declared level dock, use **Stop** or power the station's rear
   input. Wait until the whole train is Ready, then **Open restraints** and Shift
   to leave. Redstone power removal never automatically dispatches this family.

A stop elsewhere is an emergency hold, not an unload station. Power/controller,
frame, coupling or support loss stops travel and keeps restraints closed. Restore
and inspect the cause, use Stop to acknowledge the repaired state, and explicitly
**Resume to dock** while everyone remains restrained. Lost matching track uses
**Repair matching track** then **Realign matching track** before Resume. Returning
power alone cannot restart the train. Ordinary-bearing towing is not supported.
A saved inverted chassis keeps its physical pose and fixed seat identities when
reopened; it is not reset upright to permit unloading.

The operator-only `/fd demo fixed_inverted` builds an original lifted stadium with
four quarter-roll spans. `/fd demo fixed_inverted_train` supplies four linked cars;
`/fd demo fixed_inverted_track` builds the route without a car. Run the command
high enough above terrain in a clear area: its bounds extend roughly8 blocks left,
152 right,56 behind and360 forward, with6 blocks below and32 above the origin.
These commands are disclosed example setup, not survival construction. The route
uses ordinary native Chain sources, including one continuous128-block Chain span
through the roll driven from its upright approach. Keep motors away from banked
anchors so neither seats nor running gear intersect them. It has a48-block level dock and 32-radius return turns and spans no longer than64 blocks;
you can build the same layout with anchors, track items and native controls.

The exact route inputs and evidence are in
[Fixed Inverted evidence](evidence/0.6/fixed-inverted/README.md). Implementation and
qualification are in progress; that checkpoint names current gaps rather than
claiming the prepared example has already passed a player run.
