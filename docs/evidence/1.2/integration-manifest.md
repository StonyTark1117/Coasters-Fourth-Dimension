# 1.2 selective integration manifest

This manifest records the finite FD1701/FD1703 integration set after connected
client C2/C6 acceptance. It is intentionally selective: unrelated 1.3 asset and
renderer work remains outside the 1.2 package.

## Included 1.2 production sources

- `integration/RideLocalOperations.java` — fixed `arrow_fixed_circuit_v1`, six
  reservations blocks, three-train admission, dispatch guards, reconciliation,
  explicit resume and removal cleanup.
- `runtime/FoundationData.java` — persisted route, train, reservation and
  fail-closed state.
- `vehicle/VehicleLifecycle.java` — permanent native removal callback into route
  cleanup; transient unload remains unresolved and braked.
- `vehicle/VehicleOperations.java`, `runtime/CarrierRuntime.java`,
  `editor/EditorNetwork.java`, `tools/OperatorPanel.java`, and
  `client/tools/OperatorScreen.java` — production control and tick integration.
- `track/ExampleLayouts.java` and the FD1701 fixture data — reproducible bounded
  Arrow route setup.

## Accepted evidence consumed

- C1/C5: `fd1701c-final2.log`.
- C3: `fd1701c-c3c4-observer.log`.
- C4: `fd1701c-c4-ticket.log`, including documented Sable force-load tickets,
  delayed same-UUID stability, reservation equality and explicit resume.

These cases remain closed under the validation policy. C2/C6 are accepted from the
2026-11-22 connected run in the copied world. The raw
`/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/coordinator-console.jsonl`
readbacks show three physical trains and six disjoint current/next reservations:
ea2945a0 owns 0/1, f24db323 owns 2/3, and 2b1ec00a owns 4/5. The player
mounted f24's native seat, closed restraints, and dispatched through the normal
Ride panel. With one rider assignment, f24's center moved from x=18.528 to
x=33.983. At 09:55:20 the saved `server.log` records
`FD_RIDE_RECONCILE_BRAKE` for f24 with computed current=3, next=4 and
nextOwner=2b1ec00a. The next route readback shows f24 braked, its 2/3 pair
retained, and 2b's 4/5 pair unchanged. Capture and restraint release occurred
after this brake; normal dismount cleared the rider assignment. The saved world,
server log, console readbacks and `20261122T165501.098827Z-state.png` panel
view remain in that run's evidence directory. This is direct occupied physical
separation and player/server agreement; C1/C3/C4/C5 evidence above is reused.

## Disclosed fixture repair

`runtime/Fd1701FixtureRepair.java` adds the validation-only command
`/fd fd1701_repair <chassis-uuid>`. It repairs only missing-air native cells in the
identified saved fixture and rejects arbitrary non-air replacement. This is a
bounded fixture repair for the observed serialized missing-bearing state; it is
not evidence of lossless cold restore and does not change the production
fail-closed `cartCenterWorld` guard.

## Excluded from this integration

FD2101/FD2104 model-review, crossbeam and renderer work; FD2102/FD2103 variants;
unrelated generated assets; and any 1.4 work remain outside this selective 1.2
package until their owners deliver their own accepted handoffs.
