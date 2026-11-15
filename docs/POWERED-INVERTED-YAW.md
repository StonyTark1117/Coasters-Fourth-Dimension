# Powered Inverted Yaw

Powered Inverted Yaw has two upright seats below its overhead track. Each car
turns independently around a physical vertical yaw spindle. One ride controller
moves a train of one to four identical cars; there is no prone changeover.

## Build and supply the circuit

Craft sixteen track items from six iron ingots, two redstone dust and cyan dye.
The car uses two Create Cyan Seats, a Mechanical Bearing, a native Coaster Cart,
four iron ingots and one matching track item. A circuit governor uses four iron,
two redstone, two Create Shafts and a Clutch.

Connect native Coasters anchors using Powered Inverted Yaw Track. Leave six
blocks between car bearings and at least 28 blocks of level station track for
four cars. Use radius 12 or larger, bank at most 45 degrees and slope at most 30
degrees. This vehicle does not invert. Keep the yaw reservation clear: 1.5 blocks
to each side, 1.1 above the track, 4.5 below and 1.75 ahead/behind each bearing.
Platforms belong below the seats and outside the rotating reservation.

Make one closed, unbranched circuit for the complete train. Every section needs
full native Chain coverage and a supplied circuit governor. Point each governor's
amber output at its Chain anchor through a straight shaft line no longer than
sixteen blocks. Supply at least 128 RPM to the rear, with enough Create stress
capacity. This is a kinetic substitute for onboard propulsion: the governors
consume supplied power and do not generate it.

With the complete empty train stopped, use an unbound governor with an empty
hand within twelve blocks of the train. It becomes the master and binds the
other governors on the circuit. One governor must serve each complete section;
no competing controller can own those governors or the train. A section must
be at least eight blocks long. Bind a Boarding Station at the level dock, within
twelve blocks of the nearest stopped car.

## Author yaw, speed and stops

Use the Rotation Tool on the side of an anchor to open its section form. Empty
and stop the ride before changing a program. Give each interval a name, start
and end distance. Saving the same name replaces that interval. The form also
sets its travel speed, at most 8 blocks/s; cover every moving part of the circuit.
The governors limit acceleration and deceleration to 2 blocks/s².

- **PROFILE:** yaw keys use `distance:degrees`, separated by semicolons. For
  example, `0:0;64:360` turns once over 64 blocks. Reversing retraces the keys.
- **TARGET:** enter the coordinates of a Yaw Marker placed by the ride owner.
  Each car projects that marker from its own physical pivot; height does not
  change the heading in the chassis plane.
- **SHOW_STOP:** enter yaw keys, a show name and duration. Add a stop inside
  the interval. The train first stops and acknowledges its native hold; only
  then does the timed yaw program advance.

Blend distance smooths entry from the current heading. Winding chooses the
nearest, positive or negative reachable turn. Stored yaw is unwrapped within
-1440 to +1440 degrees; zero modulo 360 does not discard accumulated turns.
Yaw speed is at most 0.6 rad/s and acceleration at most 0.8 rad/s².

A blank Stop distance means no stop. Otherwise choose a distance inside the
interval, dwell 20–1200 server ticks and the direction afterward:0 keeps the
current direction, +1 selects forward, -1 selects reverse. Dwell begins only after
all cars stop. A show waits for its actual yaw target to settle before moving
again. Forward follows the first bound section's endpoint order; the controller
resolves the remaining circuit's direction. Reversal waits for five actual
stopped ticks.

## Ride and return

Right-click an open seat cushion at the captured dock. The Rotation Tool opens
the vehicle controls while seated. Close restraints, then Dispatch route. The
screen closes as travel begins; independent player look remains available.
The operating view reports physical yaw, target, phase, requested travel, dwell,
restraints, authority hold and event sequence.

Press Capture / unwind while returning to the level dock. The governors first
ramp travel down. At the stopped dock, yaw deliberately unwinds to zero and
latches. Wait for Ready, open restraints, then use Shift to unload. Capture
elsewhere stops the train but does not make that place safe for unloading.
Retain / stop is an emergency hold. Set forward/reverse is available only after
the whole train has stopped; it does not dispatch by itself. If you stopped
outside the dock, select the direction back toward it and use Explicit resume.
Open Capture controls before reaching the dock and leave braking distance
(speed squared divided by four, plus a margin) inside its level section.

## Scene outputs and interrupted operation

The master governor emits a four-tick redstone pulse for SECTION_ENTER,
STOP_HELD, DWELL_COMPLETE, YAW_REACHED, RIDE_FAULT and DOCK_CAPTURED. These are
bounded transition events carrying ride/car identity, section or event ID,
server tick, authority epoch and sequence. Boundary reentry is limited to one
repeat per 20 ticks. Toggle event chime, while empty and stopped, enables the
registered note-block chime. Event consumers cannot release restraints, change
travel authority or execute commands. Reload does not replay old pulses/audio.

Missing power, a removed governor, changed route/consist, missing body, blocked
sweep, stale authority or invalid scenery target retains riders and stops travel.
Restore the named condition, then use Explicit resume to revalidate. A paused
show resumes its saved remaining dwell and yaw work; power restoration and
reload alone never launch it. Recovery keeps the native travel hold for fresh
stopped samples before releasing a new authority epoch. Capture / unwind instead requests a deliberate
station return to zero when already on a safe dock. A failed unwind keeps the
car stopped and restrained.

## Supplied example

In a clear area, `/fd demo powered_inverted_yaw` supplies a one-car circuit;
`powered_inverted_yaw_train` supplies four cars and
`powered_inverted_yaw_track` supplies only the circuit/equipment. Example setup
is disclosed construction assistance; normal controls still operate the ride.
The route turns once while travelling, tracks a central scenery marker, performs
a timed yaw show, then reverses. Capture on the level dock after returning,
wait for unwind, open restraints and unload. The one/four-car examples wait for
physical dock latches before coupling and binding the master.
