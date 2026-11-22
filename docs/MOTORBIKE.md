# Motorbike Car

The Motorbike Car carries two riders on narrow side-by-side saddles. It uses one native above-track chassis and a rear hinged hold-down. The `straddle_v1` configuration supports one unlinked car on matching Motorbike Track.

## Build a ride

1. Craft Motorbike Track from one native Coasters Track and orange dye. Place Coasters anchors, then use Motorbike Track on the two endpoints to bind the section. Survival placement consumes one matching track item per four blocks of section length, rounded up.
2. Keep curves at radius 16 or greater, grades at 30° or less, and banks at 45° or less. Leave a clear occupied volume 1.85 blocks each side, 2.65 above the graph, and 1.5 ahead/behind the car. Use native Chain propulsion with a nominal maximum of four blocks per second; gravity spans are allowed.
3. Provide a straight, level station section at least 12 blocks long. Leave two blocks between the bearing and either section end. Build dry, sturdy platforms on both sides, with the exit centers three blocks from the track center, floor 0.50–1.05 blocks above the track graph and two blocks of clear headroom. Keep the body and hold-down sweep clear.
4. Craft a Motorbike Car using two saddles, five iron ingots, one native Coasters Track and one native Coaster Cart. Place it on the matching track with at least six blocks to another bearing. The car item is consumed only after successful Survival placement.
5. Place a Boarding Station within twelve blocks. Use it to bind the stopped upright car; sneak-use to rebind. The station records the original section identity. Repair changed track using the existing Repair/Realign tools before resuming.

## Ride and return

Use an open saddle normally to board. Open the car's Rotation Tool screen, close the hold-down, then choose Dispatch. The model straddles the saddle, with feet at the steps and hands toward the bars; mouse look remains available. Closed restraints prevent ordinary Shift dismount.

Stop requests braking and retained capture. Open the hold-down only after the car is upright and still at its bound level station with both platforms clear. Shift then exits to a safe nearby floor. For a retained off-station failure, keep riders secured, repair the route and use Resume to return to the bound platform before unloading; do not remove supporting track to unload passengers.

Loss or reversal of an accepted Chain drive retains the car with the hold-down closed. Restoring power does not release it automatically. Inspect the route, restore support/power, and explicitly Resume to return to the dock. A cold restart also retains a previously running car until Resume. The car has no coupling, recovery tow, rotary-seat programming or posture changeover.

Resume releases the retained stop; it does not propel the car. If the car stops on an unpowered span where gravity cannot move it, supply or extend native Chain propulsion to that span before choosing Resume. Keep the existing route, support and travel-direction requirements satisfied; explicit Resume rescans the drive directions.

Tools → Palette changes upholstery independently of the mechanical configuration. Skin body replacement is unsupported and is denied for Motorbike; comfort controls retain their existing meanings.

## Original examples

`/fd demo motorbike` builds the original powered loop, a car and its bound platform. `/fd demo motorbike_track` builds the same track and platform for your own car placement. Both require operator permission and a clear area near world origin; generated construction is example setup, not a Survival walkthrough. The loop includes a banked section. Two Chain drives sit midway along level straights, away from curve joints, so the native chassis clears their shafts; keep this straight approach when constructing your own drive ports.

The dimensioned implementation contract is [FD2202](specs/FD2202-motorbike.md). Runtime evidence is collected separately under [FD2202 evidence](evidence/1.4/fd2202/).
