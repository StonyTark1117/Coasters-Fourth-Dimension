# Building and operating original moving track

The eight moving-track controllers use real native Coasters beds and fixed docking
interfaces. A controller's construction panel chooses one original car configuration,
a straight 16, 32 or 48 block bed, a world origin, a cardinal direction, and one finite
program. Vehicle paint, skins and body variants do not change compatibility.

Supply at least 16 actual RPM at the stationary input. Each controller uses 16
Create stress units per RPM (1024 at 64 RPM); an overstressed input counts as
unpowered. Higher RPM does not increase the selected program's speed limit.

This is the player procedure for the accepted finite 1.5 implementation. The
[integration record](evidence/1.5/integration.md) maps construction, occupied motion,
recovery and measured limits. Maximum-size capture/Move validation can pause the
server while it checks the full geometry; capacity does not promise smoothness.

## Build a working assembly

1. Craft and place the controller for the intended mechanism. Connect Create kinetic
   power to its rear shaft. The panel reports actual input RPM; an overstressed or
   unpowered drive cannot start motion. Keep the controller outside the moving sweep.
2. Open the controller normally, choose the exact car configuration and program, then
   set the bed origin and cardinal direction. The origin is the center block of the
   moving bed, not the controller position. Leave space for all original riders,
   carriers, moving stays, fixed guides and support columns.
3. Request **Preview**. This server calculation spends nothing. Inspect the full
   moving sweep, fixed machinery, and native socket anchors. Open **Materials** for
   exact item quantities and **Socket positions** for exact anchor block coordinates.
   Nearby ghosts remain visible when closing the panel to place those anchors;
   **Clear preview** removes them. A new selection invalidates the old preview. **Compatibility** explains the selected pairing and its
   restraint, carrier and pitch limits; **Guide** includes a worked example for each
   mechanism.
4. Place ordinary native anchor blocks at every previewed socket. Leave their curves
   unattached when their exact docking geometry changes their normal block position
   or orientation. Clear the full previewed space and load its chunks. A refusal names
   the position or condition to repair; request **Validate** again after repairing it.
5. Carry the exact material bill and choose **Build**. Survival construction consumes
   parts only after native bed creation and placement succeed. Creative construction
   does not spend items and does not create refundable materials. An unsuccessful
   build restores its placements and inventory.
6. After Build, connect fixed approaches and receiving track using the normal matching
   track tool. Registered sockets supply the exact native endpoint and orientation,
   including fractional endpoints for a 30-degree tilt. Do not approximate these by
   rounding track geometry. Remove and rejoin an earlier curve if the panel reports
   that it does not fit the registered socket.
7. Bring the complete supported homogeneous train onto the bed and stop it. Maintain
   two blocks of clear bed beyond both ends. Close occupied restraints, secure every
   articulated carrier, release other propulsion ownership, and choose the named
   destination. **Capture** independently inspects the real train, functional rail,
   fixed approaches, receiving reservation, clearances and measured locks.
8. Choose **Move**. The program proceeds only while its real authority, parts, power,
   restraints, reservation and clearance remain valid. **Release** requires the
   physical bed to have reached and locked at its named interface. Allow the whole
   released train to clear the receiving span before **Return** resets the empty bed.

A matching functional insert is a material item, not a cosmetic setting. Each bed
requires one insert module per length block. Its recipe uses eight iron ingots and
that configuration's normal track item to make eight modules. Bed beams, overhead
supports, Pipeline side-contact beds, guides, pivots, locks and sockets have separate
recipes and an exact construction bill.

## Eight bounded examples

Use a 16-block bed for these initial examples, increasing to 32 or 48 only when the
complete train and both end margins require it. The examples describe normal panel
choices and actual fixed approaches; they do not require setup commands.

