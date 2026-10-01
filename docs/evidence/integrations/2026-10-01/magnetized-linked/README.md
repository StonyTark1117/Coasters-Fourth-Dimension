# Magnetized occupied coupled Arrow acceptance

The native Magnetized fixture now includes a two-car Arrow train with one real
Create-seated pig in each car. Both cars retain their native coupling and riders
through forward acceleration, forward braking, reverse acceleration, reverse
braking and validated recapture. Four trailing-car interlocks reject propulsion on
the otherwise valid lead car. Restoring the trailing car immediately restores the
native propulsion impulse. No production propulsion or interlock behavior changed.

## Recorded results

| Profile | Native checks | Absent-addon skips | Result |
| --- | ---: | ---: | --- |
| Magnetized | 34 registered | 5 | Passed |
| Combined pack | 34 registered | 0 | Passed |
| Standalone | 34 registered | 7 | Passed |

All nineteen unit tests pass. All three native processes exit zero. In both installed
profiles, each car reaches approximately +3.6023 and -3.6036 blocks/second; forward
and reverse braking leave less than 0.001 blocks/second. Each healthy trailing-car
counterfactual produces approximately 0.32 blocks/second of native velocity change.
These measurements describe this fixture and configuration, not arbitrary layouts.

## Setup and checks

`magnetizedLinkedArrowAndTrailingInterlocks` invokes a linked mode of the existing
`MagnetizedRideCheck`. Its independent flat 60-block Arrow track is at
(-900, 160, 200), with cars six blocks apart. The existing single-car, five-family
fixture still runs at its original coordinates. Track placement, vehicle creation,
train coupling, Create seats, dispatch, restraints and recapture use their normal
native APIs. All riders board before restraints close. Cleanup unloads both cars
before removing either chassis.

The powered native anchor uses MAGNET at 10% and BRAKE at 100%. The fixture sets an
initial velocity of +1 or -1 blocks/second because Magnetized chooses its acceleration
direction from existing motion. The reverse stage tests powered reverse travel on
flat track, not gravity-driven rollback on a slope.

The trailing-car probes independently set held, open-restraint, maintenance retention
and fault states. They call the actual native chain-drive method that contains
Magnetized's injection and verify that the lead chassis receives no velocity change.
Each probe then restores a healthy trailing car and repeats the same call; an impulse
greater than 0.001 must occur. These state mutations are synchronous test probes,
restored before the next tick. They are not an operational dispatch bypass.

Normal dispatch follows the probes. Both cars must exceed +1.1 blocks/second forward
and -1.1 in reverse; both brake stages must reduce speed below 0.15. Both cars must
remain fault-free with mounted riders, retain a two-car native consist, and reach
boarding readiness after capture. Only then can normal restraint opening and rider
unloading occur. The original five-family run continues to verify PAX exclusion
and Free Spin's independent selected-carrier magnetic equipment.

## Reproduction and evidence

Use Java 21 and run these sequentially from the repository root:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer -PfdIntegrations=magnetized
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion -PfdPaxEnergyReload
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer
```

The combined profile's FE flag preserves the existing occupied PAX energy checks.
Each native process uses the normal disposable GameTest world. Optional fixtures
record explicit skips; those are registered successes, not compatibility evidence.
The base profile verifies that the new fixture remains isolated when Magnetized is
absent. Full compressed logs, relevant events, exit codes, source delta, unit XML,
compiled-class/JAR hashes and an independent verifier retain the exact checkpoint.

```sh
python3 scripts/verify-magnetized-linked.py docs/evidence/integrations/2026-10-01/magnetized-linked
```

`verification.json` records per-car measured speeds, four healthy counterfactual
impulses per installed profile, and occupied recapture. The verifier checks the raw
log markers, registered native totals, explicit skips, nineteen unit results and
evidence hashes. Its numerical thresholds are the actual native test requirements.

The initial compile required explicit server-body casts. The first native attempt
put the new track at x=-1250; normal placement rejected it outside the supported
1,024-block boundary. Both failures are retained as `compile-first` and `layout-first`
and excluded from passing evidence. The corrected location stays within that limit.

Pinned environment: Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Sable 2.0.3, Coasters Simulated 0.1.5,
Fourth Dimension 0.9.0-dev.1 and Magnetized 1.1.0 (CurseForge file 8668703).
The combined profile adds Engineered 1.1.1, Track Styles 1.2.0, Additions 0.2.0,
Immersion 0.2.3, Aeronautics 1.3.2 and Interiors 0.6.1v3.

This is a two-car Arrow, flat-track development-runtime checkpoint. It does not
certify arbitrary train length, curves/slopes, braking distance, configuration
extremes, saved Magnetized anchor-mode migration, occupied restart/removal,
chunk interruption, client equipment rendering or multiplayer. Existing dependency
loader warnings remain in the logs. Full FD901–FD904 acceptance remains open.
