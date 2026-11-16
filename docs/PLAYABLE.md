# Version 0.1 acceptance: FD101–FD109

This is the historical 0.1 acceptance record. Dependency versions, schema numbers,
test counts and scope limits below describe that checkpoint. Use the
[current installation and 17-style guide](../README.md) for the supported release.

The 0.1 implementation adds original controlled Arrow/S&S-style and legacy ZacSpin-style rides. The package audit below covers the bounded near-origin build; it does not certify the expanded 1.0 release. FD101–FD109 are complete within the stated limits.

## Package audit

| Package | Implementation | Acceptance evidence |
| --- | --- | --- |
| FD101 | `track/TrackPlacement`, `TrackDesign`, `TrackGeometry`, `client/OriginalTrackRenderer`; two original track items, placement ghosts, UUIDs, geometry fingerprints and explicit rebinding. Both native BER and Flywheel rail rendering are suppressed only on our sections. | `TrackContentGameTests` uses both actual items, saves identities and detects geometry changes. Original mesh and pinned wheel geometry were inspected in a client. No optional track provider is installed. |
| FD102 | Arc-length lookup, unwrapped monotone Hermite profiles with explicit slopes, per-axle evaluation, signed rack displacement and endpoint continuity. Visible control rails and carrier targets read the same profile. | `TrackMathTest`, `ControlledTrainGameTests`, `DemoRideGameTests`: linked cars six blocks apart reach 6.281 rad, differ by 3.024 rad at separate axles, cross matching junctions and retrace on rollback. Both closed-loop endpoints match mechanically. Boundary tests reject disconnected matching edges, including the topology used at non-junction crossings. Stops hold the current profile target. |
| FD103 | Sable owns gravity, inertia and chassis acceleration. Independent legacy pivots use native constraints, viscous damping, ballast and exactly one mass contribution per actual passenger. | `VehicleContentGameTests` measures an added/removed 0.8 native mass unit and distinct carrier responses; foundation tests retain angular state during reversals. The occupied native legacy hill circuit swings roughly −1.171 to +0.481 rad and settles at capture. These are measured fixtures, not guaranteed flip counts. |
| FD104 | Original four-seat Arrow and eight-seat legacy cars, two native carrier groups each, original restraints/axles/struts, strict Arrow coupling at six-block axle spacing. | Native seat counts, passenger attachments, loading, linked cars, clearance rejection, full circuits and persistence. Real players use the original seats and mounted editor; server and observing client see the same carrier/passenger identity. |
| FD105 | Server-validated rotation editor, copy/paste, mirror/reverse, geometry rebind, free damping/ballast tuning, cyan pose and yellow/red clearance previews. | `EditorGameTests` rejects malformed/non-finite/oversize input, stale revisions, denied permissions, excessive reach, occupied approaches and discontinuous profiles. Accepted edits survive serialization. Client previews identify controlled poses versus a declared free reference scenario. |
| FD106 | Stopped/aligned/low-speed boarding, restraints, gradual capture, maintenance retention, original directional redstone station, delayed platform exits and reconnect restoration. | `OperationsGameTests` covers rear dispatch/capture input and distinct front ready/right locked outputs, retention and support loss. Real-player locked sneak is denied; unlocked dismount clears the mount and returns to a platform with normal gravity. Occupied restart restores the assigned seat and continues the ride. |
| FD107 | Family/controller checks at initial placement, assembly/snap, engaged binding, links, route boundaries, editing/dispatch and saved-data loading. Lost support requests braking and retains restraints. | Foundation compatibility matrix plus native wrong-family locked/snap/mixed-consist checks. Both-direction braking stops before unsupported boundaries. Disconnected matching edges cannot replace a binding. Native body removal recovers passengers and removes the remaining vehicle bodies. |
| FD108 | `OrdinaryGearCertificate` certifies only the pinned, unmodified single native Coasters bearing against our measured main rails. | `OrdinaryCartGameTests` traverses both original tracks with that bearing and rejects an added wide body. Accepted ordinary bearings have no addon carrier record or seat rotation. Rail access is separate from the future FD402 towing evaluation. |
| FD109 | 0.1.0 JAR, six essential item recipes, station drop, creative tab, instructions and two original powered example circuits. | Clean packaged client/server runs, a fresh production NeoForge installation with four mod JARs, dedicated and integrated-server ride checks, and the automated suite described below. |

## Architecture and operating choices

Coasters owns the graph, chassis engagement, chain propulsion and native train links. Sable owns the physical bodies, rotary constraints, transforms and camera. Create supplies the passenger entity; addon-owned seat blocks provide the original geometry, explicit boarding and restraint checks. There is no second client-side carrier simulation and no duplicate gravity force.

