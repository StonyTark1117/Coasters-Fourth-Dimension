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
[Magnetized](evidence/integrations/2026-09-30/magnetized-1.1.0/README.md), and
[Immersion](evidence/integrations/2026-09-30/immersion-0.2.3/README.md), and
[combined two-client Arrow](evidence/integrations/2026-09-30/combined-two-client/README.md), and
[rider view recovery](evidence/integrations/2026-09-30/rider-view-recovery/README.md).

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

The adapter targets the inspected motor entry and mass calculation:

- Held, unrestrained, retained or faulted 4D consists cannot be driven by the motor.
  PAX remains excluded from external motor propulsion throughout its lifecycle.
  Held cars still supply native span/controller and observer presence reports,
  including when the outer Magnetized guard cancels the complete drive.
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
New Free Spin examples brake on a separate elevated straight and keep the passive
spin equipment section powered. A 4-block/s station launch and approach brake keep
captured cars inside the station operating range. Existing saved examples must be
rebuilt or have their native controllers reconfigured; they are not silently changed.
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
layout constraints. A [PAX ownership follow-up](evidence/integrations/2026-09-30/engineered-pax/README.md)
places active Engineered equipment across the occupied four-car shuttle route.
Opposing launch, STOP and TRIM commands remain subordinate to the physical winch
through lift, power-loss hold/resume, gravity return and capture, while the native
controller and observer continue receiving train-presence reports. This bounded
native check does not complete the client/lifecycle or FE-on PAX matrix.

An [FE-powered PAX extension](evidence/integrations/2026-10-01/engineered-pax-fe/README.md)
enables Engineered's energy requirement during an occupied compact shuttle cycle.
Empty, full and partial native FE buffers exercise each controller phase while the
physical winch retains ownership, including its power-loss fault and explicit resume.
The probes require exact energy balances, original rider/carrier attachments and
occupied telemetry. Base, Engineered-only and combined profiles pass all 33 registered
native tests (with explicit optional skips) and 19 unit tests. This is direct capability
input; FE-powered PAX chunk/cold-restart,
human-client and external-energy-network checks remain separate acceptance work.

A separate [FE acceptance fixture](evidence/integrations/2026-09-30/engineered-energy/README.md)
enables the energy requirement for a linked Arrow pair and single legacy, Free Spin
and revised cars. Real anchor capabilities supply FE. Captured cars remain held,
empty buffers prevent launch and report starvation, filling permits forward/reverse
launch, and empty-buffer braking regenerates FE in both directions. All five native
passengers remain mounted through recapture, with the Arrow coupling intact. The
26-test suites pass with Engineered alone, the combined pack and base isolation;
19 unit tests pass. This is straight-track evidence at default energy tuning.
External cables/generators, partial power, configuration extremes, energy persistence,
linked curves/slopes and gravity rollback remain outside this result.

The [chunk persistence follow-up](evidence/integrations/2026-09-30/engineered-energy-reload/README.md)
verifies exact FE, native span and controller sequence restoration after actual
unoccupied equipment chunk eviction, then repeats the occupied operations checks.
It exposed global endpoint sampling and Additions redstone polling that kept idle
4D anchors loaded. Inactive 4D sections now avoid those reads; endpoint filtering
preserves junction topology, and periodic geometry validation waits for ticking
endpoints. All 27 native checks pass in base, five individual-addon and combined
profiles, with explicit optional skips recorded. Occupied chunk interruption and
human-player process restart with FE enabled remain open.

The [limited-energy extension](evidence/integrations/2026-09-30/engineered-limited-energy/README.md)
adds a 100 FE pulse, 5 FE/tick trickle, supply loss and full-refill recovery for the
same five occupied cars. Braking works with regeneration disabled; full regeneration
fills the minimum 1,000 FE buffer exactly without overflow. Normal recapture retains
all passengers and the Arrow coupling. The test restores its energy settings and
passes in Engineered-only and combined packs, with base isolation checked separately.
This is direct capability input, not an external generator/cable test; broader
configuration and occupied lifecycle coverage remains open.

A [packaged occupied FE restart](evidence/integrations/2026-09-30/engineered-occupied-restart/README.md)
now preserves five native pig passengers, fourteen carrier bodies, an Arrow coupling
and four exact FE balances in combined and Engineered-only processes. It exposed a
boarding guard that rejected saved non-player attachments through closed restraints;
the fix recognizes only known saved occupants and valid seats before the entities
join the level. Unknown serialized riders remain rejected, and live boarding retains
its checks. The restored cars launch, regenerate while braking, recapture and unload.
Human-player FE reconnect and occupied chunk interruption remain separate open cases.

