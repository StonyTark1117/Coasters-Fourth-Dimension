# Optional integrations: development and acceptance

This work extends FD401 and FD403 on the integration branch. It does not close FD305,
FD404, or the FD901–FD904 release gates. The original release chat is separately
validating occupied revised-carrier and PAX lifecycle recovery. Integration code starts
from commit `9e8ae6f975286fff2831981ce975e689ccdc7b41`; its final changes and evidence
must be reconciled before release.

## Implemented foundation

- Functional track providers register before worlds load and must declare a matching
  family, variant, contacts, mechanisms and clearance. Native curve resolution passes
  through this registry. The original provider remains built in.
- Schema 5 separates functional provider from appearance provider/style. Schemas 1–4
  migrate on read. Unknown functional providers retain their saved IDs, profiles,
  magnets and revisions, but cannot resolve or authorize travel. Their absence does not
  invalidate the stored geometry. Unknown appearances retain their saved selection and
  fall back to our complete original mesh. No external appearance is activated yet.
- Component ownership explicitly covers spine, ties, running rails, control rails and
  magnetic equipment. The foundation currently assigns all of these to our renderer;
  an external adapter must implement and validate its ownership changes.
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

## Track Styles 1.2.0 investigation

The September 30 release notes explicitly add Arrow 4D single/double-spine styles,
despite the main description still listing Arrow 4D as future work.
[Released artifact and notes](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles/files/9020601).

The inspected artifact is `curse.maven:coasters-simulated-track-styles-1642719:9020601`,
SHA-256 `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38`.
Its model registry exposes center beam, tie, left rail and right rail components.
Both Arrow 4D side-rail models contain main and static control-rail geometry together.
They do not establish a profile-driven control-rail interface. A full-model overlay
would duplicate our moving rails; render ownership must be resolved before enabling it.

The project page reports MIT while the released metadata still says All Rights Reserved.
No implementation, models or textures were copied into our source or output. Further
work must use independently authored runtime integration and separately installed assets,
with applicable terms resolved before distribution. See the updated dependency audit.
FD801 is now actionable investigation, not blocked on the existence of an Arrow 4D
release; mechanical compatibility remains unverified.

## Remaining acceptance

- Complete and reconcile FD305 occupied restart, rider and lifecycle evidence.
- Finish Engineered rollback, linked-train, optional FE, lifecycle and multiplayer
  checks; PAX propulsion stays excluded until separately proven.
- Extend the packaged Ponder/goggles visual checks to every animation phase and both
  multiplayer clients; verify live Display Link output in those sessions.
- Implement and validate Track Styles appearance selection, component rendering,
  transitions and removal, without altering mechanical data.
- Add Additions station signaling first; validate any rotating-seat replacement separately.
- Test Magnetized acceleration/braking and magnetic equipment interactions.
- Validate Immersion camera composition, inversion, mouse look, dismount and comfort
  behavior with its full dependency stack.
- Run base, individual-addon and combined-pack dedicated-server/two-client/late-join
  matrices, migrations/removal, interruption cases and measured rendering/performance.
  Publish exact tested versions and evidence, not inferred compatibility.
