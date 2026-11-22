# 1.3 car-model integration — 2026-11-22

**FD2101, FD2104, FD2102 and FD2103 are accepted.** Primary `main`, starting
from `b5e78f31b72a6c22dae25219d1eefa1f5de20a3e`. This is an unpublished
implementation checkpoint; no release tag, upload or stable promotion is implied.

| Required outcome | Evidence and disposition |
| --- | --- |
| Evidenced base defects | Prior [FD2101 repairs](fd2101/README.md) retained. The finite P1 Pipeline, P2 S1/S2 mounting and P3 Axis/Suspended brake repairs passed actual assembled geometry and affected client checks. |
| Broader base refinement | [Finite W1/I1/F1 work and readiness](base-refinement.md) accepted. Wing shell/material/shoe treatment and Fixed Inverted frame/shell hierarchy go beyond direct gap repair; Flying saddles add a metal support path. The [17-style reference review](model-reference-review.md) plus [assembled reconciliation](base-structure-reconciliation.md) give explicit dispositions. Remaining stylistic suggestions are optional future work. |
| Two geometric body choices | [FD2102 acceptance](body-variants/README.md): compact Wing and open-frame Fixed Inverted, 22 exports including all 16 Wing dyes and four picker miniatures, repeatable generator and positive-area contact audits. Preserve original pivots, seat count, restraint meshes, rider/camera ownership, track and outer envelopes. |
| Usable selection and persistence | Normal Tools → Car body preview/cancel/Apply, separate upholstery, occupied ride/stop/unload and cold restart passed for both. Native tests passed 2/2 through production action82 with exact state comparisons and atomic invalid/stale/permission/occupied/moving denials. |
| Unavailable choice/resource behavior | Unknown and wrong-family saved IDs retain their raw value but resolve to Original. Actual private missing-texture reload falls back and restoration recovers the alternate without changing saved data. |
| Affected package/content | [Package comparison](body-variants/package.txt): both independent Java 21 builds are byte-identical, and all 85 changed exports equal source. New classes/resources are present. Builder instructions and roadmap/queue are reconciled. |
| Runtime completion | Base refinement, Flying, Spinning, Axis/Suspended, native tests and both body-client records retain clean shutdowns. Coordinator verified the final exact variant units inactive/MainPID0. Saved worlds and raw logs preserved. |

Selected artifact: `coasters-fourth-dimension-1.1.0.jar`, SHA-256
`90ddc4e88d0fc280cc572e0a821dfdad5520a30e5f609c5578dab087417ca24b`,
independently built under `fd13-base-20261122/build` and
`fd13-wing-body-20261122/build` on persistent NVME4 storage. The existing 1.1.0
artifact label is retained; this checkpoint does not perform release versioning.
Actual source/export hashes are in each evidence record.

## Evidence reuse and limits

Base repairs/refinements completed before variant implementation. Earlier
FD917 and 1.0/1.1/1.2 acceptance remains closed: physics, propulsion, construction,
seat placement, capacity, camera and operator mechanics were not changed by the
body choices. The shared renderer addition is restricted to Wing/Fixed Inverted
fixed shells beneath existing restraint and palette wrappers. Both affected
families were observed; no family×profile campaign is added.

P3 Axis/Suspended rotated views are empty static fixtures, combined with analytic
attachment/clearance and retained occupied mechanics. Native body occupancy uses
an actual pig seat mount and movement uses disclosed velocity injection; the
separate real-client runs establish player controls and rides. Variant worlds
use command/retained fixtures and disclosed camera positioning, not new normal
construction evidence. Resource fallback was exercised on Wing's shared wrapper;
Fixed Inverted's distinct geometry/orientation and client workflow were checked
separately. Same-picker reload-ID lifetime is a code-review result. Warning and
setup limitations remain in the raw run records; none is silently relabelled.

No required 1.3 case remains. Optional aesthetic work and 1.4 mechanics are separate
assignments. Pre-existing untracked `logs/` and all historical worktrees remain
preserved. Integration commits and exact remote verification are reported by the
coordinator after this manifest is committed.