| Controller | Example configuration and preset | Fixed layout and operation |
| --- | --- | --- |
| Transfer | Wing, 4-block translation | Two parallel level native approaches, matching wing-compatible inserts, clear transverse guide stroke. Capture a stopped whole train, move, lock and release. |
| Turntable | Wing, port 0 to 90 | Level perpendicular approaches and a clear circular rim. A full show turn is an explicit finite preset; it still ends at a named locked port. |
| Drop | Dive, 4-block descent | Upper and lower matching Dive sockets, with clearance below all seats. Descent is controlled; reset requires power. |
| Tilt | Wing, 30-degree pitch | Level start and the exact angled receiving interface. Build registers the fractional socket geometry before the normal fixed-track connection. Classic Spinning may use 30 degrees but not 90. |
| Elevator | Classic Spinning S1, 4-block rise | Level matching S1 interfaces at both elevations, full-height guide clearance and a held spinning carrier. Other finite rises are 8, 16 and 32 blocks. |
| Teeter | Wing, one cycle | Clear the occupied envelope on both sides of the pitch pivot. The finite cycle returns to its source socket; release only after measured locking. |
| Side-drop | S2 Spinning, 4-block preset | Build the exact translated and lowered named destination shown by the preview. Retain the spinning carrier before the combined translation. |
| Compound | S2 Spinning, port 0 to 90 with 15-degree pitch | Clear the combined yaw-and-pitch sweep and yaw rim. The finite program finishes level at the selected port; its intermediate pitch does not grant arbitrary editable motion. |

Overhead configurations use the matching upper support and original overhead rail;
Pipeline uses its own side-contact support and insert. Its support bed is four blocks below the rail origin, underneath the hanging cabin floor; keep that lower machinery sweep clear. Never substitute an ordinary
under-rail bed merely because a car appears to fit. The server checks all 22 original
configuration identities and their permitted motions. Ordinary native service carts
must remain empty. After their release, use **Dispatch empty service train** with a
real fixed powered Chain span; they do not dispatch automatically. Fourth Dimension
cars use their existing normal vehicle Dispatch control. Every family retains its
existing lower car-count and route limits. Recovery carts, mixed families, custom native bodies, external track,
managed 1.2 circuit trains, and Launch Systems handoffs are outside this package.

## Holds, recovery and removal

Power loss, changed geometry, missing parts, lost receiving space, unsafe restraints,
foreign authority, or lost runtime handles produce a held state. Read the panel's
reason and repair that exact condition. **Resume** and **Return** are explicit guarded
requests, not automatic restart after power comes back. A saved occupied assembly
reloads conservatively and does not silently resume its program.

Use **Evacuate** only after the assembly is stopped and the server can establish the
required safe exits. It does not waive the native body or clearance checks. Do not
remove an occupied controller or its support parts to bypass a hold.

To remove a completed installation, release the train, clear the bed, stop the empty
assembly and use **Dismantle**. It verifies the owned fixed parts and empty native bed,
removes its owned structures, and refunds only the exact materials that this assembly
previously consumed. It refuses occupied or reserved removal.

## Disclosed validation setup helpers

When launched with `fourthdimension.validation=true`, an operator may use
`/fd moving_setup transfer` (or another finite controller ID) to place the controller,
a rear Creative Motor, and the exact native anchor blocks near the operator. It gives
the computed ingredient bill and reports the exact normal panel choices. It never
builds the assembly or performs Capture, Move, Release, boarding or restraint input.
Choose an empty loaded site high enough for the complete machinery before setup.

The normal player then previews, inspects the bill and socket list, and builds.
For the bounded Wing transfer client fixture, `/fd moving_connect` creates the
fixed approach curves after Build, and `/fd moving_spawn_wing` creates one native
Wing after the source has physically locked. These are explicitly setup fixtures,
not evidence of normal track-tool construction or normal car placement. Board and
operate all ride controls normally afterward. Other mechanism examples use normal
track construction. `/fd moving_inspect` records the saved state, operator snapshot,
real body poses, native plot coordinates, seat transforms and occupants without
changing the ride.


Pipeline beds use a low side-contact frame plus two connected end portals. The
server material list includes 38 portal braces. Keep the outboard uprights and
upper headers clear; they retain the native anchor blocks as part of the same
moving structure without putting a floor through the hanging cabin. The **Server
message** button opens the full latest validation/error text when the small panel
summary does not fit.
