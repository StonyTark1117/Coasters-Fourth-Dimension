# Building and operating the five families

Use matching track and car items. A 4D car cannot run on ordinary track or on
another 4D family's track. Track appearance changes decoration; it does not
change the mechanism or make an incompatible car eligible.

## Craft and discover

All fifteen player items have crafting recipes. Acquiring the ingredient below
discovers the corresponding recipe in the vanilla recipe book. Crafting an item
also discovers its recipe. Track recipes make sixteen items; magnetic equipment
recipes make two items; the remaining recipes make one item.

| Item | Discovery ingredient | Use |
| --- | --- | --- |
| Controlled 4D Track | Red dye | Arrow/S&S controlled cars |
| Legacy ZacSpin Track | Yellow dye | Original free-rotation ZacSpin cars |
| Magnetic Free Spin Track | Blue dye | Free Spin cars and magnetic spans |
| Revised Free-Rotor Track | Purple dye | Revised independent paired carriers |
| PAX-Inspired Shuttle Track | Green dye | PAX shuttle trains |
| Each family car | Its matching track item | Assemble the corresponding car |
| Rotation Tool | Compass | Profiles, tuning, capture and restraints |
| Magnetic Spin Inducer | Redstone dust | Apply Free Spin torque |
| Magnetic Spin Damper | Nether quartz | Oppose Free Spin angular velocity |
| 4D Boarding Station | Comparator | Capture and dispatch non-PAX cars |
| PAX Shuttle Winch | PAX track item | Lift, release and recapture the shuttle |

Track recipes use six iron ingots, two copper ingots and the family dye. Each car
uses two Create red seats, one iron bars item, four iron ingots, one matching
track item and one native Coasters cart. The recipe book supplies the complete
equipment patterns. The optional Track Styles and Engineered mods are not needed
to obtain these items.

## Build a circuit

1. Place two native Coasters anchors in a clear area. Use the matching family
   track item on the first anchor and then the second. Sneak-click cancels the
   first selection. Use the native Coasters tools to shape the connection.
   Survival placement consumes one matching track item for every four blocks
   of sampled curve length, rounded up for each connection: a 32-block straight
   costs eight items. Have the required items in your inventory before the second
   click. Anchor endpoints must be distinct and no more than 64 blocks apart.
2. Connect the rest of the route with the same family. Leave room for the entire
   car, rotating carriers and riders; a wheel-sized corridor is insufficient.
3. Use the matching car item on a track anchor to assemble the car. Carriers
   begin captured. Wait for them to settle before boarding.
4. Use the Rotation Tool on a section's anchor to edit it. After changing native
   track geometry, rebind the profile before operating. Use the tool on a car
   to control capture and restraints; while seated, use it to open that car's
   editor.
5. Board with empty hands while the car is stopped, all carriers are aligned and
   restraints are open. Close restraints before dispatch. Capture and wait for
   readiness before opening restraints and dismounting onto a platform.

Create drive power or the native chain system moves the chassis on a circuit.
The addon controls carrier rotation separately. Seats rotate around their hinge
while following the chassis; changing a decorative style does not change that
motion.

Current builds require horizontal coordinates within ±1024 and available physics
chunks. The per-dimension limits are 256 track sections, 64 vehicles, 8192 profile
keys and 512 magnetic spans, with at most eight spans on one section. These are
tested bounds, not general-world or unlimited-layout support.

## Family walkthroughs

| Family | Matching car and operating exercise |
| --- | --- |
| Controlled Arrow/S&S | The four-seat car has two driven wing carriers. Edit section keys as `distance:degrees[:slope]`, separated by semicolons. Angles stay unwrapped, so a full turn is 360 degrees rather than zero. Capture, board, close restraints and dispatch around the original powered circuit. |
| Legacy ZacSpin | The eight-seat car has two free carriers with back-to-back seating. Tune damping and ballast on the car; section rotation keys do not drive its carriers. Rider distribution changes the free response. Capture both groups before unloading. |
| S&S-inspired Free Spin | Four independent paired carriers use free dynamics. Install inducers or dampers on matching track and edit each span with the Rotation Tool. Compare spin before and after the zone. A damper slows rotation; the capture system supplies the boarding lock. |
| Revised free rotor | Four independent paired carriers have lower hinges and a different chassis layout. Run it on Revised Free-Rotor Track and compare the groups' responses with different rider loading. This is an inspired variant, not a claim of exact manufacturer coupling. |
| PAX-inspired shuttle | Assemble and couple four matching cars, six blocks apart, between two spikes. Bind a nearby winch, supply Create shaft rotation, board and close restraints. Start the winch and follow lift → outbound → returning → capturing → boarding. Open restraints only after final capture and readiness. |

