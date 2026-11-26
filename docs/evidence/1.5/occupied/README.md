# Occupied moving-track translation and cold recovery

Owner: root coordinator on primary main, base f85f4a277c7c7542a4f07a158f18f580beb41187. This is not FD2001 acceptance.

Current result: attempt 5 closes the occupied translation, power-loss hold, actual
cold-process restoration, explicit Resume, Release, fixed-track departure and
normal unload cases. Earlier failed attempts below remain diagnostic history.
Distinct occupied yaw/drop/pitch observations are recorded separately.

## Attempt 1 — occupied admission failure

Persistent run `/mnt/NVME4/fourthdimension-runs/fd15-occupied-20261122`; server25821/RCON25822, rider Dev display351, operator Observer display352. Java21, each client1536M heap and6GiB Minecraft-only service; dedicated server4GiB scope. Artifact `ff1b8a048a5f28091c911652c1b36d7c500e4e1decb381680532caed84024d67`. Frozen JAR and server/client logs preserved under `evidence/attempt-1` in that run. Xvfb/client/server units verified inactive after server saved all dimensions.

Both real clients joined and independently demonstrated camera motion plus normal reachable stone placement using the private display helper. Disclosed setup supplied controller/Create64RPM motor, endpoint anchors, exact ingredients and player positions. Operator used normal Survival panel Preview, Validate and Build; before/after inventory proves spending51 beds,8 guides,6 locks,8 sockets and16 Wing inserts. Track approach connections and native Wing spawn were disclosed fixture setup, not normal construction evidence.

Dev mounted outer Wing seat by normal right-click at21:36:20Z (FD_BOARDING_EVENT/ASSIGN), then closed restraints through seated Rotation Tool controls. Observer selected destination end and pressed Capture. It correctly remained DOCKED with no retained cars, but incorrectly refused the occupied train as exceeding16 blocks. Raw `captured-inspect.txt` and console/input JSONL retain state. Server Dev Pos=[7.5000001825,128.9375141225,3.5] is world coordinates; SeatEntity is in native plot coordinates20481036,129,20483080. Admission applied the native transform again to the rider AABB. Coordinate conversion repair is required before repeating this exact case. This attempt earns no occupied-motion pass.

The Material list screen also blurred its own text through inherited background rendering; Bill/Guide/Socket UI repair pending the next packaged check. Main panel labels truncated at854x480 but the actions used above were identifiable. The final machinery saddle geometry was not included in this frozen artifact and has no visual acceptance from this run.

## Attempt 2 — cold dock support defect

Package `afcf968faba2ac99a30d19acc06653c83e1f415de30c411b9968cab71259ac05` includes rider bounds and UI fixes. Reload restored Dev to the original seat with closed restraints, but the previously uncaptured Wing lost fixed rail support when assembly restoration removed its bridge and fell to the ground. `restart-2-inspect.txt`, raw client/server logs and frozen JAR in `evidence/attempt-2` preserve the defect. Empty-body bookkeeping was incorrectly treated as physically empty track. Actual bed origin also shifted0.1875 vertically across this asset-changing restart, so normal Return refused it as outside the motion path; native COM/restore diagnosis remains required. No occupied movement or restart acceptance is claimed. Ordinary Open restraints refused the unaligned grounded car, preserving its guard. All five root run services now inactive; server saved all dimensions cleanly. World and logs preserved.

The independent released-service persistence fixture passed1/1 on Java21 in `/mnt/NVME4/fourthdimension-runs/fd15-root-20261122/service-persistence-1.log` (clean saved shutdown, exit0). It checks live versus disk dispatch, retained parent/child local frames, occupied receiving envelope and corrupt hierarchy refusal; it is in-process serialization, not cold physical restoration.


## Attempt 3 — occupied translation and power hold pass; cold child frame defect

Fixed package `375cd42d9ec16b5451c1366b9b1bde9c2509a288feb0cd70b9d6e1324d191008`, new preserved `world-stable-3`, same private displays/ports and limits. Build log `prepare-4.log`; frozen JAR and before/cold logs in `evidence/attempt-3`. No source/artifact change occurred across its restart.

Normal Survival Preview/Validate/Build created bed341fc0a8-fc2e-4796-b62e-565aeea3d548 and assembly4245a3ea-9659-444c-891f-329f8085614e. Disclosed approach/native Wing setup created chassis d817fdf2-8152-4310-9c80-136396c2f9a3. Dev normally mounted and closed restraints. Capture first correctly rejected the temporary boarding platform block7,127,1; removing that disclosed setup block cleared the sweep, and normal Capture succeeded. This closes the rider world/plot AABB defect from attempt1.

Normal Move completed4m to the locked destination. `captured.txt` and `destination.txt` preserve the same bed/chassis/rider assignment, SeatEntity137cce54-e4c0-4ceb-ac70-e697ee566362 and closed restraints. Seat world position changed x7.5→11.5 with unchanged height; second real Observer rendered seated Dev at the destination, and rider view showed the translated approach. The file named `moving.txt` was collected after arrival and must not be represented as an intermediate sample.

Normal Return started; `return-moving.txt` records actual MOVING before disclosed drive removal at-4,128,3. `power-hold.txt` records FAULT_HOLD, zero native velocity, original rider seat, closed restraints and bed origin15.1078094443,128.5,3.5. Both clients then exited and the dedicated server saved all dimensions and exited. Same-artifact cold restart restored the rider assignment normally, left FAULT_HOLD/dispatch inhibited and preserved bed origin within0.000041m (no0.1875 shape-change shift).

