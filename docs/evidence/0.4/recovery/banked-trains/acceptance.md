# FD402 occupied original trains across a bend and bank

Dev.20 advances **FD402 in the 0.4 stage toward 0.9.0**. Original occupied
two-car Arrow and four-car PAX trains now traverse a complete quarter bend,
sustained 30-degree bank, bank-down and level runout before normal braking,
power cut, stationary recovery disconnection and native unloading. Every car,
carrier and native passenger seat keeps its original identity, and recovery
disconnection preserves the original train links.

## Route and assembly

`BankedRecoveryExamples.createTrain` extends the earlier single-car route with
48-block level approach and runout sections. The 50.5266-block quarter bend,
32-block sustained bank and 24-block bank-down remain. Total native length is
202.5266 blocks. Native banking causes a small centerline height change even
though anchors share their height; maximum absolute native tangent Y is about
0.004186. This nearly level route does not certify compact PAX spike towing;
its measured occupied propulsion limitation remains in the
[stall checkpoint](../chain-stall/acceptance.md).

`/fd demo recovery_bank_train_arrow` and `recovery_bank_train_pax` provide
original operator examples in a clear, loaded 153 by 14 by 93 corridor. Cars
start at route distances 12/18 for Arrow or 12/18/24/30 for PAX. The ordinary
bearing starts six blocks ahead of the front car. Native assembly runs after
80 world ticks, while the cars remain captured and stopped. Missing/not-ready
cars or unexpected external links reject assembly and request inspection.
The queued task is removed before execution and does not start towing.

The native regression exercises this same queue and event listener, then
checks the actual native train links after 100 ticks. It boards all eight Arrow
and sixteen PAX native seats, records each seat entity's UUID and every carrier
body UUID, and selects the ordinary bearing through sneak-use of the Rotation
Tool. Production editor handlers close restraints, retain and attach each train
before selecting the other train's bearing. This respects operator-owned
selection; an initial exploratory setup selected both bearings before attaching
and failed correctly. Its sequencing was corrected before final acceptance.

At 160 ticks, normal **Start tow** and vanilla lever interaction supply native
chain power from an eight-RPM Create motor. Only the certified bare ordinary
bearing receives longitudinal propulsion. No car receives an added actuator,
commanded movement velocity or motion teleport. The FakePlayer moves only to
perform nearby controls. The ordinary bearing has no 4D record or seat effects.
PAX is unassigned; existing occupied physical winch-handover evidence remains
separate from these routes.

## Whole-train acceptance

Every original car must remain on matching native support, retained and fault-free
with its original carriers, recovery identity and seat mounts. Every car must
visit all five sections and sample the sustained bank for more than 100 ticks.
Arrow's full-turn profile and every rail's original profile record remain
unchanged while carriers stay retained. Each adjacent original coupling and the
recovery pair is checked; every chassis and the ordinary bearing are measured
against their nearest native snapped-block anchor and track-frame quaternion.

Normal braking begins only after **all original cars** have reached at least
five blocks into the final level runout. The lever then cuts external power.
After 100 ticks, every car must be ready, chain input must be zero and the
motor must remain installed at eight RPM. Stationary recovery disconnection
must leave exactly the original train's chassis IDs linked, remove the ordinary
bearing's link and preserve retention/closed restraints. Restraint opening
precedes native unloading of all 24 original seats. The harness removes only
its owned passengers, bodies and rail records after preserving the evidence.

Required sampled bounds remain 0.15 blocks of axle-spacing error, 0.04 radians
of retained carrier angle, 0.77 blocks/s including sampling tolerance on the
unchanged 0.75 recovery cap, 0.1 blocks of native anchor offset and five degrees
of frame error. Profile-specific records contain the actual maxima and minimum
sustained-bank tick counts for each original train. The original single-car
five-family case also remains required in both profiles.


Recorded final standalone maxima:

| Train | Riders | Minimum bank ticks per car | Spacing error (blocks) | Retained angle (rad) | Speed (blocks/s) | Anchor error (blocks) | Frame error (degrees) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ARROW | 8 | 1324 | 0.000265 | 0.000561 | 0.503916 | 0.000325 | 0.046840 |
| PAX | 16 | 1421 | 0.000272 | 0.000485 | 0.486939 | 0.000309 | 0.045352 |

## Independent frames and reproduction

The shared native exporter retains `recovery-bank-native-frames.json` for the
five single-car routes and writes `recovery-bank-train-native-frames.json` for
these two original train routes. At four samples per block, the independent
checker verifies route family/count, 48-block approach/runout, normalized
orthogonal frames, continuous native joints, 90-degree heading change, the
full 32-block sustained bank, level endpoints and Arrow's nonzero full turn.
Native position gaps are zero; maximum adjacent frame-vector differences are
about 0.00016211, below the 0.001 native finite-difference tolerance. These are
track-frame/profile checks, not a mesh-clearance audit.

Standalone and combined Track Styles/Engineered profiles both pass all 37
required native tests and 25 unit tests on the same dev.20 artifact. SHA-256:
`684292ccf913d8f99af689a29c4f2eb91b665493c96c8bcbdfbbdf209469ac08`.
Both original-train and all five single-car route exports pass the independent
geometry checker in each profile.

Final profile records pin source, JAR, log and both native-frame hashes, all
37 required native tests, 25 unit tests and per-train/single-car metrics. CI runs
both independent geometry checks and retains both frame exports. Reproduce with
Java 21:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer --no-daemon
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer --no-daemon -PfdIntegrations=styles,engineered
python3 scripts/verify-recovery-bank-frames.py /tmp/fourthdimension-7af457c2/build/runs/gametest/recovery-bank-train-native-frames.json --trains
```

Use `build/runs/gametest/` in a checkout without `:` in its path. GameTests reset
only their disposable world. Keep valuable GUI worlds outside directories that
`clean` deletes. Dev.19's source, metrics and all eight CI jobs remain separately
pinned in the [single-car checkpoint](../banked-tow/acceptance.md).

## Exact-source CI

All twelve jobs on `b6888fcabb5d0daa37a63bde8c00ced8254ae55c` passed. GitHub
reported two push runs (36943634352, 36943636504) and one PR run (36943637290),
each with standalone, Styles, Engineered and combined profiles. Each full job
log confirms all 37 required native tests, five single-car and two original-train
completion markers, preserved original seats/carriers/train links and normal
brake/power-cut/detach/unload, plus both independent route geometry checks.
[Recorded run/job identities](ci-dev20.json) pin this checkpoint separately
from later source revisions.

## Remaining FD402 work

This proves native occupied recovery of these original trains across these
complete nearly level bends/banks, including original link/seat preservation
and body/track alignment. It does not certify chassis/carrier/rider or linked-car
mesh clearance, physical Arrow rack/follower clearance under retention, rendered
player/operator workflows or safe exits, Engineered recovery FE operation, a
general coupler/grade rating or the complete multiplayer/fresh-JVM lifecycle.
The failed compact occupied PAX spike pull remains unsupported. FD402, FD404
and the 0.9 release gate remain open.
