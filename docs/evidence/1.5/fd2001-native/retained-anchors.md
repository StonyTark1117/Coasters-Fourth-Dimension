# Retained body frames and physical dock locks

2026-11-22, owner `/root/fd13_body_design`; shared schema/runtime integration is
owned by the parent coordinator.

## Named cold-reload defect

The same-artifact occupied cold run in
`/mnt/NVME4/fourthdimension-runs/fd15-occupied-20261122/evidence/attempt-3/`
kept the bed's physical origin but shifted the Wing seat geometry. `power-hold.txt`
and `cold-hold.txt` retain the exact before/after identity, pose and rider state.
The saved local body position represented the capture-time center of mass. On
restore the adapter paired it with the newly loaded body's current rotation point,
which can differ while rider mass is restored. This mixed two physical anchors.

New captures save the exact plot-local constraint anchor with the corresponding
bed-local rigid pose. Restore uses that anchor regardless of the currently
reported center of mass; it never teleports a live body. Root-owned persistence
roundtrip/legacy/corruption tests cover the new optional per-body anchor record.
Actual occupied cold-process confirmation remains the root owner's case.

Experimental legacy retained records without anchors are preserved. The adapter
refuses travel/release, independently world-holds each actual surviving body and
keeps the bed brake active for validated service evacuation. It does not invent
an anchor from the new mass distribution or silently drop the assembly.

## Named rolling-entry defect

`pax-recovery-2.log` reached normal original PAX lift/outbound travel, then a leading
car's rolling entry displaced the nominally docked bed 0.28856 blocks with native
Z velocity 3.45376 blocks/s. The previous adapter exposed a locked graph bridge
while retaining only the finite travel motor, so `locksEngaged` did not represent
an actual rigid dock lock under train load.

Docking now installs a real native all-axis world-to-bed constraint after measured
alignment/stopping and before graph publication. Publication failure rolls back
the new lock. Disconnection publishes the removed bridge before dropping the
physical dock lock. `liveDockEdge` requires that exact valid lock in addition to
measured alignment, stopped readback, saved identity and phase. The changed PAX
entry/recovery case is the focused physical verification; passed sibling endpoints
are not replayed merely because the adapter source changed.

The focused legacy physical check passed 1/1 in `legacy-anchor-2.log`, with
`world-legacy-anchor-2` and `legacy-anchor-2-source.sha256` preserved under the
native run root. It removes anchors from a disclosed in-process disk-state
roundtrip of a real retained Wing, checks 50 ticks of actual independent native
support within 0.025 blocks, and verifies travel/release denial and unchanged
legacy identity records. Clean all-dimensions save/shutdown and exit 0 followed.
The initial diagnostic found a mutable-vector bug in the new independent-hold
helper; copying the local anchor before transforming it to world space fixed the
actual native constraint validation failure.

## Further cold diagnosis

The root's `attempt-4` same-artifact occupied cold case still shifted with saved
anchors, so anchor persistence alone is not claimed as the completed cold fix.
Inspection of the pinned upstream Sable 2.0.6 source (tag commit
`ad8b4d377741fbd923f4d863b2cf9e462daff9a6`, `joints.rs`) shows native joint anchors
are recomputed against native center of mass every tick. A stale cached native
joint offset was therefore rejected as the diagnosis.

The project's existing `NativeLoading.update` explicitly reconciles Sable's
saved occupied origin with rebuilt offline rider mass on its first sample.
The new retained-family early return bypassed that routine altogether. The
shared coordinator owner is restoring mass/rider bookkeeping for retained
carriers while continuing to suppress their family travel solvers. Cold-process
geometry acceptance remains open until that changed path is actually observed.

## Cold repair verified, 2026-11-26

The coordinator's [attempt 5](../occupied/README.md#attempt-5--occupied-cold-recovery-and-normal-departure-accepted) now verifies the changed mass-maintenance path with two real clients and an actual same-artifact server process restart. The original occupied seat world position differs by only 9.7421e-7 blocks, the bed/child geometry and stable rider assignment agree, FAULT_HOLD persists, and explicit normal Resume, Release, fixed-track departure and normal unload pass. All five owned runtime units stopped after clean save. This supersedes the pending cold acceptance statements above without changing the failed raw records.
