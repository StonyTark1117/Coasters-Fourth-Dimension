# Seated Pipeline

Pipeline uses four fixed seats between dedicated side rails, in one or two cars.
The whole car follows banks and rolls. There is no standing mode, spring seat or
independent rotating seat. The P rail gauge is three blocks; ordinary Coasters
track and ordinary N-contact carts cannot substitute for Pipeline equipment.

Craft sixteen Pipeline Track with `ICI / IDI / ICI`: I is iron ingot, C copper
ingot and D blue dye. This arrangement differs from Free Spin's blue recipe.
Craft one car with `SIS / SCS / ITI`: S is a Create red seat, I iron ingot,
C a native Coasters bearing and T a crafted Pipeline Track.

Use the track on two native anchors to build a paid, dedicated section. Reserve
the whole cabin: half-width 2.05, 1 block above and 2.5 below the graph line,
half-length 2.75. Keep radius at least 16 and roll change at most 5 degrees per
block. Side supports stay outside the cabin; ties pass below its floor. Place
cars by aiming at the rendered rail and using the car item. Keep eight blocks
between bearings; normal Minecraft Chain links at most two identical cars.

Use an empty hand on a nearby Boarding Station to bind the stopped car. After
coupling, Shift-right-click the station to rebind the complete train. The dock
must be level, at least twenty blocks long, clear and large enough for both cars.
Use an open seat to board, then open the Rotation Tool editor, close restraints
and Dispatch. Native Create-powered Chain supplies motion; speed must remain at
or below twelve blocks per second. Stop brakes the whole train. Restraints open
and Shift unload only when every car is stopped at the bound level dock.

For reverse travel, Stop first, reverse the native power source using its normal
controls, then explicitly Resume. Resume validates the complete loaded route and
accepts its new drive direction. Changing direction while travelling, losing power,
losing a controller or breaking the retained consist stops the train and keeps
restraints closed. Repair the reported condition and explicitly Resume to the dock.
A restart also keeps the actual car pose and seat assignments while travel is held;
it never automatically dispatches or teleports a car back to its station.

`/fd demo pipeline` creates a disclosed powered fixed loop with a bank/full roll,
return and station. `/fd demo pipeline_train` adds the second car with eight-block
coupling. `/fd demo pipeline_track` builds only the route. These operator commands
are setup conveniences; normal paid track/car construction remains available.
