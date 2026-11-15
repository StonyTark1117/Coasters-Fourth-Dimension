# FD1901S — Powered Inverted Yaw specification

**Specification complete; implementation remains FD1901A–C / FD1902.** Version 1,
2026-11-14 America/Phoenix. Normative [conventions](specification-conventions.md)
and [shared yaw contract](shared-yaw-contract.md).

## Reference and fixed upright configuration

[MACK's primary Inverted Powered description](https://www.mack-rides.com/products/inverted-powered-coaster)
confirms below-track powered forward/backward travel, full-turn vehicle rotation,
and lighting/audio features. Our two-seat per-car configuration, dimensions,
limits, finite events and kinetic drive are original game choices. Riders remain
seated upright **relative to their carrier**. There is no prone changeover and no
free lateral Suspended swing. Yaw physically rotates each car's carrier; it is
not a camera-look or entire-consist rotation effect.

| Item | Game specification |
| --- | --- |
| Variant / track | `powered_inverted / upright_yaw_v1`; dedicated overhead N contacts, yaw clearance, powered-drive and stop/capture capability |
| Pivot / transform | `(0,-3.0,0)`, chassis local Y; `B*T(P)*Ry(psi)*seatLocal`; phi identically0, no posture joint |
| Seats | Two forward-facing at `(±.50,0,0)` relative pivot, body restraint/foot support; one independent physical yaw carrier per car |
| Closed occupied bound | X=[-1,1], Y=[-1.25,1.50], Z=[-.60,.70]; horizontal rotation radius≤sqrt(1²+.7²)=1.22066 |
| Whole envelope | half-width1.50, aboveO1.10, belowO4.50, half-length1.75; slender overhead spindle and frame clear the rotating box |
| Consist / travel | One to four identical cars, center distance6; one complete homogeneous train per declared circuit; radius≥12, bank≤45°, slope≤30°, no inversion; speed≤8 |
| Yaw | Shared [-8π,+8π] unwrapped window; full turns, deliberate unwind; |omega|≤.6, |alpha|≤.8; physical position servo, no gravity-free-spin mode |
| Stops / dock | Editable canonical stops and speed sections; travel acceleration/deceleration≤2 blocks/s²; dwell20–1200 ticks; level dock≥28 for four cars |

Below-track N support is reused from Fixed Inverted; the top occupant bound Y=-1.50
clears the measured native -.70 underbrace by .80. Original scenery/supports stay
outside the swept cylinder and full consist reservation. Ordinary track lacks the
powered-drive/stop/yaw capability even if running gear matches. A color or external
appearance provider is insufficient for acceptance.

## Working standalone drive and travel authority

The standalone provider is **native Coasters Create-powered Chain** over the
complete powered route, supplied by normal kinetic sources. Its actual pinned
`applySpeedMatch` primitive and original-family operation are retained evidence;
this vehicle's speed-section/dwell/reverse controller is FD1901A implementation.
Provide complete drive coverage through every declared moving segment; gap/drive
unavailability is a visible hold/fault, not an unpowered magic speed assignment.
At show stops the shared passive brake holds the native chassis. This is an
original kinetic substitute for MACK's onboard powered system, clearly taught.
No Engineered, Magnetized or other optional adapter is mandatory. Optional providers
can be selected only through separately accepted capability adapters.

FD1701A is the **one** authority for requested speed, direction, stop and resume.
Acceleration-limited requests operate the real Create kinetic source through an
interlocked, stress-supplied drive governor (speed control plus clutch/reversal),
with native anchor-speed readback. The native matching call reads that source;
its final argument is time-step, not requested speed. Yaw/scene commands
cannot call speed matching. A reverse request first decelerates to |v|≤.05 for five
ticks, validates the complete route/consist and starts the same provider in the
opposite canonical direction. Native reverse-drive and provider sign are measured
in the implementation prototype; a display label alone does not count.

Travel phases: `DOCK_OPEN → RESTRAINED_CAPTURED → RUN → STOP_APPROACH → HELD_DWELL
→ RESUME/REVERSE → RUN → DOCK_UNWIND_CAPTURE → OPEN`, with independent shared yaw
modes. Editable speeds/stops/dwell/program bindings require empty, stopped,
authorized editing. No arbitrary live occupied edits or park-wide multi-train
management is required. Dwell ticks begin only after the native travel hold readback.
A completed yaw scene cannot auto-open restraints or release a faulted drive.

## Yaw, events, faults and saved state

Per-car PROFILE/TARGET/BLEND/SHOW_STOP and station winding/capture follow the shared
yaw contract. Each car evaluates its **own** canonical route position and physical
pivot, with its own winding state. Lead-car distance cannot substitute for a trailing
car's scenery projection. Authored programs retrace on reversal; a target mode
recomputes from current pose; show-stop yaw does not advance distance profiles.

Finite scene events: `SECTION_ENTER`, `STOP_HELD`, `DWELL_COMPLETE`, `YAW_REACHED`,
`RIDE_FAULT`, `DOCK_CAPTURED`. Each includes ride/car identity where applicable,
section/event ID, server tick, authority epoch and monotonic sequence. Standalone
output is a bounded redstone pulse from the bound controller plus an optional
registered sound cue; existing light/audio adapters consume these validated events.
No arbitrary commands/scripts or downloaded sound assets. At most one event per
transition per car, with a20-tick minimum repeat interval for oscillating boundary
reentry; power restore does not replay old scene events. Event consumers cannot
change travel/restraints directly.

Power/provider loss brakes travel and yaw toward held pose, retains restraints
and freezes dwell completion. Removed controller or stale authority cannot release
holds. Missing marker/degenerate projection/winding exhaustion follow shared
bounded fallback and visible reason. A failed station unwind leaves the car stopped
and restrained; zero modulo2π does not erase stored turns. Coupler/body/route loss
faults the train. Explicit recovery revalidates then resumes; no auto-launch after
reload and no free-swing substitute.

Persist common and shared yaw schema plus selected propulsion provider/binding,
commanded/actual travel direction, speed-section index, next stop, remaining dwell,
hold acknowledgment, scene sequence/dedup state and pending unwind. Late join
receives independent car poses and event state; it does not replay past audio.
Server/native body owns yaw and seat/rider transform; independent look/comfort
remains with the rider. Common camera restoration applies.

## Finite implementation acceptance and original content

C1–C6 plus: native powered forward/reverse/dwell at configured speed; full-turn
per-car yaw, target-facing in motion and held show stop; independent trailing-car
target projection; safe winding/capture; standalone operation with no optional
provider; drive/controller loss and interrupted turn/dwell restart; bounded,
deduplicated scene outputs. The native occupied prototype is one and four cars
on a powered loop with a scene marker, stop, reverse segment and station. Record
engagement, actual signed travel, per-car psi/omega/target, route distance and
interlock/event sequence; then capture/unload normally. Reuse FD1802A's yaw math,
physical-body/network and fallback proof wherever identical.

Content: original overhead N track/powered-route presentation, bogie/spindle,
upright two-seat carrier/restraint, four-car linking guidance, yaw/marker/speed/
stop/dwell controls, finite light/audio event output, car/track/control items and
recipes, readable fault/pose/hold display and one player-buildable powered show
example. Both this distinct vehicle and both Flying variants remain pre-1.0.