A further [connected-route interruption check](evidence/integrations/2026-09-30/connected-track-chunks/README.md)
covers flat and varying-bank routes for all five families. Native bank-chain lookups
now read only resident inactive 4D anchors, preventing the active end of a connected
route from renewing distant chunk tickets. Ten unoccupied routes retain their true
endpoint topology and all sixty geometry fingerprints through partial eviction and
reload. The fixture explicitly flushes native pending saves before asserting actual
block-entity removal; it is not an automatic-eviction timing or occupied-interruption
claim. All 28 registered native checks pass across the seven profiles, with optional
skips recorded, alongside 19 unit tests.

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
stack prevent claiming warning-free dedicated startup. A [linked Arrow follow-up](evidence/integrations/2026-09-30/additions-consist/README.md)
checks native mixed/partial-span rejection, occupancy thresholds, cooldown,
continuous readiness, departure latching and BOOST/BRAKE/reverse/STOP with two riders.
Other supported train configurations, occupancy patterns, curves/slopes, moving track,
restart/removal, multiplayer and visual acceptance remain open. See the integration evidence directory for exact completed
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

## Immersion 0.2.3: carrier camera adapter

The `-PfdIntegrations=immersion` profile installs the exact
[0.2.3 artifact](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion/files/8765632)
and Aeronautics bundled 1.3.2 alongside our baseline. It can be combined with all
four other integration profiles. The adapter applies only to this Immersion version
and loads its optional camera classes only on the client.

Sable remains the sole owner of the rider's physical camera rotation. Immersion's
existing smoothing filters that carrier orientation; our adapter supplies its
telemetry/body effects with the actual registered carrier's render pose, independent
of mouse look or smoothing-cache availability. It does not apply a second chassis
or carrier rotation. This also keeps inversion/G axes correct when head inertia is
set to zero and Immersion clears its own raw-orientation cache.

For a local 4D rider, Immersion's `camera.enabled` master switch also bypasses its
orientation filter. Physical seat rotation still comes from Sable. Bank, shake,
G response, neck state, body travel and motion history reset when changing seats,
carriers, worlds or riders, on dismount, and when toggling that switch. The adapter
does not change the player's mouse angles or saved comfort settings. Ordinary
passengers keep Immersion's existing behavior.

Current packaged-client evidence covers an Arrow ride, normal controls and dismount,
mouse input, inversion, zero head inertia and the camera master switch. This does
not certify every family, spectator/third-person mode or maximum comfort settings.
A later combined-pack Arrow session adds two real clients, late join, occupied
dedicated restart, inversion and normal exit. A follow-up fixes double conversion
of saved local mouse angles and stale native tracking on frozen-tick reconnect.
Bounded Arrow cold/live/moving reconnect checks preserve local view direction;
other families and camera modes still need coverage. Exact observations and outstanding cases
are recorded in [Immersion evidence](evidence/integrations/2026-09-30/immersion-0.2.3/README.md).
Its seat-aligner/pose features are not a replacement for 4D carrier seats or controls.

## Combined-pack multiplayer evidence

A packaged dedicated server and two packaged clients ran all five pinned adapters
plus Aeronautics/Interiors. The Arrow Engineered circuit exercised normal boarding,
occupied cold restart, server-validated dispatch/capture, inversion and normal exit.
All 891 received validation snapshots matched server hashes; 395 snapshots were
identical on both clients. Goggles/readouts synchronized occupancy, restraint,
dispatch and retention transitions. Track Styles single/double spine changes retained
mechanical rail data and survived late join and resource reload. See the
[reproduction and limitations](evidence/integrations/2026-09-30/combined-two-client/README.md).

`-PfdValidation` enables read-only packet hashes and looked-at station diagnostics
for packaged server/client runs. A validation tick is attached only to debug
snapshots and is ignored by persistence. These measurements are not performance
baselines and never grant operation authority.

Legacy and Free Spin now have [two-client Display Link, occupied restart and
circuit evidence](evidence/integrations/2026-09-30/spin-multiplayer/README.md). That
session exposed and corrected demonstration braking/approach placement and held-car
Engineered presence reporting. It preserves the failed runs as well as the bounded
passing checks; it does not complete the lifecycle matrix.

Revised ZacSpin and PAX also have [occupied two-client restart and riding
evidence](evidence/integrations/2026-09-30/revised-pax-multiplayer/README.md).
Both restored their original carrier/seat assignments, completed a native circuit
or winch cycle, and allowed normal unloading. Create displays followed occupancy,
readiness and PAX phases; all 460 received snapshots matched the server. The
clean repeat disables revised Additions auto-dispatch to retain native station
ownership. An invalid preceding automation attempt is preserved separately.
This extends bounded combined-pack occupied restart evidence to all five families,
but does not establish full inversion, every seat, camera mode or lifecycle case.

A [transport comparison](evidence/integrations/2026-09-30/transport-recovery/README.md)
reproduced the transient missing-body burst on an occupied UDP cold restart while
an identical TCP restart had no such errors. Both restored the rider and native
carriers. Six client tracking-range departures/returns per transport restored all
sixteen nearby carrier bodies. This supports a Sable cross-transport ordering
explanation; it is not an upstream fix or server chunk-unload certification.
The report documents the tested `attempt_udp_networking=false` troubleshooting
fallback. Default transport settings remain unchanged.

