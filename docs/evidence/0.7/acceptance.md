# Milestone 0.7 acceptance

Integration owner: `01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`.
Roller Ball A–C and New-generation Flying A are integrated at `3de3d128` and
verified on origin/main. The final content/cleanup handoff closes all sixteen cards below. **Milestone
0.7 is complete on 2026-11-15.** This record is the final integration boundary.

## Sixteen-card reconciliation

| Cards | Required outcome and evidence | Status |
| --- | --- | --- |
| FD1302A–C | [Axis mechanics/occupied handoff](axis/A-handoff.md), [content and normal construction](axis/BC-acceptance.md): two-car banked roll, drive/free/damp and capture, normal Chain, distinct dock recovery and unload. | Complete; A integrated `2b805fcb`, B/C `0a49c089`. |
| FD1502A–C | [Roller Ball mechanics/occupied/restart](roller-ball/A-handoff.md), [content and normal construction](roller-ball/BC-acceptance.md): four-seat transverse native tumble, lift latch/guide, summit release, return/unload, exact-route recovery and actual saved-boundary restart. | Complete; integrated `3de3d128`. |
| FD1801A–C; FD1801 | [Conventional Flying mechanics/occupied restart](conventional-flying/README.md), [construction/content](conventional-flying/construction/README.md): separate restraints, seated/prone changeover, native travel and seated unload, interrupted posture retention. | Complete; A `5fe557f2`, B/C/outcome `fe993053`. |
| FD1802A–C; FD1802 | [Qualified shared yaw/mechanics](new-generation-flying/QUALIFIED-A.md): physical compound yaw/posture, both-posture scenery targets, unwrapped winding, four-car/reverse/fallback, loaded stopped Show, real two-client/restart and normal return/unload. | Complete: A at `3de3d128`; [normal construction/content](new-generation-flying/construction/README.md) closes B/C and FD1802. |
| FD1803 | Both independently accepted Flying outcomes feed this aggregate. | Complete from Conventional and New-generation A–C; no joint replay. |
| FD701 | FD601 plus Axis, Roller Ball and both Flying outcomes; evidence reconciliation only. | Complete; all sixteen 0.7 cards reconciled, owned runtimes stopped. |

## Shared changes and carry-forward

The native carrier and seat contracts remain authoritative. Native Chain owns
travel; moving passenger bodies and native constraints own physical rotation.
New Generation adds an independently loaded yaw deck and posture carrier;
Roller Ball adds a dedicated four-seat transverse body and constrained vertical
route. Their qualified native and real-client cases address these differences.

A real occupied Roller restart exposed duplicate conversion of saved seat-local
look by the normal native remount. The narrow shared fix stores loaded yaw/pitch,
waits for the exact restored carrier/seat on the client, and restores them once.
The exact saved-state reconnect restored both angles exactly and ordinary mouse
look persisted afterward. This is representative coverage of the changed shared
restoration path; accepted physical state, remote rider, comfort and sibling
workflows retain their evidence. It does not require every family to restart.

The additive Flying example overload uses native lifted endpoints for the NG
fixture while the Conventional default remains unchanged. Loaded NG Show now
holds the chassis with a real native world constraint; it does not brake the
legitimate translating inner COM during yaw. These named corrections have
passing affected native/client evidence. They introduce no global collision
exemption, moving-track substitute, or profile-wide qualification campaign.

Java 21 package generation passes. Native pass logs, targeted contract checks,
occupied input and render captures, actual persistence, normal crafting/placement,
and finite FD916 content reviews are combined by behavior. Raw log whitespace is
preserved; source and documentation diff checks pass. New artifact hashes,
content-only updates and this aggregate record do not invalidate accepted cases.
FD601 and earlier milestones remain closed. FD901–FD905 retain final release
qualification; this development milestone does not publish a release.

## Runtime and integration boundary

Axis, Conventional Flying and Roller Ball worlds/logs are preserved and their
owned client/server/Xvfb units are inactive. NG completed its same-owner B/C
continuation, removed temporary access scaffolds and ten construction chunk
tickets, saved the ClearShow world, then stopped client/server/Xvfb and both
observer units. [Cleanup](new-generation-flying/construction/cleanup.txt) records
all five units inactive/MainPID 0. The lead independently confirmed the units
inactive, no active 0.7 units and no private displays 316–319 or NG launch process.
No active runtime or unfinished package remains.

NG normal Survival input crafted track, two cars and a marker; a 48-block
connection consumed twelve track items, both cars were placed and bound, normal
Chain linked at exactly eight blocks, and the track form saved a TARGET section
with the actual placed marker identity. The corrected inventory/held car model
adds its missing support frame and foot links; an exact-source resource override,
explicitly selected above mod resources, passed the affected visual check. Its
native vehicle geometry is unchanged. Final resource-only changes reuse the
accepted Java 21 compile and native/client checks without another physics run.

Pre-existing untracked `logs/` is preserved. No historical branch/worktree was
reused or removed; all work remains on primary `main`. No 0.8 task or publication
is activated by this acceptance record.