The Engineered audit in [PLAN.md](../PLAN.md#d07-what-existing-addons-can-provide) identified useful longitudinal braking, holding and observation facilities. Version 0.1 keeps Engineered optional: its reviewed functions do not provide the carrier alignment, restraint and seat-restoration interlocks needed here, and FD109 requires a standalone ride. The original station implements those interlocks and a minimal native chassis stop/dispatch gate. Future external operations adapters belong to FD401; no Engineered compatibility is claimed by these tests.

Production body groups extend Sable's loading dependency chain so a chassis and its wings load together. Saved player-to-seat assignments are distinct from current occupancy: offline riders reserve their seat but add no mass. Login waits for native bodies, restores the assigned seat even with closed restraints, then restores the player's original gravity flag. Permanent body removal clears assignments; ordinary unload does not delete the vehicle.

## Mechanical conventions

Track geometry is original game-scale content, not a claim of manufacturer drawing fidelity. Main-rail half-gauge is 0.772 blocks. Main rails use an original elliptical section: lateral half-width 0.13125, vertical half-height 0.075, centered 0.325 above the graph path. The pinned native wheel renderer places upper/lower wheel centers 0.600/0.050 above the path, with nominal wheel radius 0.2: contact planes are therefore 0.400 and 0.250. The right wheel tread spans 0.625–0.9375 laterally; the rail occupies 0.64075–0.90325. The native path guide supplies lateral retention; this bearing has no separately certified side-wheel interface. The certification applies only to one unmodified native bearing, including its rendered wheels, not arbitrary assemblies. Native tests demonstrate traversal on both original tracks and reject added bodies. These are pinned game-model dimensions, not real manufacturer measurements.

Arrow control rails lie at lateral offsets ±1.35. Their height is `-0.85 + angle * (0.5 / (8*pi))` blocks relative to the track centerline, with angles limited to ±8π. The finite stroke is an idealized direct rack mapping. Matching a final quaternion modulo 2π does not satisfy mechanical continuity. Legacy free-spin track has no control rails.

The new saved-data schema is 2. Schema 1 fixture data migrates to deterministic track identities; production rail records persist UUID, variant, geometry fingerprint, revision and validity. A changed curve retains its ID but invalidates its profile until explicit rebinding. Unknown schemas and identities fail closed.

The foundation's ±1024 coordinate restriction remains. It must be investigated before advertising general-world support. All acceptance claims must retain this restriction until evidence justifies changing it.

## Reproducible validation

Run with Java 21:

```sh
./gradlew build runGameTestServer
```

The current suite has **12 JUnit tests and 11 required native GameTests**. It includes actual native constraints, powered occupied demonstration circuits, per-car controlled motion, a fitting ordinary bearing, station/redstone behavior, support loss, group loading dependencies, editor rejects and persistence. Native game tests use actual passenger entities because NeoForge FakePlayer refuses mounting; separate client checks cover real players.

The maximum supported functional snapshot (256 sections, 8192 aggregate profile keys, 64 eight-seat vehicles with assignments) encodes to **569,324 bytes**. The round-trip test also checks the bounded 8 MiB NBT allocation accounting limit; wire size remains below Minecraft's 1 MiB custom-payload limit. Protocol 2 accompanies save schema 2.

For binary validation, `-PfdPackaged` uses an isolated empty source set and loads the built addon JAR. Logs identify its archive as the code source. Client and server directories contain no optional coaster addons. A separate server was installed using NeoForge's official 21.1.248 installer and launched through its generated production argument file. Its mods directory contains only this addon, full Create 6.0.10, Coasters 0.1.5 and Sable 2.0.3; their required libraries are supplied by their own distributions. Test-world copies are disposable. The single-player check uses the same packaged addon with Minecraft's integrated server.

The occupied restart comparison uses the **final saved state after shutdown**, not an earlier live sample: all four persisted carrier UUIDs, angles and angular velocities match the frozen restart exactly. A fresh client restores the Arrow rider at about 3.551 rad with restraints closed, then continues to station capture. A separate legacy disconnect/rejoin also restored its rider. After the two-tick dismount fix, the Arrow rider exits at `(-105.5, 123, 28.5)`, with no RootVehicle and normal gravity.

## Evidence and limits

Evidence files and the [final artifact checksum](evidence/0.1/artifact.sha256) are collected under `docs/evidence/0.1`. Screenshots document original geometry, editor/previews, observing-client state and station exits; log extracts document native assertions and actual player sessions. The final local build passed all 23 tests; GitHub Actions runs the same build and native checks on the pushed revision.

The supported scope remains deliberately bounded: ±1024 horizontal coordinates; 256 track sections, 64 vehicles and 8192 profile keys per dimension; only the two original functional families; Arrow links at six blocks; single legacy cars; and only the measured bare native bearing for ordinary access. The free preview is an empty 45-degree, zero-angular-speed reference at two blocks/s, not a prediction of arbitrary rider loading. Original geometry and declared mass are game approximations. No Track Styles compatibility, recovery towing, magnetic Free Spin, revised ZacSpin, PAX shuttle, shader matrix or universal performance guarantee is claimed. Those remain addressable later roadmap packages.


### Evidence index

- [Native assertions and build result](evidence/0.1/native-acceptance.txt), [JUnit counts](evidence/0.1/unit-tests.txt).
- [Single-player ride logs](evidence/0.1/singleplayer-rides.txt), [legacy station return](evidence/0.1/singleplayer-legacy-return.png).
- [Production installation](evidence/0.1/production-install.txt), [both production rides](evidence/0.1/production-rides.txt), [Arrow return](evidence/0.1/production-arrow-return.png), [legacy return](evidence/0.1/production-legacy-return.png), [production dismount](evidence/0.1/production-dismount.txt).
- [Final artifact startup](evidence/0.1/final-artifact-startup.txt), [wrapped free preview](evidence/0.1/free-preview.png), [controlled preview](evidence/0.1/controlled-preview.png), [main-rail wheel alignment and platform exit](evidence/0.1/arrow-wheel-alignment.png).
- [Occupied final saved state](evidence/0.1/occupied-restart-save.json), [matching frozen restart](evidence/0.1/occupied-frozen-restart.txt), [legacy rejoin](evidence/0.1/legacy-rejoin.txt), [observing client](evidence/0.1/remote-legacy-station.png), [restored normal dismount](evidence/0.1/dismount.txt).

The final post-ride change only wraps the preview HUD label and formats source. The resulting JAR passed the complete automated suite and a production server/client startup plus preview check; the ride mechanics are unchanged from the recorded full circuits.
