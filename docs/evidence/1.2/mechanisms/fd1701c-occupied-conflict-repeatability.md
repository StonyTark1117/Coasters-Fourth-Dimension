# FD1701C occupied-next conflict repeatability

The real connected replay on 2026-11-22 supersedes the synthetic fixture
proposal below. The player resumed and dispatched f24 on its existing rail 2
binding; its native body moved into rail 3 while 2b retained block 4. The
validation-gated `FD_RIDE_RECONCILE_BRAKE` log recorded computed current=3,
next=4 and nextOwner=2b before manual Capture. The saved console route state
retained f24's 2/3 reservations and braked it. See
[the integration manifest](../integration-manifest.md) for the accepted C2/C6
evidence. No synthetic binding edit was used.

Read-only diagnosis of the saved copy
`/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/server-repair-copy`.
No runtime, world, or production file was modified.

## Current saved precondition

The persisted `ride_local` state contains three disjoint pairs:

```text
ea2945a0...  block 0 -> 1, reservations 0 and 1, braking/unresolved/restored
f24db323...  block 2 -> 3, reservations 2 and 3, braking/unresolved/restored
2b1ec00a...  block 4 -> 5, reservations 4 and 5, braking/restored
```

The staged body is currently stopped near
`(41.427,-55.875,11.415)`, but its declared rail binding remains route index
4, which maps to block `4` and next block `5`. `RideLocalOperations.resume`
recomputes that same pair, checks that both owners are itself, then clears
`braking`, `unresolved`, and `restored`. It cannot create an occupied-next
conflict in this state.

## Historical proposed conflict fixture (unused)

Preserve the saved copy as a baseline. In a new validation-only fixture, keep
the six reservations above and set one admitted train's declared rail binding
to the following route segment without changing its stored route pair. For
example, change the f24 lead's binding from route index 2 (`2 -> 3`) to route
index 3 (`3 -> 4`) while leaving its stored train and reservations at `2 -> 3`.
Block `4` is already owned by staged chassis `2b1ec00a...`.

Then invoke the normal reconciliation path with that train not already braking.
The expected authoritative result is:

```text
FD_RIDE_RECONCILE_BRAKE
  chassis=f24db323-ddfa-4a9f-a58d-267b7ad92b8a
  reason=occupied-next current=3 next=4
  nextOwner=2b1ec00a-cc1a-488b-99be-98ab18dc6d67
```

The train must remain stored at `2 -> 3`, retain its reservations, and become
braked with native velocity reset. This is the direct C2 occupied-next guard.
An earlier snapshot with this deliberately mismatched binding would also work;
the current saved copy contains no such state, and an explicit Resume is a safe
baseline check rather than a conflict reproduction.
