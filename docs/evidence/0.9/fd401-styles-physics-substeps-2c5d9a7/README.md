# FD401 Styles clearance at native physics substeps

The [audit report](report.json) passes a 0.02-block separating-gap requirement
for the coupled, occupied two-car Arrow recovery route at every recorded Sable
Rapier post-physics substep. The dedicated server ran the named
`occupiedoriginaltrainstraversebendandbankbeforedisconnect` GameTest once with
Styles 1.2.0 on the classpath. Both Arrow (two cars, eight riders) and PAX
(four cars, sixteen riders) completed five sections and the normal
brake/disconnect/unload path. Only Arrow's seven native bodies were recorded
at physics-substep resolution. The [server log](server-console.log.gz) retains
the run output and normal shutdown.

[`RecoverySubstepTrace.java`](../../../../src/main/java/dev/fourthdimension/gametest/RecoverySubstepTrace.java)
captured poses after each Sable physics substep. The
[raw substep trace](recovery-bank-train-arrow-substeps.jsonl.gz) contains 13,414
sequential post-physics steps at two steps per tick, including a completion
marker. Its 6,548 full-step poses in the audit window match the
[native tick trace](recovery-bank-train-mesh-trace.jsonl.gz) exactly for every
tracked Arrow body. The separate [native route frames](recovery-bank-train-native-frames.json)
and tick trace cover the complete route, riders, and body block inventory.

[`audit-recovery-substeps.py`](../../../../scripts/audit-recovery-substeps.py)
reconstructs the inspected default Styles meshes from the pinned provider JAR.
It checks single spine on every section, double spine on every section, and the
saved Original → single → double → Original → single whole-section plan. For
each body it subtracts a conservative bound on rigid motion to the nearest
tick sample from the sampled geometry clearance. The local radius includes
native blocks and wheel meshes, plus fixture rider and conservative vanilla
player envelopes where mounted. It checks neighboring cars separately.

| Plan | Arrow tick poses | Physics steps | Least wheel/body lower bound | Required gap |
|---|---:|---:|---:|---:|
| Single spine | 6,548 | 13,414 | 0.0359836 | 0.02 |
| Double spine | 6,548 | 13,414 | 0.0359836 | 0.02 |
| Whole-section transition | 6,548 | 13,414 | 0.0359836 | 0.02 |

The common minimum comes from the native full-spin wheel near an Original
running support; it is independent of which Styles spine decorates the other
sections. The conservative rider/player lower bounds are at least 0.0672
blocks. The report pins the source files, base/Minecraft/Styles/Rapier JARs,
provider model resources, and raw input bytes. The trace instrumented the
development GameTest classes; production geometry sources match the frozen
`2c5d9a7` candidate, but this was not a run of the exact frozen whole JAR.

To reproduce locally, decompress the archived tick trace and run the auditor
with the same four JARs, route frames, raw tick trace, and compressed substep
trace in that order. The source and JAR hash checks reject changed inputs.
`SHA256SUMS` records the archived files and auditor source. The original
uncompressed tick-trace SHA-256 is in the report's `inputHashes`.

This bounds the **recorded post-physics poses** for the inspected default
meshes and player envelopes. The solver does not expose a continuous
trajectory inside each discrete substep. Client extrapolation, equipped or
cosmetic player geometry, arbitrary resource replacements, and pixel-isolated
ownership of every provider tie/native support remain separate questions.