However, the retained Wing child COM shifted from(14.812,129.421,3.5) to(15.108,129.325,3.5), despite unchanged saved local x=-0.2962961346,y=0.9213104248 and unchanged displayed rotationPoint. The seat moved correspondingly. `cold-hold.txt` and cold logs preserve this separate native child-frame restoration defect. The recreated transient SeatEntity has a different entity UUID; the stable rider-to-seat-block assignment remains the same. Cold retained-pose acceptance remains open; no explicit Resume or final unload was attempted on the failed state. All five root units stopped after clean save; exact world and logs retained for diagnosis.


## Anchor repair and targeted persistence check

The cold defect was traced to binding a capture-time relative COM pose against the child’s current, temporarily different native rotation point. Capture now persists the exact plot-local constraint anchor alongside each body pose; restore uses that saved anchor. Legacy experimental records without anchors preserve identities and fault state, and the adapter refuses movement while securing surviving bodies independently.

`/mnt/NVME4/fourthdimension-runs/fd15-root-20261122/anchor-persistence-1.log` passed1/1 on Java21 with clean saved shutdown. It verifies exact anchor NBT roundtrip, copy isolation, legacy identity/fault preservation, and malformed/nonfinite/orphan anchor refusal. This is serialization evidence; the physical cold restart remains pending.

Attempt4 uses frozen artifact `343579c2c856d5b53c68549b94a1c4adb30edfe1f54d4eaa96cd5269b5f5faf1` and fresh `world-anchors-4` in the existing root private run. Earlier worlds are preserved.

Attempt4’s normal Capture saved the exact child anchor `(20481032.796296302,129.2962962962964,20483080.5)` and normal Move entered MOVING before disclosed power removal. `power-hold.txt` records childCOM(11.638,129.421,3.5), bedCOM(11.934,127.594,3.5), closed restraints and the original rider assignment. Cold readback still shifted the child to(11.934,129.325,3.5) while bed remained stable. Persisting the anchor alone is insufficient; native COM/constraint rebasing is under diagnosis. No cold pass or Resume claimed. Logs and fixed artifact remain under `evidence/attempt-4`; all five root units verified inactive after saved shutdown.

## Attempt 5 — occupied cold recovery and normal departure accepted

2026-11-26, same private run paths, displays, ports and limits; fresh preserved
`world-loading-5`. Frozen JAR SHA-256
`9c5d155d8ac7dafff0429af26b8d0bf1e4c0520b72aceab918d02ed9b863483c`
is retained with `artifact.json`, console/input readbacks and before/after-cold
logs in the run's `evidence/attempt-5/`. `prepare-6.log` records its Java 21 build.
There was no artifact change across the cold restart.

`CarrierRuntime` now performs existing `NativeLoading.update` mass maintenance
for retained carriers before restoring joints. Suppressing family travel must
not suppress the first-load native origin/occupied mass reconciliation. This
completes the anchor repair without moving a vehicle to an invented dock pose.

Normal Survival Preview/Validate/Build, normal boarding and restraint closure,
Capture and Move used real Dev and Observer clients. Disclosed setup supplied
ingredients, controller power, fixed approaches and the original Wing. Assembly
`f0b647e2-a20c-49f9-9a19-8d69974fd272`, bed
`1994f760-b7a7-453c-8112-03027c8ef382` and original Wing
`b48ef337-9a6d-4df5-a577-80a09cede5d7` remain identifiable throughout.
`moving.txt` records actual MOVING before removal of the stationary drive;
`power-hold.txt` records FAULT_HOLD with zero native velocities and closed restraints.

Both clients exited; the dedicated server saved and exited. After same-artifact
cold restart and real client reconnection, `cold-occupied.txt` retains the same
car and Dev's local seat `(4,1,0)`, closed restraints and FAULT_HOLD. Dev's world
position changed from `(7.512877826131257,128.9373844196658,3.4999999999998126)`
to `(7.512878797017015,128.93738450006572,3.4999999999997997)`, a measured
`9.7421e-7` block displacement. Bed and occupied child COM also agree. The
transient SeatEntity was recreated; stable rider/car/seat assignment was not.
`cold-hold.txt` predates client reconnection and is not the occupied comparison.

The first drive restoration fixture wrote the wrong motor fields; normal Resume
correctly refused absent actual Create power. Replacing the disclosed motor with
verified 64 RPM fixed that setup. Normal Resume at 18:05:43Z completed four-block
travel to RECONNECTED (`resumed-destination.txt`). A stale Release token was
correctly refused; normal Refresh and Release at 18:07:07Z entered RELEASING.
The earlier `resume-destination.txt` and `released.txt` belong to those refused
attempts, not the successful readbacks.

An actual native Chain span and fixed motor were then supplied through disclosed
NBT/world fixture setup (`chain-fixture.jsonl`), not claimed as normal Chain
construction. Normal rider Dispatch moved the original occupied Wing from the
bed onto the fixed departure, ending at z=32.972 (`chain-departure.txt`). Normal
Stop, normal restraint Open and voluntary Shift dismount completed at 18:09:38Z.
`unloaded.txt` has the original car, empty rider assignment, open restraints,
no fault and Dev at `(11.5,128,32.172095477581024)` without a RootVehicle.
A disclosed fixed platform supported the exit; no teleport performed it.

The table correctly retains its receiving reservation while the released train
still occupies the reserved fixed span ending at z=35. RELEASING in this state
is not a leaked car-service owner: ordinary fixed-track Stop/Open/unload works.
The fixture does not claim a cleared receiving span or empty return afterward.

Final server save reports all dimensions saved. All five exact client/server/
Xvfb units were independently verified inactive with MainPID=0; `cleanup.json`
records that check. The saved world, frozen package, failed attempts, screenshots
and raw logs are preserved. No broader family/profile or rider-comfort claim is made.
