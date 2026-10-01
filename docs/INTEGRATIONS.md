# Optional integrations: development and acceptance

This branch implements FD401/FD403 integration work; FD404 and the FD901–FD904
release gates remain open. Bounded FD305 evidence has been reconciled from
`801cc4d` and `5ebe6fd` (local cherry-picks `f71e131` and `31a759d`). The committed
schema-3 → 4 integrated-world verifier passes, and both occupied revised/PAX
SavedData pairs compare exactly across frozen cold restart. This does not certify
the later schema-5 optional-addon lifecycle matrix or resolve the recorded client
resource exit and transient movement-packet investigation.

Recorded checks: [family gate](evidence/0.3/README.md),
[foundation/Create/Engineered](evidence/integrations/2026-09-30/README.md),
[Track Styles](evidence/integrations/2026-09-30/styles-1.2.0/README.md), and
[Additions](evidence/integrations/2026-09-30/additions-0.2.0/README.md), and
[Magnetized](evidence/integrations/2026-09-30/magnetized-1.1.0/README.md).

## Implemented foundation

- Functional track providers register before worlds load and must declare a matching
  family, variant, contacts, mechanisms and clearance. Native curve resolution passes
  through this registry. The original provider remains built in.
- Schema 5 separates functional provider from appearance provider/style. Schemas 1–4
  migrate on read. Unknown functional providers retain their saved IDs, profiles,
  magnets and revisions, but cannot resolve or authorize travel. Their absence does not
  invalidate the stored geometry. Unknown appearances retain their saved selection and
  fall back to our complete original mesh. Track Styles 1.2.0 currently offers two
  optional Arrow spine appearances.
- Component ownership explicitly covers spine, ties, running rails, control rails and
  magnetic equipment. Only the spine can currently belong to the Track Styles
  resource adapter; all other components remain with our original renderer.
- Optional adapters and mixins check mod presence and exact inspected versions before
  resolving external classes. Dependencies are compile-only unless a development
  integration profile is selected. No optional JAR is included in our artifact.
- Immutable telemetry reports whole-consist occupancy/capacity, capture, boarding
  alignment, restraints, maintenance retention, dispatch eligibility/denial, faults and
  shuttle phase. It contains no passenger identities.
- Dispatch telemetry and dispatch execution share the same preflight. Block-controller
  requests verify the actual binding and station distance; PAX uses its winch state
  machine. Telemetry is advisory and never bypasses a fresh operation check.

## Create

Boarding Stations and PAX Shuttle Winches expose goggles diagnostics and six Display
Link sources: ride status, occupancy, restraints, dispatch readiness, phase and fault.
Station snapshots refresh every five ticks and winch snapshots every ten; changes are
synchronized to tracking clients. The existing front ready output still means boarding
alignment, independently of restraints and dispatch eligibility.

Hold the Ponder key over family tracks/cars, the Rotation Tool, Boarding Station,
Shuttle Winch or magnetic equipment. Five original schematic family scenes and a station
scene illustrate carrier grouping, matching tracks, editing, capture, restraints,
magnetic equipment, and shuttle phases. Animation illustrates operation; it is not
native physics evidence. Structures are reproducible with
`python scripts/generate-ponder-scenes.py`.

## Engineered 1.1.1

Pinned artifact: `curse.maven:coasters-engineered-1643631:8863559`.

Two narrow mixins target the inspected motor entry and mass calculation:

- Held, unrestrained, retained or faulted 4D consists cannot be driven by the motor.
  PAX remains excluded from external motor propulsion throughout its lifecycle.
- Engineered's native rivet accounting is augmented with our independent carrier
  bodies, including their declared loading, without counting bodies already included.
  Missing or unsettled carriers produce zero available drive mass.

The original Arrow, legacy ZacSpin and Free Spin demonstration circuits can use
Engineered equipment in place of native chain drives:

```text
/execute positioned -800 147 -712 run fd demo engineered arrow
/execute positioned -650 147 -712 run fd demo engineered legacy
/execute positioned -500 147 -712 run fd demo engineered free_spin
```

Use an empty test world. Each example uses the existing 47 × 17 × 69 reserved volume.
Close restraints, then pulse the station's rear input. The same input advances the
nearby Engineered controller; its falling edge requests validated dispatch. An early
motor launch command cannot release the held car. After departure, power the input
again to request return capture. Reset the first motor controller before another cycle.
The occupied single-car native fixtures pass for all three listed families. These
remain development fixtures while the wider compatibility matrix is completed.

