# Engineered operating and style-family checkpoint

This is partial FD401 evidence for `0.9.0-dev.3`; FD401 and the 0.4/0.9 gates
remain open. Schema and network protocol remain 5. The final artifact checksum is
in `operating-artifact.sha256`. This record covers native dedicated GameTests and
unit tests; no separate dev.3 GUI/player or shader observation is claimed.

## Reproduction and tested profiles

Use the exact base stack in `build.gradle`, Java 21 and the pinned optional
artifacts in `docs/INTEGRATIONS.md`. Each GameTest run recreates its own disposable
world. Run profiles sequentially because they share that run directory:

```sh
./gradlew build runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=styles
./gradlew runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=styles,engineered
```

All four runs pass 22 registered required GameTests. The two Engineered-specific
tests explicitly report `not exercised` without the inspected addon: standalone
and Styles-only therefore exercise 20 core cases plus two availability guards.
Engineered-only and combined exercise all 22, including both installed-controller
scenarios. `operating-{profile}-native.txt` preserves native measurements and final
results for each profile. `operating-unit.json` records 23 JUnit tests, zero
failures/errors/skips. CI repeats a clean build, unit tests and native tests for
each of these four profiles, with separate evidence artifacts.

## Independent optional adapters

The released Engineered 1.1.1 API was inspected locally. Its `CartAssembly.bodies`
collects riveted members, so it omits this addon's rotary carrier bodies. The new
adapter appends only the seed cart's live carriers and deduplicates all body UUIDs.
It preserves native riveted members and per-cart force ownership; it does not
apply an extra force to each child or add the rest of a coupled train to every
cart's motor mass. The native motor applies its own propulsion force and rotary
joints transmit the load. Real Engineered `MotorForce.mass` results are compared
with the sum of the actual native body masses before and after boarding.

The second adapter guards the installed `MotorDrive.apply` entry directly. A
captured or maintenance-held car rejects longitudinal propulsion. PAX always
retains its winch/gravity-return ownership, including while moving. This avoids
depending on the ordering of callbacks on the base chain-drive hook. Both adapters
are optional target-name mixins; exact metadata version checks fence their policy.
The native tests assert both injection methods exist on the actual pinned classes
before testing behavior. A missing target can boot without implying compatibility.
No third-party implementation, resource or JAR is copied into this addon.

## Actual controller, passenger and force scenarios

`EngineeredGameTests.installedMotorsRespectCaptureAndDriveDispatchedFamilies`
builds five independent 48-block original matching rails and uses the separately
installed controller block and public span/step interfaces. It creates real cars
and seats one native Pig passenger in each. Initial capture is allowed to settle;
the measured tick-60..100 hold has zero longitudinal drift on every family.
Initial assembly settling is logged separately, including about -0.114 blocks for
Arrow, and is not represented as motor-driven motion.

After closing restraints and dispatching through normal vehicle operations,
Engineered LAUNCH at 10 blocks/s propels the four non-PAX families. Their measured
travel after 100 ticks is approximately 16.2–16.5 blocks, with speed 6.2–6.4
blocks/s. These profiles are flat: this proves longitudinal native propulsion,
not a complete powered controlled-rotation program. The retained PAX car remains
stationary. Family, track binding and the native passenger remain intact. Moving
cars reject premature restraint release.

| Family | Native members | Unoccupied assembly mass | With one passenger |
| --- | --- | --- | --- |
| Controlled Arrow | 3 | 15 | 15.8 |
| Legacy ZacSpin | 3 | 19 | 19.8 |
| S&S Free Spin | 5 | 37 | 37.8 |
| Revised ZacSpin | 5 | 39 | 39.8 |
| PAX | 3 | 15 | 15.8 |

Mass units are the pinned engine's game-scale units. Native loading contributes
0.8 for the declared 80 kg rider once; the adapter does not invent passenger mass.
The fixture contains no extra riveted additions, so additional rivet combinations
are not certified by these measurements.

Switching the actual installed controller to STOP with value zero brings each
non-PAX chassis below 0.1 blocks/s. STOP leaves rotating-carrier capture and
restraint state under our normal operator policy. Requesting carrier capture,
waiting for ready alignment, opening restraints, unloading, then enabling
maintenance retention keeps every car stationary against a later LAUNCH at 20.
The final tick-480..590 retention interval also has zero longitudinal drift.

`ordinaryBearingUsesMotorWithoutRotaryMembership` uses the pinned unmodified
native bearing on original Arrow running rails. LAUNCH moves it about 10.56
blocks, `CartAssembly.bodies` remains a single body with mass 1.0, no 4D vehicle
record is created, and STOP slows it below 0.1 blocks/s. This proves ordinary
propulsion/rail access, not loaded recovery towing.

The existing sixteen-rider PAX fixture and the taller two-cycle fixture now each
install three opposing LAUNCH spans at 20 blocks/s. A callback registered at test
setup also calls the actual installed motor entry for every train car while the
cycle runs, independently of the base chain-drive callback. The physical winch
still lifts, holds on a power interruption where tested, resumes, releases,
returns under gravity and recaptures; the tall train completes two full cycles.
The prior route-availability/block-entity reload fixture remains unchanged and
has no opposing motor spans, so this record does not claim motor-span reload
coverage.

## Style-family eligibility

Both inspected Arrow decorative IDs now support only controlled Arrow functional
track. The editor and renderer enforce this boundary. Original remains available
for every family. The unit test covers both IDs across all five families, plus
unknown IDs. The native action-5 test performs ten real packet decisions: with
Styles 1.2.0 installed, both Arrow selections are accepted and all eight other
family selections rejected; without it, all ten external selections are rejected.
Each decision preserves the original profile, functional family and variant.
Historical single-spine resource/provider lifecycle evidence remains separate.

## Failures resolved before this checkpoint

The first fixture attempted to read the native cached cart center immediately
after assembly; it is not initialized until physics runs. The fixture now records
logical body pose and measures the settled hold window. A target of 3 blocks/s
also supplied a gradual full-span launch unsuitable for the original displacement
assertion; using target 10 gives a measurable native launch. Correct rotary mass
and the changed controller target are separate changes, not an isolated claim
that the membership adapter alone caused the measured acceleration.

The first expanded combined fixture registered an `onEachTick` callback inside a
scheduled callback, mutating GameTest's scheduled map during its iteration. Its
server crash was a test-harness failure. Registration now happens at test setup
and only replaces the runnable at the later tick. Final profiles pass with that
repair; this is not presented as a diagnosed Sable/Engineered production crash.

## Remaining acceptance

Curved/banked and double-spine styling, full clearance, styled rides and rendering
modes remain open. Engineered FE-enabled operation, real-player operator actions,
redstone station/controller wiring, other controller sequences and an installed
motor driving a complete non-flat controlled program are not covered here.
Recovery coupling/towing, full teaching content, the wider lifecycle/migration
matrix and quantitative performance targets retain their roadmap packages.
