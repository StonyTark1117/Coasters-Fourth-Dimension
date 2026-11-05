# FD401B selected-JAR coupled Arrow geometry sweep

On 2026-11-05, an isolated Java 21 dedicated server loaded the selected
`ec2fcda` qualification JAR (SHA-256
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`)
with Track Styles 1.2.0. RCON ran exactly one packaged named test:
`test run occupiedoriginaltrainstraversebendandbankbeforedisconnect`.
`JAVA_TOOL_OPTIONS` enabled the `fourthdimension,fourthdimension_fd402`
GameTest namespaces. The server used isolated ports 25691/25692, heap
`1536M`, and an isolated world; Gradle stopped normally with `BUILD SUCCESSFUL`.
The [server log](server.log) records both Arrow cars and all four PAX cars
visiting five sections, retaining native links, riders, seats and rotation
profiles, then braking, detaching and unloading normally. This B audit uses
the Arrow trace only.

The packaged fixture generated [native route frames](native-frames.json), a
[per-tick body/rider trace](mesh-trace.jsonl.gz), and an
[Arrow post-physics substep trace](arrow-substeps.jsonl.gz). The raw per-tick
trace had 6,544 Arrow samples and 6,947 PAX samples; the Arrow substep trace
had 13,408 steps at two steps per tick. The [stopped world](world.tar.gz),
[generated VM arguments](serverRunVmArgs.txt), [program arguments](serverRunProgramArgs.txt),
[legacy classpath](serverLegacyClasspath.txt), and [hash inventory](SHA256SUMS)
preserve the run. The selected JAR is retained in the
[qualification bundle](../../qualification/2026-11-05-ec2fcda/README.md).

The independent [substep report](audit-substeps.json) passed a 0.02-block
separating-gap requirement for prospective default single spine, default
double spine, and Original/single/double/Original/single whole-section plans.
Each plan's smallest sampled track separation was 0.0488577 blocks. After a
conservative motion bound from the nearest full tick, the smallest
post-physics lower bound was 0.0361087 for a body, 0.0361262 for a wheel,
and above 0.0672 for native riders and a conservative empty-handed vanilla
player envelope.

The separate [between-tick report](audit-interpolation.json) applied the
inspected Sable 2.0.3 snapshot interpolation to the same fresh trace. The
maximum whole-body motion bound was 0.0291551 blocks. For each of the three
style plans, 263 intervals near the global margin received direct local
geometry checks; the minimum swept lower bound was 0.03 blocks. Both audits
returned `passes=true`.

`audit-recovery-styles.py` now pins the current `BankedRecoveryExamples` and
mesh-auditor hashes. The former added a separate initial-profile scene while
this train fixture still calls `createTrain(..., false)`; the latter added
conservative player/rack checks. `audit-recovery-substeps.py` now pins the
packaged train fixture and substep exporter with the current namespace and
trace call. The existing clearance math was not loosened. The new
[`audit-selected-swept-styles.py`](../../../../scripts/audit-selected-swept-styles.py)
binds the selected JAR, fresh substep report, source hashes, archived tick
payload and inspected Sable JARs to the existing swept-geometry functions.

To reproduce, decompress `mesh-trace.jsonl.gz` into an ordinary file and run
`scripts/audit-recovery-substeps.py` with the pinned Coasters 0.1.5,
Minecraft 1.21.1 client resources, Styles 1.2.0, and Sable Rapier 2.0.3 JARs,
then `native-frames.json`, the decompressed tick trace, and
`arrow-substeps.jsonl.gz`. Run `scripts/audit-selected-swept-styles.py` with
the selected qualification JAR, the same base/Minecraft/Styles JARs,
`native-frames.json`, the archived compressed tick trace, and the passing
`audit-substeps.json`. Both scripts pin or verify the required source and
input hashes; the reports include the exact dependency hashes.

**Boundary:** The GameTest drove native Original track and exported its
occupied coupled poses. The audits reconstruct the two default Styles models
on those poses; they do not observe a styled client render or prove arbitrary
models, equipment, cosmetics, or larger network extrapolation. The separate
[selected-JAR styled client ride](../fd401b-coupled-styles-selected-20261105/README.md)
establishes styled motion and render activity, while its visual inversion
frames remain unresolved. The substep bound covers recorded post-physics
poses, not an unobserved continuous trajectory inside each Rapier substep.
