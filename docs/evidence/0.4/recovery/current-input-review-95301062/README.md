# FD402A current-input carry-forward review

**Result:** FD402A mechanics and bounded clearance carry forward to current
`main` at `953010623826352ab735eab7d2f728c7715ad2e9`. The selected qualification
artifact remains the clean `ec2fcda` JAR with SHA-256
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
No geometry, policy, solver, model, or dependency change leaves an affected
FD402A behavior without a focused result. This review does not close rendered
client, operator, persistence, multiplayer, or optional-power work in FD402B–F.

## Requirement audit

| FD402A requirement | Current evidence and conclusion |
| --- | --- |
| Five occupied single-car and linked Arrow/PAX banked routes | The [one-tick record](../current-tick-rider-clearance/acceptance.md) still verifies. The [PAX](../pax-native-physics-substeps/acceptance.md) and [Legacy/Free Spin/Revised](../remaining-family-native-physics-substeps/acceptance.md) substep verifiers pass against the current route, recorder, verifier and production-source hashes. The Arrow substep archive retains its passing result; its older source/resource pins are superseded by the later current-source records and the corrected-model review below. |
| Original IDs, riders, carriers, links and Arrow profiles through tow, brake, power cut, detach and unload | The [single-car](../banked-tow/acceptance.md), [linked-train](../banked-trains/acceptance.md), [PAX substep](../pax-native-physics-substeps/acceptance.md), and [remaining-family substep](../remaining-family-native-physics-substeps/acceptance.md) records cover the complete native sequence. Their relevant production route and recovery sources have not changed since the later focused records. |
| Current original models, player/rack clearance and certified equipment | The [wearable audit](../vanilla-wearable-clearance/acceptance.md) snapshots exactly match the current `MechanicalPartBlock`, `VehicleParts`, content generator, wearable verifier and all four corrected axle models. There are no changes to those inputs after `9cbdd7e6`. It checks the same retained traces at every tick and reports at least `0.08` block rack/track separation. The largest recorded half-step motion is `0.01314606` blocks, leaving more than the required `0.02` bound for the corrected rack geometry. The [equipment interlock](../certified-rider-equipment/acceptance.md) closes the deliberately unsupported held-item and non-vanilla-equipment cases. |
| Four-car Arrow flat strength and five-car rejection | The [strength record](../strength-rating/acceptance.md) retains its four-car occupied pull, brake and detach result. `RecoveryStrengthGameTests.java` and the FD402 structure remain byte-identical to that record. `RecoveryOperations.java` differs only by the later rider-equipment interlock: `MAX_RECOVERY_CARS = 4`, the five-car preflight/runtime checks, speed cap, stall handling and detach path are unchanged. The interlock has its own focused unit and mounted-player runtime result. |
| Wrong-family, stall, overspeed and unavailable/unsafe inputs fail closed | The [operator/native record](../operator-acceptance.md) retains forty wrong-family snap rejections and recovery availability checks. The [powered-stall record](../chain-stall/acceptance.md) retains the occupied PAX brake-and-retain result. `RecoveryOperations.MAX_SPEED` remains `0.75`, and its tick path still brakes overspeed, missing equipment, unsupported track, oversized consists and unsafe rider equipment. No later production change modifies those branches. |
| Pinned runtime dependencies | The current baseline retains Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Coasters Simulated 0.1.5 and Sable 2.0.3. These are the same physics/model dependency pins used by the current recovery evidence. |

The only later production change adjacent to native boarding is `8f370d09`:
`Boarding.ready` now admits a PAX car whose station-support hold is valid during
saved BOARDING restoration. It does not change recovery coupling, propulsion,
route geometry, model geometry, existing occupant identity, braking, detach, or
unload. Selected-JAR PAX cold-open/cycle records separately exercise that changed
station path.

## Verification performed

The repository-retained verifiers were run without launching Minecraft:

- current one-tick rider/rack clearance: pass;
- PAX native physics substeps: pass;
- Legacy, Free Spin and Revised native physics substeps: pass; and
- current source/model snapshot comparisons for wearable clearance: exact.

The Arrow-substep and wearable wrapper scripts report only their subsequently
edited `acceptance.md` checksum entries. Every other file in those two archived
`SHA256SUMS` lists matches. The raw traces, reports, verifier snapshots and model
snapshots were not changed or relabeled. The strength wrapper correctly reports
the additive `RecoveryOperations` change described above; its unchanged test and
structure hashes still match. Under the cumulative validation policy, the
focused equipment-interlock result answers that affected change, so a new broad
GameTest or client run is not warranted.

