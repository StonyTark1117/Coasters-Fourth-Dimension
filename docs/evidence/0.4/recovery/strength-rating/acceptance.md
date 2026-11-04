# FD402 occupied recovery strength rating

This checkpoint replaces an unbounded Arrow recovery promise with an explicit,
tested production boundary: one certified bare ordinary bearing may recover at
most **four original cars**. PAX already has a four-car native consist limit;
the same recovery rating now applies to Arrow and to every saved recovery group.

This is not a universal force or grade rating. The certified recovery vehicle
remains the pinned, unmodified Coasters bearing, the supported drive remains the
native chain at the documented eight-RPM example setting, and the compact
occupied PAX spike remains a demonstrated unsupported pull.

## Production behavior

`RecoveryOperations.MAX_RECOVERY_CARS` is `4`. A fifth car is rejected before
the operation assigns any recovery identity or creates a bearing link. Native
coupling callbacks also refuse an oversized group. A saved or otherwise
pre-existing oversized group cannot restart; its server tick brakes the group
with a specific instruction to split the train before resuming. Detach remains
available so an operator can safely recover from that state.

## Physical boundary test

The isolated `fourthdimension_fd402` GameTest builds two actual flat Arrow
consists. It first links five captured cars and verifies that production attach
rejects them without partial state. It then links four cars, boards all sixteen
native seats, closes restraints, retains every carrier, and attaches one bare
ordinary bearing through the production recovery operations.

Only the bearing receives native chain propulsion. After normal Start tow and
the eight-RPM example drive, the rated train records:

| Measurement | Result | Requirement |
| --- | ---: | ---: |
| Bearing travel | 4.63647461 blocks | >3 blocks |
| Maximum adjacent spacing error | 0.00012251 blocks | <0.15 blocks |
| Maximum retained carrier angle | 0.00061184 rad | <0.04 rad |
| Maximum body speed | 0.45923735 blocks/s | <0.77 blocks/s |
| Original occupied seats | 16/16 retained | all |
| Oversized boundary | five cars rejected | reject |

Normal braking stops all four cars. Stationary detach removes only the ordinary
bearing link and leaves exactly the four original Arrow chassis linked. The
isolated namespace ran two required tests: the strength case passed, and the
optional Engineered case reported its expected absent/uninspected skip. The
GameTest server reported all two required tests passed and Gradle exited
successfully.

[`verify.py`](verify.py) checks the compressed server log, source and fixture
hashes, exact test counts, rating assertions, measured bounds, and archived
source snapshots without rerunning Minecraft. Full-suite/profile verification
remains FD404 work.

## Remaining scope

The prior occupied four-car PAX flat/banked recovery and driven-stall evidence
remain required alongside this new four-car Arrow boundary. This result does
not support added ordinary blocks, more than four cars, the compact PAX spike,
steeper grades, arbitrary motor settings, equipped/cosmetic player meshes,
multiplayer lifecycle coverage, or within-substep trajectories.