## Removing optional addons

A [schema-5 dedicated-server removal matrix](evidence/integrations/2026-09-30/removal-matrix/README.md)
now covers all five families while parked and unoccupied: a combined-pack control,
each addon omitted separately, all five omitted, and reinstallation on the saved
base-pack world. All eight cold restarts preserved the 35 rail sections, eight
vehicles, twenty carrier bodies, functional/appearance IDs, profiles, magnetic
spans and operating state. All three PAX couplings were restored. Only live carrier
angle and angular velocity were excluded from exact saved-field comparisons.

Missing Engineered and Additions registries discard their blocks when affected
chunks load. Reinstalling those JARs after saving does **not** restore the controllers,
stations or fins; eleven queried blocks remained air in the reinstalled test world.
Back up the complete stopped world before changing a pack. Restore that backup with
its original pack to recover removed equipment, or rebuild/rebind equipment and
validate operation before boarding riders. Retained appearance IDs and original
functional track data do not constitute a backup of an addon's blocks.

This result does not cover occupied removal, Magnetized-specific anchor-mode
migration, Immersion camera removal, client rendering or the full lifecycle matrix.

## Remaining acceptance

Five native passengers now survive [parked and moving chunk eviction/reload with
Engineered energy enabled](evidence/integrations/2026-10-01/engineered-occupied-chunks/README.md)
in Engineered-only and combined-pack dedicated servers. Both runs prove removal and
fresh recreation of all five chassis, fourteen carriers and twelve anchor/controller
references, then verify original passenger mounts, motion, recapture and normal
unloading. The four straight-track families retain mechanical data and parked FE
balances. Human-player/client interruption, PAX and partly loaded occupied routes
remain outside that evidence.

A separate [occupied PAX lift eviction check](evidence/integrations/2026-10-01/pax-occupied-chunks/README.md)
restores four coupled cars, eight carriers, the native carriage and sixteen original
passenger attachments. The winch retains a fault hold until explicit resume, then
completes the gravity-return/capture cycle and normal unloading. The Engineered
variant also evicts/reloads its controller and observer and exercises competing
commands throughout recovery under the default energy-free configuration. This
earlier checkpoint covered lift interruption only. The follow-up below extends the
phase coverage; FE-on PAX lifecycle and human-player/client recovery remain open.

The [PAX phase-recovery follow-up](evidence/integrations/2026-10-01/pax-phase-recovery/README.md)
extends the native fixture to outbound, returning and capture interruptions. It
corrects premature catch engagement, an inactive-winch block lookup that could
retain chunks, and missing world-guide restoration for an empty carriage, including
when the carriage reloads before the winch chunk. It also closes the native loading
dependency group over every linked chassis and rotary carrier. Gravity faults retain restraints and require braking/readiness followed by explicit manual
recovery; they do not promise an instantaneous stationary hold on a slope. All four
occupied phase regressions pass in base, five individual-addon and combined native
profiles, with 32 registered checks and explicit absent-addon skips per profile,
plus 19 unit tests. This does not close the wider lifecycle/client matrix.

- Extend reconciled FD305 evidence to the schema-5 optional-addon lifecycle matrix;
  investigate the recorded client resource exit. Extend the bounded transport
  diagnosis above to adverse network conditions and any persistent missing-body case.
- Extend Engineered straight linked-train and FE evidence to curves/slopes, gravity
  rollback, further supply patterns/configuration extremes, external energy networks and
  lifecycle/multiplayer checks. PAX remains winch-owned, with the bounded
  opposing-equipment checks recorded above.
- Extend the packaged Ponder/goggles visual checks to every animation phase and both
  multiplayer clients; extend the verified live legacy/Free Spin Display Links to
  the complete station/winch and addon matrix.
- Extend bounded Track Styles two-client synchronization/reload evidence to the
  full multiplayer matrix, occupied removal/reinstallation, resource replacement,
  transition and performance evidence; broader appearances and its own tool integration
  remain outside the current two-spine adapter.
- Extend the bounded Additions linked Arrow/mixed-span/fin-mode checks to the
  remaining train configurations, lifecycle and multiplayer acceptance;
  validate any rotating-seat replacement separately.
- Extend Magnetized native straight-track evidence to linked trains, curves/slopes,
  equipment visuals, configuration extremes, lifecycle and multiplayer.
- Extend the bounded five-family Immersion camera/restart evidence to every camera
  mode, comfort extreme, inversion and lifecycle transition. The combined-pack
  two-client cycles remain bounded evidence only.
- Run base, individual-addon and combined-pack dedicated-server/two-client/late-join
  matrices, migrations/removal, interruption cases and measured rendering/performance.
  Publish exact tested versions and evidence, not inferred compatibility.