The ordinary boarding station binds a nearby non-PAX car when used empty-handed.
Power at its rear requests capture; releasing power requests dispatch. Its front
indicates readiness and its right side indicates closed restraints. Binding the
station does not supply propulsion. Binding searches within twelve blocks;
capture, dispatch and the status outputs operate when the car center is within
eight blocks. Position the station beside a clear boarding platform outside the
carriers' swept envelope. PAX uses the winch's cycle instead of the
station's normal redstone dispatch.

For PAX, build an unbranched matching route with two open ends. Both spike
samples, three blocks inward from their ends, must be at least ten blocks above
the station track. The station tangent must be nearly level. Leave at least
25 blocks of route before the lead car and at least `6 × (cars − 1) + 20` blocks
after it, measured along the route. You can operate one to four matching cars;
the four-car walkthrough needs at least 38 blocks after the lead car. A flat
straight section can hold a car but cannot bind as a working shuttle route.

Place the winch within twelve blocks of a stopped train and sneak-use it
to bind. An operator uses the winch to start the cycle. After an interrupted lift,
restore power and inspect the route, then use the winch normally to resume.
Restrained riders need an operator on the platform. Keep the winch, its supports
and the complete route available during operation. See the more detailed
[shuttle workflow](REVISED-SHUTTLE.md) and [magnetic editor conventions](MAGNETIC.md).

## Shipped example layouts

In a disposable creative world with commands enabled, stand in a large clear
area inside the supported coordinate bounds. `/fd demo arrow`, `/fd demo legacy`,
`/fd demo free_spin` and `/fd demo revised` build powered circuits near the
operator. `/fd demo pax` and `/fd demo pax_tall` build shuttle routes, platforms,
winches and Create creative motors. The PAX example finishes its native couplings
after four seconds. Place separate examples far enough apart that their swept
corridors cannot intersect.

These commands create world content; they are examples rather than the survival
acquisition path. They use the standalone dependencies. Follow the corresponding
family exercise above to board, dispatch, capture and unload through normal
controls. The PAX examples are original two-spike game layouts, not replicas of
PAX's real loop geometry. The existing powered-layout tests and packaged
five-family observations are recorded under the bounded 0.3 gate.

## In-game Ponder tutorials

Hover any Fourth Dimension item in an inventory and hold the Ponder key
(W by default). Every one of the fifteen craftable items has lessons. Use the
on-screen next/previous controls to move between scenes, and replay to review a
step. The matching lesson explains all five families and the ordinary-cart
exception. Track and car items also teach anchor construction and the relevant
carrier mechanism. The Rotation Tool, magnetic equipment, boarding station and
winch expose the lessons for their workflows.

Nine original scenes cover matching, construction, controlled Arrow, legacy
free rotation, magnetic Free Spin, revised carriers, magnetic spans, boarding,
and PAX catch/lift/release/return. Colored strips and the compact shuttle route
are diagrams, not native track blocks to copy into a world. Their animations
explain the controls and sequence; actual free spin depends on physics and rider
loading. Use the family walkthroughs and playable examples for a real layout.

For a packaged development client with English selected,
`./gradlew runClient -PfdPackaged -PfdPonderValidation` runs an opt-in check after
joining a world. It compiles and plays every item registration, replays restored
scenes, checks the English translations and repeats after a completed resource
reload. `FD_PONDER passed` is client playback evidence; visual readability and
normal shortcut access are checked separately.

## Ordinary carts and recovery

Ordinary access is conditional on a verified adapter fitting the main running
rails and surrounding mechanism. Current evidence certifies only the unmodified
bare native bearing. It receives no carrier mechanism, rotation profile or
magnetic seat assistance. It does not acquire 4D benefits simply by attaching
to a 4D track.

Arrow cars require controlled Arrow track even during recovery. Disabled 4D cars
also remain on their matching family track. Dev.15 permits guarded mixed recovery
consists with a selected certified bare bearing, retained carriers and closed
restraints. It supplies separate start/brake/disconnect controls and a stationary
PAX winch handover. Follow the [recovery guide](RECOVERY.md). Native Arrow-to-Arrow
and PAX-to-PAX coupling remain separate mechanisms. The earlier
[mechanical bench](evidence/0.4/recovery/mechanical-acceptance.md) established flat
feasibility. Full real-client, curved/banked, strength and lifecycle recovery
acceptance remains open under FD402.

This guide, item guidance, recipe discovery and the Ponder lessons progress
FD403. Complete survival building/operating exercises and the full 0.4 gate
through a real client remain unfinished. Native survival crafting/placement has
[separate bounded evidence](evidence/0.4/survival-acceptance.md). See the
[Ponder checkpoint](evidence/0.4/ponder-acceptance.md)
for the exact packaged-client and native-resource checks.
