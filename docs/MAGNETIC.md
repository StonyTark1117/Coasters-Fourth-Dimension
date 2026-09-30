# Version 0.2 acceptance: FD201–FD204

Version 0.2 adds original magnetic Free Spin track, an eight-seat car, passive magnetic equipment, loading presets and an example circuit. It retains the bounded 0.1 build's controlled Arrow and legacy ZacSpin families. This is the 0.2 gate, not the five-family 1.0 release.

## Topology and physical conventions

S&S describes an eight-passenger car, arranged in pairs with four passengers on each side, and magnetic influence on free rotation. See its [ride page](https://www.s-s.com/ride-pages/4d-free-spin-coaster) and [product sheet](https://assets-global.website-files.com/60be9a2c8c73a085920bb514/636c251f0271e50223f23f92_4d%20Free%20Spin%20Coaster.pdf). Those references do not establish every physical linkage or exact coupling arrangement. Our **four independent two-seat hinges** are an original game approximation, not a verified replica of the manufacturer's mechanism.

Coasters owns track engagement and chassis propulsion. Sable owns the five native bodies, four rotary constraints, gravity, rider transforms and camera. The carrier pairs have local pivots at lateral ±3 and longitudinal ±3 blocks; native local +Z is opposite track forward. Front/rear labels refer to that fixed local frame. Each carrier holds two seats and uses its own angle, velocity, occupancy, ballast and capture state. Pairs face outward longitudinally and can rotate independently; reversing does not swap their identities. Free Spin cars cannot couple.

The main rail and bare native bearing dimensions remain those measured in [0.1](PLAYABLE.md#mechanical-conventions). Magnetic bars sit at lateral ±1.6, height 0.65, radius 0.12 blocks. Their supports run below the lower wheel to an outside knee, then upward outside the tread. The right bar occupies 1.48–1.72 laterally, beyond the native tread's 0.9375 outer edge and inside the wing envelope's inner edge at 2.0. Support tops under the tread are at −0.495, below the lower wheel's −0.15 bottom. Placement checks the full wing/rider envelope: pair centers at ±3.5 laterally, half extents 1.5 × 2.7 × 2.7; the full car extends 5.7 longitudinally each way. Adjacent cars require twelve-block axle separation if either is Free Spin. These bounds are conservative for the original content, not a certificate for arbitrary attached blocks.

Native mass units correspond to 100 kg of declared loading. Each actual rider adds 0.8 exactly once, at the modeled torso. An empty original pair measures mass 4, first moment 2.625 and pivot inertia 4.1119791667. Ballast uses the same torso locations, independently of passengers. Empty/balanced/front-loaded presets set zero, 160 kg per pair, or 160 kg on left-front only. Apply presets or uniform tuning only to an empty captured car. Uniform tuning clears previous per-pair ballast.

## Equipment and editor

Craft Magnetic Inducers and Dampers, then use them on matching Free Spin track. The item installs an initial span and opens the editor. Alternatively, use the Rotation Tool and enter:

```text
3:9:induce:1:4:0.2:1;12:18:induce:-1:4:0.2:8;24:32:damp:1:5:0.2:15
```

This example fits a 36-block section. Format is `start:end:mode:direction:strength:falloff:targets`. Distances start at the section's first anchor. Spans must remain inside the section, with length at least 0.05; direction is ±1; strength is 0–20; falloff is the linear ramp fraction at each end, 0–0.5. Target bits are left-front 1, left-rear 2, right-front 4, right-rear 8; add bits to select multiple groups. Damper direction is accepted for consistent data shape but has no physical effect. Orange equipment induces rotation; cyan equipment damps it.

Every additional span costs one matching item in survival; removals refund items. Edits validate permission, reach, revision, actual geometry fingerprint, bounds, finite numbers, capacity and inventory before mutation. Occupied sections and their immediate approaches cannot be edited. Changed curves require explicit geometry rebinding, which scales magnetic distances and profile distances atomically. Maximums are eight spans per section, 512 per dimension and 4096 text characters. Existing limits of 256 sections, 64 vehicles and 8192 total profile keys remain.

Preview loading cycles through empty, balanced and front-only cases. Preview Seats draws four groups with cyan poses and yellow/red conservative clearance. It uses a declared 45-degree, zero-angular-speed start, powered constant speed of two blocks/s, selected damping, measured original empty mass properties and specified ballast. It includes the edited magnetic spans, each group's longitudinal offset and target mask. It is a reproducible comparison model, not a prediction of arbitrary live native motion, changing speeds or passenger arrangements. It does not mutate the car.

All four groups must settle upright with low angular velocity, stopped translation and valid track before boarding or releasing restraints. Existing station capture, separate ready/closed signals, maintenance retention, seat reservations and rider restoration apply to every group. Locking a Free Spin car does not permit Arrow, legacy, ordinary or cosmetic-only track. An unmodified ordinary native bearing may use the matching running rails but receives no magnetic seat effect. Towing remains unverified.

## Passive interaction

`MagneticRuntime` samples each actual carrier pivot against the vehicle's bound or connected adjacent section. It integrates the exact trapezoidal falloff over swept arc length, including intervening connected sections, once per native physics step. It seeds a fresh previous sample after reload, so old passages cannot replay. Unexpected or ambiguous routes request retained capture. Tested high-speed/reverse operation is 40 blocks/s; the system does not infer arbitrary teleports as valid passes.

`PassiveCoupler` uses the native kinetic mass matrix, including the translation/rotation cross term. For an inducer its slip direction is `(1, −direction*0.35)`; a damper uses `(0, 1)`. Exponentially bounded friction opposes slip, with impulse magnitude capped at `100*exposure`. It applies both translation and angular/orbital changes, giving the chassis reciprocal drag rather than free rotary power. Its quadratic energy change is nonpositive in exact arithmetic. Native float velocities require a resolution floor: impulses below `max(1e-7, kineticEnergy*5e-7)` are skipped. Tests additionally measure actual native kinetic energy before/after each applied impulse.

Stationary induction has zero swept exposure. A stationary damper can dissipate existing angular motion; zero slip cannot produce motion. An external chain drive can replenish lost energy, as in the example. This is a bounded game magnetic interface, not an electromagnetic field solver. Ordinary cars never enter the Free Spin torque path.

## Package audit

| Package | Implemented behavior | Validation |
| --- | --- | --- |
| FD201 | Original blue functional track, centered axles, original car/seats/restraints, four independent pairs and full placement clearance. | Eight actual passengers stay mounted through the native circuit; groups take distinct angular paths; topology, identities and offsets survive save/load. Measured gear/bar/support clearance and client geometry inspection. |
| FD202 | Inducer/damper items and original meshes, bounded editing, exact swept falloff, masks, reversible canonical direction, reciprocal native impulses. | 40-block/s forward/reverse traversal over a 0.05-block span integrates exactly 0.075 total weighted blocks, two impulses on the selected group and zero on others. Randomized mass-matrix tests and the native occupied circuit verify energy does not increase. Ordinary bearings traverse active maximum-strength spans with no addon carriers or torque. |
| FD203 | Transactional zone editor, three ballast presets, declared reference previews, all-group boarding/capture. | Real item/survival transactions and invalid-edit cases; preset serialization; last-group motion prevents restraint release; client editing/preview and real-player capture/dispatch/dismount. |
| FD204 | Recipes, instructions, original powered `/fd demo free_spin`, schema/protocol migration and dependency-only packaging. | Build/unit/native suites, production dedicated server, two real clients, occupied rotating restart, exact saved-state comparison, original 0.1-world migration and packaged single-player acceptance. |

## Reproduction and persistence

Run `./gradlew build runGameTestServer` with Java 21. The suite contains **17 JUnit tests and 14 required native GameTests**. Both occupied 0.1 demonstration loops, family boundaries, existing editor/station behavior and ordinary access remain included. FakePlayer does not mount reliably, so native tests use actual pig passengers; real-player sessions separately validate seats/camera and multiplayer.

The production installation uses Minecraft 1.21.1, NeoForge 21.1.248, full Create 6.0.10, Coasters 0.1.5, Sable 2.0.3 and this addon, with libraries supplied by their distributions. No optional coaster addons are installed. `-PfdPackaged` loads the built addon archive through an empty source set, rather than loose classes. See README for the run commands.

Schema 3 reads schemas 1 and 2, defaults old carrier longitudinal offsets/ballast to zero and absent magnets to empty, and validates the new four-pair topology. Protocol 3 requires matching client/server versions. Back up before upgrading; downgrading upgraded worlds is unsupported. The migration check preserves all 16 earlier production rail IDs, endpoints, families, variants, profiles and geometry hashes. The occupied restart compares the final saved state after shutdown to a frozen server: all eight carrier angles and angular velocities match exactly, including the rotating Free Spin rider. Rejoining restores the assigned seat; the observing client sees the same body/passenger while motion continues. The car subsequently captures all groups and permits a normal-gravity platform exit.

The maximum supported functional snapshot includes 512 magnets and 64 four-pair/eight-seat vehicles. It encodes to **659,884 bytes**, below the 1 MiB wire limit, and round-trips within the bounded 8 MiB NBT accounting allowance.

## Evidence and remaining limits

Acceptance artifacts are indexed in [evidence/0.2](evidence/0.2/README.md). The final JAR checksum, native assertions, unit counts, multiplayer/restart/migration comparisons and single-player/visual checks accompany this gate. The post-multiplayer changes refine only reference-preview geometry/mass and equipment support rendering; the final archive runs the complete automated suite and a full single-player ride.

The ±1024 horizontal coordinate restriction remains. Only the three original families are advertised. Exact manufacturer coupling, revised ZacSpin, PAX shuttle/lift, Track Styles styling/track adapters, recovery towing, broad shader compatibility and large-scene performance certification remain later roadmap work. Restricted third-party assets are not bundled.
