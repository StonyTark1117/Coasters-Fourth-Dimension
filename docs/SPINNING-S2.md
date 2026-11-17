# Launched Inverting Spinning

This is the four-seat red-harness Spinning car on **Launched Inverting Spinning Track**.
It supports one to four identical cars, with eight blocks between bearings. Its
free seat turntable rotates about the car's local vertical axis, including when
the track is upside down. Uneven passengers and acceleration affect the spin.
It has no programmed scenery-facing target.

## Build and supply the launch

Craft sixteen track pieces with six iron ingots (top and bottom rows), two
redstone dust (middle left/right) and red dye (center). The car recipe uses four
Create Red Seats, a Mechanical Bearing, a native Coaster Cart, two iron ingots and
one Launched Inverting Spinning track piece. The Kinetic Launch Governor recipe uses four iron ingots,
two redstone dust, two Create Shafts and a Clutch.

Place and connect Launched Inverting Spinning track with the normal track item, anchors and Wrench.
Keep bends at radius 16 or greater, roll change at most 5 degrees per block, and a
clear rotating-car reservation of 2.25 blocks to either side and 3.65 above the track.
Allow 2.25 blocks ahead/behind each bearing. Classic Spinning track is a different
variant and cannot substitute for inversion-rated track.

Use a level dock at least 36 blocks long. Place the car and bind a Boarding Station.
For a train, wait for each empty car's captured readiness, link with the normal
Chain, then bind the station to the complete train.

Commit a native Chain drive span at least 24 blocks long. Put the governor's amber
output toward that anchor through a straight matching-axis shaft line of at most
sixteen blocks. Supply its rear input with non-overstressed Create kinetic power
at 192 RPM or greater. The governor consumes stress; it is not a power source.
Sneak-right-click the governor with an empty hand near an empty stopped train to
bind it. Ordinary use shows its power, output and travel-speed status.

The governor ramps real native Chain travel from 0 to 12 blocks/s at no more than
3 blocks/s². Magnetic devices cannot propel the train. A long return may need
additional powered Chain sections because native rolling drag consumes coast
speed; the example uses an 8 blocks/s powered return after its unpowered inversion.
All native Chain travel remains subject to the same ride authority and stop.

## Board, spin and return

Right-click a seat cushion from the platform while the car is stopped, captured
and unrestrained. Use the Rotation Tool while seated to open the car controls.
Close restraints, then Dispatch. The editor closes as motion starts. Independent
look remains available while the native carrier carries the player through spin
and track roll. Closed restraints prevent Shift from unloading a moving car.

An Inducer span applies signed local-Y spin torque; a Damper span opposes the
current relative spin. Place the matching magnetic attachment with its normal
item and edit its span using the Rotation Tool on the side of a track anchor.
Each car has one carrier: use target mask `1`. For example,
`2:20:damp:1:6:0.1:1` adds a Damper from distance 2 to 20 with strength 6.
Applying an added span consumes one equipment item; removing it refunds that item.
Clear the section and its approaches before editing. A stronger field is still bounded:
magnetic angular acceleration is at most 1 rad/s², and at 3 rad/s both modes damp.
The car retains passive damping outside magnetic spans.

On the level dock, press Capture. Travel brakes first; the turntable then aligns
to the nearest equivalent station orientation. Wait for Ready, open restraints,
and use Shift to leave. Capture on an inversion stops travel but does not make
that location a safe boarding dock. Resume to dock is an explicit retained
recovery action after checking the route and power.

For a designated Chain lift, enable Lift lock on the track before operating.
Enter with settled captured seats. The lock holds the angle relative to the car,
including changes in pitch, and releases after the complete car clears the span.
Do not enable it as a way to force a moving free turntable to a new angle.

## Interrupted operation

Loss of launch power, missing return Chain power, a missing body, changed coupler,
invalid route or blocked sweep retains restraints and stops travel. Restore the
specific fault, then explicitly Capture/revalidate. Restoring power alone does
not dispatch. A saved pending ramp likewise stays held after restart; retained
winding and spin state are not reset to zero.

Set forward or Set reverse only while the whole train is stopped. The controls
hold the governor and require explicit dispatch/resume afterward. Reverse travel
does not reverse the meaning of the seat's stored spin angle. Never use a second
controller or magnetic field as a competing longitudinal drive.

## Original example

An operator can use `/fd demo s2_spinning` or `/fd demo s2_spinning_train` in a
clear area. Use `/fd demo s2_spinning_track` for the route and unbound equipment without a car.
These commands are disclosed route setup, not proof of normal
construction. They provide a level launch/dock, an unpowered downhill full roll,
Inducer and Damper spans, and a supplied Chain return. Four-car setup waits for
physical dock latches before coupling. Operate it with the normal controls above.
Capture as the train returns to the launch dock; it does not stop automatically.
