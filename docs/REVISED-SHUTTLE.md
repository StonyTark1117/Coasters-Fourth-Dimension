# Revised carriers and PAX shuttle development

FD301 is specified. FD302–FD305 are not yet release-approved. The active target is the complete 0.9.0 roadmap, including the prerequisite 0.3 and 0.4 gates. Passing native tests below establishes these scenarios only; occupied/active dedicated-server restart, real clients, full eligibility and migration checks, tutorials, and performance gates remain outstanding.

## FD301: evidence and original topology

Intamin's [product page](https://www.intamin.com/product/zac-spin-coaster/) and [June 2020 flyer](https://www.intamin.com/wp-content/uploads/2019/09/IAR-2020-Flyer-RollerCoaster-zac-spin-coaster.pdf) describe alternative seating and eight-rider vehicles. Neither establishes exact generation-specific hinge geometry or pair coupling. The new preset is therefore **Revised Free-Rotor Car (Inspired Variant)**, not a verified historical replica.

The original game specification has eight seats in four independently constrained two-seat groups. Two rows sit at longitudinal offsets −3 and +3, on each wing at lateral offsets −3 and +3. Each child has its own body UUID, native rotor joint, angle, angular velocity, occupancy, and loading state. It uses `zacspin_revised_v1` matching functional track, 14-block minimum axle spacing, and a larger swept envelope. It cannot use legacy, magnetic Free Spin, Arrow, or PAX track.

The parent pivot height is 1.5 blocks above the bearing center, with child hinge offset −0.5 from the child rotation point. The neutral seat height remains unchanged. Original lower axle and crossarm geometry brings the hinge nearer the empty group center of mass. Native measurements for an empty pair are mass **4**, first moment **1.5**, and hinge inertia **1.9947916667** in Sable's units. The empty vertical COM offset is **−0.375** blocks. These are game geometry measurements, not manufacturer dimensions. Passenger mass comes from each actual occupied seat exactly once.

`RevisedCarrierGameTests` boards eight actual pigs, closes restraints, traverses the original hilly circuit, checks four independent paired bodies, measures nonzero rotation, and round-trips the child hinge offset through saved data. GUI example: `/fd demo revised`.

## FD303–FD304: original shuttle mechanism

The [PAX manufacturer brochure](https://www.pax.ru/upload/Бумеранг%20–%20две%20петли%2040м.pdf), inspected as a primary PDF, describes free inertial seat rotation and a lift carriage that catches the train, raises it, releases it, and permits a braked return before unloading. Its specification section lists four cars with four riders per car. The same brochure describes a larger installation separately; our sixteen-seat configuration follows the specification section, without claiming installation fidelity.

The addon uses four original four-seat cars, two independent wing pairs per car, `pax_shuttle_v1` matching track, native six-block couplings, and a maximum of four linked cars. A fixed-world unbranched route must have two elevated open spikes and a level station. Every segment must retain its functional family and unchanged geometry. Ordinary native chain propulsion is disabled for PAX cars; an ordinary fitting cart still receives ordinary behavior.

A craftable **PAX Shuttle Winch** accepts Create shaft power (16–128 RPM; maximum guide speed four blocks/second). It allocates an original Sable carriage body and drives it through a world guide constraint. A separate mechanical catch joins that body to the lead chassis. The carriage pulls the real linked train uphill; no train teleport or launch impulse implements the lift. At the release point it settles, removes the catch, and leaves the train to native gravity and track guidance. End margins are faults, never successful turnarounds or endpoint jumps.

Operating phases are boarding → lifting → outbound → returning → capturing → boarding. Carriers remain captured during the lift and rotate freely after release. Restraints remain closed through outbound and return station passes. Final braking applies a common bounded translation reduction to chassis and carrier bodies; captured rotors align separately. The returning carriage approaches before engaging its catch and brings the train back to the bound boarding position. Moving a sleeping guide wakes the carriage without adding an impulse.

Power loss while lifting holds the mechanical catch and requires explicit inspection/resume. Persisted active phases reload interrupted, rather than continuing offline travel. Winch, car, carrier, route, and consist availability are checked. Route chunks must remain in the server's block-ticking range, as required by Sable physics; cached geometry in a merely loaded chunk is insufficient. Missing or changed support faults the train. Full server restart and client lifecycle validation remain pending.

## Player workflow and examples

Craft matching PAX track and four PAX cars, place them six blocks apart on level track between the spikes, and use native couplings. Place the winch within twelve blocks of the train. Sneak-use binds the captured train; the controller chooses the car nearest the lift spike. Supply Create shaft power, board, and close restraints with the rotation tool. Use the winch or dispatch through the car editor to begin. After final capture, open restraints and unload. Ordinary boarding-station redstone does not dispatch or capture a PAX train during its shuttle cycle.

Normal use on a lift fault explicitly resumes after power/support restoration. Sneak-use on a fault requests physical recovery to the station, once every car and carrier has stopped and aligned. Native chunk-availability and winch reload testing covers this recovery path; full server restart acceptance remains pending. Do not remove the winch or route while operating.

`/fd demo pax` builds the compact original example; `/fd demo pax_tall` builds the taller example. Both include four cars, a winch, a creative motor, and boarding platforms. They assemble native couplings after four seconds. They are simple two-spike game layouts, not copies of PAX's loop geometry. All route chunks must remain available for physics, the swept corridor must be empty, and the existing near-origin limits apply.

## Automated evidence

The 2026-09-30 native run passed **18 GameTests**, including:

- Eight occupied revised seats, four independent rotors, changed pivot/mass properties, native circuit traversal, and NBT hinge-offset persistence.
- Sixteen occupied PAX seats, physical lift carriage, power interruption with less than 0.3-block rollback, explicit resume, gravity reversal, final capture, retained native coupling and rail engagement, and unloading.
- Two complete cycles of the taller four-car example, including rejection of outbound restraint opening and complete final carrier alignment.
- Removal of the remote spike's forced chunk tickets, proof that it left block-ticking range, interrupted hold, real winch block-entity save/reload preserving body IDs and consist, less than 0.3-block rollback while unavailable, and physical recovery after chunk availability was restored. The base graph can retain loaded geometry, so this proves loss of physics availability rather than complete chunk eviction.
- Existing foundation, Arrow, legacy ZacSpin, magnetic Free Spin, editing, operations, boundary, and ordinary-cart checks.

Evidence excerpt: [native run](evidence/0.3/native-progress.txt). Reproduce with Java 21 and `./gradlew build runGameTestServer`. This is development evidence, not the FD305 or FD905 acceptance gate.

## Packaged server checkpoint

The `0.9.0-dev.1` JAR booted in a fresh official NeoForge 21.1.248 production installation with full Create 6.0.10-280, Coasters 0.1.5, and Sable 2.0.3. Both PAX examples assembled their native four-car consists; the revised example created four independent carrier groups. The save contained schema 4, fourteen original rails, nine vehicles, and twenty carrier groups. See [startup and assembly](evidence/0.3/production-startup.txt).

A clean cold restart at boarding, frozen before resumed simulation, preserved the complete addon SavedData exactly. Both winches retained their four car IDs and boarding phase. One subsequent native tick preserved all identities, rails/profiles, seats and hinge offsets, with all nine vehicle faults empty. See [restart evidence](evidence/0.3/production-frozen-restart.txt). This does not prove occupied or active-lift cold restart, which remains required.

GitHub's clean-build/native workflow also passed at implementation commit `0ab941a`: [CI run](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/actions/runs/36760640541). Development is reviewable in [draft PR #1](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/pull/1).