Build and run:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PfdIntegrations=engineered
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runClient -PfdIntegrations=engineered
```

The optional native test explicitly logs a skip when Engineered 1.1.1 is absent.
That skip is not compatibility evidence. It exercises occupied circuits, the real
observer, controller STOP-to-LAUNCH transition, carrier mass, rejected early dispatch,
and station return capture. Braking distances and uphill launch speeds remain physical
layout constraints.

For simultaneous builds and packaged-client QA, copy the built JAR to an immutable
temporary path and pass `-PfdPackaged -PfdPackagedJar=/absolute/path/to/snapshot.jar`.
Do not replace the archive underneath a running client.

## Track Styles 1.2.0 spine adapter

The September 30 release notes explicitly add Arrow 4D single/double-spine styles,
despite the main description still listing Arrow 4D as future work.
[Released artifact and notes](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles/files/9020601).

The inspected artifact is `curse.maven:coasters-simulated-track-styles-1642719:9020601`,
SHA-256 `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38`.
Its model registry exposes center beam, tie, left rail and right rail components.
Both Arrow 4D side-rail models contain main and static control-rail geometry together.
They do not establish a profile-driven control-rail interface. A full-model overlay
would duplicate our moving rails; render ownership must be resolved before enabling it.

The development adapter offers **Arrow single spine** and **Arrow double spine**
through the Rotation Tool's **Change appearance** button. Hover for the next choice.
Selection applies to one complete, unoccupied Arrow section and uses the same reach,
build-permission, adjacent-occupancy and revision checks as the profile editor. Other
families keep their original appearance. This does not integrate Track Styles' own
paint/tool options or change its behavior on ordinary Coasters tracks.

Only the separately installed center-beam model and textures are read at runtime.
Our renderer bends the imported vertices along the section, removes internal end
caps, and keeps the spine centered on our support line. Our original ties, running
rails, profile-driven control rails and magnetic equipment remain authoritative.
Different styles meet at section boundaries; there is no within-section style split.

The adapter checks exactly version 1.2.0. Removing it retains the saved style choice
while rendering the entire original family mesh; reinstalling restores that choice.
Missing or empty model resources also fall back to the original spine. Resource reloads
invalidate cached meshes through baked-model identity, and deleted tracks release
their cached mesh. Functional identity, profile, magnets, clearance and eligibility
never derive from the appearance.

Run with `-PfdIntegrations=styles`, or `-PfdIntegrations=engineered,styles` for both.
The 1.2.0 artifact emits client-class mixin warnings on the dedicated server and
invalid resource-path warnings on the client. Native startup/testing proceeds;
these warnings are not a clean-startup certification.

The project page reports MIT while the released metadata still says All Rights Reserved.
No implementation, models or textures were copied into our source or output. Further
work must use independently authored runtime integration and separately installed assets,
with applicable terms resolved before distribution. See the updated dependency audit.
FD801 covers a future functional adapter beyond these cosmetic spines. It is now
actionable investigation, not blocked on the existence of an Arrow 4D release;
mechanical equivalence remains unverified.

## Additions 0.2.0: station and fin adapter

The optional development profile `-PfdIntegrations=additions` installs the exact
[Additions 0.2.0 artifact](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions/files/8772429),
[Aeronautics bundled 1.3.2](https://www.curseforge.com/minecraft/mc-mods/create-aeronautics/files/8763471)
(including Simulated and Offroad 1.3.2), and
[Interiors 0.6.1 v3](https://www.curseforge.com/minecraft/mc-mods/interiors/files/8974633).
This satisfies Additions' Simulated/Interiors dependencies alongside our existing
Create 6.0.10, Coasters 0.1.5 and Sable 2.0.3 baseline. Combine profile names with
commas, for example `-PfdIntegrations=engineered,styles,additions`.

The adapter applies only to Additions 0.2.0. Its station's native span scanner
identifies the complete consist; shared 4D telemetry supplies independent-carrier
capacity, occupancy, restraint/alignment state, faults and readiness. Stations
provide the same six Create Display Link sources and goggle readout as our station.
Snapshots synchronize to clients but are not persisted as operation authority.

For a 4D consist, wireless channels mean:

| Channel | Meaning |
| --- | --- |
| Occupied | 15 while held in the scanned station; drops to 0 only after successful automatic dispatch. |
| Weight | 1–15 using actual occupied seats divided by total carrier capacity; 1 for an empty car. |
| Bars | 15 when every car's restraints are closed, otherwise 0. This reports actual restraints rather than the native early-release timer. |
| Pre-station | Retains the native waiting-train signal until the station clears. |

Enable auto-dispatch to capture an arriving non-PAX consist. The station waits for
alignment, closed restraints, released maintenance retention, no fault, its weight
threshold and arrival cooldown, then a continuous ready delay. Capture and dispatch
call the shared server-validated operations interface. Authority exists only during
that station's fresh native span scan; cached state or a remote request cannot reuse
it. An ambiguous/mixed span cannot dispatch. A successful departure remains latched
until the span empties, avoiding repeated capture of the departing car.

PAX diagnostics remain available, but automatic capture/dispatch is denied with a
winch instruction. Its catch and shuttle phases retain control. This is an explicit
unsupported propulsion combination, not PAX interoperability certification.

Additions fins cannot propel a held, unrestrained, retained or faulted 4D consist,
or any PAX car. For an eligible 4D car, velocity increments reach only its chassis
and registered carrier bodies, preserving each body's angular velocity. Ordinary
carts' proximity-based coupling cannot apply those increments to nearby 4D bodies.
No Additions seat replacement or sensor adapter is included.

Native checks and exact limits are recorded in [Additions evidence](evidence/integrations/2026-09-30/additions-0.2.0/README.md); runtime warnings from this dependency
stack prevent claiming warning-free dedicated startup. Linked trains, rollback,
fin speed-control mode, moving track, restart/removal, multiplayer and visual
acceptance remain open. See the integration evidence directory for exact completed
checks before advertising any combination.

## Magnetized 1.1.0: guarded acceleration and brakes

The `-PfdIntegrations=magnetized` profile installs the exact
[1.1.0 artifact](https://www.curseforge.com/minecraft/mc-mods/coasters-magnetized/files/8668703),
`curse.maven:coasters-magnetized-1642979:8668703`. It can be combined with
`engineered,styles,additions`. Its runtime adapter checks exactly version 1.1.0.

Magnetized injects propulsion into Coasters' chain-drive method. A whole-method
wrapper applies our consist interlocks before any injected drive callback runs,
independent of injection order. Held, unrestrained, retained or faulted 4D consists
cannot be driven. PAX remains under winch control throughout its shuttle cycle;
Magnetized propulsion is deliberately excluded even after the mechanical catch
releases. Ordinary Coasters cars continue through the original method.

Powered MAGNET anchors accelerate in the car's existing direction of travel;
BRAKE anchors decelerate toward rest. This artifact needs initial motion to select
a direction and does not launch a stationary car. The native fixture supplies
that initial motion explicitly. Its four non-PAX families accelerate forward and
backward, brake, retain riders and recapture; Free Spin's independent seat-magnet
selection continues to apply to its selected carrier only. Equipment strength and
braking distance remain layout constraints. See the exact native measurements in
[Magnetized evidence](evidence/integrations/2026-09-30/magnetized-1.1.0/README.md).

Functional family, control profiles and seat magnets remain ours. The addon saves
its anchor equipment separately; no rail/provider identity is rewritten. Our
renderer retains the running rails, ties, spine and mechanical equipment, with
Track Styles optionally owning the Arrow spine as above. Magnetized's replacement
rail models target the base Coasters renderer and are not imported into our mesh.
Client equipment visibility, linked trains, curves/slopes, configuration extremes,
restart/removal and multiplayer still need acceptance before advertising support.

## Remaining acceptance

- Extend reconciled FD305 evidence to the schema-5 optional-addon lifecycle matrix;
  investigate the recorded client resource exit and transient movement-packet messages.
- Finish Engineered rollback, linked-train, optional FE, lifecycle and multiplayer
  checks; PAX propulsion stays excluded until separately proven.
- Extend the packaged Ponder/goggles visual checks to every animation phase and both
  multiplayer clients; verify live Display Link output in those sessions.
- Finish Track Styles multiplayer, removal/reinstallation, resource replacement,
  transition and performance evidence; broader appearances and its own tool integration
  remain outside the current two-spine adapter.
- Finish Additions linked/mixed trains, fin modes, lifecycle and multiplayer acceptance;
  validate any rotating-seat replacement separately.
- Extend Magnetized native straight-track evidence to linked trains, curves/slopes,
  equipment visuals, configuration extremes, lifecycle and multiplayer.
- Validate Immersion camera composition, inversion, mouse look, dismount and comfort
  behavior with its full dependency stack.
- Run base, individual-addon and combined-pack dedicated-server/two-client/late-join
  matrices, migrations/removal, interruption cases and measured rendering/performance.
  Publish exact tested versions and evidence, not inferred compatibility.
