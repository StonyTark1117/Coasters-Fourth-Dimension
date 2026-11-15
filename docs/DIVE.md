# Dive — six-abreast fixed row

Implementation in qualification for FD1702A–C. The one supported variant has six
fixed seats per car and one to three native cars coupled at eight blocks. Use its
own white/iron Dive Track: ordinary N rails do not reserve the wide occupied row
or declare the hold section. Keep 3.25 blocks clear on either side, 3.65 above the
rail design origin and .95 below, including the whole crest and drop. Curves need
radius at least16 and roll change at most5 degrees per block; travel is limited
to16 blocks/s. The passive drop brake starts below that limit.

## Build and board

Craft Dive Track with iron across the top/bottom rows and copper–white dye–copper
in the middle; each recipe gives16. The car uses six Create red seats in its first
two rows and iron–native Coasters bearing–Dive Track in the bottom row. The hold
controller uses `IRI/CTC/IRI`: iron, redstone, copper and Dive Track respectively.

Use crafted Dive Track between normal native anchors, then use the crafted car
while aiming at the dedicated rail. Leave eight blocks between car centers and
connect their native bearings with normal Minecraft Chain. Bind a Boarding
Station to the complete stopped train on a level dock at least28 blocks long.
Board normally, close the restraints in the Rotation Tool editor, and Dispatch.
Only a verified level dock permits opening and dismounting.

## Author the hold

Use a fixed descending section at least28 blocks long, aimed60 degrees downward.
Its outgoing connection must be one unambiguous matching Dive section with clear
occupied envelopes. Put the Dive Hold controller within twelve blocks of the
hold section's start, outside the swept row. Empty-hand use binds the section.
Supply redstone to enable release. Empty-hand use reports phase/countdown/fault;
sneak-use changes dwell by20 ticks, cycling20–200, only while the bound train is
empty and stopped. Default dwell is60 simulation ticks.

The incoming native Chain must deliver no more than2 blocks/s at the hold; the
example uses1 for margin. Its power source sits on an upright approach, connected
by native Chain across the crest and hold. Keep motor blocks outside the wide row.
No motor or launch impulse starts the drop: release suppresses Chain while any
car remains on the hold section, and gravity starts motion.

The front car reaches20 blocks along the bound section before passive braking
acquires the train. Every car must settle below.05 blocks/s and have a physical
native hold for five server ticks. Only then does the dwell count down. All rear
cars retain their own native graph frames; the train is never rotated about the
front bearing. The server commits DROP and its authority epoch before removing
the physical constraints.

## Interruptions and return

Power/controller loss freezes the hold and countdown. Restore the original
controller, power, matching support and exit; use the car's explicit Resume after
the whole train stops. Reload also requires Resume, and elapsed wall-clock time
cannot expire the dwell. A restored DROP remains released-state recovery: brake
and resume that state instead of moving the cars back to the crest or starting a
second countdown. Closed restraints remain retained during repair.

Stop at the returning level dock, then open and unload. A ride authority retained
by the hold controller can be transferred while stopped to a bound station using
the existing authority controls before a new station dispatch.

Operator example commands are `/fd demo dive`, `/fd demo dive_train` (three cars)
and `/fd demo dive_track` (buildable route without a car). These commands provide
disclosed setup; they do not count as player construction. The loop includes an
uphill native Chain lift, slow crest, physical hold, gravity drop and powered
return with a level station.
