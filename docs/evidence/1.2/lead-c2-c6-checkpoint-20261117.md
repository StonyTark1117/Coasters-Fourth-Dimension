# FD1701 C2/C6 checkpoint — 2026-11-17

This historical checkpoint was resolved by the coordinator's 2026-11-22
connected replay. The [integration manifest](integration-manifest.md) records
the accepted C2/C6 physical separation and player/server evidence; the
"remaining acceptance" section below describes the earlier handoff only.

The connected-client runtime was transferred from Art to Lead after Art released
all units. The original Art server/world remains untouched. Work used the copied
world:

`/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/server-repair-copy`

Lead-owned runtime resources were display `:329`, server port `25668`, RCON
`25669`, Java 21, client heap `1536M`, client scope `6G`. Server, client and Xvfb
were stopped cleanly after the run; all owned units reported inactive/MainPID 0.

## Completed in this run

The serialized chassis `ea2945a0-d2c8-4a23-b152-f113b742e8c9` was missing its
native `coaster_cart` and `arrow_controlled_controller` blocks. The validation-only
`/fd fd1701_repair <uuid>` helper restored only those missing-air cells on the copy.
It preserves the vehicle UUID, carriers and route data and rejects non-air
conflicts. This remains disclosed fixture repair, not lossless cold-restore proof.

The normal connected Ride operator panel then completed **Resume route**. Final
readback was:

```text
route=arrow_fixed_circuit_v1 trains=1 reservations=2
chassis=ea2945a0-d2c8-4a23-b152-f113b742e8c9 block=0 nextBlock=1
center=(9.548790350015718,-59.50120975049769,46.58352104102707)
braking=false unresolved=false restored=false
```

## Remaining acceptance

Execution ownership is **Lead** after the explicit transfer from Art; Art's prior
connected run is retained as evidence only.

C2/C6 remain open. No evidence yet proves three physically occupied trains
interacting through disjoint current/next block pairs with authoritative owners.
The other known chassis are `f24db323-ddfa-4a9f-a58d-267b7ad92b8a` and
`e050dec4-ade7-4c73-80db-4e3b37af2518`; `/fd fd1701_list` reports their native
centers. The next concrete action is to select each overhead chassis with the
Rotation Tool from a verified aim, open the normal Ride operator panel, admit it,
and record route state after each admission. Do not substitute native-only
admission or physical availability counts.

The usage meter later reported 100% with no credits, but the account still
reported ordinary usage allowed and no rate-limit or spend-control rejection.
That meter alone is not a runtime blocker; C2/C6 remain open and the next
authorized run should resume from this checkpoint.

Accepted C1/C3/C4/C5 remain closed. Incomplete FD2101/FD2104 work is outside this
checkpoint and must not be integrated into the 1.2 package.

## Execution handoff — 2026-11-17

C2/C6 execution is released to coordinator `01a1a961-64bb-7701-9a19-2acee5ff23a3`.
The last Lead-owned units are inactive: `fd12-lead-c2c6-20261117-client.service`,
`fd12-lead-c2c6-20261117-server.service`, and
`fd12-lead-c2c6-20261117-xvfb.service`. The reserved display was `:329`; server
and RCON ports were `25668` and `25669`. The copied server/world remains at
`/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/server-repair-copy`,
with client files at
`/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/client`.

The latest connected player was `Dev`. The last authoritative route readback
still showed one train (`ea2945a0-d2c8-4a23-b152-f113b742e8c9`) with block `0`,
next block `1`, and two reservations owned by that chassis. The latest attempted
target was the overhead chassis `f24db323-ddfa-4a9f-a58d-267b7ad92b8a`; fresh
helper input was recorded under `evidence/20261117T1115*`, but no valid chassis
bind or admission was observed. The exact last F3 capture is
`/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/f3-seat.png`.

No runtime input or C2/C6 helper edits remain with Lead after this handoff.

## Coordinator read-only route diagnosis — 2026-11-22

The later connected attempt admitted `f24db323-ddfa-4a9f-a58d-267b7ad92b8a`
on block 2 with next block 3. Admission of
`e050dec4-ade7-4c73-80db-4e3b37af2518` was refused by the normal operator path
because its current position is on ordered Arrow rail index 3, which maps to
block 3 with next block 4. Block 3 is already reserved by f24 as its next
block. This is the intended guard in `RideLocalOperations.admit` (the block
pairs are `[0,1]`, `[2,3]`, `[4,5]`), not a source defect. The minimum
operational repair is to advance or reposition e050 to rail index 4 (block
4→5), or advance f24 before retrying admission. No code change is indicated.
