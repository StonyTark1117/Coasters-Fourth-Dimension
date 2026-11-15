# Milestone 0.8 acceptance

**All 23 cards are complete, 2026-11-15.** Integration owner
`01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`.
The named operations profile and individual capabilities below satisfy FD813.
No 0.9,1.2, moving-track work or public release is activated.

## Twenty-three-card reconciliation

| Cards | Required outcome and evidence | Closure |
| --- | --- | --- |
| FD501B–D (3) | [Exact Additions/CTNB/Magnetized dependency dispositions](dependencies/README.md), unchanged core and no migration. | Complete. |
| FD1701A (1) | [Ride-local ownership](authority/README.md): stale/foreign/held authority guards and normal occupied claim, transfer, receiving dispatch and unload. | Complete, `cc094ada`. |
| FD1401A–C (3) | [Pipeline mechanics and occupied forward/reverse/recovery](pipeline/README.md), [normal construction/content](pipeline/construction/README.md): independent native Y dynamics, crafted content, paid P track, normal coupling/station controls, return/unload. | Complete, `b47bce4e`, `e713c9a0`. |
| FD1501D–F (3) | [S2 launch/inversion/rollback/governor/loss and occupied handoff](spinning-s2/README.md), [crafted content and player acceptance](spinning-s2/content/README.md), real saved-state restart. | Complete, `17f59751`, `d3037ae0`. |
| FD1702A–C; FD1702 (4) | [Dive native hold/drop, occupied interrupted release/restart, return/unload, crafted/placed content and controls](dive/README.md). | Complete, `57791c9e`, `4a771b1a`. |
| FD1901A–C; FD1901; FD1902 (5) | [Powered Inverted Yaw](powered-inverted-yaw/README.md): one/four-car upright full-turn travel/show/reverse, actual drive/controller loss, normal crafted content, occupied interrupted-show restart, same-car return/unload and finite scene output. | Complete, `a3d6a2ef`, `d4472253`. |
| FD810 (1) | [Additions whole-consist telemetry, native signal/automatic-request guards and normal occupied operator example](additions/README.md). | Complete, `2358796a`. |
| FD811 (1) | [Seven measured CTNB materials, exact eligibility/profile preservation, real provider joins, native controls and normal repaint/place/bind/occupied reverse/station/unload](ctnb/README.md). | Complete, `7bade620`. |
| FD812 (1) | [Actual Magnetized callbacks, Wing/S2 owner transfer, bounded start/brake/reverse/power behavior and normal occupied configuration/return/unload](magnetized/README.md). | Complete, `694be3d0`. |
| FD813 (1) | [Actual combined installed force/controller conflicts](operations/README.md) plus all named outcomes above and FD701 carry-forward. | Complete; no required case remains. |

## Shared behavior and carry-forward

FD1701A supplies one ride-local longitudinal owner with fresh release tokens and
fail-closed loss/transfer. New families consume it through their native physical
vehicles, constraints, seats, restraints and recovery rules. Optional drive fences
prevent duplicate addon/native force without affecting ordinary unmanaged carts.
CTNB accepts only measured native-contact Wing/Arrow tracks; Magnetized accepts
Wing/S2, and Additions requests operate through the established station contract.
Native installed conflict probes cover their actual competing callbacks together.

Individual mechanics, normal player input, rendering, content and persistence
records combine by behavior. The native independent-car cases do not replace
occupied proof; each new family has its required real-client occupied handoff.
Unchanged shared rider synchronization and earlier milestones retain their accepted
evidence, including the representative two-client and restored-look contract.
No blanket replay was scheduled for a new JAR hash or test-only collector repair.
The new CTNB input bridge changes only selected material use; its normal packet
workflow passed. Powered's final recovery correction passed its affected loss
case and the same saved car's normal return/unload.

The named operations profile is `styles,engineered,additions,ctnb,magnetized`.
Its actual control conflicts passed 1/1; the individual adapter and family evidence
provides their distinct coverage. This does not certify future presentation/seat
combinations. FD916 consumes the finite accepted content views later; FD901–905
retain release qualification. Historical bounded run notes do not reopen completed
siblings, and no remaining required criterion is hidden as cosmetic polish.

## Runtime and artifact boundary

Every 0.8 owner stopped its client, saved/exited its server, then stopped its private
Xvfb. The lead independently checked final Dive317, Powered318 and CTNB316 units
inactive/MainPID0; earlier Pipeline, S2, authority, Additions and Magnetized runs
retain their recorded completed cleanup. No active 0.8 unit/client/server/display
or live handoff remains. [Final runtime inventory](runtime-cleanup.txt) records
this boundary; valuable worlds, raw logs and screenshots remain on persistent
storage outside build directories.

Java 21 package generation passed after the final source changes. Development JAR:
`/mnt/NVME4/fourthdimension-runs/fd08-operations-20261115/combined-build/libs/coasters-fourth-dimension-0.9.0-dev.27.jar`,
SHA256 `47197e11773f5b92fa6471fc5fcdb9e66bdb2d5a34ac8c6092dab4cf26c04611`.
The existing development version label does not activate milestone 0.9 or publish
a release. Source/docs diff checks pass; raw log whitespace remains unchanged.
Main contains all owner changes. The final response records the exact pushed
remote commit; pre-existing untracked `logs/` remains preserved.
