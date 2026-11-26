# 1.5 moving-track design and compatibility plan

Adopted 2026-11-22 (America/Phoenix), for **FD2001–FD2007**. This is a
implemented finite contract, accepted 2026-11-26; the
[integration record](../evidence/1.5/integration.md) maps its actual evidence and limits.
[ROADMAP.md](../../ROADMAP.md#fd2007) owns scope and status;
the [execution guide](../roadmap/moving-track.md) owns procedures and the
[current queue](../CURRENT-QUALIFICATION.md#current-work-queue) owns assignments.
The four 1.4 configurations are accepted at `f85f4a27`. The human authorized
implementation through 1.5 on 2026-11-22; FD2001–FD2007 are complete.
The linked evidence, rather than the design text alone, establishes acceptance.

## Real-coaster basis

References were checked on 2026-11-22. Separate installed ride examples from
manufacturer concepts. The game dimensions, compatibility choices and controls
below are original design decisions; a real coaster is not evidence that our
physics or a different manufacturer's vehicle supports the same mechanism.
Use original models and textures, not copied ride assets.

| Mechanism | Reference and supported observation | Application in Fourth Dimension |
| --- | --- | --- |
| Transfer platform | [Coasterpedia's transfer-track survey](https://coasterpedia.net/wiki/Transfer_track) illustrates sliding transfer on Mega-Lite and describes train storage/maintenance use, including Olympia Looping. | Move a fully captured train sideways to a selected parallel siding; manual operation and a service example, without automatic depot scheduling. Occupied operation is our additional game requirement. |
| Turntable | [Voltron's RCDB record](https://rcdb.com/7121.htm?p=0) lists a turntable; [Intamin's catalog](https://www.intamin.com/product/multi-dimension-coaster/) describes a full-turn table with up to four access points. | A retained train changes heading independently of any spinning seat; reserve one selected exit before disconnecting. |
| Drop track | [Alton Towers' TH13TEEN description](https://www.altontowers.com/explore/theme-park/rides-attractions/th13teen/) describes a vertical moving-track drop followed by reverse travel. | Separate the vertical drop/brake/reset cycle from the later forward or reverse departure. A Dive car's ordinary fixed-track hold is not this mechanism. |
| Tilt track | [Gravity Max on Coasterpedia](https://coasterpedia.net/wiki/Gravity_Max) identifies its 90-degree tilt section; [Vekoma](https://www.vekoma.com/tilt-coasters) explicitly pairs its tilt element with sit-down thrill coasters. | Capture on a horizontal deck, pitch the deck, prove the exit lock, then release. Do not infer that every car can accept a vertical endpoint. |
| Elevator | [Cobra's Curse construction record from PCL](https://www.pcl.com/ca/en/our-work/cobra-s-curse-at-busch-gardens-tampa-bay) documents the coaster vehicle being raised by an elevator. | A controlled lift between level stops, with an occupied Classic Spinning example; lifting is a distinct cycle from dropping. |
| Teeter-totter | [Intamin's Multi Dimension catalog](https://www.intamin.com/product/multi-dimension-coaster/) describes a rocking tilt element. This reference is a product concept, not a claimed installed ride. | A bounded pitch sequence that returns to a locked level exit. |
| Side-drop | [Intamin's Uncharted project](https://www.intamin.com/project/uncharted_portaventura/) documents a side drop and controlled-spinning vehicles. | One prescribed lateral-and-downward path. The deck stays level in our initial design; this is not an assertion about the installation's exact machinery. |
| Compound table | [Intamin's rotating teeter-totter](https://www.intamin.com/product/multi-dimension-coaster/) combines tilt and base rotation; the same catalog distinguishes a three-axis Gyro Table. | Implement yaw plus pitch only, with simultaneous motion. Three-axis gyro motion, bungee lift, Edge Tilt and Side Drop Valley are outside 1.5. |

## Dependency and implementation baseline

Use the inspected project pins in [build.gradle](../../build.gradle),
[gradle.properties](../../gradle.properties) and the
[dependency record](../evidence/0.9/dependencies/README.md): Minecraft 1.21.1,
Java 21, NeoForge 21.1.256, Coasters Simulated 0.1.5, Create 6.0.10-280,
Sable 2.0.6, Flywheel 1.0.6 and the existing Ponder/Registrate stack. These are
the development baseline, not a claim about the latest upstream versions.
Do not upgrade or require another mod merely to adopt this plan.

| Existing seam | What 1.5 must add or preserve |
| --- | --- |
| Coasters graph, anchors, path sampling and cart snapping | Inspect `CoasterPathGraphManager`, `CoasterPathTrackFrame` and snap lifecycle in the pinned artifact. Supply transactional docking and graph invalidation; no traversable edge may bridge moving and fixed track while unlocked. Existing access to sublevel samples does not establish this capability. |
| Sable chassis, child bodies, constraints and riders | Retain actual bodies and attachments across moving support, unloaded chunks and restart. Choose one transform owner; never animate the mesh while leaving physics on the old edge. |
| [TrackContract](../../src/main/java/dev/fourthdimension/contract/TrackContract.java), [NativeTrackFit](../../src/main/java/dev/fourthdimension/contract/NativeTrackFit.java), [VehicleContract](../../src/main/java/dev/fourthdimension/contract/VehicleContract.java) | Add a versioned moving-support capability alongside existing contact, mount, family, variant and clearance eligibility. Preserve ordinary-track restrictions and semantic mechanisms. Equal gauge is insufficient. |
| [TrackDesign](../../src/main/java/dev/fourthdimension/track/TrackDesign.java), [EligibilityHooks](../../src/main/java/dev/fourthdimension/runtime/EligibilityHooks.java), family `*Track` validators | Most added families currently reject sublevel-hosted track explicitly. Introduce a narrow authenticated moving-assembly path; do not delete the fixed-world guard globally. Geometry caches must include assembly/revision/pose identity or invalidate when those change. |
| [RideAuthority](../../src/main/java/dev/fourthdimension/integration/RideAuthority.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java) | Add retained moving-support ownership and a whole-consist handoff. A longitudinal permission token alone neither owns a moving rail nor reserves an endpoint. Keep owner/epoch/sequence rejection and inhibit competing propulsion. |
| [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [ConsistRules](../../src/main/java/dev/fourthdimension/vehicle/ConsistRules.java) | Persist assembly, docking, retained-body and reservation identities. Retain existing family/link limits; add deck-fit admission rather than increasing consist sizes. Migrate old saves with no moving assembly by default. |
| Existing stations, Rotation Tool, operator panel and presentation | Extend their actual server snapshots and permission checks. Keep rider, camera, restraint, appearance and body-model ownership; never create a second independent passenger pose. |

FD2001 must record the actual native hooks and implement any needed bounded
adapter before dependent mechanisms consume it. If a required hook cannot be
made to work, name the blocked criterion and retain the 1.5 task as incomplete;
do not substitute decorative animation or silently remove a planned pairing.

## Assemblies and motion limits

The player builds a purpose-made controller, moving bed, guides/pivot, end locks
and fixed docking sockets, then chooses a functional rail insert. This is a
small catalog of assemblies, not arbitrary coaster track glued to Create
contraptions. Overhead inserts use an overhead gantry; Pipeline uses its own
side-rail bed. Supports and machinery must stay outside the occupied sweep.

One straight, untwisted rail span moves as a unit. Proposed deck lengths are
16, 32 and 48 blocks. One complete homogeneous train may occupy a table, with
at most four cars and never more than its existing family limit. Single-car
families stay single-car. Fit uses measured first-to-last occupied bounds,
couplers and two blocks of longitudinal margin at each end, not car count alone.
Oversize trains are refused before admission; partial trains must never move.
No stations, boarding, coupling, repainting or geometry editing on a moving bed.

These are finite **game-design targets** to implement and measure in FD2001
and the consuming package, not sourced real-world dimensions or existing API
limits. Record any justified revision here before dependent content is built;
changes must retain all eight outcomes and the declared compatibility scope.

| Package | Initial geometry / selectable program | Motion target and endpoint rules |
| --- | --- | --- |
| FD2002 transfer | Two parallel level docking positions; lateral offset 4, 8 or 16 blocks | Up to 2 blocks/s; lock at one position before any rolling movement. |
| FD2003 turntable | Level yaw table; up to four configured ports at 0/90/180/270 degrees | Up to 15 degrees/s; optional full 360-degree retained show turn, with no connection while passing intermediate ports. |
| FD2004 drop | Two level stops, 4/8/12 blocks apart vertically | Drop capped at 4 blocks/s; a defined braking zone and powered reset, not uncontrolled world gravity. |
| FD2005 tilt | Horizontal entry and either a 30-degree or 90-degree downward exit | Up to 15 degrees/s; selected car must support the entire pitch path and exit. Return empty after departure, or retained if the trip is cancelled. |
| FD2006A elevator | Two level stops, 4/8/16/32 blocks apart | Powered up/down at up to 2 blocks/s; no bounce or free-fall program. |
| FD2006B teeter-totter | Pitch between −15 and +15 degrees, one to three cycles, then level | Up to 15 degrees/s; one declared level departure, no release during rocking. |
| FD2006C side-drop | Paired lateral/down offsets of 4/4 or 8/8 blocks; level deck along a prescribed diagonal | Path speed capped at 4 blocks/s, bounded braking and powered return; prove the actual combined sweep. |
| FD2006D compound | Up to four level yaw ports; one program combining the shortest yaw to the selected port (up to 180 degrees) with pitch within ±15 degrees | Up to 15 degrees/s per axis, concurrent during part of the cycle; finish level and locked at the selected port. Use a fixed clockwise tie-break for 180 degrees. No roll/heave axis. |

All programs use bounded acceleration and deceleration. Each package freezes
its acceleration, jerk, stopping-distance, actuator load and lock-tolerance
numbers from the retained native prototype before occupied acceptance; speed
caps alone do not define a safe capture or stop. These measurements are steps
inside existing acceptance, not new gates. Use server tick time and physical
readback, never animation elapsed time as evidence of alignment.

The implemented version-1 program uses a seventh-order rest-to-rest time law.
Level translation has commanded speed/acceleration/jerk caps of 2/1/2 in block
and second units; drop and side-drop use 4/2/4. Each angular coordinate is capped
at 15°/s, 7.5°/s² and 15°/s³. Concurrent compound rotation uses the composed
spatial angular vector; coordinate caps must not be described as independent
physical-vector bounds. Powered return uses the slower level limits.

The native actuator has finite per-axis stiffness/damping/force settings of
4000/160/24 times retained total native mass for translation and
40000/1600/240 times mass for rotation. A stationary controller consumes 16
Create stress units per RPM, requires at least 16 actual RPM and refuses an
overstressed network; 64 RPM therefore consumes 1024 stress units. More RPM
does not bypass the finite program speed. Dock publication requires position
error ≤0.025 blocks, orientation error ≤0.25°, linear speed ≤0.025 blocks/s and
angular speed ≤0.1°/s, plus actual native physical locks.

[Native physical observations](../evidence/1.5/fd2001-native/physical-limits.md)
freeze the measured acceleration, jerk, stopping excursion and load results for
the selected presets. Emergency braking and solver wake transients exceed the
command derivatives; they are disclosed separately, including the drop's
0.289871-block observed stop excursion. The conservative occupied sweep and
measured stopped/aligned guards remain authoritative. These are game-physics
qualification measurements, not continuous peak or passenger comfort guarantees.

Core power is Create rotation through a **stationary** mechanism drive input.
The controller supplies bounded capture/feed drive only while docked and owns
the table actuator while retained. Stress/load and speed gearing are finite
and documented. No FE source or Launch Systems installation is required.
Power loss brakes/retains the bed and cars; restoration requires explicit
validated Resume. Native Chain remains on fixed approaches/departures; do not
pretend its shaft or drive span remains connected through a moving gap.

## Track and car compatibility

The table is the **accepted finite 1.5 compatibility contract**; the integration
record distinguishes family-specific checks from shared representative evidence.
`Yes` means implement and qualify that pairing. `30°` permits only the shallow
tilt preset. `—` means explicitly unavailable in 1.5, with a refusal reason;
it does not create another release prerequisite. Every Yes also needs deck fit,
retention readiness, allowed endpoints, route validation and a clear sweep.

Columns: **T** transfer, **R** turntable, **E** elevator, **D** drop,
**P** tilt, **B** teeter-totter, **S** side-drop, **C** compound.
Track names identify functional inserts, not livery or provider appearance.

| Car configuration | Required functional rail insert and fixed endpoints | T/R/E | D | P | B | S | C |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wing | Ordinary Coasters N-contact track, above-rail mount; no functional 4D section | Yes | Yes | Yes | Yes | Yes | Yes |
| Arrow controlled 4D | Arrow four-rail track, including continuous control rails | Yes | Yes | Yes | Yes | Yes | Yes |
| Legacy ZacSpin | Legacy ZacSpin track | Yes | — | — | — | — | — |
| S&S Free Spin | Free Spin track with its magnetic interface | Yes | — | — | — | — | — |
| Revised ZacSpin | Revised ZacSpin track | Yes | — | — | — | — | — |
| PAX shuttle | PAX track and retained shuttle route state | Yes | — | — | — | — | — |
| Classic Spinning (S1) | Classic Spinning track | Yes | Yes | 30° | Yes | Yes | Yes |
| Launched Inverting Spinning (S2) | S2 track and its magnet/drive interfaces | Yes | Yes | Yes | Yes | Yes | Yes |
| Seated Pipeline | Pipeline P-contact side rails, not ordinary N rails | Yes | Yes | Yes | Yes | Yes | Yes |
| Dive | Dive track | Yes | Yes | Yes | Yes | Yes | Yes |
| Suspended swing | Suspended overhead track | Yes | — | — | — | — | — |
| Fixed Inverted | Fixed Inverted overhead track | Yes | Yes | Yes | Yes | Yes | Yes |
| Axis | Axis track with rotation-drive/lift-lock interfaces | Yes | — | — | — | — | — |
| Conventional Flying | Conventional Flying overhead track with posture lock | Yes | — | — | — | — | — |
| New-generation Flying | NG Flying overhead track with posture and yaw contracts | Yes | — | — | — | — | — |
| Powered Inverted Yaw | Powered Inverted overhead track with drive/yaw contracts | Yes | — | — | — | — | — |
| Roller Ball | Roller Ball track and ordered route-section semantics | Yes | — | — | — | — | — |
| Standard standing (1.4) | Standard Standing track | Yes | — | — | — | — | — |
| Surf standing (1.4) | Surf track; distinct from both Standard Standing and seated Pipeline | Yes | — | — | — | — | — |
| Motorbike (1.4) | Motorbike track | Yes | — | — | — | — | — |
| Suspended Flying Bike (1.4) | Flying Bike overhead track and governor route contract | Yes | — | — | — | — | — |
| Native ordinary service cart | Inspected unmodified Coasters chassis on ordinary N-contact track; empty only | Empty only | — | — | — | — | — |

The broad level-motion group supports service/routing and lift use across our
catalog without introducing new combined carrier dynamics. Fast effects and
pitch programs initially target the listed seated configurations. A family
being capable of an inversion elsewhere is not automatic approval for a new
moving-support load, pose, route or restraint combination. Unlisted custom
native contraptions, mixed-family trains and attached recovery carts are refused.

Required family handoffs inside FD2001 and their consumers:

- **Arrow:** preserve local control-rail stroke and profile continuity while
  the assembly moves; do not read changing world coordinates as new track
  progress. Freeze its local seat target during retention, then resume from
  the correct released position. Never detach the control rails from the bed.
- **Spinning, swing, Axis and passive 4D carriers:** capture/damp and positively
  hold each carrier relative to the chassis before moving. Retained table yaw
  must not be confused with seat yaw or free spin. S1/S2 show tables in 1.5
  carry locked seats; independently spinning seats during table motion are out
  of scope. Passive flip/swing/Axis cars receive level-motion support only.
- **Overhead and Flying:** reserve the full below-track rider volume. Keep
  posture locked in the admitted state; NG Flying and Powered Inverted Yaw
  suspend scene targeting and hold yaw. No posture change while retained.
- **Pipeline:** use its distinct gauge and side-rail contacts throughout both
  docks and the bed. A reskinned N-contact rail is never an adapter.
- **Standing/Surf/Motorbike:** keep the existing restraints closed; return Surf
  travel to home and latch it. Do not widen the existing pitch/bank limits or
  allow a 90-degree tilt by calling it a platform animation.
- **PAX, Roller Ball, Powered Inverted Yaw and Flying Bike:** extend their
  actual route/section validators to understand registered dock transitions,
  suspend launch/lift/scene sequencing during capture, and revalidate the
  destination before return to route control. Do not bypass those validators.
  Flying Bike's occupied acceptance depends on its own FD2204 handoff only.

Eligibility is the intersection of provider capability, contact/mount,
family/variant, motion permission, complete-train fit, pose/retention readiness
and valid destination. Source it from one server-owned capability table used by
admission, tooltips, previews, diagnostics and saved-program validation. Unknown
providers/versions or missing capabilities deny admission. Every fixed endpoint
and the bed must preserve the same functional configuration; moving a car never
converts it to another family. Existing 1.3 body choices and liveries keep their
contract and use their measured occupied envelope.

## Optional integrations and exclusions

| Dependency / existing feature | 1.5 boundary |
| --- | --- |
| Track Styles and Coaster Skin | Preserve accepted cosmetic choices on eligible FD/ordinary inserts; one renderer per component and Original fallback. Cosmetic metadata never grants moving support. FD801's future functional Track Styles rail is not a 1.5 prerequisite. |
| Engineered and Magnetized | Keep their already qualified fixed-track consumers. Add an explicit stopped whole-train handoff to/from mechanism control on fixed approach/departure spans, with conflicting force inhibited. Do not place their propulsion hardware on the moving bed or widen their car eligibility. |
| CTNB | Preserve its seven qualified fixed-track materials and existing Wing/Arrow restrictions. CTNB materials on the moving bed are unsupported in 1.5; use a native-material docking span with the same functional configuration and explicit authority handoff. Repainting cannot evade admission. |
| Additions stations / optional seats | Retain existing qualified fixed-platform boarding and restraint telemetry. Moving beds are not boarding stations. Seat substitutions need an existing accepted fit/pose contract and the full table sweep; installation alone grants nothing. |
| Immersion, Flywheel and Veil | Preserve the accepted rider/camera/render ownership and fallback. Qualify only changed moving-transform synchronization/render paths; client effects do not drive server physics. |
| Waterparked and separate Create Coasters | Coexistence only. No slide/boat or Create-train rails/cars on these tables. |
| 1.2 managed multi-train circuit | Its accepted fixed eight-segment Arrow circuit is not a dynamic routing API. Refuse registration of a moving assembly inside that managed route in 1.5. Reserve one train/table and a clear destination on standalone routes; no automatic depot or park-wide scheduler. |
| Launch Systems | **Future FD2008, outside 1.5.** The existing external longitudinal API/0.1 bridge remains accepted within its recorded scope; it does not certify moving-track handoff. Refuse its ownership for moving-segment admission until that future adapter is qualified. |

## How the player learns and operates compatibility

1. **Before crafting/placement:** mechanism and car tooltips show translated
   names, allowed rail inserts and motion badges with words/icons, not color
   alone. Advanced detail identifies the required addon/version where relevant.
   An ordinary native cart explicitly says “Empty service transfer only”.
2. **Before assembly:** the Rotation Tool's **Moving track → Compatibility**
   panel lets the player select a car configuration and mechanism. It explains
   supported motions, required rail/mount, deck length and any required carrier
   lock. Unsupported options remain inspectable with the reason.
3. **Before committing resources:** a ghost preview shows bed, pivot/guide,
   docking arrows, train-length reservation and full swept clearance. Validate
   checks contacts, socket position/tangent/up alignment, matching family,
   loaded destination and obstruction. Refusal spends no materials; edits of
   an occupied or reserved assembly are denied.
4. **Normal operation:** select named destination, Capture, wait for “Whole
   train secured”, then Move. The panel shows owner, car/rail, locks, route
   reservation, power and fault. A normal configured cycle can release only
   after measured docking and all dispatch guards pass. A fault/reload always
   returns held and requires explicit Resume; redstone requests use the same
   guards and cannot bypass them.
5. **Useful failures:** examples include “Surf: level transfer, rotation and
   lift only”, “Classic Spinning: choose 30° tilt”, “Pipeline needs P rails”,
   “Train exceeds 32-block bed”, “Exit B occupied”, “Carrier not locked”, and
   “Launch Systems moving-track adapter is not available”. Explain the action
   to fix a configuration separately from an unsupported feature.
6. **Teaching:** deliver survival recipes, original connected machinery/models,
   a short in-game guide/Ponder scene and a buildable example per mechanism.
   Use Wing for transfer/turntable/tilt, S1 for lift and shallow show motion,
   S2 for side-drop/compound, and Dive for drop. Include one overhead assembly
   and a Pipeline insert example to teach clearance and rail mismatch. Creative
   setup commands aid exploration but cannot establish normal construction.

## State, interruption and recovery contract

`DOCKED → ADMITTING → CAPTURED → RESERVED → DISCONNECTED → MOVING →
ALIGNING → LOCKED → RECONNECTED → RELEASING` is the normal server lifecycle.
Any failed guard enters retained `FAULT_HOLD`. Persist the assembly UUID,
mechanism/insert version, train/body IDs, local pose/velocity, source/destination,
motion phase, locks and authority epoch. Reconstruct reservations by checking
live occupants and endpoints, not by trusting saved release permission.

Capture requires all linked bodies on the bed, zero rolling motion, closed
restraints, family carrier/posture locks and a valid destination. At the locked
dock, stop admission and reserve the outgoing receiving space before removing
connections. Pose composition is assembly → chassis → carrier/posture → seat;
rendering/interpolation and rider/camera synchronization consume that same state.
Release reserves sufficient clear fixed track for the complete train. Invalid
or unloaded exits, a second claimant, an overhanging car or unsupported owner
leave the train held and other approaches unavailable.

On power loss, endpoint removal, controller replacement, missing body, obstruction
or chunk loss, retain the train without a stale graph connection. Persist a
recoverable stopped pose; use no automatic chunk loading or teleport-to-dock
shortcut. Reload must not duplicate cars, riders, graph edges or reservations.
Offer repair/revalidate and controlled return to a valid dock. If no dock can
be restored, use an explicitly designed stationary evacuation/service access
with normal restraint and safe-exit checks; never release riders into the sweep.
Occupied breaking/disassembly is refused. FD2001 must prove a viable recovery
path, not merely retain passengers indefinitely.

## Delivery order and acceptance

All eight mechanisms belong to **1.5**, reconciled by **FD2007**. Feasibility is
the first implementation step, not a later decision about which release owns
them. There are no speculative FD2007 suffix releases. Publication remains a
separate action; no 1.0–1.4 accepted case is reopened by this planning change.

| Step | Complete outcome and evidence |
| --- | --- |
| FD2001 | Freeze native hook inventory, capability schema, limits and authority/state ownership. Retain an occupied Wing translation prototype plus native wrong-track/oversize/reservation/power-loss/restart/endpoint-loss recovery. Prove the changed occupied transform path early, including a second real client for new synchronization. Integrate family retention/route adapters before their consumers; use distinct Arrow-control, overhead, Pipeline and 1.4 state cases where necessary. |
| FD2002–FD2005 | Build the four base mechanisms from that integrated contract, each with original content, recipes, normal controls, compatibility readback, example and its distinct motion/interruption acceptance. Translation, yaw, drop and pitch each require actual physical/native proof and occupied behavior; one axis cannot stand in for another. |
| FD2006A–D | Elevator consumes drop retention; teeter-totter consumes tilt; side-drop consumes transfer/drop; compound consumes yaw/tilt. Prove the complete programmed cycle, combined sweep when applicable, and its distinct interrupted recovery. Independent component checks alone cannot close side-drop or compound. |
| FD2006 | Reconcile the four named additional styles, reusing closed child cases. |
| FD2007 | Reconcile all eight mechanisms, the declared car/track matrix, player compatibility guidance, optional handoffs/rejections, save migration, packaging and measured workload. Record finite supported limits and verify a maximum admitted train, denial of one beyond the limit, and two independent tables contending for one exit. Reuse the mechanism evidence; do not replay an entire family × mechanism × addon product. |

Use native tests for every distinct admission/pose/route/retention behavior and
boundary, representative connected clients for common UI/physics/render paths,
and additional occupied checks where a family has a genuinely different
mount/carrier/restraint path. Reuse the accepted 1.4 handoffs; this document
does not extend their fixed-track evidence to moving support. Acceptance includes honest
unsupported-pair feedback, not just positive examples. Follow
[VALIDATION-POLICY.md](../VALIDATION-POLICY.md): one passing affected check closes
that case, aggregate review does not require a fresh run, and owned runtimes
must be saved/stopped before completion.

## Future Launch Systems compatibility

[FD2008](../../ROADMAP.md#fd2008) is a separate future task with no release
number or 1.5 dependency. It will use the
[already integrated external authority contract](../evidence/launch-systems-authority-20261122/README.md)
and the accepted moving-segment contract to specify eligible controllers,
cars, approach/exit spans, retained handoff, power loss, stale-token rejection,
restart and missing-mod behavior. It must prove actual consumer force and
occupied handoff in both projects before advertising support. Do not add its
launch presets, FE/network assumptions or specialty/multi-train expansion to
FD2001–FD2007. Existing fixed-track bridge evidence remains unchanged.
