# FD402 fresh-process recovery lifecycle checkpoint

This checkpoint records a normal occupied recovery group surviving a client/server
process shutdown and completing its normal release in a second process. The
initial Arrow observation was extended to all five single-car families below.
These are bounded historical development-client observations; exact-artifact
identity is missing from those runs. A later selected-production linked-train
extension resolves the remaining FD402D uncertainty; multiplayer acceptance
remains separate under FD402E.

The opt-in pause fixture creates the normal recovery example, boards the local
player, closes restraints, retains the car, and uses the production select and
attach operations. It then remains braked and linked; the first client exits
only after observing the rendered native link. No second example is built.

On the next process, the resume fixture rejects an empty world, a missing
ordinary bearing, a missing native link, a non-retained car, a non-braked car,
or missing original motor/lever controls. It accepts only the saved linked
chassis and bearing IDs. The normal player restore path remounts the rider;
the fixture then starts the native chain, brakes, detaches, opens restraints,
and unloads the rider through the production operations.

The development-client check was run in two separate JVMs against one world:

1. Pause stage: linked `af475614-e2a8-4295-b8e9-b3d7b382bfea` to ordinary
   bearing `00133676-a77d-413f-849c-d7d2815edc45`, and exited successfully
   after 40 rendered client samples.
2. Resume stage: restored that same native link and mounted player, observed
   more than 5.7 blocks of chassis and bearing displacement, then passed the
   normal brake/detach/restraints-open/unload lifecycle. The process exited
   successfully.

The original fixture arguments below describe the two stages. Before a new run,
follow [AGENTS.md](../../../../../AGENTS.md): select and verify Java 21, check
memory, reserve an owned private Xvfb, give both stages the same isolated game
directory, use `-PfdClientMaxHeap=1536M`, and cap each entire client launch at
the current 6 GiB default. These argument excerpts are not standalone uncapped
launch instructions.
Use an empty validation world first, then reopen that same world:

```sh
./gradlew --offline --no-daemon runClient \
  -PfdCarrierFixture -PfdCarrierFixtureRecovery \
  -PfdCarrierFixtureRecoveryPause \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRider \
  -PfdCarrierValidationRender

./gradlew --offline --no-daemon runClient \
  -PfdCarrierFixture -PfdCarrierFixtureRecoveryResume \
  -PfdCarrierFixtureRecoveryCycle \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion \
  -PfdCarrierValidationRecoveryCycle -PfdCarrierValidationRider \
  -PfdCarrierValidationRender
```

The initial Arrow result is fixture-operated and one-player. It does not establish
ordinary player tools/UI, Engineered/FE operation, full multiplayer behavior,
or broad rendered clearance. The later rows extend family coverage only.

## Family extensions (2026-11-04)

The same two-process checkpoint was also run for the flat `PAX` fixture in
`/tmp/fd402-pax-lifecycle.4576DY`, with each game JVM capped by
`-PfdClientMaxHeap=1536M`. The pause stage observed the rendered native link
for chassis `055e186a-6dbe-433f-a821-8be61434bcf8` and ordinary bearing
`8aed87a5-bf2d-440a-8467-3bc86ed5389a`. The resume stage restored that link,
observed 5.742 blocks of motion for both bodies, and passed after production
brake, detach, restraint opening, and rider unload. No out-of-memory event
occurred.

The same capped two-process check also passed for Legacy
(`FD402LegacyLifecycle`, 5.667 blocks), Free Spin
(`FD402FreeSpinCycle`, 5.384 blocks), and Revised
(`FD402RevisedLifecycle`, 5.355 blocks). Free Spin exposed a normal native-unlink
packet arriving before its larger carrier snapshot cleared `recoveryCart`; the
client validator now recognizes that observed unlink plus rider dismount as the
same completed production release. This completes the observed bounded all-family
one-player lifecycle matrix. Artifact provenance, packaged qualification and
multiplayer evidence remain separate requirements for these historical rows.

## Selected-production linked-train extension (2026-11-05)

The [current selected-production checkpoint](../linked-train-fresh-process-656725bc-20261105/README.md)
adds the original linked two-car Arrow and four-car PAX cases without replaying
the historical five-family matrix. Each occupied train was saved braked and
linked to one ordinary bearing, exited its first JVM, restored the exact native
graph in a second JVM without automatic motion, then explicitly towed, braked,
detached, opened restraints and unloaded normally. The retained final saves have
neither recovery links nor player `RootVehicle` assignments.

The clients used the selected clean JAR's production entries plus a focused
validation-only `CarrierValidationFixture` overlay. Its archive has the same 394
entries as selected SHA `656725bc…`; exactly the fixture class and javac's
synthetic switch helper differ. All production persistence, recovery, boarding,
vehicle, native-link and rendering entries are byte-identical. Together with the
retained five-family observations, this closes FD402D under the cumulative
validation policy. FD402E still owns two-client interruption coverage.

## Retained observations and artifact boundary

On 2026-11-04 the original pause and resume logs were recovered from all five
game directories and archived here, before relying on temporary local paths.
[observations.json](observations.json) records their full hashes, source locations,
same-chassis pause/release messages and client validation results;
[SHA256SUMS](SHA256SUMS) protects the retained payloads.

| Family | Pause record | Resume record | Recorded displacement |
| --- | --- | --- | --- |
| Arrow | [Log](arrow-pause.log.gz) | [Log](arrow-resume.log.gz) | 5.741 blocks |
| PAX | [Log](pax-pause.log.gz) | [Log](pax-resume.log.gz) | 5.742 blocks |
| Legacy | [Log](legacy-pause.log.gz) | [Log](legacy-resume.log.gz) | 5.667 blocks |
| Free Spin | [Log](free-spin-pause.log.gz) | [Log](free-spin-resume.log.gz) | 5.384 blocks |
| Revised | [Log](revised-pause.log.gz) | [Log](revised-resume.log.gz) | 5.355 blocks |

These development clients did not retain a launch-time source/binary manifest.
Source commit and runtime-JAR digest are therefore explicitly unknown in the
observation index; a JAR built later would not recover that missing evidence.
The logs establish observed completion, not equivalence to the
[current qualification artifact](../../../../CURRENT-QUALIFICATION.md).
The selected-production linked-train extension above supplies the scoped current
check that the roadmap required, so missing provenance in these historical rows
does not reopen FD402D. FD402E separately requires two-client dedicated-server
interruptions. Neither the fixture-operated one-player result nor a render-path
counter proves that broader claim. Final transient replicated rider-assignment
counts are retained in the raw results and are not relabeled as cleanup proof;
the linked-train record audits the retained pause and final saves separately.
