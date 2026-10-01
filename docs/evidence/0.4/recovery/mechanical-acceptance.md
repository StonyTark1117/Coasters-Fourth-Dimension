# FD402 native recovery coupler experiment

This checkpoint establishes bounded mechanical feasibility. It does not certify
player-operated recovery towing or close FD402. Production still rejects mixed
ordinary/4D consists. Existing native couplers can transmit a slow pull to all
five original families on flat matching track, including loaded maintenance-retained
cars. A policy rejection must therefore not be reported as a demonstrated
mechanical inability to tow.

## Experiment and measurement

Starting source: `2dc1bfbf668bcadb30e42fe669f0bd043a9a9317`, development version
`0.9.0-dev.14`. The shipped JAR is unchanged. The experiment temporarily admits
one certified ordinary bearing paired with a captured 4D vehicle and accepts
that bearing during consist eligibility. This admission is a diagnostic bypass,
not a complete production recovery policy. The archived patches and fixtures
are outside the Java source set and are not compiled into the release.

Each case uses a separate 48-block flat original track. Native track placement
creates its functional family; `VehicleFactory` creates the corresponding
vehicle. Native `CoasterCartSpawner` creates the pinned bare ordinary bearing
on the same main rails, with no controller or carrier. Both vehicles stay on
that matching family rail. `CoasterCartTrainLinkConstraint.linkCarts` creates
the actual native coupler at the spacing below. No native coupler code is
modified. The diagnostic policy does not certify other ordinary assemblies.

| Family | Coupler spacing (blocks) | Occupied cases: native passengers |
| --- | ---: | ---: |
| Controlled Arrow/S&S | 6 | 4 |
| Legacy ZacSpin | 6 | 8 |
| S&S Free Spin | 12 | 8 |
| Revised ZacSpin | 14 | 8 |
| PAX | 6 | 4 |

Each family has empty captured, fully occupied captured, and fully occupied
maintenance-retained cases. Passengers are native pigs mounted through
Create's `SeatBlock.sitDown`, with no AI; these are actual native seat occupants,
not real-player recovery acceptance. Ballast is zero. Restraints close through
normal `VehicleOperations.restraints`. The retained case uses normal retention
before coupling, retaining both captured hinges and translational braking.
The other cases explicitly clear the chassis stopping flag for this bench,
while retaining captured hinges; this is not a supported player operation.

A Sable generic world constraint acts only on the ordinary bearing. It accelerates
the world anchor at 0.1 blocks/s² up to 0.5 blocks/s, then holds after commanding
ten blocks. Axis motor settings are stiffness 30000, damping 4000 and maximum
force 80000 in the API's native units. Neither the 4D chassis nor its carriers
are teleported or directly driven. This artificial actuator is a measurement
fixture, not a craftable locomotive or proof of native motor/FE compatibility.
PAX cases are unassigned standalone cars, not a caught winch-owned shuttle train.

Native coupling settles for 40 ticks before measuring both body centers.
Travel is measured independently from each settled starting center. The pull
runs for 600 ticks. Each tick measures link presence, distance error and carrier
angle, with fault samples every 100 ticks; the final check verifies greater than 9.5 blocks of
trailing travel, agreement within 0.1 blocks, no link loss, spacing error below
0.15 blocks, angles below 0.04 radians, unchanged rider count, matching rail
binding and absence of 4D data on the ordinary bearing.

The initial fixture compared motion from the pre-settle coupling pose. Native
settling moved the trailing body about 0.114 blocks before towing, causing a
false movement-difference failure despite correct spacing. The corrected
fixture checks settled spacing within 0.001 blocks before starting the pull.
An earlier fixture completed the physical pulls but attempted to reuse a
removed guide during teardown; the corrected fixture clears those handles.
Neither failed harness run is counted as a passed validation.

## Evidence and limits

The [Arrow results](arrow-settled-results.json) and [native output](arrow-settled-native.txt)
record three successful pulls plus all 31 required native tests passing. The
[five-family results](five-family-results.json) and [native output](five-family-native.txt)
record fifteen successful pulls plus all 31 required native tests passing.
Maximum measured distance error was below 0.0001 blocks; maximum captured
angle was below 0.0009 radians. Trailing travel ranged from 9.9983 to 10.0003
blocks, with leading/trailing travel agreement within 0.00001 blocks. Both include the same existing 30-test suite
plus one temporary mechanical bench.

Required next work includes a guarded normal coupling/unlink workflow,
independent per-vehicle track eligibility, certified ordinary gear throughout
operation, maintenance/restraint and dispatch rules, ordinary-cart exclusion
from all 4D effects, mixed-consist editing, save/network/restore handling,
coupler break and unavailable-body recovery, offsets, braking/strength under
native propulsion, curved/banked clearance, nonzero Arrow control profiles,
and an assigned PAX train's catch ownership. A proven normal recovery example
is still required if feasible. This successful straight pull does not justify
closing any of those requirements or inventing a coupling limitation.

After removing the experiment, [production checks](production-restored-results.json)
confirm exact restoration of `ConsistRules`, absence of the temporary compiled
class/admission, all 30 original native tests passing, 25 unit tests passing
and the unchanged dev.14 JAR digest
`00db91b038658488676962ee85caf02692d2b8963a5ff0e9c306031459b7bcdc`.
The eight passing exact-head CI jobs from the preceding occupied checkpoint
are separately archived under `docs/evidence/0.9/occupied-ci-*.json`.

## Reproduce on the recorded source

Use Java 21 and the pinned dependencies in the recorded commit, with a clean
checkout. For the expanded bench, apply `five-family-bench-policy.patch` and
copy `five-family-bench.java.txt` to
`src/main/java/dev/fourthdimension/gametest/RecoveryCouplerBench.java`.
Run `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon`.
Do not package or distribute the experiment. Remove the temporary Java fixture,
reverse the policy patch, and rerun the original native suite afterward.
The Arrow-only fixture and its separate patch are also archived.
