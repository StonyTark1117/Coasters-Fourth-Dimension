# FD2008 occupied Wing handoff and cold recovery

Accepted 2026-11-27. Two real clients (Dev rider, Observer operator), dedicated
server on loopback 25861, private Xvfb :361/:362. Both game heaps 1536M, each
Minecraft-only limit 6 GiB; server heap 2 GiB, limit 4 GiB. Java 21. Root owns
this run. Final five units are inactive, PID 0, Result success in
`final-cleanup.json`; the server saved every dimension. No live handoff remains.

Original Wing `be57a955-c4f0-40a0-ae5b-e1fc60f470e9`, moving assembly
`6069540e-d2bc-4f2d-81b7-ad5fd2893311`. Real 48-block bed translates eight blocks.
The Creative GameTest fixture supplies geometry/equipment; this is normal
operation and rider synchronization evidence, not survival construction.

## Observed sequence (UTC; server log uses UTC−7)

- 08:54:08 normal right-click boards local seat (4,1,0); server assigns Dev.
  Normal panel closes restraints. Observer normally Shift-right-clicks the
  source LS controller, which reserves this original chassis.
- 08:56:21 normal Capture suspends the old LS token at epoch 2. Normal Move
  starts physical translation; `moving.json` records MOVING.
- Removing the stationary motor at (-435,130,-420), a disclosed failure
  injection, produces FAULT_HOLD. Rider remains closed and held.
- Both clients quit and the server saves/stops successfully. All three game
  processes restart with unchanged classes/resources/input JAR hashes in
  `cold-artifact-hashes.json.gz`. Dev rejoins the same original car and seat.
  The transient seat entity is rebuilt; the saved assignment is preserved.
  Position changes only 0.000014906 blocks across restart. `cold-rejoined.json`
  retains closed restraints and suspended original LS ownership. Native moving
  state requires explicit Resume; restart does not dispatch.
- Restore the same fixed motor (64 RPM), then normal Resume completes physical
  movement. Normal Release starts receiving service with original authority held.
- Install the disclosed fixed receiver/downstream fixture. Normal receiver arm
  at 09:01:11 assigns a fresh epoch-4 owner; `receiver-armed.json` shows held
  authority and service dispatched=false. Arming does not launch.
- At 09:04:56 a normal top-input lever rising edge launches via actual LS drive.
  The earlier side lever correctly did not request launch: LS reads its TOP
  redstone input. A separate low then high edge at 09:05:54 authorizes the Tire
  exit transfer. Original Wing moves onto downstream fixed track; at 09:06:16
  `departed.json` has no moving service cars and the downstream epoch-5 owner.
- Normal Stop, Open restraints and voluntary Shift dismount complete the cycle
  at 09:06:53. `unloaded.json` has open restraints, no riders and no vehicle
  fault. Dev has no RootVehicle, position (-415.5,130,-374.5). A disclosed fixed
  stone platform supports dismount; the rider was not teleported off the train.

`console.jsonl.gz` retains commands/readbacks; the server and both client logs
are preserved. Saved FoundationData snapshots and selected screenshots accompany
this record. Full input/screenshot sequence, both attempts, exact launch scripts,
class tree and saved world `server/world-occupied2` remain at
`/mnt/NVME4/fourthdimension-runs/fd16-occupied-20261127`.

The first setup completed its GameTest before players arrived. GameTest cleanup
removed forced chunks; its later drift/mount failure is preserved as a fixture
diagnostic, not accepted general chunk-reload behavior. The corrected run starts
with players present and restores forced chunks after fixture cleanup. Camera
turning and reachable vanilla use were verified with fixed default bindings and
raw input; actual mod actions have corresponding server/state readbacks.

This run uses the frozen compile-8 LS input `fd16-20261127/inputs/launch-systems.jar`.
Later Arrow-only deep-mount changes do not change Wing's ordinary three-block
mounting path. The final package reconciliation must retain that distinction;
this record is not execution on the later LS candidate. Native force measurement
and stale/competing request coverage are in the sibling native record.
